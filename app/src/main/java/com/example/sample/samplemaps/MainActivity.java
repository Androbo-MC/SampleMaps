package com.example.sample.samplemaps;

import android.app.Activity;
import android.app.ProgressDialog;
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
    private List<EditText> textBoxList = new ArrayList<>();
    private List<String> textList = new ArrayList<>();
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

            textList.add(text.getText().toString());
        }
        // テキストが入力されていた数だけリクエストを投げる
        for (String text : textList) {
            if (null != text && !text.isEmpty()) {

                counter++;
                getPlaceInfo(text);
            }
        }
    }

    private void getPlaceInfo(final String searchText) {

        // 1つめのリクエストを投げる時にくるくるを表示
        if (counter == 1) {

            dialog = new ProgressDialog(MainActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("検索中");
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
                }

                // 全てのレスポンスが取得できたらMAPへ画面遷移
                if (counter == latList.size() && counter == lngList.size()){

                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    // 取得した緯度経度を渡す
                    intent.putStringArrayListExtra("latitude", latList);
                    intent.putStringArrayListExtra("longitude", lngList);
                    intent.putExtra("counter", counter);
                    startActivity(intent);
                    // 各リストを初期化
                    latList.clear();
                    lngList.clear();
                    textList.clear();
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
        // バックボタンが押されたらアプリ終了
        if (e.getAction() == KeyEvent.ACTION_UP && e.getKeyCode() == KeyEvent.KEYCODE_BACK) { //バックボタンが離された時

            finish();   // でもこれで終了しないぽい
        }
        return super.dispatchKeyEvent(e);
    }
}
