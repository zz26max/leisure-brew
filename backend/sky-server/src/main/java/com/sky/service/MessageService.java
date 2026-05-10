package com.sky.service;

import com.sky.dto.MessagePageQueryDTO;
import com.sky.entity.Message;
import com.sky.result.PageResult;

import java.util.List;

public interface MessageService {

    void save(Message message);

    PageResult pageQuery(MessagePageQueryDTO dto);

    int countUnread();

    void markAsRead(Long id);

    void batchMarkAsRead(List<Long> ids);
}
