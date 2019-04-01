package com.vermolen.uurrooster;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.vermolen.uurrooster.Classes.*;
import com.vermolen.uurrooster.Classes.Enums.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;

public class KalenderActivity extends AppCompatActivity {
    List<LinearLayout> panels;

    ImageButton btnToolbarEdit;
    ImageButton btnToolbarGaNaar;
    ImageButton btnToolbarSave;
    Spinner cboToolbarShiften;
    ImageButton btnToolbarGom;
    ImageButton btnToolbarKalender;
    ImageButton btnToolbarLijst;

    TextView lblMaand;
    ImageButton btnVorigeMaand;
    ImageButton btnVolgendeMaand;

    LinearLayout llContent;

    ProgressBar prgLoading;

    Map<Integer, List<String>> werkData;
    Map<Integer, List<String>> wisselData;
    Map<Integer, List<String>> persoonlijkData;
    Map<Voorkeur, String> voorkeuren;

    Maand maand;
    int jaar;

    Type type;
    String currNieuweShift;
    Map<Integer, String> nieuweShiften;
    List<Integer> removeShiften;

    boolean hasWissel;
    private boolean isGomActive;

    boolean useBackgroundShiftChange;
    boolean useBackgroundShift;
    boolean showPersonalNotes;
    String alternatif;
    private int firstDay;
    private int intTextSize;
    private Map<String, List<String>> shiften;

    private AsyncTask<Void, Void, Void> loadVoorkeuren;
    private AsyncTask<Void, Void, Void> werk;
    private AsyncTask<Void, Void, Void> wissel;
    private AsyncTask<Void, Void, Void> persoonlijk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kalender);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.toolbar_kalender);
        Toolbar parent = (Toolbar) getSupportActionBar().getCustomView().getParent();
        ((TextView) getSupportActionBar().getCustomView().findViewById(R.id.lblToolbarTitel)).setText(getResources().getString(R.string.calendar));
        parent.setPadding(0, 0, 0, 0);
        parent.setContentInsetsAbsolute(0, 0);

        View view = getSupportActionBar().getCustomView();
        btnToolbarEdit = view.findViewById(R.id.btnEditKalenderToolbar);
        btnToolbarGaNaar = view.findViewById(R.id.btnGaNaarKalenderToolbar);
        btnToolbarSave = view.findViewById(R.id.btnNieuwOpslaan);
        btnToolbarGom = view.findViewById(R.id.btnGom);
        isGomActive = false;

        cboToolbarShiften = view.findViewById(R.id.cboNieuwShiften);
        //ArrayAdapter<String> shiften = TextReader.getShiften(this, R.layout.spinner_item, dirRes);

        initViews();
        handleEvents();

        new AsyncTask<Void, Void, ArrayAdapter<String>>(){
            @Override
            protected void onPreExecute(){
                cboToolbarShiften.setEnabled(false);
                cboToolbarShiften.setAlpha(.5f);

                prgLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected ArrayAdapter<String> doInBackground(Void... voids) {
                shiften = Reader.getShiften();

//                ArrayAdapter<String> adapterShiften = new ArrayAdapter<>(KalenderActivity.this, R.layout.spinner_item);
//
//                for(String shift : shiften.keySet()){
//                    adapterShiften.add(shift);
//                }
//
//                return adapterShiften;
                return Reader.getShiften(KalenderActivity.this, R.layout.spinner_item);
            }

            @Override
            protected void onPostExecute(ArrayAdapter<String> shiften) {
                shiften.sort(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.compareTo(o2);
                    }
                });
                cboToolbarShiften.setAdapter(shiften);
                if (cboToolbarShiften.getAdapter().getCount() > 0) {
                    cboToolbarShiften.setSelection(0);
                }
                cboToolbarShiften.setEnabled(true);
                cboToolbarShiften.setAlpha(1f);
            }
        }.executeOnExecutor(THREAD_POOL_EXECUTOR);

        loadVoorkeuren = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                voorkeuren = Reader.getVoorkeuren();
                return null;
            }
        }.executeOnExecutor(THREAD_POOL_EXECUTOR);

        type = Type.OVERZICHT;

        Calendar c = Calendar.getInstance();
        jaar = getIntent().getIntExtra("jaar", c.get(Calendar.YEAR));
        int intMaand = getIntent().getIntExtra("maand", c.get(Calendar.MONTH) + 1);
        maand = Maand.valueOf(intMaand);

        loadSettings();

        //checkFirstTime();

        vulKalender(llContent);
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

    private void checkFirstTime() {
        final SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        boolean first = sharedPref.getBoolean("first", true);
        if (first) {
            final Map<String, String> messages = new HashMap<>();
            messages.put(getResources().getString(R.string.messageKalenderTitel1), getResources().getString(R.string.messageKalender1));
            messages.put(getResources().getString(R.string.messageKalenderTitel2), getResources().getString(R.string.messageKalender2));
            messages.put(getResources().getString(R.string.details), getResources().getString(R.string.messageKalender3));
            messages.put(getResources().getString(R.string.messageKalenderTitel4), getResources().getString(R.string.messageKalender4));

            FirstTimeDialog firstTimeDialog = new FirstTimeDialog(this, messages, sharedPref);

            firstTimeDialog.show();
        }
    }

    private void initViews() {
        panels = new ArrayList<>();
        btnVorigeMaand = findViewById(R.id.btnVorigeMaand);
        btnVolgendeMaand = findViewById(R.id.btnVolgendeMaand);
        lblMaand = findViewById(R.id.lblMaand);

        prgLoading = findViewById(R.id.prgLoading);
        prgLoading.setVisibility(View.GONE);

        llContent = findViewById(R.id.llCalendarContent);

        hasWissel = false;

        btnToolbarKalender = findViewById(R.id.btnKalenderView);
        btnToolbarLijst = findViewById(R.id.btnLijstView);
    }

    private void handleEvents() {
        findViewById(R.id.imgBackButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        lblMaand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                Maand currMaand = Maand.valueOf(cal.get(Calendar.MONTH) + 1);
                int currJaar = cal.get(Calendar.YEAR);
                if (!(currMaand == maand && currJaar == currJaar)) {
                    maand = currMaand;
                    jaar = currJaar;
                    //vulKalender();
                    Intent intent = new Intent(KalenderActivity.this, KalenderActivity.class);
                    intent.putExtra("maand", maand.getNr());
                    intent.putExtra("jaar", jaar);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }
            }
        });

        btnVorigeMaand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int intVorigeMaand = maand.getNr() - 1;
                if (intVorigeMaand == 0) {
                    intVorigeMaand = 12;
                    jaar--;
                }
                maand = Maand.valueOf(intVorigeMaand);

                //vulKalender();
                Intent intent = new Intent(KalenderActivity.this, KalenderActivity.class);
                intent.putExtra("maand", maand.getNr());
                intent.putExtra("jaar", jaar);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.right_out);
                finish();


                /*final LinearLayout llMaand = (LinearLayout) findViewById(R.id.kalender);
                llMaand.animate()
                        .translationX(llMaand.getWidth())
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                prgLoading.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                vulKalender();
                                llMaand.setX(0 - llMaand.getWidth());
                                llMaand.animate()
                                        .translationX(0)
                                        .setListener(new Animator.AnimatorListener() {
                                            @Override
                                            public void onAnimationStart(Animator animation) {
                                                prgLoading.setVisibility(View.GONE);
                                            }

                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                            }

                                            @Override
                                            public void onAnimationCancel(Animator animation) {
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animator animation) {
                                            }
                                        })
                                        .start();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                            }
                        }).start();*/
            }
        });

        btnVolgendeMaand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int intVolgendeMaand = maand.getNr() + 1;
                if (intVolgendeMaand == 13) {
                    intVolgendeMaand = 1;
                    jaar++;
                }
                maand = Maand.valueOf(intVolgendeMaand);
                //vulKalender();
                Intent intent = new Intent(KalenderActivity.this, KalenderActivity.class);
                intent.putExtra("maand", maand.getNr());
                intent.putExtra("jaar", jaar);
                startActivity(intent);
                overridePendingTransition(R.anim.left_in, R.anim.left_out);
                finish();
            }
        });

        btnToolbarGaNaar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(KalenderActivity.this);

                builder.setTitle(getResources().getString(R.string.goTo) + ":");

                /*ArrayAdapter<String> jaren = new ArrayAdapter(KalenderActivity.this, R.layout.spinner_item);

                final Map<Integer, List<String>> spinnerData = TextReader.getArchief(dirRes);
                final Map<Integer, ArrayAdapter> cboData = new HashMap<Integer, ArrayAdapter>();

                for (int jaar : spinnerData.keySet()) {
                    List<String> maanden = spinnerData.get(jaar);
                    jaren.add(String.valueOf(jaar));

                    ArrayAdapter adapter = new ArrayAdapter(KalenderActivity.this, R.layout.spinner_item);
                    for (String maand : maanden) {
                        adapter.add(maand);
                    }
                    cboData.put(jaar, adapter);
                }

                jaren.sort(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.compareTo(o2);
                    }
                });*/

                final DatePicker datePicker = new DatePicker(KalenderActivity.this);
                builder.setView(datePicker);

                builder.setCancelable(true).setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int intMaand = datePicker.getMonth();
                        maand = Maand.valueOf(intMaand + 1);
                        jaar = datePicker.getYear();

                        Intent intent = new Intent(KalenderActivity.this, KalenderActivity.class);

                        intent.putExtra("maand", maand.getNr());
                        intent.putExtra("jaar", jaar);

                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                        finish();
                    }
                });

                builder.show();
            }
        });

        btnToolbarGom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGomActive) {
                    btnToolbarGom.setBackgroundColor(Color.WHITE);
                    cboToolbarShiften.setEnabled(false);
                    isGomActive = true;
                } else {
                    btnToolbarGom.setBackgroundColor(Color.TRANSPARENT);
                    cboToolbarShiften.setEnabled(true);
                    isGomActive = false;
                }
            }
        });

        btnToolbarEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cboToolbarShiften.getAdapter().getCount() == 0) {
                    Toast.makeText(KalenderActivity.this, getResources().getString(R.string.toastCreateShiftFirst), Toast.LENGTH_LONG).show();
                } else {
                    btnToolbarEdit.setVisibility(View.GONE);
                    btnToolbarGaNaar.setVisibility(View.GONE);
                    btnVolgendeMaand.setVisibility(View.INVISIBLE);
                    btnVorigeMaand.setVisibility(View.INVISIBLE);
                    btnToolbarSave.setVisibility(View.VISIBLE);
                    cboToolbarShiften.setVisibility(View.VISIBLE);
                    btnToolbarGom.setVisibility(View.VISIBLE);
                    btnToolbarLijst.setVisibility(View.GONE);

                    nieuweShiften = new HashMap<>();
                    removeShiften = new ArrayList<Integer>();

                    type = Type.NIEUW;
                    vulKalender(llContent);
                }
            }
        });

        btnToolbarSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnToolbarEdit.setVisibility(View.VISIBLE);
                btnToolbarGaNaar.setVisibility(View.VISIBLE);
                btnVolgendeMaand.setVisibility(View.VISIBLE);
                btnVorigeMaand.setVisibility(View.VISIBLE);
                btnToolbarSave.setVisibility(View.GONE);
                cboToolbarShiften.setVisibility(View.GONE);
                btnToolbarGom.setVisibility(View.GONE);
                btnToolbarGom.setBackgroundColor(Color.TRANSPARENT);
                isGomActive = false;
                cboToolbarShiften.setEnabled(true);
                btnToolbarLijst.setVisibility(View.VISIBLE);

                final Maand maand = KalenderActivity.this.maand;
                final int jaar = KalenderActivity.this.jaar;

                new AsyncTask<Void, Void, Void>(){
                    @Override
                    protected void onPreExecute() {
                        btnToolbarEdit.setEnabled(false);
                        btnToolbarEdit.setAlpha(.5f);

                        for (int i : nieuweShiften.keySet()) {
                            String shift = nieuweShiften.get(i);
                            if (shift.equals("")) {
                                CalendarSingletons.removeWerkData(i);
                                //werkData.remove(i);
                            } else {
                                //werkData.put(i, Arrays.asList(shift));
                                CalendarSingletons.addWerkData(i, Arrays.asList(shift));
                            }
                        }

                        for (int i : removeShiften) {
                            //werkData.remove(i);
                            CalendarSingletons.removeWerkData(i);
                        }
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {

                        for (final int i : nieuweShiften.keySet()) {
                            final String shift = nieuweShiften.get(i);
                            if (shift.equals("")) {
                                Writer.removeWerk(i, maand, jaar);
                                werkData.remove(i);
                            } else {
                                Writer.addWerkShift(shift, shiften.get(shift), i, maand, jaar);
                                werkData.remove(i);
                                List<String> data = new ArrayList<>();
                                data.add(shift);
                                werkData.put(i, data);
                            }
                        }

                        for (final int i : removeShiften) {
                            Writer.removeWerk(i, maand, jaar);
                            werkData.remove(i);
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        Methodes.updateWidgets(KalenderActivity.this, getApplication());
                        btnToolbarEdit.setEnabled(true);
                        btnToolbarEdit.setAlpha(1f);
                    }
                }.executeOnExecutor(THREAD_POOL_EXECUTOR);


                type = Type.OVERZICHT;
                vulKalender(llContent);

                /*boolean firstDay = false;
                boolean laatsteDag = false;

                for (int i = 1; i <= 42; i++){
                    Resources res = getResources();
                    int id = res.getIdentifier("dag" + i, "id", KalenderActivity.this.getPackageName());

                    LinearLayout llMain = (LinearLayout) findViewById(id);
                    TextView lblHeader = (TextView) llMain.findViewById(R.id.lblHeaderDag);
                    int dag = Integer.parseInt(lblHeader.getText().toString().replace("*", ""));

                    int max = Methodes.getMaxDagenInMaand(maand.getNr(), jaar);

                    if (dag == 1){
                        firstDay = true;
                    }

                    if (firstDay){
                        if (i == max){
                            laatsteDag = true;
                        }

                        String shift = ((TextView) llMain.findViewById(R.id.lblInhoudDagWerk)).getText().toString();
                        TextWriter.addWerkShift(shift, dag, maand, jaar, dirRes);
                    }

                    if (laatsteDag){
                        break;
                    }
                }*/
            }
        });

        btnToolbarKalender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*switcher.showNext();*/
                btnToolbarLijst.setVisibility(View.VISIBLE);
                btnToolbarKalender.setVisibility(View.GONE);
                btnToolbarEdit.setVisibility(View.VISIBLE);
                vulKalender(llContent);
            }
        });
        btnToolbarLijst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*switcher.showNext();*/
                btnToolbarLijst.setVisibility(View.GONE);
                btnToolbarKalender.setVisibility(View.VISIBLE);
                btnToolbarEdit.setVisibility(View.GONE);
                vulLijst();
            }
        });

        cboToolbarShiften.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currNieuweShift = cboToolbarShiften.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    //TODO
    Map<Integer, List<String>> werkDataVorige;
    Map<Integer, List<String>> werkDataVolgende;

    private void vulLijst(){
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.MONTH, maand.getNr() - 1);
        cal.set(Calendar.YEAR, jaar);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        werk = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                werkData = Reader.getWerkData(maand, jaar);
                return null;
            }
        }.executeOnExecutor(THREAD_POOL_EXECUTOR);
        wissel = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                wisselData = Reader.getWisselData(maand, jaar);
                return null;
            }
        }.executeOnExecutor(THREAD_POOL_EXECUTOR);
        persoonlijk = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                persoonlijkData = Reader.getPersoonlijkData(maand, jaar);
                return null;
            }
        }.executeOnExecutor(THREAD_POOL_EXECUTOR);

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                prgLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    werk.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                try {
                    wissel.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                try {
                    persoonlijk.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                leegmakenKalender();

                try {
                    loadVoorkeuren.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                int intColor;
                try {
                    intColor = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_CALENDAR));
                    (findViewById(R.id.KalenderForm)).setBackgroundColor(intColor);
                } catch (Exception e) {

                }

                try {
                    intColor = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_CALENDAR_HEADER));
                    ((GradientDrawable) lblMaand.getBackground()).setColor(intColor);
                } catch (Exception e) {
                    ((GradientDrawable) findViewById(R.id.lblMaand).getBackground()).setColor(Color.TRANSPARENT);
                }


                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

                lblMaand.setText(maand.toString(KalenderActivity.this) + " " + String.valueOf(jaar));
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

                boolean firstDayFound = false;
                int huidigeDag = 1;

                LinearLayout llLijst = new LinearLayout(KalenderActivity.this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.setMargins(4, 4, 4, 4);
                llLijst.setLayoutParams(params);
                llLijst.setOrientation(LinearLayout.VERTICAL);

                ScrollView scrollView = new ScrollView(KalenderActivity.this);
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                scrollView.setLayoutParams(params);
                scrollView.addView(llLijst);

                for (int i = 1; i <= 42; i++) {
                    final int maxDagenInHuidigeMaand = Methodes.getMaxDagenInMaand(maand.getNr(), jaar);

                    if (firstDayOfWeekThisMonth == i) {
                        firstDayFound = true;
                    }

                    if (firstDayFound) {
                        if (huidigeDag > maxDagenInHuidigeMaand) {

                        } else {
                            //Lijst
                            View lijstView = inflater.inflate(R.layout.kalender_lijst_layout, null);

                            TextView lblLijstHeader = lijstView.findViewById(R.id.lblHeaderLijst);
                            lblLijstHeader.setText("");
                            TextView lblLijstHeaderDag = lijstView.findViewById(R.id.lblLijstHeaderDag);
                            lblLijstHeaderDag.setText("");
                            TextView lblLijstWerkData = lijstView.findViewById(R.id.lblLijstShift);
                            lblLijstWerkData.setText("");
                            TextView lblLijstTijdstippen = lijstView.findViewById(R.id.lblLijstTijdstippen);
                            lblLijstTijdstippen.setText("");
                            TextView lblLijstOrigineleShift = lijstView.findViewById(R.id.lblLijstOrigineelShift);
                            lblLijstOrigineleShift.setText("");
                            TextView lblLijstPersoonlijkeData = lijstView.findViewById(R.id.lblLijstPersoonlijk);
                            lblLijstPersoonlijkeData.setText("");

                            llLijst.addView(lijstView);
                            //Lijst

                            Calendar currCal = Calendar.getInstance();
                            if (huidigeDag == currCal.get(Calendar.DAY_OF_MONTH) && (maand.getNr() - 1) == currCal.get(Calendar.MONTH) && jaar == currCal.get(Calendar.YEAR)) {
                                lblLijstHeader.setTypeface(Typeface.DEFAULT_BOLD);
                            }
                            lblLijstHeader.setText(String.valueOf(huidigeDag));

                            cal.set(Calendar.DAY_OF_MONTH, huidigeDag);
                            int currDagInWeek = cal.get(Calendar.DAY_OF_WEEK);
                            String strDag = Methodes.getDag(currDagInWeek, KalenderActivity.this);
                            lblLijstHeaderDag.setText(strDag);

                            String shift = geefDataWerk(huidigeDag, true);

                            List<String> shiftData = shiften.get(shift);
                            if (shiftData != null) {
                                if (shiftData.get(1).equals("0") && shiftData.get(3).equals("0")) {
                                    lblLijstTijdstippen.setVisibility(View.INVISIBLE);
                                } else {
                                    String tekst = String.format("\t%02d:%02d - %02d:%02d", Integer.parseInt(shiftData.get(1)), Integer.parseInt(shiftData.get(2)),
                                            Integer.parseInt(shiftData.get(3)), Integer.parseInt(shiftData.get(4)));
                                    lblLijstTijdstippen.setText(tekst);
                                }
                            }

                            String activiteit = geefDataPersoonlijk(huidigeDag);

                            if (hasWissel) {
                                lblLijstOrigineleShift.setVisibility(View.VISIBLE);
                                List<String> wisselData = geefDataWissel(huidigeDag);
                                if (shiftData != null) {
                                    if (shiftData.get(0).equals("")) {
                                        lblLijstWerkData.setText(wisselData.get(1) + "\n\t"/* + wisselData.get(2) + "\n\t"*/ + wisselData.get(0));
                                    } else {
                                        lblLijstWerkData.setText(shiftData.get(0) + " (" + shift + ")\n\t"/* + wisselData.get(2) + "\n\t"*/ + wisselData.get(0));
                                    }
                                }
                                lblLijstOrigineleShift.setText("(" + geefDataWerk(huidigeDag, false) + ")");
                            } else {
                                lblLijstOrigineleShift.setVisibility(View.GONE);
                                if (shiftData != null) {
                                    if (shiftData.get(0).equals("")) {
                                        lblLijstWerkData.setText(shift);
                                    } else {
                                        lblLijstWerkData.setText(shiftData.get(0) + " (" + shift + ")");
                                    }
                                }
                            }

                            if (!activiteit.equals("")) {
                                lblLijstPersoonlijkeData.setText(activiteit);
                            }

                            huidigeDag++;

                            int kleur;
                            try {
                                kleur = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_DAYS_HEADER));
                                ((GradientDrawable) llLijst.findViewById(R.id.llLijstHeader).getBackground()).setColor(kleur);
                            } catch (Exception e) {
                                ((GradientDrawable) llLijst.findViewById(R.id.llLijstHeader).getBackground()).setColor(Color.GRAY);
                            }
                            try {
                                kleur = Integer.parseInt(voorkeuren.get(Voorkeur.TEXTCOLOR_DAY_HEADER));
                                lblLijstHeader.setTextColor(kleur);
                            } catch (Exception e) {
                            }
                            try {
                                kleur = Integer.parseInt(voorkeuren.get(Voorkeur.TEXTCOLOR_DAY_CONTENT));
                                lblLijstWerkData.setTextColor(kleur);
                                lblLijstPersoonlijkeData.setTextColor(kleur);
                            } catch (Exception e) {
                            }

                            if (!useBackgroundShiftChange) {
                                hasWissel = false;
                            }
                            if (hasWissel) {
                                try {
                                    kleur = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_SHIFT_CHANGE));
                                    ((GradientDrawable) (lijstView.findViewById(R.id.llLijstAchtergrond)).getBackground()).setColor(kleur);
                                } catch (Exception e) {
                                    ((GradientDrawable) (lijstView.findViewById(R.id.llLijstAchtergrond)).getBackground()).setColor(Color.WHITE);
                                }
                            } else {
                                try {
                                    kleur = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_DAYS));
                                    ((GradientDrawable) (lijstView.findViewById(R.id.llLijstAchtergrond)).getBackground()).setColor(kleur);
                                } catch (Exception e) {
                                    ((GradientDrawable) (lijstView.findViewById(R.id.llLijstAchtergrond)).getBackground()).setColor(Color.LTGRAY);
                                }
                            }
                        }
                    }
                }

                (llContent).addView(scrollView);

                prgLoading.setVisibility(View.GONE);
            }
        }.executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    private void vulKalender(final LinearLayout llCalContent) {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.MONTH, maand.getNr() - 1);
        lblMaand.setText(maand.toString(KalenderActivity.this) + " " + String.valueOf(jaar));
        cal.set(Calendar.YEAR, jaar);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        final AsyncTask tskLoadCalendar = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                btnToolbarLijst.setEnabled(false);
                btnToolbarEdit.setEnabled(false);
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                leegmakenKalender();

                int intColor;

                try {
                    loadVoorkeuren.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                try {
                    intColor = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_CALENDAR));
                    (findViewById(R.id.KalenderForm)).setBackgroundColor(intColor);
                } catch (Exception e) {

                }

                try {
                    intColor = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_CALENDAR_HEADER));
                    ((GradientDrawable) lblMaand.getBackground()).setColor(intColor);
                } catch (Exception e) {
                    ((GradientDrawable) findViewById(R.id.lblMaand).getBackground()).setColor(Color.TRANSPARENT);
                }


                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

                LinearLayout llContent = (LinearLayout) inflater.inflate(R.layout.month_widget, llCalContent);


                try {
                    intColor = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_WEEKDAYS));
                    ((GradientDrawable) llContent.findViewById(R.id.header).getBackground()).setColor(intColor);
                } catch (Exception e) {
                    ((GradientDrawable) llContent.findViewById(R.id.header).getBackground()).setColor(Color.TRANSPARENT);
                }

                TextView lblHeadMA = llContent.findViewById(R.id.lblHeadMA);
                TextView lblHeadDI = llContent.findViewById(R.id.lblHeadDI);
                TextView lblHeadWO = llContent.findViewById(R.id.lblHeadWO);
                TextView lblHeadDO = llContent.findViewById(R.id.lblHeadDO);
                TextView lblHeadVR = llContent.findViewById(R.id.lblHeadVR);
                TextView lblHeadZA = llContent.findViewById(R.id.lblHeadZA);
                TextView lblHeadZO = llContent.findViewById(R.id.lblHeadZO);

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

                lblHeadMA.setText(Methodes.getDagVerkort(eersteDag++, KalenderActivity.this));
                lblHeadDI.setText(Methodes.getDagVerkort(eersteDag++, KalenderActivity.this));
                lblHeadWO.setText(Methodes.getDagVerkort(eersteDag++, KalenderActivity.this));
                lblHeadDO.setText(Methodes.getDagVerkort(eersteDag++, KalenderActivity.this));
                lblHeadVR.setText(Methodes.getDagVerkort(eersteDag++, KalenderActivity.this));
                lblHeadZA.setText(Methodes.getDagVerkort(eersteDag++, KalenderActivity.this));
                lblHeadZO.setText(Methodes.getDagVerkort(eersteDag, KalenderActivity.this));

                LinearLayout llRow1 = llContent.findViewById(R.id.row1);
                LinearLayout llRowLast = llContent.findViewById(R.id.row6);

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
                    int id = res.getIdentifier("dag" + i, "id", KalenderActivity.this.getPackageName());

                    //Kalender
                    LayoutInflater kalenderInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    LinearLayout kalenderView = (LinearLayout) kalenderInflater.inflate(R.layout.dag_layout, null);

                    // fill in any details dynamically here
                    final LinearLayout pnlDagInhoud = kalenderView.findViewById(R.id.pnlInhoudDag);

                    OnSwipeTouchListener onSwipeTouchListener = new OnSwipeTouchListener() {
                        @Override
                        public boolean onSwipeRight() {
                            int intMaand = maand.getNr();
                            intMaand--;
                            if (intMaand == 0) {
                                maand = Maand.valueOf(12);
                                jaar--;
                            } else {
                                maand = Maand.valueOf(intMaand);
                            }

                            //vulKalender();
                            Intent intent = new Intent(KalenderActivity.this, KalenderActivity.class);
                            intent.putExtra("maand", maand.getNr());
                            intent.putExtra("jaar", jaar);
                            startActivity(intent);
                            overridePendingTransition(R.anim.right_in, R.anim.right_out);
                            finish();

                            return true;
                        }

                        @Override
                        public boolean onSwipeLeft() {
                            int intMaand = maand.getNr();
                            intMaand++;
                            if (intMaand == 13) {
                                maand = Maand.valueOf(1);
                                jaar++;
                            } else {
                                maand = Maand.valueOf(intMaand);
                            }

                            //vulKalender();
                            Intent intent = new Intent(KalenderActivity.this, KalenderActivity.class);
                            intent.putExtra("maand", maand.getNr());
                            intent.putExtra("jaar", jaar);
                            startActivity(intent);
                            overridePendingTransition(R.anim.left_in, R.anim.left_out);
                            finish();

                            return true;
                        }
                    };
                    pnlDagInhoud.setOnTouchListener(onSwipeTouchListener);

                    TextView lblHeaderDag = kalenderView.findViewById(R.id.lblHeaderDag);
                    lblHeaderDag.setTextSize(intTextSize);
                    final TextView lblWerkData = kalenderView.findViewById(R.id.lblInhoudDagWerk);
                    lblWerkData.setText("");
                    final TextView lblPersoonlijkData = kalenderView.findViewById(R.id.lblInhoudDagPersoonlijk);
                    lblPersoonlijkData.setText("");

                    if (type == Type.NIEUW) {
                        lblPersoonlijkData.setVisibility(View.GONE);
                        lblWerkData.setTextSize(16);

                        pnlDagInhoud.setOnTouchListener(null);
                        pnlDagInhoud.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                hasWissel = false;
                                String shift = geefDataWerk(dagNr, true);

                                if (hasWissel) {
                                    Toast.makeText(KalenderActivity.this, getResources().getString(R.string.toastCantEditShiftChange), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (isGomActive) {
                                    if (nieuweShiften.containsKey(dagNr)) {
                                        nieuweShiften.remove(dagNr);

                                        lblWerkData.setText(shift);
                                    } else {
                                        removeShiften.add(dagNr);

                                        lblWerkData.setText("");
                                    }
                                } else {
                                    if (removeShiften.contains(dagNr)) {
                                        removeShiften.remove(dagNr);
                                    }

                                    nieuweShiften.put(dagNr, currNieuweShift);
                                    lblWerkData.setText(currNieuweShift);
                                }
                            }
                        });
                    } else {
                        lblPersoonlijkData.setVisibility(View.VISIBLE);
                        lblWerkData.setTextSize(intTextSize);
                        lblPersoonlijkData.setTextSize(intTextSize);

                        pnlDagInhoud.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(KalenderActivity.this, DetailsActivity.class);
                                intent.putExtra("dag", dagNr);
                                intent.putExtra("maand", maand.getNr());
                                intent.putExtra("jaar", jaar);

                                startActivityForResult(intent, 0);
                            }
                        });
                    }

                    // insert into main view
                    ViewGroup llMain = llContent.findViewById(id);
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
                                ((GradientDrawable) lblHeaderDag.getBackground()).setAlpha(100);
                            } catch (Exception e) {
                                ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setColor(Color.GRAY);
                            }
                            ((GradientDrawable) lblHeaderDag.getBackground()).setColor(Color.GRAY);
                        } else {

                            Calendar currCal = Calendar.getInstance();
                            if (huidigeDag == currCal.get(Calendar.DAY_OF_MONTH) && (maand.getNr() - 1) == currCal.get(Calendar.MONTH) && jaar == currCal.get(Calendar.YEAR)) {
                                lblHeaderDag.setText("*" + String.valueOf(huidigeDag) + "*");
                            } else {
                                lblHeaderDag.setText(String.valueOf(huidigeDag));
                            }

                            cal.set(Calendar.DAY_OF_MONTH, huidigeDag);
                            /*int currDagInWeek = cal.get(Calendar.DAY_OF_WEEK);

                            String shift = geefDataWerk(huidigeDag, true);
                            lblWerkData.setText(shift);

                            String activiteit = geefDataPersoonlijk(huidigeDag);
                            if (!activiteit.equals("")) {
                                if (showPersonalNotes) {
                                    lblPersoonlijkData.setText(activiteit);
                                } else {
                                    lblPersoonlijkData.setText(alternatif);
                                }
                            }*/

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

                            /*if (!useBackgroundShiftChange) {
                                hasWissel = false;
                            }
                            if (hasWissel) {
                                try {
                                    kleur = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_SHIFT_CHANGE));
                                    ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setColor(kleur);
                                } catch (Exception e) {
                                    ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setColor(Color.WHITE);
                                }
                            } else {*/
                                try {
                                    kleur = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_DAYS));
                                    ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setColor(kleur);
                                } catch (Exception e) {
                                    ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setColor(Color.LTGRAY);
                                }

                                /*if (useBackgroundShift) {
                                    List<String> shiftData = shiften.get(shift);
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
                            }*/
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
                            ((GradientDrawable) lblHeaderDag.getBackground()).setAlpha(100);
                        } catch (Exception e) {
                            ((GradientDrawable) (kalenderView.findViewById(R.id.llAchtergrondDag)).getBackground()).setColor(Color.GRAY);
                        }
                        ((GradientDrawable) lblHeaderDag.getBackground()).setColor(Color.GRAY);
                    }
                }
            }
        }.executeOnExecutor(THREAD_POOL_EXECUTOR);

        werk = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                werkData = Reader.getWerkData(maand, jaar);
                return null;
            }
        }.executeOnExecutor(THREAD_POOL_EXECUTOR);
        wissel = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                wisselData = Reader.getWisselData(maand, jaar);
                return null;
            }
        }.executeOnExecutor(THREAD_POOL_EXECUTOR);
        persoonlijk = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                persoonlijkData = Reader.getPersoonlijkData(maand, jaar);
                return null;
            }
        }.executeOnExecutor(THREAD_POOL_EXECUTOR);
        //TODO
        final AsyncTask<Void, Void, Void> vorigeWerk = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Maand vorigeMaand = Maand.valueOf(maand.getNr() - 1);
                int vorigJaar = jaar;

                if (vorigeMaand.getNr() == 12) {
                    vorigJaar--;
                }

                werkDataVorige = Reader.getWerkData(vorigeMaand, vorigJaar);
                return null;
            }
        }.executeOnExecutor(THREAD_POOL_EXECUTOR);
        final AsyncTask<Void, Void, Void> volgendeWerk = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                Maand volgendeMaand = Maand.valueOf(maand.getNr() + 1);
                int volgendJaar = jaar;

                if (volgendeMaand.getNr() == 1){
                    volgendJaar++;
                }

                werkDataVolgende = Reader.getWerkData(volgendeMaand, volgendJaar);
                return null;
            }
        }.executeOnExecutor(THREAD_POOL_EXECUTOR);

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                prgLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    werk.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                try {
                    wissel.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                try {
                    persoonlijk.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                try {
                    vorigeWerk.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                try {
                    volgendeWerk.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                try {
                    tskLoadCalendar.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                int firstDayOfWeekThisMonth;

                cal.set(Calendar.DATE, 1);
                cal.set(Calendar.MONTH, maand.getNr() - 1);
                cal.set(Calendar.YEAR, jaar);
                cal.set(Calendar.DAY_OF_MONTH, 1);

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
                if (firstDayOfWeekThisMonth <= 1) {
                    firstDayOfWeekThisMonth += 7;
                }

                boolean firstDayFound = false;
                int huidigeDag = 1;
                int volgendeMaand = 1;

                for (int i = 1; i <= 42; i++) {
                    final int panelNr = i;

                    Resources res = getResources();
                    int id = res.getIdentifier("dag" + i, "id", KalenderActivity.this.getPackageName());

                    //Kalender
                    final LinearLayout kalenderView = llContent.findViewById(id);

                    final TextView lblWerkData = kalenderView.findViewById(R.id.lblInhoudDagWerk);
                    final TextView lblPersoonlijkData = kalenderView.findViewById(R.id.lblInhoudDagPersoonlijk);

                    final int maxDagenInHuidigeMaand = Methodes.getMaxDagenInMaand(maand.getNr(), jaar);
                    final int maxDagenInVorigeMaand = Methodes.getMaxDagenInMaand(maand.getNr() - 1, jaar);

                    if (firstDayOfWeekThisMonth == i) {
                        firstDayFound = true;
                    }

                    if (firstDayFound) {
                        if (huidigeDag > maxDagenInHuidigeMaand) {
                            try{
                                lblWerkData.setText(werkDataVolgende.get(volgendeMaand++).get(0));
                            }catch (Exception e){ }
                        } else {
                            Calendar currCal = Calendar.getInstance();

                            cal.set(Calendar.DAY_OF_MONTH, huidigeDag);
                            int currDagInWeek = cal.get(Calendar.DAY_OF_WEEK);

                            final String shift = geefDataWerk(huidigeDag, true);
                            lblWerkData.setText(shift);

                            String activiteit = geefDataPersoonlijk(huidigeDag);
                            if (!activiteit.equals("")) {
                                if (showPersonalNotes) {
                                    lblPersoonlijkData.setText(activiteit);
                                } else {
                                    lblPersoonlijkData.setText(alternatif);
                                }
                            }

                            huidigeDag++;
                            int kleur;

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
                                if (useBackgroundShift) {
                                    List<String> shiftData = shiften.get(shift);
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
                    }else{
                        int vorige = maxDagenInVorigeMaand - (firstDayOfWeekThisMonth - 1) + panelNr;

                        try{
                            lblWerkData.setText(werkDataVorige.get(vorige).get(0));
                        }catch (Exception e){ }
                    }
                }

                prgLoading.setVisibility(View.GONE);
                btnToolbarLijst.setEnabled(true);
                btnToolbarEdit.setEnabled(true);
            }
        }.executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    private void leegmakenKalender() {
        (llContent).removeAllViews();
    }

    private String geefDataWerk(int dag, boolean wisselHeeftVoorrang) {
        List<String> lst = new ArrayList<String>();
        lst = werkData.get(dag);

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
            hasWissel = false;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 0:
                if (resultCode == 1){
                    vulKalender(llContent);
                    Methodes.updateWidgets(this, getApplication());
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (btnToolbarKalender.getVisibility() == View.VISIBLE){
            btnToolbarKalender.performClick();
        }else{
            if (btnToolbarSave.getVisibility() == View.VISIBLE){
                if (nieuweShiften.size() > 0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(KalenderActivity.this);

                    builder.setTitle(getResources().getString(R.string.areYouSure));
                    builder.setMessage(getResources().getString(R.string.undoChanges));

                    builder.setCancelable(true).setNegativeButton(getResources().getString(android.R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { }
                    }).setPositiveButton(getResources().getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            nieuweShiften.clear();
                            btnToolbarSave.performClick();
                        }
                    });
                    builder.show();
                }else{
                    btnToolbarSave.performClick();
                }
            }else {
                super.onBackPressed();
            }
        }
    }
}


/*

                    switch (voorkeur){
                        case BACKGROUND_SHIFT_CHANGE:
                            break;
                        case BACKGROUND_CALENDAR_HEADER:
                            break;
                        case BACKGROUND_DAYS:
                            break;
                        case BACKGROUND_DAYS_HEADER:
                            break;
                        case BACKGROUND_CALENDAR:
                            break;
                        case BACKGROUND_WEEKDAYS:
                            break;
                        case LETTERTYPE_HEADERS:
                            break;
                        case LETTERTYPE_DAG_HEADERS:
                            break;
                        case LETTERTYPE_DAG_INHOUD:
                            break;
                        case LETTERTYPE_WEEKDAGEN:
                            break;
                        case TEXTCOLOR_TITELS:
                            break;
                        case TEXTCOLOR_DAYS:
                            break;
                        case TEXTCOLOR_DAY_HEADER:
                            break;
                        case TEXTCOLOR_DAY_CONTENT:
                            break;
                        case DETAILS_ACHTERGROND:
                            break;
                        case DETAILS_LETTERTYPE_DATUM:
                            break;
                        case DETAILS_LETTERTYPE_TITELS:
                            break;
                        case DETAILS_LETTERTYPE_INHOUD:
                            break;
                        case DETAILS_KLEUR_DATUM:
                            break;
                        case DETAILS_KLEUR_TITELS:
                            break;
                        case DETAILS_KLEUR_INHOUD:
                            break;
                        case DETAILS_ACHTERGROND_DATUM:
                            break;
                        case DETAILS_ACHTERGROND_TITELS:
                            break;
                        case DETAILS_ACHTERGROND_INHOUD:
                            break;
                    }
 */