package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DrinkPageQueryDTO;
import com.sky.entity.Drink;
import com.sky.enumeration.OperationType;
import com.sky.vo.DrinkVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;
import java.util.Map;

@Mapper
public interface DrinkMapper {



    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 插入菜品数据
     * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Drink drink);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<DrinkVO> pageQuery(DrinkPageQueryDTO dishPageQueryDTO);

    /**
     * 根据主键查询菜品
     * @param id
     */
    @Select("select * from dish where id = #{id} ")
    Drink getById(Long id);

    /**
     * 根据id删除菜品
     */
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);
    void deleteByIds(List<Long> ids);

    /**
     * 根据id查询菜品
     *//*
    @Select("select * from dish where id = #{id}")
    Dish selectByid(Long dishId);*/

    @AutoFill(value = OperationType.UPDATE)
    void update(Drink drink);

    /**
     * 动态条件查询菜品
     * @param dish
     * @return
     */
    List<Drink> list(Drink drink);

    /**
     * 根据套餐id查询菜品
     * @param id
     * @return
     */
    @Select("select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = #{setmealId}")
    List<Drink> getBySetmealId(Long id);

    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
