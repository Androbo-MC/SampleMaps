package com.example.sample.samplemaps;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private ArrayList<String> latList = new ArrayList<>();
    private ArrayList<String> lngList = new ArrayList<>();
    private ArrayList<EditText> textBoxList = new ArrayList<>();
    private ArrayList<String> inputStationList = new ArrayList<>();
    private int counter = 0;
    private ProgressDialog dialog = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // buttonを取得
        Button btn = (Button)findViewById(R.id.Button01);
        btn.setOnClickListener(this);
    }

    public void onClick(View v) {

        // 入力されたテキストボックスを取得
        textBoxList.add((EditText) findViewById(R.id.editText1));
        textBoxList.add((EditText) findViewById(R.id.editText2));
        textBoxList.add((EditText) findViewById(R.id.editText3));
        textBoxList.add((EditText) findViewById(R.id.editText4));
        textBoxList.add((EditText) findViewById(R.id.editText5));
        // 文字列のリストに詰める
        for (EditText text : textBoxList) {

            if (!text.getText().toString().isEmpty()) {

                inputStationList.add(text.getText().toString());
            }
        }
        // テキストが入力されていた数だけリクエストを投げる
        for (String text : inputStationList) {
            if (null != text && !text.isEmpty()) {

                counter++;
                getPlaceInfo(text);
            }
        }
    }

    // 指定された駅の数だけリクエスト投げて場所を特定
    private void getPlaceInfo(final String searchText) {

        // 1つめのリクエストを投げる時にくるくるを表示
        if (counter == 1) {

            dialog = new ProgressDialog(MainActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("検索中");
            dialog.setCancelable(false);
            dialog.show();
        }
        new AsyncTask<Void, Void, String>() {          //登録処理は非同期で
            @Override
            protected String doInBackground(Void... params) {

                String result = null;

                try {
                    Request request = new Request.Builder()
                            .url("https://maps.googleapis.com/maps/api/geocode/json?address=" + searchText
                                    + "&key=AIzaSyB8HkNuPwjMz3Ec0YPO66xZ6Srr8Zhshn0")
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
                    JSONObject jsonObj = new JSONObject(result).getJSONArray("results")
                            .getJSONObject(0).getJSONObject("geometry").getJSONObject("location");

                    // DoubleのリストだとIntentで送りにくいのでStringに変換
                    latList.add(String.valueOf(jsonObj.getDouble("lat")));
                    lngList.add(String.valueOf(jsonObj.getDouble("lng")));

                } catch(JSONException e) {

                    e.printStackTrace();
                    dialog.dismiss();

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("検索結果が取得できません。")
                            .setMessage("入力をやり直して下さい。")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    // ほんとは終了じゃなくて再描画的にしたい
                                    finish();
                                }
                            })
                            .setCancelable(false)
                            .show();
                }

                // 全てのレスポンスが取得できたらMAPへ画面遷移
                if (counter == latList.size() && counter == lngList.size()){
                    // 取得した緯度経度を渡す
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class)
                            .putStringArrayListExtra("latitude", latList)
                            .putStringArrayListExtra("longitude", lngList)
                            .putStringArrayListExtra("inputStationList", inputStationList);
                    startActivity(intent);
                    // 各リストを初期化
                    latList.clear();
                    lngList.clear();
                    inputStationList.clear();
                    textBoxList.clear();
                    counter = 0;
                    // くるくるを消去
                    dialog.dismiss();
                }
            }
        }.execute();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        // バックボタンが離された時
        if (e.getAction() == KeyEvent.ACTION_UP && e.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            // このアクティビティの終了(MAPへ戻る)
//            finish(); //finishしなくても終わるぽい
        }
        return super.dispatchKeyEvent(e);
    }
}
