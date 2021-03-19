function addFeature(param) {

    /// TODO: POLYGON ((280 380, 280 200, 60 200, 60 380, 180 220, 280 380),(40 160, 260 160, 240 60,
    // 20 80, 40 160))
    var geom="";
    var geoData=param.data;
    if(geoData.length>0){
        for(var i=0,k=geoData.length;i<k;i++){
            var geoObj=geoData[i];
            if(geoObj.gtype == "polygon"){
                geom="POLYGON((";
                var d=geoObj.data;
                var geotext=d[0][0] +" "+d[0][1]+",";
                for(var j=1,jk=d.length;j<jk;j++){
                    var p=d[j];
                    geotext+=p[0] +" "+p[1]+",";
                }
                geotext+=d[0][0] +" "+d[0][1];
                geom+=geotext+"))";


            }else if(geoObj.gtype=='rectangle'){
                geom="POLYGON((";
                var d=geoObj.data;
                var geotext=d[0][0] +" "+d[0][1]+",";
                geotext+=d[0][0] +" "+d[1][1]+",";
                geotext+=d[1][0] +" "+d[1][1]+",";
                geotext+=d[1][0] +" "+d[0][1]+",";
                geotext+=d[0][0] +" "+d[0][1];
                geom+=geotext+"))";

            }
        }
    }
    document.getElementById("geometryTxt").value=geom;
    // var req={"type":"add","layerName":"geo_parking_polygon","feature":{"geometry":"\""+geom+"\"","properties":{"building_id":"b009","parking_no":"车位XX00"}}};
    // var req={"type":"add","layerName":"geo_parking_polygon","feature":{"geometry":"\""+geom+"\""}};
 //   var req={"type":"add","layerName":"geo_parking_polygon","geometry":geom,"properties":{"building_id":"b009","parking_no":"车位XX00"}};
    // alert(req);
   // reqSend("http://localhost:8080/apps/geo/map/fs",req);
}

function reqSend(url,data){
    // var myRequest = new Request.JSON({url:url}).post(data);
    var myRequest = new Request({url:url}).post(JSON.JSON.encode(data));
    return ;
    var myRequest = new Request.JSONP({
        url: url,
        data: data,
        async: true,
        link: "cancel",
        onComplete: function (resdata) {
          alert(resdata);
        } ,
        onFailure: function () {

        }
    }).send();
}

function loadTestData(){
    var geom=[{"gtype":"polygon","data":[[-107.9296875,60.732421875],[-101.42578125,42.275390625],[-59.58984375,52.119140625],[-70.6640625,59.501953125],[-76.11328125,62.138671875],[-84.90234375,60.99609375],[-84.90234375,61.875],[-84.90234375,61.875]]}];
    map.paletteLayer.loadData(geom);
}
function geoSql(geoValue){
    var sql="INSERT INTO geotools.parking_gis " +
        "(parking_uuid, parking_no, gis_point, width, height, opacity, angle)" +
        "VALUES(replace(uuid(),'-',''), '车位XX', ST_PolygonFromText('POLYGON (("+geoValue+"))')";
    sql+=", 100.0000, 100.0000, 1.0, 180)";
    return sql;
}
function drawSql(param){
    addFeature(param);
    return ;
    var geoData=param.data;
    if(geoData.length>0){
        for(var i=0,k=geoData.length;i<k;i++){
            var geoObj=geoData[i];
            if(geoObj.gtype == "polygon"){
                var d=geoObj.data;
                var geotext=d[0][0] +" "+d[0][1]+",";
                for(var j=1,jk=d.length;j<jk;j++){
                    var p=d[j];
                    geotext+=p[0] +" "+p[1]+",";
                }
                geotext+=d[0][0] +" "+d[0][1];
                alert(geoSql(geotext));

            }else if(geoObj.gtype=='rectangle'){
                var d=geoObj.data;
                var geotext=d[0][0] +" "+d[0][1]+",";
                geotext+=d[0][0] +" "+d[1][1]+",";
                geotext+=d[1][0] +" "+d[1][1]+",";
                geotext+=d[1][0] +" "+d[0][1]+",";
                geotext+=d[0][0] +" "+d[0][1];
                alert(geoSql(geotext));
            }
        }
    }
}