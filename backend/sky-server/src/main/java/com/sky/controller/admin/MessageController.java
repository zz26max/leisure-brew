package com.sky.controller.admin;

import com.sky.dto.MessagePageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/messages")
@Slf4j
@Api(tags = "消息通知接口")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/page")
    @ApiOperation("分页查询消息")
    public Result<PageResult> page(MessagePageQueryDTO dto) {
        log.info("分页查询消息：{}", dto);
        PageResult pageResult = messageService.pageQuery(dto);
        return Result.success(pageResult);
    }

    @GetMapping("/countUnread")
    @ApiOperation("获取未读消息数量")
    public Result<Integer> countUnread() {
        int count = messageService.countUnread();
        return Result.success(count);
    }

    @PutMapping("/{id}")
    @ApiOperation("标记单条消息已读")
    public Result markAsRead(@PathVariable Long id) {
        log.info("标记消息已读：{}", id);
        messageService.markAsRead(id);
        return Result.success();
    }

    @PutMapping("/batch")
    @ApiOperation("批量标记消息已读")
    public Result batchMarkAsRead(@RequestBody List<Long> ids) {
        log.info("批量标记消息已读：{}", ids);
        messageService.batchMarkAsRead(ids);
        return Result.success();
    }
}
