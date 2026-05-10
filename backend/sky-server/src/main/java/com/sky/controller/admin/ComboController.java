package com.sky.controller.admin;

import com.sky.dto.ComboDTO;
import com.sky.dto.ComboPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.ComboService;
import com.sky.vo.ComboVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 套餐相关接口
 */
@RestController
@RequestMapping("/admin/combo")
@Slf4j
@Api(tags = "套餐相关接口")
public class ComboController {

    @Autowired
    private ComboService comboService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增套餐")
    public Result save(@RequestBody ComboDTO setmealDTO) {
        comboService.saveWithDish(setmealDTO);
        // 新增套餐，清空该分类缓存
        cleanCache("setmeal_" + setmealDTO.getCategoryId());
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> page(ComboPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页查询...");
        PageResult pageResult = comboService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public Result delete(@RequestParam List<Long> ids){
        log.info("删除套餐：{}",ids);
        comboService.deleteBatch(ids);
        // 删除可能影响多个分类和套餐菜品关系，清空相关全部缓存
        cleanCache("setmeal_*");
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<ComboVO> getById(Long id){
        log.info("根据id查询套餐（回显）：{}",id);
        ComboVO setmealVO = comboService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }
    @PutMapping
    @ApiOperation("修改套餐")
    public Result update(@RequestBody ComboDTO setmealDTO){
        comboService.update(setmealDTO);
        // 修改可能影响分类或套餐菜品关系，清空相关全部缓存
        cleanCache("setmeal_*");
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("套餐起售/停售")
    public Result starOrStop(@PathVariable Integer status,Long id){
        log.info("套餐起售/停售：{}",id);
        comboService.starOrStop(status,id);
        // 状态变更，清空整个缓存
        cleanCache("setmeal_*");
        return Result.success();
    }

    // 清空缓存通用方法
    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
