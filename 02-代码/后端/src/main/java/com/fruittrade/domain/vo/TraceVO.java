package com.fruittrade.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 物流轨迹节点 VO。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraceVO {

    private String time;
    private String text;
}
