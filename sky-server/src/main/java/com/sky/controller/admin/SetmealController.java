package com.sky.controller.admin;

import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {
@Autowired
private SetmealService setmealService;

/**
 * 套餐分页查询
 * @param setmealPageQueryDTO
 * @return
 */
@GetMapping("/page")
public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
    log.info("分页查询");
    PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
    return Result.success(pageResult);
}
/**
 * 根据id查询套餐数据
 * @param id
 * @return
 */
    @GetMapping("{id}")
    public Result<SetmealVO> getById(@PathVariable Long id){
        log.info("查询id为{}的套餐数据", id);
        SetmealVO setmealVO = setmealService.getById(id);

        return Result.success(setmealVO);
    }
/**
 * 批量起售停售
 * @param status
 * @param ids
 * @return
 */
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("启用或禁用套餐");
        setmealService.startOrStop(status, id);
        return Result.success();
    }
    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result delete(@RequestParam List<Long> ids){
    log.info("批量删除：{}",ids);
    setmealService.delete(ids);
    return Result.success();
    }
    /**
     * 新增套餐
     * @param setmealVO
     * @return
     */
    @PostMapping
    @CacheEvict(cacheNames = "setmealCache", key="#setmealVO.categoryId")
    public Result save(@RequestBody SetmealVO setmealVO){
        log.info("新增套餐：{}",setmealVO);
        setmealService.save(setmealVO);
        return Result.success();
    }
    /**
     * 修改套餐
     * @param setmealVO
     * @return
     */
    @PutMapping
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result update(@RequestBody SetmealVO setmealVO){
        log.info("修改套餐：{}",setmealVO);
        setmealService.update(setmealVO);
        return Result.success();
    }
}
