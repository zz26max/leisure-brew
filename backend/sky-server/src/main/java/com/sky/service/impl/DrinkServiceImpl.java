package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DrinkDTO;
import com.sky.dto.DrinkPageQueryDTO;
import com.sky.entity.Drink;
import com.sky.entity.DrinkSpec;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DrinkSpecMapper;
import com.sky.mapper.DrinkMapper;
import com.sky.mapper.ComboDrinkMapper;
import com.sky.result.PageResult;
import com.sky.service.DrinkService;
import com.sky.vo.DrinkVO;
import com.wechat.pay.contrib.apache.httpclient.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class DrinkServiceImpl implements DrinkService {

    @Autowired
    private DrinkMapper drinkMapper;
    @Autowired
    private DrinkSpecMapper drinkSpecMapper;
    @Autowired
    private ComboDrinkMapper comboDrinkMapper;

    /**
     * 新增菜品跟口味
     * @param dishDTO
     */
    @Transactional//开启事务管理
    public void saveWithFlavor(DrinkDTO dishDTO) {

        Drink drink = new Drink();
        //对象属性拷贝
        BeanUtils.copyProperties(dishDTO, drink);

        //向菜品表插入1条数据
        drinkMapper.insert(drink);

        //获取insert语句生成的主键值id
        Long dishid = drink.getId();

        List<DrinkSpec> specs = dishDTO.getSpecs();
        if (specs != null && specs.size() > 0) {
            specs.forEach(dishflavor -> {
                dishflavor.setDrinkId(dishid);
            });
            //向口味插入n条数据
            drinkSpecMapper.insertBatch(specs);
        }

    }


    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DrinkPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DrinkVO> page = drinkMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 菜品批量删除
     * @param ids
     */
    @Transactional  //操作多表开启事务
    public void delectBatch(List<Long> ids) {
        //判断当前菜品是否可删除--是否起售中
        for (Long id : ids) {
            Drink drink = drinkMapper.getById(id);
            if (drink.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //判断当前菜品是否可删除--是否在套餐中
        List<Long> setmealIds = comboDrinkMapper.getComboDrinkIdsByDrinkIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            //当前菜品被套餐关联
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        /*//删除菜品
        for (Long id : ids) {
            drinkMapper.deleteById(id);
            //删除菜品关联口味数据
            drinkSpecMapper.deleteByDishId(id);
        }*/

        //优化：去掉循环，一句SQL操作完事务
        drinkMapper.deleteByIds(ids);
        drinkSpecMapper.deleteByDishIds(ids);


    }

    /**
     * 根据菜品id查询菜品数据
     * @param dishId
     */
    public DrinkVO getById(Long dishId) {
        //根据id查询菜品数据
        Drink drink= drinkMapper.getById(dishId);
        //根据id查询口味数据
        List<DrinkSpec> specs = drinkSpecMapper.getByDishId(dishId);
        //封装为VO对象
        DrinkVO dishVO = new DrinkVO();
        BeanUtils.copyProperties(drink, dishVO);
        dishVO.setSpecs(specs);
        return dishVO;
    }

    /**
     * 修改菜品基本信息跟相关联口味
     * @param dishDTO
     */
    public void updateWithFlavor(DrinkDTO dishDTO) {

        Drink drink = new Drink();
        BeanUtils.copyProperties(dishDTO, drink);
        //修改基本信息
        drinkMapper.update(drink);

        //删除口味数据
        drinkSpecMapper.deleteByDishId(dishDTO.getId());
        //插入新口味数据
        List<DrinkSpec> specs = dishDTO.getSpecs();
        if (specs != null && specs.size() > 0) {
            specs.forEach(dishflavor -> {
                dishflavor.setDrinkId(dishDTO.getId());
            });
            //向口味插入n条数据
            drinkSpecMapper.insertBatch(specs);
        }


    }

    /**
     * 根据套餐id查询套餐
     * @param categoryId
     * @return
     */
    public List list(Long categoryId) {
        Drink drink = Drink.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return drinkMapper.list(drink);
    }

    /**
     * 起售/停售菜品
     */
    public void status(Long dishId,Integer status) {
        // 1. 校验菜品是否存在
        Drink drink = drinkMapper.getById(dishId);
        if (drink == null) {
            try {
                throw new NotFoundException(MessageConstant.DISH_NOT_FOUND);
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        // 2. 校验停售逻辑：若菜品被套餐关联，不能停售（可选业务规则）
        if (status.equals("0")) {
            List<Long> setmealIds = comboDrinkMapper.getComboDrinkIdsByDrinkIds(Collections.singletonList(dishId));
            if (setmealIds != null && !setmealIds.isEmpty()) {
                try {
                    throw new RuntimeException();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // 3. 更新菜品状态
        drink.setStatus(status);
        drinkMapper.update(drink);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DrinkVO> listWithFlavor(Drink drink) {
        List<Drink> drinkList = drinkMapper.list(drink);

        List<DrinkVO> drinkVOList = new ArrayList<>();

        for (Drink d : drinkList) {
            DrinkVO drinkVO = new DrinkVO();
            BeanUtils.copyProperties(d,drinkVO);

            //根据菜品id查询对应的口味
            List<DrinkSpec> specs = drinkSpecMapper.getByDishId(d.getId());

            drinkVO.setSpecs(specs);
            drinkVOList.add(drinkVO);
        }

        return drinkVOList;
    }
}
