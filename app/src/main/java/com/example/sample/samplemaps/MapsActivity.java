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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.sample.samplemaps.R.id.map;

/**
 * マップアクティビティクラス
 *
 * 入力された駅名の座標位置と中間地点の位置を示す。
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener {

    private ArrayList<Double> latList = new ArrayList<>();
    private ArrayList<Double> lngList = new ArrayList<>();
    private ArrayList<LatLng> latLngList = new ArrayList<>();
    private ArrayList<String> inputStationList = new ArrayList<>();
    private ArrayList<String> resultStationList = new ArrayList<>();
    private ArrayList<String> selectedStationList = new ArrayList<>();
    private ArrayList<SearchResultModel> resultModelList = new ArrayList<>();
    private ArrayList<ArrayList<SearchResultModel>> resultList = new ArrayList<>();
    private LatLng centerLatLng = null;
    private ProgressDialog dialog = null;
    private int progressValue = 0;

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
        Double maxLat = null;
        Double minLat = null;
        Double maxLng = null;
        Double minLng = null;
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
            // 座標の合計・最大・最小を出す
            for (double lat : latList) {
                sumLat += lat;
                if (maxLat == null) {
                    maxLat = lat;
                } else {
                    maxLat = Math.max(maxLat, lat);
                }
                if (minLat == null) {
                    minLat = lat;
                } else {
                    minLat = Math.min(minLat, lat);
                }
            }
            for (double lng : lngList) {
                sumLng += lng;
                if (maxLng == null) {
                    maxLng = lng;
                } else {
                    maxLng = Math.max(maxLng, lng);
                }
                if (minLng == null) {
                    minLng = lng;
                } else {
                    minLng = Math.min(minLng, lng);
                }
            }
            // 合計から平均、最大・最少から最大距離を出す
            aveLat = sumLat / latList.size();
            aveLng = sumLng / lngList.size();
            centerLatLng = new LatLng(aveLat, aveLng);
            maxDistanceLat = maxLat - minLat;
            maxDistanceLng = maxLng - minLng;
            // 最大距離に応じてズーム具合を調整する
            int zoomLevel = 0;
            Log.d("debug", String.valueOf(maxDistanceLat));
            Log.d("debug", String.valueOf(maxDistanceLng));
            if (maxDistanceLat <= 0.03 && maxDistanceLng <= 0.03) {
                zoomLevel = 14;
            } else if (maxDistanceLat <= 0.06 && maxDistanceLng <= 0.06) {
                zoomLevel = 13;
            } else if (maxDistanceLat <= 0.1 && maxDistanceLng <= 0.1) {
                zoomLevel = 12;
            } else if (maxDistanceLat <= 0.2 && maxDistanceLng <= 0.2) {
                zoomLevel = 11;
            } else if (maxDistanceLat <= 0.4 && maxDistanceLng <= 0.4) {
                zoomLevel = 10;
            } else {
                zoomLevel = 9;
            }
            // 中間地点に色違いのピンをセットして情報ウインドウも表示
            Marker centerMarker = mMap.addMarker(new MarkerOptions().position(centerLatLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .title("中間地点！"));
            centerMarker.showInfoWindow();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, zoomLevel));    //2～21で大きいほどズーム
            // 情報ウインドウのリスナーをセット
            mMap.setOnInfoWindowClickListener(this);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        // バックボタンが押された時
        if (e.getAction() == KeyEvent.ACTION_UP && e.getKeyCode() == KeyEvent.KEYCODE_BACK) { //バックボタンが離された時
        }
        return super.dispatchKeyEvent(e);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        // 情報ウインドウがタップされた時の処理
        new AlertDialog.Builder(this)
                .setTitle("周辺駅検索")
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
        dialog.setCancelable(false);
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
/*                        if (resultStationList.size() >= 10) {
                            // とりあえず10駅までにしておく
                            break;
                        }*/
                    }
                } catch(JSONException e) {

                    e.printStackTrace();
                }
                // くるくるを消去
                dialog.dismiss();

                // 候補にする駅をチェックボックスで選ばせる
                final String[] chkItems = resultStationList.toArray(new String[0]);
                final boolean[] chkSts = new boolean[chkItems.length];  // 初期値でfalseが入ってる
                AlertDialog.Builder checkDlg = new AlertDialog.Builder(MapsActivity.this);
                checkDlg.setTitle("候補にする駅を選択");
                checkDlg.setMultiChoiceItems(
                        chkItems,
                        chkSts,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which, boolean flag) {
                                // 項目選択時の処理
                                // which は、選択されたアイテムのインデックス
                                // flag は、チェック状態
                                chkSts[which] = flag;
                            }
                        });
                checkDlg.setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int which) {
                                // OKボタンクリック処理
                                for (int i = 0; i < chkItems.length; i++) {
                                    // チェックが入っていた駅名を選択済リストに詰める
                                    if (chkSts[i]) {
                                        selectedStationList.add(chkItems[i]);
                                    }
                                }
                                if (selectedStationList.size() == 0) {
                                    // チェックが何もなかったら何もしない
                                    return;
                                }
                                // プログレスバーで進捗を表示
                                dialog = new ProgressDialog(MapsActivity.this);
                                dialog.setTitle("所要時間を検索中");
                                dialog.setMessage("しばらくお待ち下さい...");
                                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                dialog.setCancelable(false);
                                dialog.setMax(inputStationList.size() * selectedStationList.size());
                                dialog.setProgress(progressValue);
                                dialog.show();
                                // 入力した駅と中間地点周辺駅の所要時間等を取得する
                                for (String selectedStation : selectedStationList) {
                                    // Jorudan検索のために"駅"を削除
//                                        selectedStation = selectedStation.substring(0, selectedStation.length()-1);

                                    for (String inputStation : inputStationList) {
                                        // Jorudan検索のために"駅"を削除
//                                        inputStation = inputStation.substring(0, inputStation.length()-1);

                                        getStationDetail(inputStation, selectedStation);
                                    }
                                }
                            }
                        });
                // 表示
                checkDlg.create().show();
            }
        }.execute();

        // 再検索用にリストをクリア
        resultStationList.clear();
        selectedStationList.clear();
    }

    /**
     * 駅詳細情報取得
     *
     * 取得した駅名から詳細情報(所要時間等)を取得する。
     *
     * @param _inputStation 前画面で入力された駅名
     * @param _selectedStation チェックボックスで選択された駅名
     */
    private void getStationDetail(final String _inputStation, final String _selectedStation) {

        new AsyncTask<Void, Void, String>() {          //登録処理は非同期で

            @Override
            protected String doInBackground(Void... params) {

                String result = null;
                String inputStation = _inputStation;
                String selectedStation = _selectedStation;
                // 大手町駅が愛媛になってしまう事への対応
                if (inputStation.equals("大手町駅")) {
                    inputStation = "大手町";
                }
                if (selectedStation.equals("大手町駅")) {
                    selectedStation = "大手町";
                }
                try {
                    Request request = new Request.Builder()
//                            .url("http://www.jorudan.co.jp/norikae/route/"
//                                    + inputStation + "_" + selectedStation + ".html")
                            .url("http://www.jorudan.co.jp/norikae/cgi/nori.cgi?Sok=決+定&eki1="
                                    + inputStation + "&eki2=" + selectedStation)
                            .get()
                            .build();

                    OkHttpClient client = new OkHttpClient();

                    Response response = client.newCall(request).execute();
                    result = response.body().string();

                } catch (IOException e) {
                    e.printStackTrace();
                    dialog.dismiss();

                    new AlertDialog.Builder(MapsActivity.this)
                            .setTitle("通信に失敗しました。")
                            .setMessage("入力をやり直して下さい。")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setCancelable(false)
                            .show();
                }
                return result;
            }

            // 結果に応じた処理をUIスレッドで行う
            @Override
            protected void onPostExecute(String result) {

                Log.d("debug", _inputStation + _selectedStation);

                SearchResultModel model = new SearchResultModel();
                model.setStationNameFrom(_inputStation);
                model.setStationNameTo(_selectedStation);

                // 入力駅 == 候補駅だった時は所要時間0分
                if (_inputStation.equals(_selectedStation)) {
                    model.setFastestTime("0分");
                    model.setTransfer("乗換 0回");
                    model.setCost("0円");
                } else {
                    // 取得したwebページから必要な各情報を取得
                    Document doc = Jsoup.parse(result);
/*                   Elements routeList = doc.getElementById("Bk_list_tbody").children();
                for (Element route : routeList) {
                Log.d("debug", route.child(2).text());
                }*/
                    // 例外的に対応する必要のあるケースを先に処理
                    // ・近すぎる検索(例：有楽町～日比谷)
                    if (doc.getElementById("search_msg").text().equals("検索できない駅の指定です。（近距離です。）")) {
                        model.setFastestTime("0分");
                        model.setTransfer("乗換 0回");
                        model.setCost("0円");
                    } else {
                        model.setFastestTime(doc.getElementById("Bk_list_tbody").child(0).child(2).text());
                        model.setTransfer(doc.getElementById("Bk_list_tbody").child(0).child(3).text());
                        model.setCost(doc.getElementById("Bk_list_tbody").child(0).child(4).text());
                    }
                }
                resultModelList.add(model);

                Log.d("debug", resultModelList.get(resultModelList.size()-1).getStationNameFrom());
                Log.d("debug", resultModelList.get(resultModelList.size()-1).getStationNameTo());
                Log.d("debug", resultModelList.get(resultModelList.size()-1).getFastestTime());

                // プログレスバーを進める
                progressValue++;
                dialog.setProgress(progressValue);

                if (_inputStation.equals(inputStationList.get(inputStationList.size()-1))) {
                    // inputStationの区切りでいったん大元のリストに詰めてリセット
                    resultList.add(resultModelList);
                    resultModelList = new ArrayList<>();
                }
                // 全てのレスポンスが取得できたら画面遷移
                if (_inputStation.equals(inputStationList.get(inputStationList.size()-1))
                        && _selectedStation.equals(selectedStationList.get(selectedStationList.size()-1))) {

                    Intent intent = new Intent(MapsActivity.this, ListActivity.class)
                            .putExtra("result", resultList);
                    startActivity(intent);

                    Log.d("debug", "最後");
                    // 各値の初期化
                    resultList.clear();
                    resultModelList.clear();
                    progressValue = 0;
                    // くるくるを消去
                    dialog.dismiss();
                }
            }
        }.execute();
    }
}
