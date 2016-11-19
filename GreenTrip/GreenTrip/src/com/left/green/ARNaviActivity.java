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
    private PoiSearch.Query query;// Poi��ѯ������
	private PoiSearch poiSearch;
	private List<PoiItem> poiItems;// poi����
	private LatLonPoint lp;// ��¼�û�λ��
	private Camera camera;   
    private boolean preview;  
    private SurfaceView surfaceView ;
    private ImageView naveimg;
    private ImageView back;
    private FrameLayout frameLayout;
    private SensorManager sensorManager;  //��ȡ���������ݣ����Զ�̬�ı�naveimg
    private MySensorEventListener sensorEventListener; 
    private double azimuth;//�豸��λ��
	private List<Busstation> busstations;//��¼���й���վ��
	private int tt;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_arnavi);
        init();
    }
 
    private void init() {
    	//ȷ������Ĳ���
    	frameLayout= (FrameLayout) findViewById(R.id.mark);
    	//��ȡ��Ӧ��������    
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE); 
        sensorEventListener = new MySensorEventListener();  
        surfaceView=(SurfaceView) findViewById(R.id.surfaceView); 
        naveimg=(ImageView) findViewById(R.id.naveimg);
        back=(ImageView) findViewById(R.id.back);
        busstations=new ArrayList<Busstation>();
        surfaceView.getHolder().addCallback(new SurfaceViewCallback());  
    	mLocationManagerProxy = LocationManagerProxy.getInstance(this);
        //�˷���Ϊÿ���̶�ʱ��ᷢ��һ�ζ�λ����Ϊ�˼��ٵ������Ļ������������ģ�
        //ע�����ú��ʵĶ�λʱ��ļ���������ں���ʱ�����removeUpdates()������ȡ����λ����
        //�ڶ�λ�������ں��ʵ��������ڵ���destroy()����     
        //����������ʱ��Ϊ-1����λֻ��һ��
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
        //��ȡ���򴫸���    
        Sensor orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);  
        //����������
        sensorManager.registerListener(sensorEventListener, orientationSensor, 
        		SensorManager.SENSOR_ORIENTATION);    
        super.onResume();    
    }    
    
    /**
     * �˷��������
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
            //��ȡλ����Ϣ
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
	 * ��ʼ����poi����
	 */
	protected void doSearchQuery() {
		// ��һ��������ʾ�����ַ������ڶ���������ʾpoi�������ͣ�������������ʾpoi�������򣨿��ַ�������ȫ����
		query = new PoiSearch.Query("", "������վ", "");
		query.setPageSize(30);// ����ÿҳ��෵�ض�����poiitem
		query.setPageNum(0);// ���ò��һҳ
		if (lp != null) {
			poiSearch = new PoiSearch(this, query);
			poiSearch.setOnPoiSearchListener(this);
			// ������������Ϊ��lp��ΪԲ�ģ�����Χ1000�׷�Χ
			poiSearch.setBound(new SearchBound(lp, 1000, true));
			poiSearch.searchPOIAsyn();// �첽����
		}
	}
	
	/**
	 * POI�����ص�����
	 */
	@Override
	public void onPoiSearched(PoiResult result, int rCode) {
		if (rCode == 0) {
			if (result != null && result.getQuery() != null) {
				if (result.getQuery().equals(query)) {
					// ȡ�õ�һҳ��poiitem���ݣ�ҳ��������0��ʼ
					poiItems = result.getPois();
					if(poiItems.size()>0){
						//׼������վ������
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
         * surfaceView �������ɹ�����ô˷��� 
         */  
        @Override  
        public void surfaceCreated(SurfaceHolder holder) {  
            /*  
             * ��SurfaceView������֮�� ������ͷ 
             * ע���� android.hardware.Camera 
             */  
            camera = Camera.open();  
            camera.setDisplayOrientation(90);
            /* 
             * This method must be called before startPreview(). otherwise surfaceviewû��ͼ�� 
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
         * SurfaceView ������ʱ�ͷŵ� ����ͷ 
         */  
        @Override  
        public void surfaceDestroyed(SurfaceHolder holder) {  
            if(camera != null) {  
                /* ������ͷ���ڹ�������ֹͣ�� */  
                if(preview) {  
                    camera.stopPreview();  
                    preview = false;  
                }  
                //���ע���˴˻ص�����release֮ǰ���ã�����release֮�󻹻ص���crash  
                camera.setPreviewCallback(null);  
                camera.release();  
            }  
        }        
    }  
	
	/**
	 * POI����ص���δʹ��
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
        //���Եõ�������ʵʱ���������ı仯ֵ    
        @SuppressLint("NewApi") @Override    
        public void onSensorChanged(SensorEvent event)     
        {       
            //�õ������ֵ    
            if(event.sensor.getType()==Sensor.TYPE_ORIENTATION)    
            {    
            	int j=0;
            	//�����
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
        //��д�仯    
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
	
	//����վ������Ļ����ʾ�ĵط�
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
			Log.e("left", poiItems.get(i).toString()+"����վ�Ƕ�"+shida1);
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
