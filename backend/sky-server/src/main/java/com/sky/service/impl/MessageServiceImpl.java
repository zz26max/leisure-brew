package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.MessagePageQueryDTO;
import com.sky.entity.Message;
import com.sky.mapper.MessageMapper;
import com.sky.result.PageResult;
import com.sky.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public void save(Message message) {
        messageMapper.insert(message);
    }

    @Override
    public PageResult pageQuery(MessagePageQueryDTO dto) {
        PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
        Page<Message> page = messageMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public int countUnread() {
        return messageMapper.countByStatus(1);
    }

    @Override
    public void markAsRead(Long id) {
        messageMapper.updateStatus(id, 2);
    }

    @Override
    public void batchMarkAsRead(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            messageMapper.updateStatusBatch(ids, 2);
        }
    }
}
