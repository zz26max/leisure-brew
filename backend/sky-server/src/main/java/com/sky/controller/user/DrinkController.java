package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Drink;
import com.sky.result.Result;
import com.sky.service.DrinkService;
import com.sky.vo.DrinkVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@RestController("userDrinkController")
@RequestMapping("/user/drink")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DrinkController {
    @Autowired
    private DrinkService drinkService;
    @Qualifier("redisTemplate")
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DrinkVO>> list(Long categoryId) {
        // 动态拼接key -> dish + 菜品id
        String key = "dish_" + categoryId;
        // 查询redis缓存
        List<DrinkVO> list = (List<DrinkVO>) redisTemplate.opsForValue().get(key);
        if (list != null) {
            // 命中缓存（包含空列表），直接返回，避免重复查库
            return Result.success(list);
        }

        // 没有就查询数据库并载入缓存
        Drink drink = new Drink();
        drink.setCategoryId(categoryId);
        drink.setStatus(StatusConstant.ENABLE);// 查询起售中的菜品

        list = drinkService.listWithFlavor(drink);
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
