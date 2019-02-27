package com.vermolen.uurrooster.Classes;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.vermolen.uurrooster.Classes.Enums.Maand;
import com.vermolen.uurrooster.Classes.Enums.Voorkeur;
import com.vermolen.uurrooster.DB.DataDao;

import java.util.List;
import java.util.Map;

public class Reader {
    private final static DataDao dataDao = new DataDao();

    public static ArrayAdapter<String> getShiften(Context context, int layoutRes){
        if (UserSingleton.getInstance().getUser_id() == -1){
            return TextReader.getShiften(context, layoutRes, DirResSingleton.getInstance());
        }else{
            //return shiftenDao.getShiften(context, layoutRes);
            return CalendarSingletons.getShiftenAdapter(context, layoutRes);
        }
    }

    public static Map<String, List<String>> getShiften(){
        if (UserSingleton.getInstance().getUser_id() == -1){
            return TextReader.getShiften(DirResSingleton.getInstance());
        }else{
            //return shiftenDao.getShiften();
            return CalendarSingletons.getShiften();
        }
    }

    public static ArrayAdapter<String> getCollegas(Context context, int layoutRes){
        if (UserSingleton.getInstance().getUser_id() == -1){
            return TextReader.getCollegas(context, layoutRes, DirResSingleton.getInstance());
        }else{
            //return collegasDao.getCollegasAdapter(context, layoutRes);
            return CalendarSingletons.getCollegasAdapter(context, layoutRes);
        }
    }

    public static List<String> getCollegas() {
        if (UserSingleton.getInstance().getUser_id() == -1){
            return TextReader.getCollegas(DirResSingleton.getInstance());
        }else{
            //return shiftenDao.getShiften();
            return CalendarSingletons.getCollegasAdapter();
        }
    }

    public static Map<Integer, List<String>> getWerkData(Maand maand, int jaar) {
        if (UserSingleton.getInstance().getUser_id() == -1){
            return TextReader.getWerkData(maand, jaar, DirResSingleton.getInstance());
        }else{
            //return dataDao.getWerkData(maand, jaar);
            return CalendarSingletons.getWerkData(maand, jaar);
        }
    }

    public static Map<Integer, List<String>> getWisselData(Maand maand, int jaar) {
        if (UserSingleton.getInstance().getUser_id() == -1){
            return TextReader.getWisselData(maand, jaar, DirResSingleton.getInstance());
        }else{
            //return dataDao.getWisselData(maand, jaar);
            return CalendarSingletons.getWisselData(maand, jaar);
        }
    }

    public static Map<Integer, List<String>> getPersoonlijkData(Maand maand, int jaar) {
        if (UserSingleton.getInstance().getUser_id() == -1){
            return TextReader.getPersoonlijkData(maand, jaar, DirResSingleton.getInstance());
        }else{
            //return dataDao.getPersoonlijkData(maand, jaar);
            return CalendarSingletons.getPersoonlijkData(maand, jaar);
        }
    }

    public static Map<Voorkeur, String> getVoorkeuren() {
        if (UserSingleton.getInstance().getUser_id() == -1){
            return TextReader.getVoorkeuren(DirResSingleton.getInstance());
        }else{
            return CalendarSingletons.getVoorkeuren();
        }
    }

    public static Map<Integer, List<String>> getArchief() {
        if (UserSingleton.getInstance().getUser_id() == -1){
            return TextReader.getArchief(DirResSingleton.getInstance());
        }else{
            return dataDao.getArchief();
        }
    }
}
