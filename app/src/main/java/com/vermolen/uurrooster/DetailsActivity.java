package com.vermolen.uurrooster;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.vermolen.uurrooster.Classes.CalendarSingletons;
import com.vermolen.uurrooster.Classes.DirResSingleton;
import com.vermolen.uurrooster.Classes.Enums.Maand;
import com.vermolen.uurrooster.Classes.Enums.Voorkeur;
import com.vermolen.uurrooster.Classes.Methodes;
import com.vermolen.uurrooster.Classes.OnSwipeTouchListener;
import com.vermolen.uurrooster.Classes.Reader;
import com.vermolen.uurrooster.Classes.TextReader;
import com.vermolen.uurrooster.Classes.TextWriter;
import com.vermolen.uurrooster.Classes.Writer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class DetailsActivity extends AppCompatActivity {
    ImageButton btnOpslaan;
    ImageButton btnDelete;
    ImageButton btnEdit;

    ProgressBar prgLoading;

    TextView lblDatum;
    Spinner cboShift;
    TextView lblShift;
    TextView lblOriginele;
    TextView lblExtra;
    TextView lblTijdstippen;
    CheckBox chkWissel;
    ToggleButton tglRichting;
    CheckBox chkIsZO;
    Spinner cboCollegasWissel;
    TextView lblCollegaWissel;
    EditText txtPersoonlijk;
    TextView lblPersoonlijk;

    int dag;
    Maand maand;
    int jaar;

    String shift;

    ArrayAdapter<String> shiften;
    ArrayAdapter<String> collegas;
    private Map<Integer, List<String>> werkData;
    private Map<Integer, List<String>> wisselData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.toolbar_delete_edit_save);
        Toolbar parent = (Toolbar) getSupportActionBar().getCustomView().getParent();
        ((TextView) getSupportActionBar().getCustomView().findViewById(R.id.lblToolbarDeleteEditSaveTitel)).setText(getResources().getString(R.string.details));
        parent.setPadding(0,0,0,0);
        parent.setContentInsetsAbsolute(0, 0);

        setResult(0);

        initViews();
        handleEvents();
        //setPreferences();

        dag = getIntent().getExtras().getInt("dag");
        maand = Maand.valueOf(getIntent().getExtras().getInt("maand"));
        jaar = getIntent().getExtras().getInt("jaar");

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                prgLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                shiften = Reader.getShiften(DetailsActivity.this, R.layout.spinner_item);
                shiften.add("");
                collegas = Reader.getCollegas(DetailsActivity.this, R.layout.spinner_item);

                werkData = Reader.getWerkData(maand, jaar);
                wisselData = Reader.getWisselData(maand, jaar);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                cboShift.setAdapter(shiften);
                if (shiften.getCount() > 0){
                    cboShift.setSelection(0);
                }

                cboCollegasWissel.setAdapter(collegas);
                if (collegas.getCount() > 0){
                    cboCollegasWissel.setSelection(0);
                }

                laadGegevens();

                disableAll();

                prgLoading.setVisibility(View.GONE);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void disableAll() {
        lblShift.setVisibility(View.VISIBLE);
        cboShift.setVisibility(View.GONE);
        cboCollegasWissel.setVisibility(View.GONE);
        if (chkWissel.isChecked()){
            lblCollegaWissel.setVisibility(View.VISIBLE);
        }else {
            lblCollegaWissel.setVisibility(View.GONE);
        }
        tglRichting.setEnabled(false);
        chkIsZO.setEnabled(false);
        chkWissel.setEnabled(false);
        txtPersoonlijk.setVisibility(View.GONE);
        lblPersoonlijk.setVisibility(View.VISIBLE);
    }

    private void enableAll() {
        if (!chkWissel.isChecked()){
            lblShift.setVisibility(View.VISIBLE);
            cboShift.setVisibility(View.GONE);
        }else{
            lblShift.setVisibility(View.GONE);
            cboShift.setVisibility(View.VISIBLE);
        }

        if (lblShift.getText().toString().equals("")){
            lblShift.setVisibility(View.GONE);
            cboShift.setVisibility(View.VISIBLE);
        }

        if (chkWissel.isChecked()){
            cboCollegasWissel.setVisibility(View.VISIBLE);
        }else {
            cboCollegasWissel.setVisibility(View.GONE);
        }
        lblCollegaWissel.setVisibility(View.GONE);
        tglRichting.setEnabled(true);
        chkIsZO.setEnabled(true);
        chkWissel.setEnabled(true);
        txtPersoonlijk.setVisibility(View.VISIBLE);
        lblPersoonlijk.setVisibility(View.GONE);
    }

    private void laadGegevens(){
        lblDatum.setText(String.valueOf(dag) + " " + maand.toString(this) + " " + String.valueOf(jaar));

        try{
            shift = geefDataWerk(dag, true);
            cboShift.setSelection(shiften.getPosition(shift));
            lblShift.setText(shift);

            lblOriginele.setText(geefDataWerk(dag, false));
        }catch (Exception e){
            shift = "";
        }
        try{
            lblPersoonlijk.setText(Reader.getPersoonlijkData(maand, jaar).get(dag).get(0));
        }catch (Exception e){
            lblPersoonlijk.setText("");
        }

        List<String> wisselData = geefDataWissel(dag);
        if (wisselData != null){
            chkWissel.setChecked(true);
            String collega = wisselData.get(0);
            String wisselShift = wisselData.get(1);
            String richting = wisselData.get(2);
            String isZiekteOpvang = wisselData.get(3);

            cboShift.setSelection(shiften.getPosition(wisselShift));
            if (richting.equals("Ik Voor")){
                tglRichting.setChecked(true);
            }else{
                tglRichting.setChecked(false);
            }

            cboCollegasWissel.setSelection(collegas.getPosition(collega));
            lblCollegaWissel.setText(collega);

            if (isZiekteOpvang.equals("True")){
                chkIsZO.setChecked(true);
            }else{
                chkIsZO.setChecked(false);
            }
        }else{
            chkWissel.setChecked(false);
        }

        setShiftData(geefDataWerk(dag, true));
    }

    private void setShiftData(String currShift){
        //String currShift = geefDataWerk(dag, true);

        if (!currShift.equals("")){
            String shift = currShift;
            lblShift.setText(shift);
            List<String> shiftData = Reader.getShiften().get(shift);
            lblExtra.setText(shiftData.get(0));
            if (!shiftData.get(1).equals("0") && !shiftData.get(3).equals("0")){
                String tekst = String.format("%02d:%02d - %02d:%02d", Integer.parseInt(shiftData.get(1)), Integer.parseInt(shiftData.get(2)),
                        Integer.parseInt(shiftData.get(3)), Integer.parseInt(shiftData.get(4)));
                lblTijdstippen.setText("\t" + tekst);
                lblTijdstippen.setVisibility(View.VISIBLE);
            }else{
                lblTijdstippen.setVisibility(View.GONE);
            }
        }
    }

    private String geefDataWerk(int dag, boolean wisselHeeftVoorrang) {
        List<String> lst = new ArrayList<String>();
        lst = werkData.get(dag);

        if (wisselHeeftVoorrang) {
            List<String> wisselList = geefDataWissel(dag);
            if (wisselList != null) {
                String strWisselData = wisselList.get(1);/* + " " + wisselList[2] + " " + wisselList[0]*/
                ;

                if (!(strWisselData.equals(""))) {
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

    private void initViews() {
        View view = getSupportActionBar().getCustomView();
        btnOpslaan = (ImageButton) view.findViewById(R.id.btnOpslaan);
        btnDelete = (ImageButton) view.findViewById(R.id.btnDelete);
        btnEdit = (ImageButton) view.findViewById(R.id.btnEdit);

        prgLoading = (ProgressBar) findViewById(R.id.prgLoading);

        lblDatum = (TextView) findViewById(R.id.lblDetailsDatum);

        cboShift = (Spinner) findViewById(R.id.cboDetailsShiften);
        lblShift = (TextView) findViewById(R.id.lblDetailsShift);

        lblOriginele = (TextView) findViewById(R.id.lblDetailsOrigineleShift);
        findViewById(R.id.llDetailsOrigineleShift).setVisibility(View.GONE);

        lblTijdstippen = (TextView) findViewById(R.id.lblDetailsTijdstip);
        lblExtra = (TextView) findViewById(R.id.lblDetailsExtra);

        chkWissel = (CheckBox) findViewById(R.id.chkDetailsWissel);
        tglRichting = (ToggleButton) findViewById(R.id.tglRichting);
        tglRichting.setVisibility(View.GONE);
        cboCollegasWissel = (Spinner) findViewById(R.id.cboDetailsCollegaWissel);
        cboCollegasWissel.setVisibility(View.GONE);
        lblCollegaWissel = (TextView) findViewById(R.id.lblDetailsCollegaWissel);
        lblCollegaWissel.setVisibility(View.GONE);
        chkIsZO = (CheckBox) findViewById(R.id.chkZiekteOpvang);
        chkIsZO.setVisibility(View.GONE);

        txtPersoonlijk = (EditText) findViewById(R.id.txtDetailsPersoonlijk);
        lblPersoonlijk = (TextView) findViewById(R.id.lblDetailsPersoonlijk);
    }

    private void handleEvents() {
        ((ImageButton) findViewById(R.id.imgBackButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        (findViewById(R.id.detailsScroll)).setOnTouchListener(new OnSwipeTouchListener(){
            @Override
            public boolean onSwipeRight() {
                int dag = DetailsActivity.this.dag;
                dag--;
                int maand = DetailsActivity.this.maand.getNr();
                int jaar = DetailsActivity.this.jaar;

                if (dag == 0) {
                    maand--;
                    if (maand == 0){
                        jaar--;
                        maand = 12;
                    }
                    dag = Methodes.getMaxDagenInMaand(maand, jaar);
                }

                Intent intent = new Intent(DetailsActivity.this, DetailsActivity.class);
                intent.putExtra("dag", dag);
                intent.putExtra("maand", maand);
                intent.putExtra("jaar", jaar);

                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.right_out);
                finish();

                return true;
            }

            @Override
            public boolean onSwipeLeft() {
                int dag = DetailsActivity.this.dag;
                dag++;
                int maand = DetailsActivity.this.maand.getNr();
                int jaar = DetailsActivity.this.jaar;

                if (dag > Methodes.getMaxDagenInMaand(maand, jaar)) {
                    maand++;
                    if (maand == 13){
                        jaar++;
                        maand = 1;
                    }
                    dag = 1;
                }

                Intent intent = new Intent(DetailsActivity.this, DetailsActivity.class);
                intent.putExtra("dag", dag);
                intent.putExtra("maand", maand);
                intent.putExtra("jaar", jaar);

                startActivity(intent);
                overridePendingTransition(R.anim.left_in, R.anim.left_out);
                finish();

                return true;
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currMaand = Calendar.getInstance().get(Calendar.MONTH) + 1;
                int currJaar = Calendar.getInstance().get(Calendar.YEAR);

                if (jaar == currJaar){
                    if (maand.getNr() < currMaand) {
                        Toast.makeText(DetailsActivity.this, getResources().getString(R.string.toastArchive), Toast.LENGTH_LONG).show();
                        return;
                    }
                }else{
                    if (jaar < currJaar){
                        Toast.makeText(DetailsActivity.this, getResources().getString(R.string.toastArchive), Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                enableAll();
                btnEdit.setVisibility(View.GONE);
                btnOpslaan.setVisibility(View.VISIBLE);
                lblPersoonlijk.setVisibility(View.GONE);
                txtPersoonlijk.setVisibility(View.VISIBLE);
                txtPersoonlijk.setText(lblPersoonlijk.getText().toString());

                cboShift.setSelection(shiften.getPosition(lblShift.getText().toString()));
            }
        });

        btnOpslaan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Void>(){
                    @Override
                    protected void onPreExecute() {
                        prgLoading.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        if (shift.equals("")){
                            if (cboShift.getAdapter().getCount() > 1 && !cboShift.getSelectedItem().toString().equals("")){
                                shift = cboShift.getSelectedItem().toString();
                                Writer.addWerkShift(shift, dag, maand, jaar);
                                CalendarSingletons.addWerkData(dag, Arrays.asList(shift));

                                werkData = Reader.getWerkData(maand, jaar);
                            }
                        }

                        boolean bleWissel = chkWissel.isChecked();
                        if (bleWissel){
                            String orig = lblOriginele.getText().toString();
                            String wissel = cboShift.getSelectedItem().toString();

                            if (orig.equals(wissel)){
                                Toast.makeText(DetailsActivity.this, getResources().getString(R.string.toastSameAsOriginal), Toast.LENGTH_SHORT).show();
                                return null;
                            }else{
                                if (geefDataWerk(dag, false).equals("")){
                                    Writer.addWerkShift(shift, dag, maand, jaar);
                                    CalendarSingletons.addWerkData(dag, Arrays.asList(shift));
                                    werkData = Reader.getWerkData(maand, jaar);
                                }
                                String shift = cboShift.getSelectedItem().toString();
                                String collega = cboCollegasWissel.getSelectedItem().toString();
                                String richting = "";
                                if (tglRichting.isChecked()){
                                    richting = "Ik Voor";
                                }else   {
                                    richting = "Voor Mij";
                                }

                                String ZO = ((chkIsZO.isChecked()) ? "True" : "False");

                                Writer.writeWissel(shift, collega, richting, ZO, dag, maand, jaar);
                                CalendarSingletons.addWisselData(dag, Arrays.asList(collega, shift, richting, ZO));
                                wisselData = Reader.getWisselData(maand, jaar);
                            }
                        }else{
                            Writer.removeWissel(dag, maand, jaar);
                            CalendarSingletons.removeWisselData(dag);
                            if (!shift.equals("")){
                                Writer.editWerkShift(shift, dag, maand, jaar);
                                CalendarSingletons.editWerkShift(shift, dag);

                                wisselData = Reader.getWisselData(maand, jaar);
                                werkData = Reader.getWerkData(maand, jaar);
                            }
                        }

                        String tekst = txtPersoonlijk.getText().toString();
                        if (!tekst.equals("")){
                            Writer.writePersoonlijk(tekst, dag, maand, jaar);
                            CalendarSingletons.addPersoonlijkData(dag, Arrays.asList(tekst));
                        }else{
                            Writer.removePersoonlijk(dag, maand, jaar);
                            CalendarSingletons.removePersoonlijkeData(dag);
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {Methodes.updateWidgets(DetailsActivity.this, getApplication());
                        disableAll();
                        btnEdit.setVisibility(View.VISIBLE);
                        btnOpslaan.setVisibility(View.GONE);
                        lblPersoonlijk.setVisibility(View.VISIBLE);
                        txtPersoonlijk.setVisibility(View.GONE);
                        lblPersoonlijk.setText(txtPersoonlijk.getText().toString());
                        lblShift.setText(shift);

                        laadGegevens();

                        prgLoading.setVisibility(View.GONE);

                        setResult(1);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);

                builder.setTitle(getResources().getString(R.string.delete));
                builder.setMessage(getResources().getString(R.string.deleteDataOfTheDay));

                builder.setCancelable(true)
                        .setPositiveButton(getResources().getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Writer.removeWerk(dag, maand, jaar);
                                Writer.removePersoonlijk(dag, maand, jaar);
                                Writer.removeWissel(dag, maand, jaar);
                                CalendarSingletons.removeWerkData(dag);
                                CalendarSingletons.removePersoonlijkeData(dag);
                                CalendarSingletons.removeWisselData(dag);

                                setResult(1);
                                finish();
                            }
                        }).setNegativeButton(getResources().getString(android.R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

                builder.show();
            }
        });

        chkWissel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (cboCollegasWissel.getAdapter().getCount() == 0){
                        Toast.makeText(DetailsActivity.this, getResources().getString(R.string.toastAddCollegue), Toast.LENGTH_LONG).show();
                        chkWissel.setChecked(false);
                        return;
                    }

                    if (shift.equals(""))
                    {
                        if(cboShift.getAdapter().getCount() > 1){ //Shift "" wordt automatisch toegevoegd
                            if (!cboShift.getSelectedItem().toString().equals("")){
                                shift = cboShift.getSelectedItem().toString();
                                Writer.addWerkShift(shift, dag, maand, jaar);
                                CalendarSingletons.addWerkData(dag, Arrays.asList(shift));
                                werkData = Reader.getWerkData(maand, jaar);
                                lblOriginele.setText(shift);

                                setResult(1);
                            }else{
                                Toast.makeText(DetailsActivity.this, getResources().getString(R.string.toastSelectShift), Toast.LENGTH_SHORT).show();
                                chkWissel.setChecked(false);
                                return;
                            }
                        }else{
                            Toast.makeText(DetailsActivity.this, getResources().getString(R.string.toastAddShift), Toast.LENGTH_SHORT).show();
                            chkWissel.setChecked(false);
                            return;
                        }
                    }
                    enableAll();

                    cboCollegasWissel.setVisibility(View.VISIBLE);
                    //tglRichting.setVisibility(View.VISIBLE);
                    //chkIsZO.setVisibility(View.VISIBLE);
                    cboShift.setClickable(true);
                    findViewById(R.id.llDetailsOrigineleShift).setVisibility(View.VISIBLE);
                }else{
                    cboShift.setVisibility(View.GONE);
                    lblShift.setVisibility(View.VISIBLE);
                    shift = lblOriginele.getText().toString();
                    setShiftData(lblOriginele.getText().toString());

                    cboCollegasWissel.setVisibility(View.GONE);
                    tglRichting.setVisibility(View.GONE);
                    chkIsZO.setVisibility(View.GONE);

                    cboShift.setSelection(shiften.getPosition(lblOriginele.getText().toString()));
                    findViewById(R.id.llDetailsOrigineleShift).setVisibility(View.GONE);
                }
            }
        });

        chkIsZO.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    tglRichting.setChecked(false);
                    tglRichting.setEnabled(false);
                }else{
                    tglRichting.setEnabled(true);
                }
            }
        });

        cboShift.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setShiftData(shiften.getItem(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        cboCollegasWissel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                lblCollegaWissel.setText(cboCollegasWissel.getSelectedItem().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setPreferences() {
        Map<Voorkeur, String> voorkeuren = Reader.getVoorkeuren();

        try {
            ((LinearLayout) findViewById(R.id.llDetailsAchtergrond)).setBackgroundColor(Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_CALENDAR)));
        } catch (Exception e) { }

        try{
            int color = Integer.parseInt(voorkeuren.get(Voorkeur.TEXTCOLOR_TITELS));
            ((TextView)findViewById(R.id.lblDetailsTitelShift)).setTextColor(color);
            ((TextView)findViewById(R.id.lblDetailsTitelWissel)).setTextColor(color);
            ((TextView)findViewById(R.id.lblDetailsTitelPersoonlijk)).setTextColor(color);
        }catch (Exception e) { }

        try{
            int color = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_CALENDAR_HEADER));
            ((LinearLayout) findViewById(R.id.llTitelShift)).setBackgroundColor(color);
            ((LinearLayout) findViewById(R.id.llTitelWissel)).setBackgroundColor(color);
            ((LinearLayout) findViewById(R.id.llTitelPersoonlijk)).setBackgroundColor(color);
        }catch (Exception e) { }

        try{
            int color = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_DAYS));
            ((LinearLayout) findViewById(R.id.llInhoudDatum)).setBackgroundColor(color);
            ((LinearLayout) findViewById(R.id.llInhoudShift)).setBackgroundColor(color);
            ((LinearLayout) findViewById(R.id.llInhoudExtra)).setBackgroundColor(color);
            ((LinearLayout) findViewById(R.id.llInhoudWissel)).setBackgroundColor(color);
            ((LinearLayout) findViewById(R.id.llInhoudPersoonlijk)).setBackgroundColor(color);
        }catch (Exception e) { }

        try{
            int color = Integer.parseInt(voorkeuren.get(Voorkeur.TEXTCOLOR_DAY_CONTENT));
            ((TextView)findViewById(R.id.lblDetailsDatum)).setTextColor(color);
            ((Spinner)findViewById(R.id.cboDetailsShiften)).setBackgroundColor(color);
            ((TextView)findViewById(R.id.lblDetailsOrigineleShift)).setTextColor(color);
            ((TextView)findViewById(R.id.lblDetailsExtra)).setTextColor(color);
            ((Spinner)findViewById(R.id.cboDetailsCollegaWissel)).setBackgroundColor(color);
            ((TextView)findViewById(R.id.lblDetailsTijdstip)).setTextColor(color);
            ((TextView)findViewById(R.id.lblDetailsPersoonlijk)).setTextColor(color);
            ((CheckBox)findViewById(R.id.chkZiekteOpvang)).setTextColor(color);
            ((CheckBox)findViewById(R.id.chkDetailsWissel)).setTextColor(color);
        }catch (Exception e) { }
    }

    @Override
    public void onBackPressed() {
        if (btnOpslaan.getVisibility() == View.VISIBLE){
            btnOpslaan.setVisibility(View.GONE);
            btnEdit.setVisibility(View.VISIBLE);
            laadGegevens();
            disableAll();
        }else{
            finish();
        }
    }
}
