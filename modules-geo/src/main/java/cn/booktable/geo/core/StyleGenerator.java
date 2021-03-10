package cn.booktable.geo.core;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import org.geoserver.catalog.StyleGenerator.ColorRamp.Entry;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.*;
import org.geotools.styling.Font;
import org.geotools.styling.Stroke;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.NilExpression;

/**
 * @author ljc
 */
public class StyleGenerator {

    private static Map<String,Color> ramp=new HashMap<>();
    private StyleFactory sf;
    private FilterFactory ff;

    private static StyleGenerator mInstance=null;
    static {
        ramp.put("red", Color.decode("0xFF3300"));
        ramp.put("black", Color.decode("0x000000"));
        ramp.put("white", Color.decode("0xFFFFFF"));
        ramp.put("blue", Color.decode("0x0000FF"));
        ramp.put("gray", Color.decode("0x808080"));
        ramp.put("green", Color.decode("0x008000"));
        ramp.put("orange", Color.decode("0xFF6600"));
        ramp.put("dark orange", Color.decode("0xFF9900"));
        ramp.put("gold", Color.decode("0xFFCC00"));
        ramp.put("yellow", Color.decode("0xFFFF00"));
        ramp.put("dark yellow", Color.decode("0x99CC00"));
        ramp.put("teal", Color.decode("0x00CC33"));
        ramp.put("cyan", Color.decode("0x0099CC"));
        ramp.put("azure", Color.decode("0x0033CC"));
        ramp.put("violet", Color.decode("0x3300FF"));
    }

    public static StyleGenerator instance(){
        if(mInstance==null){
            mInstance=new StyleGenerator();
        }
        return mInstance;
    }

    public StyleGenerator() {
        sf = CommonFactoryFinder.getStyleFactory();
        ff = CommonFactoryFinder.getFilterFactory(null);
    }


    private Color colorCode(String code){
        Color color= ramp.get(code);
        if(color!=null){
            return color;
        }
        return Color.decode(code);
    }

    public StyleType parseStyleType(String name){
        StyleType styleType=null;
        if(StyleType.POLYGON.name().equals(name)){
            styleType=StyleType.POLYGON;
        }else if(StyleType.POINT.name().equals(name)){
            styleType=StyleType.POINT;
        }else if(StyleType.LINE.name().equals(name)){
            styleType=StyleType.LINE;
        }
        return styleType;
    }

    public Style defaultStyle(StyleType styleType){
        Style style=null;
        switch (styleType){
            case LINE:style=lineStyle();break;
            case POINT:style=pointStyle();break;
            case POLYGON:style=polygonStyle();break;
        }
        return style;
    }

    public Style polygonStyle(){
       Style style= SLD.createPolygonStyle(Color.BLUE,Color.RED,1);
       return style;
    }

    public Style pointStyle(){
        Style style=SLD.createPointStyle("circle", Color.RED, Color.yellow, 0.5f, 10f);
        return style;
    }

    public Style lineStyle(){
        Style  style= SLD.createLineStyle(Color.BLUE,1);
        return style;
    }

    public Style getStyle(StyleType type){
        if(type.compareTo(StyleType.POLYGON)==0){
            return polygonStyle();
        }
        if(type.compareTo(StyleType.POINT)==0){
            return pointStyle();
        }
        if(type.compareTo(StyleType.LINE)==0){
            return lineStyle();
        }
        return null;
    }

    private String replaceValue(String value,String defaultValue){
        if(StringUtils.isBlank(value)){
            return defaultValue;
        }
        return value;
    }

    private int replaceIntegerValue(Integer value,int defaultValue){
        return value==null?defaultValue:value.intValue();
    }
    private float replaceFloatValue(Float value,float defaultValue){
        return value==null?defaultValue:value.floatValue();
    }

    public  Style createPolygonStyle(
            Color outlineColor, Color fillColor, float opacity, String labelField, Font labelFont,Color fontColor,String iconUri,String iconFormat) {
        Stroke stroke = sf.createStroke(ff.literal(outlineColor), ff.literal(1.0f));
        Fill fill = Fill.NULL;
        if (fillColor != null) {
            fill = sf.createFill(ff.literal(fillColor), ff.literal(opacity));
        }
        PolygonSymbolizer polySym = sf.createPolygonSymbolizer(stroke, fill, null);
        PointSymbolizer iconSym=null;
        if(iconUri!=null && iconFormat!=null){
            // mark
            StyleBuilder sb = new StyleBuilder();
            ExternalGraphic extg = sb.createExternalGraphic(iconUri, iconFormat);
//            Mark mark = sb.createMark("square");
//            mark.getStroke().setWidth(sb.getFilterFactory().literal(10));
//            Graphic graphic = sb.createGraphic(extg, mark, null);
            Graphic graphic = sb.createGraphic(extg, null, null);
            graphic.setSize(NilExpression.NIL);
            iconSym= sb.createPointSymbolizer(graphic);
        }
        if (labelField == null) {

//            return SLD.wrapSymbolizers(polySym);
            return iconSym==null?SLD.wrapSymbolizers(polySym):SLD.wrapSymbolizers(polySym,iconSym);

        } else {
            Font font = (labelFont == null ? sf.getDefaultFont() : labelFont);
            Fill labelFill = sf.createFill(ff.literal(fontColor));

            TextSymbolizer textSym =
                    sf.createTextSymbolizer(
                            labelFill,
                            new Font[] {font},
                            null,
                            ff.property(labelField),
                            null,
                            null);





//            return SLD.wrapSymbolizers(polySym, textSym);
            return iconSym==null?SLD.wrapSymbolizers(polySym,textSym):SLD.wrapSymbolizers(polySym,textSym,iconSym);
        }
    }
    public Style parse(PolygonStyle polygonStyle){
        Color outlineColor=colorCode(replaceValue(polygonStyle.getOutlineColor(),"black"));
        Color fillColor=colorCode(replaceValue(polygonStyle.getFillColor(),"green"));
        float opacity=replaceFloatValue(polygonStyle.getOpacity(),1.0f);
        String labelField=polygonStyle.getLabelField();
        String iconUrl=replaceValue(polygonStyle.getIconUri(),null);
        String iconFormat=replaceValue(polygonStyle.getIconFormat(),null);
        Style style=null;
        if(StringUtils.isNotBlank(labelField)){

            Font labelFont=sf.createFont(
                        ff.literal(replaceValue(polygonStyle.getFontFamily(),"Serif")),
                        ff.literal(replaceValue(polygonStyle.getFontStyle(),"normal")),
                        ff.literal(replaceValue(polygonStyle.getFontWeight(),"normal")),
                        ff.literal(replaceIntegerValue(polygonStyle.getFontSize(),12)));

            style=createPolygonStyle(outlineColor,fillColor,opacity,labelField,labelFont,colorCode(replaceValue(polygonStyle.getFontColor(),"black")),iconUrl,iconFormat);

        }else {
//            style=SLD.createPolygonStyle(outlineColor,fillColor,opacity);
            style=createPolygonStyle(outlineColor,fillColor,opacity,null,null,null,iconUrl,iconFormat);
        }
        return style;
    }

    public Style parse(PointStyle pointStyle){
        Style style=null;
        String wellKnownName=replaceValue(pointStyle.getWellKnownName(),"Circle");
        Color lineColor=colorCode(replaceValue(pointStyle.getLineColor(),"black"));
        Color fillColor=colorCode(replaceValue(pointStyle.getFillColor(),"green"));
        float opacity=replaceFloatValue(pointStyle.getOpacity(),1.0f);
        float size=replaceFloatValue(pointStyle.getSize(),3f);
        String labelField=pointStyle.getLabelField();
        if(StringUtils.isNotBlank(labelField)) {
            Font labelFont = sf.createFont(
                    ff.literal(replaceValue(pointStyle.getFontFamily(), "Serif")),
                    ff.literal(replaceValue(pointStyle.getFontStyle(), "normal")),
                    ff.literal(replaceValue(pointStyle.getFontWeight(), "normal")),
                    ff.literal(replaceIntegerValue(pointStyle.getFontSize(), 12)));
            style=SLD.createPointStyle(wellKnownName,lineColor,fillColor,opacity,size,labelField,labelFont);
        }else{
            style=SLD.createPointStyle(wellKnownName,lineColor,fillColor,opacity,size);
        }
        return  style;
    }

    public Style parse(LineStyle lineStyle){
        Style style=null;
        Color lineColor=colorCode(replaceValue(lineStyle.getLineColor(),"black"));
        float width=replaceFloatValue(lineStyle.getWidth(),1f);
        String labelField=lineStyle.getLabelField();
        if(StringUtils.isNotBlank(labelField)) {
            Font labelFont = sf.createFont(
                    ff.literal(replaceValue(lineStyle.getFontFamily(), "Serif")),
                    ff.literal(replaceValue(lineStyle.getFontStyle(), "normal")),
                    ff.literal(replaceValue(lineStyle.getFontWeight(), "normal")),
                    ff.literal(replaceIntegerValue(lineStyle.getFontSize(), 12)));
            style=SLD.createLineStyle(lineColor,width,labelField,labelFont);
        }else{
            style=SLD.createLineStyle(lineColor,width);
        }
        return  style;
    }

    public Style toStyle(String json,StyleType type){
        if(type.compareTo(StyleType.POLYGON)==0){
            PolygonStyle polygonStyle= JSON.parseObject(json,PolygonStyle.class);
            return parse(polygonStyle);
        }
        if(type.compareTo(StyleType.POINT)==0){
            PointStyle pointStyle=JSON.parseObject(json,PointStyle.class);
            return parse(pointStyle);
        }
        if(type.compareTo(StyleType.LINE)==0){
            LineStyle lineStyle=JSON.parseObject(json,LineStyle.class);
            return parse(lineStyle);
        }
        return null;
    }

}
