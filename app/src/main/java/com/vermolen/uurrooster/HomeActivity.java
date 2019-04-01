package com.vermolen.uurrooster;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.vermolen.uurrooster.Classes.CalendarSingletons;
import com.vermolen.uurrooster.Classes.DirResSingleton;
import com.vermolen.uurrooster.Classes.FirstTimeDialog;
import com.vermolen.uurrooster.Classes.Methodes;
import com.vermolen.uurrooster.Classes.UserSingleton;
import com.vermolen.uurrooster.DB.UserDao;
import com.vermolen.uurrooster.Model.User;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    SharedPreferences sharedPref;
    UserDao userDao;

    User USER;

    private ImageButton btnKalender;
    private ImageButton btnData;
    private ImageButton btnCollegas;
    private ImageButton btnShiften;
    private ImageButton btnInstellingen;

    private ProgressBar prgLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setTitle(getResources().getString(R.string.app_name));

        initViews();
        handleEvents();

        if (!isNetworkAvailable()){
            new AlertDialog.Builder(this)
                    .setTitle("No Internet")
                    .setMessage("For this application to work properly you need an active internet connection")
                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .show();

            return;
        }

        readUser();

        //checkFirstTime();

        CalendarSingletons.sharedPreferencesCalendar = getSharedPreferences("calendar", MODE_PRIVATE);
        CalendarSingletons.contentResolver = getContentResolver();

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("Settings", MODE_PRIVATE);
        boolean startupCalendar = sharedPref.getBoolean("startupScreen", false);
        if (startupCalendar){
            Intent intent = new Intent(HomeActivity.this, KalenderActivity.class);
            intent.putExtra("maand", Calendar.getInstance().get(Calendar.MONTH));
            intent.putExtra("jaar", Calendar.getInstance().get(Calendar.YEAR));
            startActivity(intent);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void readUser() {
        final int sharedUserId = sharedPref.getInt("USER", -2);

        if (sharedUserId == -2){
            //-2 = Geen gegevens, -1 = Geen gebruiker, 0..* = Gebruiker id
            final int intUser_id = getIntent().getIntExtra("user_id", -2);

            if (intUser_id != -2){
                if (intUser_id == -1){
                    Toast.makeText(this, R.string.welcome, Toast.LENGTH_SHORT).show();

                    User user = new User();
                    user.setUser_id(-1);
                    UserSingleton.setInstance(user);
                    USER = user;

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("USER", -1);
                    editor.commit();
                }else{
                    loadUser(intUser_id);
                }
            }else{
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }else if (sharedUserId == -1) {
            Toast.makeText(this, R.string.welcome, Toast.LENGTH_SHORT).show();

            User user = new User();
            user.setUser_id(-1);
            UserSingleton.setInstance(user);
            USER = user;

            File dirRes = new File(sharedPref.getString("DirRes", getApplicationContext().getFilesDir().getAbsolutePath() + "//Resources//"));
            DirResSingleton.setInstance(dirRes);
        }else {
            loadUser(sharedUserId);
        }

        //Methodes.updateWidgets(this, getApplication());
    }

    private void loadUser(final int intUser_id) {
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                prgLoading.setVisibility(View.VISIBLE);

                btnCollegas.setEnabled(false);
                btnShiften.setEnabled(false);
                btnKalender.setEnabled(false);
                btnInstellingen.setEnabled(false);
                btnData.setEnabled(false);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                USER = userDao.getUserById(intUser_id);
                UserSingleton.setInstance(USER);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("USER", USER.getUser_id());
                editor.commit();

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                btnCollegas.setEnabled(true);
                btnShiften.setEnabled(true);
                btnKalender.setEnabled(true);
                btnInstellingen.setEnabled(true);
                btnData.setEnabled(true);

                prgLoading.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, getResources().getString(R.string.welcome) + ", " + USER.getUsername(), Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    private void checkFirstTime() {
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        boolean first = sharedPref.getBoolean("first", true);
        if (first){
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.welcome));

            Map<String, String> messages = new HashMap<>();
            messages.put(getResources().getString(R.string.welcome), getResources().getString(R.string.messageHome1));
            messages.put(getResources().getString(R.string.messageHomeTitel2), getResources().getString(R.string.messageHome2));
            messages.put(getResources().getString(R.string.messageHomeTitel3), getResources().getString(R.string.messageHome3));

            FirstTimeDialog dialog = new FirstTimeDialog(this, messages, sharedPref);
            dialog.show();
        }
    }

    private void initViews() {
        userDao = new UserDao();
        sharedPref = this.getSharedPreferences("USER", Context.MODE_PRIVATE);

        btnKalender = (ImageButton)findViewById(R.id.btnKalender);
        btnData = (ImageButton) findViewById(R.id.btnData);
        btnCollegas = (ImageButton) findViewById(R.id.btnCollegas);
        btnShiften = (ImageButton) findViewById(R.id.btnShiften);
        btnInstellingen = (ImageButton)findViewById(R.id.btnInstellingen);

        prgLoading = (ProgressBar) findViewById(R.id.prgLoading);
    }

    private void handleEvents() {
        btnKalender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prgLoading.setVisibility(View.VISIBLE);

                Intent intent = new Intent(HomeActivity.this, KalenderActivity.class);
                startActivity(intent);

                prgLoading.setVisibility(View.INVISIBLE);
            }
        });

        btnData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, DataActivity.class);
                startActivity(intent);
            }
        });

        btnCollegas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prgLoading.setVisibility(View.VISIBLE);

                Intent intent = new Intent(HomeActivity.this, CollegasActivity.class);
                startActivity(intent);

                prgLoading.setVisibility(View.INVISIBLE);
            }
        });

        btnShiften.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prgLoading.setVisibility(View.VISIBLE);

                Intent intent = new Intent(HomeActivity.this, ShiftenActivity.class);
                startActivity(intent);

                prgLoading.setVisibility(View.INVISIBLE);
            }
        });

        btnInstellingen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prgLoading.setVisibility(View.VISIBLE);

                Intent intent = new Intent(HomeActivity.this, InstellingenActivity.class);
                startActivityForResult(intent, 0);

                prgLoading.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 0:
                //Instellingen sluit af
                //TODO: Bij instellingen mogelijkheid geven om uit te loggen of te syncen met ander account of in loggen met ander account
                //readUser();
                break;
        }
    }
}
