package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Combo;
import com.sky.entity.Drink;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.ComboMapper;
import com.sky.mapper.DrinkMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartMapper shoppingCartMapper;
    private final DrinkMapper drinkMapper;
    private final ComboMapper comboMapper;

    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        validateProductChoice(shoppingCartDTO);

        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (!list.isEmpty()) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            cart.setUserId(userId);
            shoppingCartMapper.updateNumberById(cart);
            return;
        }

        if (shoppingCartDTO.getDishId() != null) {
            Drink drink = drinkMapper.getById(shoppingCartDTO.getDishId());
            ensureAvailable(drink == null ? null : drink.getStatus());
            shoppingCart.setName(drink.getName());
            shoppingCart.setImage(drink.getImage());
            shoppingCart.setAmount(drink.getPrice());
        } else {
            Combo combo = comboMapper.getById(shoppingCartDTO.getSetmealId());
            ensureAvailable(combo == null ? null : combo.getStatus());
            shoppingCart.setName(combo.getName());
            shoppingCart.setImage(combo.getImage());
            shoppingCart.setAmount(combo.getPrice());
        }

        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartMapper.insert(shoppingCart);
    }

    public List<ShoppingCart> showShoppingCart() {
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .build();
        return shoppingCartMapper.list(shoppingCart);
    }

    public void cleanShoppingCart() {
        shoppingCartMapper.cleanByUserId(BaseContext.getCurrentId());
    }

    public void delete(ShoppingCartDTO shoppingCartDTO) {
        validateProductChoice(shoppingCartDTO);
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if (list.isEmpty()) {
            return;
        }

        ShoppingCart cart = list.get(0);
        if (cart.getNumber() <= 1) {
            shoppingCartMapper.deleteByIdAndUserId(cart.getId(), userId);
            return;
        }

        cart.setNumber(cart.getNumber() - 1);
        cart.setUserId(userId);
        shoppingCartMapper.updateNumberById(cart);
    }

    private void validateProductChoice(ShoppingCartDTO shoppingCartDTO) {
        boolean hasDrink = shoppingCartDTO != null && shoppingCartDTO.getDishId() != null;
        boolean hasCombo = shoppingCartDTO != null && shoppingCartDTO.getSetmealId() != null;
        if (hasDrink == hasCombo) {
            throw new ShoppingCartBusinessException("请选择一个有效商品");
        }
    }

    private void ensureAvailable(Integer status) {
        if (!StatusConstant.ENABLE.equals(status)) {
            throw new ShoppingCartBusinessException(MessageConstant.ITEM_UNAVAILABLE);
        }
    }
}
