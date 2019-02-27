package com.vermolen.uurrooster;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.vermolen.uurrooster.Classes.AmbilWarnaDialog;
import com.vermolen.uurrooster.Classes.DirResSingleton;
import com.vermolen.uurrooster.Classes.FirstTimeDialog;
import com.vermolen.uurrooster.Classes.Methodes;
import com.vermolen.uurrooster.Classes.ObjTimePicker;
import com.vermolen.uurrooster.Classes.Reader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NieuweShiftActivity extends AppCompatActivity {
    private String shift = "";
    private String extra = "";
    private int begin = 0;
    private int beginMin = 0;
    private int eind = 0;
    private int eindMin = 0;
    private int kleur = Color.TRANSPARENT;

    private ProgressBar prgLoading;

    private EditText txtShift;
    private EditText txtExtra;
    private TextView lblBegin;
    private TextView lblBeginMin;
    private TextView lblEind;
    private TextView lblEindMin;
    private LinearLayout pnlKleur;

    private ImageButton btnOpslaan;

    private List<String> shiften;

    private File dirRes;

    private int result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nieuwe_shift);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.toolbar_delete_edit_save);
        Toolbar parent = (Toolbar) getSupportActionBar().getCustomView().getParent();
        ((TextView) getSupportActionBar().getCustomView().findViewById(R.id.lblToolbarDeleteEditSaveTitel)).setText(getResources().getString(R.string.newShift));
        parent.setPadding(0,0,0,0);
        parent.setContentInsetsAbsolute(0, 0);

        dirRes = DirResSingleton.getInstance();

        initViews();
        handleEvents();

        shiften = new ArrayList<>();

        final boolean isEdit = getIntent().getBooleanExtra("isEdit", false);

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                btnOpslaan.setAlpha(0.5f);
                btnOpslaan.setEnabled(false);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                shiften.addAll(Reader.getShiften().keySet());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (isEdit){
                    shiften.remove(shift);
                }

                btnOpslaan.setAlpha(1f);
                btnOpslaan.setEnabled(true);
            }
        }.execute();

        if (isEdit){
            shift = getIntent().getStringExtra("shift");
            extra = getIntent().getStringExtra("extra");
            begin = getIntent().getIntExtra("begin", 0);
            beginMin = getIntent().getIntExtra("beginMin", 0);
            eind = getIntent().getIntExtra("eind", 0);
            eindMin = getIntent().getIntExtra("eindMin", 0);
            kleur = getIntent().getIntExtra("kleur", 0);

            setData();

            result = 1;
        }else{
            ((GradientDrawable) pnlKleur.getBackground()).setColor(Color.TRANSPARENT);
            pnlKleur.setTag(0);

            result = 2;
        }

        //checkFirstTime();
    }

    private void checkFirstTime(){
        final SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        boolean first = sharedPref.getBoolean("first", true);
        if (first){
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.tutorial));

            Map<String, String> messages = new HashMap<>();
            messages.put(getResources().getString(R.string.edit) +"/" + getResources().getString(R.string.add) + " " + getResources().getString(R.string.shift),
                    getResources().getString(R.string.messageNieuweShift1));

            FirstTimeDialog dialog = new FirstTimeDialog(this, messages, sharedPref);
            dialog.show();
        }

    }

    private void initViews() {
        View view = getSupportActionBar().getCustomView();

        prgLoading = (ProgressBar) findViewById(R.id.prgLoading);
        btnOpslaan = (ImageButton) view.findViewById(R.id.btnOpslaan);
        view.findViewById(R.id.btnDelete).setVisibility(View.GONE);
        view.findViewById(R.id.btnEdit).setVisibility(View.GONE);
        btnOpslaan.setVisibility(View.VISIBLE);

        txtShift =(EditText) findViewById(R.id.txtNieuweShift);
        txtExtra = (EditText) findViewById(R.id.txtNieuweExtra);
        lblBegin = (TextView) findViewById(R.id.lblNieuwBegin);
        lblBeginMin = (TextView) findViewById(R.id.lblNieuwBeginMin);
        lblEind = (TextView) findViewById(R.id.lblNieuwEind);
        lblEindMin = (TextView) findViewById(R.id.lblNieuwEindMin);
        pnlKleur = (LinearLayout) findViewById(R.id.pnlKleur);
    }

    private void setData(){
        txtShift.setText(shift);

        txtExtra.setText(extra);
        lblBegin.setText(String.format("%02d", begin));
        lblBeginMin.setText(String.format("%02d", beginMin));
        lblEind.setText(String.format("%02d", eind));
        lblEindMin.setText(String.format("%02d", eindMin));

        ((GradientDrawable) pnlKleur.getBackground()).setColor(kleur);
        pnlKleur.setTag(kleur);
//        if (kleur == 0){
//            ((GradientDrawable) pnlKleur.getBackground()).setColor(Color.TRANSPARENT);
//        }else{
//            ((GradientDrawable) pnlKleur.getBackground()).setColor(kleur);
//        }
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
                if (txtShift.getText().toString().equals("")){
                    Toast.makeText(NieuweShiftActivity.this, getResources().getString(R.string.toastEnterShiftCode), Toast.LENGTH_LONG).show();
                    return;
                }
                if (shiften.contains(txtShift.getText().toString())){
                    Toast.makeText(NieuweShiftActivity.this, "'" + txtShift.getText().toString() + "' " + getResources().getString(R.string.toastExists)  + ", " + getResources().getString(R.string.toastEnterSomethingElse), Toast.LENGTH_LONG).show();
                    return;
                }

                try{
                    Intent result = new Intent();
                    result.putExtra("shift", txtShift.getText().toString());
                    result.putExtra("extra", txtExtra.getText().toString());
                    result.putExtra("begin", Integer.parseInt(lblBegin.getText().toString()));
                    result.putExtra("beginMin", Integer.parseInt(lblBeginMin.getText().toString()));
                    result.putExtra("eind", Integer.parseInt(lblEind.getText().toString()));
                    result.putExtra("eindMin", Integer.parseInt(lblEindMin.getText().toString()));
                    result.putExtra("kleur", kleur);

                    setResult(NieuweShiftActivity.this.result, result);
                    finish();
                }catch (Exception e){
                    Toast.makeText(NieuweShiftActivity.this, getResources().getString(R.string.toastFieldsCorrect), Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.llBegin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NieuweShiftActivity.this);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final TimePicker timePicker = new TimePicker(NieuweShiftActivity.this);
                    timePicker.setIs24HourView(true);
                    timePicker.setHour(begin);
                    timePicker.setMinute(beginMin);

                    builder.setCancelable(true).setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                begin = timePicker.getHour();
                                beginMin = timePicker.getMinute();
                                lblBegin.setText(String.format("%02d", begin));
                                lblBeginMin.setText(String.format("%02d", beginMin));
                            }
                        }
                    });

                    builder.setView(timePicker);
                }else{
                    final ObjTimePicker timePicker = new ObjTimePicker(begin, beginMin, NieuweShiftActivity.this);

                    builder.setView(timePicker.getTimePicker());

                    builder.setCancelable(true).setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            begin = timePicker.getHour();
                            beginMin = timePicker.getMinute();
                            lblBegin.setText(String.format("%02d", begin));
                            lblBeginMin.setText(String.format("%02d", beginMin));
                        }
                    });
                }

                builder.show();
            }
        });
        findViewById(R.id.llEind).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NieuweShiftActivity.this);

                builder.setCancelable(true).setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final TimePicker timePicker = new TimePicker(NieuweShiftActivity.this);
                    timePicker.setIs24HourView(true);
                    timePicker.setHour(eind);
                    timePicker.setMinute(eindMin);

                    builder.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                eind = timePicker.getHour();
                                eindMin = timePicker.getMinute();
                                lblEind.setText(String.format("%02d", eind));
                                lblEindMin.setText(String.format("%02d", eindMin));
                            }
                        }
                    });

                    builder.setView(timePicker);
                }else{
                    final ObjTimePicker timePicker = new ObjTimePicker(eind, eindMin, NieuweShiftActivity.this);

                    builder.setView(timePicker.getTimePicker());

                    builder.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            eind = timePicker.getHour();
                            eindMin = timePicker.getMinute();
                            lblEind.setText(String.format("%02d", eind));
                            lblEindMin.setText(String.format("%02d", eindMin));
                        }
                    });
                }

                builder.show();
            }
        });
        pnlKleur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmbilWarnaDialog dialog = new AmbilWarnaDialog(NieuweShiftActivity.this, kleur, false, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) { }
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        ((GradientDrawable) pnlKleur.getBackground()).setColor(color);
                        kleur = color;
                        Methodes.updateWidgets(NieuweShiftActivity.this, getApplication());
                    }
                    @Override
                    public void onNeutral(AmbilWarnaDialog dialog) {
                        ((GradientDrawable) pnlKleur.getBackground()).setColor(Color.TRANSPARENT);
                        kleur = 0;
                    }
                }, getResources().getString(R.string.delete), true);
                dialog.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(0);
        finish();
    }
}
