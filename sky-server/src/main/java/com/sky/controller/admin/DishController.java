package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping
    @ApiOperation("保存菜品及相应的口味")
    public Result saveWithFlavor(@RequestBody DishDTO dishDTO){
        log.info("保存菜品， {}", dishDTO);

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

    @PutMapping
    @ApiOperation("修改菜品信息")
    public Result updateDish(@RequestBody DishDTO dishDTO){
        log.info("修改菜品信息， {}", dishDTO);

        dishService.updateDish(dishDTO);
        return Result.success();
    }
}
