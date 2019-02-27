package com.vermolen.uurrooster.Classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vermolen.uurrooster.R;

/**
 * Created by Brent on 6/12/2017.
 */

public class ObjTimePicker {
    private static View TimePicker;

    private static int hour;
    private static int minute;

    private static TextView lblBegin;
    private static TextView lblBeginMin;

    private static SeekBar skbBegin;
    private static SeekBar skbBeginMin;

    public ObjTimePicker(Context context){
        this(0, 0, context);
    }

    public ObjTimePicker(int hour, int minute, Context context){
        LayoutInflater inflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.TimePicker = inflater.inflate(R.layout.dag_layout, null);
        this.hour = hour;
        this.minute = minute;

        initViews();
        handleEvents();
    }

    public static View getTimePicker() {
        return TimePicker;
    }

    private void initViews() {
        lblBegin = (TextView) TimePicker.findViewById(R.id.lblNieuwBegin);
        lblBegin.setText(String.valueOf(hour));
        lblBeginMin = (TextView) TimePicker.findViewById(R.id.lblNieuwBeginMin);
        lblBeginMin.setText(String.valueOf(minute));

        skbBegin = (SeekBar) TimePicker.findViewById(R.id.skbVan);
        skbBeginMin = (SeekBar) TimePicker.findViewById(R.id.skbVanMin);
    }

    private void handleEvents() {
        skbBegin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lblBegin.setText(String.valueOf(progress));
                hour = progress;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        skbBeginMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lblBeginMin.setText(String.valueOf(progress));
                minute = progress;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    public static int getHour() {
        return hour;
    }

    public static int getMinute() {
        return minute;
    }
}
