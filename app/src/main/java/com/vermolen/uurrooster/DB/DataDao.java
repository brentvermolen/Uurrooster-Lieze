package com.vermolen.uurrooster.DB;

import com.vermolen.uurrooster.Classes.Enums.Maand;
import com.vermolen.uurrooster.Classes.Enums.Voorkeur;
import com.vermolen.uurrooster.Classes.UserSingleton;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataDao {
    private ConnectionClass connectionClass;
    private String z;

    public DataDao(){
        connectionClass = new ConnectionClass();
    }

    public void removeWerk(int dag, Maand maand, int jaar) {
        int intMaand = maand.getNr();
        Connection con = null;

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                String qryDelete = "Delete From cal_werk Where " +
                        "user_id=" + UserSingleton.getInstance().getUser_id() + " AND " +
                        "dag=" + dag + " AND " +
                        "maand=" + intMaand + " AND " +
                        "jaar=" + jaar;

                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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

    public void addWerkShift(String shift, int dag, Maand maand, int jaar) {
        int intMaand = maand.getNr();
        Connection con = null;

        removeWerk(dag, maand, jaar);

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                String qryInsert = "Insert Into cal_werk Values(" +
                        UserSingleton.getInstance().getUser_id() + ", " +
                        dag + ", " +
                        intMaand + ", " +
                        jaar + ", '" +
                        shift + "');";

                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                stmt.executeUpdate(qryInsert);
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

    public Map<Integer, List<String>> getWerkData(Maand maand, int jaar) {
        Map<Integer, List<String>> werkData = new HashMap<>();

        Connection con = null;

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                String query = "Select * From cal_werk Where maand=" + maand.getNr() + " AND jaar=" + jaar + " AND user_id=" + UserSingleton.getInstance().getUser_id();

                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()){
                    List<String> data = new ArrayList<>();
                    data.add(rs.getString(5));

                    werkData.put(rs.getInt(2), data);
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

        return werkData;
    }

    public Map<Integer, List<String>> getWisselData(Maand maand, int jaar) {
        Map<Integer, List<String>> wisselData = new HashMap<>();

        Connection con = null;

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                String query = "Select * From cal_wissel Where maand=" + maand.getNr() + " AND jaar=" + jaar + " AND user_id=" + UserSingleton.getInstance().getUser_id();

                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()){
                    List<String> data = new ArrayList<>();
                    data.add(rs.getString(5));
                    data.add(rs.getString(6));
                    data.add(rs.getString(7));
                    data.add(rs.getString(8));

                    wisselData.put(rs.getInt(2), data);
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

        return wisselData;
    }

    public Map<Integer, List<String>> getPersoonlijkData(Maand maand, int jaar) {
        Map<Integer, List<String>> persoonlijkData = new HashMap<>();

        Connection con = null;

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                String query = "Select * From cal_persoonlijk Where maand=" + maand.getNr() + " AND jaar=" + jaar + " AND user_id=" + UserSingleton.getInstance().getUser_id();

                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()){
                    List<String> data = new ArrayList<>();
                    String tekst = rs.getString(5);
                    tekst = tekst.replace("\\n", "\n").replace("/", "\\");

                    data.add(tekst);

                    persoonlijkData.put(rs.getInt(2), data);
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

        return persoonlijkData;
    }

    public void writeWissel(String shift, String collega, String richting, String zo, int dag, Maand maand, int jaar) {
        int intMaand = maand.getNr();
        Connection con = null;

        removeWissel(dag, maand, jaar);

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                String qryInsert = "Insert Into cal_wissel Values(" +
                        UserSingleton.getInstance().getUser_id() + ", " +
                        dag + ", " +
                        intMaand + ", " +
                        jaar + ", '" +
                        collega + "', '" +
                        shift + "', '" +
                        richting + "', '" +
                        zo + "');";

                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                stmt.executeUpdate(qryInsert);
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

    public void removeWissel(int dag, Maand maand, int jaar) {
        int intMaand = maand.getNr();
        Connection con = null;

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                String qryDelete = "Delete From cal_wissel Where " +
                        "user_id=" + UserSingleton.getInstance().getUser_id() + " AND " +
                        "dag=" + dag + " AND " +
                        "maand=" + intMaand + " AND " +
                        "jaar=" + jaar;

                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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

    public void editWerkShift(String shift, int dag, Maand maand, int jaar) {
        int intMaand = maand.getNr();
        Connection con = null;

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                String qryDelete = "Update cal_werk Set shift='" + shift + "' Where " +
                        "user_id=" + UserSingleton.getInstance().getUser_id() + " AND " +
                        "dag=" + dag + " AND " +
                        "maand=" + intMaand + " AND " +
                        "jaar=" + jaar;

                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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

    public void removePersoonlijk(int dag, Maand maand, int jaar) {
        int intMaand = maand.getNr();
        Connection con = null;

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                String qryDelete = "Delete From cal_persoonlijk Where " +
                        "user_id=" + UserSingleton.getInstance().getUser_id() + " AND " +
                        "dag=" + dag + " AND " +
                        "maand=" + intMaand + " AND " +
                        "jaar=" + jaar;

                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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

    public void writePersoonlijk(String tekst, int dag, Maand maand, int jaar) {
        int intMaand = maand.getNr();
        Connection con = null;

        removePersoonlijk(dag, maand, jaar);

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                String qryInsert = "Insert Into cal_persoonlijk Values(" +
                        UserSingleton.getInstance().getUser_id() + ", " +
                        dag + ", " +
                        intMaand + ", " +
                        jaar + ", '" +
                        tekst + "');";

                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                stmt.executeUpdate(qryInsert);
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

    public void writeVoorkeur(Voorkeur voorkeur, String value) {
        Connection con = null;

        removeVoorkeur(voorkeur);

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                String qryInsert = "Insert Into cal_voorkeuren Values(" +
                        UserSingleton.getInstance().getUser_id() + ", " +
                        voorkeur.getNr() + ", '" +
                        value + "');";

                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                stmt.executeUpdate(qryInsert);
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

    private void removeVoorkeur(Voorkeur voorkeur) {
        Connection con = null;

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                String qryDelete = "Delete From cal_voorkeuren Where " +
                        "user_id=" + UserSingleton.getInstance().getUser_id() + " AND " +
                        "voorkeur=" + voorkeur.getNr();

                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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

    public Map<Integer, List<String>> getArchief() {
        Map<Integer, List<String>> archief = new HashMap<>();

        Connection con = null;

        try {
            con = connectionClass.CONN();

            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                String qrySelectWerk = "Select Distinct maand, jaar From cal_werk Where user_id=" + UserSingleton.getInstance().getUser_id();

                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(qrySelectWerk);

                List<String> lstMaanden;
                while (rs.next()){
                    if (archief.containsKey(rs.getInt(2))){
                        lstMaanden = archief.get(rs.getInt(2));
                        archief.remove(rs.getInt(2));
                        lstMaanden.add(Maand.valueOf(rs.getInt(1)).toString());
                        archief.put(rs.getInt(2), lstMaanden);
                    }else{
                        lstMaanden = new ArrayList<>();
                        lstMaanden.add(Maand.valueOf(rs.getInt(1)).toString());
                        archief.put(rs.getInt(2), lstMaanden);
                    }
                }

                String qrySelectPersoonlijk = "Select Distinct maand, jaar From cal_persoonlijk Where user_id=" + UserSingleton.getInstance().getUser_id();

                stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                rs = stmt.executeQuery(qrySelectPersoonlijk);

                while (rs.next()){
                    if (archief.containsKey(rs.getInt(2))){
                        lstMaanden = archief.get(rs.getInt(2));
                        archief.remove(rs.getInt(2));
                        lstMaanden.add(Maand.valueOf(rs.getInt(1)).toString());
                        archief.put(rs.getInt(2), lstMaanden);
                    }else{
                        lstMaanden = new ArrayList<>();
                        lstMaanden.add(Maand.valueOf(rs.getInt(1)).toString());
                        archief.put(rs.getInt(2), lstMaanden);
                    }
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

        return archief;
    }
}
