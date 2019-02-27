package com.vermolen.uurrooster;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vermolen.uurrooster.Classes.Enums.Voorkeur;
import com.vermolen.uurrooster.Classes.TextReader;

import java.io.File;
import java.util.Map;

public class WijzigVoorkeurActivity extends AppCompatActivity {
    File dirRes;
    Voorkeur voorkeur;

    LinearLayout pnlKleur;
    TextView lblRood;
    TextView lblGroen;
    TextView lblBlauw;
    SeekBar skbRood;
    SeekBar skbGroen;
    SeekBar skbBlauw;
    EditText txtHex;
    Button btnOpslaan;

    int kleur;
    int rood;
    int groen;
    int blauw;

    private Map<Voorkeur, String> voorkeuren;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wijzig_voorkeur);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.toolbar_delete_edit_save);
        Toolbar parent = (Toolbar) getSupportActionBar().getCustomView().getParent();
        ((TextView) getSupportActionBar().getCustomView().findViewById(R.id.lblToolbarDeleteEditSaveTitel)).setText(getResources().getString(R.string.editPreference));
        parent.setPadding(0,0,0,0);
        parent.setContentInsetsAbsolute(0, 0);

        dirRes = new File(getIntent().getExtras().getString("dirRes"));
        voorkeur = Voorkeur.valueOfNr(getIntent().getExtras().getInt("voorkeur"));
        voorkeuren = TextReader.getVoorkeuren(dirRes);

        initViews();
        handleEvents();

        try{
            kleur = Integer.parseInt(voorkeuren.get(voorkeur));
            rood = Color.red(kleur);
            groen = Color.green(kleur);
            blauw = Color.blue(kleur);
        }catch (Exception e){
            rood = 255;
            groen = 255;
            blauw = 255;
        }

        setKleur();
    }

    private void initViews() {
        pnlKleur = (LinearLayout) findViewById(R.id.pnlKleur);
        lblRood = (TextView) findViewById(R.id.lblWijzigRood);
        lblGroen = (TextView) findViewById(R.id.lblWijzigGroen);
        lblBlauw = (TextView) findViewById(R.id.lblWijzigBlauw);
        skbRood = (SeekBar) findViewById(R.id.skbRood);
        skbGroen = (SeekBar) findViewById(R.id.skbGroen);
        skbBlauw = (SeekBar) findViewById(R.id.skbBlauw);
        txtHex = (EditText) findViewById(R.id.txtHexTekst);
        btnOpslaan = (Button) findViewById(R.id.btnWijzigOpslaan);
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
        skbRood.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rood = progress;
                setKleur();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        skbGroen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                groen = progress;
                setKleur();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        skbBlauw.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                blauw = progress;
                setKleur();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        txtHex.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    kleur = Color.parseColor("#" + s.toString());
                    rood = Color.red(kleur);
                    groen = Color.green(kleur);
                    blauw = Color.blue(kleur);
                    setKleur();
                }catch (Exception e){ }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });
        btnOpslaan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                result.putExtra("voorkeur", voorkeur.name());
                result.putExtra("kleur", kleur);
                setResult(1, result);
                finish();
            }
        });
    }

    private void setKleur(){
        skbRood.setProgress(rood);
        lblRood.setText(String.valueOf(rood));
        skbGroen.setProgress(groen);
        lblGroen.setText(String.valueOf(groen));
        skbBlauw.setProgress(blauw);
        lblBlauw.setText(String.valueOf(blauw));

        kleur = Color.rgb(rood, groen, blauw);
        pnlKleur.setBackgroundColor(kleur);

        String hex = String.format("%02X%02X%02X", rood, groen, blauw);
        if (!txtHex.getText().toString().equals(hex)){
            txtHex.setText(hex);
        }
    }

    @Override
    public void onBackPressed() {
        setResult(0);
        finish();
    }
}
