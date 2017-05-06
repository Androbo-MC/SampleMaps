package com.example.sample.samplemaps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;

public class DetailActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //Mainから要素を受け取って表示
        Intent intent = getIntent();
        DisplayModel displayModel = (DisplayModel)intent.getSerializableExtra("displayModel");
        // そのままだと見づらいのでいったんリストに詰める
        ArrayList<String> stationFrom = new ArrayList<>();
        ArrayList<String> stationTo = new ArrayList<>();
        ArrayList<String> time = new ArrayList<>();
        ArrayList<String> transfer = new ArrayList<>();
        ArrayList<String> cost = new ArrayList<>();
        for (SearchResultModel model : displayModel.getDetailList()) {
            stationFrom.add(model.getStationNameFrom());
            stationTo.add(model.getStationNameTo());
            time.add(model.getFastestTime());
            transfer.add(model.getTransfer());
            cost.add(model.getCost());
        }
        // 画面表示処理
        TextView title1 = (TextView)this.findViewById(R.id.title1);
        title1.setText(stationFrom.get(0) + " → " + stationTo.get(0));
        TextView time1 = (TextView)this.findViewById(R.id.time1);
        time1.setText(("所要時間 " + time.get(0)));
        TextView transfer1 = (TextView)this.findViewById(R.id.transfer1);
        transfer1.setText(transfer.get(0));
        TextView cost1 = (TextView)this.findViewById(R.id.cost1);
        cost1.setText(("運賃 " + cost.get(0)));
        // 詳細リストの長さが2以上あれば2行目をセットする
        if (displayModel.getDetailList().size() > 1) {
            TextView title2 = (TextView)this.findViewById(R.id.title2);
            title2.setText(stationFrom.get(1) + " → " + stationTo.get(1));
            TextView time2 = (TextView)this.findViewById(R.id.time2);
            time2.setText(("所要時間 " + time.get(1)));
            TextView transfer2 = (TextView)this.findViewById(R.id.transfer2);
            transfer2.setText(transfer.get(1));
            TextView cost2 = (TextView)this.findViewById(R.id.cost2);
            cost2.setText(("運賃 " + cost.get(1)));
        }
        if (displayModel.getDetailList().size() > 2) {
            TextView title3 = (TextView)this.findViewById(R.id.title3);
            title3.setText(stationFrom.get(2) + " → " + stationTo.get(2));
            TextView time3 = (TextView)this.findViewById(R.id.time3);
            time3.setText(("所要時間 " + time.get(2)));
            TextView transfer3 = (TextView)this.findViewById(R.id.transfer3);
            transfer3.setText(transfer.get(2));
            TextView cost3 = (TextView)this.findViewById(R.id.cost3);
            cost3.setText(("運賃 " + cost.get(2)));
        }
        if (displayModel.getDetailList().size() > 3) {
            TextView title4 = (TextView)this.findViewById(R.id.title4);
            title4.setText(stationFrom.get(3) + " → " + stationTo.get(3));
            TextView time4 = (TextView)this.findViewById(R.id.time4);
            time4.setText(("所要時間 " + time.get(3)));
            TextView transfer4 = (TextView)this.findViewById(R.id.transfer4);
            transfer4.setText(transfer.get(3));
            TextView cost4 = (TextView)this.findViewById(R.id.cost4);
            cost4.setText(("運賃 " + cost.get(3)));
        }
        if (displayModel.getDetailList().size() > 4) {
            TextView title5 = (TextView)this.findViewById(R.id.title5);
            title5.setText(stationFrom.get(4) + " → " + stationTo.get(4));
            TextView time5 = (TextView)this.findViewById(R.id.time5);
            time5.setText(("所要時間 " + time.get(4)));
            TextView transfer5 = (TextView)this.findViewById(R.id.transfer5);
            transfer5.setText(transfer.get(4));
            TextView cost5 = (TextView)this.findViewById(R.id.cost5);
            cost5.setText(("運賃 " + cost.get(4)));
        }
    }
}
