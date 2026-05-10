package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Drink;
import com.sky.entity.Combo;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DrinkMapper;
import com.sky.mapper.ComboMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    public ShoppingCartMapper shoppingCartMapper;
    @Autowired
    public DrinkMapper drinkMapper;
    @Autowired
    public ComboMapper comboMapper;
    /**
     * 添加购物车
     */
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断购物车内有无该商品
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //有就update number +1
        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
            return;
        }
        //没有就添加到cart表

        //判断当前添加的是菜品还是套餐
        Long dishId = shoppingCartDTO.getDishId();
        if (dishId != null) {
            //当前添加的是菜品
            Drink drink = drinkMapper.getById(dishId);
            shoppingCart.setName(drink.getName());
            shoppingCart.setImage(drink.getImage());
            shoppingCart.setAmount(drink.getPrice());
        }else {
            //当前添加的是套餐
            Combo combo = comboMapper.getById(shoppingCartDTO.getSetmealId());
            shoppingCart.setName(combo.getName());
            shoppingCart.setImage(combo.getImage());
            shoppingCart.setAmount(combo.getPrice());
        }

        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartMapper.insert(shoppingCart);
    }

    /**
     * 查看购物车
     * @return
     */
    public List<ShoppingCart> showShoppingCart() {
        //获取微信用户id
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .id(userId)
                .build();

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        return list;
    }

    /**
     * 清空购物车
     */
    public void cleanShoppingCart() {
        //获取微信用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.cleanByUserId(userId);
    }


    /**
     * 删除购物车菜品
     * @param shoppingCartDTO
     */
    public void delete(ShoppingCartDTO shoppingCartDTO) {
        //查询当前微信用户购物车数据
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if (list != null && list.size() > 0) {
            shoppingCart= list.get(0);
            Integer number = shoppingCart.getNumber();
            if (number == 1) {
                //如果数量为1直接删除
                shoppingCartMapper.deleteById(shoppingCart.getId());
            }else {
                //如果数量不为1修改number-1
                shoppingCart.setNumber(shoppingCart.getNumber()-1);
                shoppingCartMapper.updateNumberById(shoppingCart);
            }
        }


    }
}
