package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 口味 Mapper
 */

@Mapper
public interface FlavorMapper {
    /**
     * 批量插入
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    @Delete("delete from dish_flavor where dish_id=#{dishId}")
    void deleteByDishId(Long dishId);

    /**
     * 批量删除口味表
     * @param ids
     */
    void deleteByDishIds(List<Long> ids);

    /**
     * 根据菜品ID查询对应口味
     * @param dishId
     * @return
     */
    @Select("select * from dish_flavor where dish_id=#{dishId}")
    List<DishFlavor> getById(Long dishId);

    ///**
    // * 修改口味信息
    // * @param dishDTO
    // */
    //void update(DishDTO dishDTO);
}
