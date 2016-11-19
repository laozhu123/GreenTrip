package com.left.green;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;
import com.left.util.Point;
import com.left.view.Busstation;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup.LayoutParams;

@SuppressWarnings("deprecation")
public class ARNaviActivity extends Activity implements AMapLocationListener,
OnPoiSearchListener{
   
	private LocationManagerProxy mLocationManagerProxy;
    private PoiSearch.Query query;// Poi查询条件类
	private PoiSearch poiSearch;
	private List<PoiItem> poiItems;// poi数据
	private LatLonPoint lp;// 记录用户位置
	private Camera camera;   
    private boolean preview;  
    private SurfaceView surfaceView ;
    private ImageView naveimg;
    private ImageView back;
    private FrameLayout frameLayout;
    private SensorManager sensorManager;  //获取传感器数据，用以动态改变naveimg
    private MySensorEventListener sensorEventListener; 
    private double azimuth;//设备方位角
	private List<Busstation> busstations;//记录所有公交站点
	private int tt;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_arnavi);
        init();
    }
 
    private void init() {
    	//确定界面的布局
    	frameLayout= (FrameLayout) findViewById(R.id.mark);
    	//获取感应器管理器    
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE); 
        sensorEventListener = new MySensorEventListener();  
        surfaceView=(SurfaceView) findViewById(R.id.surfaceView); 
        naveimg=(ImageView) findViewById(R.id.naveimg);
        back=(ImageView) findViewById(R.id.back);
        busstations=new ArrayList<Busstation>();
        surfaceView.getHolder().addCallback(new SurfaceViewCallback());  
    	mLocationManagerProxy = LocationManagerProxy.getInstance(this);
        //此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        //注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求
        //在定位结束后，在合适的生命周期调用destroy()方法     
        //其中如果间隔时间为-1，则定位只定一次
        mLocationManagerProxy.requestLocationData(
                LocationProviderProxy.AMapNetwork, -1, 15, this);
        mLocationManagerProxy.setGpsEnable(true);
        back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
    }
 
    @Override    
    protected void onResume()     
    {    
        //获取方向传感器    
        Sensor orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);  
        //传感器监听
        sensorManager.registerListener(sensorEventListener, orientationSensor, 
        		SensorManager.SENSOR_ORIENTATION);    
        super.onResume();    
    }    
    
    /**
     * 此方法需存在
     */
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener); 
        stopLocation();
    }
 
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if(amapLocation != null && amapLocation.getAMapException().getErrorCode() == 0){
            //获取位置信息
            Double geoLat = amapLocation.getLatitude();
            Double geoLng = amapLocation.getLongitude();  
            lp=new LatLonPoint(geoLat, geoLng);
            doSearchQuery();
        }
    }
    
    private void stopLocation() {
        if (mLocationManagerProxy != null) {
        	mLocationManagerProxy.removeUpdates(this);
        	mLocationManagerProxy.destory();
        }
        mLocationManagerProxy = null;
    }

    /**
	 * 开始进行poi搜索
	 */
	protected void doSearchQuery() {
		// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
		query = new PoiSearch.Query("", "公交车站", "");
		query.setPageSize(30);// 设置每页最多返回多少条poiitem
		query.setPageNum(0);// 设置查第一页
		if (lp != null) {
			poiSearch = new PoiSearch(this, query);
			poiSearch.setOnPoiSearchListener(this);
			// 设置搜索区域为以lp点为圆心，其周围1000米范围
			poiSearch.setBound(new SearchBound(lp, 1000, true));
			poiSearch.searchPOIAsyn();// 异步搜索
		}
	}
	
	/**
	 * POI搜索回调方法
	 */
	@Override
	public void onPoiSearched(PoiResult result, int rCode) {
		if (rCode == 0) {
			if (result != null && result.getQuery() != null) {
				if (result.getQuery().equals(query)) {
					// 取得第一页的poiitem数据，页数从数字0开始
					poiItems = result.getPois();
					if(poiItems.size()>0){
						//准备附近站点数据
						busstations.clear();
						for(int i=0;i<poiItems.size();i++){
							Busstation busstation=new Busstation(ARNaviActivity.this);
		            		busstation.setBusname(poiItems.get(i).toString());
		            		busstation.setBusdetail(poiItems.get(i).getSnippet());
		            		busstation.setVisibility(View.INVISIBLE);
		            		busstations.add(busstation);
		            		frameLayout.addView(busstations.get(i));
						}	
					}
				}
			} 
		} 
	}
	
	private final class SurfaceViewCallback implements Callback {  
        /** 
         * surfaceView 被创建成功后调用此方法 
         */  
        @Override  
        public void surfaceCreated(SurfaceHolder holder) {  
            /*  
             * 在SurfaceView创建好之后 打开摄像头 
             * 注意是 android.hardware.Camera 
             */  
            camera = Camera.open();  
            camera.setDisplayOrientation(90);
            /* 
             * This method must be called before startPreview(). otherwise surfaceview没有图像 
             */  
            try {  
                camera.setPreviewDisplay(holder);  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
            camera.startPreview();  
            preview = true;  
        }  
        @Override  
        public void surfaceChanged(SurfaceHolder holder,int format,int width,int height) {  
        }  
        /** 
         * SurfaceView 被销毁时释放掉 摄像头 
         */  
        @Override  
        public void surfaceDestroyed(SurfaceHolder holder) {  
            if(camera != null) {  
                /* 若摄像头正在工作，先停止它 */  
                if(preview) {  
                    camera.stopPreview();  
                    preview = false;  
                }  
                //如果注册了此回调，在release之前调用，否则release之后还回调，crash  
                camera.setPreviewCallback(null);  
                camera.release();  
            }  
        }        
    }  
	
	/**
	 * POI详情回调，未使用
	 */
	@Override
	public void onPoiItemDetailSearched(PoiItemDetail result, int rCode) {
		if (rCode == 0) {
			if (result != null) {
				
			}
		}
	}
	
	private final class MySensorEventListener implements SensorEventListener    
    {    
        //可以得到传感器实时测量出来的变化值    
        @SuppressLint("NewApi") @Override    
        public void onSensorChanged(SensorEvent event)     
        {       
            //得到方向的值    
            if(event.sensor.getType()==Sensor.TYPE_ORIENTATION)    
            {    
            	int j=0;
            	//方向角
            	azimuth=event.values[0];
                naveimg.setRotation(event.values[0]); 
            	for(int i=0;i<busstations.size();i++){
            		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams
              				.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            		  Point point=setposition(lp,i);
                      layoutParams.leftMargin=(int)point.getX();
            		 if(point.isShow()){
            			 layoutParams.topMargin=30+130*j;
            			 frameLayout.updateViewLayout(busstations.get(i), layoutParams);
                     	 busstations.get(i).setVisibility(View.VISIBLE);   
                     	 j++;
            		 }
            		 else {
            			 frameLayout.updateViewLayout(busstations.get(i), layoutParams);
                     	 busstations.get(i).setVisibility(View.GONE);
            		 }	
            	}
            }    
        }    
        //重写变化    
        @Override    
        public void onAccuracyChanged(Sensor sensor, int accuracy)     
        {    
        }    
    }    

	@Override
	public void onLocationChanged(Location location) {
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}
	
	//计算站点在屏幕上显示的地方
	private Point setposition(LatLonPoint position,int i){
	    double shida1=Math.atan2(poiItems.get(i).getLatLonPoint().getLatitude()-
	    		position.getLatitude(),(poiItems.get(i).getLatLonPoint().getLongitude()
	    		-position.getLongitude())*Math.sin(poiItems.get(i).getLatLonPoint().getLatitude()))*180/Math.PI;
		tt++;	
		shida1=shida1-90;
		if(shida1<0)
			shida1+=360;
	    Point point=new Point();
		WindowManager windowManager = (WindowManager)getSystemService(Context.
				WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        double midwidth = display.getWidth()/2;
        double k=midwidth/15;
        double magin=(shida1-azimuth);
        double res=magin;
        if(res>180)
        	res=res-180;
        else if(res<-180)
        	res=res+180;
        else 
        	res=shida1-azimuth;
		point.setX(midwidth+k*magin);
		point.setY(40);
		if(tt>400){
			Log.e("left", poiItems.get(i).toString()+"公交站角度"+shida1);
			Log.e("margin", magin+"");
			Log.e("azimuth", azimuth+"");
			Log.e("res", res+"");
		}
		if(tt>402){
			tt=0;
		}
		if(Math.abs(magin)<50)
			point.setShow(true);
		else
			point.setShow(false);
		return point;	
	}
}
