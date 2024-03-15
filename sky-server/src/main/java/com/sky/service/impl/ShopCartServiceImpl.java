package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShopCartMapper;
import com.sky.service.ShopCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShopCartServiceImpl implements ShopCartService {

    @Autowired
    private ShopCartMapper shopCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 将菜品或套餐数据插入购物车表单
     *
     * @param shoppingCartDTO
     */
    @Override
    public void insertShopCart(ShoppingCartDTO shoppingCartDTO) {
        // 获取当前用户id
        Long currentId = BaseContext.getCurrentId();
        // 判断当前物品是否已加入购物车
        ShoppingCart shoppingCart = BeanUtil.copyProperties(shoppingCartDTO, ShoppingCart.class);
        shoppingCart.setUserId(currentId);
        List<ShoppingCart> list = shopCartMapper.list(shoppingCart);

        // 1 当前菜品已加入购物车
        if(list != null && !list.isEmpty()){
            // 数量加1 不重新插入
            ShoppingCart shoppingCart1 = list.get(0);
            shoppingCart1.setNumber(shoppingCart1.getNumber() + 1);
            shopCartMapper.updateNumber(shoppingCart1);
            return;
        }

        // 2 当前菜品或套餐未加入购物车
        // 2.1 加入的是菜品
        Long dishId = shoppingCartDTO.getDishId();
        if(dishId != null){
            Dish dish = dishMapper.getById(dishId);
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
            shoppingCart.setName(dish.getName());

            shoppingCart.setDishFlavor(shoppingCartDTO.getDishFlavor());
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shopCartMapper.insert(shoppingCart);
            return;
        }

        // 2.2 加入的是套餐
        Long setmealId = shoppingCartDTO.getSetmealId();
        Setmeal setmeal = setmealMapper.getById(setmealId);
        shoppingCart.setImage(setmeal.getImage());
        shoppingCart.setAmount(setmeal.getPrice());
        shoppingCart.setName(setmeal.getName());

        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shopCartMapper.insert(shoppingCart);
    }

    /**
     * 展示当前用户的购物车
     *
     * @return
     */
    @Override
    public List<ShoppingCart> showShopCart() {
        Long currentId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(currentId).build();
        List<ShoppingCart> list = shopCartMapper.list(shoppingCart);
        return list;
    }

    /**
     * 清空购物车
     */
    @Override
    public void cleanShopCart() {
        Long currentId = BaseContext.getCurrentId();
        shopCartMapper.deleteByUserId(currentId);
    }

    /**
     * 删除购物车中的一个商品
     *
     * @param shoppingCartDTO
     */
    @Override
    public void subShopCart(ShoppingCartDTO shoppingCartDTO) {
        // 获取当前用户id
        Long currentId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = BeanUtil.copyProperties(shoppingCartDTO, ShoppingCart.class);
        shoppingCart.setUserId(currentId);
        List<ShoppingCart> list = shopCartMapper.list(shoppingCart);

        // 获得该商品在购物车中的当前数量
        ShoppingCart shoppingCart1 = list.get(0);
        Integer number = shoppingCart1.getNumber();
        // 数量为1 直接删除该条数据
        if(number == 1){
            shopCartMapper.deleteById(shoppingCart1.getId());
            return;
        }

        // 数量不为1 直接数量减一 更新数据
        shoppingCart1.setNumber(number - 1);
        shopCartMapper.updateNumber(shoppingCart1);
    }
}
