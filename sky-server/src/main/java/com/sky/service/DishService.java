package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
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

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);

    /**
     * 根据分类id获取该分类下的所有菜品
     * @param id
     */
    List<Dish> getByCategoryId(String id);
}
