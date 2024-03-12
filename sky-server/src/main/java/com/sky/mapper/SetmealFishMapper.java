package com.sky.mapper;


import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealFishMapper {
    /**
     * 查询是否有菜品与套餐仍然进行关联
     */
    List<Long> queryBatch(List<Long> dishIds);
}
