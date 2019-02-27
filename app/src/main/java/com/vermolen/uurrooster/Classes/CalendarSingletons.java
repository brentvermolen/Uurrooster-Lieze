package com.vermolen.uurrooster.Classes;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.vermolen.uurrooster.Classes.Enums.Maand;
import com.vermolen.uurrooster.Classes.Enums.Voorkeur;
import com.vermolen.uurrooster.DB.CollegasDao;
import com.vermolen.uurrooster.DB.DataDao;
import com.vermolen.uurrooster.DB.ShiftenDao;
import com.vermolen.uurrooster.DB.VoorkeurenDao;
import com.vermolen.uurrooster.R;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CalendarSingletons {
    private static CollegasDao collegasDao = new CollegasDao();
    private static ShiftenDao shiftenDao = new ShiftenDao();
    private static DataDao dataDao = new DataDao();
    private static VoorkeurenDao voorkeurenDao = new VoorkeurenDao();

    private static Map<String, List<String>> shiften;
    private static List<String> collegas;
    private static Map<Voorkeur, String> voorkeuren;

    private static Map<Integer, List<String>> werkData;
    private static Maand werkMaand;
    private static int werkJaar;

    private static Map<Integer, List<String>> wisselData;
    private static Maand wisselMaand;
    private static int wisselJaar;

    private static Map<Integer, List<String>> persoonlijkData;
    private static Maand persoonlijkMaand;
    private static int persoonlijkJaar;

    public static Map<String, List<String>> getShiften(){
        if (shiften == null) {
            shiften = shiftenDao.getShiften();
        }

        return shiften;
    }

    public static ArrayAdapter<String> getShiftenAdapter(Context context, int layoutRes){
        if (shiften == null){
            shiften = shiftenDao.getShiften();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, layoutRes);
        adapter.addAll(shiften.keySet());

        return adapter;
    }

    public static ArrayAdapter<String> getCollegasAdapter(Context context, int layoutRes){
        if (collegas == null){
            collegas = collegasDao.getCollegas();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, layoutRes);
        adapter.addAll(collegas);

        return adapter;
    }

    public static List<String> getCollegasAdapter() {
        if (collegas == null){
            collegas = collegasDao.getCollegas();
        }

        return collegas;
    }

    public static Map<Voorkeur, String> getVoorkeuren(){
        if (voorkeuren == null){
            voorkeuren = voorkeurenDao.getVoorkeuren();
        }

        return voorkeuren;
    }

    public static void editVoorkeur(Voorkeur voorkeur, String value){
        if (voorkeuren != null){
            voorkeuren.put(voorkeur, value);
        }
    }

    public static Map<Integer, List<String>> getWerkData(Maand maand, int jaar){
        if (werkData == null || werkMaand.getNr() != maand.getNr() || werkJaar != jaar){
            werkData = dataDao.getWerkData(maand, jaar);

            werkMaand = maand;
            werkJaar = jaar;
        }

        return werkData;
    }

    public static Map<Integer, List<String>> getWisselData(Maand maand, int jaar){
        if (wisselData == null || wisselMaand.getNr() != maand.getNr() || wisselJaar != jaar){
            wisselData = dataDao.getWisselData(maand, jaar);

            wisselMaand = maand;
            wisselJaar = jaar;
        }

        return wisselData;
    }

    public static Map<Integer, List<String>> getPersoonlijkData(Maand maand, int jaar){
        if (persoonlijkData == null || persoonlijkMaand.getNr() != maand.getNr() || persoonlijkJaar != jaar){
            persoonlijkData = dataDao.getPersoonlijkData(maand, jaar);

            persoonlijkMaand = maand;
            persoonlijkJaar = jaar;
        }

        return persoonlijkData;
    }

    public static void addShift(String shift, List<String> shiftData) {
        if (shiften == null){
            shiften = shiftenDao.getShiften();
        }

        shiften.put(shift, shiftData);
    }

    public static void editShift(String oud, String shift, List<String> shiftData) {
        if (shiften == null){
            shiften = shiftenDao.getShiften();
        }

        removeShift(oud);
        addShift(shift, shiftData);

        Set<Integer> keySet = werkData.keySet();
        for (Integer key : keySet){
            List<String> sd = werkData.get(key);
            if (sd != null){
                if (sd.get(0).equals(oud)){
                    werkData.put(key, Arrays.asList(shift));
                }
            }
        }

        keySet = wisselData.keySet();
        for (Integer key : keySet){
            List<String> sd = wisselData.get(key);
            if (sd != null){
                if (sd.get(1).equals(oud)){
                    wisselData.put(key, Arrays.asList(sd.get(0), shift, sd.get(2), sd.get(3)));
                }
            }
        }
    }

    public static void removeShift(String shift) {
        if (shiften == null){
            shiften = shiftenDao.getShiften();
        }

        shiften.remove(shift);
    }

    public static void addCollega(String collega){
        if (collegas != null){
            collegas.add(collega);
        }
    }

    public static void editCollega(String oud, String nieuw){
        removeCollega(oud);
        addCollega(nieuw);
    }

    public static void removeCollega(String collega){
        if (collegas != null){
            collegas.remove(collega);
        }
    }

    public static void removeWerkData(int dag) {
        if (werkData != null){
            werkData.remove(dag);
        }
    }

    public static void addWerkData(int dag, List<String> shift) {
        if (werkData != null){
            werkData.put(dag, shift);
        }
    }

    public static void addWisselData(int dag, List<String> shift) {
        if (wisselData != null){
            wisselData.put(dag, shift);
        }
    }

    public static void addPersoonlijkData(int dag, List<String> shift) {
        if (persoonlijkData != null){
            persoonlijkData.put(dag, shift);
        }
    }

    public static void removeWisselData(int dag) {
        if (wisselData != null){
            wisselData.remove(dag);
        }
    }

    public static void editWerkShift(String shift, int dag) {
        if (werkData != null){
            werkData.put(dag, Arrays.asList(shift));
        }
    }

    public static void removePersoonlijkeData(int dag) {
        if (persoonlijkData != null){
            persoonlijkData.remove(dag);
        }
    }
}
