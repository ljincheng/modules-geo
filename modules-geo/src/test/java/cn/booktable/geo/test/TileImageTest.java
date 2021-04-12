package cn.booktable.geo.test;

import cn.booktable.geo.provider.TileModelProvider;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

import java.math.BigDecimal;
import java.text.Bidi;

/**
 * @author ljc
 */
public class TileImageTest {

    private static final   double imgSize=256;
    private Coordinate origin=new Coordinate(-180.00,90.00);
    private Coordinate area=new Coordinate(360,180);
    private double[] resolution2(double z){
        double pis=Math.pow(2,z)*imgSize;
        double x= area.x/pis;
        double y= area.y/pis;
        return new double[]{x,y};
    }


    public void testBbox(double z,double cell,double row){
        double[] res=resolution2(z);
        double xmin=cell*imgSize*res[0] + origin.x;
        double ymax=origin.y - row*imgSize*res[1];
        double xmax=xmin+res[0]*imgSize;
        double ymin=ymax-res[1]*imgSize;
        String info=String.format("z=%f,cell=%f,row=%f,bbox=%f,%f,%f,%f",z,cell,row,xmin,ymin,xmax,ymax);
        System.out.println("======================\r\n"+info);
    }

   // @Test
    public void testTileWrite(){
        testBbox(2,2,1);
        String bbox=TileModelProvider.instance().bbox(2,2,1);
        System.out.println("======================\r\nbbox=="+bbox);
    }
}
