package com.example.sample.samplemaps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

/**
 * 自作アダプター
 *
 * カスタムしたアダプターでリストビューの1行の内容を定義する。
 */
public class MyAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater myInflater;
    private ArrayList<DisplayModel> list;

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
        // 小数点第1位で四捨五入して表示
        TextView listViewRow = (TextView) convertView.findViewById(R.id.list_view_row);
        listViewRow.setText("平均所要時間 " + String.format(Locale.US, "%.1f", list.get(position).getAveTime()) + "分\n"
                + "平均乗換回数 " + String.format(Locale.US, "%.1f", list.get(position).getAveTrans()) + "回\n"
                + "平均乗車運賃 " + String.format(Locale.US, "%.1f", list.get(position).getAveCost()) + "円\n");

        return convertView;
    }
}
