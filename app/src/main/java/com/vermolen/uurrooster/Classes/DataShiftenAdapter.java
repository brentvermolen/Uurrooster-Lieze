package com.vermolen.uurrooster.Classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vermolen.uurrooster.R;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by Brent on 9/12/2017.
 */

public class DataShiftenAdapter extends BaseAdapter {

    private final Map<String, List<String>> alleShiften;
    Map<String, List<String>> shiften;

    LayoutInflater inflater;

    public DataShiftenAdapter(Context context, Map<String, List<String>> shiften){
        this.shiften = shiften;
        alleShiften = Reader.getShiften();

        inflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return shiften.keySet().size();
    }

    @Override
    public Object getItem(int position) {
        byte bytTeller = 0;
        for (String shift : shiften.keySet()){
            if (bytTeller++ == position){
                return shiften.get(shift);
            }
        }

        return 0;
    }

    public String getItemName(int position){
        byte bytTeller = 0;
        for (String shift : shiften.keySet()){
            if (bytTeller++ == position){
                return shift;
            }
        }

        return "";
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder{
        TextView lblShiftCode;
        TextView lblShiftOmschrijving;
        TextView lblAantal;
        TextView lblOpen;
        TextView lblClose;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null){
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.data_shift_list_layout, null);

            holder.lblShiftCode = (TextView) convertView.findViewById(R.id.lblShiftCode);
            holder.lblShiftOmschrijving = (TextView) convertView.findViewById(R.id.lblShiftOmschrijving);
            holder.lblAantal = (TextView) convertView.findViewById(R.id.lblAantal);
            holder.lblOpen = (TextView) convertView.findViewById(R.id.lblOpen);
            holder.lblClose = (TextView) convertView.findViewById(R.id.lblClose);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        List<String> shiftData = alleShiften.get(getItemName(position));
        if (shiftData.get(0).equals("")){
            holder.lblShiftCode.setVisibility(View.GONE);
            holder.lblOpen.setVisibility(View.GONE);
            holder.lblClose.setVisibility(View.GONE);
            holder.lblShiftOmschrijving.setText(getItemName(position));
        }else{
            holder.lblShiftCode.setVisibility(View.VISIBLE);
            holder.lblOpen.setVisibility(View.VISIBLE);
            holder.lblClose.setVisibility(View.VISIBLE);
            holder.lblShiftCode.setText(getItemName(position));
            holder.lblShiftOmschrijving.setText(shiftData.get(0));
        }

        int aantal = Integer.parseInt(String.valueOf(((List<String>)getItem(position)).size()));
        holder.lblAantal.setText(String.valueOf(aantal));

        return convertView;
    }
}
