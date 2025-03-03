package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Employee;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.DishNameRepeatException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类业务层
 */
@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 新增菜品实现
     *
     * @param dishDTO
     */
    @Override
    // 因为要操作两张表，所以要添加事务，保证原子性操作
    @Transactional
    public void addDish(DishDTO dishDTO) {
        // 检查菜品名是否重复
        if (dishMapper.getDishByName(dishDTO.getName()) != null)
            throw new DishNameRepeatException("菜品名称已存在");
        // 向dish表中插入记录
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.addDish(dish);

        // 获取返回的菜品id
        Long dishId = dish.getId();

        // 向dish_flavor表批量插入记录
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() != 0) {
            // 设置菜品id
            flavors.forEach(item -> item.setDishId(dishId));
            // 批量插入
            dishFlavorMapper.addBatch(flavors);
        }
    }

    /**
     * 菜品分页实现
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        // PageHelper
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQurey(dishPageQueryDTO);

        long total = page.getTotal();
        List<DishVO> records = page.getResult();
        return new PageResult(total, records);
    }

    /**
     * 删除菜品实现
     *
     * @param ids
     */
    @Override
    // 因为要操作两张表，所以要添加事务，保证原子性操作
    @Transactional
    public void batchDeleteDishes(String ids) {
        // 剪切字符串，获取单独的id
        String[] arr = ids.split(",");
        ArrayList<Long> idList = new ArrayList<>();
        // 字符串 -> Integer
        for (int i = 0; i < arr.length; i++) {
            Long id = Long.parseLong(arr[i]);
            // 正在启售的菜品不能被删除
            if (dishMapper.getStatusById(id).equals(StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
            // 关联了套餐的菜品不能被删除
            if (!dishMapper.getSetmealId(id).equals(0L)) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
            // 满足以上条件再加入idList中
            idList.add(id);
        }
        // 批量删除菜品
        dishMapper.batchDeleteDishes(idList);
        // 删除该菜品后，要把该菜品具有的口味同时删除
        dishFlavorMapper.batchDeleteDishFlavores(idList);
    }

    /**
     * 修改菜品
     *
     * @param dishVO
     */
    @Override
    @Transactional
    public void updateDish(DishVO dishVO) {
        // 检查菜品名是否重复
        if (dishMapper.getDishByName(dishVO.getName()) != null)
            throw new DishNameRepeatException("菜品名称已存在");
        // 对菜品表进行更新操作
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishVO, dish);
        dishMapper.update(dish);

        // 先将该菜品的所有口味全部删除
        dishFlavorMapper.deleteDishFlavores(dishVO.getId());

        // 再对口味表进行更新操作
        List<DishFlavor> flavors = dishVO.getFlavors();
        Long dishId = dishVO.getId();
        if (flavors != null && flavors.size() != 0) {
            // 设置菜品id
            flavors.forEach(item -> item.setDishId(dishId));
            // 批量插入
            dishFlavorMapper.addBatch(flavors);
        }
    }

    /**
     * 获取DishVO对象
     *
     * @param id
     * @return
     */
    @Override
    public DishVO getDishVOById(Long id) {
        // 需要获取菜品的信息
        Dish dish = dishMapper.getDishById(id);

        // 需要获取菜品所在分类的id和name
        String categoryName = categoryMapper.getCategoryNameById(dish.getCategoryId());

        // 需要获取该菜品的口味
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(dish.getId());

        // 设置菜品的信息
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);

        // 设置菜品所在分类的name
        // 前端已经维护好了 分类id--分类名称 的键值对，所以给不给categoryName没有影响
//        dishVO.setCategoryName(categoryName);

        // 设置菜品口味
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }
}
