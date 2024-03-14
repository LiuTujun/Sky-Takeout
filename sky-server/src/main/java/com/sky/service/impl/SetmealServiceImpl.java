package com.sky.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐业务实现
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        return setmealMapper.list(setmeal);
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

    /**
     * 新增套餐功能
     *
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void addSetmeal(SetmealDTO setmealDTO) {
        //拷贝属性
        Setmeal setmeal = BeanUtil.copyProperties(setmealDTO, Setmeal.class);
        // 1 插入套餐表
        setmealMapper.insert(setmeal);

        // 2 插入套餐-菜品关系表
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //设定套餐中菜品的套餐id
        setmealDishes = setmealDishes.stream().peek(setmealDish -> setmealDish.setSetmealId(setmeal.getId())).collect(Collectors.toList());
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 根据id查询套餐 及其对应的菜品
     *
     * @param id
     */
    @Override
    public SetmealVO getSetmealById(String id) {
        Long setmeanId = Long.parseLong(id);

        // 查询对应的套餐信息
        Setmeal setmeal = setmealMapper.getById(setmeanId);

        // 查询对应套餐的所属所有菜品
        List<SetmealDish> bySetmealId = setmealDishMapper.getBySetmealId(setmeanId);

        SetmealVO setmealVO = BeanUtil.copyProperties(setmeal, SetmealVO.class);
        setmealVO.setSetmealDishes(bySetmealId);
        return setmealVO;
    }

    /**
     * 分页查询 查询所有的套餐信息
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult queryPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        // 分页查询所有套餐信息
        Page<SetmealVO> setmeals = setmealMapper.queryPage(setmealPageQueryDTO);

        //设置所有套餐的相关菜品信息
        for(SetmealVO setmealVO: setmeals){
            // 查询对应套餐的所属所有菜品
            List<SetmealDish> bySetmealId = setmealDishMapper.getBySetmealId(setmealVO.getId());
            setmealVO.setSetmealDishes(bySetmealId);
        }

        // 构建pageResult
        PageResult pageResult = PageResult.builder()
                .total(setmeals.getTotal())
                .records(setmeals.getResult()).build();

        return pageResult;
    }

    /**
     * 修改套餐信息
     *
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void updateSetmeal(SetmealDTO setmealDTO) {
        //拷贝属性
        Setmeal setmeal = BeanUtil.copyProperties(setmealDTO, Setmeal.class);
        // 1 更新套餐信息
        setmealMapper.update(setmeal);

        // 2 更新套餐-菜品关系表
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        // 2.1 删除所有原始套餐表中相关联的菜品
        setmealDishMapper.deleteById(setmealDTO.getId());
        // 2.2 更新新的套餐表
        //设定套餐中菜品的套餐id
        setmealDishes = setmealDishes.stream().peek(setmealDish -> setmealDish.setSetmealId(setmeal.getId())).collect(Collectors.toList());
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 更改指定套餐的销售状态
     *
     * @param id
     * @param status
     */
    @Override
    public void switchStatus(String id, String status) {
        Long setmealId = Long.parseLong(id);
        Integer setmealStatus = Integer.parseInt(status);
        setmealMapper.updateStatus(setmealId, setmealStatus);
    }

    /**
     * 根据套餐id批量删除
     *
     * @param ids
     */
    @Override
    public void deleteBatch(List<Long> ids) {
        setmealMapper.deleteBatch(ids);
    }
}
