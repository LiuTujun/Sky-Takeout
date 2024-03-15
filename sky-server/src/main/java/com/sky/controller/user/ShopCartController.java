package com.sky.controller.user;


import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShopCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "C端-购物车模块")
public class ShopCartController {

    @Autowired
    private ShopCartService shopCartService;

    /**
     * 添加菜品或者套餐到购物车
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    @ApiOperation("添加菜品或者套餐到购物车")
    public Result addShopCart(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加菜品或者套餐到购物车: {}", shoppingCartDTO);
        shopCartService.insertShopCart(shoppingCartDTO);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCart>> showShopCart(){
        log.info("查看购物车");

        List<ShoppingCart> list = shopCartService.showShopCart();
        return Result.success(list);
    }

    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result cleanShopCart(){
        log.info("清空购物车");

        shopCartService.cleanShopCart();
        return Result.success();
    }

    @PostMapping("/sub")
    @ApiOperation("删除购物车中一个商品")
    public Result subShopCart(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("删除购物车中一个商品: {}", shoppingCartDTO);

        shopCartService.subShopCart(shoppingCartDTO);
        return Result.success();
    }
}
