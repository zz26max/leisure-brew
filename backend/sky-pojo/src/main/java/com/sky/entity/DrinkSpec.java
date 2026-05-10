package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 饮品规格
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrinkSpec implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    //饮品id
    private Long drinkId;

    //规格名称
    private String name;

    //规格数据list
    private String value;

}
