package com.vermolen.uurrooster.DB;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.vermolen.uurrooster.Classes.CalendarSingletons;
import com.vermolen.uurrooster.Classes.UserSingleton;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CollegasDao {
    private ConnectionClass connectionClass;
    private String z;

    public CollegasDao(){
        connectionClass = new ConnectionClass();
    }

    public List<String> getCollegas() {
        Connection con = null;
        List<String> data = new ArrayList<>();

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {

                String query = "Select * From cal_collegas Where user_id='" + UserSingleton.getInstance().getUser_id() + "'";
                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()){
                    data.add(rs.getString(3));
                }

                Collections.sort(data, new Comparator<String>() {
                    @Override
                    public int compare(String s, String t1) {
                        return s.compareTo(t1);
                    }
                });
            }
        } catch (Exception ex) {
            z = "Exceptions";
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return data;
    }

    public int getCollegaIdByName(String name) {
        Connection con = null;

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {

                String query = "Select * From cal_collegas Where user_id='" + UserSingleton.getInstance().getUser_id() + "' and collega='" + name + "'";
                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(query);

                try{
                    rs.first();
                    return rs.getInt(2);
                }catch (Exception e){
                    z = "No records";
                }
            }
        } catch (Exception ex) {
            z = "Exceptions";
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return -1;
    }

    public void editCollega(String oudeText, String nieuweText) {
        int id = getCollegaIdByName(oudeText);

        if (id != -1) {
            Connection con = null;

            try {
                con = connectionClass.CONN();

                if (con == null) {
                    z = "Error in connection with SQL server";
                } else {

                    String query = "update cal_collegas set collega='" + nieuweText + "' where collega_id=" + id + " and user_id=" + UserSingleton.getInstance().getUser_id() + ";";
                    Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    stmt.executeUpdate(query);
                }
            } catch (Exception ex) {
                z = "Exceptions";
            } finally {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addCollega(String collega){
        Connection con = null;

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                int count;

                String qryCount = "Select Max(collega_id) From cal_collegas Where user_id=" + UserSingleton.getInstance().getUser_id();
                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(qryCount);

                try{
                    rs.first();
                    count = rs.getInt(1);
                }catch (Exception e){ count = -1; }

                String query = "Insert Into cal_collegas Values(" +
                        UserSingleton.getInstance().getUser_id() + ", " +
                        ++count + ", '" +
                        collega + "');";
                stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                stmt.executeUpdate(query);
            }
        } catch (Exception ex) {
            z = "Exceptions";
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeCollega(String collega) {
        int id = getCollegaIdByName(collega);

        if (id != -1) {
            Connection con = null;

            try {
                con = connectionClass.CONN();

                if (con == null) {
                    z = "Error in connection with SQL server";
                } else {

                    String query = "Delete From cal_collegas Where collega_id=" + id + " AND user_id=" + UserSingleton.getInstance().getUser_id();
                    Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    stmt.executeUpdate(query);
                }
            } catch (Exception ex) {
                z = "Exceptions";
            } finally {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
