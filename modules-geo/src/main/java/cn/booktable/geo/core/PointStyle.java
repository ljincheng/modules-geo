package cn.booktable.geo.core;

import lombok.Data;


/**
 * @author ljc
 */
@Data
public class PointStyle {
    private String wellKnownName;
    private String lineColor;
    private String fillColor;
    private Float opacity;
    private Float size;

    private String labelField;
    // labelFont 设置
    private String fontFamily;
    private String fontStyle;
    private String fontWeight;
    private Integer fontSize;
    private String fontColor;

}
