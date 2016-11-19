package com.left.green;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Inputtips.InputtipsListener;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.Query;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;
import com.left.adapter.PoiAdapter;
import com.left.bean.Poibean;
import com.left.tool.Bitmap_view;
import com.left.tool.MarkerView;
import com.left.util.DrawtoBitmap;
import com.left.util.ToastUtil;

public class GoOutActivity extends Activity implements LocationSource,
		AMapLocationListener, OnClickListener, OnPoiSearchListener,
		OnMarkerClickListener, InfoWindowAdapter {
	private AMap aMap;
	private MapView mapView;
	private OnLocationChangedListener mListener;
	private LocationManagerProxy mAMapLocationManager;
	private TextView city;
	private String cityid;
	private String adcode;
	private ImageView go, request, bicycle;
	private double lat;
	private double lon;
	int currentPage;
	private PoiSearch.Query query;// Poi��ѯ������
	private PoiSearch poiSearch;
	private PoiResult poiResult; // poi���صĽ��
	DrawtoBitmap d;
	LatLng location;
	AMapLocation amapLocation;
	AutoCompleteTextView input;
	ListView poilist;
	private ArrayList<Poibean> data;
	private PoiAdapter poi;
	ImageView search;
	int type; // ��һ����ַ 1 �����г� 2
	Bitmap_view bView;
	Bitmap bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_go);
		mapView = (MapView) findViewById(R.id.map);
		city = (TextView) findViewById(R.id.city);
		request = (ImageView) findViewById(R.id.request);
		bicycle = (ImageView) findViewById(R.id.bicycle);
		bicycle.setOnClickListener(this);
		go = (ImageView) findViewById(R.id.go);
		mapView.onCreate(savedInstanceState);
		input = (AutoCompleteTextView) findViewById(R.id.input);
		poilist = (ListView) findViewById(R.id.poilist);
		search = (ImageView) findViewById(R.id.search);
		d = new DrawtoBitmap(this);
		data = new ArrayList<Poibean>();
		poi = new PoiAdapter(this, data);
		poilist.setAdapter(poi);
		bView = new Bitmap_view();
		bitmap = bView.createViewBitmap(Color.GREEN);
		init();
		go.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(GoOutActivity.this,
						SearchActivity.class);
				intent.putExtra("city", cityid);
				intent.putExtra("adcode", adcode);
				intent.putExtra("lat", lat);
				intent.putExtra("lon", lon);
				startActivity(intent);
			}
		});
		request.setOnClickListener(this);

		poilist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				input.setText(data.get(arg2).getName());
				poilist.setVisibility(View.GONE);
			}
		});

		input.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				poilist.setVisibility(View.VISIBLE);
				// listView.setVisibility(View.GONE);
				String newText = s.toString().trim();
				Inputtips inputTips = new Inputtips(GoOutActivity.this,
						new InputtipsListener() {
							@Override
							public void onGetInputtips(List<Tip> tipList,
									int rCode) {
								if (rCode == 0) {// ��ȷ����
									data.clear();
									for (int i = 0; i < tipList.size(); i++) {
										if (tipList.get(i).getAdcode()
												.equals(adcode)) {
											Log.e("", i + "");
											Poibean poibean = new Poibean();
											poibean.setDistrict(tipList.get(i)
													.getDistrict());
											poibean.setName(tipList.get(i)
													.getName());
											data.add(poibean);
										}
									}
									poi.notifyDataSetChanged();
								}
							}
						});
				try {
					inputTips.requestInputtips(newText, city.getText()
							.toString());
					// ��һ��������ʾ��ʾ�ؼ��֣��ڶ�������Ĭ�ϴ���ȫ����Ҳ����Ϊ��������
				} catch (com.amap.api.services.core.AMapException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {

			}
		});

		search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// �����̹ر�
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				boolean isOpen = imm.isActive();// isOpen������true�����ʾ���뷨��
				if (isOpen) {
					imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
				}
				if (!input.getText().toString().equals("")) {
					type = 1;
					doPoiSearch(input.getText().toString());
				}
			}
		});
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (poilist.getVisibility() == View.VISIBLE) {
			poilist.setVisibility(View.GONE);
		} else {
			finish();
		}
	}

	public void doPoiSearch(String keyWord) {
		currentPage = 0;
		query = new PoiSearch.Query(keyWord, "", city.getText().toString());// ��һ��������ʾ�����ַ������ڶ���������ʾpoi�������ͣ�������������ʾpoi�������򣨿��ַ�������ȫ����
		query.setPageSize(10);// ����ÿҳ��෵�ض�����poiitem
		query.setPageNum(currentPage);// ���ò��һҳ

		poiSearch = new PoiSearch(this, query);
		poiSearch.setOnPoiSearchListener(this);
		// ������������Ϊ��lp��ΪԲ�ģ�����Χ2000�׷�Χ
		poiSearch.searchPOIAsyn();// �첽����
	}

	/**
	 * ��ʼ��AMap����
	 */
	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
		LatLng location = new LatLng(30.2, 120.0);
		aMap.moveCamera(CameraUpdateFactory.newLatLng(location));
		aMap.moveCamera(CameraUpdateFactory.zoomTo(10));
		aMap.setOnMarkerClickListener(this);// ��ӵ��marker�����¼�
		aMap.setInfoWindowAdapter(this);// �����ʾinfowindow�����¼�
		activate(null);
	}

	private void setUpMap() {
		aMap.getUiSettings().setCompassEnabled(true);
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// ����Ĭ�϶�λ��ť�Ƿ���ʾ

	}

	/**
	 * �˷��������
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * �˷��������
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
		deactivate();
	}

	/**
	 * �˷��������
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	/**
	 * �˷����Ѿ�����
	 */
	@Override
	public void onLocationChanged(Location location) {
	}

	/**
	 * ��λ�ɹ���ص�����
	 */
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (amapLocation != null) {
			if (amapLocation.getAMapException().getErrorCode() == 0) {
				city.setText(amapLocation.getCity());
				lat = amapLocation.getLatitude();
				lon = amapLocation.getLongitude();
				cityid = amapLocation.getCityCode();
				adcode = amapLocation.getAdCode();
				aMap.clear();
				location = new LatLng(amapLocation.getLatitude(),
						amapLocation.getLongitude());
				this.amapLocation = amapLocation;
				aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
						.position(location).title("�ҵ�λ��")
						.snippet(amapLocation.getAddress()).draggable(true));
				aMap.moveCamera(CameraUpdateFactory.newLatLng(location));
				aMap.moveCamera(CameraUpdateFactory.zoomTo(14));

				// setCircle();

			}
			deactivate();
		}
	}

	private void setCircle() {
		// TODO Auto-generated method stub
		aMap.addCircle(new CircleOptions().center(new LatLng(lat, lon))
				.radius(500).strokeColor(Color.argb(50, 0, 190, 0))
				.fillColor(Color.argb(100, 1, 190, 1)).strokeWidth(0));
	}

	/**
	 * ���λ
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
			/*
			 * mAMapLocManager.setGpsEnable(false);
			 * 1.0.2�汾��������������true��ʾ��϶�λ�а���gps��λ��false��ʾ�����綨λ��Ĭ����true Location
			 * API��λ����GPS�������϶�λ��ʽ
			 * ����һ�������Ƕ�λprovider���ڶ�������ʱ�������2000���룬������������������λ���ף����ĸ������Ƕ�λ������
			 */
			mAMapLocationManager.requestLocationUpdates(
					LocationProviderProxy.AMapNetwork, 5000, 10, this);
		}
	}

	/**
	 * ֹͣ��λ
	 */
	@Override
	public void deactivate() {
		if (mAMapLocationManager != null) {
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destroy();
		}
		mAMapLocationManager = null;
	}

	@Override
	public void onProviderDisabled(String arg0) {

	}

	@Override
	public void onProviderEnabled(String arg0) {

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.request:
			activate(null);
			break;
		case R.id.bicycle:
			type = 2;
			searchBicyclePoi();
			break;
		default:
			break;
		}
	}

	private void searchBicyclePoi() {
		// TODO Auto-generated method stub
		currentPage = 0;
		query = new PoiSearch.Query("���г����޵�", "", city.getText().toString());// ��һ��������ʾ�����ַ������ڶ���������ʾpoi�������ͣ�������������ʾpoi�������򣨿��ַ�������ȫ����
		query.setPageSize(10);// ����ÿҳ��෵�ض�����poiitem
		query.setPageNum(currentPage);// ���ò��һҳ

		poiSearch = new PoiSearch(this, query);
		poiSearch.setOnPoiSearchListener(this);
		poiSearch.setBound(new SearchBound(new LatLonPoint(lat, lon), 2000,
				true));//
		// ������������Ϊ��lp��ΪԲ�ģ�����Χ2000�׷�Χ
		poiSearch.searchPOIAsyn();// �첽����
	}

	/**
	 * POI����ص�
	 */
	@Override
	public void onPoiItemDetailSearched(PoiItemDetail result, int rCode) {
	}

	@Override
	public void onPoiSearched(PoiResult result, int rCode) {
		// TODO Auto-generated method stub
		MarkerView markerView = new MarkerView(this);
		if (rCode == 0) {
			if (result != null && result.getQuery() != null) {// ����poi�Ľ��
				if (result.getQuery().equals(query)) {// �Ƿ���ͬһ��
					poiResult = result;
					// ȡ����������poiitems�ж���ҳ
					List<PoiItem> poiItems = poiResult.getPois();// ȡ�õ�һҳ��poiitem���ݣ�ҳ��������0��ʼ
					List<SuggestionCity> suggestionCities = poiResult
							.getSearchSuggestionCitys();// ����������poiitem����ʱ���᷵�غ��������ؼ��ֵĳ�����Ϣ

					if (poiItems != null && poiItems.size() > 0) {
						aMap.clear();// ����֮ǰ��ͼ��

						for (int i = 0; i < poiItems.size(); i++) {
							aMap.addMarker(new MarkerOptions()
									.anchor(0.5f, 0.5f)
									.position(
											new LatLng(poiItems.get(i)
													.getLatLonPoint()
													.getLatitude(), poiItems
													.get(i).getLatLonPoint()
													.getLongitude()))
									.title(poiItems.get(i).getTitle())
									.snippet(poiItems.get(i).getSnippet())
									.draggable(true)
									.icon(markerView.getBitmapDes(i+1, bitmap)));
						}
						aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
								.position(location).title("�ҵ�λ��")
								.snippet(amapLocation.getAddress())
								.draggable(true));
						
						if (type == 1) {
							aMap.moveCamera(CameraUpdateFactory
									.newLatLng(new LatLng(poiItems.get(0)
											.getLatLonPoint().getLatitude(),
											poiItems.get(0).getLatLonPoint()
													.getLongitude())));
						}
						if (type == 2) {
							aMap.moveCamera(CameraUpdateFactory
									.newLatLng(location));
						}
						aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
					} else if (suggestionCities != null
							&& suggestionCities.size() > 0) {
					} else {
						ToastUtil.show(GoOutActivity.this, "no result");
					}
				}
			} else {
				ToastUtil.show(GoOutActivity.this, "δ�鵽���");
			}
		} else if (rCode == 27) {
			ToastUtil.show(GoOutActivity.this, "�����쳣");
		} else if (rCode == 32) {
			ToastUtil.show(GoOutActivity.this, "key����");
		} else {
			ToastUtil.show(GoOutActivity.this, "δ֪����" + rCode);
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if (marker.isInfoWindowShown()) {
			marker.hideInfoWindow();
		} else {
			marker.showInfoWindow();
		}

		return true;
	}

	@Override
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		// TODO Auto-generated method stub
		View view = getLayoutInflater().inflate(R.layout.poikeywordsearch_uri,
				null);
		TextView title = (TextView) view.findViewById(R.id.title);
		title.setText(marker.getTitle());

		TextView snippet = (TextView) view.findViewById(R.id.snippet);
		snippet.setText(marker.getSnippet());
		return view;
	}
}