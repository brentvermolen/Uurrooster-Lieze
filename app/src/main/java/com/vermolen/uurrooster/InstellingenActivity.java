package com.vermolen.uurrooster;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.vermolen.uurrooster.Classes.AmbilWarnaDialog;
import com.vermolen.uurrooster.Classes.CalendarSingletons;
import com.vermolen.uurrooster.Classes.Enums.Maand;
import com.vermolen.uurrooster.Classes.Enums.Type;
import com.vermolen.uurrooster.Classes.Enums.Voorkeur;
import com.vermolen.uurrooster.Classes.FirstTimeDialog;
import com.vermolen.uurrooster.Classes.Methodes;
import com.vermolen.uurrooster.Classes.OnSwipeTouchListener;
import com.vermolen.uurrooster.Classes.Reader;
import com.vermolen.uurrooster.Classes.TextReader;
import com.vermolen.uurrooster.Classes.TextWriter;
import com.vermolen.uurrooster.Classes.UserSingleton;
import com.vermolen.uurrooster.Classes.Writer;
import com.vermolen.uurrooster.Model.User;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstellingenActivity extends AppCompatActivity {
    User USER;

    LinearLayout llLayOut;

    ProgressBar prgLoading;

    TextView lblNaam;
    TextView lblGebDatum;
    TextView lblAdres;
    TextView lblWoonplaats;
    EditText txtNaam;
    EditText txtGeboortedatum;
    EditText txtAdres;
    EditText txtWoonplaats;

    CheckBox chkStartupScreen;
    CheckBox chkBackgroundShiftChange;
    CheckBox chkBackgroundShift;
    CheckBox chkShowPersonal;
    EditText txtAlternatief;
    LinearLayout llAlternatief;
    Spinner cboFirstDay;
    TextView lblPreviewTextSize;
    SeekBar skbTextSize;

    ImageButton btnOpslaan;
    ImageButton btnEdit;
    ImageButton btnPreview;
    boolean isCalShown;

    LinearLayout llCalPreview;

    TextView lblKalender;
    Button btnKiesKalender;

    private Map<Voorkeur, String> voorkeuren;
    private ArrayAdapter<String> days;

    private TabHost host;

    int firstDay;
    private String alternatif;
    private boolean useBackgroundShiftChange;
    private boolean useBackgroundShift;
    private int intTextSize;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instellingen);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.toolbar_delete_edit_save);
        Toolbar parent = (Toolbar) getSupportActionBar().getCustomView().getParent();
        ((TextView) getSupportActionBar().getCustomView().findViewById(R.id.lblToolbarDeleteEditSaveTitel)).setText(getResources().getString(R.string.settings));
        parent.setPadding(0,0,0,0);
        parent.setContentInsetsAbsolute(0, 0);

        USER = UserSingleton.getInstance();

        initViews();
        handleEvents();

        sharedPref = getApplicationContext().getSharedPreferences("Settings", MODE_PRIVATE);

        loadSettings();

        initVoorkeuren();
    }

    private void loadSettings() {
        chkStartupScreen.setChecked(sharedPref.getBoolean("startupScreen", false));
        useBackgroundShiftChange = sharedPref.getBoolean("backgroundShiftChange", true);
        chkBackgroundShiftChange.setChecked(useBackgroundShiftChange);
        useBackgroundShift = sharedPref.getBoolean("backgroundShift", true);
        chkBackgroundShift.setChecked(useBackgroundShift);
        chkShowPersonal.setChecked(sharedPref.getBoolean("showPersonalNotes", true));
        alternatif = sharedPref.getString("alternatief", getResources().getString(R.string.notes));
        txtAlternatief.setText(alternatif);
        firstDay = sharedPref.getInt("firstDay", 0);
        cboFirstDay.setSelection(firstDay);
        intTextSize = sharedPref.getInt("textSize", 10);
        skbTextSize.setProgress(intTextSize);
        lblPreviewTextSize.setTextSize(intTextSize);
    }

    private void checkFirstTime(final String tab) {
        final SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        boolean first = sharedPref.getBoolean("first" + tab, true);
        if (first){
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.tutorial));

            Map<String, String> messages = new HashMap<>();
            if (tab.equals(getResources().getString(R.string.profile))){
                messages.put(getResources().getString(R.string.profile), getResources().getString(R.string.messageInstellingen1));
            }else{
                if (tab.equals(getResources().getString(R.string.settings))){
                    messages.put(getResources().getString(R.string.settings), getResources().getString(R.string.messageInstellingen2));
                }else {
                    if (tab.equals(getResources().getString(R.string.lay_out))){
                        messages.put(getResources().getString(R.string.lay_out), getResources().getString(R.string.messageInstellingen3));
                    }
                }
            }

            FirstTimeDialog dialog = new FirstTimeDialog(this, messages, sharedPref, true);
            dialog.show();
        }
    }

    private void initVoorkeuren() {
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                prgLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                voorkeuren = Reader.getVoorkeuren();

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Voorkeur[] voorkeurViews = new Voorkeur[] {
                    Voorkeur.BACKGROUND_CALENDAR,
                    Voorkeur.BACKGROUND_CALENDAR_HEADER,
                    Voorkeur.BACKGROUND_WEEKDAYS,
                    Voorkeur.BACKGROUND_DAYS_HEADER,
                    Voorkeur.BACKGROUND_DAYS,
                    Voorkeur.BACKGROUND_SHIFT_CHANGE,
                    Voorkeur.TEXTCOLOR_TITELS,
                    Voorkeur.TEXTCOLOR_DAY_HEADER,
                    Voorkeur.TEXTCOLOR_DAY_CONTENT
            };

                llLayOut.removeAllViews();

                for (final Voorkeur voorkeur : voorkeurViews){
                    LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View v = vi.inflate(R.layout.wijzig_voorkeur_layout, null);

                    TextView lblHeader = (TextView) v.findViewById(R.id.lblVoorkeurHeader);
                    final LinearLayout llPanel = (LinearLayout) v.findViewById(R.id.pnlVoorkeurAchtergrond);

                    Button btnWijzig = (Button) v.findViewById(R.id.btnVoorkeurWijzigen);
                    btnWijzig.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int kleur;
                            try{
                                kleur = Integer.parseInt(Reader.getVoorkeuren().get(voorkeur));
                            }catch (Exception ex){
                                kleur = Color.BLACK;
                            }
                            AmbilWarnaDialog dialog = new AmbilWarnaDialog(InstellingenActivity.this, kleur, false, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                                @Override
                                public void onCancel(AmbilWarnaDialog dialog) { }
                                @Override
                                public void onOk(AmbilWarnaDialog dialog, final int color) {
                                    llPanel.setBackgroundColor(color);
                                    new AsyncTask<Void, Void, Void>(){
                                        @Override
                                        protected Void doInBackground(Void... voids) {
                                            Writer.writeVoorkeur(voorkeur, String.valueOf(color));
                                            return null;
                                        }
                                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    CalendarSingletons.editVoorkeur(voorkeur, String.valueOf(color));
                                    initVoorkeuren();
                                }
                                @Override
                                public void onNeutral(AmbilWarnaDialog dialog) {
                                    if (voorkeur.toString().toLowerCase().startsWith("textcolor")){
                                        llPanel.setBackgroundColor(Color.BLACK);
                                        new AsyncTask<Void, Void, Void>(){
                                            @Override
                                            protected Void doInBackground(Void... voids) {
                                                Writer.writeVoorkeur(voorkeur, String.valueOf(Color.BLACK));
                                                return null;
                                            }
                                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                        CalendarSingletons.editVoorkeur(voorkeur, String.valueOf(Color.BLACK));
                                    }else{
                                        llPanel.setBackgroundColor(Color.TRANSPARENT);
                                        new AsyncTask<Void, Void, Void>(){
                                            @Override
                                            protected Void doInBackground(Void... voids) {
                                                Writer.writeVoorkeur(voorkeur, "EMPTY");
                                                return null;
                                            }
                                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                        CalendarSingletons.editVoorkeur(voorkeur, "EMPTY");
                                    }

                                    initVoorkeuren();
                                }
                            }, getResources().getString(R.string.delete), true);
                            dialog.show();
                        }
                    });

                    lblHeader.setText(voorkeur.toString(InstellingenActivity.this));

                    try{
                        ((GradientDrawable)llPanel.getBackground()).setColor(Integer.parseInt(voorkeuren.get(voorkeur)));
                    }catch (Exception e){
                        if (voorkeur.toString().toLowerCase().startsWith("textcolor")){
                            ((GradientDrawable)llPanel.getBackground()).setColor(Color.BLACK);
                        }else{
                            ((GradientDrawable)llPanel.getBackground()).setColor(Color.TRANSPARENT);
                        }
                    }

                    llLayOut.addView(v);
                }

                prgLoading.setVisibility(View.GONE);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setEdit(){
        txtNaam.setText(lblNaam.getText().toString());
        txtGeboortedatum.setText(lblGebDatum.getText().toString());
        txtAdres.setText(lblAdres.getText().toString());
        txtWoonplaats.setText(lblWoonplaats.getText().toString());


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            txtGeboortedatum.setVisibility(View.VISIBLE);
            lblGebDatum.setVisibility(View.GONE);
        }
        lblGebDatum.setTextColor(Color.BLACK);

        txtNaam.setVisibility(View.VISIBLE);
        txtAdres.setVisibility(View.VISIBLE);
        txtWoonplaats.setVisibility(View.VISIBLE);
        lblNaam.setVisibility(View.GONE);
        lblAdres.setVisibility(View.GONE);
        lblWoonplaats.setVisibility(View.GONE);
    }

    private void setSaved(){
        lblNaam.setText(txtNaam.getText().toString());
        lblGebDatum.setText(txtGeboortedatum.getText().toString());
        lblAdres.setText(txtAdres.getText().toString());
        lblWoonplaats.setText(txtWoonplaats.getText().toString());

        txtGeboortedatum.setVisibility(View.GONE);
        lblGebDatum.setVisibility(View.VISIBLE);
        lblGebDatum.setTextColor(lblNaam.getCurrentTextColor());

        txtNaam.setVisibility(View.GONE);
        txtAdres.setVisibility(View.GONE);
        txtWoonplaats.setVisibility(View.GONE);
        lblNaam.setVisibility(View.VISIBLE);
        lblAdres.setVisibility(View.VISIBLE);
        lblWoonplaats.setVisibility(View.VISIBLE);
    }

    private void initViews() {
        View view = getSupportActionBar().getCustomView();
        view.findViewById(R.id.btnDelete).setVisibility(View.GONE);

        prgLoading = (ProgressBar) findViewById(R.id.prgLoading);

        btnOpslaan = (ImageButton) view.findViewById(R.id.btnOpslaan);
        btnOpslaan.setVisibility(View.GONE);
        btnEdit = (ImageButton) view.findViewById(R.id.btnEdit);
        btnEdit.setVisibility(View.VISIBLE);
        btnPreview = (ImageButton) view.findViewById(R.id.btnPreview);
        btnPreview.setVisibility(View.GONE);
        isCalShown = false;

        llLayOut = (LinearLayout) findViewById(R.id.llVoorkeurenInhoud);

        lblNaam = (TextView) findViewById(R.id.lblProfielUser);
        lblNaam.setText(USER.getUsername());
        lblGebDatum = (TextView) findViewById(R.id.lblProfielGebDatum);
        lblAdres = (TextView) findViewById(R.id.lblProfielAdres);
        lblWoonplaats = (TextView) findViewById(R.id.lblProfielWoonplaats);

        txtNaam = (EditText) findViewById(R.id.txtProfielUser);
        txtGeboortedatum = (EditText) findViewById(R.id.txtProfielGebDatum);
        txtAdres = (EditText) findViewById(R.id.txtProfielAdres);
        txtWoonplaats = (EditText) findViewById(R.id.txtProfielWoonplaats);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            txtGeboortedatum.setVisibility(View.VISIBLE);
            lblGebDatum.setVisibility(View.GONE);
        }else{
            txtGeboortedatum.setVisibility(View.GONE);
            lblGebDatum.setVisibility(View.VISIBLE);
        }

        chkStartupScreen = (CheckBox) findViewById(R.id.chkStartupScreen);
        chkBackgroundShiftChange = (CheckBox) findViewById(R.id.chkBackgroundShiftChange);
        chkBackgroundShift = (CheckBox) findViewById(R.id.chkBackgroundShift);
        chkShowPersonal = (CheckBox) findViewById(R.id.chkShowPersonal);
        txtAlternatief = (EditText) findViewById(R.id.txtAlternatief);
        llAlternatief = (LinearLayout) findViewById(R.id.llAlternatief);
        cboFirstDay = (Spinner) findViewById(R.id.cboFirstDay);
        lblPreviewTextSize = (TextView) findViewById(R.id.lblPreviewSize);
        skbTextSize = (SeekBar) findViewById(R.id.skbTextSize);

        lblKalender = findViewById(R.id.lblKalender);
        btnKiesKalender = findViewById(R.id.btnKiesKalender);
        if (CalendarSingletons.sharedPreferencesCalendar.getBoolean("synced", false)){
            lblKalender.setText(CalendarSingletons.sharedPreferencesCalendar.getString("calendar", ""));
        }

        days = new ArrayAdapter<>(this, R.layout.spinner_item);
        for (int i = 2; i <= 7; i++){
            days.add(Methodes.getDag(i, this));
        }
        days.add(Methodes.getDag(1, this));
        cboFirstDay.setAdapter(days);

        llCalPreview = (LinearLayout) findViewById(R.id.llCalPreview);

        host = (TabHost)findViewById(R.id.tabHost);

        host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                btnEdit.setVisibility(View.GONE);
                btnPreview.setVisibility(View.GONE);
                btnOpslaan.setVisibility(View.GONE);

                if (tabId.equals(getResources().getString(R.string.profile))){
                    btnEdit.setVisibility(View.VISIBLE);
                }else{
                    if (btnOpslaan.getVisibility() == View.VISIBLE){
                        host.setCurrentTab(0);
                        Toast.makeText(InstellingenActivity.this, getResources().getString(R.string.toastCannotChangeWhileEdit), Toast.LENGTH_LONG).show();
                        btnOpslaan.setVisibility(View.VISIBLE);
                    }else{
                        if (tabId.equals(getResources().getString(R.string.lay_out)) || tabId.equals(getResources().getString(R.string.settings))){
                            btnPreview.setVisibility(View.VISIBLE);
                        }
                    }
                }

                //checkFirstTime(tabId);
            }
        });

        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec(getResources().getString(R.string.profile));
        spec.setContent(R.id.Profiel);
        spec.setIndicator(getResources().getString(R.string.profile));
        host.addTab(spec);
        host.setTag(R.id.Profiel, getResources().getString(R.string.profile));

        //Tab 3
        spec = host.newTabSpec(getResources().getString(R.string.settings));
        spec.setContent(R.id.Settings);
        spec.setIndicator(getResources().getString(R.string.settings));
        host.addTab(spec);
        host.setTag(R.id.Settings, getResources().getString(R.string.settings));

        //Tab 2
        spec = host.newTabSpec(getResources().getString(R.string.lay_out));
        spec.setContent(R.id.LayOut);
        spec.setIndicator(getResources().getString(R.string.lay_out));
        host.addTab(spec);
        host.setTag(R.id.LayOut, getResources().getString(R.string.lay_out));
    }

    private void handleEvents() {
        ((ImageButton) findViewById(R.id.imgBackButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btnOpslaan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String gebDatum = txtGeboortedatum.getText().toString()
                        .replace("-", "/").replace(".", "/");
                String[] split = gebDatum.split("/");

                int dag;
                int maand;
                int jaar;

                try{
                    dag = Integer.parseInt(split[0]);
                    maand = Integer.parseInt(split[1]);
                    jaar = Integer.parseInt(split[2]);
                    if (jaar < 100){
                        jaar += 1900;
                    }
                }catch (Exception e){
                    Toast.makeText(InstellingenActivity.this, getResources().getString(R.string.toastEnterValidDoB), Toast.LENGTH_LONG).show();
                    return;
                }
                if (jaar > Calendar.getInstance().get(Calendar.YEAR) || jaar < 1901){
                    Toast.makeText(InstellingenActivity.this, getResources().getString(R.string.toastEnterValidDoB), Toast.LENGTH_LONG).show();
                    return;
                }

                if (maand < 1 || maand > 12){
                    Toast.makeText(InstellingenActivity.this, getResources().getString(R.string.toastEnterValidDoB), Toast.LENGTH_LONG).show();
                    return;
                }

                int max = Methodes.getMaxDagenInMaand(maand, jaar);
                if (dag < 1 || dag > max){
                    Toast.makeText(InstellingenActivity.this, getResources().getString(R.string.toastEnterValidDoB), Toast.LENGTH_LONG).show();
                    return;
                }

                txtGeboortedatum.setText(String.format("%02d/%02d/%04d", dag, maand, jaar));

                //TODO
                //Writer.writeProfiel(txtGeboortedatum.getText().toString(), txtAdres.getText().toString(),
                //        txtWoonplaats.getText().toString());

                SharedPreferences.Editor editor = getSharedPreferences("USER", Context.MODE_PRIVATE).edit();
                editor.putString("USER", txtNaam.getText().toString());
                editor.apply();
                editor.commit();

                host.setEnabled(true);
                btnEdit.setVisibility(View.VISIBLE);
                btnOpslaan.setVisibility(View.GONE);
                setSaved();
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                host.setEnabled(false);
                btnEdit.setVisibility(View.GONE);
                btnOpslaan.setVisibility(View.VISIBLE);
                setEdit();
            }
        });

        btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCalShown){
                    host.setVisibility(View.VISIBLE);
                    findViewById(R.id.calendar).setVisibility(View.GONE);
                    btnPreview.setImageResource(R.drawable.preview);
                    isCalShown = false;
                }else{
                    host.setVisibility(View.GONE);
                    findViewById(R.id.calendar).setVisibility(View.VISIBLE);
                    btnPreview.setImageResource(R.drawable.no_preview);
                    isCalShown = true;
                    vulKalender();
                }
            }
        });

        lblGebDatum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnOpslaan.getVisibility() == View.VISIBLE){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        final DatePicker datePicker = new DatePicker(InstellingenActivity.this);

                        AlertDialog.Builder builder = new AlertDialog.Builder(InstellingenActivity.this);

                        builder.setView(datePicker);

                        builder.setCancelable(true).setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int day = datePicker.getDayOfMonth();
                                int month = datePicker.getMonth();
                                int jaar = datePicker.getYear();

                                String gebDatum = String.format("%02d/%02d/%04d", day, month, jaar);
                                txtGeboortedatum.setText(gebDatum);
                                lblGebDatum.setText(gebDatum);
                            }
                        });

                        builder.show();
                    }
                }
            }
        });

        chkStartupScreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final boolean checked = isChecked;
                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {

                        SharedPreferences.Editor editor = sharedPref.edit();

                        editor.putBoolean("startupScreen", checked);

                        editor.apply();
                        editor.commit();

                        return null;
                    }
                };
            }
        });

        chkBackgroundShiftChange.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final boolean checked = isChecked;

                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {

                        SharedPreferences.Editor editor = sharedPref.edit();

                        editor.putBoolean("backgroundShiftChange", checked);

                        editor.apply();
                        editor.commit();

                        return null;
                    }
                };

                task.execute();
            }
        });

        chkBackgroundShift.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final boolean checked = isChecked;

                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {

                        SharedPreferences.Editor editor = sharedPref.edit();

                        editor.putBoolean("backgroundShift", checked);

                        editor.apply();
                        editor.commit();

                        return null;
                    }
                };

                task.execute();
            }
        });
        chkShowPersonal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final boolean checked = isChecked;

                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {

                        SharedPreferences.Editor editor = sharedPref.edit();

                        editor.putBoolean("showPersonalNotes", checked);

                        editor.apply();
                        editor.commit();

                        return null;
                    }
                };

                task.execute();

                if (isChecked){
                    llAlternatief.setVisibility(View.GONE);
                }else{
                    llAlternatief.setVisibility(View.VISIBLE);
                }
            }
        });
        txtAlternatief.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                final String tekst = txtAlternatief.getText().toString();

                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {

                        SharedPreferences.Editor editor = sharedPref.edit();

                        if (tekst.equals("")){
                            editor.putString("alternatief", getResources().getString(R.string.notes));
                        }else {
                            editor.putString("alternatief", tekst);
                        }

                        editor.apply();
                        editor.commit();

                        return null;
                    }
                };

                task.execute();
            }
        });
        cboFirstDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final int pos = position;

                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {

                        SharedPreferences.Editor editor = sharedPref.edit();

                        editor.putInt("firstDay", pos);
                        editor.apply();
                        editor.commit();

                        return null;
                    }
                };

                task.execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        skbTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 5){
                    progress = 5;
                }
                lblPreviewTextSize.setTextSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = sharedPref.edit();

                int progress = seekBar.getProgress();
                if (progress < 5){
                    progress = 5;
                }
                editor.putInt("textSize", progress);

                editor.apply();
                editor.commit();
            }
        });

        btnKiesKalender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor cur = Methodes.getAllCalendars(InstellingenActivity.this);

                final int PROJECTION_ID_INDEX = 0;
                final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
                final int PROJECTION_DISPLAY_NAME_INDEX = 2;
                final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

                final Map<Long, String> calendars = new HashMap<>();

                while (cur.moveToNext()){
                    long calID = 0;
                    String displayName = null;
                    String accountName = null;
                    String ownerName = null;

                    // Get the field values
                    calID = cur.getLong(PROJECTION_ID_INDEX);
                    displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
                    accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
                    ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);

                    calendars.put(calID, displayName);
                }

                ListView lst = new ListView(InstellingenActivity.this);

                final List<String> calNames = new ArrayList<String>(calendars.values());
                lst.setAdapter(new ArrayAdapter<String>(InstellingenActivity.this, android.R.layout.simple_list_item_1, calNames));

                lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        for (Long id : calendars.keySet()){
                            if (calendars.get(id).equals(calNames.get(i))){

                                CalendarSingletons.sharedPreferencesCalendar.edit().putBoolean("synced", true).apply();
                                CalendarSingletons.sharedPreferencesCalendar.edit().putString("calendar", calNames.get(i)).apply();
                                CalendarSingletons.sharedPreferencesCalendar.edit().putLong("cal_id", id).apply();
                                CalendarSingletons.sharedPreferencesCalendar.edit().commit();
                                lblKalender.setText(calNames.get(i));
                                break;
                            }
                        }
                    }
                });

                new AlertDialog.Builder(InstellingenActivity.this)
                        .setView(lst)
                        .show();
            }
        });
    }

    private void vulKalender(){
        llCalPreview.removeAllViews();
        loadSettings();

        LayoutInflater inflator = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        LinearLayout llCalender = (LinearLayout) inflator.inflate(R.layout.month_widget, llCalPreview);

        TextView lblMaand = (TextView) findViewById(R.id.lblMaand);

        Calendar cal = Calendar.getInstance();

        Maand maand = Maand.valueOf(cal.get(Calendar.MONTH) + 1);
        int jaar = cal.get(Calendar.YEAR);

        int intColor;
        try {
            intColor = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_CALENDAR));
            ((LinearLayout) findViewById(R.id.calendar)).setBackgroundColor(intColor);
        } catch (Exception e) {

        }

        try {
            intColor = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_CALENDAR_HEADER));
            ((GradientDrawable) lblMaand.getBackground()).setColor(intColor);
        } catch (Exception e) {
            ((GradientDrawable) lblMaand.getBackground()).setColor(Color.TRANSPARENT);
        }


        try {
            intColor = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_WEEKDAYS));
            ((GradientDrawable) llCalender.findViewById(R.id.header).getBackground()).setColor(intColor);
        } catch (Exception e) {
            ((GradientDrawable) llCalender.findViewById(R.id.header).getBackground()).setColor(Color.TRANSPARENT);
        }

        TextView lblHeadMA = (TextView) llCalender.findViewById(R.id.lblHeadMA);
        TextView lblHeadDI = (TextView) llCalender.findViewById(R.id.lblHeadDI);
        TextView lblHeadWO = (TextView) llCalender.findViewById(R.id.lblHeadWO);
        TextView lblHeadDO = (TextView) llCalender.findViewById(R.id.lblHeadDO);
        TextView lblHeadVR = (TextView) llCalender.findViewById(R.id.lblHeadVR);
        TextView lblHeadZA = (TextView) llCalender.findViewById(R.id.lblHeadZA);
        TextView lblHeadZO = (TextView) llCalender.findViewById(R.id.lblHeadZO);

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

        lblMaand.setText(getResources().getString(R.string.month) + " " + getResources().getString(R.string.preview));
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

        //eersteDag = 0; //maandag
        firstDayOfWeekThisMonth -= firstDay;
        if (firstDayOfWeekThisMonth < 1) {
            firstDayOfWeekThisMonth += 7;
        }

        int eersteDag = firstDay;
        eersteDag += 2;
        if (eersteDag >= 8) {
            eersteDag -= 7;
        }

        lblHeadMA.setText(Methodes.getDagVerkort(eersteDag++, this));
        lblHeadDI.setText(Methodes.getDagVerkort(eersteDag++, this));
        lblHeadWO.setText(Methodes.getDagVerkort(eersteDag++, this));
        lblHeadDO.setText(Methodes.getDagVerkort(eersteDag++, this));
        lblHeadVR.setText(Methodes.getDagVerkort(eersteDag++, this));
        lblHeadZA.setText(Methodes.getDagVerkort(eersteDag++, this));
        lblHeadZO.setText(Methodes.getDagVerkort(eersteDag, this));

        LinearLayout llRow1 = (LinearLayout) llCalender.findViewById(R.id.row1);
        LinearLayout llRowLast = (LinearLayout) llCalender.findViewById(R.id.row6);

        if (firstDayOfWeekThisMonth == 1) {
            firstDayOfWeekThisMonth = 8;
        }

        boolean firstDayFound = false;
        int huidigeDag = 1;
        int volgendeMaand = 1;

        for (int i = 1; i <= 42; i++) {
            final int panelNr = i;
            final int dagNr = huidigeDag;

            Resources res = getResources();
            int id = res.getIdentifier("dag" + i, "id", InstellingenActivity.this.getPackageName());

            //Kalender
            LinearLayout kalenderView = (LinearLayout) inflator.inflate(R.layout.dag_layout, null);

            // fill in any details dynamically here
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


            // insert into main view
            ViewGroup llMain = (ViewGroup) llCalender.findViewById(id);
            llMain.addView(kalenderView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            final int maxDagenInHuidigeMaand = Methodes.getMaxDagenInMaand(maand.getNr(), jaar);
            final int maxDagenInVorigeMaand = Methodes.getMaxDagenInMaand(maand.getNr() - 1, jaar);

            if (firstDayOfWeekThisMonth == i) {
                firstDayFound = true;
            }

            if (firstDayFound) {
                if (huidigeDag > maxDagenInHuidigeMaand) {
                    if (i == 36){
                        llRowLast.setVisibility(View.GONE);
                    }

                    lblHeaderDag.setText(String.valueOf(volgendeMaand++));

                    pnlDagInhoud.setClickable(false);
                    try {
                        int kleur = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_DAYS));
                        ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setColor(kleur);
                        ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setAlpha(100);
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
                    String activiteit = "";

                    switch (dagNr){
                        case 4:
                        case 13:
                        case 17:
                        case 24:
                        activiteit = alternatif;
                    }

                    if (!activiteit.equals("")) {
                        lblPersoonlijkData.setText(activiteit);
                    }

                    boolean hasWissel = false;

                    switch (dagNr){
                        case 5:
                        case 9:
                        case 13:
                        case 17:
                        case 26:
                            hasWissel = true;
                    }

                    String shift = "";
                    if (hasWissel){
                        shift = getResources().getString(R.string.change).substring(0, 1).toUpperCase();
                    }else{
                        shift = getResources().getString(R.string.shift).substring(0, 1).toUpperCase();
                    }
                    lblWerkData.setText(shift);

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

                    if (!useBackgroundShiftChange) {
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

                        if (useBackgroundShift) {
                            List<String> shiftData = Reader.getShiften().get(shift);
                            if (shiftData != null) {
                                kleur = Integer.parseInt(shiftData.get(5));
                                if (kleur != 0) {
                                    try {
                                        ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setColor(kleur);
                                    } catch (Exception e) {
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (i == 7){
                    llRow1.setVisibility(View.GONE);
                }

                pnlDagInhoud.setClickable(false);
                int vorige = maxDagenInVorigeMaand - (firstDayOfWeekThisMonth - 1) + panelNr;
                lblHeaderDag.setText(String.valueOf(vorige));
                try {
                    int kleur = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_DAYS));
                    ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setColor(kleur);
                    ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setAlpha(100);
                } catch (Exception e) {
                    ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setColor(Color.GRAY);
                }
                ((GradientDrawable) lblHeaderDag.getBackground()).setColor(Color.GRAY);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isCalShown){
            btnPreview.performClick();
            return;
        }

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Methodes.updateWidgets(InstellingenActivity.this, getApplication());
                return null;
            }
        };
        task.execute();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Methodes.updateWidgets(InstellingenActivity.this, getApplication());
                return null;
            }
        };
        task.execute();
        super.onDestroy();
    }
}
