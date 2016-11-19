package com.left.green;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Tip;
import com.amap.api.services.help.Inputtips.InputtipsListener;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.Query;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.FromAndTo;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.WalkRouteResult;
import com.left.adapter.BusLineAdapter;
import com.left.adapter.PoiAdapter;
import com.left.bean.Poibean;
import com.left.instruction.BusLineInstruction;
import com.left.tool.Values;
import com.left.util.AMapUtil;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends Activity implements OnRouteSearchListener,
		OnPoiSearchListener ,OnClickListener{

	private ImageView back;
	private ImageView walk;
	private ImageView bus;
	private ImageView bicycle;
	private ImageView mix;
	private TextView search;
	private ListView  poilist;
	private View listlayout,prefer;
	private ListView  listView;
	private String mode = "bus";
	private AutoCompleteTextView startText;// 输入搜索关键字
	private AutoCompleteTextView endText;// 输入搜索关键字
	private String city = "";// 记录城市
	private List<LatLonPoint> point;
	private LatLonPoint position;
	private BusLineAdapter adapter;
	private ArrayList<BusLineInstruction> buslineInstructions;
	private ArrayList<Poibean> data;
	private PoiAdapter poi;
	private int type; // 1 startpoint 2 endpoint

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search);
		back = (ImageView) findViewById(R.id.back);
		walk = (ImageView) findViewById(R.id.walk);
		bus = (ImageView) findViewById(R.id.bus);
		bicycle = (ImageView) findViewById(R.id.bicycle);
		mix = (ImageView) findViewById(R.id.mix);
		search = (TextView) findViewById(R.id.search);
		listlayout = findViewById(R.id.listlayout);
		listView = (ListView) findViewById(R.id.list);
		poilist = (ListView) findViewById(R.id.poilist);
		prefer = findViewById(R.id.prefer);
		buslineInstructions = new ArrayList<BusLineInstruction>();
		adapter = new BusLineAdapter(this, buslineInstructions);
		listView.setAdapter(adapter);
		bus.setImageResource(R.drawable.busnormal);
		Intent intent = getIntent();
		city = intent.getStringExtra("city");
		position = new LatLonPoint(intent.getDoubleExtra("lat", 0),
				intent.getDoubleExtra("lon", 0));
		point = new ArrayList<LatLonPoint>();
		startText = (AutoCompleteTextView) findViewById(R.id.startpoint);
		endText = (AutoCompleteTextView) findViewById(R.id.endpoint);
		endText.requestFocus();
		data = new ArrayList<Poibean>();
		poi = new PoiAdapter(this, data);
		poilist.setAdapter(poi);
		
		poilist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				switch (type) {
				case 1:
					startText.setText(data.get(arg2).getName()
							+ data.get(arg2).getDistrict());
					break;
				case 2:
					endText.setText(data.get(arg2).getName()
							+ data.get(arg2).getDistrict());
					break;
				default:
					break;
				}
				poilist.setVisibility(View.GONE);
			}
		});
		prefer.setOnClickListener(this);
		// 添加文本输入框监听事件
		startText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				type = 1;
				poilist.setVisibility(View.VISIBLE);
				listlayout.setVisibility(View.GONE);
//				listView.setVisibility(View.GONE);
				String newText = s.toString().trim();
				Inputtips inputTips = new Inputtips(SearchActivity.this,
						new InputtipsListener() {
							@Override
							public void onGetInputtips(List<Tip> tipList,
									int rCode) {
								if (rCode == 0) {// 正确返回
									data.clear();
									for (int i = 0; i < tipList.size(); i++) {
										if (tipList
												.get(i)
												.getAdcode()
												.equals(getIntent()
														.getStringExtra(
																"adcode"))) {
											
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
					inputTips.requestInputtips(newText, city);
					// 第一个参数表示提示关键字，第二个参数默认代表全国，也可以为城市区号
				} catch (AMapException e) {
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
		endText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				type = 2;
				poilist.setVisibility(View.VISIBLE);
				listlayout.setVisibility(View.GONE);
//				listView.setVisibility(View.GONE);
				String newText = s.toString().trim();
				Inputtips inputTips = new Inputtips(SearchActivity.this,
						new InputtipsListener() {
							@Override
							public void onGetInputtips(List<Tip> tipList,
									int rCode) {
								if (rCode == 0) {// 正确返回
									data.clear();
									for (int i = 0; i < tipList.size(); i++) {
										if (tipList
												.get(i)
												.getAdcode()
												.equals(getIntent()
														.getStringExtra(
																"adcode"))) {
											Log.e("", i+"");
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
					inputTips.requestInputtips(newText, city);
					// 第一个参数表示提示关键字，第二个参数默认代表全国，也可以为城市区号
				} catch (AMapException e) {
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
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		bus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				reset();
				bus.setImageResource(R.drawable.busnormal);
				mode = "bus";
			}
		});
		walk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				listlayout.setVisibility(View.GONE);
				reset();
				walk.setImageResource(R.drawable.walknormal);
				mode = "walk";
			}
		});
		bicycle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				listlayout.setVisibility(View.GONE);
				reset();
				bicycle.setImageResource(R.drawable.bicyclenormal);
				mode = "bicycle";
			}
		});
		mix.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				listlayout.setVisibility(View.GONE);
				reset();
				mix.setImageResource(R.drawable.mixnormal);
				mode = "mix";
			}
		});
		search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// 将键盘关闭
//				listView.setVisibility(View.VISIBLE);
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				boolean isOpen = imm.isActive();// isOpen若返回true，则表示输入法打开
				if (isOpen) {
					imm.hideSoftInputFromWindow(startText.getWindowToken(), 0);
					imm.hideSoftInputFromWindow(endText.getWindowToken(), 0);
				}
				String startp = AMapUtil.checkEditText(startText);
				String endp = AMapUtil.checkEditText(endText);
				if (startp.equals(""))
					Toast.makeText(SearchActivity.this, "请输入起点",
							Toast.LENGTH_SHORT).show();
				else if (endp.equals(""))
					Toast.makeText(SearchActivity.this, "请输入目的地",
							Toast.LENGTH_SHORT).show();
				else if (startp.equals("我的位置")) {
					point.clear();
					point.add(position);
					doPoiSearch(endp);
				} else {
					point.clear();
					doPoiSearch(startp);
					doPoiSearch(endp);
				}
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(SearchActivity.this,
						RouteActivity.class);
				intent.putExtra("city", city);
				intent.putExtra("mode", mode);
				intent.putExtra("buspathnum", arg2);
				double[] pathpoint = new double[4];
				pathpoint[0] = point.get(0).getLatitude();
				pathpoint[1] = point.get(0).getLongitude();
				pathpoint[2] = point.get(1).getLatitude();
				pathpoint[3] = point.get(1).getLongitude();
				intent.putExtra("pathpoint", pathpoint);
				startActivity(intent);
			}
		});
	}

	private void reset() {
		bus.setImageResource(R.drawable.buspress);
		walk.setImageResource(R.drawable.walk);
		bicycle.setImageResource(R.drawable.bicyclepress);
		mix.setImageResource(R.drawable.mixpress);
	}

	// 搜索公交信息
	private void searchbus(LatLonPoint startPoint, LatLonPoint endPoint) {
		FromAndTo fromAndTo = new FromAndTo(startPoint, endPoint);
		RouteSearch routeSearch = new RouteSearch(this);
		routeSearch.setRouteSearchListener(this);
		RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(
				fromAndTo, RouteSearch.BusDefault, city, 0);
		routeSearch.calculateBusRouteAsyn(query);
	}

	@Override
	public void onBusRouteSearched(BusRouteResult busRouteResult, int rCode) {
		// 路径规划中公交模式
		if (rCode == 0 && busRouteResult != null
				&& busRouteResult.getPaths() != null
				&& busRouteResult.getPaths().size() > 0) {
			buslineInstructions.clear();
			poilist.setVisibility(View.GONE);
			listlayout.setVisibility(View.VISIBLE);
			for (int i = 0; i < busRouteResult.getPaths().size(); i++) {
				// 遍历所有方案
				BusPath busPath = busRouteResult.getPaths().get(i);
				BusLineInstruction instruction = new BusLineInstruction();
				String buslinename = "";
				double distance = busPath.getDistance();
				long duration = busPath.getDuration();
				float walk = busPath.getWalkDistance();
				// 取出对应方案中所有路段的公交线路名
				for (int j = 0; j < busPath.getSteps().size(); j++) {
					if (busPath.getSteps().get(j).getBusLine() != null) {
						if (j > 0)
							buslinename += "->";
						for (int k = 0; k < busPath.getSteps().get(j)
								.getBusLine().getBusLineName().length() - 1; k++) {
							if (!busPath.getSteps().get(j).getBusLine()
									.getBusLineName().substring(k, k + 1)
									.equals("("))
								buslinename += busPath.getSteps().get(j)
										.getBusLine().getBusLineName()
										.substring(k, k + 1);
							else
								break;
						}
					}
				}
				instruction.setName(buslinename);
				DecimalFormat df = new DecimalFormat("###.0");
				instruction.setDetail(duration / 3600 + "小时" + duration % 3600
						/ 60 + "分钟  |  " + df.format(distance / 1000) + "公里  |  "
						+ "步行" + (int) walk + "米");
				buslineInstructions.add(instruction);
			}
			adapter.notifyDataSetChanged();
		} else {
			Toast.makeText(SearchActivity.this, "无相关公交线路信息", Toast.LENGTH_SHORT)
					.show();
		}
	}

	public void doPoiSearch(String keyWord) {
		Query query = new PoiSearch.Query(keyWord, "", city);
		query.setPageSize(10);// 设置每页最多返回多少条poiitem
		query.setPageNum(0);// 设置查第一页
		PoiSearch poiSearch = new PoiSearch(this, query);
		poiSearch.setOnPoiSearchListener(this);
		poiSearch.searchPOIAsyn();
	}

	@Override
	public void onDriveRouteSearched(DriveRouteResult arg0, int arg1) {

	}

	@Override
	public void onWalkRouteSearched(WalkRouteResult arg0, int arg1) {

	}

	@Override
	public void onPoiItemDetailSearched(PoiItemDetail arg0, int arg1) {

	}

	@Override
	public void onPoiSearched(PoiResult result, int rcode) {
		// 搜索POI的结果
		if (rcode == 0 && result != null && result.getQuery() != null) {
			ArrayList<PoiItem> poiItems = result.getPois();
			// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
			if (poiItems != null && poiItems.size() > 0) {
				point.add(poiItems.get(0).getLatLonPoint());
				if (point.size() == 2) {
					if (mode.equals("bus")) {
						searchbus(point.get(0), point.get(1));
					}else {
//直接转到routeactivity
						Intent intent = new Intent(SearchActivity.this,
								RouteActivity.class);
						intent.putExtra("city", city);
						intent.putExtra("mode", mode);
						intent.putExtra("buspathnum", 0);
						double[] pathpoint = new double[4];
						pathpoint[0] = point.get(0).getLatitude();
						pathpoint[1] = point.get(0).getLongitude();
						pathpoint[2] = point.get(1).getLatitude();
						pathpoint[3] = point.get(1).getLongitude();
						intent.putExtra("pathpoint", pathpoint);
						startActivity(intent);
					}
				}
			} else {
				Toast.makeText(SearchActivity.this, "无相关地理位置信息",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(SearchActivity.this, "请确认网络是否连通", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.prefer:
			Toast.makeText(SearchActivity.this, "偏好选择", Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
	}
}
