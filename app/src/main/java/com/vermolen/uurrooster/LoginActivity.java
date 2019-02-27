package com.vermolen.uurrooster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.vermolen.uurrooster.Classes.DirResSingleton;
import com.vermolen.uurrooster.Classes.UserSingleton;
import com.vermolen.uurrooster.DB.UserDao;
import com.vermolen.uurrooster.Model.User;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity {

    UserDao userDao;

    User USER;

    SharedPreferences sharedPref;
    boolean resLoaded;
    File dirRes;

    ProgressBar prgLoading;
    EditText txtUsername;
    EditText txtPassword;
    Button btnSignIn;
    Button btnContinueAsGuest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        resLoaded = false;

        initViews();
        handleEvents();

        loadData();
    }

    private void initViews() {
        userDao = new UserDao();

        sharedPref = getApplicationContext().getSharedPreferences("Settings", MODE_PRIVATE);

        prgLoading = (ProgressBar) findViewById(R.id.prgLoading);

        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtWachtwoord);
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnContinueAsGuest = (Button) findViewById(R.id.btnLoginAsGuest);
    }

    private void handleEvents() {
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View view) {
                new AsyncTask<Void, Void, User>() {
                    protected void onPreExecute() {
                        prgLoading.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected User doInBackground(Void... voids) {
                        User user = userDao.getUserByUsername(txtUsername.getText().toString());

                        if (user.getPassword().equals(txtPassword.getText().toString())) {
                            return user;
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(User user) {
                        prgLoading.setVisibility(View.GONE);

                        if (user != null) {
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("user_id", user.getUser_id());
                            startActivity(intent);
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }.execute();
            }
        });

        btnContinueAsGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();

                User user = new User();
                user.setUser_id(-1);
                UserSingleton.setInstance(user);

                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("user_id", -1);
                intent.putExtra("dirRes", DirResSingleton.getInstance());
                startActivity(intent);
            }
        });
    }

    private void loadData(){
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                prgLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                createDirRes();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                prgLoading.setVisibility(View.INVISIBLE);
            }
        };
        try {
            task.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void createDirRes(){
        dirRes = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "//Resources//");
        DirResSingleton.setInstance(dirRes);
        createDir(dirRes);
        File dirData = new File(dirRes.getAbsolutePath() + "//Data//");
        createDir(dirData);
        File fileVoorkeuren = new File(dirData.getAbsolutePath() + "//Voorkeuren.uaz");
        createFile(fileVoorkeuren);
        File fileProfiel = new File(dirData.getAbsolutePath() + "//Profiel.uaz");
        createFile(fileProfiel);
        File fileShiften = new File(dirData.getAbsolutePath() + "//Shiften.uaz");
        createFile(fileShiften);
        File fileCollegas = new File(dirData.getAbsolutePath() + "//Collegas.uaz");
        createFile(fileCollegas);
        File dirWerk = new File(dirRes.getAbsolutePath() + "//Werk//");
        createDir(dirWerk);
        File dirWissels = new File(dirRes.getAbsolutePath() + "//Wissels//");
        createDir(dirWissels);
        File dirPersoonlijk = new File(dirRes.getAbsolutePath() + "//Persoonlijk//");
        createDir(dirPersoonlijk);

        resLoaded = true;
    }

    private void createFile(File file){
        try {
            if (file.exists()) {
                if (!file.isFile()) {
                    file.delete();
                    file.createNewFile();
                }
            }else {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDir(File dir){
        if (dir.exists()){
            if (!dir.isDirectory()){
                dir.delete();
                dir.mkdirs();
            }
        }else{
            dir.mkdirs();
        }
    }
}
