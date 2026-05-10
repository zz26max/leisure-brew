package com.sky.service;

import com.sky.dto.ComboDTO;
import com.sky.dto.ComboPageQueryDTO;
import com.sky.entity.Combo;
import com.sky.result.PageResult;
import com.sky.vo.DrinkItemVO;
import com.sky.vo.ComboVO;

import java.util.List;


public interface ComboService {


    /**
     * 新增菜品
     * @param comboDTO
     */
    void saveWithDish(ComboDTO setmealDTO);

    /**
     * 套餐分页查询
     * @param comboPageQueryDTO
     */
    PageResult pageQuery(ComboPageQueryDTO setmealPageQueryDTO);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    ComboVO getByIdWithDish(Long id);

    /**
     * 修改套餐
     * @param comboDTO
     */
    void update(ComboDTO setmealDTO);

    /**
     * 套餐起售/停售
     * @param status
     * @param id
     */
    void starOrStop(Integer status, Long id);

    /**
     * 条件查询
     * @param combo
     * @return
     */
    List<Combo> list(Combo combo);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DrinkItemVO> getDrinkItemById(Long id);
}

