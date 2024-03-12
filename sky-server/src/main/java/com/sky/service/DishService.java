package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    void saveWithFlavor(DishDTO dishDTO);

    /**
     * 分页查询
     * @param queryDTO
     */
    PageResult queryPage(DishPageQueryDTO queryDTO);

    void deletDishsById(List<Long> ids);

    DishVO getById(Long id);

    /**
     * 修改菜品信息
     * @param dishDTO
     */
    void updateDish(DishDTO dishDTO);
}
