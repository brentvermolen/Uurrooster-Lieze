package com.vermolen.uurrooster.Classes;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.vermolen.uurrooster.Classes.Enums.Maand;
import com.vermolen.uurrooster.Classes.Enums.Voorkeur;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by Brent on 20/11/2017.
 */

public class TextReader {
    public static ArrayAdapter<String> getShiften(Context context, int layoutRes, File dirRes){
        ArrayAdapter<String> shiften = new ArrayAdapter<String>(context, layoutRes);

        try {
            BufferedReader br = new BufferedReader(new FileReader(dirRes.getAbsolutePath() + "/Data/Shiften.uaz"));
            String line;

            while ((line = br.readLine()) != null) {
                String[] split = line.split(";");

                shiften.add(split[0]);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        shiften.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }
        });

        return shiften;
    }

    public static Map<String, List<String>> getShiften(File dirRes){
        Map<String, List<String>> shiften = new HashMap<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(dirRes.getAbsolutePath() + "/Data/Shiften.uaz"));
            String line;

            while ((line = br.readLine()) != null) {
                String[] split = line.split(";");
                List<String> data = new ArrayList<>();
                data.add(split[1]);
                data.add(split[2]);
                data.add(split[3]);
                data.add(split[4]);
                data.add(split[5]);
                try{
                    data.add(split[6]);
                }catch (Exception e){
                    data.add("0");
                }

                shiften.put(split[0], data);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return shiften;
    }

    public static ArrayAdapter<String> getCollegas(Context context, int layoutRes, File dirRes){
        ArrayAdapter<String> collegas = new ArrayAdapter<String>(context, layoutRes);

        try {
            BufferedReader br = new BufferedReader(new FileReader(dirRes.getAbsolutePath() + "/Data/Collegas.uaz"));
            String line;

            while ((line = br.readLine()) != null) {
                collegas.add(line);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        collegas.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }
        });

        return collegas;
    }

    public static ArrayList<String> getCollegas(File dirRes){
        ArrayList<String> collegas = new ArrayList<String>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(dirRes.getAbsolutePath() + "/Data/Collegas.uaz"));
            String line;

            while ((line = br.readLine()) != null) {
                collegas.add(line);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(collegas, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }
        });

        return collegas;
    }

    public  static Map<Integer, List<String>> getWerkData(Maand maand, int jaar, File dirRes){
        Map<Integer, List<String>> werkData = new HashMap<>();

        File bestand = new File(dirRes.getAbsolutePath() + "/Werk/" + String.valueOf(jaar) + "/" + maand.toString() + ".uaz");
        if (bestand.exists())
        {
            try {
                BufferedReader br = new BufferedReader(new FileReader(bestand));

                String lijn = "";

                while ((lijn = br.readLine()) != null)
                {
                    String[] split = lijn.split(";");
                    List<String> data = new ArrayList<>();
                    int dag = Integer.valueOf(split[0]);
                    data.add(split[1]);

                    werkData.put(dag, data);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return werkData;
    }

    public static Map<Integer, List<String>> getWisselData(Maand maand, int jaar, File dirRes) {
        Map<Integer, List<String>> wisselData = new HashMap<>();

        File bestand = new File(dirRes.getAbsolutePath() + "/Wissels/" + String.valueOf(jaar) + "/" + maand.toString() + ".uaz");
        if (bestand.exists())
        {
            try {
                BufferedReader br = new BufferedReader(new FileReader(bestand));

                String lijn = "";

                while ((lijn = br.readLine()) != null)
                {
                    String[] split = lijn.split(";");
                    List<String> data = new ArrayList<>();
                    int dag = Integer.valueOf(split[0]);
                    String collega = split[1];
                    String shift = split[2];
                    String richting = split[3];
                    String isZiekteOpvang = split[4];

                    data.add(collega);
                    data.add(shift);
                    data.add(richting);
                    data.add(isZiekteOpvang);

                    wisselData.put(dag, data);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return wisselData;
    }

    public static Map<Integer, List<String>> getPersoonlijkData(Maand maand, int jaar, File dirRes) {
        Map<Integer, List<String>> persoonlijkData = new HashMap<>();

        File bestand = new File(dirRes.getAbsolutePath() + "/Persoonlijk/" + String.valueOf(jaar) + "/" + maand.toString() + ".uaz");

        if (bestand.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(bestand));

                String lijn = "";

                while ((lijn = br.readLine()) != null) {
                    String[] split = lijn.split(";");
                    List<String> data = new ArrayList<>();
                    int dag = Integer.valueOf(split[0]);
                    data.add(split[1].replace("\\n", "\n"));

                    persoonlijkData.put(dag, data);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return persoonlijkData;
    }

    /*public static String readVoorkeur(Voorkeur voorkeur, Context context){
        SharedPreferences sharedPref = context.getSharedPreferences("Voorkeuren", Context.MODE_PRIVATE);
        String strVoorkeur = voorkeur.toString();
        String result = sharedPref.getString(strVoorkeur, "0");

        return result;
    }*/

    public static Map<Voorkeur, String> getVoorkeuren(File dirRes) {
        SortedMap<Voorkeur, String> voorkeuren = new TreeMap<>(new Comparator<Voorkeur>() {
            @Override
            public int compare(Voorkeur o1, Voorkeur o2) {
                return o1.getNr() - o2.getNr();
            }
        });

        File bestand = new File(dirRes.getAbsolutePath() + "/Data/Voorkeuren.uaz");
        if (bestand.exists()){
            try{
                BufferedReader br = new BufferedReader(new FileReader(bestand));

                String lijn = "";
                while ((lijn = br.readLine()) != null){
                    String[] split = lijn.split(";");

                    byte bytTeller = 0;
                    for (Voorkeur v : Voorkeur.values()){
                        v = Voorkeur.valueOfNr(bytTeller);
                        try{
                            voorkeuren.put(v, split[bytTeller]);
                        }catch (Exception e){
                            voorkeuren.put(v, "");
                        }
                        bytTeller++;
                    }
                }
                if (voorkeuren.size() == 0){
                    byte bytTeller = 0;
                    for (Voorkeur v : Voorkeur.values()){
                        v = Voorkeur.valueOfNr(bytTeller);
                        voorkeuren.put(v, "");
                        bytTeller++;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return voorkeuren;
    }

    /*public static Map<Voorkeur, String> getVoorkeuren(File dirRes) {
        Map<Voorkeur, String> voorkeuren = new HashMap<>();

        File bestand = new File(dirRes.getAbsolutePath() + "/Data/Voorkeuren.uaz");
        if (bestand.exists()){
            try {
                BufferedReader br = new BufferedReader(new FileReader(bestand));

                String lijn = "";
                while ((lijn = br.readLine()) != null){
                    String[] split = lijn.split(";");

                    byte bytTeller = 0;
                    for (String str : split){
                        Voorkeur voorkeur = null;
                        switch (bytTeller){
                            case 0:
                                voorkeur = Voorkeur.BACKGROUND_SHIFT_CHANGE;
                                break;
                            case 1:
                                voorkeur = Voorkeur.BACKGROUND_CALENDAR_HEADER;
                                break;
                            case 2:
                                voorkeur = Voorkeur.BACKGROUND_DAYS;
                                break;
                            case 3:
                                voorkeur = Voorkeur.BACKGROUND_DAYS_HEADER;
                                break;
                            case 4:
                                voorkeur = Voorkeur.BACKGROUND_CALENDAR;
                                break;
                            case 5:
                                voorkeur = Voorkeur.LETTERTYPE_HEADERS;
                                break;
                            case 6:
                                voorkeur = Voorkeur.LETTERTYPE_DAG_HEADERS;
                                break;
                            case 7:
                                voorkeur = Voorkeur.LETTERTYPE_DAG_INHOUD;
                                break;
                            case 8:
                                voorkeur = Voorkeur.TEXTCOLOR_TITELS;
                                break;
                            case 9:
                                voorkeur = Voorkeur.TEXTCOLOR_DAYS;
                                break;
                            case 10:
                                voorkeur = Voorkeur.TEXTCOLOR_DAY_HEADER;
                                break;
                            case 11:
                                voorkeur = Voorkeur.TEXTCOLOR_DAY_CONTENT;
                                break;
                            case 12:
                                voorkeur = Voorkeur.BACKGROUND_WEEKDAYS;
                                break;
                            case 13:
                                voorkeur = Voorkeur.LETTERTYPE_WEEKDAGEN;
                                break;
                            case 14:
                                voorkeur = Voorkeur.DETAILS_ACHTERGROND;
                                break;
                            case 15:
                                voorkeur = Voorkeur.DETAILS_LETTERTYPE_DATUM;
                                break;
                            case 16:
                                voorkeur = Voorkeur.DETAILS_LETTERTYPE_TITELS;
                                break;
                            case 17:
                                voorkeur = Voorkeur.DETAILS_LETTERTYPE_INHOUD;
                                break;
                            case 18:
                                voorkeur = Voorkeur.DETAILS_KLEUR_DATUM;
                                break;
                            case 19:
                                voorkeur = Voorkeur.DETAILS_KLEUR_TITELS;
                                break;
                            case 20:
                                voorkeur = Voorkeur.DETAILS_KLEUR_INHOUD;
                                break;
                            case 21:
                                voorkeur = Voorkeur.DETAILS_ACHTERGROND_DATUM;
                                break;
                            case 22:
                                voorkeur = Voorkeur.DETAILS_ACHTERGROND_TITELS;
                                break;
                            case 23:
                                voorkeur = Voorkeur.DETAILS_ACHTERGROND_INHOUD;
                                break;
                        }

                        voorkeuren.put(voorkeur, str);
                        bytTeller++;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return voorkeuren;
    }*/

    public static Map<Integer, List<String>> getArchief(File dirRes){
        Map<Integer, List<String>> archief = new HashMap<>();

        File mapWerk = new File(dirRes.getAbsolutePath() + "/werk/");

        if (mapWerk.exists() && mapWerk.isDirectory()){
            File[] jaren = mapWerk.listFiles();

            for (File jaar : jaren){
                if (jaar.isDirectory()){
                    File[] maanden = jaar.listFiles();
                    int intJaar = Integer.valueOf(jaar.getName());
                    List<String> lstMaanden = null;
                    try{
                        lstMaanden = archief.get(intJaar);
                        archief.remove(intJaar);
                    }catch (Exception e){}

                    if (lstMaanden == null){
                        lstMaanden = new ArrayList<>();
                    }

                    for(File maand : maanden){
                        String strMaand = maand.getName().replace(".uaz", "");
                        if (!lstMaanden.contains(strMaand)){
                            lstMaanden.add(strMaand);
                        }
                    }

                    lstMaanden = Methodes.sorteerMaanden(lstMaanden);
                    archief.put(intJaar, lstMaanden);
                }
            }
        }

        File mapWissel = new File(dirRes.getAbsolutePath() + "/wissels/");

        if (mapWissel.exists() && mapWissel.isDirectory()){
            File[] jaren = mapWissel.listFiles();

            for (File jaar : jaren){
                if (jaar.isDirectory()){
                    File[] maanden = jaar.listFiles();
                    int intJaar = Integer.valueOf(jaar.getName());
                    List<String> lstMaanden = null;
                    try{
                        lstMaanden = archief.get(intJaar);
                        archief.remove(intJaar);
                    }catch (Exception e){}

                    if (lstMaanden == null){
                        lstMaanden = new ArrayList<>();
                    }

                    for(File maand : maanden){
                        String strMaand = maand.getName().replace(".uaz", "");
                        if (!lstMaanden.contains(strMaand)){
                            lstMaanden.add(strMaand);
                        }
                    }

                    lstMaanden = Methodes.sorteerMaanden(lstMaanden);
                    archief.put(intJaar, lstMaanden);
                }
            }
        }

        File mapPersoonlijk = new File(dirRes.getAbsolutePath() + "/persoonlijk/");

        if (mapPersoonlijk.exists() && mapPersoonlijk.isDirectory()){
            File[] jaren = mapPersoonlijk.listFiles();

            for (File jaar : jaren){
                if (jaar.isDirectory()){
                    File[] maanden = jaar.listFiles();
                    int intJaar = Integer.valueOf(jaar.getName());
                    List<String> lstMaanden = null;
                    try{
                        lstMaanden = archief.get(intJaar);
                        archief.remove(intJaar);
                    }catch (Exception e){}

                    if (lstMaanden == null){
                        lstMaanden = new ArrayList<>();
                    }

                    for(File maand : maanden){
                        String strMaand = maand.getName().replace(".uaz", "");
                        if (!lstMaanden.contains(strMaand)){
                            lstMaanden.add(strMaand);
                        }
                    }

                    lstMaanden = Methodes.sorteerMaanden(lstMaanden);
                    archief.put(intJaar, lstMaanden);
                }
            }
        }

        Calendar cal = Calendar.getInstance();
        int intMaand = cal.get(Calendar.MONTH) + 1;
        int intJaar = cal.get(Calendar.YEAR);

        for (int i = intJaar; i <= intJaar + 2; i++){
            int jaar = i;

            for (int j = 1; j <= 12; j++){
                int maand = j;
                if (jaar == intJaar && maand < intMaand){
                    continue;
                }

                List<String> maanden = null;
                try{
                    maanden = archief.get(jaar);
                    archief.remove(jaar);
                }catch (Exception e){}

                if (maanden == null){
                    maanden = new ArrayList<>();
                }

                if (!maanden.contains(Maand.valueOf(maand).toString())){
                    maanden.add(Maand.valueOf(maand).toString());
                }

                maanden = Methodes.sorteerMaanden(maanden);
                archief.put(jaar, maanden);
            }
        }

        return archief;
    }

    public static List<String> getProfiel(File dirRes){
        List<String> lst = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(dirRes.getAbsolutePath() + "/Data/" + "Profiel.uaz"));

            String lijn = "";
            while ((lijn = br.readLine()) != null){
                String[] split = lijn.split(";");
                lst.add(split[0]);
                lst.add(split[1]);
                lst.add(split[2]);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lst;
    }
}
