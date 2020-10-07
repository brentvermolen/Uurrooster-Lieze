package com.vermolen.uurrooster.DB;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import org.postgresql.util.PSQLException;

import java.net.ConnectException;
import java.sql.*;

public class ConnectionClass {
    /*String ip = "184.168.194.64";
    String classs = "net.sourceforge.jtds.jdbc.Driver";
    String db = "brentvermolen-trakt";
    String un = "brentvermolen";
    String password = "R~xh0v10";*/

/*    String ip = "5.10.80.108:780";
    String classs = "net.sourceforge.jtds.jdbc.Driver";
    String db = "vermolens_trakt";
    String un = "vermolens_brent";
    String password = "Mo%13mp5";*/

//    String ip = "192.168.0.114:5432";
    public static boolean isLocal = false;

    String ip = "81.83.203.251:5432";
    String classs = "org.postgresql.Driver";
    String db = "calendar";
    String un = "admin";
    String password = "nimda";

    @SuppressLint("NewApi")
    public Connection CONN() {
        if (isLocal) {
            ip = "192.168.0.114:5432";
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String ConnURL = null;
        try {
            try{
                Class.forName(classs);
                ConnURL = "jdbc:postgresql://" + ip + "/" + db;
                conn = DriverManager.getConnection(ConnURL, un, password);
            }catch (Exception psqle) {
                // Attempt to connect locally
                ip = "192.168.0.114:5432";

                Class.forName(classs);
                ConnURL = "jdbc:postgresql://" + ip + "/" + db;
                conn = DriverManager.getConnection(ConnURL, un, password);
                isLocal = true;
            }
        } catch (SQLException se) {
            Log.e("ERRO", se.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("ERRO", e.getMessage());
        } catch (Exception e) {
            Log.e("ERRO", e.getMessage());
        }
        return conn;
    }
}
