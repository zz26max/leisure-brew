package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MessagePageQueryDTO implements Serializable {
    private Integer pageNum;
    private Integer pageSize;
    /** 状态: 1=未读, 2=已读 */
    private Integer status;
}
