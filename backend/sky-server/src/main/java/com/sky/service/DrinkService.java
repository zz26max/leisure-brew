package com.sky.service;

import com.sky.dto.DrinkDTO;
import com.sky.dto.DrinkPageQueryDTO;
import com.sky.entity.Drink;
import com.sky.result.PageResult;
import com.sky.vo.DrinkVO;

import java.io.Serializable;
import java.util.List;

public interface DrinkService {

    /**
     * 新增菜品跟口味
     * @param drinkDTO
     */
    public void saveWithFlavor(DrinkDTO dishDTO);

    /**
     * 菜品分页查询
     * @param drinkPageQueryDTO
     * @return
     */
    PageResult pageQuery(DrinkPageQueryDTO dishPageQueryDTO);

    /**
     * 菜品批量删除
     * @param ids
     */
    void delectBatch(List<Long> ids);

    /**
     * 根据菜品id查询菜品数据
     * @param drinkId
     */
    DrinkVO getById(Long dishId);

    /**
     * 修改菜品
     * @param drinkDTO
     */
    void updateWithFlavor(DrinkDTO dishDTO);

    /**
     * 根据套餐id查询套餐
     * @param categoryId
     * @return
     */
    List<Drink> list(Long categoryId);

    /**
     * 起售/停售菜品
     */
    void status(Long dishId, Integer status);

    /**
     * 条件查询菜品和口味
     * @param drink
     * @return
     */
    List<DrinkVO> listWithFlavor(Drink drink);

}
