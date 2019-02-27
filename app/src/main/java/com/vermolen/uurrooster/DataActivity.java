package com.vermolen.uurrooster;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.vermolen.uurrooster.Classes.DataShiftenAdapter;
import com.vermolen.uurrooster.Classes.DataWisselAdapter;
import com.vermolen.uurrooster.Classes.Enums.Maand;
import com.vermolen.uurrooster.Classes.Enums.Voorkeur;
import com.vermolen.uurrooster.Classes.FirstTimeDialog;
import com.vermolen.uurrooster.Classes.Methodes;
import com.vermolen.uurrooster.Classes.Reader;
import com.vermolen.uurrooster.Classes.TextReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataActivity extends AppCompatActivity {
    private ListView lstShiften;
    private LinearLayout llShiftenInfo;
    private ListView lstWissels;
    private LinearLayout llWisselInfo;

    private ProgressBar prgLoading;

    private ImageButton btnEdit;

    private int dagVan;
    private int maandVan;
    private int jaarVan;
    private int dagTot;
    private int maandTot;
    private int jaarTot;
    private Map<Integer, List<String>> werkData;
    private Map<Integer, List<String>> wisselData;
    private Map<Integer, List<String>> persoonlijkData;

    boolean hasWissel;

    private Map<String, List<String>> shiften;
    private Map<String /*collega*/, Map<String/*shift*/, List<String>/*lijst datums*/>> wissels;

    private static boolean datePicked = false;
    private static int positionShifts = 0;
    private static int positionWissels = 0;
    private static int currentTab = 0;

    private SharedPreferences sharedPref;
    private TabHost host;

    private RadioButton rdbMaand;
    private RadioButton rdbJaar;
    private RadioButton rdbTabShift;
    private RadioButton rdbTabChanges;

    private LinearLayout llPreview;

    private String currRdb;
    private Map<Voorkeur, String> voorkeuren;

    private Bitmap bitmap = null;
    private String title;

    private Map<String, List<String>> shiftData;

    boolean useBackgroundShiftChange;
    boolean useBackgroundShift;
    boolean showPersonalNotes;
    String alternatif;
    int firstDay;
    private int intTextSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.toolbar_delete_edit_save);
        Toolbar parent = (Toolbar) getSupportActionBar().getCustomView().getParent();
        ((TextView) getSupportActionBar().getCustomView().findViewById(R.id.lblToolbarDeleteEditSaveTitel)).setText(getResources().getString(R.string.workData));
        parent.setPadding(0,0,0,0);
        parent.setContentInsetsAbsolute(0, 0);

        loadSettings();

        initViews();
        handleEvents();

        currRdb = "";

        readDates();

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                prgLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                shiftData = Reader.getShiften();
                voorkeuren = Reader.getVoorkeuren();

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (dagVan == 0 && maandVan == 0 && jaarVan == 0 && dagTot == 0 && maandTot == 0 && jaarTot == 0){
                    sharedPref = getPreferences(MODE_PRIVATE);
                    dagVan = sharedPref.getInt("dagVan", 1);
                    maandVan = sharedPref.getInt("maandVan", Calendar.getInstance().get(Calendar.MONTH) + 1);
                    jaarVan = sharedPref.getInt("jaarVan", Calendar.getInstance().get(Calendar.YEAR));
                    dagTot = sharedPref.getInt("dagTot", 1);
                    maandTot = sharedPref.getInt("maandTot", Calendar.getInstance().get(Calendar.MONTH) + 1);
                    jaarTot = sharedPref.getInt("jaarTot", Calendar.getInstance().get(Calendar.YEAR));

                    datePicked = false;
                }else{
                    datePicked = true;
                }

                if (datePicked == false){
                    positionShifts = 0;
                    positionWissels = 0;
                    currentTab = 0;
                    requestDate();
                }else{
                    vulAdapters();
                }

                //checkFirstTime();

                prgLoading.setVisibility(View.GONE);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void checkFirstTime() {
        final SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        boolean first = sharedPref.getBoolean("first", true);
        if (first){
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.tutorial));

            Map<String, String> messages = new HashMap<>();
            messages.put(getResources().getString(R.string.workData), getResources().getString(R.string.messageData1));
            messages.put(getResources().getString(R.string.messageDataTitel2), getResources().getString(R.string.messageData2));
            messages.put(getResources().getString(R.string.print), getResources().getString(R.string.messageData3));

            FirstTimeDialog dialog = new FirstTimeDialog(this, messages, sharedPref);
            dialog.show();
        }
    }

    private void loadSettings() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("Settings", MODE_PRIVATE);
        useBackgroundShift = sharedPref.getBoolean("backgroundShift", true);
        useBackgroundShiftChange = sharedPref.getBoolean("backgroundShiftChange", true);
        showPersonalNotes = sharedPref.getBoolean("showPersonalNotes", true);
        alternatif = sharedPref.getString("alternatief", getResources().getString(R.string.notes));
        firstDay = sharedPref.getInt("firstDay", 0);
        intTextSize = sharedPref.getInt("textSize", 10);
    }

    private void initViews() {
        host = (TabHost) findViewById(R.id.tabHost);

        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec(getResources().getString(R.string.shifts));
        spec.setContent(R.id.Shiften);
        spec.setIndicator(getResources().getString(R.string.shifts));
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec(getResources().getString(R.string.changes));
        spec.setContent(R.id.Wissels);
        spec.setIndicator(getResources().getString(R.string.changes));
        host.addTab(spec);

        //Tab 3
        spec = host.newTabSpec(getResources().getString(R.string.print));
        spec.setContent(R.id.Print);
        spec.setIndicator(getResources().getString(R.string.print));
        host.addTab(spec);

        btnEdit = (ImageButton) findViewById(R.id.btnEdit);
        ((ImageButton)findViewById(R.id.btnDelete)).setVisibility(View.GONE);
        ((ImageButton)findViewById(R.id.btnOpslaan)).setVisibility(View.GONE);

        lstShiften = (ListView) findViewById(R.id.lstShiften);

        llShiftenInfo = (LinearLayout) findViewById(R.id.llShiftenInfo);
        LinearLayout llShiften = (LinearLayout) findViewById(R.id.llShiften);

        ((GradientDrawable)llShiften.getBackground()).setColor(Color.TRANSPARENT);

        lstWissels = (ListView) findViewById(R.id.lstWissels);

        llWisselInfo = (LinearLayout) findViewById(R.id.llWisselInfo);
        LinearLayout llWissels = (LinearLayout) findViewById(R.id.llWissels);

        ((GradientDrawable)llWissels.getBackground()).setColor(Color.TRANSPARENT);

        rdbMaand = (RadioButton) findViewById(R.id.rdbMaandKalender);
        rdbJaar = (RadioButton) findViewById(R.id.rdbJaarOverzicht);
        rdbTabShift = (RadioButton) findViewById(R.id.rdbTabShift);
        rdbTabChanges = (RadioButton) findViewById(R.id.rdbTabChange);

        llPreview = (LinearLayout) findViewById(R.id.llPreview);

        prgLoading = (ProgressBar) findViewById(R.id.prgLoading);
    }

    private void handleEvents() {
        ((ImageButton) findViewById(R.id.imgBackButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        lstShiften.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataActivity.this.positionShifts = position;
                int vis = ((TextView) view.findViewById(R.id.lblShiftCode)).getVisibility();
                String shiftCode;
                String shiftOmschrijving = "";
                String aantal;
                String title;

                aantal = ((TextView) view.findViewById(R.id.lblAantal)).getText().toString();

                if (vis == View.GONE){
                    shiftCode = ((TextView) view.findViewById(R.id.lblShiftOmschrijving)).getText().toString();
                    title = shiftCode + " - " + aantal;
                }else {
                    shiftCode = ((TextView) view.findViewById(R.id.lblShiftCode)).getText().toString();
                    shiftOmschrijving = ((TextView) view.findViewById(R.id.lblShiftOmschrijving)).getText().toString();
                    title = shiftOmschrijving + " (" + shiftCode + ") - " + aantal;
                }

                setShiftInfo(shiftCode, title, true);
            }
        });

        lstWissels.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataActivity.this.positionWissels = position;
                String collega = ((TextView) view.findViewById(R.id.lblCollega)).getText().toString();
                String aantal = ((TextView) view.findViewById(R.id.lblAantal)).getText().toString();

                String title = collega + " - " + aantal;

                setWisselInfo(collega, title);
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                positionShifts = 0;
                positionWissels = 0;
                rdbMaand.setChecked(false);
                rdbJaar.setChecked(false);
                rdbTabShift.setChecked(false);
                rdbTabChanges.setChecked(false);
                requestDate();
            }
        });

        host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                currentTab = host.getCurrentTab();
            }
        });

        rdbMaand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    rdbJaar.setChecked(false);
                    rdbTabShift.setChecked(false);
                    rdbTabChanges.setChecked(false);

                    currRdb = "Maand";
                    createBitmap();
                }
            }
        });

        rdbJaar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    rdbMaand.setChecked(false);
                    rdbTabShift.setChecked(false);
                    rdbTabChanges.setChecked(false);

                    currRdb = "Jaar";
                    createBitmap();
                }
            }
        });

        rdbTabShift.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    rdbMaand.setChecked(false);
                    rdbJaar.setChecked(false);
                    rdbTabChanges.setChecked(false);

                    currRdb = "TabShift";
                    createBitmap();
                }
            }
        });

        rdbTabChanges.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    rdbMaand.setChecked(false);
                    rdbJaar.setChecked(false);
                    rdbTabShift.setChecked(false);

                    currRdb = "TabChanges";
                    createBitmap();
                }
            }
        });
    }

    private void readDates() {
        sharedPref = getPreferences(MODE_PRIVATE);
        dagVan = sharedPref.getInt("dagVan", 0);
        maandVan = sharedPref.getInt("maandVan", 0);
        jaarVan = sharedPref.getInt("jaarVan", 0);
        dagTot = sharedPref.getInt("dagTot", 0);
        maandTot = sharedPref.getInt("maandTot", 0);
        jaarTot = sharedPref.getInt("jaarTot", 0);
    }

    private void requestDate() {
        AlertDialog.Builder builderRequestDates = new AlertDialog.Builder(this);
        builderRequestDates.setTitle(getResources().getString(R.string.setPeriod));
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(10, 3, 3, 3);
        ll.setLayoutParams(params);

        TextView lblTitelVan = new TextView(this);
        lblTitelVan.setTypeface(Typeface.DEFAULT_BOLD);
        lblTitelVan.setTextSize(20);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            lblTitelVan.setGravity(Gravity.CENTER);
            lblTitelVan.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        }
        lblTitelVan.setText(getResources().getString(R.string.from));
        ll.addView(lblTitelVan);

        final TextView lblVan = new TextView(this);
        lblVan.setTextSize(20);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            lblVan.setGravity(Gravity.CENTER);
            lblVan.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        }
        lblVan.setText(String.format("\n%02d/%02d/%04d\n", dagVan, maandVan, jaarVan));

        lblVan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DataActivity.this);

                final DatePicker datePickerVan = new DatePicker(DataActivity.this);
                datePickerVan.updateDate(jaarVan, maandVan - 1, dagVan);

                builder.setView(datePickerVan);

                builder.setCancelable(true).setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dagVan = datePickerVan.getDayOfMonth();
                        maandVan = datePickerVan.getMonth() + 1;
                        jaarVan = datePickerVan.getYear();
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("dagVan", dagVan);
                        editor.putInt("maandVan", maandVan);
                        editor.putInt("jaarVan", jaarVan);
                        editor.apply();
                        editor.commit();
                        lblVan.setText(String.format("\n%02d/%02d/%04d\n", dagVan, maandVan, jaarVan));
                    }
                });
                builder.show();
            }
        });
        ll.addView(lblVan);


        TextView lblTitelTot = new TextView(this);
        lblTitelTot.setTypeface(Typeface.DEFAULT_BOLD);
        lblTitelTot.setTextSize(20);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            lblTitelTot.setGravity(Gravity.CENTER);
            lblTitelTot.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        }
        lblTitelTot.setText(getResources().getString(R.string.to));
        ll.addView(lblTitelTot);

        final TextView lblTot = new TextView(this);
        lblTot.setTextSize(20);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            lblTot.setGravity(Gravity.CENTER);
            lblTot.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        }
        lblTot.setText(String.format("\n%02d/%02d/%04d\n", dagTot, maandTot, jaarTot));
        lblTot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DataActivity.this);

                final DatePicker datePickerTot = new DatePicker(DataActivity.this);
                datePickerTot.updateDate(jaarTot, maandTot - 1, dagTot);

                builder.setView(datePickerTot);

                builder.setCancelable(true).setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dagTot = datePickerTot.getDayOfMonth();
                        maandTot = datePickerTot.getMonth() + 1;
                        jaarTot = datePickerTot.getYear();
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("dagTot", dagTot);
                        editor.putInt("maandTot", maandTot);
                        editor.putInt("jaarTot", jaarTot);
                        editor.apply();
                        editor.commit();
                        lblTot.setText(String.format("\n%02d/%02d/%04d\n", dagTot, maandTot, jaarTot));
                    }
                });
                builder.show();
            }
        });
        ll.addView(lblTot);

        builderRequestDates.setView(ll);

        builderRequestDates.setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (!datePicked){
                    datePicked = false;
                    finish();
                }
            }
        }).setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!datePicked){
                    datePicked = false;
                    finish();
                }
            }
        }).setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                vulAdapters();

                if (jaarVan > jaarTot){
                    error();
                    return;
                }else{
                    if (jaarVan == jaarTot){
                        if (maandVan > maandTot){
                            error();
                            return;
                        }else{
                            if (maandVan == maandTot){
                                if (dagVan > dagTot){
                                    error();
                                    return;
                                }
                            }
                        }
                    }
                }

                datePicked = true;
            }
        });

        builderRequestDates.show();
    }

    private void error(){
        Toast.makeText(this, getResources().getString(R.string.toastCheckDate), Toast.LENGTH_LONG).show();
    }

    private void vulAdapters() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                prgLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                shiften = new HashMap<String, List<String>>();
                wissels = new HashMap<String, Map<String, List<String>>>();

                Maand maandVan = Maand.valueOf(DataActivity.this.maandVan);
                Maand maandTot = Maand.valueOf(DataActivity.this.maandTot);

                for (int i = jaarVan; i <= jaarTot; i++) {
                    int jaar = i;

                    if (jaarVan < jaarTot) {
                        if (jaarVan == i) {
                            for (int j = maandVan.getNr(); j <= 12; j++) {
                                werkData = Reader.getWerkData(Maand.valueOf(j), i);
                                wisselData = Reader.getWisselData(Maand.valueOf(j), i);

                                for (int dag : werkData.keySet()) {
                                    if (j == maandVan.getNr()) {
                                        if (dag < dagVan) {
                                            continue;
                                        }
                                    }

                                    String shift = geefDataWerk(dag, true);
                                    if (hasWissel) {
                                        List<String> wissel = geefDataWissel(dag);
                                        String collega = wissel.get(0);
                                        String wisselShift = wissel.get(1);
                                        Map<String, List<String>> shiftenWissels = wissels.get(collega);
                                        if (shiftenWissels == null) {
                                            shiftenWissels = new HashMap<String, List<String>>();
                                        }
                                        List<String> datums = shiftenWissels.get(wisselShift);
                                        if (datums == null) {
                                            datums = new ArrayList<>();
                                        }

                                        datums.add(String.format("%02d/%02d/%04d", dag, j, i));
                                        if (shiftenWissels.containsKey(wisselShift)) {
                                            shiftenWissels.remove(wisselShift);
                                        }
                                        shiftenWissels.put(wisselShift, datums);
                                        if (wissels.containsKey(collega)) {
                                            wissels.remove(collega);
                                        }
                                        wissels.put(collega, shiftenWissels);
                                    }

                                    List<String> aantal = shiften.get(shift);
                                    if (aantal == null) {
                                        aantal = new ArrayList<>();
                                    }

                                    if (shiften.containsKey(shift)) {
                                        shiften.remove(shift);
                                    }
                                    aantal.add(String.format("%02d/%02d/%04d", dag, j, i));
                                    shiften.put(shift, aantal);
                                }
                            }

                        } else {
                            for (int j = 1; j <= 12; j++) {
                                werkData = Reader.getWerkData(Maand.valueOf(j), i);
                                wisselData = Reader.getWisselData(Maand.valueOf(j), i);

                                for (int dag : werkData.keySet()) {
                                    if (i == jaarTot) {
                                        if (j == maandTot.getNr()) {
                                            if (dag > dagTot) {
                                                continue;
                                            }
                                        }
                                    }

                                    String shift = geefDataWerk(dag, true);
                                    if (hasWissel) {
                                        List<String> wissel = geefDataWissel(dag);
                                        String collega = wissel.get(0);
                                        String wisselShift = wissel.get(1);
                                        Map<String, List<String>> shiftenWissels = wissels.get(collega);
                                        if (shiftenWissels == null) {
                                            shiftenWissels = new HashMap<String, List<String>>();
                                        }
                                        List<String> datums = shiftenWissels.get(wisselShift);
                                        if (datums == null) {
                                            datums = new ArrayList<>();
                                        }
                                        datums.add(String.format("%02d/%02d/%04d", dag, j, i));
                                        if (shiftenWissels.containsKey(wisselShift)) {
                                            shiftenWissels.remove(wisselShift);
                                        }
                                        shiftenWissels.put(wisselShift, datums);
                                        if (wissels.containsKey(collega)) {
                                            wissels.remove(collega);
                                        }
                                        wissels.put(collega, shiftenWissels);
                                    }

                                    List<String> aantal = shiften.get(shift);
                                    if (aantal == null) {
                                        aantal = new ArrayList<>();
                                    }

                                    if (shiften.containsKey(shift)) {
                                        shiften.remove(shift);
                                    }
                                    aantal.add(String.format("%02d/%02d/%04d", dag, j, i));
                                    shiften.put(shift, aantal);
                                }
                            }
                        }
                    } else {
                        for (int j = maandVan.getNr(); j <= maandTot.getNr(); j++) {
                            werkData = Reader.getWerkData(Maand.valueOf(j), i);
                            wisselData = Reader.getWisselData(Maand.valueOf(j), i);
                            for (int dag : werkData.keySet()) {
                                if (j == maandVan.getNr()) {
                                    if (dag < dagVan) {
                                        continue;
                                    }
                                }
                                if (j == maandTot.getNr()) {
                                    if (dag > dagTot) {
                                        continue;
                                    }
                                }

                                String shift = geefDataWerk(dag, true);
                                if (hasWissel) {
                                    List<String> wissel = geefDataWissel(dag);
                                    String collega = wissel.get(0);
                                    String wisselShift = wissel.get(1);
                                    Map<String, List<String>> shiftenWissels = wissels.get(collega);
                                    if (shiftenWissels == null) {
                                        shiftenWissels = new HashMap<String, List<String>>();
                                    }
                                    List<String> datums = shiftenWissels.get(wisselShift);
                                    if (datums == null) {
                                        datums = new ArrayList<>();
                                    }
                                    datums.add(String.format("%02d/%02d/%04d", dag, j, i));
                                    if (shiftenWissels.containsKey(wisselShift)) {
                                        shiftenWissels.remove(wisselShift);
                                    }
                                    shiftenWissels.put(wisselShift, datums);
                                    if (wissels.containsKey(collega)) {
                                        wissels.remove(collega);
                                    }
                                    wissels.put(collega, shiftenWissels);
                                }

                                List<String> aantal = shiften.get(shift);
                                if (aantal == null) {
                                    aantal = new ArrayList<String>();
                                }

                                if (shiften.containsKey(shift)) {
                                    shiften.remove(shift);
                                }
                                aantal.add(String.format("%02d/%02d/%04d", dag, j, i));
                                shiften.put(shift, aantal);
                            }
                        }
                    }
                }


                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                prgLoading.setVisibility(View.GONE);

                DataShiftenAdapter dataShiftenAdapter = new DataShiftenAdapter(DataActivity.this, shiften);

                lstShiften.setAdapter(dataShiftenAdapter);
                try {
                    lstShiften.performItemClick(dataShiftenAdapter.getView(positionShifts, null, null), positionShifts, dataShiftenAdapter.getItemId(positionShifts));
                } catch (Exception e) {
                    if (dataShiftenAdapter.getCount() > 0) {
                        lstShiften.performItemClick(dataShiftenAdapter.getView(0, null, null), 0, dataShiftenAdapter.getItemId(0));
                    } else {
                        llShiftenInfo.removeAllViews();

                        TextView lbl = (TextView) findViewById(R.id.lblShiftTitel);
                        lbl.setText("");
                    }
                }

                DataWisselAdapter dataWisselAdapter = new DataWisselAdapter(DataActivity.this, wissels);

                lstWissels.setAdapter(dataWisselAdapter);
                try {
                    lstWissels.performItemClick(dataWisselAdapter.getView(positionWissels, null, null), positionWissels, dataWisselAdapter.getItemId(positionShifts));
                } catch (Exception e) {
                    if (dataWisselAdapter.getCount() > 0) {
                        lstWissels.performItemClick(dataWisselAdapter.getView(0, null, null), 0, dataWisselAdapter.getItemId(0));
                    } else {
                        llWisselInfo.removeAllViews();

                        TextView lbl = (TextView) findViewById(R.id.lblWisselTitel);
                        lbl.setText("");
                    }
                }

                host.setCurrentTab(currentTab);
            }
        };

        task.execute();
    }

    private String geefDataWerk(int dag, boolean wisselHeeftVoorrang) {
        List<String> lst = new ArrayList<String>();
        lst = werkData.get(dag);

        hasWissel = false;

        if (wisselHeeftVoorrang) {
            List<String> wisselList = geefDataWissel(dag);
            if (wisselList != null) {
                String strWisselData = wisselList.get(1);

                if (!(strWisselData.equals(""))) {
                    hasWissel = true;
                    if (wisselList.get(3).equals("True")) {
                        return "ZO " + strWisselData;
                    } else {
                        return strWisselData;
                    }
                }
            }
        }

        if (lst == null) {
            return "";
        }

        return lst.get(0);
    }

    private List<String> geefDataWissel(int dag) {
        List<String> lst = new ArrayList<>();
        lst = wisselData.get(dag);

        return lst;
    }

    private String geefDataPersoonlijk(int dag) {
        List<String> lst = new ArrayList<>();
        lst = persoonlijkData.get(dag);

        if (lst == null) {
            return "";
        }

        if (!(lst.get(0).equals(""))) {
            return lst.get(0);
        }

        return "";
    }

    private void setWisselInfo(String collega, String title) {
        llWisselInfo.setOrientation(LinearLayout.HORIZONTAL);
        llWisselInfo.removeAllViews();

        TextView lbl = (TextView) findViewById(R.id.lblWisselTitel);
        lbl.setText(title);

        LinearLayout llKolom1 = new LinearLayout(this);
        llKolom1.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params1.weight = 1;
        llKolom1.setLayoutParams(params1);

        LinearLayout llKolom2 = new LinearLayout(this);
        llKolom2.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params2.weight = 1;
        llKolom2.setLayoutParams(params2);

        Map<String, List<String>> shiften = wissels.get(collega);
        int intTeller = 0;

        for (String shift : shiften.keySet()){
            List<String> datums = shiften.get(shift);

            TextView lblShift = new TextView(this);
            lblShift.setTextSize(20);
            lblShift.setTypeface(Typeface.DEFAULT_BOLD);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                lblShift.setGravity(Gravity.CENTER);
                lblShift.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            }
            lblShift.setText(shift);

            if (intTeller % 2 == 0){
                llKolom1.addView(lblShift);
            }else{
                llKolom2.addView(lblShift);
            }

            for (String datum : datums){
                TextView lblDatum = new TextView(this);
                lblDatum.setTextSize(20);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    lblDatum.setGravity(Gravity.CENTER);
                    lblDatum.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                }
                lblDatum.setText(datum);

                if (intTeller % 2 == 0){
                    llKolom1.addView(lblDatum);
                }else{
                    llKolom2.addView(lblDatum);
                }
            }

            intTeller++;
        }

        llWisselInfo.addView(llKolom1);
        llWisselInfo.addView(llKolom2);
    }

    public LinearLayout setShiftInfo(String shift, String title, boolean addViewToMain) {
        llShiftenInfo.setOrientation(LinearLayout.VERTICAL);
        llShiftenInfo.removeAllViews();

        TextView lbl = (TextView) findViewById(R.id.lblShiftTitel);
        lbl.setText(title);

        LinearLayout llContent = new LinearLayout(this);
        llContent.setOrientation(LinearLayout.HORIZONTAL);

        List<String> datums = shiften.get(shift);
        Collections.sort(datums, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] date1 = o1.split("/");
                String[] date2 = o2.split("/");

                int dag1 = Integer.parseInt(date1[0]);
                int maand1 = Integer.parseInt(date1[1]);
                int jaar1 = Integer.parseInt(date1[2]);

                int dag2 = Integer.parseInt(date2[0]);
                int maand2 = Integer.parseInt(date2[1]);
                int jaar2 = Integer.parseInt(date2[2]);

                if (jaar1 < jaar2){
                    return -1;
                }else{
                    if (jaar1 > jaar2){
                        return 1;
                    }else{
                        if (maand1 < maand2){
                            return -1;
                        }else{
                            if (maand1 > maand2){
                                return 1;
                            }else{
                                if (dag1 < dag2){
                                    return -1;
                                }else{
                                    if (dag1 > dag2){
                                        return 1;
                                    }else{
                                        return 0;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        int aantal = datums.size();

        LinearLayout llKolom1 = new LinearLayout(this);
        llKolom1.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params1.weight = 1;
        llKolom1.setLayoutParams(params1);

        LinearLayout llKolom2 = new LinearLayout(this);
        llKolom2.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params2.weight = 1;
        llKolom2.setLayoutParams(params2);

        for (int i = 0; i < aantal; i++) {
            TextView txt = new TextView(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                txt.setGravity(Gravity.CENTER);
                txt.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            }
            txt.setTextSize(20);
            txt.setText(datums.get(i));
            txt.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (i % 2 == 0){
                llKolom1.addView(txt);
            }else{
                llKolom2.addView(txt);
            }
        }

        llContent.addView(llKolom1);
        llContent.addView(llKolom2);

        if (addViewToMain){
            llShiftenInfo.addView(llContent);
        }

        return llContent;
    }

    private String getShiftInfoHtml(String shift, String title) {
        String html = "";

        List<String> datums = shiften.get(shift);
        Collections.sort(datums, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] date1 = o1.split("/");
                String[] date2 = o2.split("/");

                int dag1 = Integer.parseInt(date1[0]);
                int maand1 = Integer.parseInt(date1[1]);
                int jaar1 = Integer.parseInt(date1[2]);

                int dag2 = Integer.parseInt(date2[0]);
                int maand2 = Integer.parseInt(date2[1]);
                int jaar2 = Integer.parseInt(date2[2]);

                if (jaar1 < jaar2){
                    return -1;
                }else{
                    if (jaar1 > jaar2){
                        return 1;
                    }else{
                        if (maand1 < maand2){
                            return -1;
                        }else{
                            if (maand1 > maand2){
                                return 1;
                            }else{
                                if (dag1 < dag2){
                                    return -1;
                                }else{
                                    if (dag1 > dag2){
                                        return 1;
                                    }else{
                                        return 0;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        int aantal = datums.size();

        String contentKolom1 = "";
        String contentKolom2 = "";
        String contentKolom3 = "";
        String contentKolom4 = "";
        String contentKolom5 = "";

        int kolom = 0;
        for (int i = 0; i < aantal; i++) {
            switch (kolom){
                case 0:
                    contentKolom1 += "<p align='center'>" + datums.get(i) + "</p>\n";
                    break;
                case 1:
                    contentKolom2 += "<p align='center'>" + datums.get(i) + "</p>\n";
                    break;
                case 2:
                    contentKolom3 += "<p align='center'>" + datums.get(i) + "</p>\n";
                    break;
                case 3:
                    contentKolom4 += "<p align='center'>" + datums.get(i) + "</p>\n";
                    break;
                case 4:
                    contentKolom5 += "<p align='center'>" + datums.get(i) + "</p>\n";
                    kolom = -1;

            }
            kolom++;
        }

        String shiftOmschrijving = shiftData.get(shift).get(0);
        if (shiftOmschrijving.equals("")){
            html = "<h3 padding='0' margin='5'>" + shift + " - " + aantal + "</h3>\n";
        }else{
            html = "<h3 padding='0' margin='5'>" + shiftOmschrijving + " (" + shift + ") - " + aantal + "</h3>\n";
        }
        html += "<div class='row'>\n" +
                "   <div class='column'>\n" +
                        contentKolom1 +
                "   </div>\n" +
                "   <div class='column'>\n" +
                        contentKolom2 +
                "   </div>\n" +
                "   <div class='column'>\n" +
                        contentKolom3 +
                "   </div>\n" +
                "   <div class='column'>\n" +
                        contentKolom4 +
                "   </div>\n" +
                "   <div class='column'>\n" +
                        contentKolom5 +
                "   </div>\n" +
                "</div>\n";

        return html;
    }

    private String getChangesInfoHtml(String collega, String title) {
        String html;

        String contentKolom1 = "";
        String contentKolom2 = "";
        String contentKolom3 = "";
        String contentKolom4 = "";

        Map<String, List<String>> wissels = this.wissels.get(collega);

        int kolom = 0;

        List<String> wisselsShiften = new ArrayList<>(wissels.keySet());
        Collections.sort(wisselsShiften, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return s.compareTo(t1);
            }
        });

        for (String shift : wisselsShiften){
            List<String> datums = wissels.get(shift);

            switch (kolom){
                case 0:
                    contentKolom1 += "<h3 align='center'>" + shift + "</h3>\n";
                    break;
                case 1:
                    contentKolom2 += "<h3 align='center'>" + shift + "</h3>\n";
                    break;
                case 2:
                    contentKolom3 += "<h3 align='center'>" + shift + "</h3>\n";
                    break;
                case 3:
                    contentKolom4 += "<h3 align='center'>" + shift + "</h3>\n";
                    break;
            }

            for (String datum : datums){
                switch (kolom){
                    case 0:
                        contentKolom1 += "<p align='center'>" + datum + "</p>\n";
                        break;
                    case 1:
                        contentKolom2 += "<p align='center'>" + datum + "</p>\n";
                        break;
                    case 2:
                        contentKolom3 += "<p align='center'>" + datum + "</p>\n";
                        break;
                    case 3:
                        contentKolom4 += "<p align='center'>" + datum + "</p>\n";
                        kolom = -1;
                        break;
                }
            }

            kolom++;
        }


        html = "<h3 padding='0' margin='5'>" + collega + "</h3>\n";
        html += "<div class='row'>\n" +
                "   <div class='column'>\n" +
                        contentKolom1 +
                "   </div>\n" +
                "   <div class='column'>\n" +
                        contentKolom2 +
                "   </div>\n" +
                "   <div class='column'>\n" +
                        contentKolom3 +
                "   </div>\n" +
                "   <div class='column'>\n" +
                        contentKolom4 +
                "   </div>\n" +
                "</div>\n";

        return html;
    }

    private void createBitmap() {
        bitmap = null;
        title = "overview";

        switch (currRdb) {
            case "Maand":
                AlertDialog.Builder builderMonth = new AlertDialog.Builder(this);
                builderMonth.setTitle(getResources().getString(R.string.specifyMonth));
                final DatePicker datePicker = new DatePicker(this);
                datePicker.updateDate(jaarVan, maandVan - 1, dagVan);

                builderMonth.setView(datePicker);

                builderMonth.setCancelable(true).setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int maand = datePicker.getMonth() + 1;
                        final Maand mndMaand = Maand.valueOf(maand);

                        final int jaar = datePicker.getYear();
                        int vorigeMaand = maand--;
                        int maxDagenInMaand = Methodes.getMaxDagenInMaand(maand, jaar);
                        int maxDagenInVorigeMaand;
                        if (vorigeMaand == 0) {
                            vorigeMaand = 12;
                            maxDagenInVorigeMaand = Methodes.getMaxDagenInMaand(vorigeMaand, jaar - 1);
                        } else {
                            maxDagenInVorigeMaand = Methodes.getMaxDagenInMaand(vorigeMaand, jaar);
                        }

                        title = mndMaand.toString(DataActivity.this) + String.valueOf(jaar);
                        ((TextView) findViewById(R.id.lblMaand)).setText(title);

                        final LinearLayout llMonth = (LinearLayout) findViewById(R.id.llInflateMonth);

                        new AsyncTask<Void, Void, Void>(){
                            @Override
                            protected void onPreExecute() {
                                prgLoading.setVisibility(View.VISIBLE);
                            }

                            @Override
                            protected Void doInBackground(Void... voids) {
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                vulMaand(mndMaand, jaar);

                                try {
                                    llMonth.setDrawingCacheEnabled(true);
                                    bitmap = Bitmap.createBitmap(llMonth.getDrawingCache(true));
                                    llMonth.setDrawingCacheEnabled(false);
                                    printBitmap(bitmap, title);
                                } catch (Exception e) {
                                    rdbMaand.setChecked(false);
                                    rdbJaar.setChecked(false);
                                    rdbTabShift.setChecked(false);
                                    rdbTabChanges.setChecked(false);
                                }

                                prgLoading.setVisibility(View.GONE);
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                });

                builderMonth.show();
                break;
            case "Jaar":
                AlertDialog.Builder builderJaar = new AlertDialog.Builder(this);

                builderJaar.setTitle(getResources().getString(R.string.specifyYear));

                Map<Integer, List<String>> archief = Reader.getArchief();
                final Spinner cboJaren = new Spinner(this);
                Integer[] arrJaren = new Integer[archief.keySet().size()];
                List<Integer> lstJaren = new ArrayList<>();
                for (int jaar : archief.keySet()) {
                    lstJaren.add(jaar);
                }
                Collections.sort(lstJaren, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer integer, Integer t1) {
                        return integer - t1;
                    }
                });
                lstJaren.toArray(arrJaren);

                ArrayAdapter<Integer> jarenAdapter = new ArrayAdapter<Integer>(this, R.layout.spinner_item, arrJaren);
                cboJaren.setAdapter(jarenAdapter);

                builderJaar.setView(cboJaren);

                builderJaar.setCancelable(true).setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final int jaar = Integer.parseInt(cboJaren.getSelectedItem().toString());
                        title = getResources().getString(R.string.optionYearOverview) + " " + String.valueOf(jaar);

                        new AsyncTask<Void, Void, Void>(){
                            @Override
                            protected void onPreExecute() {
                                prgLoading.setVisibility(View.VISIBLE);
                            }

                            @Override
                            protected Void doInBackground(Void... voids) {
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                vulJaar(jaar);

                                LinearLayout llYear = (LinearLayout) findViewById(R.id.llInflateYear);

                                try {
                                    llYear.setDrawingCacheEnabled(true);
                                    bitmap = Bitmap.createBitmap(llYear.getDrawingCache());
                                    llYear.setDrawingCacheEnabled(false);
                                    printBitmap(bitmap, title);
                                } catch (Exception e) {
                                    rdbMaand.setChecked(false);
                                    rdbJaar.setChecked(false);
                                    rdbTabShift.setChecked(false);
                                    rdbTabChanges.setChecked(false);
                                }

                                prgLoading.setVisibility(View.GONE);
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                });

                builderJaar.show();
                break;
            case "TabShift":
                ImageView img = (ImageView) findViewById(R.id.imageView);
                img.setImageBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888));

                WebView webview = new WebView(DataActivity.this);

                webview.setWebViewClient(new WebViewClient(){
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        return false;
                    }
                });

                String title = getResources().getString(R.string.shiftData);
                String html = getIntroHtml() + "\n" +
                        "<h2 align='center'>" + title + "</h2>\n" +
                        "<h2 align='center'>" + dagVan + "/" + maandVan + "/" + jaarVan +
                                        " - " + dagTot + "/" + maandTot + "/" + jaarTot + "</h2>\n";

                List<String> shiftenSorted = new ArrayList<>(shiften.keySet());

                Collections.sort(shiftenSorted, new Comparator<String>() {
                    @Override
                    public int compare(String s, String t1) {
                        return s.compareTo(t1);
                    }
                });

                for (String shift : shiftenSorted){
                    html += getShiftInfoHtml(shift, title);
                }

                html += getEndHtml();
                webview.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null);

                printText(webview, title);
                rdbTabShift.setChecked(false);
                break;
            case "TabChanges":
                img = (ImageView) findViewById(R.id.imageView);
                img.setImageBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888));

                WebView webviewChanges = new WebView(DataActivity.this);
                webviewChanges.setWebViewClient(new WebViewClient(){
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        return false;
                    }
                });

                String titleChanges = getResources().getString(R.string.shiftChangeData);
                String htmlChanges = getIntroHtml() + "\n" +
                        "<h2 align='center'>" + titleChanges + "</h2>\n" +
                        "<h2 align='center'>" + dagVan + "/" + maandVan + "/" + jaarVan +
                        " - " + dagTot + "/" + maandTot + "/" + jaarTot + "</h2>\n";

                List<String> changesSorted = new ArrayList<>(wissels.keySet());

                Collections.sort(changesSorted, new Comparator<String>() {
                    @Override
                    public int compare(String s, String t1) {
                        return s.compareTo(t1);
                    }
                });

                for (String collega : changesSorted){
                    htmlChanges += getChangesInfoHtml(collega, titleChanges);
                }

                htmlChanges += "\n" +
                        "</body>\n" +
                        "</html>";
                webviewChanges.loadDataWithBaseURL(null, htmlChanges, "text/HTML", "UTF-8", null);

                printText(webviewChanges, titleChanges);
                rdbTabChanges.setChecked(false);
                break;
        }
    }

    private void printText(WebView webView, String title){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PrintManager printer = (PrintManager) getSystemService(PRINT_SERVICE);

            WebView.LayoutParams params = new WebView.LayoutParams(WebView.LayoutParams.MATCH_PARENT, WebView.LayoutParams.MATCH_PARENT, 0, 0);
            webView.setLayoutParams(params);

            PrintDocumentAdapter adapter = webView.createPrintDocumentAdapter(title);

            PrintAttributes.Builder attr = new PrintAttributes.Builder();
            attr.setMediaSize(PrintAttributes.MediaSize.ISO_A4);

            printer.getPrintJobs().add(printer.print(title, adapter, attr.build()));
        }else{
            Toast.makeText(this, getResources().getString(R.string.toastPrintingNotSupported), Toast.LENGTH_SHORT).show();
        }
    }

    private void printBitmap(final Bitmap bitmap, final String title){
        ImageView img = (ImageView) findViewById(R.id.imageView);
        ((GradientDrawable) img.getBackground()).setColor(Color.TRANSPARENT);

        img.setImageBitmap(bitmap);

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    PrintHelper photoPrinter = new PrintHelper(DataActivity.this);
                    photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                    photoPrinter.printBitmap(title, bitmap);
                }catch (Exception e) { }
            }
        });

        rdbMaand.setChecked(false);
        rdbJaar.setChecked(false);
        rdbTabShift.setChecked(false);
        rdbTabChanges.setChecked(false);
    }

    private String getIntroHtml(){
        String html = "<!DOCTYPE html>\n" +
                "<head>\n" +
                "<style>\n" +
                "* {\n" +
                "\tbox-sizing: border-box;\n" +
                "}\n" +
                ".column {\n" +
                "\tfloat: left;\n" +
                "\twidth: 20%;\n" +
                "}\n" +
                ".row:after {\n" +
                "\tcontent: \"\";\n" +
                "\tdisplay: table;\n" +
                "\tclear: both;\n" +
                "}\n" +
                "p {\n" +
                "\tmargin: 2px;\n" +
                "}\n" +
                "\n" +
                "h2 {\n" +
                "\tmargin: 5px;\n" +
                "}\n" +
                "\n" +
                "h3 {\n" +
                "\tmargin: 5px;\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n";

        return html;
    }

    public String getEndHtml() {
        return "</body>\n" +
                "</html>\n";
    }

    private void vulJaar(int jaar) {
        int maandVan = 1;
        int maandTot = 12;

        Resources res = getResources();

        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();

        float textSize;
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180){
            textSize = getTextSize(((TextView) findViewById(R.id.dagenHead1)).getHeight());
        }else{
            textSize = getTextSize(((TextView) findViewById(R.id.dagenHead1)).getWidth());
        }

        for (int i = 1; i <= 31; i++) {
            int id = res.getIdentifier("dagenHead" + i, "id", getPackageName());
            ((TextView) findViewById(id)).setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }

        for (int i = maandVan; i <= maandTot; i++){
            Maand maand = Maand.valueOf(i);

            werkData = Reader.getWerkData(maand, jaar);
            wisselData = Reader.getWisselData(maand, jaar);

            int maxDagenInMaand = Methodes.getMaxDagenInMaand(maand.getNr(), jaar);
            int dagVan = 1;
            int dagTot = 31;

            LinearLayout llExampleMaand = (LinearLayout) findViewById(R.id.dagenHeader);

            int idMaand = res.getIdentifier("dagen" + maand.toString(this), "id", getPackageName());
            LinearLayout llRootMaand = (LinearLayout) findViewById(idMaand);
            llRootMaand.removeAllViews();

            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            View inflate;

            if (llRootMaand.getOrientation() == LinearLayout.VERTICAL){
                inflate = inflater.inflate(R.layout.layout_jaar_maand_text, null);

                /*int idHeadMaand = res.getIdentifier(maand.toString(), "id", getPackageName());
                VerticalTextView lblHeadMaand = (VerticalTextView) findViewById(idHeadMaand);
                int width = ((TextView) findViewById(R.id.dagen)).getWidth();
                lblHeadMaand.setPadding(0, width / 3, 0, 0);*/
            }else{
                inflate = inflater.inflate(R.layout.layout_jaar_maand_text_land, null);
            }

            LinearLayout llMaand = (LinearLayout) inflate.findViewById(R.id.llMaand);

            int width = llExampleMaand.getWidth();
            int height = llExampleMaand.getHeight();

            llMaand.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
            llMaand.layout(0, 0, width, height);
            llMaand.setOrientation(llRootMaand.getOrientation());

            llRootMaand.addView(llMaand);

            for (int dag = dagVan; dag <= dagTot; dag++) {
                String shift = geefDataWerk(dag, true);

                int dagId = res.getIdentifier("dagen" + dag, "id", getPackageName());

                TextView lblShift = (TextView) llMaand.findViewById(dagId);

                LinearLayout parent = (LinearLayout) lblShift.getParent();

                /*lblShift.measure(View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.EXACTLY));
                lblShift.layout(0, 0, lblShift.getMeasuredWidth(), lblShift.getMeasuredHeight());*/

                lblShift.setHeight(parent.getWidth());
                int verschil = (parent.getWidth() - parent.getHeight()) / 2;

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.setMargins(0, 0 - verschil, 0, 0 - verschil);
                lblShift.setPadding(verschil, 0, verschil, 0);

                lblShift.setLayoutParams(params);

                if (hasWissel){
                    try{
                        int color = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_SHIFT_CHANGE));
                        ((GradientDrawable) ((View)lblShift.getParent()).getBackground()).setColor(color);
                    }catch (Exception e) { }
                }else{
                    ((GradientDrawable) ((View)lblShift.getParent()).getBackground()).setColor(Color.TRANSPARENT);
                    lblShift.setBackgroundColor(Color.TRANSPARENT);
                }

                List<String> shiftData = Reader.getShiften().get(shift);
                if (shiftData != null){
                    int kleur = Integer.parseInt(shiftData.get(5));
                    if (kleur != 0){
                        try{
                            ((GradientDrawable) ((View)lblShift.getParent()).getBackground()).setColor(kleur);
                        }catch (Exception e) { }
                    }
                }

                if (dag > maxDagenInMaand){
                    ((GradientDrawable) ((View)lblShift.getParent()).getBackground()).setColor(Color.GRAY);
                }

                lblShift.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                lblShift.setText(shift);
            }
        }
    }

    private int getTextSize(int width){
        Paint paint = new Paint();
        Rect bounds = new Rect();

        int text_width = 0;

        String text = "ZIEK";
        text_width =  width;

        int text_check_w = 0;

        int incr_text_size = 25;
        boolean found_desired_size = true;

        while (found_desired_size){
            paint.setTextSize(incr_text_size);// have this the same as your text size

            paint.getTextBounds(text, 0, text.length(), bounds);

            text_check_w =  bounds.width();
            incr_text_size--;

            if (text_width > text_check_w){
                found_desired_size = false;
            }
        }
        return incr_text_size;
    }

    private void vulMaand(Maand maand, int jaar) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.MONTH, maand.getNr() - 1);
        cal.set(Calendar.YEAR, jaar);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        werkData = Reader.getWerkData(maand, jaar);
        wisselData = Reader.getWisselData(maand, jaar);
        persoonlijkData = Reader.getPersoonlijkData(maand, jaar);

        Map<String, List<String>> shiften = Reader.getShiften();

        TextView lblMaand = (TextView) findViewById(R.id.lblMaand);

        int intColor;
        try {
            intColor = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_CALENDAR));
            (findViewById(R.id.KalenderForm)).setBackgroundColor(intColor);
        } catch (Exception e) {
        }

        try {
            intColor = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_CALENDAR_HEADER));
            ((GradientDrawable) lblMaand.getBackground()).setColor(intColor);
        } catch (Exception e) { }


        try {
            intColor = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_WEEKDAYS));
            ((GradientDrawable) findViewById(R.id.header).getBackground()).setColor(intColor);
        } catch (Exception e) { }


        TextView lblHeadMA = (TextView) findViewById(R.id.lblHeadMA);
        TextView lblHeadDI = (TextView) findViewById(R.id.lblHeadDI);
        TextView lblHeadWO = (TextView) findViewById(R.id.lblHeadWO);
        TextView lblHeadDO = (TextView) findViewById(R.id.lblHeadDO);
        TextView lblHeadVR = (TextView) findViewById(R.id.lblHeadVR);
        TextView lblHeadZA = (TextView) findViewById(R.id.lblHeadZA);
        TextView lblHeadZO = (TextView) findViewById(R.id.lblHeadZO);

        try {
            intColor = Integer.parseInt(voorkeuren.get(Voorkeur.TEXTCOLOR_TITELS));
            lblMaand.setTextColor(intColor);
            lblHeadMA.setTextColor(intColor);
            lblHeadDI.setTextColor(intColor);
            lblHeadWO.setTextColor(intColor);
            lblHeadDO.setTextColor(intColor);
            lblHeadVR.setTextColor(intColor);
            lblHeadZA.setTextColor(intColor);
            lblHeadZO.setTextColor(intColor);
        } catch (Exception e) {
        }

        lblMaand.setText(maand.toString(this) + " " + String.valueOf(jaar));

        int firstDayOfWeekThisMonth;

        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                firstDayOfWeekThisMonth = 1;
                break;
            case Calendar.TUESDAY:
                firstDayOfWeekThisMonth = 2;
                break;
            case Calendar.WEDNESDAY:
                firstDayOfWeekThisMonth = 3;
                break;
            case Calendar.THURSDAY:
                firstDayOfWeekThisMonth = 4;
                break;
            case Calendar.FRIDAY:
                firstDayOfWeekThisMonth = 5;
                break;
            case Calendar.SATURDAY:
                firstDayOfWeekThisMonth = 6;
                break;
            case Calendar.SUNDAY:
                firstDayOfWeekThisMonth = 7;
                break;
            default:
                firstDayOfWeekThisMonth = 1;
                break;
        }

        firstDayOfWeekThisMonth -= firstDay;
        if (firstDayOfWeekThisMonth < 1){
            firstDayOfWeekThisMonth += 7;
        }

        int eersteDag = firstDay;
        eersteDag += 2;
        if (eersteDag >= 8){
            eersteDag -= 7;
        }
        lblHeadMA.setText(Methodes.getDagVerkort(eersteDag++, this));
        lblHeadDI.setText(Methodes.getDagVerkort(eersteDag++, this));
        lblHeadWO.setText(Methodes.getDagVerkort(eersteDag++, this));
        lblHeadDO.setText(Methodes.getDagVerkort(eersteDag++, this));
        lblHeadVR.setText(Methodes.getDagVerkort(eersteDag++, this));
        lblHeadZA.setText(Methodes.getDagVerkort(eersteDag++, this));
        lblHeadZO.setText(Methodes.getDagVerkort(eersteDag, this));

        boolean firstDayFound = false;
        int huidigeDag = 1;
        int volgendeMaand = 1;

        for (int i = 1; i <= 42; i++) {
            final int panelNr = i;
            final int dagNr = huidigeDag;

            Resources res = getResources();
            int id = res.getIdentifier("dag" + i, "id", this.getPackageName());
            ViewGroup llMain = (ViewGroup) findViewById(id);
            //Kalender
            LayoutInflater kalenderInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View kalenderView = kalenderInflater.inflate(R.layout.dag_layout, null);
            kalenderView.measure(View.MeasureSpec.makeMeasureSpec(llMain.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(llMain.getHeight(), View.MeasureSpec.EXACTLY));
            kalenderView.layout(0, 0, kalenderView.getMeasuredWidth(), kalenderView.getMeasuredHeight());
            ((TextView) kalenderView.findViewById(R.id.lblHeaderDag)).setText(String.valueOf(i));

            final LinearLayout pnlDagInhoud = (LinearLayout) kalenderView.findViewById(R.id.pnlInhoudDag);

            TextView lblHeaderDag = (TextView) kalenderView.findViewById(R.id.lblHeaderDag);
            lblHeaderDag.setTextSize(intTextSize);
            final TextView lblWerkData = (TextView) kalenderView.findViewById(R.id.lblInhoudDagWerk);
            lblWerkData.setText("");
            final TextView lblPersoonlijkData = (TextView) kalenderView.findViewById(R.id.lblInhoudDagPersoonlijk);

            lblPersoonlijkData.setText("");
            lblPersoonlijkData.setVisibility(View.VISIBLE);
            lblWerkData.setTextSize(intTextSize);
            lblPersoonlijkData.setTextSize(intTextSize);

            // fill in any details dynamically here
            llMain.removeAllViews();
            llMain.addView(kalenderView);

            final int maxDagenInHuidigeMaand = Methodes.getMaxDagenInMaand(maand.getNr(), jaar);
            final int maxDagenInVorigeMaand = Methodes.getMaxDagenInMaand(maand.getNr() - 1, jaar);

            if (firstDayOfWeekThisMonth == i) {
                firstDayFound = true;
            }

            if (firstDayFound) {
                if (huidigeDag > maxDagenInHuidigeMaand) {
                    lblHeaderDag.setText(String.valueOf(volgendeMaand++));

                    pnlDagInhoud.setClickable(false);
                    try {
                        int kleur = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_DAYS));
                        ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setColor(kleur);
                        ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setAlpha(210);
                        ((GradientDrawable) lblHeaderDag.getBackground()).setAlpha(100);
                    } catch (Exception e) {
                        ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setColor(Color.GRAY);
                    }
                    ((GradientDrawable) lblHeaderDag.getBackground()).setColor(Color.GRAY);
                } else {
                    Calendar currCal = Calendar.getInstance();
                    if (huidigeDag == currCal.get(Calendar.DAY_OF_MONTH) && (maand.getNr() - 1) == (int) currCal.get(Calendar.MONTH) && jaar == currCal.get(Calendar.YEAR)) {
                        lblHeaderDag.setText("*" + String.valueOf(huidigeDag) + "*");
                    } else {
                        lblHeaderDag.setText(String.valueOf(huidigeDag));
                    }

                    cal.set(Calendar.DAY_OF_MONTH, huidigeDag);
                    int currDagInWeek = cal.get(Calendar.DAY_OF_WEEK);

                    String shift = geefDataWerk(huidigeDag, true);

                    List<String> shiftData = shiften.get(shift);

                    String activiteit = geefDataPersoonlijk(huidigeDag);

                    lblWerkData.setText(shift);

                    if (!activiteit.equals("")) {
                        if (showPersonalNotes){
                            lblPersoonlijkData.setText(activiteit);
                        }else{
                            lblPersoonlijkData.setText(alternatif);
                        }
                    }

                    huidigeDag++;

                    int kleur;
                    try {
                        kleur = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_DAYS_HEADER));
                        ((GradientDrawable) lblHeaderDag.getBackground()).setColor(kleur);
                    } catch (Exception e) {
                        ((GradientDrawable) lblHeaderDag.getBackground()).setColor(Color.GRAY);
                    }
                    try {
                        kleur = Integer.parseInt(voorkeuren.get(Voorkeur.TEXTCOLOR_DAY_HEADER));
                        lblHeaderDag.setTextColor(kleur);
                    } catch (Exception e) {
                    }
                    try {
                        kleur = Integer.parseInt(voorkeuren.get(Voorkeur.TEXTCOLOR_DAY_CONTENT));
                        lblWerkData.setTextColor(kleur);
                        lblPersoonlijkData.setTextColor(kleur);
                    } catch (Exception e) {
                    }

                    if (!useBackgroundShiftChange){
                        hasWissel = false;
                    }

                    if (hasWissel) {
                        try {
                            kleur = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_SHIFT_CHANGE));
                            ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setColor(kleur);
                        } catch (Exception e) {
                            ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setColor(Color.WHITE);
                        }
                    } else {
                        try {
                            kleur = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_DAYS));
                            ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setColor(kleur);
                        } catch (Exception e) {
                            ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setColor(Color.LTGRAY);
                        }

                        if (useBackgroundShift){
                            if (shiftData != null){
                                kleur = Integer.parseInt(shiftData.get(5));
                                if (kleur != 0){
                                    try{
                                        ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setColor(kleur);
                                    }catch (Exception e) { }
                                }
                            }
                        }
                    }
                }
            } else {
                pnlDagInhoud.setClickable(false);
                int vorige = maxDagenInVorigeMaand - (firstDayOfWeekThisMonth - 1) + panelNr;
                lblHeaderDag.setText(String.valueOf(vorige));
                try {
                    int kleur = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_DAYS));
                    ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setColor(kleur);
                    ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setAlpha(210);
                    ((GradientDrawable) lblHeaderDag.getBackground()).setAlpha(100);
                } catch (Exception e) {
                    ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setColor(Color.GRAY);
                }
                ((GradientDrawable) lblHeaderDag.getBackground()).setColor(Color.GRAY);
            }
        }
    }

    @Override
    public void onBackPressed() {
        datePicked = false;
        super.onBackPressed();
    }
}