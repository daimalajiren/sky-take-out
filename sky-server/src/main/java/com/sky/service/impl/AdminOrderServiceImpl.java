package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.result.PageResult;
import com.sky.service.AdminOrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AdminOrderServiceImpl implements AdminOrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("订单搜索：{}",ordersPageQueryDTO);
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList();

        // 查询出订单明细，并封装入OrderVO进行响应
        if (page != null && page.getTotal() > 0) {
            for (Orders orders : page) {
                Long orderId = orders.getId();// 订单id

                // 查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);

                list.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), list);
    }
/**
     * 订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO getorderDetailById(Long id) {
        Orders orders = orderMapper.getorderDetailById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        List<OrderDetail> orderDetailList = null;
        if (orderVO != null) {
            orderDetailList = orderDetailMapper.getByOrderId(id);

        }
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        List<Map<String, Object>> countsOrder = orderMapper.countsOrder();
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();

        if (countsOrder != null && !countsOrder.isEmpty()) {
            for (Map<String, Object> map : countsOrder) {
                Object countObj = map.get("count_num");
                Object statusObj = map.get("status");

                if (countObj != null && statusObj != null) {
                    Integer count = countObj instanceof Integer ?
                            (Integer) countObj : Integer.parseInt(countObj.toString());
                    Integer status = statusObj instanceof Integer ?
                            (Integer) statusObj : Integer.parseInt(statusObj.toString());

                    if (Orders.TO_BE_CONFIRMED.equals(status)) {
                        orderStatisticsVO.setToBeConfirmed(count);
                    } else if (Orders.CONFIRMED.equals(status)) {
                        orderStatisticsVO.setConfirmed(count);
                    } else if (Orders.DELIVERY_IN_PROGRESS.equals(status)) {
                        orderStatisticsVO.setDeliveryInProgress(count);
                    }
                }
            }
        }

        return orderStatisticsVO;
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     * @return
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        log.info("接单：{}",ordersConfirmDTO);
        ordersConfirmDTO.setStatus(Orders.CONFIRMED);
        orderMapper.updateStatus(ordersConfirmDTO);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     * @return
     */
    @Override
    public void reject(OrdersRejectionDTO ordersRejectionDTO) {
        log.info("拒单：{}",ordersRejectionDTO);
        //TODO 退款
        orderMapper.updateStatusAndRejectionReason(ordersRejectionDTO);
    }
/**
     * 派送订单
     * @param id
     * @return
     */
    @Override
    public void delivery(Long id) {
        OrdersConfirmDTO ordersConfirmDTO = new OrdersConfirmDTO();
        ordersConfirmDTO.setId(id);
        ordersConfirmDTO.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.updateStatus(ordersConfirmDTO);
    }
/**
     * 完成订单
     * @param id
     * @return
     */
    @Override
    public void complete(Long id) {
        OrdersConfirmDTO ordersConfirmDTO = new OrdersConfirmDTO();
        ordersConfirmDTO.setId(id);
        ordersConfirmDTO.setStatus(Orders.COMPLETED);
        orderMapper.updateStatus(ordersConfirmDTO);
        orderMapper.updateDeliveryTime(id, LocalDateTime.now());

    }


}
