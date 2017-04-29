package com.example.sample.samplemaps;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.sample.samplemaps.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private List<Double> latList = new ArrayList<>();
    private List<Double> lngList = new ArrayList<>();
    private List<LatLng> latLngList = new ArrayList<>();
    private List<String> inputStationList = new ArrayList<>();
    private List<String> resultStationList = new ArrayList<>();
    private ArrayList<SearchResultModel> resultModelList = new ArrayList<>();
    private LatLng centerLatLng = null;
    private ProgressDialog dialog = null;

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
        if (null == intent.getStringArrayListExtra("latitude")
                || null == intent.getStringArrayListExtra("longitude")) {

            intent = new Intent(this, MainActivity.class);
            startActivityForResult(intent, 1);
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
            inputStationList = intent.getStringArrayListExtra(("inputStationList"));

        }
    }

    public void onActivityResult( int requestCode, int resultCode, Intent intent ) {
        // メイン画面からバックボタンで戻ってきた時はアプリ終了
        if (requestCode == 1) {

            this.finish();
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

            for (int i = 0; i < latList.size(); i++) {
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
            aveLat = sumLat / latList.size();
            for (double lng : lngList) {
                sumLng += lng;
                if (maxDistanceLng < lng) {
                    maxDistanceLng = lng;
                }
            }
            aveLng = sumLng / lngList.size();
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
            centerLatLng = null;
        }
        return super.dispatchKeyEvent(e);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        // 情報ウインドウがタップされた時の処理
        new AlertDialog.Builder(this)
                .setTitle("周辺施設検索")
                .setMessage("この地点の周辺駅情報を表示しますか？")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        getStationInfo(centerLatLng);
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

    // 中間地点から近い駅の情報を取得
    private void getStationInfo(final LatLng centerLatLng) {

        // リクエストを投げる時にくるくるを表示
        dialog = new ProgressDialog(MapsActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("検索中");
        dialog.show();

        new AsyncTask<Void, Void, String>() {          //登録処理は非同期で
            @Override
            protected String doInBackground(Void... params) {

                String result = null;

                try {
                    Request request = new Request.Builder()
                            .url("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                                    String.valueOf(centerLatLng.latitude) + "," +
                                    String.valueOf(centerLatLng.longitude) +
                                    "&rankby=distance&type=train_station&language=ja&key=AIzaSyCnmpmaLnH9WQxkxDqF1NtDHwlkfr1ocqM")
                            .get()
                            .build();

                    OkHttpClient client = new OkHttpClient();

                    Response response = client.newCall(request).execute();
                    result = response.body().string();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            // 結果に応じた処理をUIスレッドで行う
            @Override
            protected void onPostExecute(String result) {



                try {
                    // 取得したJSONから必要な部分だけを抜き取る
                    JSONArray jsonArray = new JSONObject(result).getJSONArray("results");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String station = jsonArray.getJSONObject(i).getString("name");
                        // "○○駅"ではない検索結果を除外
                        if (!station.substring(station.length()-1).equals("駅")) {
                            continue;
                        }
                        // Jorudan検索のために"駅"を削除
                        // → 駅付きでも検索できた(あとこれだとinputStationListの方は駅消せてないし)
//                        station = station.substring(0, station.length()-1);
                        // 中間地点に近い順の駅名をリストに詰める
                        resultStationList.add(station);
                        Log.d("debug", station);
                        if (resultStationList.size() >= 10) {
                            // とりあえず10駅までにしておく
                            break;
                        }
                    }
                } catch(JSONException e) {

                    e.printStackTrace();
                }

                // 入力した駅と中間地点周辺駅の所要時間等を取得する
                for (String inputStation : inputStationList) {

                    for (String resultStation : resultStationList) {

                        getStationDetail(inputStation, resultStation);
                    }
                }
//                getStationDetail("新橋", "品川");
            }
        }.execute();
    }

    // 取得した駅名から詳細情報(所要時間等)を取得
    private void getStationDetail(final String inputStation, final String resultStation) {

        new AsyncTask<Void, Void, String>() {          //登録処理は非同期で
            @Override
            protected String doInBackground(Void... params) {

                String result = null;

                try {
                    Request request = new Request.Builder()
                            .url("http://www.jorudan.co.jp/norikae/route/"
                                    + inputStation + "_" + resultStation + ".html")
                            .get()
                            .build();

                    OkHttpClient client = new OkHttpClient();

                    Response response = client.newCall(request).execute();
                    result = response.body().string();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            // 結果に応じた処理をUIスレッドで行う
            @Override
            protected void onPostExecute(String result) {

                Log.d("debug", inputStation + resultStation);

                SearchResultModel model = new SearchResultModel();
                model.setStationNameFrom(inputStation);
                model.setStationNameTo(resultStation);

                // 入力駅 == 候補駅だった時は所要時間0分
                if (inputStation.equals(resultStation)) {
                    model.setFastestTime("0分");
                } else {
                    // 取得したwebページから必要な所要時間情報を取得
                    Document doc = Jsoup.parse(result);
/*                   Elements routeList = doc.getElementById("Bk_list_tbody").children();
                    for (Element route : routeList) {
                    Log.d("debug", route.child(2).text());
                    }*/
                    model.setFastestTime(doc.getElementById("Bk_list_tbody").child(0).child(2).text());
                }

                resultModelList.add(model);

                Log.d("debug", resultModelList.get(resultModelList.size()-1).getStationNameFrom());
                Log.d("debug", resultModelList.get(resultModelList.size()-1).getStationNameTo());
                Log.d("debug", resultModelList.get(resultModelList.size()-1).getFastestTime());

                // 全てのレスポンスが取得できたら画面遷移(これからやる)
                if (inputStation.equals(inputStationList.get(inputStationList.size()-1))
                        && resultStation.equals(resultStationList.get(resultStationList.size()-1))) {

                    Log.d("debug", "最後");
                    // くるくるを消去
                    dialog.dismiss();
                }
            }
        }.execute();
    }
}
