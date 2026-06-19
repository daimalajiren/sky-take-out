package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.UserService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;
    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        log.info("营业额统计：{}到{}", begin, end);
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(begin.isBefore(end))
        {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        String join = StringUtil.join(",", dateList);
        List<Double> turnoverList = new ArrayList<>();
        for(LocalDate date : dateList)
        {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnoverList.add(turnover == null ? 0.0 : turnover);
        }
        String join1 = StringUtil.join(",", turnoverList);

        return TurnoverReportVO.builder()
                .dateList(join)
                .turnoverList(join1)
                .build();
    }
/**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        log.info("用户统计：{}到{}", begin, end);
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(begin.isBefore(end))
        {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
            String join = StringUtil.join(",", dateList);

            List<Integer> totalUserList = new ArrayList<>();
            for(LocalDate date : dateList) {
                LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
                LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
                Map map = new HashMap();
                map.put("end", endTime);
                Integer totalUser = userMapper.countByMap(map);
                totalUserList.add(totalUser);
            }
                String join2 = StringUtil.join(",", totalUserList);

        List<Integer> newUserList = new ArrayList<>();
        for(LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("end", endTime);
            map.put("begin", beginTime);
            Integer newUser = userMapper.countByMap(map);
            newUserList.add(newUser);
        }


        String join1 = StringUtil.join(",", newUserList);

        return UserReportVO.builder()
                .dateList(join)
                .newUserList(join1)
                .totalUserList(join2)
                .build();
    }
/**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        log.info("订单统计：{}到{}", begin, end);
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(begin.isBefore(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        String join = StringUtil.join(",", dateList);
        Integer totalOrderCount = 0;
        Integer totalvalidOrderCount = 0;
        Double orderCompletionRate = 0.0;
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        for(LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            Integer orderCount = orderMapper.countByMap(map);
            orderCountList.add(orderCount);
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.countByMap(map);
            validOrderCountList.add(validOrderCount);
        }
        for(int i = 0; i < orderCountList.size(); i++)
        {
            totalOrderCount += orderCountList.get(i);
            totalvalidOrderCount += validOrderCountList.get(i);
        }
//        totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
//        totalvalidOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        if(totalOrderCount != 0) {
            orderCompletionRate = totalvalidOrderCount.doubleValue() / totalOrderCount;
        }
        String join1 = StringUtil.join(",", orderCountList);
        String join2 = StringUtil.join(",", validOrderCountList);
        return OrderReportVO.builder()
                .validOrderCount(totalvalidOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .totalOrderCount(totalOrderCount)
                .dateList(join)
                .orderCountList(join1)
                .validOrderCountList(join2)
                .build();
    }
/**
     * 查询销量排名top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        log.info("查询top10商品：{}到{}", begin, end);
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String join = StringUtil.join(",", names);
        String join1 = StringUtil.join(",", numbers);
        return SalesTop10ReportVO.builder()
                .nameList(join)
                .numberList(join1)
                .build();
    }
/**
     * 导出营业数据
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) throws IOException {
        LocalDate Begin = LocalDate.now().minusDays(30);
        LocalDate End = LocalDate.now().minusDays(1);
        LocalDateTime timeBegin = LocalDateTime.of(Begin, LocalTime.MIN);
        LocalDateTime timeEnd = LocalDateTime.of(End, LocalTime.MAX);
        BusinessDataVO businessData = workspaceService.getBusinessData(timeBegin, timeEnd);

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        XSSFWorkbook excel = new XSSFWorkbook(in);
        XSSFSheet sheet1 = excel.getSheet("Sheet1");
        sheet1.getRow(1)
                .getCell(1)
                .setCellValue("时间：" + timeBegin + "至" + timeEnd);
        sheet1.getRow(3)
                .getCell(2)
                .setCellValue(businessData.getTurnover());
        sheet1.getRow(3)
                .getCell(4)
                .setCellValue(businessData.getOrderCompletionRate());
        sheet1.getRow(3)
                .getCell(6)
                .setCellValue(businessData.getNewUsers());
        sheet1.getRow(4)
                .getCell(2)
                .setCellValue(businessData.getValidOrderCount());
        sheet1.getRow(4)
                .getCell(4)
                .setCellValue(businessData.getUnitPrice());


        for (int i = 0; i < 30; i++) {
            LocalDate date = Begin.minusDays(i);
            LocalDateTime localDateTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime localDateTime1 = LocalDateTime.of(date, LocalTime.MAX);
            BusinessDataVO businessData1 = workspaceService.getBusinessData(localDateTime, localDateTime1);
            XSSFRow row = sheet1.getRow(7 + i);
            row.getCell(1)
                    .setCellValue(date.toString());
            row.getCell(2)
                    .setCellValue(businessData1.getTurnover());
            row.getCell(3)
                    .setCellValue(businessData1.getValidOrderCount());
            row.getCell(4)
                    .setCellValue(businessData1.getOrderCompletionRate());
            row.getCell(5)
                    .setCellValue(businessData1.getUnitPrice());
            row.getCell(6)
                    .setCellValue(businessData1.getNewUsers());
        }

        ServletOutputStream outputStream = response.getOutputStream();
        excel.write(outputStream);
        outputStream.close();
        excel.close();
    }
}
