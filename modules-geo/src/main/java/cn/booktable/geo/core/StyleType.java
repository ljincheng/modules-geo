package cn.booktable.geo.core;

/**
 * @author ljc
 */
public enum StyleType {
    POINT("Point"), // 点
    LINE("Line"), // 线
    POLYGON("Polygon"), // 多边形
    RASTER("Raster"), // 栅格
    GENERIC("Generic"); // Unknown

    private final String name;

    private StyleType(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}