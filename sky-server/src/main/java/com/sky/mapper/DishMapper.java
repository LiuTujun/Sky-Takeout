package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(OperationType.INSERT) // 自动填充公共属性
    void insert(Dish dish);

    Page<DishVO> query(DishPageQueryDTO queryDTO);

    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    @Delete("delete from dish where id=#{id}")
    void deleteById(Long id);

    /**
     * 批量删除
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    List<Dish> getByIds(List<Long> ids);

    /**
     * 修改菜品信息
     * @param dish
     */
    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据分类Id查询菜品
     * @param dish
     * @return
     */
    @Select("select * from dish where category_id=#{categoryId}")
    List<Dish> list(Dish dish);

    /**
     * 改变菜品的起售 停售状态
     * @param dishId
     * @param dishStatus
     */
    @Update("update dish set status=#{dishStatus} where id=#{dishId}")
    void switchStatus(Long dishId, Integer dishStatus);

    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
