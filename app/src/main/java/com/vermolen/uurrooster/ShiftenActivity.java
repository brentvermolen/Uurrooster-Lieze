package com.vermolen.uurrooster;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vermolen.uurrooster.Classes.CalendarSingletons;
import com.vermolen.uurrooster.Classes.DirResSingleton;
import com.vermolen.uurrooster.Classes.FirstTimeDialog;
import com.vermolen.uurrooster.Classes.Methodes;
import com.vermolen.uurrooster.Classes.Reader;
import com.vermolen.uurrooster.Classes.TextReader;
import com.vermolen.uurrooster.Classes.TextWriter;
import com.vermolen.uurrooster.Classes.Writer;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftenActivity extends AppCompatActivity {
    File dirRes;
    ArrayAdapter<String> shiften;
    Map<String, List<String>> shiftData;

    ProgressBar prgLoading;

    ListView lstShift;
    TextView lblShift;
    TextView lblExtraInfo;
    TextView lblTijdstippen;
    LinearLayout pnlKleur;

    int currentSelectedItem;

    Button btnWijzigen;
    Button btnVerwijderen;
    Button btnToevoegen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shiften);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.toolbar_delete_edit_save);
        Toolbar parent = (Toolbar) getSupportActionBar().getCustomView().getParent();
        ((TextView) getSupportActionBar().getCustomView().findViewById(R.id.lblToolbarDeleteEditSaveTitel)).setText(getResources().getString(R.string.shifts));
        parent.setPadding(0,0,0,0);
        parent.setContentInsetsAbsolute(0, 0);

        dirRes = DirResSingleton.getInstance();

        initViews();
        handleEvents();

        //checkFirstTime();
    }

    private void checkFirstTime() {
        final SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        boolean first = sharedPref.getBoolean("first", true);
        if (first){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.tutorial));

            Map<String, String> messages = new HashMap<>();
            messages.put(getResources().getString(R.string.shifts), getResources().getString(R.string.messageShifts1));

            FirstTimeDialog dialog = new FirstTimeDialog(this, messages, sharedPref);
            dialog.show();
        }
    }

    private void initViews() {
        prgLoading = (ProgressBar) findViewById(R.id.prgLoading);
        lstShift = (ListView) findViewById(R.id.lstShiften);

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                prgLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                shiftData = Reader.getShiften();
                shiften = Reader.getShiften(ShiftenActivity.this, R.layout.spinner_item);

                shiften.sort(new Comparator<String>() {
                    @Override
                    public int compare(String s, String t1) {
                        return s.compareTo(t1);
                    }
                });

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                lstShift.setAdapter(shiften);
                prgLoading.setVisibility(View.GONE);
            }
        }.execute();

        lblShift = (TextView) findViewById(R.id.lblShift);
        lblExtraInfo = (TextView) findViewById(R.id.lblExtraInfoShift);
        lblTijdstippen = (TextView) findViewById(R.id.lblTijdstippen);
        pnlKleur = (LinearLayout) findViewById(R.id.pnlKleur);
        ((GradientDrawable) pnlKleur.getBackground()).setColor(Color.TRANSPARENT);
        pnlKleur.setTag(0);

        btnWijzigen = (Button) findViewById(R.id.btnWijzig);
        btnWijzigen.setEnabled(false);
        btnVerwijderen = (Button) findViewById(R.id.btnVerwijder);
        btnVerwijderen.setEnabled(false);
        btnToevoegen = (Button) findViewById(R.id.btnToevoegen);
    }

    private void handleEvents() {
        ((ImageButton) findViewById(R.id.imgBackButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        ((ImageButton) findViewById(R.id.btnDelete)).setVisibility(View.GONE);
        ((ImageButton) findViewById(R.id.btnEdit)).setVisibility(View.GONE);
        ((ImageButton) findViewById(R.id.btnOpslaan)).setVisibility(View.GONE);
        lstShift.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    final String tekst = lstShift.getItemAtPosition(position).toString();
                    lblShift.setText(tekst);
                    currentSelectedItem = position;

                    List<String> data = shiftData.get(tekst);

                    if (data != null) {
                        lblExtraInfo.setText(data.get(0));
                        try {
                            String begin = data.get(1);
                            String beginMin = data.get(2);
                            String eind = data.get(3);
                            String eindMin = data.get(4);
                            String tijdstip = String.format("%02d:%02d - %02d:%02d", Integer.parseInt(begin),
                                    Integer.parseInt(beginMin), Integer.parseInt(eind), Integer.parseInt(eindMin));

                            lblTijdstippen.setText(tijdstip);
                        } catch (Exception e) {
                        }

                        int kleur = Integer.parseInt(data.get(5));
                        pnlKleur.setTag(kleur);

                        if (kleur != 0) {
                            try {
                                ((GradientDrawable) pnlKleur.getBackground()).setColor(kleur);
                                pnlKleur.setTag(kleur);
                            } catch (Exception e) {
                            }
                        } else {
                            ((GradientDrawable) pnlKleur.getBackground()).setColor(Color.TRANSPARENT);
                            pnlKleur.setTag(0);
                        }
                    }

                    btnWijzigen.setEnabled(true);
                    btnVerwijderen.setEnabled(true);
                } catch (Exception e) {

                }
            }
        });

        btnWijzigen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShiftenActivity.this, NieuweShiftActivity.class);
                intent.putExtra("isEdit", true);
                intent.putExtra("shift", lblShift.getText().toString());
                intent.putExtra("extra", lblExtraInfo.getText().toString());

                String[] splitTijdstip = lblTijdstippen.getText().toString().split(" - ");
                String[] splitBegin = splitTijdstip[0].split(":");
                String[] splitEind = splitTijdstip[1].split(":");
                intent.putExtra("begin", Integer.parseInt(splitBegin[0]));
                intent.putExtra("beginMin", Integer.parseInt(splitBegin[1]));
                intent.putExtra("eind", Integer.parseInt(splitEind[0]));
                intent.putExtra("eindMin", Integer.parseInt(splitEind[1]));
                intent.putExtra("kleur", (int)pnlKleur.getTag());

                startActivityForResult(intent, 0);
            }
        });

        btnVerwijderen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShiftenActivity.this);
                builder.setTitle(getResources().getString(R.string.delete));
                builder.setMessage(getResources().getString(R.string.delete) + " '" + lblShift.getText().toString() + "'\n" + getResources().getString(R.string.areYouSure));

                builder.setCancelable(true).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AsyncTask<Void, Void, Void>(){
                            @Override
                            protected void onPreExecute() {
                                CalendarSingletons.removeShift(lblShift.getText().toString());
                            }

                            @Override
                            protected Void doInBackground(Void... voids) {
                                Writer.removeShift(lblShift.getText().toString());
                                return null;
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        shiften.remove(lblShift.getText().toString());
                        shiftData.remove(lblShift.getText().toString());

                        lblShift.setText("");
                        lblExtraInfo.setText("");
                        lblTijdstippen.setText("");
                        ((GradientDrawable) pnlKleur.getBackground()).setColor(0);

                        if (lstShift.getAdapter().getCount() == 0){
                            btnVerwijderen.setEnabled(false);
                            btnWijzigen.setEnabled(false);
                        }else{
                            lstShift.performItemClick(null, 0, shiften.getItemId(0));
                        }
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });

                builder.show();
            }
        });

        btnToevoegen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShiftenActivity.this, NieuweShiftActivity.class);
                intent.putExtra("isEdit", false);
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 0:
                if (resultCode != 0){
                    Bundle extras = data.getExtras();

                    final String shift = extras.getString("shift");
                    String extra = extras.getString("extra");
                    int begin = extras.getInt("begin");
                    int beginMin = extras.getInt("beginMin");
                    int eind = extras.getInt("eind");
                    int eindMin = extras.getInt("eindMin");
                    int kleur = extras.getInt("kleur");
                    final List<String> shiftData = new ArrayList<>();
                    shiftData.add(extra);
                    shiftData.add(String.valueOf(begin));
                    shiftData.add(String.valueOf(beginMin));
                    shiftData.add(String.valueOf(eind));
                    shiftData.add(String.valueOf(eindMin));
                    shiftData.add(String.valueOf(kleur));

                    if (resultCode == 1){
                        new AsyncTask<Void, Void, Void>(){
                            @Override
                            protected void onPreExecute() {
                                CalendarSingletons.editShift(lblShift.getText().toString(), shift, shiftData);
                            }

                            @Override
                            protected Void doInBackground(Void... voids) {
                                Writer.editShift(lblShift.getText().toString(), shift, shiftData);
                                return null;
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                        shiften.remove(lblShift.getText().toString());
                    }else{
                        new AsyncTask<Void, Void, Void>(){
                            @Override
                            protected void onPreExecute() {
                                prgLoading.setVisibility(View.VISIBLE);
                            }

                            @Override
                            protected Void doInBackground(Void... voids) {
                                Writer.writeShift(shift, shiftData);
                                ShiftenActivity.this.shiftData = Reader.getShiften();
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                int pos = shiften.getPosition(shift);
                                lstShift.performItemClick(null, pos, shiften.getItemId(pos));
                                prgLoading.setVisibility(View.GONE);

                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }

                    CalendarSingletons.addShift(shift, shiftData);
                    shiften.add(shift);
                    /*lblShift.setText(shift);
                    String tijdstip = String.format("%02d:%02d - %02d:%02d", begin,
                            beginMin, eind, eindMin);

                    lblTijdstippen.setText(tijdstip);
                    lblExtraInfo.setText(extra);

                    if (kleur != 0){
                        try{
                            ((GradientDrawable) pnlKleur.getBackground()).setColor(kleur);
                        }catch (Exception e){ }
                    }else{
                        ((GradientDrawable) pnlKleur.getBackground()).setColor(Color.TRANSPARENT);
                    }*/

                    shiften.sort(new Comparator<String>() {
                        @Override
                        public int compare(String o1, String o2) {
                            return o1.compareTo(o2);
                        }
                    });

                    Methodes.updateWidgets(ShiftenActivity.this, getApplication());
                }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
