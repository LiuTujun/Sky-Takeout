package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShopCartMapper {
    /**
     * 条件查询 购物车
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 更新购物车数量
     * @param shoppingCart1
     */
    @Update("update shopping_cart set number=#{number} where id=#{id}")
    void updateNumber(ShoppingCart shoppingCart1);

    /**
     * 将当前数据插入购物车
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart(name, image, user_id, dish_id, setmeal_id, dish_flavor, amount, create_time) " +
            "value (#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{amount},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 清空购物车
     * @param currentId : 当前用户的Id
     */
    @Delete("delete from shopping_cart where user_id=#{currentId}")
    void deleteByUserId(Long currentId);

    /**
     * 删除一条数据
     * @param Id :
     */
    @Delete("delete from shopping_cart where id=#{Id}")
    void deleteById(Long Id);
}
