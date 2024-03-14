package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜品管理
 */
@Api(tags = "菜品表单")
@Slf4j
@RestController
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 清除redis缓存
     * @param pattern ： redis key的模式
     */
    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }


    @PostMapping
    @ApiOperation("新增菜品及相应的口味")
    public Result saveWithFlavor(@RequestBody DishDTO dishDTO){
        log.info("新增菜品， {}", dishDTO);

        // 新增菜品 清除对应分类的缓存信息
        String key = "dish_" + dishDTO.getCategoryId();
        cleanCache(key);

        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询菜品信息")
    public Result<PageResult> queryPage(DishPageQueryDTO queryDTO){
        log.info("分页查询菜品信息， {}", queryDTO);

        PageResult pageResult = dishService.queryPage(queryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("删除菜品")
    public Result deletDishsById(@RequestParam List<Long> ids){
        log.info("删除菜品信息， {}", ids);

        // 可能影响多个 清除所有分类的缓存信息
        cleanCache("dish_*");

        dishService.deletDishsById(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据菜品ID获取信息")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据菜品ID获取信息， {}", id);

        DishVO dishVO = dishService.getById(id);
        return Result.success(dishVO);
    }

    @GetMapping("/list")
    @ApiOperation("根据分类ID获取菜品信息")
    public Result<List<Dish>> getByCategoryId(String categoryId){
        log.info("根据分类ID获取菜品信息， {}", categoryId);

        List<Dish> dishes = dishService.getByCategoryId(categoryId);
        return Result.success(dishes);
    }

    @PutMapping
    @ApiOperation("修改菜品信息")
    public Result updateDish(@RequestBody DishDTO dishDTO){
        log.info("修改菜品信息， {}", dishDTO);

        // 可能影响多个 清除所有分类的缓存信息
        cleanCache("dish_*");

        dishService.updateDish(dishDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售、停售")
    public Result switchStatus(@PathVariable String status, String id){
        log.info("{}菜品起售、停售， {}", id, status);

        // 可能影响多个 清除所有分类的缓存信息
        cleanCache("dish_*");

        dishService.switchStatus(id, status);
        return Result.success();
    }
}
