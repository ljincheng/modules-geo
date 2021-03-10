package cn.booktable.geo.core;

import lombok.Data;
import org.geotools.styling.Font;

import java.awt.*;

/**
 * @author ljc
 */
@Data
public class PolygonStyle {
    private String outlineColor;
    private String fillColor;
    private Float opacity;
    private String labelField;

    // labelFont 设置
    private String fontFamily;
    private String fontStyle;
    private String fontWeight;
    private Integer fontSize;
    private String fontColor;
    // icon
    private String iconUri;
    private String iconFormat;
}
