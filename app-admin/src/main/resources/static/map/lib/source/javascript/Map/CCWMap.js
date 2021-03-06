
(function(){
    MapGIS.extend({
            CCWMap:new Class({
                Extends:MapGIS.Map,
                Implements:[Events,Options],
                options:{
                    fullMode:false,
                    maptype: 0,
                    level: 1,
                    levelMax: 6
                },
                paletteLayer:null,
                imageLayer: null,
                trafficLayer:null,
                markerLayer:null,
                ccwLayer:null,
                baseLayer:null,
                winTimeout:null,
                setActiveTool:function(activetype)
                {

                    switch(activetype)
                    {
                        case "*1":
                            if(this.paletteActiveFunHand!=null)
                            {
                                this.paletteLayer.removeEvent("change",this.paletteActiveFunHand);
                                this.paletteActiveFunHand=null;
                            }
                            this.paletteActiveFunHand=this.paletteActiveFun.bind(this);
                            this.paletteLayer.addEvent("change",this.paletteActiveFunHand);
                            this.paletteLayer.clear();
                            this.paletteLayer.paintPolygon(true);
                            break;
                        case "*2":
                            if(this.paletteActiveFunHand!=null)
                            {
                                this.paletteLayer.removeEvent("change",this.paletteActiveFunHand);
                                this.paletteActiveFunHand=null;
                            }
                            this.paletteActiveFunHand=this.paletteActiveFun.bind(this);
                            this.paletteLayer.addEvent("change",this.paletteActiveFunHand);
                            this.paletteLayer.clear();
                            this.paletteLayer.paintCircle(true);
                            break;
                        default:
                            break;

                    }
                    this.parent(activetype);
                    if(this.paletteLayer){
                        this.paletteLayer.stop();
                    }
                },


                initialize: function (container, options) {
                    this.options.contentMenu.obj =[];
                    var szmodel=new MapGIS.CCWModel();
                    if(typeof(USEREXTENT)!="undefined")
                    {
                        this.setOptions(USEREXTENT);
                    }
                    this.parent(container,options,szmodel);
                    var baseMenuList= [{ title: "??????", fn: function () { this.setActiveTool('+'); } .bind(this) }, { title: "??????", fn: function () { this.setActiveTool('-'); } .bind(this) }, { title: "??????", fn: function () { this.setActiveTool('$'); } .bind(this) }, { title: "??????", fn: function () { this.reposition(); } .bind(this) } ];
                    this.options.contentMenu.obj = new MapGIS.MapContentMenu(this,{baseMenuList:baseMenuList});
                    this.options.contentMenu.obj.div.inject(this.toolsdiv);
                    this.customLoading();
                    this.fireEvent("load", true);
                },
                customLoading:function(){
                    // this.baseLayer = new MapGIS.Layers.OpenstreetLayer({id:"LB01",zIndex:1});
                    this.baseLayer =new MapGIS.Layers.CCWFeatureLayer({id:"ccwLayer01",zIndex:1,url:"http://localhost:8080/apps/geo/map/image/{z}/{y}/{x}",startShowLevel:0});
                    this.addLayer(this.baseLayer);
                    // this.ccwLayer = new MapGIS.Layers.CCWFeatureLayer({id:"ccwLayer01",url:"http://localhost:8080/apps/geo/map/wms?bbox={bbox}&z={z}&x={x}&y={y}&area={area}&reload=true",startShowLevel:1,zIndex:100});
                    this.imageLayer = new MapGIS.Layers.MarkerLayer({ id: "imageLayer01", zIndex:25 });
                    this.markerLayer = new MapGIS.Layers.MarkerLayer({ id: "markerLayer01", zIndex:26, effects:true });
                    var stopEvent=this.stopEvent.bind(this);
                    this.paletteLayer=new MapGIS.Layers.PaletteLayer({id:"mappalettelayer",zIndex:20});
                    this.addLayers([ this.markerLayer,this.imageLayer,this.paletteLayer]);
                    if(this._maploading)
                    {
                        this._maploading();
                    }
                },
                aviMap:function()
                {
                    var url="http://mt{s}.google.cn/vt/lyrs=s@164&hl=zh-CN&gl=CN&src=app&expIds=201527&rlbl=1&x={y}&y={x}&z={z}";
                    this.baseLayer.setOptions({url:url,allowRefresh:true});
                    if(this.trafficLayer==null)
                    {
                        this.trafficLayer = new MapGIS.Layers.OpenstreetLayer({id:"LB02",url:"http://mt{s}.google.cn/vt/imgtp=png32&lyrs=h@285000000&hl=zh-CN&gl=CN&src=app&expIds=201527&rlbl=1&s=Galileo&x={y}&y={x}&z={z}",zIndex:1});
                        this.addLayer(this.trafficLayer);
                    }

                    this.trafficLayer.show();
                    this.baseLayer.refresh();

                    this.baseLayer.setOptions({allowRefresh:false});
                },
                defaultMap:function()
                {
                    var url="http://mt{s}.google.cn/vt/x={y}&y={x}&z={z}";
                    // this.baseLayer.setOptions({url:url,allowRefresh:true});
                    this.baseLayer.setOptions({allowRefresh:true});
                    if(this.trafficLayer==null)
                    {
                        this.trafficLayer = new MapGIS.Layers.OpenstreetLayer({id:"LB02",url:"http://mt{s}.google.cn/vt/imgtp=png32&lyrs=h@285000000&hl=zh-CN&gl=CN&src=app&expIds=201527&rlbl=1&s=Galileo&x={y}&y={x}&z={z}",zIndex:1});
                        this.trafficLayer.hide();
                        this.addLayer(this.trafficLayer);
                    }
                    this.trafficLayer.hide();
                    this.baseLayer.refresh();
                    this.baseLayer.setOptions({allowRefresh:false});
                }
            })
        }
    )
})();