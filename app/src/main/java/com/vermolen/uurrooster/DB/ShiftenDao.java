package com.vermolen.uurrooster.DB;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.vermolen.uurrooster.Classes.CalendarSingletons;
import com.vermolen.uurrooster.Classes.UserSingleton;
import com.vermolen.uurrooster.Model.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftenDao {
    private ConnectionClass connectionClass;
    private String z;

    public ShiftenDao(){
        connectionClass = new ConnectionClass();
    }

    public ArrayAdapter<String> getShiften(Context context, int layoutRes){
        Connection con = null;
        ArrayAdapter<String> data = new ArrayAdapter<>(context, layoutRes);

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {

                String query = "Select * From dbo.cal_shiften Where user_id='" + UserSingleton.getInstance().getUser_id() + "'";
                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()){
                    data.add(rs.getString(3));
                }

                data.sort(new Comparator<String>() {
                    @Override
                    public int compare(String s, String t1) {
                        return s.toLowerCase().compareTo(t1.toLowerCase());
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

    public Map<String, List<String>> getShiften(){
        Map<String, List<String>> shiften = new HashMap<>();
        Connection con = null;

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {

                String query = "Select * From dbo.cal_shiften Where user_id='" + UserSingleton.getInstance().getUser_id() + "'";
                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()){
                    List<String> shift = new ArrayList<>();
                    shift.add(rs.getString(4));
                    shift.add(rs.getString(5));
                    shift.add(rs.getString(6));
                    shift.add(rs.getString(7));
                    shift.add(rs.getString(8));
                    shift.add(rs.getString(9));

                    shiften.put(rs.getString(3), shift);
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

        return shiften;
    }

    public boolean addShift(String shift, List<String> shiftData){
        Connection con = null;

        boolean success = false;

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                String qryCount = "Select Max(shift_id) From dbo.cal_shiften Where user_id=" + UserSingleton.getInstance().getUser_id() + ";";
                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(qryCount);

                int count;
                try{
                    rs.first();
                    count = rs.getInt(1);
                }catch (Exception e) {  count = 0; }

                String qryInsert = "Insert Into dbo.cal_shiften Values(" +
                        UserSingleton.getInstance().getUser_id() + ", " +
                        ++count + ", '" +
                        shift + "', '" +
                        shiftData.get(0) + "', " +
                        shiftData.get(1)  + ", " +
                        shiftData.get(2) + ", " +
                        shiftData.get(3) + ", " +
                        shiftData.get(4) + ", " +
                        shiftData.get(5) + ");";
                stmt.executeUpdate(qryInsert);

                success = true;
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

        return success;
    }

    public void editShift(String oud, String nieuw, List<String> shiftData) {
        Connection con = null;

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                String qryCount = "Select shift_id From dbo.cal_shiften Where shift='" + oud + "' and user_id=" + UserSingleton.getInstance().getUser_id() + ";";
                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(qryCount);

                int shift_id;
                try{
                    rs.first();
                    shift_id = rs.getInt(1);
                }catch (Exception e) {
                    throw new IllegalArgumentException("Shift not found");
                }

                String qryUpdate = "Update dbo.cal_shiften Set " +
                        "shift='" + nieuw + "', extra='" +
                        shiftData.get(0) + "', \"begin\"=" +
                        shiftData.get(1)  + ", beginMin=" +
                        shiftData.get(2) + ", eind=" +
                        shiftData.get(3) + ", eindMin=" +
                        shiftData.get(4) + ", kleur=" +
                        shiftData.get(5) +
                        " Where user_id=" + UserSingleton.getInstance().getUser_id() +
                        " and shift_id=" + shift_id;
                stmt.executeUpdate(qryUpdate);

                String qryUpdateWerkData = "Update dbo.cal_werk Set shift='" + nieuw + "' Where shift='" + oud + "'";
                stmt.executeUpdate(qryUpdateWerkData);

                String qryUpdateWisselData = "Update dbo.cal_wissel Set shift='" + nieuw + "' Where shift='" + oud + "'";
                stmt.executeUpdate(qryUpdateWisselData);
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

    public void removeShift(String shift) {
        Connection con = null;

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                String qryCount = "Select shift_id From dbo.cal_shiften Where shift='" + shift + "' and user_id=" + UserSingleton.getInstance().getUser_id() + ";";
                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(qryCount);

                int shift_id;
                try {
                    rs.first();
                    shift_id = rs.getInt(1);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Shift not found");
                }

                String qryDelete = "Delete From dbo.cal_shiften Where " +
                        "user_id=" + UserSingleton.getInstance().getUser_id() + " AND " +
                        "shift_id=" + shift_id + ";";

                stmt.executeUpdate(qryDelete);
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
