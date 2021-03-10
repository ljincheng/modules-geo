package cn.booktable.geo.core;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author ljc
 */
public class ProduceImageMap {

    public  BufferedImage createImage(int width,int height){
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Rectangle imageBounds = new Rectangle(0, 0, width, height);
        Graphics2D gr = image.createGraphics();
        gr.setPaint(Color.WHITE);
        gr.fill(imageBounds);
        return image;
    }


    public void outputImage(String formatName,String bbox,MapContext mapContext){

    }


}
