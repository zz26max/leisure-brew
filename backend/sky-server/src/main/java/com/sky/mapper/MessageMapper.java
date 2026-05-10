package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.MessagePageQueryDTO;
import com.sky.entity.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MessageMapper {

    @Insert("insert into message (type, content, details, order_id, status, create_time) " +
            "values (#{type}, #{content}, #{details}, #{orderId}, #{status}, #{createTime})")
    void insert(Message message);

    Page<Message> pageQuery(MessagePageQueryDTO dto);

    @Select("select count(*) from message where status = #{status}")
    int countByStatus(Integer status);

    @Update("update message set status = #{status} where id = #{id}")
    void updateStatus(Long id, Integer status);

    void updateStatusBatch(List<Long> ids, Integer status);
}
