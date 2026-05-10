package com.sky.controller.admin;

import com.sky.dto.DrinkDTO;
import com.sky.dto.DrinkPageQueryDTO;
import com.sky.entity.Drink;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DrinkService;
import com.sky.vo.DrinkVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/drink")
@Api("菜品相关接口")
@Slf4j
public class DrinkController {

    @Autowired
    private DrinkService drinkService;
    @Qualifier("redisTemplate")
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DrinkDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        drinkService.saveWithFlavor(dishDTO);

        // 新增菜品，清空相关分类缓存
        cleanCashe("dish_" + dishDTO.getCategoryId());

        return Result.success();
    }

    /**
     * 菜品分类查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分类查询")
    public Result<PageResult> page(DrinkPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分类查询：{}", dishPageQueryDTO);
        PageResult pageResult = drinkService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 菜品删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("菜品删除")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("菜品批量删除：{}",ids);
        drinkService.delectBatch(ids);

        //可能影响多个分类，直接清空缓存
        cleanCashe("dish_*");

        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据菜品id查询")
    public Result<DrinkVO> getById(@PathVariable Long id) {
        log.info("根据菜品id查询：{}",id);
        DrinkVO dishVO = drinkService.getById(id);
        return Result.success(dishVO);
    }

    @PutMapping()
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DrinkDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        drinkService.updateWithFlavor(dishDTO);

        //可能影响多个分类，直接清空缓存
        cleanCashe("dish_*");

        return Result.success();
    }

    /**
     * 根据id查询套餐菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Drink>> list(Long categoryId) {
        log.info("根据分类id查询菜品：{}",categoryId);
        List<Drink> list = drinkService.list(categoryId);
        return Result.success(list);
    }


    @PostMapping("status/{status}")
    @ApiOperation("菜品起售/停售")
    public Result status(@PathVariable Integer status,@RequestParam Long id) {
        log.info("起售/停售菜品：{}",id);
        drinkService.status(id,status);

        //可能影响多个分类，直接清空缓存
        cleanCashe("dish_*");

        return Result.success();
    }

    //清空缓存方法
    private void cleanCashe(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
