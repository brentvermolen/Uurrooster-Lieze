package com.vermolen.uurrooster.DB;

import android.os.AsyncTask;

import com.vermolen.uurrooster.Model.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDao{
    private ConnectionClass connectionClass;
    private String z;

    public UserDao(){
        connectionClass = new ConnectionClass();
    }

    public User getUserByUsername(String username){
        Connection con = null;
        List<User> data = new ArrayList<>();

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {

                String query = "Select * From dbo.cal_users Where username='" + username + "'";
                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()){
                    User user = new User();
                    user.setUser_id(rs.getInt(1));
                    user.setUsername(rs.getString(2));
                    user.setPassword(rs.getString(3));

                    data.add(user);
                }
            }
        } catch (Exception ex) {
            z = "Exceptions";
        } finally {
            try {
                con.close();
            } catch (NullPointerException | SQLException e) {
                e.printStackTrace();
            }
        }

        if (data.size() > 0){
            return data.get(0);
        }else{
            return null;
        }
    }

    public User getUserById(int user_id){
        Connection con = null;
        List<User> data = new ArrayList<>();

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {

                String query = "Select * From dbo.cal_users Where user_id='" + user_id + "'";
                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()){
                    User user = new User();
                    user.setUser_id(rs.getInt(1));
                    user.setUsername(rs.getString(2));
                    user.setPassword(rs.getString(3));

                    data.add(user);
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

        if (data.size() > 0){
            return data.get(0);
        }else{
            return null;
        }
    }

    public boolean addUser(User user){
        Connection con = null;
        boolean success = false;

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                String qryCount = "Select Max(user_id) From dbo.cal_users";
                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(qryCount);

                rs.first();
                int count = rs.getInt(1);

                String query = "Insert Into dbo.cal_users Values (" + ++count + ", '" + user.getUsername() + "', '" + user.getPassword() + "')";
                stmt.executeUpdate(query);
            }

            success = true;
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
}
