package com.example.jh.walkroutedemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.example.jh.walkroutedemo.overlay.WalkRouteOverlay;
import com.example.jh.walkroutedemo.utils.AMapServicesUtil;
import com.example.jh.walkroutedemo.utils.ChString;
import com.example.jh.walkroutedemo.utils.ToastUtil;

import java.text.DecimalFormat;

/**
 * 学习下步行轨迹绘制并能够进行更改定制
 * 分下还有什么因素影响！
 */
public class WalkRouteActivity extends AppCompatActivity implements RouteSearch.OnRouteSearchListener, AMap.OnMapClickListener, AMap.OnMarkerClickListener, AMap.OnInfoWindowClickListener, AMap.InfoWindowAdapter {

    private static final String TAG = WalkRouteActivity.class.getSimpleName();
    private AMap aMap;
    private MapView mapView;
    private Context mContext;
    private RouteSearch mRouteSearch;
    private WalkRouteResult mWalkRouteResult;
    private LatLonPoint mStartPoint = new LatLonPoint(39.942295, 116.335891);//起点，116.335891,39.942295
    private LatLonPoint mEndPoint = new LatLonPoint(39.995576, 116.481288);//终点，116.481288,39.995576
    private final int ROUTE_TYPE_WALK = 3;

    private RelativeLayout mBottomLayout;
    private TextView mRotueTimeDes, mRouteDetailDes;
    private ProgressDialog progDialog = null;// 搜索时进度条

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_activity);

        mContext = this.getApplicationContext();
        mapView = (MapView) findViewById(R.id.route_map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
        setfromandtoMarker();
        // 设置路线
        searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
    }

    /**
     * 开始搜索路径规划方案
     */
    private void searchRouteResult(int routeType, int mode) {
        if (mStartPoint == null) {
            Toast.makeText(mContext, "定位中，稍后再试...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mEndPoint == null) {
            Toast.makeText(mContext, "终点未设置", Toast.LENGTH_SHORT).show();
        }
        showProgressDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                mStartPoint, mEndPoint);
        if (routeType == ROUTE_TYPE_WALK) { // 步行路径规划
            RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, mode);
            mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
        }
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    private void setfromandtoMarker() {
        aMap.addMarker(new MarkerOptions()
                .position(AMapServicesUtil.convertToLatLng(mStartPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
        aMap.addMarker(new MarkerOptions()
                .position(AMapServicesUtil.convertToLatLng(mEndPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
    }




    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        // 注册监听
        registerListener();
        // 初始化、创建路线的监听，重写4个方法
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);

        mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
        mRotueTimeDes = (TextView) findViewById(R.id.firstline);
        mRouteDetailDes = (TextView) findViewById(R.id.secondline);
    }

    /**
     * 注册监听
     */
    private void registerListener() {
        aMap.setOnMapClickListener(WalkRouteActivity.this);
        aMap.setOnMarkerClickListener(WalkRouteActivity.this);
        aMap.setOnInfoWindowClickListener(WalkRouteActivity.this);
        aMap.setInfoWindowAdapter(WalkRouteActivity.this);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
        Log.e(TAG, "onWalkRouteSearched 方法执行");
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if(errorCode == AMapException.CODE_AMAP_SUCCESS){
            if(result != null && result.getPaths() != null){
                if(result.getPaths().size() > 0){
                    mWalkRouteResult = result;
                    Log.e(TAG, "result =" + result);
                    final WalkPath walkPath = mWalkRouteResult.getPaths().get(0);
                    WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(this, aMap, walkPath,
                            mWalkRouteResult.getStartPos(),
                            mWalkRouteResult.getTargetPos());
                    Log.e(TAG, "起点、终点 =" +  mWalkRouteResult.getStartPos() + mWalkRouteResult.getTargetPos());
                    walkRouteOverlay.removeFromMap();
                    // 加载marker
                    walkRouteOverlay.addToMap();
                    // 移动镜头到当前的视角
                    walkRouteOverlay.zoomToSpan();
                    mBottomLayout.setVisibility(View.VISIBLE);
                    int dis = (int) walkPath.getDistance();
                    int dur = (int) walkPath.getDuration();
                    String des = getFriendlyTime(dur)+"("+ getFriendlyLength(dis)+")";
                    mRotueTimeDes.setText(des);
                    mRouteDetailDes.setVisibility(View.GONE);
                    mBottomLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            细节部分暂时不考虑
//                            Intent intent = new Intent(mContext,
//                                    WalkRouteDetailActivity.class);
//                            intent.putExtra("walk_path", walkPath);
//                            intent.putExtra("walk_result",
//                                    mWalkRouteResult);
//                            startActivity(intent);
                        }
                    });
                }else if(result != null && result.getPaths() == null){
                    ToastUtil.show(mContext, R.string.no_result);
                }
            }else {
                ToastUtil.show(mContext, R.string.no_result);
            }
        }else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }

    public static String getFriendlyLength(int lenMeter) {
        if (lenMeter > 10000) // 10 km
        {
            int dis = lenMeter / 1000;
            return dis + ChString.Kilometer;
        }

        if (lenMeter > 1000) {
            float dis = (float) lenMeter / 1000;
            DecimalFormat fnum = new DecimalFormat("##0.0");
            String dstr = fnum.format(dis);
            return dstr + ChString.Kilometer;
        }

        if (lenMeter > 100) {
            int dis = lenMeter / 50 * 50;
            return dis + ChString.Meter;
        }

        int dis = lenMeter / 10 * 10;
        if (dis == 0) {
            dis = 10;
        }

        return dis + ChString.Meter;
    }

    public static String getFriendlyTime(int second) {
        if (second > 3600) {
            int hour = second / 3600;
            int miniate = (second % 3600) / 60;
            return hour + "小时" + miniate + "分钟";
        }
        if (second >= 60) {
            int miniate = second / 60;
            return miniate + "分钟";
        }
        return second + "秒";
    }


    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

    /**
     * 地图点击事件
     *
     * @param latLng
     */
    @Override
    public void onMapClick(LatLng latLng) {
        Log.e(TAG, "onMapClick 方法执行");
    }


    /**
     * InfoWindow的点击事件
     *
     * @param marker
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.e(TAG, "onInfoWindowClick 方法执行");
    }


    /**
     * setInfoWindowAdapter 的两个方法
     *
     * @param marker
     * @return
     */
    @Override
    public View getInfoWindow(Marker marker) {
        Log.e(TAG, "getInfoWindow 方法执行");
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        Log.e(TAG, "getInfoContents 方法执行");
        return null;
    }

    /**
     * marker的点击监听事件
     *
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.e(TAG, "onMarkerClick 方法执行");
        return false;
    }
}
