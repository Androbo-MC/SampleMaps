package com.example.sample.samplemaps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

// アダプターをカスタムする
public class MyAdapter extends BaseAdapter {
    Context context;
    LayoutInflater myInflater;
    ArrayList<DisplayModel> list;

    // コンストラクタでnew時に呼ばれる処理を書いとく
    public MyAdapter(Context context, ArrayList<DisplayModel> list) {
        this.context = context;
        this.myInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.list = list;   // 引数で受け取ったlistを入れる
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // リストビュー1行ごとに呼ばれる
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // ビューがあれば再利用する処理
        if (convertView == null) {
            convertView = myInflater.inflate(R.layout.listview_row, parent, false);
        }
        // 各テキストをレイアウトと繋ぐ
        TextView message = (TextView) convertView.findViewById(R.id.title);
        message.setText(list.get(position).getTitle());
        TextView time = (TextView) convertView.findViewById(R.id.ave_time);
        // 小数点第1位で四捨五入して表示
        time.setText("平均所要時間 " + String.format("%.1f", list.get(position).getAveTime()) + "分");
        TextView read = (TextView) convertView.findViewById(R.id.ave_trans);
        read.setText("平均乗換回数 " + String.format("%.1f", list.get(position).getAveTrans()) + "回");

        return convertView;
    }
}
