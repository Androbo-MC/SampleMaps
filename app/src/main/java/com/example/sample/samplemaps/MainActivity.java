package com.example.sample.samplemaps;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * メインアクティビティ
 *
 * 内部的にはマップが先に呼ばれるが、実質はここが初期画面。
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private ArrayList<String> latList = new ArrayList<>();
    private ArrayList<String> lngList = new ArrayList<>();
    private ArrayList<EditText> textBoxList = new ArrayList<>();
    private ArrayList<String> inputStationList = new ArrayList<>();
    private int counter = 0;
    private int viewCounter = 1;
    private ProgressDialog dialog = null;
    private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // カウンター初期化
        counter = 0;
        viewCounter = 1;
        // 入力されたテキストボックスを取得
        textBoxList.add((EditText) findViewById(R.id.edit_text1));

        // buttonを取得
        Button btn = (Button)findViewById(R.id.button1);
        btn.setOnClickListener(this);
    }

    // テキストエリア追加用の＋ボタンが押されたら呼ばれる
    public void addTextView(View v) {
        // ビューの親となるレイアウトを取得
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_main_layout);
        // ひとつ前のテキストエリアからヒントを削除
        textBoxList.get(textBoxList.size()-1).setHint("");
        // テキストエリアを動的に生成
        EditText editText = new EditText(this);
        editText.setHint("駅名を入力");
        editText.setEms(10);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setId(viewCounter);
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(WC, WC);
        // 1回目だけは静的なIDを参照する。
        if (viewCounter == 1) {
            param.addRule(RelativeLayout.BELOW, getResources().getIdentifier(
                    "edit_text" + Integer.toString(textBoxList.size()), "id", getPackageName()));
            param.addRule(RelativeLayout.ALIGN_LEFT, getResources().getIdentifier(
                    "edit_text" + Integer.toString(textBoxList.size()), "id", getPackageName()));
        } else {
            // こいつの第二引数が0だとルール削除の意味になるようなので、カウンターは1から開始
            param.addRule(RelativeLayout.BELOW, viewCounter-1);
            param.addRule(RelativeLayout.ALIGN_LEFT, viewCounter-1);
        }
        layout.addView(editText, param);
        textBoxList.add(editText);
        viewCounter++;
    }

    public void onClick(View v) {

        // 文字列のリストに詰める
        for (EditText text : textBoxList) {
            // 空のテキストエリアは含まない
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

    /**
     * 位置情報取得
     *
     * 指定された駅の数だけリクエスト投げて場所を特定する。
     *
     * @param searchText テキストボックスに入力された駅名
     */
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
