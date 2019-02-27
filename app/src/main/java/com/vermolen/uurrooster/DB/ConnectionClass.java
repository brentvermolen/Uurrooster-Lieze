package com.vermolen.uurrooster.DB;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionClass {
    /*String ip = "184.168.194.64";
    String classs = "net.sourceforge.jtds.jdbc.Driver";
    String db = "brentvermolen-trakt";
    String un = "brentvermolen";
    String password = "R~xh0v10";*/

    String ip = "5.10.80.108:780";
    String classs = "net.sourceforge.jtds.jdbc.Driver";
    String db = "vermolens_trakt";
    String un = "vermolens_brent";
    String password = "Mo%13mp5";

    @SuppressLint("NewApi")
    public Connection CONN() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String ConnURL = null;
        try {
            Class.forName(classs);
            ConnURL = "jdbc:jtds:sqlserver://" + ip + ";"
                    + "databaseName=" + db + ";user=" + un + ";password="
                    + password + ";";
            conn = DriverManager.getConnection(ConnURL);
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
