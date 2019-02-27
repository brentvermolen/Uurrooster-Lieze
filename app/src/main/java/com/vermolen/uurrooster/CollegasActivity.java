package com.vermolen.uurrooster;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vermolen.uurrooster.Classes.CalendarSingletons;
import com.vermolen.uurrooster.Classes.DirResSingleton;
import com.vermolen.uurrooster.Classes.FirstTimeDialog;
import com.vermolen.uurrooster.Classes.Reader;
import com.vermolen.uurrooster.Classes.TextReader;
import com.vermolen.uurrooster.Classes.TextWriter;
import com.vermolen.uurrooster.Classes.Writer;
import com.vermolen.uurrooster.Widget.TodayWidget;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class CollegasActivity extends AppCompatActivity {
    File dirRes;
    ArrayAdapter<String> collegas;

    ListView lstCollega;
    TextView lblCollega;

    ProgressBar prgLoading;

    int currentSelectedItem;

    Button btnWijzigen;
    Button btnVerwijderen;
    Button btnToevoegen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collegas);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.toolbar_delete_edit_save);
        Toolbar parent = (Toolbar) getSupportActionBar().getCustomView().getParent();
        ((TextView) getSupportActionBar().getCustomView().findViewById(R.id.lblToolbarDeleteEditSaveTitel)).setText(getResources().getString(R.string.collegues));
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
            messages.put(getResources().getString(R.string.collegues), getResources().getString(R.string.messageCollegas1));

            FirstTimeDialog dialog = new FirstTimeDialog(this, messages, sharedPref);
            dialog.show();
        }
    }

    private void initViews() {
        lstCollega = (ListView) findViewById(R.id.lstCollegas);
        prgLoading = (ProgressBar) findViewById(R.id.prgLoading);

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                prgLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                collegas = Reader.getCollegas(CollegasActivity.this, R.layout.spinner_item);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                lstCollega.setAdapter(collegas);
                prgLoading.setVisibility(View.GONE);
            }
        }.execute();

        lblCollega = (TextView) findViewById(R.id.lblCollega);

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
        lstCollega.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String tekst = lstCollega.getItemAtPosition(position).toString();
                    lblCollega.setText(tekst);
                    currentSelectedItem = position;

                    btnWijzigen.setEnabled(true);
                    btnVerwijderen.setEnabled(true);
                } catch (Exception e) {

                }
            }
        });

        btnWijzigen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String oudeText = lblCollega.getText().toString();

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CollegasActivity.this);

                final EditText et = new EditText(CollegasActivity.this);
                et.setText(oudeText);

                alertDialogBuilder.setView(et);

                alertDialogBuilder.setCancelable(true).setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final String nieuweText = et.getText().toString();
                        lblCollega.setText(nieuweText);

                        new AsyncTask<Void, Void, Void>(){
                            @Override
                            protected void onPreExecute() {
                                CalendarSingletons.editCollega(oudeText, nieuweText);
                            }

                            @Override
                            protected Void doInBackground(Void... voids) {
                                Writer.editCollega(oudeText, nieuweText);
                                return null;
                            }
                        }.execute();
                        collegas.remove(oudeText);
                        collegas.add(nieuweText);

                        collegas.sort(new Comparator<String>() {
                            @Override
                            public int compare(String o1, String o2) {
                                return o1.toLowerCase().compareTo(o2.toLowerCase());
                            }
                        });

                        //Reload TodayWidget
                        Intent intent = new Intent(CollegasActivity.this, TodayWidget.class);
                        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
                        // since it seems the onUpdate() is only fired on that:
                        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), TodayWidget.class));
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                        sendBroadcast(intent);
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialog.show();
            }
        });

        btnVerwijderen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CollegasActivity.this);
                builder.setTitle(getResources().getString(R.string.delete));

                final String collega = lblCollega.getText().toString();

                builder.setMessage(getResources().getString(R.string.delete) + " '" + collega + "'\n" + getResources().getString(R.string.areYouSure));

                builder.setCancelable(true).setPositiveButton(getResources().getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AsyncTask<Void, Void, Void>(){
                            @Override
                            protected void onPreExecute() {
                                CalendarSingletons.removeCollega(collega);
                            }

                            @Override
                            protected Void doInBackground(Void... voids) {
                                Writer.removeCollega(collega);
                                return null;
                            }
                        }.execute();

                        collegas.remove(collega);
                        lblCollega.setText("");

                        if (lstCollega.getAdapter().getCount() == 0){
                            btnVerwijderen.setEnabled(false);
                            btnWijzigen.setEnabled(false);
                        }else{
                            lstCollega.performItemClick(null, 0, collegas.getItemId(0));
                        }
                    }
                }).setNegativeButton(getResources().getString(android.R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });

                builder.show();
            }
        });

        btnToevoegen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CollegasActivity.this);
                alertDialogBuilder.setTitle(getResources().getString(R.string.newCollegue));

                final EditText et = new EditText(CollegasActivity.this);
                et.setMaxLines(1);
                et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

                alertDialogBuilder.setView(et);

                // set prompts.xml to alertdialog builder

                // set dialog message
                alertDialogBuilder.setCancelable(true).setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final String nieuweText = et.getText().toString();
                        lblCollega.setText(nieuweText);

                        if (collegas.getPosition(nieuweText) != -1){
                            Toast.makeText(CollegasActivity.this, getResources().getString(R.string.newCollegue) + " " + getResources().getString(R.string.toastExists), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        new AsyncTask<Void, Void, Void>(){
                            @Override
                            protected void onPreExecute() {
                                CalendarSingletons.addCollega(nieuweText);
                            }

                            @Override
                            protected Void doInBackground(Void... voids) {
                                Writer.writeCollega(nieuweText);
                                return null;
                            }
                        }.execute();

                        //TODO
                        //collegas.add(nieuweText);
                        collegas.sort(new Comparator<String>() {
                            @Override
                            public int compare(String o1, String o2) {
                                return o1.toLowerCase().compareTo(o2.toLowerCase());
                            }
                        });
                        lstCollega.performItemClick(null, collegas.getPosition(nieuweText), collegas.getItemId(collegas.getPosition(nieuweText)));
                    }
                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
