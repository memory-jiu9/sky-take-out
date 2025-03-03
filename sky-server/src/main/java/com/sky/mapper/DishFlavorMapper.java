package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 删除口味
     *
     * @param idList
     */
    void batchDeleteDishFlavores(ArrayList<Long> idList);

    /**
     * 批量插入口味
     *
     * @param flavors
     */
    void addBatch(List<DishFlavor> flavors);

    /**
     * 获取菜品的口味
     *
     * @param dishId
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);

    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteDishFlavores(Long dishId);
}
