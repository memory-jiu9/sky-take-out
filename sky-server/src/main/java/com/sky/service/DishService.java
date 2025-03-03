package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品
     * @param dishDTO
     */
    void addDish(DishDTO dishDTO);

    /**
     * 菜品分页
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 删除菜品
     * @param ids
     */
    void batchDeleteDishes(String ids);

    /**
     * 修改菜品
     * @param dishVO
     */
    void updateDish(DishVO dishVO);

    /**
     * 获取DishVO对象
     * @param id
     * @return
     */
    DishVO getDishVOById(Long id);
}
