package com.example.sample.samplemaps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * リストアクティビティ
 *
 * 取得された所要時間等の詳細情報をリストビュー表示する。
 */
public class ListActivity extends Activity implements AdapterView.OnItemClickListener {

    private ArrayList<DisplayModel> displayList = new ArrayList<>();
    private ListView listview;
    private MyAdapter myadapter;
    public static int FVP = 0;
    public static int y = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Intent intent = getIntent();
        ArrayList<ArrayList<SearchResultModel>> resultList =
                (ArrayList<ArrayList<SearchResultModel>>)intent.getSerializableExtra("result");

        Log.d("debug", "detail test");

        for (ArrayList<SearchResultModel> list : resultList) {

            DisplayModel displayModel = new DisplayModel();
            displayModel.setTitle(list.get(0).getStationNameTo());
            double aveTime = 0;
            double aveTrans = 0;
            double aveCost = 0;

            for (SearchResultModel model : list) {

                if (model.getFastestTime().contains("時間")) {
                    // 1時間以上かかる場合の処理
                    aveTime += Integer.parseInt(model.getFastestTime().substring(0, 1)) * 60;
                    aveTime += Integer.parseInt(model.getFastestTime().substring(3, model.getFastestTime().length()-1));
                    Log.d("debug", Double.toString(aveTime));
                } else {
                    // "分"を削除して数値型にして足す
                    aveTime += Integer.parseInt(model.getFastestTime().substring(0, model.getFastestTime().length()-1));
                }
                // "乗換 ○回"の後ろから2文字目を数値型にして足す
                aveTrans += Integer.parseInt(model.getTransfer().substring(model.getTransfer().length()-2, model.getTransfer().length()-1));
                // "円"を削除して数値型にして足す
                if (model.getCost().contains(",")) {
                    // カンマがある(1000円以上)の場合は空文字置換してから処理
                    aveCost += Integer.parseInt(model.getCost().replaceAll(",", "")
                            .substring(0, model.getCost().length()-2));
                } else {
                    aveCost += Integer.parseInt(model.getCost().substring(0, model.getCost().length()-1));
                }
            }
            // 合計から平均値出す
            aveTime = aveTime / list.size();
            aveTrans = aveTrans / list.size();
            aveCost = aveCost / list.size();
            // 計算した平均と詳細リストをモデルにセット
            displayModel.setAveTime(aveTime);
            displayModel.setAveTrans(aveTrans);
            displayModel.setAveCost(aveCost);
            displayModel.setDetailList(list);
            // モデルをリストに追加
            displayList.add(displayModel);
        }
        // 所要時間でソート
        Collections.sort(displayList, new Comparator<DisplayModel>() {
            @Override
            public int compare(DisplayModel model1, DisplayModel model2) {

                return Double.compare(model1.getAveTime(), model2.getAveTime());
            }
        });
        for (DisplayModel model : displayList) {

            Log.d("debug", model.getTitle());
            Log.d("debug", Double.toString(model.getAveTime()));
        }
        // ここからリストビュー表示の処理やる
        //AdapterでListの配列をListViewへ
        listview = (ListView) findViewById(R.id.ListView);
        myadapter = new MyAdapter(this, displayList);
        listview.setAdapter(myadapter);
        listview.setSelectionFromTop(FVP, y);       // 記憶してあったリストの位置を取得
        listview.setOnItemClickListener(this);      // ListViewに対してはOnClickListenerじゃなくてこれを使う
    }

    // リストがクリックされた時の処理
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

        // リストの現在位置を取得(初期位置は両方0)
        FVP = listview.getFirstVisiblePosition();
        y = listview.getChildAt(0).getTop();

        // 詳細画面への遷移
        Intent intent = new Intent(this, DetailActivity.class)
                .putExtra("displayModel", displayList.get(position));
        startActivity(intent);
    }
}