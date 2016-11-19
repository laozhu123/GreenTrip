package com.left.green;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
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
import com.gitonway.niftydialogeffects.widget.niftydialogeffects.Effectstype;
import com.gitonway.niftydialogeffects.widget.niftydialogeffects.NiftyDialogBuilder;
import com.left.adapter.BusAdapter;
import com.left.adapter.BusDetailAdapter;
import com.left.adapter.MixAdapter;
import com.left.adapter.WalkAdapter;
import com.left.adapter.WalkDetailAdapter;
import com.left.bean.FirstItem;
import com.left.bean.SecondItem;
import com.left.instruction.BusDetailInstruction;
import com.left.instruction.BusInstruction;
import com.left.instruction.MixInstruction;
import com.left.instruction.WalkDetailInstruction;
import com.left.instruction.WalkInstruction;
import com.left.tool.Values;
import com.left.util.BusStationList;
import com.left.util.PoiBike;
import com.left.util.WalkStepList;

@SuppressLint("InflateParams")
public class RouteActivity extends Activity implements OnRouteSearchListener,
		OnPoiSearchListener {

	private LatLonPoint startPoint;
	private LatLonPoint endPoint;
	private FromAndTo fromAndTo;
	private RouteSearch routeSearch;
	private MapView mMapView;
	private AMap mAMap;
	private int walkMode = RouteSearch.WalkDefault;// 步行默认模式
	private int busMode = RouteSearch.BusDefault;// 公交默认模式
	private ImageView back;
	private TextView title;
	private TextView path;
	private Button ar;
	private RelativeLayout detail;
	private LinearLayout taost;
	private View walkview;
	private ListView pathlist;
	private WalkAdapter adapter;
	private ArrayList<WalkInstruction> walkInstructions;
	private View busview;
	private ListView buspathlist;
	private BusAdapter busadapter;
	private ArrayList<BusInstruction> busInstructions;
	private View mixview;
	private ListView mixpathlist;
	private MixAdapter mixadapter;
	private ArrayList<MixInstruction> mixInstructions;
	private ArrayList<BusDetailInstruction> busdetailInstructions;
	private ArrayList<WalkDetailInstruction> walkDetailInstructions;
	private NiftyDialogBuilder dialogBuilder;
	private String mode;
	private String city;
	private String busname = "";
	private PoiResult poiResult; // poi返回的结果
	private PoiSearch.Query query;// Poi查询条件类
	private PoiSearch poiSearch;// POI搜索
	private List<String> busID;// 用来存放站点信息
	private List<BusStationList> busStationList;// 用来存放listview中的公交站点的信息
	private List<String> walkID;
	private List<WalkStepList> walkStepLists;
	private List<PoiBike> poiBikes;
	private boolean isfirst = true;// 用以记录是否首次规划步行路径，以排除mix模式下的再次规划
	private boolean isfirstbike = true;// 用以记录是否首次搜索自行车租赁点，以排除mix模式下的再次搜索
	private float mixdistance;// 用以记录mix模式下总里程数
	private float mixwalkdistance;
	private float mixbicycledistance;
	private long mixtime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_route);
		mMapView = (MapView) findViewById(R.id.map);
		back = (ImageView) findViewById(R.id.back);
		title = (TextView) findViewById(R.id.title);
		detail = (RelativeLayout) findViewById(R.id.detail);
		taost = (LinearLayout) findViewById(R.id.tosat);
		path = (TextView) findViewById(R.id.path);
		ar = (Button) findViewById(R.id.arnavi);
		busID = new ArrayList<String>();
		busStationList = new ArrayList<BusStationList>(); // 每个中存放公交二级内容BusStationList中有一个array
		walkID = new ArrayList<String>();
		walkStepLists = new ArrayList<WalkStepList>(); // 每个中存放步行二级内容WalkStepList中有一个array
		poiBikes = new ArrayList<PoiBike>(); // 所有自行车租赁点
		mMapView.onCreate(savedInstanceState);
		walkview = LayoutInflater.from(this).inflate(R.layout.walk_listview,
				null);
		pathlist = (ListView) walkview.findViewById(R.id.walklistview);
		busview = LayoutInflater.from(this)
				.inflate(R.layout.bus_listview, null);
		buspathlist = (ListView) busview.findViewById(R.id.buslistview);
		mixview = LayoutInflater.from(this)
				.inflate(R.layout.mix_listview, null);
		mixpathlist = (ListView) mixview.findViewById(R.id.mixlistview);
		dialogBuilder = new NiftyDialogBuilder(this, R.style.dialog_untran);

		// 3种方式其实是4中 walk和bicycle共用了
		walkInstructions = new ArrayList<WalkInstruction>();
		adapter = new WalkAdapter(this, walkInstructions);
		pathlist.setAdapter(adapter);
		busInstructions = new ArrayList<BusInstruction>();
		busadapter = new BusAdapter(this, busInstructions);
		buspathlist.setAdapter(busadapter);
		mixInstructions = new ArrayList<MixInstruction>();
		mixadapter = new MixAdapter(this, mixInstructions);
		mixpathlist.setAdapter(mixadapter);

		// 二级内容列表
		busdetailInstructions = new ArrayList<BusDetailInstruction>();
		walkDetailInstructions = new ArrayList<WalkDetailInstruction>();

		Values.firstItems = new ArrayList<FirstItem>();
		Values.secondItems = new ArrayList<SecondItem>();
		FirstItem item = new FirstItem();
		item.setTitle("我的位置");
		item.setType(4);
		Values.firstItems.add(item);
		SecondItem item2 = new SecondItem();
		item2.setSteps(new ArrayList<String>());
		Values.secondItems.add(item2);

		mAMap = mMapView.getMap();
		Intent intent = getIntent();
		/* 取出Intent中附加的数据 */
		mode = intent.getStringExtra("mode");
		city = intent.getStringExtra("city");
		double[] pathpoint = intent.getDoubleArrayExtra("pathpoint");
		startPoint = new LatLonPoint(pathpoint[0], pathpoint[1]);
		endPoint = new LatLonPoint(pathpoint[2], pathpoint[3]);
		fromAndTo = new FromAndTo(startPoint, endPoint);
		routeSearch = new RouteSearch(this);
		routeSearch.setRouteSearchListener(this);

		if (mode.equals("bus")) {
			title.setText("公交路线");
			Values.path = "公交路线";
			// 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式
			// 第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
			RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(
					fromAndTo, busMode, city, 0);
			routeSearch.calculateBusRouteAsyn(query);
		} else if (mode.equals("mix")) {
			title.setText("混合换乘路线");
			Values.path = "混合路线";
			// 先搜索起点周围的自行车租赁点
			doSearchQuery(startPoint);
		} else if (mode.equals("walk")) {
			title.setText("步行路线");
			Values.path = "步行路线";
			WalkRouteQuery query = new WalkRouteQuery(fromAndTo, walkMode);
			routeSearch.calculateWalkRouteAsyn(query);
		} else if (mode.equals("bicycle")) {
			title.setText("骑行路线");
			Values.path = "骑行路线";
			WalkRouteQuery query = new WalkRouteQuery(fromAndTo, walkMode);
			routeSearch.calculateWalkRouteAsyn(query);
		}

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		ar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(RouteActivity.this,
						ARNaviActivity.class);
				startActivity(intent);
			}
		});

		// 是个逗比吗。。两个都一样
		detail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				FirstItem item1 = new FirstItem();
				item1.setTitle("目的地");
				Values.firstItems.add(item1);
				Values.secondItems.add(new SecondItem());
				startActivity(new Intent(RouteActivity.this,
						RouteDetailActivity.class));
			}
		});
		taost.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				FirstItem item1 = new FirstItem();
				item1.setTitle("目的地");
				Values.firstItems.add(item1);
				Values.secondItems.add(new SecondItem());
				startActivity(new Intent(RouteActivity.this,
						RouteDetailActivity.class));
			}
		});

		mixpathlist.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				for (int i = 0; i < busID.size();) {
					if ((arg2 + "").equals(busID.get(i))
							|| ((arg2 + 1) + "").equals(busID.get(i))) {
						// 开始搜索公交站点相关数据
						for (int j = 0; j < busStationList.size(); j++) {
							// 找到对应的站点，获取详情
							if ((busStationList.get(j).getBusLineId() + "")
									.equals(busID.get(i + 1))) {
								busdetailInstructions.clear();
								for (int k = 0; k < busStationList.get(j)
										.getBusStationItems().size(); k++) {
									BusDetailInstruction instruction = new BusDetailInstruction();
									instruction.setName(busStationList.get(j)
											.getBusStationItems().get(k)
											.getBusStationName());
									busdetailInstructions.add(instruction);
								}
								ListView busdetaillist = new ListView(
										RouteActivity.this);
								BusDetailAdapter busdetailadapter = new BusDetailAdapter(
										RouteActivity.this,
										busdetailInstructions);
								busdetaillist.setAdapter(busdetailadapter);
								NiftyDialogBuilder busBuilder = new NiftyDialogBuilder(
										RouteActivity.this,
										R.style.dialog_untran);
								busBuilder
										.withTitle(
												busStationList.get(j)
														.getBusLineName())
										.setCustomView(busdetaillist,
												RouteActivity.this)
										.isCancelableOnTouchOutside(true)
										.withDuration(700)
										.withEffect(Effectstype.RotateBottom)
										.show();
							}
						}
					}
					i += 2;
				}
				for (int i = 0; i < walkID.size();) {
					if ((arg2 + "").equals(walkID.get(i))) {
						for (int j = 0; j < walkStepLists.size(); j++) {
							// 找到对应的站点，获取详情
							if ((walkStepLists.get(j).getWalkId() + "")
									.equals(walkID.get(i + 1))) {
								walkDetailInstructions.clear();
								for (int k = 0; k < walkStepLists.get(j)
										.getWalkstep().size(); k++) {
									WalkDetailInstruction instruction = new WalkDetailInstruction();
									if (!walkID.get(i + 1).contains("骑行")
											&& !walkID.get(i + 1).contains(
													"bus")) {
										if (k == walkStepLists.get(j)
												.getWalkstep().size() - 1) {
											String last = walkStepLists
													.get(j)
													.getWalkstep()
													.get(k)
													.getInstruction()
													.substring(
															0,
															walkStepLists
																	.get(j)
																	.getWalkstep()
																	.get(k)
																	.getInstruction()
																	.length() - 3)
													+ poiBikes.get(0)
															.getBikeRental();
											instruction.setName(last);
										} else
											instruction.setName(walkStepLists
													.get(j).getWalkstep()
													.get(k).getInstruction());
										walkDetailInstructions.add(instruction);
									} else if (walkID.get(i + 1)
											.contains("bus")) {
										instruction.setName(walkStepLists
												.get(j).getWalkstep().get(k)
												.getInstruction());
										walkDetailInstructions.add(instruction);
									} else {
										String[] bicyclepath = new String[walkStepLists
												.get(j).getWalkstep().get(k)
												.getInstruction().length()];
										for (int l = 0; l < walkStepLists
												.get(j).getWalkstep().get(k)
												.getInstruction().length(); l++) {
											bicyclepath[l] = walkStepLists
													.get(j).getWalkstep()
													.get(k).getInstruction()
													.substring(l, l + 1);
											// 将步行数据修改为骑行数据（伪的）
											if (bicyclepath[l].equals("步"))
												bicyclepath[l] = "骑";
										}
										String bicycleinstruction = "";
										for (int s = 0; s < bicyclepath.length; s++)
											bicycleinstruction += bicyclepath[s];
										// 将步行路径最后一次的引导词最后三个字“目的地”修改为第二个自行车租赁点的名字
										if (k == walkStepLists.get(j)
												.getWalkstep().size() - 1) {
											String last = bicycleinstruction
													.substring(
															0,
															bicycleinstruction
																	.length() - 3)
													+ poiBikes.get(1)
															.getBikeRental();
											instruction.setName(last);
										} else
											instruction
													.setName(bicycleinstruction);
										walkDetailInstructions.add(instruction);
									}
								}
								ListView walkdetaillist = new ListView(
										RouteActivity.this);
								WalkDetailAdapter walkdetailadapter = new WalkDetailAdapter(
										RouteActivity.this,
										walkDetailInstructions);
								walkdetaillist.setAdapter(walkdetailadapter);
								NiftyDialogBuilder busBuilder = new NiftyDialogBuilder(
										RouteActivity.this,
										R.style.dialog_untran);
								busBuilder
										.withTitle("详情")
										.setCustomView(walkdetaillist,
												RouteActivity.this)
										.isCancelableOnTouchOutside(true)
										.withDuration(700)
										.withEffect(Effectstype.Slidetop)
										.show();
							}
						}
					}
					i += 2;
				}
			}
		});
	}

	@Override
	public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int rCode) {
		// 路径规划中步行模式
		DecimalFormat df = new DecimalFormat("###.0");
		if (rCode == 0 && walkRouteResult != null
				&& walkRouteResult.getPaths() != null
				&& walkRouteResult.getPaths().size() > 0) {
			WalkPath walkPath = walkRouteResult.getPaths().get(0);

			if (mode.equals("walk")) {
				/**
 * 
 */

				Values.path = "共" + df.format(walkPath.getDistance() / 1000)
						+ "公里" + "|约" + walkPath.getDuration() / 3600 + "小时"
						+ walkPath.getDuration() % 3600 / 60 + "分钟";
			} else if (mode.equals("bicycle")) {
				/**
 * 
 */

				Values.path = "共" + df.format(walkPath.getDistance() / 1000)
						+ "公里" + "|约" + walkPath.getDuration() / 7200 + "小时"
						+ (walkPath.getDuration() / 2) % 3600 / 60 + "分钟";
			}
			// mix模式下的第一次路径规划
			else if (isfirst && mode.equals("mix")) {
				/**
 * 
 */
				Values.busline = "混合路线";

				mixInstructions.clear();
				walkID.clear();
				walkStepLists.clear();
				mixdistance += walkPath.getDistance() / 1000;
				mixwalkdistance += walkPath.getDistance() / 1000;
				mixtime += walkPath.getDuration();
				MixInstruction instruction = new MixInstruction();
				instruction.setImgId(R.drawable.walk);
				instruction.setName("步行" + (int) walkPath.getDistance() + "米");
				instruction.setColor(Color.parseColor("#77B943"));
				mixInstructions.add(instruction);
				WalkStepList stationList = new WalkStepList();
				stationList.setWalkId(walkPath.toString());
				stationList.setWalkstep(walkPath.getSteps());
				// 将数据填充进去，方便后续点击时直接获取
				walkStepLists.add(stationList);
				walkID.add(mixInstructions.size() - 1 + "");
				walkID.add(walkPath.toString());

				/**
 * 
 */
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
				/**
 * 
 */

				mAMap.clear();// 清理之前的图标
				WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(this,
						mAMap, walkPath, walkRouteResult.getStartPos(),
						walkRouteResult.getTargetPos());
				walkRouteOverlay.removeFromMap();
				walkRouteOverlay.setNodeIconVisibility(false);
				walkRouteOverlay.addToMap();
				walkRouteOverlay.zoomToSpan();
				// 再接着规划两自行车租赁点之间线路
				startPoint = poiBikes.get(0).getPosition();
				LatLonPoint endPoint = poiBikes.get(1).getPosition();
				FromAndTo fromAndTo = new FromAndTo(startPoint, endPoint);
				isfirst = false;
				WalkRouteQuery query = new WalkRouteQuery(fromAndTo, walkMode);
				routeSearch.calculateWalkRouteAsyn(query);
			}
			// mix模式下的第二次路径规划
			else if (!isfirst && mode.equals("mix")) {
				mixdistance += walkPath.getDistance() / 1000;
				mixbicycledistance += walkPath.getDistance() / 1000;
				mixtime += walkPath.getDuration() / 2;
				MixInstruction instruction = new MixInstruction();
				instruction.setImgId(R.drawable.walk);
				instruction.setName("骑行" + (int) walkPath.getDistance() + "米");
				instruction.setColor(Color.parseColor("#0000ff"));
				mixInstructions.add(instruction);
				WalkStepList stationList = new WalkStepList();
				stationList.setWalkId(walkPath.toString() + "骑行");
				stationList.setWalkstep(walkPath.getSteps());
				// 将数据填充进去，方便后续点击时直接获取
				walkStepLists.add(stationList);
				walkID.add(mixInstructions.size() - 1 + "");
				walkID.add(walkPath.toString() + "骑行");
				WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(this,
						mAMap, walkPath, walkRouteResult.getStartPos(),
						walkRouteResult.getTargetPos());
				walkRouteOverlay.removeFromMap();
				walkRouteOverlay.setNodeIconVisibility(false);
				walkRouteOverlay.addToMap();
				walkRouteOverlay.zoomToSpan();
				// 激发公交路径搜索，最后一步，路径即全部规划完成
				startPoint = new LatLonPoint(poiBikes.get(1).getPosition()
						.getLatitude(), poiBikes.get(1).getPosition()
						.getLongitude());
				fromAndTo = new FromAndTo(startPoint, endPoint);
				RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(
						fromAndTo, busMode, city, 0);
				routeSearch.calculateBusRouteAsyn(query);

				/**
 * 
 */

				FirstItem item1_3 = new FirstItem();
				item1_3.setType(4);
				item1_3.setTitle("自行车租赁点（借车）");
				Values.firstItems.add(item1_3);
				Values.secondItems.add(new SecondItem());

				FirstItem item1 = new FirstItem();
				item1.setType(2);
				item1.setTitle("骑行" + (int) walkPath.getDistance() + "米");
				Values.firstItems.add(item1);
				List<WalkStep> heel = walkPath.getSteps();
				SecondItem item2 = new SecondItem();
				item2.setSteps(new ArrayList<String>());
				for (int j = 0; j < heel.size(); j++) {
					if (heel.get(j).getInstruction() == null) {
						break;
					}
					item2.getSteps().add(
							heel.get(j).getInstruction().replace("步", "骑"));
				}
				Values.secondItems.add(item2);

				FirstItem item1_2 = new FirstItem();
				item1_2.setType(4);
				item1_2.setTitle("自行车租赁点（还车）");
				Values.firstItems.add(item1_2);
				Values.secondItems.add(new SecondItem());
			}
			if (mode.equals("walk") || mode.equals("bicycle")) {
				walkInstructions.clear();
				for (int i = 0; i < walkPath.getSteps().size(); i++) {
					WalkInstruction instruction = new WalkInstruction();
					if (mode.equals("bicycle")) {
						String[] bicyclepath = new String[walkPath.getSteps()
								.get(i).getInstruction().length()];
						for (int j = 0; j < walkPath.getSteps().get(i)
								.getInstruction().length(); j++) {
							bicyclepath[j] = walkPath.getSteps().get(i)
									.getInstruction().substring(j, j + 1);
							// 将步行数据修改为骑行数据（伪的）
							if (bicyclepath[j].equals("步"))
								bicyclepath[j] = "骑";
						}
						String bicycleinstruction = "";
						for (int k = 0; k < bicyclepath.length; k++)
							bicycleinstruction += bicyclepath[k];
						instruction.setName(bicycleinstruction);

						/**
 * 
 */

					} else {
						/**
 * 
 */

						instruction.setName(walkPath.getSteps().get(i)
								.getInstruction());
					}
					walkInstructions.add(instruction);

				}
				if (mode.equals("walk")) {
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

				}
				if (mode.equals("bicycle")) {
					FirstItem item1 = new FirstItem();
					item1.setType(2);
					item1.setTitle("骑行" + (int) walkPath.getDistance() + "米");
					Values.firstItems.add(item1);
					List<WalkStep> heel = walkPath.getSteps();
					SecondItem item2 = new SecondItem();
					item2.setSteps(new ArrayList<String>());
					for (int j = 0; j < heel.size(); j++) {
						if (heel.get(j).getInstruction() == null) {
							break;
						}
						item2.getSteps().add(
								heel.get(j).getInstruction().replace("步", "骑"));
					}
					Values.secondItems.add(item2);

				}

				adapter.notifyDataSetChanged();
				mAMap.clear();// 清理之前的图标
				WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(this,
						mAMap, walkPath, walkRouteResult.getStartPos(),
						walkRouteResult.getTargetPos());
				walkRouteOverlay.removeFromMap();
				if (mode.equals("bicycle"))
					walkRouteOverlay.setNodeIconVisibility(false);
				walkRouteOverlay.addToMap();
				walkRouteOverlay.zoomToSpan();

			}
		}
	}

	@Override
	public void onBusRouteSearched(BusRouteResult busRouteResult, int rCode) {
		// 路径规划中公交模式
		DecimalFormat df = new DecimalFormat("###.0");
		if (rCode == 0 && busRouteResult != null
				&& busRouteResult.getPaths() != null
				&& busRouteResult.getPaths().size() > 0) {
			busID.clear();
			busStationList.clear();
			if (!mode.equals("mix")) {
				walkID.clear();
				walkStepLists.clear();
				/**
 * 
 */

			}
			Intent intent = getIntent();
			/* 取出Intent中附加的数据 */
			int pathnum;
			BusPath busPath;

			/**
* 
* */
			pathnum = getIntent().getIntExtra("buspathnum", 0);
			busPath = busRouteResult.getPaths().get(pathnum);
			// 取出对应方案中所有路段的公交线路名
			for (int j = 0; j < busPath.getSteps().size(); j++) {
				if (busPath.getSteps().get(j).getBusLine() != null) {
					if (j > 0)
						busname += "->";
					for (int k = 0; k < busPath.getSteps().get(j).getBusLine()
							.getBusLineName().length() - 1; k++) {
						if (!busPath.getSteps().get(j).getBusLine()
								.getBusLineName().substring(k, k + 1)
								.equals("("))
							busname += busPath.getSteps().get(j).getBusLine()
									.getBusLineName().substring(k, k + 1);
						else
							break;
					}
					/**
	 * 
	 */
					if (mode.equals("bus")) {
						Values.busline = busname;
					}
				}
			}
			if (mode.equals("bus")) {
				path.setText("共" + df.format(busPath.getDistance() / 1000)
						+ "公里" + "|约" + busPath.getDuration() / 3600 + "小时"
						+ busPath.getDuration() % 3600 / 60 + "分钟|步行"
						+ (int) busPath.getWalkDistance() + "米");
				busInstructions.clear();
				/**
	 * 
	 */
				Values.path = path.getText().toString();
			}

			// 后面代码不要了
			busPath = busRouteResult.getPaths().get(0);
			// mix模式下的第三次路径规划
			if (mode.equals("mix")) {
				mixdistance += busPath.getDistance() / 1000;
				mixwalkdistance += busPath.getWalkDistance() / 1000;
				mixtime += busPath.getDuration();
				if (poiBikes.size() > 0)
					path.setText("公共自行车&" + busname + "|"
							+ df.format(mixdistance) + "公里" + "|步行"
							+ df.format(mixwalkdistance) + "公里|骑行"
							+ df.format(mixbicycledistance) + "公里|约" + mixtime
							/ 3600 + "小时" + mixtime % 3600 / 60 + "分钟");
				else
					path.setText("共" + df.format(busPath.getDistance() / 1000)
							+ "公里" + "|约" + busPath.getDuration() / 3600 + "小时"
							+ busPath.getDuration() % 3600 / 60 + "分钟|步行"
							+ (int) busPath.getWalkDistance() + "米");
				/**
 * 
 */
				Values.path = path.getText().toString();
			}

			// 公交线路规划算法(写了一下午....)
			for (int i = 0; i < busPath.getSteps().size(); i++) {
				// 步行数据
				if (mode.equals("bus")) {
					BusInstruction instruction = new BusInstruction();
					instruction.setName("步行"
							+ (int) busPath.getSteps().get(i).getWalk()
									.getDistance() + "米");
					instruction.setImgId(R.drawable.walk);
					busInstructions.add(instruction);
					/**
 * 
 */
					FirstItem item = new FirstItem();
					item.setType(1);
					item.setTitle("步行"
							+ (int) busPath.getSteps().get(i).getWalk()
									.getDistance() + "米");
					Values.firstItems.add(item);

					WalkStepList stationList = new WalkStepList();
					stationList.setWalkId(busPath.getSteps().get(i).getWalk()
							.toString());
					// 走的list
					stationList.setWalkstep(busPath.getSteps().get(i).getWalk()
							.getSteps());

					// 将数据填充进去，方便后续点击时直接获取
					walkStepLists.add(stationList);
					walkID.add(busInstructions.size() - 1 + "");
					walkID.add(busPath.getSteps().get(i).getWalk().toString());
					/**
 * 
 */
					List<WalkStep> heel = busPath.getSteps().get(i).getWalk()
							.getSteps();
					SecondItem item2 = new SecondItem();
					item2.setSteps(new ArrayList<String>());
					for (int j = 0; j < heel.size(); j++) {
						if (heel.get(j).getInstruction() == null) {
							break;
						}
						item2.getSteps().add(heel.get(j).getInstruction());
					}
					Values.secondItems.add(item2);

				} else if (mode.equals("mix")) {
					MixInstruction instruction = new MixInstruction();
					instruction.setName("步行"
							+ (int) busPath.getSteps().get(i).getWalk()
									.getDistance() + "米");
					instruction.setImgId(R.drawable.walk);
					instruction.setColor(Color.parseColor("#77B943"));
					mixInstructions.add(instruction);

					/**
 * 
 */
					FirstItem item = new FirstItem();
					item.setType(1);
					item.setTitle("步行"
							+ (int) busPath.getSteps().get(i).getWalk()
									.getDistance() + "米");
					Values.firstItems.add(item);

					WalkStepList stationList = new WalkStepList();
					stationList.setWalkId(busPath.getSteps().get(i).getWalk()
							.toString()
							+ "bus");
					stationList.setWalkstep(busPath.getSteps().get(i).getWalk()
							.getSteps());
					// 将数据填充进去，方便后续点击时直接获取
					walkStepLists.add(stationList);
					walkID.add(mixInstructions.size() - 1 + "");
					walkID.add(busPath.getSteps().get(i).getWalk().toString()
							+ "bus");
					/**
 * 
 */
					List<WalkStep> heel = busPath.getSteps().get(i).getWalk()
							.getSteps();
					SecondItem item2 = new SecondItem();
					item2.setSteps(new ArrayList<String>());
					for (int j = 0; j < heel.size(); j++) {
						if (heel.get(j).getInstruction() == null) {
							break;
						}
						item2.getSteps().add(heel.get(j).getInstruction());
					}
					Values.secondItems.add(item2);

				}
				// 公交数据
				if (busPath.getSteps().get(i).getBusLine() != null) {
					BusStationList stationList = new BusStationList();
					stationList.setBusLineId(busPath.getSteps().get(i)
							.getBusLine().getBusLineId());
					stationList.setBusLineName(busPath.getSteps().get(i)
							.getBusLine().getBusLineName());
					stationList.setFirstBusTime(busPath.getSteps().get(i)
							.getBusLine().getFirstBusTime());
					stationList.setLastBusTime(busPath.getSteps().get(i)
							.getBusLine().getLastBusTime());

					// import 获取经的站
					stationList.setBusStationItems(busPath.getSteps().get(i)
							.getBusLine().getPassStations());

					stationList.setPassStationNum(busPath.getSteps().get(i)
							.getBusLine().getPassStationNum());
					// 将数据填充进去，方便后续点击时直接获取
					busStationList.add(stationList);
					if (mode.equals("bus")) {
						BusInstruction start = new BusInstruction();
						// 取出对应方案中路段的公交线路名
						String buslinename = "";
						for (int k = 0; k < busPath.getSteps().get(i)
								.getBusLine().getBusLineName().length() - 1; k++) {
							if (!busPath.getSteps().get(i).getBusLine()
									.getBusLineName().substring(k, k + 1)
									.equals("("))
								buslinename += busPath.getSteps().get(i)
										.getBusLine().getBusLineName()
										.substring(k, k + 1);
							else
								break;
						}
						start.setName(busPath.getSteps().get(i).getBusLine()
								.getDepartureBusStation().getBusStationName()
								+ "站上车" + "(乘坐" + buslinename + ")");
						start.setColor(Color.parseColor("#FF6100"));
						start.setImgId(R.drawable.car);
						busInstructions.add(start);
						BusInstruction arrive = new BusInstruction();
						arrive.setName(busPath.getSteps().get(i).getBusLine()
								.getArrivalBusStation().getBusStationName()
								+ "站下车");
						arrive.setImgId(R.drawable.car);
						busInstructions.add(arrive);
						busID.add(busInstructions.size() - 1 + "");
						busID.add(busPath.getSteps().get(i).getBusLine()
								.getBusLineId());

						/**
 * 		
 */
						// 1起点
						FirstItem item1_1 = new FirstItem();
						item1_1.setTitle(busPath.getSteps().get(i).getBusLine()
								.getDepartureBusStation().getBusStationName()
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
						if (busPath.getSteps().get(i).getBusLine()
								.getPassStations().size() != 0) {
							item1_2.setTitle2(busPath.getSteps().get(i)
									.getBusLine().getPassStations().get(0)
									.getBusStationName()
									+ "->"
									+ busPath
											.getSteps()
											.get(i)
											.getBusLine()
											.getPassStations()
											.get(busPath.getSteps().get(i)
													.getBusLine()
													.getPassStationNum() - 1)
											.getBusStationName()
									+ "方向  | "
									+ busPath.getSteps().get(i).getBusLine()
											.getPassStationNum() + "站");
						} else {
							item1_2.setTitle2("该公交路线已停运");
							Values.busline += "|不推荐此路线 公交停运";
						}

						Values.firstItems.add(item1_2);
						SecondItem item2_2 = new SecondItem();
						List<BusStationItem> passStations = busPath.getSteps()
								.get(i).getBusLine().getPassStations();
						ArrayList<String> heel = new ArrayList<String>();
						item2_2.setSteps(new ArrayList<String>());
						for (int j = 0; j < passStations.size(); j++) {
							if (passStations.get(j).getBusStationName() == null) {
								break;
							}
							heel.add(passStations.get(j).getBusStationName());
						}
						item2_2.setSteps(heel);
						Values.secondItems.add(item2_2);
						// 3终点
						FirstItem item1_3 = new FirstItem();
						item1_3.setTitle(busPath.getSteps().get(i).getBusLine()
								.getArrivalBusStation().getBusStationName()
								+ "站下车");
						item1_3.setType(4);
						Values.firstItems.add(item1_3);
						Values.secondItems.add(new SecondItem());

					} else if (mode.equals("mix")) {
						MixInstruction start = new MixInstruction();
						// 取出对应方案中路段的公交线路名
						String buslinename = "";
						for (int k = 0; k < busPath.getSteps().get(i)
								.getBusLine().getBusLineName().length() - 1; k++) {
							if (!busPath.getSteps().get(i).getBusLine()
									.getBusLineName().substring(k, k + 1)
									.equals("("))
								buslinename += busPath.getSteps().get(i)
										.getBusLine().getBusLineName()
										.substring(k, k + 1);
							else
								break;
						}
						start.setName(busPath.getSteps().get(i).getBusLine()
								.getDepartureBusStation().getBusStationName()
								+ "站上车" + "(乘坐" + buslinename + ")");
						start.setColor(Color.parseColor("#FF6100"));
						start.setImgId(R.drawable.car);
						mixInstructions.add(start);
						MixInstruction arrive = new MixInstruction();
						arrive.setName(busPath.getSteps().get(i).getBusLine()
								.getArrivalBusStation().getBusStationName()
								+ "站下车");
						arrive.setImgId(R.drawable.car);
						mixInstructions.add(arrive);
						busID.add(mixInstructions.size() - 1 + "");
						busID.add(busPath.getSteps().get(i).getBusLine()
								.getBusLineId());
						/**
 * 	
 */
						// 1起点
						FirstItem item1_1 = new FirstItem();
						item1_1.setTitle(busPath.getSteps().get(i).getBusLine()
								.getDepartureBusStation().getBusStationName()
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
						if (busPath.getSteps().get(i).getBusLine()
								.getPassStations().size() > 0) {
							item1_2.setTitle2(busPath.getSteps().get(i)
									.getBusLine().getPassStations().get(0)
									.getBusStationName()
									+ "->"
									+ busPath
											.getSteps()
											.get(i)
											.getBusLine()
											.getPassStations()
											.get(busPath.getSteps().get(i)
													.getBusLine()
													.getPassStationNum() - 1)
											.getBusStationName()
									+ "方向  | "
									+ busPath.getSteps().get(i).getBusLine()
											.getPassStationNum() + "站");
						} else {
							item1_2.setTitle2("该公交路线已停运");
							Values.busline += "|不推荐此路线 公交停运";
						}

						Values.firstItems.add(item1_2);
						SecondItem item2_2 = new SecondItem();
						item2_2.setSteps(new ArrayList<String>());
						List<BusStationItem> passStations = busPath.getSteps()
								.get(i).getBusLine().getPassStations();
						ArrayList<String> heel = new ArrayList<String>();
						for (int j = 0; j < passStations.size(); j++) {
							if (passStations.get(j).getBusStationName() == null) {
								break;
							}
							heel.add(passStations.get(j).getBusStationName());
						}
						item2_2.setSteps(heel);
						Values.secondItems.add(item2_2);
						// 3终点
						FirstItem item1_3 = new FirstItem();
						item1_3.setTitle(busPath.getSteps().get(i).getBusLine()
								.getArrivalBusStation().getBusStationName()
								+ "站下车");
						item1_3.setType(4);
						Values.firstItems.add(item1_3);
						Values.secondItems.add(new SecondItem());
					}
				}
			}

			if (mode.equals("bus")) {
				busadapter.notifyDataSetChanged();
				mAMap.clear();// 清理之前的图标
				BusRouteOverlay routeOverlay = new BusRouteOverlay(this, mAMap,
						busPath, busRouteResult.getStartPos(),
						busRouteResult.getTargetPos());
				routeOverlay.removeFromMap();
				routeOverlay.addToMap();
				routeOverlay.zoomToSpan();
			} else if (mode.equals("mix")) {
				mixadapter.notifyDataSetChanged();
				BusRouteOverlay routeOverlay = new BusRouteOverlay(this, mAMap,
						busPath, busRouteResult.getStartPos(),
						busRouteResult.getTargetPos());
				routeOverlay.removeFromMap();
				routeOverlay.setNodeIconVisibility(false);
				routeOverlay.addToMap();
				routeOverlay.zoomToSpan();
			}
		}
	}

	/**
	 * 开始进行poi搜索,找公共自行车租赁点
	 */
	protected void doSearchQuery(LatLonPoint latLonPoint) {
		query = new PoiSearch.Query("自行车租赁点", "", city);
		// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
		query.setPageSize(30);// 设置每页最多返回多少条poiitem
		query.setPageNum(0);// 设置查第一页
		poiSearch = new PoiSearch(this, query);
		poiSearch.setBound(new SearchBound(latLonPoint, 2000));
		poiSearch.setOnPoiSearchListener(this);
		poiSearch.searchPOIAsyn();
	}

	@Override
	public void onPoiSearched(PoiResult result, int rCode) {
		if (rCode == 0 && result != null && result.getQuery() != null
				&& result.getQuery().equals(query)) {// 搜索poi的结果
			poiResult = result;
			// 取得搜索到的poiitems有多少页
			List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
			// 需要找到两个自行车租赁点,且取头和中间的两个自行车站点
			if (isfirstbike && poiItems != null && poiItems.size() > 0) {
				PoiBike bike = new PoiBike();
				bike.setBikeRental(poiItems.get(0).toString());
				bike.setPosition(poiItems.get(0).getLatLonPoint());
				poiBikes.add(bike);
				isfirstbike = false;
				// 找第二个自行车租赁点，以起止点中点为搜索中心
				doSearchQuery(new LatLonPoint(
						(startPoint.getLatitude() + endPoint.getLatitude()) / 2,
						(startPoint.getLongitude() + endPoint.getLongitude()) / 2));
			} else if (!isfirstbike && poiItems != null && poiItems.size() > 0) {
				PoiBike bike = new PoiBike();
				bike.setBikeRental(poiItems.get(0).toString());
				bike.setPosition(poiItems.get(0).getLatLonPoint());
				poiBikes.add(bike);
				// 先规划步行路径
				LatLonPoint endPoint = poiBikes.get(0).getPosition();
				fromAndTo = new FromAndTo(startPoint, endPoint);
				WalkRouteQuery query = new WalkRouteQuery(fromAndTo, walkMode);
				routeSearch.calculateWalkRouteAsyn(query);
			} else if ((isfirst && poiBikes.size() == 0)
					|| (!isfirstbike && poiBikes.size() < 2)) {
				poiBikes.clear();
				// 没有自行车数据的话直接规划公交线路
				RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(
						fromAndTo, busMode, city, 0);
				routeSearch.calculateBusRouteAsyn(query);
			} else {
				poiBikes.clear();
				// 没有自行车数据的话直接规划公交线路
				RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(
						fromAndTo, busMode, city, 0);
				routeSearch.calculateBusRouteAsyn(query);
			}
		} else {
			Toast.makeText(RouteActivity.this, "请确认是否联网", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * 此方法需存在
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	/**
	 * 此方法需存在
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	/**
	 * 此方法需存在
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	public void onPoiItemDetailSearched(PoiItemDetail arg0, int arg1) {

	}

	@Override
	public void onDriveRouteSearched(DriveRouteResult driveRouteResult,
			int rCode) {

	}
}
