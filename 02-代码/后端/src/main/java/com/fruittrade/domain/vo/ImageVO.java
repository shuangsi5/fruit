package com.fruittrade.domain.vo;

import lombok.Data;

/** 商品图片 VO。 */
@Data
public class ImageVO {

    private String url;
    private Integer sort;
    private Integer isMain;
}
