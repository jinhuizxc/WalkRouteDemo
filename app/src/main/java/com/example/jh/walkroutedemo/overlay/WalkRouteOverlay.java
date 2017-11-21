package com.example.jh.walkroutedemo.overlay;

import android.content.Context;
import android.util.Log;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkStep;
import com.example.jh.walkroutedemo.R;
import com.example.jh.walkroutedemo.utils.AMapServicesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 步行路线图层类。在高德地图API里，如果要显示步行路线规划，可以用此类来创建步行路线图层。如不满足需求，也可以自己创建自定义的步行路线图层。
 * @since V2.1.0
 */
public class WalkRouteOverlay extends RouteOverlay {

	private static final String TAG = WalkRouteOverlay.class.getSimpleName();
	private PolylineOptions mPolylineOptions;

    private BitmapDescriptor walkStationDescriptor= null;

    private WalkPath walkPath;
	/**
	 * 通过此构造函数创建步行路线图层。
	 * @param context 当前activity。
	 * @param amap 地图对象。
	 * @param path 步行路线规划的一个方案。详见搜索服务模块的路径查询包（com.amap.api.services.route）中的类 <strong><a href="../../../../../../Search/com/amap/api/services/route/WalkStep.html" title="com.amap.api.services.route中的类">WalkStep</a></strong>。
	 * @param start 起点。详见搜索服务模块的核心基础包（com.amap.api.services.core）中的类<strong><a href="../../../../../../Search/com/amap/api/services/core/LatLonPoint.html" title="com.amap.api.services.core中的类">LatLonPoint</a></strong>。
	 * @param end 终点。详见搜索服务模块的核心基础包（com.amap.api.services.core）中的类<strong><a href="../../../../../../Search/com/amap/api/services/core/LatLonPoint.html" title="com.amap.api.services.core中的类">LatLonPoint</a></strong>。
	 * @since V2.1.0
	 */
	public WalkRouteOverlay(Context context, AMap amap, WalkPath path,
                            LatLonPoint start, LatLonPoint end) {
		super(context);
		this.mAMap = amap;
		this.walkPath = path;
		startPoint = AMapServicesUtil.convertToLatLng(start);
		endPoint = AMapServicesUtil.convertToLatLng(end);
	}
	/**
	 * 添加步行路线到地图中。
	 * @since V2.1.0
	 */
    public void addToMap() {

        initPolylineOptions();
        try {
            List<WalkStep> walkPaths = walkPath.getSteps();
            mPolylineOptions.add(startPoint);
			Log.e(TAG, "startPoint =" + startPoint);
            for (int i = 0; i < walkPaths.size(); i++) {
                WalkStep walkStep = walkPaths.get(i);
				Log.e(TAG, "walkStep =" + walkStep);
                LatLng latLng = AMapServicesUtil.convertToLatLng(walkStep
                        .getPolyline().get(0));
				Log.e(TAG, "步行经纬度 =" + latLng);

				// 加载步行路线marker
				addWalkStationMarkers(walkStep, latLng);
                addWalkPolyLines(walkStep);

            }
            mPolylineOptions.add(endPoint);
			// 添加开始与终点的marker
            addStartAndEndMarker();
			// 显示结果
            showPolyline();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected void addStartAndEndMarker() {
        startMarker = mAMap.addMarker((new MarkerOptions())
                .position(startPoint).icon(getStartBitmapDescriptor())
                .title("\u8D77\u70B9"));
        // startMarker.showInfoWindow();

        endMarker = mAMap.addMarker((new MarkerOptions()).position(endPoint)
                .icon(getEndBitmapDescriptor()).title("\u7EC8\u70B9"));
        // mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint,
        // getShowRouteZoom()));
    }




    /**
	 * 检查这一步的最后一点和下一步的起始点之间是否存在空隙
	 */
	private void checkDistanceToNextStep(WalkStep walkStep,
			WalkStep walkStep1) {
		LatLonPoint lastPoint = getLastWalkPoint(walkStep);
		LatLonPoint nextFirstPoint = getFirstWalkPoint(walkStep1);
		if (!(lastPoint.equals(nextFirstPoint))) {
			addWalkPolyLine(lastPoint, nextFirstPoint);
		}
	}

	/**
	 * @param walkStep
	 * @return
	 */
	private LatLonPoint getLastWalkPoint(WalkStep walkStep) {
		return walkStep.getPolyline().get(walkStep.getPolyline().size() - 1);
	}

	/**
	 * @param walkStep
	 * @return
	 */
	private LatLonPoint getFirstWalkPoint(WalkStep walkStep) {
		return walkStep.getPolyline().get(0);
	}


    private void addWalkPolyLine(LatLonPoint pointFrom, LatLonPoint pointTo) {
        addWalkPolyLine(AMapServicesUtil.convertToLatLng(pointFrom), AMapServicesUtil.convertToLatLng(pointTo));
    }

    private void addWalkPolyLine(LatLng latLngFrom, LatLng latLngTo) {
        mPolylineOptions.add(latLngFrom, latLngTo);
    }

    /**
	 * 添加所有的点组成步行路径
     * @param walkStep
     */
    private void addWalkPolyLines(WalkStep walkStep) {
		// 加入地图纹理
		//用一个数组来存放纹理
		List<BitmapDescriptor> texTuresList = new ArrayList<BitmapDescriptor>();
		texTuresList.add(BitmapDescriptorFactory.fromResource(R.drawable.map_alr));
//                texTuresList.add(BitmapDescriptorFactory.fromResource(R.drawable.map_custtexture));
//                texTuresList.add(BitmapDescriptorFactory.fromResource(R.drawable.map_alr_night));
		//指定某一段用某个纹理，对应texTuresList的index即可, 四个点对应三段颜色
		List<Integer> texIndexList = new ArrayList<Integer>();
		texIndexList.add(0);//对应上面的第0个纹理

        mPolylineOptions.addAll(convertArrList(walkStep.getPolyline()));

		//加入对应的颜色,使用setCustomTextureList 即表示使用多纹理；
		mPolylineOptions.setCustomTextureList(texTuresList);
		//设置纹理对应的Index
		mPolylineOptions.setCustomTextureIndex(texIndexList);

    }

    public static ArrayList<LatLng> convertArrList(List<LatLonPoint> shapes) {
        ArrayList<LatLng> lineShapes = new ArrayList<LatLng>();
        for (LatLonPoint point : shapes) {
            LatLng latLngTemp = AMapServicesUtil.convertToLatLng(point);
            lineShapes.add(latLngTemp);
        }
        return lineShapes;
    }



    /**
     * @param walkStep
     * @param position
     */
    private void addWalkStationMarkers(WalkStep walkStep, LatLng position) {
        addStationMarker(new MarkerOptions()
                .position(position)
                .title("\u65B9\u5411:" + walkStep.getAction()
                        + "\n\u9053\u8DEF:" + walkStep.getRoad())
                .snippet(walkStep.getInstruction()).visible(nodeIconVisible)
                .anchor(0.5f, 0.5f).icon(walkStationDescriptor));
    }

    /**
     * 初始化线段属性
     */
    private void initPolylineOptions() {

        if(walkStationDescriptor == null) {
            walkStationDescriptor = getWalkBitmapDescriptor();
        }

        mPolylineOptions = null;

        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(getWalkColor()).width(getRouteWidth());
    }


    private void showPolyline() {
        addPolyLine(mPolylineOptions);
    }
}
