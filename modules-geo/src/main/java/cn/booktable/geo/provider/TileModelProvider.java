package cn.booktable.geo.provider;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Coordinate;

/**
 * @author ljc
 */
public class TileModelProvider {

    private Coordinate origin=new Coordinate(-180.00,90.00);
    private Coordinate area=new Coordinate(360,180);
    private int tileSize;

    private static TileModelProvider mInstance=null;
    public static TileModelProvider instance(){
        if(mInstance==null){
            mInstance=new TileModelProvider(256);
        }
        return mInstance;
    }
    public TileModelProvider(int tileSize){
        this.tileSize=tileSize;
    }

    private double[] resolution(int z){
        double pis=Math.pow(2,z)*tileSize;
        double x= area.x/pis;
        double y= area.y/pis;
        return new double[]{x,y};
    }

    public int[] getTileSize(){
        return new int[]{this.tileSize,this.tileSize};
    }

    public String bbox(int z,int cell,int row){
        ReferencedEnvelope env=envelope(z, cell, row);
        String bboxStr=String.format("%f,%f,%f,%f",env.getMinX(),env.getMinY(),env.getMaxX(),env.getMaxY());
//        String info=String.format("z=%f,cell=%f,row=%f,bbox=%s",z,cell,row,bboxStr);
//        System.out.println("======================\r\n"+info);
        return bboxStr;
    }


    public ReferencedEnvelope envelope(int z,int cell,int row){
        double[] res=resolution(z);
//        double xmin=cell*tileSize*res[0] + origin.x;
//        double ymin=origin.y + row*tileSize*res[1];
//        double xmax=xmin+res[0]*tileSize;
//        double ymax=ymin+res[1]*tileSize;
//        double xmin=cell*tileSize*res[0] + origin.x;
//        double ymax=origin.y + row*tileSize*res[1];
//        double xmax=xmin+res[0]*tileSize;
//        double ymin=ymax-res[1]*tileSize;

        /**
         *   var cell = Math.floor((min.x - o.x) / res.x / tsize);
         *         var row = Math.floor((o.y - min.y) / res.y / tsize);
         *         var left = -(min.x - o.x) / res.x +tsize * cell ;
         *         var top = -(o.y - min.y) / res.y +tsize * row;
         */
//        double[] res=resolution(z);
        double xmin=cell*tileSize*res[0] + origin.x;
        double ymax=origin.y - row*tileSize*res[1];
        double xmax=xmin+res[0]*tileSize;
        double ymin=ymax-res[1]*tileSize;
        ReferencedEnvelope mapBounds =new ReferencedEnvelope(xmin,xmax,ymin,ymax, null);
        return mapBounds;
    }
}
