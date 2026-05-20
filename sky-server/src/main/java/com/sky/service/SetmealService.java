package com.sky.service;

import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    SetmealVO getById(Long id);

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void startOrStop(Integer status, Long id);

    void delete(List<Long> ids);

    void save(SetmealVO setmealVO);

    void update(SetmealVO setmealVO);
}
