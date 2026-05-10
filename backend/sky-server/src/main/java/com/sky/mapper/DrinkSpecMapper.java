package com.sky.mapper;

import com.sky.entity.DrinkSpec;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DrinkSpecMapper {

    /**
     * 批量插入口味数据
     * @param flavors
     */
    void insertBatch(List<DrinkSpec> specs);

    /**
     * 根据菜品id删除口味
     * @param DishId
     */
    @Delete("delete from dish_flavor where dish_id = #{drinkId}")
    void deleteByDishId(Long drinkId);

    void deleteByDishIds(List<Long> ids);

    /**
     * 根据菜品id查口味
     * @param dishId
     */
    @Select("select * from dish_flavor where dish_id = #{drinkId}")
    List<DrinkSpec> getByDishId(Long drinkId);
}
