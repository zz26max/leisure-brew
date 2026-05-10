package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.ComboDTO;
import com.sky.dto.ComboPageQueryDTO;
import com.sky.entity.Combo;
import com.sky.entity.ComboDrink;
import com.sky.enumeration.OperationType;
import com.sky.vo.DrinkItemVO;
import com.sky.vo.ComboVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ComboMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 新增套餐
     * @param combo
     */
    @AutoFill(OperationType.INSERT)
    void insert(Combo combo);


    /**
     * 套餐分页查询
     * @param comboPageQueryDTO
     * @return
     */
    Page<ComboVO> pageQuery(ComboPageQueryDTO comboPageQueryDTO);

    /**
     * 根据套餐id查询套餐
     * @param id
     */
    @Select("select * from setmeal where id =#{id}")
    Combo getById(Long id);

    /**
     * 删除套餐表数据
     * @param comboId
     */
    @Delete("delete from setmeal where id = #{id}")
    void deleteById(Long comboId);

    /**
     * 删除套餐菜品关联关系
     * @param comboId
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{comboId}")
    void deleteByComboId(Long comboId);


    /**
     * 修改套餐基本信息
     * @param combo
     */
    void update(Combo combo);

    /**
     * 动态条件查询套餐
     * @param combo
     * @return
     */
    List<Combo> list(Combo combo);

    /**
     * 根据套餐id查询菜品选项
     * @param comboId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{comboId}")
    List<DrinkItemVO> getDrinkItemByComboId(Long comboId);

    /**
     * 根据条件统计套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
