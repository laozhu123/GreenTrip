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
	private int walkMode = RouteSearch.WalkDefault;// ����Ĭ��ģʽ
	private int busMode = RouteSearch.BusDefault;// ����Ĭ��ģʽ
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
	private PoiResult poiResult; // poi���صĽ��
	private PoiSearch.Query query;// Poi��ѯ������
	private PoiSearch poiSearch;// POI����
	private List<String> busID;// �������վ����Ϣ
	private List<BusStationList> busStationList;// �������listview�еĹ���վ�����Ϣ
	private List<String> walkID;
	private List<WalkStepList> walkStepLists;
	private List<PoiBike> poiBikes;
	private boolean isfirst = true;// ���Լ�¼�Ƿ��״ι滮����·�������ų�mixģʽ�µ��ٴι滮
	private boolean isfirstbike = true;// ���Լ�¼�Ƿ��״��������г����޵㣬���ų�mixģʽ�µ��ٴ�����
	private float mixdistance;// ���Լ�¼mixģʽ���������
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
		busStationList = new ArrayList<BusStationList>(); // ÿ���д�Ź�����������BusStationList����һ��array
		walkID = new ArrayList<String>();
		walkStepLists = new ArrayList<WalkStepList>(); // ÿ���д�Ų��ж�������WalkStepList����һ��array
		poiBikes = new ArrayList<PoiBike>(); // �������г����޵�
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

		// 3�ַ�ʽ��ʵ��4�� walk��bicycle������
		walkInstructions = new ArrayList<WalkInstruction>();
		adapter = new WalkAdapter(this, walkInstructions);
		pathlist.setAdapter(adapter);
		busInstructions = new ArrayList<BusInstruction>();
		busadapter = new BusAdapter(this, busInstructions);
		buspathlist.setAdapter(busadapter);
		mixInstructions = new ArrayList<MixInstruction>();
		mixadapter = new MixAdapter(this, mixInstructions);
		mixpathlist.setAdapter(mixadapter);

		// ���������б�
		busdetailInstructions = new ArrayList<BusDetailInstruction>();
		walkDetailInstructions = new ArrayList<WalkDetailInstruction>();

		Values.firstItems = new ArrayList<FirstItem>();
		Values.secondItems = new ArrayList<SecondItem>();
		FirstItem item = new FirstItem();
		item.setTitle("�ҵ�λ��");
		item.setType(4);
		Values.firstItems.add(item);
		SecondItem item2 = new SecondItem();
		item2.setSteps(new ArrayList<String>());
		Values.secondItems.add(item2);

		mAMap = mMapView.getMap();
		Intent intent = getIntent();
		/* ȡ��Intent�и��ӵ����� */
		mode = intent.getStringExtra("mode");
		city = intent.getStringExtra("city");
		double[] pathpoint = intent.getDoubleArrayExtra("pathpoint");
		startPoint = new LatLonPoint(pathpoint[0], pathpoint[1]);
		endPoint = new LatLonPoint(pathpoint[2], pathpoint[3]);
		fromAndTo = new FromAndTo(startPoint, endPoint);
		routeSearch = new RouteSearch(this);
		routeSearch.setRouteSearchListener(this);

		if (mode.equals("bus")) {
			title.setText("����·��");
			Values.path = "����·��";
			// ��һ��������ʾ·���滮�������յ㣬�ڶ���������ʾ������ѯģʽ
			// ������������ʾ������ѯ�������ţ����ĸ�������ʾ�Ƿ����ҹ�೵��0��ʾ������
			RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(
					fromAndTo, busMode, city, 0);
			routeSearch.calculateBusRouteAsyn(query);
		} else if (mode.equals("mix")) {
			title.setText("��ϻ���·��");
			Values.path = "���·��";
			// �����������Χ�����г����޵�
			doSearchQuery(startPoint);
		} else if (mode.equals("walk")) {
			title.setText("����·��");
			Values.path = "����·��";
			WalkRouteQuery query = new WalkRouteQuery(fromAndTo, walkMode);
			routeSearch.calculateWalkRouteAsyn(query);
		} else if (mode.equals("bicycle")) {
			title.setText("����·��");
			Values.path = "����·��";
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

		// �Ǹ������𡣡�������һ��
		detail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				FirstItem item1 = new FirstItem();
				item1.setTitle("Ŀ�ĵ�");
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
				item1.setTitle("Ŀ�ĵ�");
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
						// ��ʼ��������վ���������
						for (int j = 0; j < busStationList.size(); j++) {
							// �ҵ���Ӧ��վ�㣬��ȡ����
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
							// �ҵ���Ӧ��վ�㣬��ȡ����
							if ((walkStepLists.get(j).getWalkId() + "")
									.equals(walkID.get(i + 1))) {
								walkDetailInstructions.clear();
								for (int k = 0; k < walkStepLists.get(j)
										.getWalkstep().size(); k++) {
									WalkDetailInstruction instruction = new WalkDetailInstruction();
									if (!walkID.get(i + 1).contains("����")
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
											// �����������޸�Ϊ�������ݣ�α�ģ�
											if (bicyclepath[l].equals("��"))
												bicyclepath[l] = "��";
										}
										String bicycleinstruction = "";
										for (int s = 0; s < bicyclepath.length; s++)
											bicycleinstruction += bicyclepath[s];
										// ������·�����һ�ε���������������֡�Ŀ�ĵء��޸�Ϊ�ڶ������г����޵������
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
										.withTitle("����")
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
		// ·���滮�в���ģʽ
		DecimalFormat df = new DecimalFormat("###.0");
		if (rCode == 0 && walkRouteResult != null
				&& walkRouteResult.getPaths() != null
				&& walkRouteResult.getPaths().size() > 0) {
			WalkPath walkPath = walkRouteResult.getPaths().get(0);

			if (mode.equals("walk")) {
				/**
 * 
 */

				Values.path = "��" + df.format(walkPath.getDistance() / 1000)
						+ "����" + "|Լ" + walkPath.getDuration() / 3600 + "Сʱ"
						+ walkPath.getDuration() % 3600 / 60 + "����";
			} else if (mode.equals("bicycle")) {
				/**
 * 
 */

				Values.path = "��" + df.format(walkPath.getDistance() / 1000)
						+ "����" + "|Լ" + walkPath.getDuration() / 7200 + "Сʱ"
						+ (walkPath.getDuration() / 2) % 3600 / 60 + "����";
			}
			// mixģʽ�µĵ�һ��·���滮
			else if (isfirst && mode.equals("mix")) {
				/**
 * 
 */
				Values.busline = "���·��";

				mixInstructions.clear();
				walkID.clear();
				walkStepLists.clear();
				mixdistance += walkPath.getDistance() / 1000;
				mixwalkdistance += walkPath.getDistance() / 1000;
				mixtime += walkPath.getDuration();
				MixInstruction instruction = new MixInstruction();
				instruction.setImgId(R.drawable.walk);
				instruction.setName("����" + (int) walkPath.getDistance() + "��");
				instruction.setColor(Color.parseColor("#77B943"));
				mixInstructions.add(instruction);
				WalkStepList stationList = new WalkStepList();
				stationList.setWalkId(walkPath.toString());
				stationList.setWalkstep(walkPath.getSteps());
				// ����������ȥ������������ʱֱ�ӻ�ȡ
				walkStepLists.add(stationList);
				walkID.add(mixInstructions.size() - 1 + "");
				walkID.add(walkPath.toString());

				/**
 * 
 */
				FirstItem item1 = new FirstItem();
				item1.setType(1);
				item1.setTitle("����" + (int) walkPath.getDistance() + "��");
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

				mAMap.clear();// ����֮ǰ��ͼ��
				WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(this,
						mAMap, walkPath, walkRouteResult.getStartPos(),
						walkRouteResult.getTargetPos());
				walkRouteOverlay.removeFromMap();
				walkRouteOverlay.setNodeIconVisibility(false);
				walkRouteOverlay.addToMap();
				walkRouteOverlay.zoomToSpan();
				// �ٽ��Ź滮�����г����޵�֮����·
				startPoint = poiBikes.get(0).getPosition();
				LatLonPoint endPoint = poiBikes.get(1).getPosition();
				FromAndTo fromAndTo = new FromAndTo(startPoint, endPoint);
				isfirst = false;
				WalkRouteQuery query = new WalkRouteQuery(fromAndTo, walkMode);
				routeSearch.calculateWalkRouteAsyn(query);
			}
			// mixģʽ�µĵڶ���·���滮
			else if (!isfirst && mode.equals("mix")) {
				mixdistance += walkPath.getDistance() / 1000;
				mixbicycledistance += walkPath.getDistance() / 1000;
				mixtime += walkPath.getDuration() / 2;
				MixInstruction instruction = new MixInstruction();
				instruction.setImgId(R.drawable.walk);
				instruction.setName("����" + (int) walkPath.getDistance() + "��");
				instruction.setColor(Color.parseColor("#0000ff"));
				mixInstructions.add(instruction);
				WalkStepList stationList = new WalkStepList();
				stationList.setWalkId(walkPath.toString() + "����");
				stationList.setWalkstep(walkPath.getSteps());
				// ����������ȥ������������ʱֱ�ӻ�ȡ
				walkStepLists.add(stationList);
				walkID.add(mixInstructions.size() - 1 + "");
				walkID.add(walkPath.toString() + "����");
				WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(this,
						mAMap, walkPath, walkRouteResult.getStartPos(),
						walkRouteResult.getTargetPos());
				walkRouteOverlay.removeFromMap();
				walkRouteOverlay.setNodeIconVisibility(false);
				walkRouteOverlay.addToMap();
				walkRouteOverlay.zoomToSpan();
				// ��������·�����������һ����·����ȫ���滮���
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
				item1_3.setTitle("���г����޵㣨�賵��");
				Values.firstItems.add(item1_3);
				Values.secondItems.add(new SecondItem());

				FirstItem item1 = new FirstItem();
				item1.setType(2);
				item1.setTitle("����" + (int) walkPath.getDistance() + "��");
				Values.firstItems.add(item1);
				List<WalkStep> heel = walkPath.getSteps();
				SecondItem item2 = new SecondItem();
				item2.setSteps(new ArrayList<String>());
				for (int j = 0; j < heel.size(); j++) {
					if (heel.get(j).getInstruction() == null) {
						break;
					}
					item2.getSteps().add(
							heel.get(j).getInstruction().replace("��", "��"));
				}
				Values.secondItems.add(item2);

				FirstItem item1_2 = new FirstItem();
				item1_2.setType(4);
				item1_2.setTitle("���г����޵㣨������");
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
							// �����������޸�Ϊ�������ݣ�α�ģ�
							if (bicyclepath[j].equals("��"))
								bicyclepath[j] = "��";
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
					item1.setTitle("����" + (int) walkPath.getDistance() + "��");
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
					item1.setTitle("����" + (int) walkPath.getDistance() + "��");
					Values.firstItems.add(item1);
					List<WalkStep> heel = walkPath.getSteps();
					SecondItem item2 = new SecondItem();
					item2.setSteps(new ArrayList<String>());
					for (int j = 0; j < heel.size(); j++) {
						if (heel.get(j).getInstruction() == null) {
							break;
						}
						item2.getSteps().add(
								heel.get(j).getInstruction().replace("��", "��"));
					}
					Values.secondItems.add(item2);

				}

				adapter.notifyDataSetChanged();
				mAMap.clear();// ����֮ǰ��ͼ��
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
		// ·���滮�й���ģʽ
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
			/* ȡ��Intent�и��ӵ����� */
			int pathnum;
			BusPath busPath;

			/**
* 
* */
			pathnum = getIntent().getIntExtra("buspathnum", 0);
			busPath = busRouteResult.getPaths().get(pathnum);
			// ȡ����Ӧ����������·�εĹ�����·��
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
				path.setText("��" + df.format(busPath.getDistance() / 1000)
						+ "����" + "|Լ" + busPath.getDuration() / 3600 + "Сʱ"
						+ busPath.getDuration() % 3600 / 60 + "����|����"
						+ (int) busPath.getWalkDistance() + "��");
				busInstructions.clear();
				/**
	 * 
	 */
				Values.path = path.getText().toString();
			}

			// ������벻Ҫ��
			busPath = busRouteResult.getPaths().get(0);
			// mixģʽ�µĵ�����·���滮
			if (mode.equals("mix")) {
				mixdistance += busPath.getDistance() / 1000;
				mixwalkdistance += busPath.getWalkDistance() / 1000;
				mixtime += busPath.getDuration();
				if (poiBikes.size() > 0)
					path.setText("�������г�&" + busname + "|"
							+ df.format(mixdistance) + "����" + "|����"
							+ df.format(mixwalkdistance) + "����|����"
							+ df.format(mixbicycledistance) + "����|Լ" + mixtime
							/ 3600 + "Сʱ" + mixtime % 3600 / 60 + "����");
				else
					path.setText("��" + df.format(busPath.getDistance() / 1000)
							+ "����" + "|Լ" + busPath.getDuration() / 3600 + "Сʱ"
							+ busPath.getDuration() % 3600 / 60 + "����|����"
							+ (int) busPath.getWalkDistance() + "��");
				/**
 * 
 */
				Values.path = path.getText().toString();
			}

			// ������·�滮�㷨(д��һ����....)
			for (int i = 0; i < busPath.getSteps().size(); i++) {
				// ��������
				if (mode.equals("bus")) {
					BusInstruction instruction = new BusInstruction();
					instruction.setName("����"
							+ (int) busPath.getSteps().get(i).getWalk()
									.getDistance() + "��");
					instruction.setImgId(R.drawable.walk);
					busInstructions.add(instruction);
					/**
 * 
 */
					FirstItem item = new FirstItem();
					item.setType(1);
					item.setTitle("����"
							+ (int) busPath.getSteps().get(i).getWalk()
									.getDistance() + "��");
					Values.firstItems.add(item);

					WalkStepList stationList = new WalkStepList();
					stationList.setWalkId(busPath.getSteps().get(i).getWalk()
							.toString());
					// �ߵ�list
					stationList.setWalkstep(busPath.getSteps().get(i).getWalk()
							.getSteps());

					// ����������ȥ������������ʱֱ�ӻ�ȡ
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
					instruction.setName("����"
							+ (int) busPath.getSteps().get(i).getWalk()
									.getDistance() + "��");
					instruction.setImgId(R.drawable.walk);
					instruction.setColor(Color.parseColor("#77B943"));
					mixInstructions.add(instruction);

					/**
 * 
 */
					FirstItem item = new FirstItem();
					item.setType(1);
					item.setTitle("����"
							+ (int) busPath.getSteps().get(i).getWalk()
									.getDistance() + "��");
					Values.firstItems.add(item);

					WalkStepList stationList = new WalkStepList();
					stationList.setWalkId(busPath.getSteps().get(i).getWalk()
							.toString()
							+ "bus");
					stationList.setWalkstep(busPath.getSteps().get(i).getWalk()
							.getSteps());
					// ����������ȥ������������ʱֱ�ӻ�ȡ
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
				// ��������
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

					// import ��ȡ����վ
					stationList.setBusStationItems(busPath.getSteps().get(i)
							.getBusLine().getPassStations());

					stationList.setPassStationNum(busPath.getSteps().get(i)
							.getBusLine().getPassStationNum());
					// ����������ȥ������������ʱֱ�ӻ�ȡ
					busStationList.add(stationList);
					if (mode.equals("bus")) {
						BusInstruction start = new BusInstruction();
						// ȡ����Ӧ������·�εĹ�����·��
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
								+ "վ�ϳ�" + "(����" + buslinename + ")");
						start.setColor(Color.parseColor("#FF6100"));
						start.setImgId(R.drawable.car);
						busInstructions.add(start);
						BusInstruction arrive = new BusInstruction();
						arrive.setName(busPath.getSteps().get(i).getBusLine()
								.getArrivalBusStation().getBusStationName()
								+ "վ�³�");
						arrive.setImgId(R.drawable.car);
						busInstructions.add(arrive);
						busID.add(busInstructions.size() - 1 + "");
						busID.add(busPath.getSteps().get(i).getBusLine()
								.getBusLineId());

						/**
 * 		
 */
						// 1���
						FirstItem item1_1 = new FirstItem();
						item1_1.setTitle(busPath.getSteps().get(i).getBusLine()
								.getDepartureBusStation().getBusStationName()
								+ "վ�ϳ�");
						item1_1.setType(4);
						Values.firstItems.add(item1_1);
						SecondItem item2_1 = new SecondItem();
						item2_1.setSteps(new ArrayList<String>());
						Values.secondItems.add(item2_1);
						// 2����
						FirstItem item1_2 = new FirstItem();
						item1_2.setTitle("����" + buslinename + "��");
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
									+ "����  | "
									+ busPath.getSteps().get(i).getBusLine()
											.getPassStationNum() + "վ");
						} else {
							item1_2.setTitle2("�ù���·����ͣ��");
							Values.busline += "|���Ƽ���·�� ����ͣ��";
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
						// 3�յ�
						FirstItem item1_3 = new FirstItem();
						item1_3.setTitle(busPath.getSteps().get(i).getBusLine()
								.getArrivalBusStation().getBusStationName()
								+ "վ�³�");
						item1_3.setType(4);
						Values.firstItems.add(item1_3);
						Values.secondItems.add(new SecondItem());

					} else if (mode.equals("mix")) {
						MixInstruction start = new MixInstruction();
						// ȡ����Ӧ������·�εĹ�����·��
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
								+ "վ�ϳ�" + "(����" + buslinename + ")");
						start.setColor(Color.parseColor("#FF6100"));
						start.setImgId(R.drawable.car);
						mixInstructions.add(start);
						MixInstruction arrive = new MixInstruction();
						arrive.setName(busPath.getSteps().get(i).getBusLine()
								.getArrivalBusStation().getBusStationName()
								+ "վ�³�");
						arrive.setImgId(R.drawable.car);
						mixInstructions.add(arrive);
						busID.add(mixInstructions.size() - 1 + "");
						busID.add(busPath.getSteps().get(i).getBusLine()
								.getBusLineId());
						/**
 * 	
 */
						// 1���
						FirstItem item1_1 = new FirstItem();
						item1_1.setTitle(busPath.getSteps().get(i).getBusLine()
								.getDepartureBusStation().getBusStationName()
								+ "վ�ϳ�");
						item1_1.setType(4);
						Values.firstItems.add(item1_1);
						SecondItem item2_1 = new SecondItem();
						item2_1.setSteps(new ArrayList<String>());
						Values.secondItems.add(item2_1);
						// 2����
						FirstItem item1_2 = new FirstItem();
						item1_2.setTitle("����" + buslinename + "��");
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
									+ "����  | "
									+ busPath.getSteps().get(i).getBusLine()
											.getPassStationNum() + "վ");
						} else {
							item1_2.setTitle2("�ù���·����ͣ��");
							Values.busline += "|���Ƽ���·�� ����ͣ��";
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
						// 3�յ�
						FirstItem item1_3 = new FirstItem();
						item1_3.setTitle(busPath.getSteps().get(i).getBusLine()
								.getArrivalBusStation().getBusStationName()
								+ "վ�³�");
						item1_3.setType(4);
						Values.firstItems.add(item1_3);
						Values.secondItems.add(new SecondItem());
					}
				}
			}

			if (mode.equals("bus")) {
				busadapter.notifyDataSetChanged();
				mAMap.clear();// ����֮ǰ��ͼ��
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
	 * ��ʼ����poi����,�ҹ������г����޵�
	 */
	protected void doSearchQuery(LatLonPoint latLonPoint) {
		query = new PoiSearch.Query("���г����޵�", "", city);
		// ��һ��������ʾ�����ַ������ڶ���������ʾpoi�������ͣ�������������ʾpoi�������򣨿��ַ�������ȫ����
		query.setPageSize(30);// ����ÿҳ��෵�ض�����poiitem
		query.setPageNum(0);// ���ò��һҳ
		poiSearch = new PoiSearch(this, query);
		poiSearch.setBound(new SearchBound(latLonPoint, 2000));
		poiSearch.setOnPoiSearchListener(this);
		poiSearch.searchPOIAsyn();
	}

	@Override
	public void onPoiSearched(PoiResult result, int rCode) {
		if (rCode == 0 && result != null && result.getQuery() != null
				&& result.getQuery().equals(query)) {// ����poi�Ľ��
			poiResult = result;
			// ȡ����������poiitems�ж���ҳ
			List<PoiItem> poiItems = poiResult.getPois();// ȡ�õ�һҳ��poiitem���ݣ�ҳ��������0��ʼ
			// ��Ҫ�ҵ��������г����޵�,��ȡͷ���м���������г�վ��
			if (isfirstbike && poiItems != null && poiItems.size() > 0) {
				PoiBike bike = new PoiBike();
				bike.setBikeRental(poiItems.get(0).toString());
				bike.setPosition(poiItems.get(0).getLatLonPoint());
				poiBikes.add(bike);
				isfirstbike = false;
				// �ҵڶ������г����޵㣬����ֹ���е�Ϊ��������
				doSearchQuery(new LatLonPoint(
						(startPoint.getLatitude() + endPoint.getLatitude()) / 2,
						(startPoint.getLongitude() + endPoint.getLongitude()) / 2));
			} else if (!isfirstbike && poiItems != null && poiItems.size() > 0) {
				PoiBike bike = new PoiBike();
				bike.setBikeRental(poiItems.get(0).toString());
				bike.setPosition(poiItems.get(0).getLatLonPoint());
				poiBikes.add(bike);
				// �ȹ滮����·��
				LatLonPoint endPoint = poiBikes.get(0).getPosition();
				fromAndTo = new FromAndTo(startPoint, endPoint);
				WalkRouteQuery query = new WalkRouteQuery(fromAndTo, walkMode);
				routeSearch.calculateWalkRouteAsyn(query);
			} else if ((isfirst && poiBikes.size() == 0)
					|| (!isfirstbike && poiBikes.size() < 2)) {
				poiBikes.clear();
				// û�����г����ݵĻ�ֱ�ӹ滮������·
				RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(
						fromAndTo, busMode, city, 0);
				routeSearch.calculateBusRouteAsyn(query);
			} else {
				poiBikes.clear();
				// û�����г����ݵĻ�ֱ�ӹ滮������·
				RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(
						fromAndTo, busMode, city, 0);
				routeSearch.calculateBusRouteAsyn(query);
			}
		} else {
			Toast.makeText(RouteActivity.this, "��ȷ���Ƿ�����", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * �˷��������
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	/**
	 * �˷��������
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	/**
	 * �˷��������
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
