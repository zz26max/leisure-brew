package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.ComboDTO;
import com.sky.dto.ComboPageQueryDTO;
import com.sky.entity.Drink;
import com.sky.entity.Combo;
import com.sky.entity.ComboDrink;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DrinkMapper;
import com.sky.mapper.ComboDrinkMapper;
import com.sky.mapper.ComboMapper;
import com.sky.result.PageResult;
import com.sky.service.ComboService;
import com.sky.vo.DrinkItemVO;
import com.sky.vo.ComboVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ComboServiceImpl implements ComboService {

    @Autowired
    private ComboMapper comboMapper;
    @Autowired
    private ComboDrinkMapper comboDrinkMapper;
    @Autowired
    private DrinkMapper drinkMapper;


    /**
     * 新增菜品
     * @param setmealDTO
     */
    public void saveWithDish(ComboDTO setmealDTO) {
        Combo combo = new Combo();
        BeanUtils.copyProperties(setmealDTO, combo);
        //向套餐表插入数据
        comboMapper.insert(combo);
        //获取生成套餐的套餐id
        Long comboId = combo.getId();

        List<ComboDrink> comboDrinks = setmealDTO.getComboDrinks();
        comboDrinks.forEach(comboDrink -> {
            comboDrink.setComboId(comboId);
        });
        //保存套餐和菜品的关联关系
        comboDrinkMapper.insertBatch(comboDrinks);
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(ComboPageQueryDTO setmealPageQueryDTO) {
        int pageNum = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        Page<ComboVO> page = comboMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    public void deleteBatch(List<Long> ids) {
        //判断套餐是否起售
        ids.forEach(id -> {
            Combo combo = comboMapper.getById(id);
            if (combo.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });
        //删除套餐
        ids.forEach(comboId -> {
            //删除套餐表数据
            comboMapper.deleteById(comboId);
            //删除套餐菜品关系表中数据
            comboMapper.deleteByComboId(comboId);
        });
    }

    /**
     * 根据id查询套餐和关联菜品数据
     * @param id
     * @return
     */
    public ComboVO getByIdWithDish(Long id) {
        //查询套餐
        Combo combo = comboMapper.getById(id);
        //查询套餐关联菜品
        List<ComboDrink> comboDrinks = comboDrinkMapper.getByComboId(id);
        //封装vo对象
        ComboVO comboVO = new ComboVO();
        BeanUtils.copyProperties(combo, comboVO);
        comboVO.setComboDrinks(comboDrinks);

        return comboVO;
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Transactional //开启事务
    public void update(ComboDTO setmealDTO) {
        Combo combo = new Combo();
        BeanUtils.copyProperties(setmealDTO, combo);

        //修改套餐表----操作setmeal表---update
        comboMapper.update(combo);
        //获取套餐id
        Long comboId = setmealDTO.getId();
        //删除套餐和菜品的关联关系---操作setmeal_dish表---delete
        comboDrinkMapper.deleteByComboId(comboId);

        List<ComboDrink> comboDrinks = setmealDTO.getComboDrinks();
        comboDrinks.forEach(comboDrink -> {
            comboDrink.setComboId(comboId);
        });
        //重新插入套餐和菜品关联关系---操作setmeal_dish表---insert
        comboDrinkMapper.insertBatch(comboDrinks);
    }

    /**
     * 套餐起售/停售
     * @param status
     * @param id
     */
    public void starOrStop(Integer status, Long id) {
        //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售
        List<Drink> drinkList = drinkMapper.getBySetmealId(id);
        if (drinkList != null && drinkList.size() > 0) {
            drinkList.forEach(drink -> {
                if (drink.getStatus() == StatusConstant.DISABLE){
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            });
        }

        Combo combo = Combo.builder()
                .id(id)
                .status(status)
                .build();
        comboMapper.update(combo);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Combo> list(Combo combo) {
        List<Combo> list = comboMapper.list(combo);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DrinkItemVO> getDrinkItemById(Long id) {
        return comboMapper.getDrinkItemByComboId(id);
    }
}
