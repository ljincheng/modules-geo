package cn.booktable.geo.core;

import lombok.Data;

/**
 * @author ljc
 */
@Data
public class PaintParam {
    private String mapId;
    private Integer z;
    private Integer x;
    private Integer y;
    private String bbox;//范围:"minX,minY,maxX,maxY"
    private String area;//输出区域："width,height"
    private String format;//类型:"jpeg/png"
    private String crs;
    private Boolean reload;
}
