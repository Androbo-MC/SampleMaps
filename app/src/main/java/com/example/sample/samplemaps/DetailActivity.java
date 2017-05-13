package com.example.sample.samplemaps;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 詳細アクティビティ
 *
 * リストビュー表示された情報から、各到着駅に関する詳細情報を表示する。
 */
public class DetailActivity extends Activity {

    private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RelativeLayout layout = new RelativeLayout(this);
        setContentView(layout, new RelativeLayout.LayoutParams(WC, WC));

        // Mainから要素を受け取って表示
        Intent intent = getIntent();
        DisplayModel displayModel = (DisplayModel)intent.getSerializableExtra("displayModel");
        // 詳細リストの要素数分、テキストビューを作成
        int i = 0;
        for (SearchResultModel model : displayModel.getDetailList()) {

            TextView textView = new TextView(this);
            textView.setText(model.getStationNameFrom() + " → " + model.getStationNameTo() + "\n"
                    + "所要時間 " + model.getFastestTime() + "\n"
                    + model.getTransfer() + "\n"
                    + "運賃 " + model.getCost() + "\n"
                    );
            textView.setId(i);
            textView.setTextColor(Color.BLACK);
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(WC, WC);
            // 最初の1回だけは親に沿うようにする。
            if (i == 0) {
                param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            } else {
                param.addRule(RelativeLayout.BELOW, i-1);
            }
            layout.addView(textView, param);
            i++;
            // なぜか1つめと2つめが重なってしまうので、空のビューを挟む
            if (i == 1) {
                textView = new TextView(this);
                textView.setText("\n\n\n\n");
                textView.setId(i);
                param = new RelativeLayout.LayoutParams(WC, WC);
                param.addRule(RelativeLayout.BELOW, i-1);
                layout.addView(textView, param);
                i++;
            }
        }
    }
}
