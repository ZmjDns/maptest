package com.maptest.overlay;

import android.content.Context;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.Doorway;
import com.amap.api.services.route.RailwayStationItem;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteBusWalkItem;
import com.amap.api.services.route.RouteRailwayItem;
import com.amap.api.services.route.TaxiItem;
import com.amap.api.services.route.WalkStep;
import com.maptest.utils.AMapServicesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 公交路线图层类。在高德地图API里，如果需要显示公交路线，可以用此类来创建公交路线图层。如不满足需求，也可以自己创建自定义的公交路线图层。
 * @since V2.1.0
 */
public class BusRouteOverlay extends RouteOverlay {

	private BusPath busPath;
	private LatLng latLng;

	/**
	 * 通过此构造函数创建公交路线图层。
	 * @param context 当前activity。
	 * @param amap 地图对象。
	 * @param path 公交路径规划的一个路段。详见搜索服务模块的路径查询包（com.amap.api.services.route）中的类<strong> <a href="../../../../../../Search/com/amap/api/services/route/BusPath.html" title="com.amap.api.services.route中的类">BusPath</a></strong>。
	 * @param start 起点坐标。详见搜索服务模块的核心基础包（com.amap.api.services.core）中的类 <strong><a href="../../../../../../Search/com/amap/api/services/core/LatLonPoint.html" title="com.amap.api.services.core中的类">LatLonPoint</a></strong>。
	 * @param end 终点坐标。详见搜索服务模块的核心基础包（com.amap.api.services.core）中的类 <strong><a href="../../../../../../Search/com/amap/api/services/core/LatLonPoint.html" title="com.amap.api.services.core中的类">LatLonPoint</a></strong>。
	 * @since V2.1.0
	 */
	public BusRouteOverlay(Context context, AMap amap, BusPath path,
						   LatLonPoint start, LatLonPoint end) {
		super(context);
		this.busPath = path;
		startPoint = AMapServicesUtil.convertToLatLng(start);
		endPoint = AMapServicesUtil.convertToLatLng(end);
		mAMap = amap;
	}

	/**
	 * 添加公交路线到地图上。
	 * @since V2.1.0
	 */

	public void addToMap() {
		/**
		 * 绘制节点和线<br>
		 * 细节情况较多<br>
		 * 两个step之间，用step和step1区分<br>
		 * 1.一个step内可能有步行和公交，然后有可能他们之间连接有断开<br>
		 * 2.step的公交和step1的步行，有可能连接有断开<br>
		 * 3.step和step1之间是公交换乘，且没有步行，需要把step的终点和step1的起点连起来<br>
		 * 4.公交最后一站和终点间有步行，加入步行线路，还会有一些步行marker<br>
		 * 5.公交最后一站和终点间无步行，之间连起来<br>
		 */
		try {
			List<BusStep> busSteps = busPath.getSteps();
			for (int i = 0; i < busSteps.size(); i++) {
				BusStep busStep = busSteps.get(i);
				if (i < busSteps.size() - 1) {
					BusStep busStep1 = busSteps.get(i + 1);// 取得当前下一个BusStep对象
					// 假如步行和公交之间连接有断开，就把步行最后一个经纬度点和公交第一个经纬度点连接起来，避免断线问题
					if (busStep.getWalk() != null
							&& busStep.getBusLine() != null) {
						checkWalkToBusline(busStep);
					}

					// 假如公交和步行之间连接有断开，就把上一公交经纬度点和下一步行第一个经纬度点连接起来，避免断线问题
					if (busStep.getBusLine() != null
							&& busStep1.getWalk() != null
							&& busStep1.getWalk().getSteps().size() > 0) {
						checkBusLineToNextWalk(busStep, busStep1);
					}
					// 假如两个公交换乘中间没有步行，就把上一公交经纬度点和下一步公交第一个经纬度点连接起来，避免断线问题
					if (busStep.getBusLine() != null
							&& busStep1.getWalk() == null
							&& busStep1.getBusLine() != null) {
						checkBusEndToNextBusStart(busStep, busStep1);
					}
					// 和上面的很类似
					if (busStep.getBusLine() != null
							&& busStep1.getWalk() == null
							&& busStep1.getBusLine() != null) {
						checkBusToNextBusNoWalk(busStep, busStep1);
					}
					if (busStep.getBusLine() != null
							&& busStep1.getRailway() != null ) {
						checkBusLineToNextRailway(busStep, busStep1);
					}
					if (busStep1.getWalk() != null &&
							busStep1.getWalk().getSteps().size() > 0 &&
							busStep.getRailway() != null) {
						checkRailwayToNextWalk(busStep, busStep1);
					}

					if ( busStep1.getRailway() != null &&
							busStep.getRailway() != null) {
						checkRailwayToNextRailway(busStep, busStep1);
					}

					if (busStep.getRailway() != null &&
							busStep1.getTaxi() != null ){
						checkRailwayToNextTaxi(busStep, busStep1);
					}


				}

				if (busStep.getWalk() != null
						&& busStep.getWalk().getSteps().size() > 0) {
					addWalkSteps(busStep);
				} else {
					if (busStep.getBusLine() == null && busStep.getRailway() == null && busStep.getTaxi() == null) {
						addWalkPolyline(latLng, endPoint);
					}
				}
				if (busStep.getBusLine() != null) {
					RouteBusLineItem routeBusLineItem = busStep.getBusLine();
					addBusLineSteps(routeBusLineItem);
					addBusStationMarkers(routeBusLineItem);
					if (i == busSteps.size() - 1) {
						addWalkPolyline(AMapServicesUtil.convertToLatLng(getLastBuslinePoint(busStep)), endPoint);
					}
				}
				if (busStep.getRailway() != null) {
					addRailwayStep(busStep.getRailway());
					addRailwayMarkers(busStep.getRailway());
					if (i == busSteps.size() - 1) {
						addWalkPolyline(AMapServicesUtil.convertToLatLng(busStep.getRailway().getArrivalstop().getLocation()), endPoint);
					}
				}
				if (busStep.getTaxi() != null) {
					addTaxiStep(busStep.getTaxi());
					addTaxiMarkers(busStep.getTaxi());
				}
			}
			addStartAndEndMarker();

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}



	private void checkRailwayToNextTaxi(BusStep busStep, BusStep busStep1) {
		LatLonPoint railwayLastPoint = busStep.getRailway().getArrivalstop().getLocation();
		LatLonPoint taxiFirstPoint = busStep1.getTaxi().getOrigin();
		if (!railwayLastPoint.equals(taxiFirstPoint)) {
			addWalkPolyLineByLatLonPoints(railwayLastPoint, taxiFirstPoint);
		}
	}

	private void checkRailwayToNextRailway(BusStep busStep, BusStep busStep1) {
		LatLonPoint railwayLastPoint = busStep.getRailway().getArrivalstop().getLocation();
		LatLonPoint railwayFirstPoint = busStep1.getRailway().getDeparturestop().getLocation();
		if (!railwayLastPoint.equals(railwayFirstPoint)) {
			addWalkPolyLineByLatLonPoints(railwayLastPoint, railwayFirstPoint);
		}
	}

	private void checkBusLineToNextRailway(BusStep busStep, BusStep busStep1) {
		LatLonPoint busLastPoint = getLastBuslinePoint(busStep);
		LatLonPoint railwayFirstPoint = busStep1.getRailway().getDeparturestop().getLocation();
		if (!busLastPoint.equals(railwayFirstPoint)) {
			addWalkPolyLineByLatLonPoints(busLastPoint, railwayFirstPoint);
		}
	}

	private void checkRailwayToNextWalk(BusStep busStep, BusStep busStep1) {
		LatLonPoint railwayLastPoint = busStep.getRailway().getArrivalstop().getLocation();
		LatLonPoint walkFirstPoint = getFirstWalkPoint(busStep1);
		if (!railwayLastPoint.equals(walkFirstPoint)) {
			addWalkPolyLineByLatLonPoints(railwayLastPoint, walkFirstPoint);
		}

	}

	private void addRailwayStep(RouteRailwayItem railway) {
		List<LatLng> railwaylistpoint = new ArrayList<LatLng>();
		List<RailwayStationItem> railwayStationItems = new ArrayList<RailwayStationItem>();
		railwayStationItems.add(railway.getDeparturestop());
		railwayStationItems.addAll(railway.getViastops());
		railwayStationItems.add(railway.getArrivalstop());
		for (int i = 0; i < railwayStationItems.size(); i++) {
			railwaylistpoint.add(AMapServicesUtil.convertToLatLng(railwayStationItems.get(i).getLocation()));
		}
		addRailwayPolyline(railwaylistpoint);
	}

	private void addTaxiStep(TaxiItem taxi){
		addPolyLine(new PolylineOptions().width(getRouteWidth())
				.color(getBusColor())
				.add(AMapServicesUtil.convertToLatLng(taxi.getOrigin()))
				.add(AMapServicesUtil.convertToLatLng(taxi.getDestination())));
	}

	/**
	 * @param busStep
	 */
	private void addWalkSteps(BusStep busStep) {
		RouteBusWalkItem routeBusWalkItem = busStep.getWalk();
		List<WalkStep> walkSteps = routeBusWalkItem.getSteps();
		for (int j = 0; j < walkSteps.size(); j++) {
			WalkStep walkStep = walkSteps.get(j);
			if (j == 0) {
				LatLng latLng = AMapServicesUtil.convertToLatLng(walkStep
						.getPolyline().get(0));
				String road = walkStep.getRoad();// 道路名字
				String instruction = getWalkSnippet(walkSteps);// 步行导航信息
				addWalkStationMarkers(latLng, road, instruction);
			}

			List<LatLng> listWalkPolyline = AMapServicesUtil
					.convertArrList(walkStep.getPolyline());
			this.latLng = listWalkPolyline.get(listWalkPolyline.size() - 1);

			addWalkPolyline(listWalkPolyline);

			// 假如步行前一段的终点和下的起点有断开，断画直线连接起来，避免断线问题
			if (j < walkSteps.size() - 1) {
				LatLng lastLatLng = listWalkPolyline.get(listWalkPolyline
						.size() - 1);
				LatLng firstlatLatLng = AMapServicesUtil
						.convertToLatLng(walkSteps.get(j + 1).getPolyline()
								.get(0));
				if (!(lastLatLng.equals(firstlatLatLng))) {
					addWalkPolyline(lastLatLng, firstlatLatLng);
				}
			}

		}
	}

	/**
	 * 添加一系列的bus PolyLine
	 *
	 * @param routeBusLineItem
	 */
	private void addBusLineSteps(RouteBusLineItem routeBusLineItem) {
		addBusLineSteps(routeBusLineItem.getPolyline());
	}

	private void addBusLineSteps(List<LatLonPoint> listPoints) {
		if (listPoints.size() < 1) {
			return;
		}
		addPolyLine(new PolylineOptions().width(getRouteWidth())
				.color(getBusColor())
				.addAll(AMapServicesUtil.convertArrList(listPoints)));
	}

	/**
	 * @param latLng
	 *            marker
	 * @param title
	 * @param snippet
	 */
	private void addWalkStationMarkers(LatLng latLng, String title,
									   String snippet) {
		addStationMarker(new MarkerOptions().position(latLng).title(title)
				.snippet(snippet).anchor(0.5f, 0.5f).visible(nodeIconVisible)
				.icon(getWalkBitmapDescriptor()));
	}

	/**
	 * @param routeBusLineItem
	 */
	private void addBusStationMarkers(RouteBusLineItem routeBusLineItem) {
		BusStationItem startBusStation = routeBusLineItem
				.getDepartureBusStation();
		LatLng position = AMapServicesUtil.convertToLatLng(startBusStation
				.getLatLonPoint());
		String title = routeBusLineItem.getBusLineName();
		String snippet = getBusSnippet(routeBusLineItem);

		addStationMarker(new MarkerOptions().position(position).title(title)
				.snippet(snippet).anchor(0.5f, 0.5f).visible(nodeIconVisible)
				.icon(getBusBitmapDescriptor()));
	}

	private void addTaxiMarkers(TaxiItem taxiItem) {

		LatLng position = AMapServicesUtil.convertToLatLng(taxiItem
				.getOrigin());
		String title = taxiItem.getmSname()+"打车";
		String snippet = "到终点";

		addStationMarker(new MarkerOptions().position(position).title(title)
				.snippet(snippet).anchor(0.5f, 0.5f).visible(nodeIconVisible)
				.icon(getDriveBitmapDescriptor()));
	}

	private void addRailwayMarkers(RouteRailwayItem railway) {
		LatLng Departureposition = AMapServicesUtil.convertToLatLng(railway
				.getDeparturestop().getLocation());
		String Departuretitle = railway.getDeparturestop().getName()+"上车";
		String Departuresnippet = railway.getName();

		addStationMarker(new MarkerOptions().position(Departureposition).title(Departuretitle)
				.snippet(Departuresnippet).anchor(0.5f, 0.5f).visible(nodeIconVisible)
				.icon(getBusBitmapDescriptor()));


		LatLng Arrivalposition = AMapServicesUtil.convertToLatLng(railway
				.getArrivalstop().getLocation());
		String Arrivaltitle = railway.getArrivalstop().getName()+"下车";
		String Arrivalsnippet = railway.getName();

		addStationMarker(new MarkerOptions().position(Arrivalposition).title(Arrivaltitle)
				.snippet(Arrivalsnippet).anchor(0.5f, 0.5f).visible(nodeIconVisible)
				.icon(getBusBitmapDescriptor()));
	}
	/**
	 * 如果换乘没有步行 检查bus最后一点和下一个step的bus起点是否一致
	 *
	 * @param busStep
	 * @param busStep1
	 */
	private void checkBusToNextBusNoWalk(BusStep busStep, BusStep busStep1) {
		LatLng endbusLatLng = AMapServicesUtil
				.convertToLatLng(getLastBuslinePoint(busStep));
		LatLng startbusLatLng = AMapServicesUtil
				.convertToLatLng(getFirstBuslinePoint(busStep1));
		if (startbusLatLng.latitude - endbusLatLng.latitude > 0.0001
				|| startbusLatLng.longitude - endbusLatLng.longitude > 0.0001) {
			drawLineArrow(endbusLatLng, startbusLatLng);// 断线用带箭头的直线连?
		}
	}

	/**
	 *
	 * checkBusToNextBusNoWalk 和这个类似
	 *
	 * @param busStep
	 * @param busStep1
	 */
	private void checkBusEndToNextBusStart(BusStep busStep, BusStep busStep1) {
		LatLonPoint busLastPoint = getLastBuslinePoint(busStep);
		LatLng endbusLatLng = AMapServicesUtil.convertToLatLng(busLastPoint);
		LatLonPoint busFirstPoint = getFirstBuslinePoint(busStep1);
		LatLng startbusLatLng = AMapServicesUtil.convertToLatLng(busFirstPoint);
		if (!endbusLatLng.equals(startbusLatLng)) {
			drawLineArrow(endbusLatLng, startbusLatLng);//
		}
	}

	/**
	 * 检查bus最后一步和下一各step的步行起点是否一致
	 *
	 * @param busStep
	 * @param busStep1
	 */
	private void checkBusLineToNextWalk(BusStep busStep, BusStep busStep1) {
		LatLonPoint busLastPoint = getLastBuslinePoint(busStep);
		LatLonPoint walkFirstPoint = getFirstWalkPoint(busStep1);
		if (!busLastPoint.equals(walkFirstPoint)) {
			addWalkPolyLineByLatLonPoints(busLastPoint, walkFirstPoint);
		}
	}

	/**
	 * 检查 步行最后一点 和 bus的起点 是否一致
	 *
	 * @param busStep
	 */
	private void checkWalkToBusline(BusStep busStep) {
		LatLonPoint walkLastPoint = getLastWalkPoint(busStep);
		LatLonPoint buslineFirstPoint = getFirstBuslinePoint(busStep);

		if (!walkLastPoint.equals(buslineFirstPoint)) {
			addWalkPolyLineByLatLonPoints(walkLastPoint, buslineFirstPoint);
		}
	}

	/**
	 * @param busStep1
	 * @return
	 */
	private LatLonPoint getFirstWalkPoint(BusStep busStep1) {
		return busStep1.getWalk().getSteps().get(0).getPolyline().get(0);
	}

	/**
	 *
	 */
	private void addWalkPolyLineByLatLonPoints(LatLonPoint pointFrom,
											   LatLonPoint pointTo) {
		LatLng latLngFrom = AMapServicesUtil.convertToLatLng(pointFrom);
		LatLng latLngTo = AMapServicesUtil.convertToLatLng(pointTo);

		addWalkPolyline(latLngFrom, latLngTo);
	}

	/**
	 * @param latLngFrom
	 * @param latLngTo
	 * @return
	 */
	private void addWalkPolyline(LatLng latLngFrom, LatLng latLngTo) {
		addPolyLine(new PolylineOptions().add(latLngFrom, latLngTo)
				.width(getRouteWidth()).color(getWalkColor()).setDottedLine(true));
	}

	/**
	 * @param listWalkPolyline
	 */
	private void addWalkPolyline(List<LatLng> listWalkPolyline) {

		addPolyLine(new PolylineOptions().addAll(listWalkPolyline)
				.color(getWalkColor()).width(getRouteWidth()).setDottedLine(true));
	}

	private void addRailwayPolyline(List<LatLng> listPolyline) {

		addPolyLine(new PolylineOptions().addAll(listPolyline)
				.color(getDriveColor()).width(getRouteWidth()));
	}


	private String getWalkSnippet(List<WalkStep> walkSteps) {
		float disNum = 0;
		for (WalkStep step : walkSteps) {
			disNum += step.getDistance();
		}
		return "\u6B65\u884C" + disNum + "\u7C73";
	}

	public void drawLineArrow(LatLng latLngFrom, LatLng latLngTo) {

		addPolyLine(new PolylineOptions().add(latLngFrom, latLngTo).width(3)
				.color(getBusColor()).width(getRouteWidth()));// 绘制直线
	}

	private String getBusSnippet(RouteBusLineItem routeBusLineItem) {
		return "("
				+ routeBusLineItem.getDepartureBusStation().getBusStationName()
				+ "-->"
				+ routeBusLineItem.getArrivalBusStation().getBusStationName()
				+ ") \u7ECF\u8FC7" + (routeBusLineItem.getPassStationNum() + 1)
				+ "\u7AD9";
	}

	/**
	 * @param busStep
	 * @return
	 */
	private LatLonPoint getLastWalkPoint(BusStep busStep) {

		List<WalkStep> walkSteps = busStep.getWalk().getSteps();
		WalkStep walkStep = walkSteps.get(walkSteps.size() - 1);
		List<LatLonPoint> lonPoints = walkStep.getPolyline();
		return lonPoints.get(lonPoints.size() - 1);
	}

	private LatLonPoint getExitPoint(BusStep busStep) {
		Doorway doorway = busStep.getExit();
		if (doorway == null) {
			return null;
		}
		return doorway.getLatLonPoint();
	}

	private LatLonPoint getLastBuslinePoint(BusStep busStep) {
		List<LatLonPoint> lonPoints = busStep.getBusLine().getPolyline();

		return lonPoints.get(lonPoints.size() - 1);
	}

	private LatLonPoint getEntrancePoint(BusStep busStep) {
		Doorway doorway = busStep.getEntrance();
		if (doorway == null) {
			return null;
		}
		return doorway.getLatLonPoint();
	}

	private LatLonPoint getFirstBuslinePoint(BusStep busStep) {
		return busStep.getBusLine().getPolyline().get(0);
	}
}




//import android.content.Context;
//
//import com.amap.api.maps.AMap;
//import com.amap.api.maps.model.LatLng;
//import com.amap.api.maps.model.Marker;
//import com.amap.api.maps.model.MarkerOptions;
//import com.amap.api.maps.model.PolylineOptions;
//import com.amap.api.services.busline.BusStationItem;
//import com.amap.api.services.core.LatLonPoint;
//import com.amap.api.services.route.BusPath;
//import com.amap.api.services.route.BusStep;
//import com.amap.api.services.route.RailwayStationItem;
//import com.amap.api.services.route.RouteBusLineItem;
//import com.amap.api.services.route.RouteBusWalkItem;
//import com.amap.api.services.route.RouteRailwayItem;
//import com.amap.api.services.route.TaxiItem;
//import com.amap.api.services.route.WalkStep;
//import com.maptest.utils.AMapServicesUtil;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by ZMJ on 2018/1/24.ss
// */
//public class BusRouteOverlay extends RouteOverlay {
//	private BusPath busPath;
//	private LatLng latLng;
//
//	public BusRouteOverlay(Context context) {
//		super(context);
//	}
//
//	public BusRouteOverlay(Context context, AMap aMap, BusPath busPath, LatLonPoint start,LatLonPoint end){
//		super(context);
//		this.busPath = busPath;
//		startPoint = AMapServicesUtil.convertToLatLng(start);
//		endPoint = AMapServicesUtil.convertToLatLng(end);
//		mAMap = aMap;
//	}
//
//	public void addToMap(){
//		try{
//			List<BusStep> busSteps = busPath.getSteps();
//			for (int i = 0 ; i < busSteps.size(); i++){
//				BusStep busStep = busSteps.get(i);
//				if (i < busSteps.size() - 1){
//					BusStep busStep1 = busSteps.get(i + 1);//获取下一个busStep对象
//					//假如步行与公交之间有间隙，就把步行的最后一个经纬度与公交的第一个经纬度连接起来
//					if (busStep.getWalk() != null && busStep.getBusLine() != null){
//						checkWalkToBusline(busStep);
//					}
//					//假如bus与walk之间有间隙，就把bus的最后一个点与walk的第一个点连接起来
//					if (busStep.getBusLine() != null && busStep1.getWalk() != null && busStep1.getWalk().getSteps().size() > 0){
//						checkBuslineToNextWalk(busStep,busStep1);
//					}
//					//假如两公交换乘之间没有步行，就把上一个bus的终点和下一个bus的起点链接
//					if (busStep.getBusLine() != null && busStep1.getWalk() == null &&busStep1.getBusLine() != null ){
//						checkBusEndToNextBusStart(busStep,busStep1);
//					}
//					//如果换乘没有步行 检查Bus最后一点和下一个step的bus起点是否一致
//					if (busStep.getBusLine() != null && busStep1.getWalk() == null && busStep1.getBusLine() != null){
//						checkBusToNextBusNoWalk(busStep,busStep1);
//					}
//					//检查换乘下一个Step是否有地铁  就将距离加到步行路线中
//					if (busStep.getBusLine() != null && busStep1.getRailway() != null){
//						checkBusLineToNextRailway(busStep,busStep1);
//					}
//					//检查下一个step的起始点是否有walk，若存在断点将其连接
//					if (busStep1.getWalk() != null && busStep1.getWalk().getSteps().size() > 0 && busStep.getRailway() == null){
//						checkRailwayToNextWalk(busStep,busStep1);
//					}
//					//检查两个railway之间是否有间隙，若有加到步行路线中
//					if (busStep1.getRailway() != null && busStep.getRailway() != null){
//						checkRailwayToNextRailway(busStep,busStep1);
//					}
//					//检查railway最后一点与下一个的起始点是否有 Taxi 若存在则加入到walk路线
//					if (busStep.getRailway() != null && busStep.getTaxi() != null){
//						checkRailwayToNextTaxi(busStep,busStep1);
//					}
//				}
//				//判断walk是否为空 不为空加入步行路线
//				if(busStep.getWalk() != null && busStep.getWalk().getSteps().size() > 0){
//					addWalkSteps(busStep);
//				}else{
//					//判断busline、railway、taxi是否为空 为空直接归入步行路线
//					if (busStep.getBusLine() == null && busStep.getRailway() == null && busStep.getTaxi() == null){
//						addWalkPolyline(latLng,endPoint);
//					}
//				}
//				//判断BusLine不为空并将busstationMarkers添加上去
//				if (busStep.getBusLine() != null){
//					RouteBusLineItem routeBusLineItem = busStep.getBusLine();
//					addBusLineSteps(routeBusLineItem);
//					addBusStationMarkers(routeBusLineItem);
//					if (i == busSteps.size() - 1){
//						addWalkPolyline(AMapServicesUtil.convertToLatLng(getLastBuslinePoint(busStep)),endPoint);
//					}
//				}
//				//判断如果railway不为空就去做railway
//				if(busStep.getRailway() != null){
//					addRailwayStep(busStep.getRailway());
//					addRailwayMarkers(busStep.getRailway());
//					if (i == busSteps.size() - 1){
//						addWalkPolyline(AMapServicesUtil.convertToLatLng(busStep.getRailway().getArrivalstop().getLocation()),endPoint);
//					}
//				}
//				//判断如果TAXI不为空就去做taxi
//				if (busStep.getTaxi() != null){
//					addTaxiStep(busStep.getTaxi());
//					addTaxiMarkers(busStep.getTaxi());
//				}
//			}
//			addStartAndEndMarker();
//
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//
//
//	}
//
//	/**
//	 * 检查步行    的最后一点   与    BUs的起点   是否一致
//	 * @param busStep
//	 */
//	private void checkWalkToBusline(BusStep busStep){
//		LatLonPoint walkListPoint = getLastWalkPoint(busStep);
//		LatLonPoint buslineFirstPoint = getFirstBuslinePoint(busStep);
//
//		if (!walkListPoint.equals(buslineFirstPoint)){
//			addWalkPolylineByLatLongPoints(walkListPoint,buslineFirstPoint);
//		}
//	}
//	/*获取步行的最后一点*/
//	private LatLonPoint getLastWalkPoint(BusStep busStep){
//		List<WalkStep> walkSteps = busStep.getWalk().getSteps();
//		WalkStep walkStep = walkSteps.get(walkSteps.size() - 1);
//		List<LatLonPoint> lonPoints = walkStep.getPolyline();
//		return lonPoints.get(lonPoints.size() - 1);
//	}
//	/*获取bus的起始点*/
//	private LatLonPoint getFirstBuslinePoint(BusStep busStep){
//		return busStep.getBusLine().getPolyline().get(0);
//	}
//
//	/**
//	 * 如果步行 与 bus 中间有空隙 则继续添加步行路线
//	 * @param pointFrom
//	 * @param pointTo
//	 */
//	private void addWalkPolylineByLatLongPoints(LatLonPoint pointFrom,LatLonPoint pointTo){
//		LatLng latLngFrom = AMapServicesUtil.convertToLatLng(pointFrom);
//		LatLng latLngTo = AMapServicesUtil.convertToLatLng(pointTo);
//
//		addWalkPolyline(latLngFrom,latLngTo);
//	}
//
//	/**
//	 * 步行路线添加方法
//	 * @param latLngFrom
//	 * @param latLngTo
//	 */
//	private void addWalkPolyline(LatLng latLngFrom,LatLng latLngTo){
//		addPolyLine(new PolylineOptions().add(latLngFrom,latLngTo)
//		.width(getRouteWidth()).color(getWalkColor()).setDottedLine(true));
//	}
//
//	/**/
//	private void addWalkPolyline(List<LatLng> listWalkPolyline){
//		addPolyLine(new PolylineOptions().addAll(listWalkPolyline).color(getWalkColor()).width(getRouteWidth()).setDottedLine(true));
//	}
//
//	/*检查bus的最后一点与下一个step的walk的起点是否一致就，如果不一致则加上步行*/
//	private void checkBuslineToNextWalk(BusStep busStep,BusStep busStep1){
//		LatLonPoint busLastPoint = getLastBuslinePoint(busStep);
//		LatLonPoint walFirstPoint = getFirstWalkPoint(busStep1);
//
//		if (!busLastPoint.equals(walFirstPoint)){
//			addWalkPolylineByLatLongPoints(busLastPoint,walFirstPoint);
//		}
//	}
//
//	/*获取bus的最后一步*/
//	private LatLonPoint getLastBuslinePoint(BusStep busStep){
//		List<LatLonPoint> lonPoints = busStep.getBusLine().getPolyline();
//		return lonPoints.get(lonPoints.size() - 1);
//	}
//	/*获取walk的第一步*/
//	private LatLonPoint getFirstWalkPoint(BusStep busStep1){
//		return busStep1.getWalk().getSteps().get(0).getPolyline().get(0);
//	}
//
//	/*检查两个bus换乘中间是否有间隙，如果有则加上步行*/
//	private void checkBusEndToNextBusStart(BusStep busStep,BusStep busStep1){
//		LatLonPoint busLastPoint = getLastBuslinePoint(busStep);
//		LatLng endBusLatLng = AMapServicesUtil.convertToLatLng(busLastPoint);
//		LatLonPoint nextBusFirstPoint = getFirstBuslinePoint(busStep1);
//		LatLng nextStartBusLatLng = AMapServicesUtil.convertToLatLng(nextBusFirstPoint);
//
//		if (!endBusLatLng.equals(nextStartBusLatLng)){
//			addWalkPolylineByLatLongPoints(busLastPoint,nextBusFirstPoint);//根据LatLngPoint来添加步行路线
//		}
//	}
//	/*如果换乘没有步行 检查Bus最后一点和下一个step的bus起点是否一致*/
//	private void checkBusToNextBusNoWalk(BusStep busStep,BusStep busStep1){
//		LatLng endbusLatLng = AMapServicesUtil.convertToLatLng(getLastBuslinePoint(busStep));
//		LatLng startbusLatLng = AMapServicesUtil.convertToLatLng(getFirstBuslinePoint(busStep1));
//		if (Math.abs(startbusLatLng.latitude - endbusLatLng.latitude) > 0.0001 || Math.abs(startbusLatLng.longitude - endbusLatLng.longitude) > 0.0001){
//			drawLineArrow(endbusLatLng,startbusLatLng);//断线用带箭头的直线连接
//		}
//	}
//
//	/*将两个点用直线连接起来*/
//	private void drawLineArrow(LatLng latLngFrom,LatLng latLngTo){
//		addPolyLine(new PolylineOptions().add(latLngFrom,latLngTo).width(3).color(getBusColor()).width(getRouteWidth()));
//	}
//
//	/*检查下一step是否Railyway，就将距离加到步行路线中*/
//	private void checkBusLineToNextRailway(BusStep busStep,BusStep busStep1){
//		LatLonPoint busLasPoint = getLastBuslinePoint(busStep);
//		LatLonPoint railwayFirstPoint = busStep1.getRailway().getDeparturestop().getLocation();//获取railyWay起始点
//		if (!busLasPoint.equals(railwayFirstPoint)){
//			addWalkPolylineByLatLongPoints(busLasPoint,railwayFirstPoint);//如果二者不一致，就将距离加到步行路线中
//		}
//	}
//
//	/*如果railway的最后一点与walk的第一点不符，链接断点*/
//	private void checkRailwayToNextWalk(BusStep busStep,BusStep busStep1){
//		LatLonPoint railwayLastPoint = busStep.getRailway().getArrivalstop().getLocation();
//		LatLonPoint walkFirstPoint = getFirstWalkPoint(busStep1);
//		if (!railwayLastPoint.equals(walkFirstPoint)){
//			addWalkPolylineByLatLongPoints(railwayLastPoint,walkFirstPoint);
//		}
//	}
//
//	/*检查两个railway之间是否有间隙，若有加到步行路线中*/
//	private void checkRailwayToNextRailway(BusStep busStep,BusStep busStep1){
//		LatLonPoint railwayLastPoint = busStep.getRailway().getArrivalstop().getLocation();
//		LatLonPoint railwayFirstPoint = busStep1.getRailway().getArrivalstop().getLocation();
//
//		if (!railwayLastPoint.equals(railwayFirstPoint)){
//			addWalkPolylineByLatLongPoints(railwayLastPoint,railwayFirstPoint);
//		}
//	}
//	/*检查railway最后一点与下一个的起始点是否有 Taxi 若存在则加入到walk路线*/
//	private void checkRailwayToNextTaxi(BusStep busStep,BusStep busStep1){
//		LatLonPoint railwayLastPoint = busStep.getRailway().getArrivalstop().getLocation();
//		LatLonPoint taxiFirstPoint = busStep1.getTaxi().getOrigin();
//		if (!railwayLastPoint.equals(taxiFirstPoint)){
//			addWalkPolylineByLatLongPoints(railwayLastPoint,taxiFirstPoint);
//		}
//	}
//
//	/**
//	 * 步行线路
//	 * @param busStep
//	 */
//	private void addWalkSteps(BusStep busStep){
//		RouteBusWalkItem routeBusWalkItem = busStep.getWalk();
//		List<WalkStep> walkSteps = routeBusWalkItem.getSteps();
//		for (int j = 0; j < walkSteps.size(); j++){
//			WalkStep walkStep = walkSteps.get(j);
//			if(j == 0){
//				LatLng latLng = AMapServicesUtil.convertToLatLng(walkStep.getPolyline().get(0));
//				String road = walkStep.getRoad();	//道路名字
//				String instruction = getWalkSnippet(walkSteps);	//获取walk导航信息
//				addWalkSattionMarkers(latLng,road,instruction);
//			}
//
//			List<LatLng> listWalkPolyline = AMapServicesUtil.convertArrList(walkStep.getPolyline());
//			this.latLng = listWalkPolyline.get(listWalkPolyline.size() - 1);
//
//			addWalkPolyline(listWalkPolyline);
//			//假如步行前一段的终点与后一步的起点有间隙，划线链接
//			if (j < walkSteps.size() - 1){
//				LatLng lastLatLng = listWalkPolyline.get(listWalkPolyline.size() -1);
//				LatLng firstLatLng = AMapServicesUtil.convertToLatLng(walkSteps.get(j+1).getPolyline().get(0));
//				if (!lastLatLng.equals(firstLatLng)){
//					addWalkPolyline(lastLatLng,firstLatLng);
//				}
//			}
//		}
//	}
//
//	/*步行导航信息*/
//	private String getWalkSnippet(List<WalkStep> walkSteps){
//		float disNum = 0;
//		for (WalkStep step : walkSteps){
//			disNum += step.getDistance();
//		}
//		return "\u6B65\u884C" + disNum + "\u7C73";
//	}
//	/*步行信息Markers*/
//	private void addWalkSattionMarkers(LatLng latLng,String title,String snippet){
//		addStationMarker(new MarkerOptions().position(latLng).title(title).snippet(snippet)
//							.anchor(0.5f,0.5f).visible(nodeIconVisible).icon(getWalkBitmapDescriptor()));
//	}
//
//	/**
//	 * 不同的busSteps
//	 * @param routeBusLineItem
//	 * @param
//	 */
//	private void addBusLineSteps(RouteBusLineItem routeBusLineItem){
//		addBusLineSteps(routeBusLineItem.getPolyline());
//	}
//	private void addBusLineSteps(List<LatLonPoint> listPoint){
//		if (listPoint.size() < 1){
//			return;
//		}
//		addPolyLine(new PolylineOptions().addAll(AMapServicesUtil.convertArrList(listPoint)).width(getRouteWidth()).color(getWalkColor()));
//	}
//
//	/**
//	 * 添加所有的BusMarkers
//	 * @param routeBusLineItem
//	 */
//	private void addBusStationMarkers(RouteBusLineItem routeBusLineItem){
//		BusStationItem startBusStation = routeBusLineItem.getDepartureBusStation();
//		LatLng position = AMapServicesUtil.convertToLatLng(startBusStation.getLatLonPoint());
//		String title = routeBusLineItem.getBusLineName();
//		String snippet = getBusSnippet(routeBusLineItem);
//
//		addBusStationMarker(new MarkerOptions().position(position).title(title).snippet(snippet).anchor(0.5f,0.5f).visible(nodeIconVisible).icon(getBusBitmapDescriptor()));
//	}
//	/**
//	 * 添加busStationMarker
//	 * @param options
//	 */
//	private void addBusStationMarker(MarkerOptions options){
//		if (options == null){
//			return;
//		}
//		Marker marker = mAMap.addMarker(options);
//		if (marker != null){
//			stationMarkers.add(marker);
//		}
//	}
//
//	/**
//	 * bus经过的BusStation信息
//	 * @param routeBusLineItem
//	 * @return
//	 */
//	private String getBusSnippet(RouteBusLineItem routeBusLineItem){
//		return "(" + routeBusLineItem.getDepartureBusStation().getBusStationName()+
//				"-->" +
//				routeBusLineItem.getArrivalBusStation().getBusStationName() + ") \u7ECF\u8FC7" +
//				(routeBusLineItem.getPassStationNum() + 1) + "\u7AD9";
//	}
//
//	/**
//	 * 获取railway的所有latlng
//	 * @param railway
//	 */
//	private void addRailwayStep(RouteRailwayItem railway){
//		List<LatLng> railwayListPoint = new ArrayList<LatLng>();
//		List<RailwayStationItem> railwayStationItems = new ArrayList<RailwayStationItem>();
//		railwayStationItems.add(railway.getDeparturestop());
//		railwayStationItems.addAll(railway.getViastops());
//		railwayStationItems.add(railway.getArrivalstop());
//		for (int i = 0; i < railwayStationItems.size(); i++){
//			railwayListPoint.add(AMapServicesUtil.convertToLatLng(railwayStationItems.get(i).getLocation()));
//		}
//		addRailwayPolyline(railwayListPoint);
//	}
//	/**/
//	private void addRailwayPolyline(List<LatLng> listPolyline) {
//		addPolyLine(new PolylineOptions().addAll(listPolyline).color(getDriveColor()).width(getRouteWidth()));
//	}
//
//	/**
//	 * 标注Railway的上下车Marker
//	 * @param railway
//	 */
//	private void addRailwayMarkers(RouteRailwayItem railway){
//		LatLng departurePosition = AMapServicesUtil.convertToLatLng(railway.getDeparturestop().getLocation());
//		String departureTitle = railway.getDeparturestop().getName() + "上车";
//		String depsrtureSnippet = railway.getName();
//
//		addStationMarker(new MarkerOptions().position(departurePosition).title(departureTitle).snippet(depsrtureSnippet)
//									.visible(nodeIconVisible).anchor(0.5f,0.5f).icon(getBusBitmapDescriptor()));
//
//		LatLng arrivePosition = AMapServicesUtil.convertToLatLng(railway.getArrivalstop().getLocation());
//		String arriveTitle = railway.getArrivalstop().getName() + "下车";
//		String arriveSnippet = railway.getName();
//
//		addStationMarker(new MarkerOptions().position(arrivePosition).title(arriveTitle).snippet(arriveSnippet)
//								.visible(nodeIconVisible).anchor(0.5f,0.5f).icon(getBusBitmapDescriptor()));
//	}
//
//	/**
//	 *
//	 * @param taxi
//	 */
//	private void addTaxiStep(TaxiItem taxi){
//		addPolyLine(new PolylineOptions().width(getRouteWidth()).color(getBusColor())
//							.add(AMapServicesUtil.convertToLatLng(taxi.getOrigin()))
//							.add(AMapServicesUtil.convertToLatLng(taxi.getDestination())));
//	}
//
//	/**
//	 * 打车并标记
//	 * @param taxiItem
//	 */
//	private void addTaxiMarkers(TaxiItem taxiItem){
//		LatLng position = AMapServicesUtil.convertToLatLng(taxiItem.getOrigin());
//		String title = taxiItem.getmSname() + "打车";
//		String snippet = "到终点";
//
//		addStationMarker(new MarkerOptions().title(title).snippet(snippet).visible(nodeIconVisible)
//								.anchor(0.5f,0.5f).icon(getBusBitmapDescriptor()));
//	}
//
//}


