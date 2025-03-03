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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品接口-DishController")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 新增菜品接口
     *
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品-addDish")
    public Result addDish(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.addDish(dishDTO);
        return Result.success();
    }

    /**
     * 菜品分页展示
     *
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页-dishPageQuery")
    public Result<PageResult> dishPageQuery(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页参数：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除菜品-batchDeleteDishes")
    public Result batchDeleteDishes(String ids) {
        log.info("要删除的菜品id：{}", ids);
        dishService.batchDeleteDishes(ids);
        return Result.success();
    }

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    private Result<DishVO> getDishById(@PathVariable Long id) {
        log.info("需要查询的菜品id是：{}", id);
        DishVO dishVO = dishService.getDishVOById(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     *
     * @param dishVO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result updateDish(@RequestBody DishVO dishVO) {
        log.info("更新的菜品信息：{}", dishVO);
        dishService.updateDish(dishVO);
        return Result.success();
    }
}
