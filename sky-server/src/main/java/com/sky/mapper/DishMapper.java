package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     *
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 根据名字查询记录
     *
     * @param name
     * @return
     */
    @Select("select * from dish where name = #{name}")
    Dish getDishByName(String name);

    /**
     * 插入菜品记录
     *
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    void addDish(Dish dish);

    /**
     * 菜品分页
     *
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQurey(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     *
     * @param idList
     */
    void batchDeleteDishes(ArrayList<Long> idList);

    /**
     * 根据菜品id获取菜品状态：启售，停售
     *
     * @param id
     * @return
     */
    @Select("select status from dish where id = #{id};")
    Integer getStatusById(Long id);

    /**
     * 判断是否绑定套餐
     * @param id
     * @return
     */
    @Select("select count(*) from dish, setmeal_dish where dish.id = #{id} and dish.id = setmeal_dish.dish_id")
    Long getSetmealId(Long id);

    /**
     * 更新菜品信息
     * @param dish
     */
    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据id获取菜品信息
     * @param id
     */
    @Select("select * from dish where id = #{id}")
    Dish getDishById(Long id);
}
