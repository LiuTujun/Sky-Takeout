package com.sky.service;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);

    /**
     * 新增套餐功能
     * @param setmealDTO
     */
    void addSetmeal(SetmealDTO setmealDTO);

    /**
     * 根据id查询套餐 及其对应的菜品
     * @param id
     */
    SetmealVO getSetmealById(String id);

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult queryPage(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 修改套餐信息
     * @param setmealDTO
     */
    void updateSetmeal(SetmealDTO setmealDTO);

    /**
     * 更改指定套餐的销售状态
     * @param id
     * @param status
     */
    void switchStatus(String id, String status);

    /**
     * 根据套餐id批量删除
     * @param ids
     */
    void deleteBatch(List<Long> ids);
}
