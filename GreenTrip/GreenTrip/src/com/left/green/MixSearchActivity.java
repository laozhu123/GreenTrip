package com.left.green;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.PoiCreator;
import com.amap.api.maps.overlay.BusRouteOverlay;
import com.amap.api.maps.overlay.WalkRouteOverlay;
import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.FromAndTo;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.WalkStep;
import com.left.bean.FirstItem;
import com.left.bean.SecondItem;
import com.left.tool.Values;
import com.left.util.PoiBike;

public class MixSearchActivity extends Activity implements
		OnRouteSearchListener, OnPoiSearchListener, OnClickListener {

	private LatLonPoint startPoint;
	private LatLonPoint endPoint;
	private LatLonPoint secondbus;
	private FromAndTo fromAndTo;
	private MapView mMapView;
	private AMap mAMap;
	private ImageView back;
	private TextView title;
	private TextView path;
	private Button ar;
	private RelativeLayout detail;
	private LinearLayout taost;
	private String city;
	private PoiResult poiResult; // poi返回的结果
	private PoiSearch.Query query;// Poi查询条件类
	private PoiSearch poiSearch;// POI搜索
	private RouteSearch routeSearch;
	private int busMode = RouteSearch.BusDefault;// 公交默认模式
	private int walkMode = RouteSearch.WalkDefault;// 步行默认模式
	private String mode;
	DecimalFormat df;
	private int type; // 1 获取距离
	private int progress;
	int poibicycle; // 1 起点附近自行车 2公交车站点旁的
	private boolean isfirstbike = true;// 用以记录是否首次搜索自行车租赁点，以排除mix模式下的再次搜索
	private List<PoiBike> poiBikes;
	private BusPath busPath;
	private long time;
	private float walkdistance;
	private float busdistance;
	private float bikedistance;
	double[] pathpoint;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_route);
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.onCreate(savedInstanceState);

		initView();
		initData();
	}

	private void initData() {
		// TODO Auto-generated method stub
		df = new DecimalFormat("###.0");
		mAMap = mMapView.getMap();
		Intent intent = getIntent();
		/* 取出Intent中附加的数据 */
		city = intent.getStringExtra("city");
		pathpoint = intent.getDoubleArrayExtra("pathpoint");
		startPoint = new LatLonPoint(pathpoint[0], pathpoint[1]);
		endPoint = new LatLonPoint(pathpoint[2], pathpoint[3]);
		fromAndTo = new FromAndTo(startPoint, endPoint);
		routeSearch = new RouteSearch(this);
		routeSearch.setRouteSearchListener(this);
		detail.setOnClickListener(this);
		taost.setOnClickListener(this);
		progress = 1;
		walkdistance = 0;
		busdistance = 0;
		bikedistance = 0;
		time = 0;
		poiBikes = new ArrayList<PoiBike>();
		title.setText("混合换乘路线");
		Values.firstItems = new ArrayList<FirstItem>();
		Values.secondItems = new ArrayList<SecondItem>();
		Values.secondItems.clear();
		Values.firstItems.clear();
		FirstItem item = new FirstItem();
		item.setTitle("我的位置");
		item.setType(4);
		Values.firstItems.add(item);
		SecondItem item2 = new SecondItem();
		item2.setSteps(new ArrayList<String>());
		Values.secondItems.add(item2);

		// 获取距离
		mode = "walk";
		type = 1;
		WalkRouteQuery query = new WalkRouteQuery(fromAndTo, walkMode);
		routeSearch.calculateWalkRouteAsyn(query);

	}

	private void initView() {
		// TODO Auto-generated method stub

		back = (ImageView) findViewById(R.id.back);
		title = (TextView) findViewById(R.id.title);
		detail = (RelativeLayout) findViewById(R.id.detail);
		taost = (LinearLayout) findViewById(R.id.tosat);
		path = (TextView) findViewById(R.id.path);
		ar = (Button) findViewById(R.id.arnavi);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.detail:
		case R.id.tosat:
			FirstItem item1_2 = new FirstItem();
			item1_2.setType(4);
			item1_2.setTitle("目的地");
			Values.firstItems.add(item1_2);
			Values.secondItems.add(new SecondItem());
			startActivity(new Intent(MixSearchActivity.this,
					RouteDetailActivity.class));
			break;
		default:
			break;
		}
	}

	@Override
	public void onPoiItemDetailSearched(PoiItemDetail arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPoiSearched(PoiResult result, int rCode) {
		if (rCode == 0 && result != null && result.getQuery() != null
				&& result.getQuery().equals(query)) {// 搜索poi的结果
			poiResult = result;
			// 取得搜索到的poiitems有多少页
			List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
			// 需要找到两个自行车租赁点,且取头和中间的两个自行车站点
			if (poiItems != null) {
				if (poiItems.size() > 0) {
					if (poibicycle == 1) {
						poiBikes.clear();
						PoiBike bike = new PoiBike();
						bike.setBikeRental(poiItems.get(0).toString());
						bike.setPosition(poiItems.get(0).getLatLonPoint());
						poiBikes.add(bike);
						poibicycle = 2;
						if (secondbus != null) {
							doSearchQuery(secondbus);
						}else {
							doSearchQuery(new LatLonPoint(pathpoint[2], pathpoint[3]));
						}
						
						
					} else {
						poibicycle = 1;
						PoiBike bike = new PoiBike();
						bike.setBikeRental(poiItems.get(0).toString());
						bike.setPosition(poiItems.get(0).getLatLonPoint());
						poiBikes.add(bike);
						// 可以处理了
						LatLonPoint endPoint = new LatLonPoint(poiBikes.get(0)
								.getPosition().getLatitude(), poiBikes.get(0)
								.getPosition().getLongitude());

						progress = 1;
						Log.e("", poiBikes.size()+"");
						fromAndTo = new FromAndTo(startPoint, endPoint);
						WalkRouteQuery query = new WalkRouteQuery(fromAndTo,
								walkMode);
						routeSearch.calculateWalkRouteAsyn(query);
					}
				} else {
					// 没有自行车点直接使用公交车 也就是降级
					if (mode.equals("bic")) {
						mode = "walk";
						type = 2;
						WalkRouteQuery query = new WalkRouteQuery(fromAndTo, walkMode);
						routeSearch.calculateWalkRouteAsyn(query);
						return;
					}
					mode = "bus";
					RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(
							fromAndTo, busMode, city, 0);
					routeSearch.calculateBusRouteAsyn(query);
				}
			} else {
				// 没有自行车点直接使用公交车 也就是降级
				if (mode.equals("bic")) {
					mode = "walk";
					type = 2;
					WalkRouteQuery query = new WalkRouteQuery(fromAndTo, walkMode);
					routeSearch.calculateWalkRouteAsyn(query);
					return;
				}
				mode = "bus";
				RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(
						fromAndTo, busMode, city, 0);
				routeSearch.calculateBusRouteAsyn(query);
			}

		} else {
			Toast.makeText(MixSearchActivity.this, "请确认是否联网",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onBusRouteSearched(BusRouteResult busRouteResult, int rCode) {
		// TODO Auto-generated method stub
		if (rCode == 0 && busRouteResult != null
				&& busRouteResult.getPaths() != null
				&& busRouteResult.getPaths().size() > 0) {
			int busnum;
			int valid;
			for (int i = 0; i < busRouteResult.getPaths().size(); i++) {
				busPath = busRouteResult.getPaths().get(i);
				busnum = 0;
				valid = 1; // 用于判断线路是否可用
				for (int j = 0; j < busPath.getSteps().size(); j++) {
					if (busPath.getSteps().get(j).getBusLine() != null) {
						if (busPath.getSteps().get(j).getBusLine()
								.getPassStations() != null) {
							// 该条线路无用
							busnum++;
							continue;
						} else {
							valid = 0;
							break;
						}
					}
				}
				if (valid == 1) {
					if (mode.equals("notsure")) {
						if (busnum == 1) {
							if (busPath.getSteps().get(0).getWalk().getDistance() <= 1000) {
								// 步行去公交车站
								String busname = "";
								for (int j = 0; j < busPath.getSteps().size(); j++) {
									if (busPath.getSteps().get(j).getBusLine() != null) {
										if (j > 0)
											busname += "->";
										for (int k = 0; k < busPath.getSteps()
												.get(j).getBusLine()
												.getBusLineName().length() - 1; k++) {
											if (!busPath.getSteps().get(j)
													.getBusLine().getBusLineName()
													.substring(k, k + 1)
													.equals("("))
												busname += busPath.getSteps()
														.get(j).getBusLine()
														.getBusLineName()
														.substring(k, k + 1);
											else
												break;
										}
									}

								}
								path.setText("共"
										+ df.format(busPath.getDistance() / 1000)
										+ "公里" + "|约" + busPath.getDuration()
										/ 3600 + "小时" + busPath.getDuration()
										% 3600 / 60 + "分钟|步行"
										+ (int) busPath.getWalkDistance() + "米");
								Values.busline = busname;
								Values.path = path.getText().toString();

								for (int j = 0; j < busPath.getSteps().size(); j++) {

									// 1
									FirstItem item = new FirstItem();
									item.setType(1);
									item.setTitle("步行"
											+ (int) busPath.getSteps().get(j)
													.getWalk().getDistance() + "米");
									Values.firstItems.add(item);

									List<WalkStep> heel = busPath.getSteps().get(j)
											.getWalk().getSteps();
									SecondItem item2 = new SecondItem();
									item2.setSteps(new ArrayList<String>());
									for (int k = 0; k < heel.size(); k++) {
										if (heel.get(k).getInstruction() == null) {
											break;
										}
										item2.getSteps().add(
												heel.get(k).getInstruction());
									}
									Values.secondItems.add(item2);

									// 2
									if (busPath.getSteps().get(j).getBusLine() != null) {
										// 获取公交车名
										String buslinename = "";
										for (int k = 0; k < busPath.getSteps()
												.get(j).getBusLine()
												.getBusLineName().length() - 1; k++) {
											if (!busPath.getSteps().get(j)
													.getBusLine().getBusLineName()
													.substring(k, k + 1)
													.equals("("))
												buslinename += busPath.getSteps()
														.get(j).getBusLine()
														.getBusLineName()
														.substring(k, k + 1);
											else
												break;
										}

										// 1起点
										FirstItem item1_1 = new FirstItem();
										item1_1.setTitle(busPath.getSteps().get(j)
												.getBusLine()
												.getDepartureBusStation()
												.getBusStationName()
												+ "站上车");
										item1_1.setType(4);
										Values.firstItems.add(item1_1);
										SecondItem item2_1 = new SecondItem();
										item2_1.setSteps(new ArrayList<String>());
										Values.secondItems.add(item2_1);
										// 2坐车
										FirstItem item1_2 = new FirstItem();
										item1_2.setTitle("乘坐" + buslinename + "车");
										item1_2.setType(3);
										if (busPath.getSteps().get(j).getBusLine()
												.getPassStations().size() != 0) {
											item1_2.setTitle2(busPath.getSteps()
													.get(j).getBusLine()
													.getPassStations().get(0)
													.getBusStationName()
													+ "->"
													+ busPath
															.getSteps()
															.get(j)
															.getBusLine()
															.getPassStations()
															.get(busPath
																	.getSteps()
																	.get(j)
																	.getBusLine()
																	.getPassStationNum() - 1)
															.getBusStationName()
													+ "方向  | "
													+ busPath.getSteps().get(j)
															.getBusLine()
															.getPassStationNum()
													+ "站");
										} else {
											item1_2.setTitle2("该公交路线已停运");
											Values.busline += "|不推荐此路线 公交停运";
										}

										Values.firstItems.add(item1_2);
										SecondItem item2_2 = new SecondItem();
										List<BusStationItem> passStations = busPath
												.getSteps().get(j).getBusLine()
												.getPassStations();
										ArrayList<String> heel1 = new ArrayList<String>();
										item2_2.setSteps(new ArrayList<String>());
										for (int q = 0; q < passStations.size(); q++) {
											if (passStations.get(q)
													.getBusStationName() == null) {
												break;
											}
											heel1.add(passStations.get(q)
													.getBusStationName());
										}
										item2_2.setSteps(heel1);
										Values.secondItems.add(item2_2);
										// 3终点
										FirstItem item1_3 = new FirstItem();
										item1_3.setTitle(busPath.getSteps().get(j)
												.getBusLine()
												.getArrivalBusStation()
												.getBusStationName()
												+ "站下车");
										item1_3.setType(4);
										Values.firstItems.add(item1_3);
										Values.secondItems.add(new SecondItem());

									}
								}
								FirstItem item1 = new FirstItem();
								item1.setTitle("目的地");
								Values.firstItems.add(item1);
								Values.secondItems.add(new SecondItem());
								mAMap.clear();// 清理之前的图标
								BusRouteOverlay routeOverlay = new BusRouteOverlay(
										this, mAMap, busPath,
										busRouteResult.getStartPos(),
										busRouteResult.getTargetPos());
								routeOverlay.removeFromMap();
								routeOverlay.addToMap();
								routeOverlay.zoomToSpan();
							} else {
								// 骑车去公交车站
								mode = "bus_bic";
								type = 1;
								poibicycle = 1;
								secondbus = busPath.getSteps().get(0).getBusLine()
										.getDepartureBusStation().getLatLonPoint();
								doSearchQuery(startPoint);
							}
						} else {
							// 要转车 尝试将次数减少
							if (busPath.getSteps().get(0).getWalk().getDistance()
									+ busPath.getSteps().get(1).getWalk()
											.getDistance()
									+ busPath.getSteps().get(0).getBusLine()
											.getDistance() <= 2500) {
								// 当起点到第二趟车的距离小于2500时 使用租赁车辆到达第二个乘车点
								secondbus = busPath.getSteps().get(1).getBusLine()
										.getDepartureBusStation().getLatLonPoint();
								poibicycle = 1;
								doSearchQuery(startPoint);
							}
						}
					}else {
						for (int j = 0; j < busPath.getSteps().size(); j++) {

							// 1
							FirstItem item = new FirstItem();
							item.setType(1);
							item.setTitle("步行"
									+ (int) busPath.getSteps().get(j)
											.getWalk().getDistance() + "米");
							Values.firstItems.add(item);

							List<WalkStep> heel = busPath.getSteps().get(j)
									.getWalk().getSteps();
							SecondItem item2 = new SecondItem();
							item2.setSteps(new ArrayList<String>());
							for (int k = 0; k < heel.size(); k++) {
								if (heel.get(k).getInstruction() == null) {
									break;
								}
								item2.getSteps().add(
										heel.get(k).getInstruction());
							}
							Values.secondItems.add(item2);

							// 2
							if (busPath.getSteps().get(j).getBusLine() != null) {
								// 获取公交车名
								String buslinename = "";
								for (int k = 0; k < busPath.getSteps()
										.get(j).getBusLine()
										.getBusLineName().length() - 1; k++) {
									if (!busPath.getSteps().get(j)
											.getBusLine().getBusLineName()
											.substring(k, k + 1)
											.equals("("))
										buslinename += busPath.getSteps()
												.get(j).getBusLine()
												.getBusLineName()
												.substring(k, k + 1);
									else
										break;
								}

								// 1起点
								FirstItem item1_1 = new FirstItem();
								item1_1.setTitle(busPath.getSteps().get(j)
										.getBusLine()
										.getDepartureBusStation()
										.getBusStationName()
										+ "站上车");
								item1_1.setType(4);
								Values.firstItems.add(item1_1);
								SecondItem item2_1 = new SecondItem();
								item2_1.setSteps(new ArrayList<String>());
								Values.secondItems.add(item2_1);
								// 2坐车
								FirstItem item1_2 = new FirstItem();
								item1_2.setTitle("乘坐" + buslinename + "车");
								item1_2.setType(3);
								if (busPath.getSteps().get(j).getBusLine()
										.getPassStations().size() != 0) {
									item1_2.setTitle2(busPath.getSteps()
											.get(j).getBusLine()
											.getPassStations().get(0)
											.getBusStationName()
											+ "->"
											+ busPath
													.getSteps()
													.get(j)
													.getBusLine()
													.getPassStations()
													.get(busPath
															.getSteps()
															.get(j)
															.getBusLine()
															.getPassStationNum() - 1)
													.getBusStationName()
											+ "方向  | "
											+ busPath.getSteps().get(j)
													.getBusLine()
													.getPassStationNum()
											+ "站");
								} else {
									item1_2.setTitle2("该公交路线已停运");
									Values.busline += "|不推荐此路线 公交停运";
								}

								Values.firstItems.add(item1_2);
								SecondItem item2_2 = new SecondItem();
								List<BusStationItem> passStations = busPath
										.getSteps().get(j).getBusLine()
										.getPassStations();
								ArrayList<String> heel1 = new ArrayList<String>();
								item2_2.setSteps(new ArrayList<String>());
								for (int q = 0; q < passStations.size(); q++) {
									if (passStations.get(q)
											.getBusStationName() == null) {
										break;
									}
									heel1.add(passStations.get(q)
											.getBusStationName());
								}
								item2_2.setSteps(heel1);
								Values.secondItems.add(item2_2);
								// 3终点
								FirstItem item1_3 = new FirstItem();
								item1_3.setTitle(busPath.getSteps().get(j)
										.getBusLine()
										.getArrivalBusStation()
										.getBusStationName()
										+ "站下车");
								item1_3.setType(4);
								Values.firstItems.add(item1_3);
								Values.secondItems.add(new SecondItem());

							}
						}
						FirstItem item1 = new FirstItem();
						item1.setTitle("目的地");
						Values.firstItems.add(item1);
						Values.secondItems.add(new SecondItem());

						BusRouteOverlay routeOverlay = new BusRouteOverlay(
								this, mAMap, busPath,
								busRouteResult.getStartPos(),
								busRouteResult.getTargetPos());
						routeOverlay.removeFromMap();
						routeOverlay.addToMap();
						routeOverlay.zoomToSpan();
					}
					return;
				}
			}
		} else {
			// 无公交，但是距离>2000 找自行车 自行车就500米范围内找 如果自行车早不到就找走过去
			Toast.makeText(this, "无公交，但是距离>2000 找自行车", Toast.LENGTH_SHORT)
					.show();
		}
	}

	protected void doSearchQuery(LatLonPoint latLonPoint) {
		query = new PoiSearch.Query("自行车租赁点", "", city);
		// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
		query.setPageSize(30);// 设置每页最多返回多少条poiitem
		query.setPageNum(0);// 设置查第一页
		poiSearch = new PoiSearch(this, query);
		poiSearch.setBound(new SearchBound(latLonPoint, 500));
		poiSearch.setOnPoiSearchListener(this);
		poiSearch.searchPOIAsyn();
	}

	@Override
	public void onDriveRouteSearched(DriveRouteResult arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int rCode) {
		// TODO Auto-generated method stub
		// 路径规划中步行模式
		DecimalFormat df = new DecimalFormat("###.0");
		if (rCode == 0 && walkRouteResult != null
				&& walkRouteResult.getPaths() != null
				&& walkRouteResult.getPaths().size() > 0) {
			WalkPath walkPath = walkRouteResult.getPaths().get(0);

			if (mode.equals("walk")) {

				if (type == 1) {
					if (walkPath.getDistance() <= 1000) {
						Values.path = "共"
								+ df.format(walkPath.getDistance() / 1000) + "公里"
								+ "|约" + walkPath.getDuration() / 3600 + "小时"
								+ walkPath.getDuration() % 3600 / 60 + "分钟";
						Values.busline = "步行路线";
						path.setText(Values.path);
						FirstItem item1 = new FirstItem();
						item1.setType(1);
						item1.setTitle("步行" + (int) walkPath.getDistance() + "米");
						Values.firstItems.add(item1);
						List<WalkStep> heel = walkPath.getSteps();
						SecondItem item2 = new SecondItem();
						item2.setSteps(new ArrayList<String>());
						for (int j = 0; j < heel.size(); j++) {
							if (heel.get(j).getInstruction() == null) {
								break;
							}
							item2.getSteps().add(heel.get(j).getInstruction());
						}
						Values.secondItems.add(item2);


						mAMap.clear();// 清理之前的图标
						WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
								this, mAMap, walkPath,
								walkRouteResult.getStartPos(),
								walkRouteResult.getTargetPos());
						walkRouteOverlay.removeFromMap();
						walkRouteOverlay.addToMap();
						walkRouteOverlay.zoomToSpan();
						return;
					} else {
						if (walkPath.getDistance() <= 2000) {
							// 找自行车
							Toast.makeText(this, "找自行车", Toast.LENGTH_SHORT)
									.show();
							mode = "bic";
							poibicycle = 1;
							doSearchQuery(startPoint);
						} else {
							// 坐公交
							mode = "notsure";
							RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(
									fromAndTo, busMode, city, 0);
							routeSearch.calculateBusRouteAsyn(query);
						}
					}
				}

				if (type == 2) {

					Values.path = "共"
							+ df.format(walkPath.getDistance() / 1000) + "公里"
							+ "|约" + walkPath.getDuration() / 3600 + "小时"
							+ walkPath.getDuration() % 3600 / 60 + "分钟";
					Values.busline = "步行路线";
					path.setText(Values.path);
					FirstItem item1 = new FirstItem();
					item1.setType(1);
					item1.setTitle("步行" + (int) walkPath.getDistance() + "米");
					Values.firstItems.add(item1);
					List<WalkStep> heel = walkPath.getSteps();
					SecondItem item2 = new SecondItem();
					item2.setSteps(new ArrayList<String>());
					for (int j = 0; j < heel.size(); j++) {
						if (heel.get(j).getInstruction() == null) {
							break;
						}
						item2.getSteps().add(heel.get(j).getInstruction());
					}
					Values.secondItems.add(item2);

					FirstItem item1_2 = new FirstItem();
					item1_2.setType(4);
					item1_2.setTitle("目的地");
					Values.firstItems.add(item1_2);
					Values.secondItems.add(new SecondItem());

					mAMap.clear();// 清理之前的图标
					WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
							this, mAMap, walkPath,
							walkRouteResult.getStartPos(),
							walkRouteResult.getTargetPos());
					walkRouteOverlay.removeFromMap();
					walkRouteOverlay.addToMap();
					walkRouteOverlay.zoomToSpan();
				}
			}

			if (mode.equals("bus_bic")) {
				if (type == 1) {
					if (progress == 1) {
						FirstItem item1 = new FirstItem();
						item1.setType(1);
						item1.setTitle("步行" + (int) walkPath.getDistance()
								+ "米");
						Values.firstItems.add(item1);
						List<WalkStep> heel = walkPath.getSteps();
						SecondItem item2 = new SecondItem();
						item2.setSteps(new ArrayList<String>());
						for (int j = 0; j < heel.size(); j++) {
							if (heel.get(j).getInstruction() == null) {
								break;
							}
							item2.getSteps().add(heel.get(j).getInstruction());
						}
						Values.secondItems.add(item2);
						WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
								this, mAMap, walkPath,
								walkRouteResult.getStartPos(),
								walkRouteResult.getTargetPos());
						walkRouteOverlay.removeFromMap();
						walkRouteOverlay.addToMap();
						walkRouteOverlay.zoomToSpan();

						walkdistance += walkPath.getDistance();
						progress = 2;
						FromAndTo fromAndTo1 = new FromAndTo(
								poiBikes.get(0).getPosition(), poiBikes.get(1)
										.getPosition());
						WalkRouteQuery query = new WalkRouteQuery(fromAndTo1,
								walkMode);
						routeSearch.calculateWalkRouteAsyn(query);
						return;
					}
					if (progress == 2) {
						FirstItem item1 = new FirstItem();
						item1.setType(2);
						item1.setTitle("骑行" + (int) walkPath.getDistance()
								+ "米");
						Values.firstItems.add(item1);
						List<WalkStep> heel = walkPath.getSteps();
						SecondItem item2 = new SecondItem();
						item2.setSteps(new ArrayList<String>());
						for (int j = 0; j < heel.size(); j++) {
							if (heel.get(j).getInstruction() == null) {
								break;
							}
							item2.getSteps().add(
									heel.get(j).getInstruction()
											.replace("步", "骑"));
						}
						WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
								this, mAMap, walkPath,
								walkRouteResult.getStartPos(),
								walkRouteResult.getTargetPos());
						walkRouteOverlay.removeFromMap();
						walkRouteOverlay.addToMap();
						walkRouteOverlay.zoomToSpan();
						Values.secondItems.add(item2);
						progress = 3;
						FromAndTo fromAndTo1 = new FromAndTo(poiBikes.get(1).getPosition(),endPoint);
						RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(
								fromAndTo1, busMode, city, 0);
						routeSearch.calculateBusRouteAsyn(query);
						return;
					}

				}
			}
			
			if (mode.equals("bic")) {
				if (progress == 1) {
					FirstItem item1 = new FirstItem();
					item1.setType(1);
					item1.setTitle("步行" + (int) walkPath.getDistance()
							+ "米");
					Values.firstItems.add(item1);
					List<WalkStep> heel = walkPath.getSteps();
					SecondItem item2 = new SecondItem();
					item2.setSteps(new ArrayList<String>());
					for (int j = 0; j < heel.size(); j++) {
						if (heel.get(j).getInstruction() == null) {
							break;
						}
						item2.getSteps().add(heel.get(j).getInstruction());
					}
					Values.secondItems.add(item2);
					WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
							this, mAMap, walkPath,
							walkRouteResult.getStartPos(),
							walkRouteResult.getTargetPos());
					walkRouteOverlay.removeFromMap();
					walkRouteOverlay.addToMap();
					walkRouteOverlay.zoomToSpan();

					walkdistance += walkPath.getDistance();
					progress = 2;
					FromAndTo fromAndTo1 = new FromAndTo(
							poiBikes.get(0).getPosition(), poiBikes.get(1)
									.getPosition());
					WalkRouteQuery query = new WalkRouteQuery(fromAndTo1,
							walkMode);
					routeSearch.calculateWalkRouteAsyn(query);
					return;
				}
				if (progress == 2) {
					FirstItem item1 = new FirstItem();
					item1.setType(2);
					item1.setTitle("骑行" + (int) walkPath.getDistance()
							+ "米");
					Values.firstItems.add(item1);
					List<WalkStep> heel = walkPath.getSteps();
					SecondItem item2 = new SecondItem();
					item2.setSteps(new ArrayList<String>());
					for (int j = 0; j < heel.size(); j++) {
						if (heel.get(j).getInstruction() == null) {
							break;
						}
						item2.getSteps().add(
								heel.get(j).getInstruction()
										.replace("步", "骑"));
					}
					WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
							this, mAMap, walkPath,
							walkRouteResult.getStartPos(),
							walkRouteResult.getTargetPos());
					walkRouteOverlay.removeFromMap();
					walkRouteOverlay.addToMap();
					walkRouteOverlay.zoomToSpan();
					Values.secondItems.add(item2);
					progress = 3;
					FromAndTo fromAndTo1 = new FromAndTo(poiBikes.get(1).getPosition(),endPoint);
					RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(
							fromAndTo1, busMode, city, 0);
					routeSearch.calculateBusRouteAsyn(query);
					return;
				}
				if (progress == 3) {
					FirstItem item1 = new FirstItem();
					item1.setType(1);
					item1.setTitle("步行" + (int) walkPath.getDistance()
							+ "米");
					Values.firstItems.add(item1);
					List<WalkStep> heel = walkPath.getSteps();
					SecondItem item2 = new SecondItem();
					item2.setSteps(new ArrayList<String>());
					for (int j = 0; j < heel.size(); j++) {
						if (heel.get(j).getInstruction() == null) {
							break;
						}
						item2.getSteps().add(heel.get(j).getInstruction());
					}
					Values.secondItems.add(item2);
					WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
							this, mAMap, walkPath,
							walkRouteResult.getStartPos(),
							walkRouteResult.getTargetPos());
					walkRouteOverlay.removeFromMap();
					walkRouteOverlay.addToMap();
					walkRouteOverlay.zoomToSpan();
					FirstItem item1_2 = new FirstItem();
					item1_2.setType(4);
					item1_2.setTitle("目的地");
					Values.firstItems.add(item1_2);
					Values.secondItems.add(new SecondItem());
					return;
				}
			}
		}
	}

}
