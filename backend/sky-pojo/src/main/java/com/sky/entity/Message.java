package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 消息类型: 1=待接单, 2=待接单(急), 3=待派送, 4=催单, 5=今日数据 */
    private Integer type;
    /** 消息内容 */
    private String content;
    /** 详情JSON */
    private String details;
    /** 关联订单ID */
    private Long orderId;
    /** 状态: 1=未读, 2=已读 */
    private Integer status;
    private LocalDateTime createTime;
}
