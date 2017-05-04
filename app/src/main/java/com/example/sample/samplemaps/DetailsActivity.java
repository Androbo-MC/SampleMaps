package com.example.sample.samplemaps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class DetailsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        ArrayList<ArrayList<SearchResultModel>> resultList =
                (ArrayList<ArrayList<SearchResultModel>>)intent.getSerializableExtra("result");
        ArrayList<HashMap<String, String>> displayList = new ArrayList<>();

        Log.d("debug", "detail test");

        for (ArrayList<SearchResultModel> list : resultList) {

            HashMap<String, String> map = new HashMap<>();
            map.put("title", list.get(0).getStationNameTo());
            double aveTime = 0;

            for (SearchResultModel model : list) {

                if (model.getFastestTime().contains("時間")) {
                    // 1時間以上かかる場合の処理(あとでやる)
                }
                // "分"を削除して数値型にして足す
                aveTime += Integer.parseInt(model.getFastestTime().substring(0, model.getFastestTime().length()-1));
            }
            aveTime = aveTime / list.size();
            map.put("aveTime", Double.toString(aveTime));
            displayList.add(map);
            Log.d("debug", map.get("title"));
            Log.d("debug", map.get("aveTime"));
        }

        // ここからリストビュー表示の処理やる
    }
}