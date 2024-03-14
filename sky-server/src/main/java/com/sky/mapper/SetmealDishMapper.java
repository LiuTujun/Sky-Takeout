package com.sky.mapper;


import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 查询是否有菜品与套餐仍然进行关联
     */
    List<Long> queryBatch(List<Long> dishIds);

    /**
     * 批量插入套餐表
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 查询对应套餐的所有菜品
     * @param setmeanId
     */
    @Select("select * from setmeal_dish where setmeal_id=#{setmeanId}")
    List<SetmealDish> getBySetmealId(Long setmeanId);

    /**
     * 删除指定套餐id的菜品
     * @param id
     */
    @Delete("delete from setmeal_dish where setmeal_id=#{id}")
    void deleteById(Long id);
}
