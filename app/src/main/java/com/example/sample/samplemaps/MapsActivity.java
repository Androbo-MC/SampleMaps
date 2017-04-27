package com.example.sample.samplemaps;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import static com.example.sample.samplemaps.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private List<Double> latList = new ArrayList<>();
    private List<Double> lngList = new ArrayList<>();
    private List<LatLng> latLngList = new ArrayList<>();
    private int counter = 0;
    private LatLng centerLatLng = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();

        // 起動時にMainActivityから表示させるための遷移
        // (なぜか向こうをデフォルトにすると落ちる)
        if (null == intent.getExtras()) {

            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {

            // Mainから遷移してきたら緯度経度のリストをStringからDoubleに戻す
            List<String> stringLatList = intent.getStringArrayListExtra("latitude");
            for (String lat : stringLatList) {

                latList.add(Double.parseDouble(lat));
        }
            List<String> stringLngList = intent.getStringArrayListExtra("longitude");
            for (String lng : stringLngList) {

                lngList.add(Double.parseDouble(lng));
            }
            counter = intent.getIntExtra("counter", 0);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        GoogleMap mMap = googleMap;
        double sumLat = 0;
        double sumLng = 0;
        double aveLat = 0;
        double aveLng = 0;
        double maxDistanceLat = 0;
        double maxDistanceLng = 0;

//        LatLng sydney = new LatLng(-34, 151);
//        LatLng sapporo = new LatLng(43.0675, 141.350784);

        if (!latList.isEmpty() && !lngList.isEmpty()) {

            for (int i = 0; i < counter; i++) {
                // 取得した座標の数だけピンをセットする
                latLngList.add(new LatLng(latList.get(i), lngList.get(i)));
                mMap.addMarker(new MarkerOptions().position(latLngList.get(i)));
            }
            // 座標の中間と最大距離を出す
            for (double lat : latList) {
                sumLat += lat;
                if (maxDistanceLat < lat) {
                    maxDistanceLat = lat;
                }
            }
            aveLat = sumLat / counter;
            for (double lng : lngList) {
                sumLng += lng;
                if (maxDistanceLng < lng) {
                    maxDistanceLng = lng;
                }
            }
            aveLng = sumLng / counter;
            centerLatLng = new LatLng(aveLat, aveLng);
            // 中間地点に色違いのピンをセットして情報ウインドウも表示
            Marker centerMarker = mMap.addMarker(new MarkerOptions().position(centerLatLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .title("Center HERE!"));
            centerMarker.showInfoWindow();
            // maxDistanceに応じてズーム具合を変える予定
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, 12));    //2～21で大きいほどズーム
            // 情報ウインドウのリスナーをセット
            mMap.setOnInfoWindowClickListener(this);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        // バックボタンが押されたら座標の各数値を初期化
        if (e.getAction() == KeyEvent.ACTION_UP && e.getKeyCode() == KeyEvent.KEYCODE_BACK) { //バックボタンが離された時

            latList.clear();
            lngList.clear();
            latLngList.clear();
            counter = 0;
            centerLatLng = null;
        }
        return super.dispatchKeyEvent(e);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        // 情報ウインドウがタップされた時の処理
        new AlertDialog.Builder(this)
                .setTitle("周辺施設検索")
                .setMessage("この地点の周辺施設を表示しますか？")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                                Toast.makeText(MapsActivity.this, "作成中",
                                        Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 何もしない
                    }
                })
                .show();
    }
}
