package com.yt.ytbibackend.model.vo;

import lombok.Data;

/**
 * BI的返回结果
 */
@Data
public class BiResponse {

    private Long chartId;

    private String genOption;

    private String genResult;
}
