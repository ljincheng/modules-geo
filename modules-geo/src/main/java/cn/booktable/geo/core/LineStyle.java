package cn.booktable.geo.core;

import lombok.Data;

/**
 * @author ljc
 */
@Data
public class LineStyle {
    private String lineColor;
    private Float width;

    private String labelField;
    // labelFont 设置
    private String fontFamily;
    private String fontStyle;
    private String fontWeight;
    private Integer fontSize;
    private String fontColor;
}
