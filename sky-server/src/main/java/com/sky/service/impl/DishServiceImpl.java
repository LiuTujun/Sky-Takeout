package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.FlavorMapper;
import com.sky.mapper.SetmealFishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private FlavorMapper flavorMapper;

    @Autowired
    private SetmealFishMapper setmealFishMapper;


    /**
     * 保存菜品及对应的口味
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        //保存菜品
        Dish dish = BeanUtil.copyProperties(dishDTO, Dish.class);
        dishMapper.insert(dish);

        // 获取菜品的id
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();

        if(flavors != null && !flavors.isEmpty()){
            flavors.stream().forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            // 批量插入
            flavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param queryDTO
     * @return
     */
    @Override
    public PageResult queryPage(DishPageQueryDTO queryDTO) {
        // 构建分页插件
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());

        // 查询分页结果
        Page<DishVO> queryPage = dishMapper.query(queryDTO);

        return PageResult.builder()
                .total(queryPage.getTotal())
                .records(queryPage.getResult()).build();
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void deletDishsById(List<Long> ids) {
        // 判断是否有菜品在起售中
        //for (Long id: ids){
        //    Dish dish = dishMapper.getById(id);
        //    if(Objects.equals(dish.getStatus(), StatusConstant.ENABLE)){
        //        throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        //    }
        //}
        // 优化sql
        List<Dish> dishes = dishMapper.getByIds(ids);
        List<Dish> dishes1 = dishes.stream().filter(dish -> Objects.equals(dish.getStatus(), StatusConstant.ENABLE)).collect(Collectors.toList());
        if(!dishes1.isEmpty()){
            log.info("起售中的菜品是：{}", dishes1);
            throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }

        // 被关联的套餐不能删除
        List<Long> longs = setmealFishMapper.queryBatch(ids);
        if(longs != null && !longs.isEmpty()){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 删除菜品信息
        //for (Long id: ids){
        //    dishMapper.deleteById(id);
        //    // 删除口味信息
        //    flavorMapper.deleteByDishId(id);
        //}

        // 优化SQL 批量删除
        dishMapper.deleteByIds(ids);
        flavorMapper.deleteByDishIds(ids);
    }

    /**
     *根据ID获取菜品信息
     * @param id
     * @return
     */
    @Override
    public DishVO getById(Long id) {
        // 获取菜品信息
        Dish dish = dishMapper.getById(id);

        // 获取菜品口味
        List<DishFlavor> flavorList = flavorMapper.getById(id);

        // 封装成VO
        DishVO dishVO = BeanUtil.copyProperties(dish, DishVO.class);
        dishVO.setFlavors(flavorList);
        return dishVO;
    }

    @Override
    public void updateDish(DishDTO dishDTO) {
        // 修改菜品的信息
        Dish dish = BeanUtil.copyProperties(dishDTO, Dish.class);
        dishMapper.update(dish);

        // 修改口味的信息
        // 删除所有之前的口味
        Long dishId = dish.getId();
        flavorMapper.deleteByDishId(dishId);

        // 添加新的口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()){
            flavors.forEach(flavor ->{
                flavor.setDishId(dishId);
            });

            flavorMapper.insertBatch(flavors);
        }
    }
}
