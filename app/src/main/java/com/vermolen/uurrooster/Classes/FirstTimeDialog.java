package com.vermolen.uurrooster.Classes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vermolen.uurrooster.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Brent on 26/12/2017.
 */

public class FirstTimeDialog extends AlertDialog.Builder {
    private Map<String, String> messages;

    public FirstTimeDialog(Context context, Map<String, String> messages, SharedPreferences sharedPref) {
        super(context);
        this.messages = messages;
        createDialog(context, messages, sharedPref, false);
    }
    public FirstTimeDialog(Context context, Map<String, String> messages, SharedPreferences sharedPref, boolean withTabs) {
        super(context);
        this.messages = messages;
        createDialog(context, messages, sharedPref, true);
    }

    private void createDialog(final Context context, final Map<String, String> messages, final SharedPreferences sharedPref, final boolean withTabs) {
        LayoutInflater inflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialog_view = inflater.inflate(R.layout.dialog_first_time, null);

        final LinearLayout viewSwitcher = (LinearLayout) dialog_view.findViewById(R.id.viewSwitch);
        final CheckBox chkDontShowAgain = (CheckBox) dialog_view.findViewById(R.id.chkDontShowAgain);

        int aantal = messages.size();
        final int[] huidige = {0};
        int teller = 1;
        final List<LinearLayout> layouts = new ArrayList<>();

        for(String titel : messages.keySet()){
            LinearLayout inner_view = (LinearLayout) inflater.inflate(R.layout.tutorial_layout, null);

            ((TextView) inner_view.findViewById(R.id.lblTitel)).setText(titel);
            ((TextView) inner_view.findViewById(R.id.lblTekst)).setText(messages.get(titel));
            ((TextView) inner_view.findViewById(R.id.lblHuidige)).setText(String.valueOf(teller++));
            ((TextView) inner_view.findViewById(R.id.lblMax)).setText(String.valueOf(aantal));

            layouts.add(inner_view);
        }

        viewSwitcher.addView(layouts.get(0));

        final ImageButton btnLeft = (ImageButton) dialog_view.findViewById(R.id.btnLeft);
        final ImageButton btnRight = (ImageButton) dialog_view.findViewById(R.id.btnRight);

        if (aantal == 1){
            btnLeft.setVisibility(View.GONE);
            btnRight.setVisibility(View.GONE);
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                btnLeft.setImageAlpha(100);
            }else{
                btnLeft.setVisibility(View.INVISIBLE);
            }
        }

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                huidige[0]--;
                if (huidige[0] <= 0){
                    huidige[0] = 0;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        btnLeft.setImageAlpha(100);
                    }else{
                        btnLeft.setVisibility(View.INVISIBLE);
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    btnRight.setImageAlpha(255);
                }else{
                    btnRight.setVisibility(View.VISIBLE);
                }

                viewSwitcher.removeAllViews();
                viewSwitcher.addView(layouts.get(huidige[0]));
            }
        });
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                huidige[0]++;
                if (huidige[0] >= messages.size() - 1){
                    huidige[0] = messages.size() - 1;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        btnRight.setImageAlpha(100);
                    }else{
                        btnRight.setVisibility(View.INVISIBLE);
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    btnLeft.setImageAlpha(255);
                }else{
                    btnLeft.setVisibility(View.VISIBLE);
                }

                viewSwitcher.removeAllViews();
                viewSwitcher.addView(layouts.get(huidige[0]));
            }
        });

        setView(dialog_view);

        setCancelable(true).setPositiveButton(context.getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (chkDontShowAgain.isChecked()) {
                    String key = "first";
                    if (withTabs){
                        key += getKeyAt(huidige[0]);
                    }
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean(key, false);
                    editor.apply();
                    editor.commit();
                }
            }
        });
    }

    private String getKeyAt(int i) {
        byte bytTeller = 0;
        for (String key : messages.keySet()){
            if (bytTeller++ == i){
                return key;
            }
        }

        return "";
    }
}
