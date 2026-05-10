package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Combo;
import com.sky.result.Result;
import com.sky.service.ComboService;
import com.sky.vo.DrinkItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@RestController("userComboController")
@RequestMapping("/user/combo")
@Api(tags = "C端-套餐浏览接口")
public class ComboController {
    @Autowired
    private ComboService comboService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 条件查询
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    public Result<List<Combo>> list(Long categoryId) {
        String key = "setmeal_" + categoryId;
        List<Combo> list = (List<Combo>) redisTemplate.opsForValue().get(key);
        if (list != null) {
            // 命中缓存（包含空列表），直接返回
            return Result.success(list);
        }

        Combo combo = new Combo();
        combo.setCategoryId(categoryId);
        combo.setStatus(StatusConstant.ENABLE);

        list = comboService.list(combo);
        if (list == null || list.isEmpty()) {
            // 缓存空结果，短TTL防止缓存穿透
            redisTemplate.opsForValue().set(key, Collections.emptyList(), 5, TimeUnit.MINUTES);
            return Result.success(list);
        }
        // 正常数据缓存更长时间，减少DB压力
        redisTemplate.opsForValue().set(key, list, 30, TimeUnit.MINUTES);

        return Result.success(list);
    }

    /**
     * 根据套餐id查询包含的菜品列表
     *
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询包含的菜品列表")
    public Result<List<DrinkItemVO>> dishList(@PathVariable("id") Long id) {
        String key = "setmeal_dish_" + id;
        List<DrinkItemVO> list = (List<DrinkItemVO>) redisTemplate.opsForValue().get(key);
        if (list != null) {
            // 命中缓存（包含空列表），直接返回
            return Result.success(list);
        }
        list = comboService.getDrinkItemById(id);
        if (list == null || list.isEmpty()) {
            // 缓存空结果，短TTL防止缓存穿透
            redisTemplate.opsForValue().set(key, Collections.emptyList(), 5, TimeUnit.MINUTES);
            return Result.success(list);
        }
        // 正常数据缓存更长时间，减少DB压力
        redisTemplate.opsForValue().set(key, list, 30, TimeUnit.MINUTES);
        return Result.success(list);
    }
}
