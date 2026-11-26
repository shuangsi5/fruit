package com.fruittrade.domain.vo;

import lombok.Data;

import java.util.List;

/** 分类树 VO。 */
@Data
public class CategoryVO {

    private Long id;
    private String name;
    private Long parentId;
    private Integer sort;
    private String icon;
    private List<CategoryVO> children;
}
