package cn.booktable.geo.entity;

import lombok.Data;

/**
 * @author ljc
 */
@Data
public class GeoImageCacheEntity {
    private String cacheId;//车位UUID
    private String imageId;//地图ID
    private Integer z;//Z
    private Integer x;//X
    private Integer y;//Y
    private String imageData;//图片
}
