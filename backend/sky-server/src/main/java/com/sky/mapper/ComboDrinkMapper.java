package com.sky.mapper;

import com.sky.entity.ComboDrink;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ComboDrinkMapper {

    /**
     * 根据菜品id查询对应套餐id
     * @param drinkIds
     * @return
     */
    List<Long> getComboDrinkIdsByDrinkIds(List<Long> drinkIds);

    /**
     * 批量保存菜品跟套餐的关联关系
     * @param comboDrinks
     */
    void insertBatch(List<ComboDrink> comboDrinks);

    /**
     * 根据id查询菜品跟套餐的关联关系
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{comboId}")
    List<ComboDrink> getByComboId(Long id);

    /**
     * 删除套餐跟菜品关系表数据
     * @param comboId
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{comboId}")
    void deleteByComboId(Long comboId);
}
