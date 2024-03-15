package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShopCartService {
    /**
     * 将菜品或套餐数据插入购物车表单
     * @param shoppingCartDTO
     */
    void insertShopCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 展示当前用户的购物车
     * @return
     */
    List<ShoppingCart> showShopCart();

    /**
     * 清空购物车
     */
    void cleanShopCart();

    /**
     * 删除购物车中的一个商品
     * @param shoppingCartDTO
     */
    void subShopCart(ShoppingCartDTO shoppingCartDTO);
}
