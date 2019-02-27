package com.vermolen.uurrooster.DB;

import com.vermolen.uurrooster.Classes.Enums.Voorkeur;
import com.vermolen.uurrooster.Classes.UserSingleton;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class VoorkeurenDao {
    private ConnectionClass connectionClass;
    private String z;

    public VoorkeurenDao(){
        connectionClass = new ConnectionClass();
    }

    public Map<Voorkeur, String> getVoorkeuren() {
        SortedMap<Voorkeur, String> voorkeuren = new TreeMap<>(new Comparator<Voorkeur>() {
            @Override
            public int compare(Voorkeur o1, Voorkeur o2) {
                return o1.getNr() - o2.getNr();
            }
        });

        Connection con = null;

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                String query = "Select * From cal_voorkeuren Where user_id=" + UserSingleton.getInstance().getUser_id();

                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(query);

                for(byte bytTeller = 0; bytTeller < Voorkeur.values().length; bytTeller++){
                    Voorkeur v = Voorkeur.valueOfNr(bytTeller++);
                    voorkeuren.put(v, "");
                }

                while (rs.next()){
                    voorkeuren.put(Voorkeur.valueOfNr(rs.getInt(2)), rs.getString(3));
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

        return voorkeuren;
    }
}
