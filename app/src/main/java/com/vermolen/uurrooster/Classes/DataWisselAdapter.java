package com.vermolen.uurrooster.Classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vermolen.uurrooster.R;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Brent on 9/12/2017.
 */

public class DataWisselAdapter extends BaseAdapter {
    private final List<String> alleCollegas;
    Map<String, Map<String, List<String>>> wissels;

    LayoutInflater inflater;

    public DataWisselAdapter(Context context, Map<String, Map<String, List<String>>> wissels){
        this.wissels = wissels;
        alleCollegas = Reader.getCollegas();

        inflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return wissels.keySet().size();
    }

    @Override
    public Object getItem(int position) {
        byte bytTeller = 0;
        for (String shift : wissels.keySet()){
            if (bytTeller++ == position){
                return wissels.get(shift);
            }
        }

        return 0;
    }

    public String getItemName(int position){
        byte bytTeller = 0;
        for (String collega : wissels.keySet()){
            if (bytTeller++ == position){
                return collega;
            }
        }

        return "";
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder{
        TextView lblCollega;
        TextView lblAantal;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null){
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.data_wissel_list_layout, null);

            holder.lblCollega = (TextView) convertView.findViewById(R.id.lblCollega);
            holder.lblAantal = (TextView) convertView.findViewById(R.id.lblAantal);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        Map<String, List<String>> data = (Map<String, List<String>>) getItem(position);
        int aantal = 0;
        for(String str : data.keySet()){
            List<String> list = data.get(str);
            aantal += list.size();
        }

        holder.lblCollega.setText(getItemName(position));
        holder.lblAantal.setText(String.valueOf(aantal));

        return convertView;
    }
}
