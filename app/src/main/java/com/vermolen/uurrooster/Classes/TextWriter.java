package com.vermolen.uurrooster.Classes;


import android.content.Context;

import com.vermolen.uurrooster.Classes.Enums.Maand;
import com.vermolen.uurrooster.Classes.Enums.Voorkeur;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Brent on 20/11/2017.
 */

public class TextWriter {
    public static void writeShift(String shift, List<String> data, File dirRes) {
        Map<String, List<String>> shiften = TextReader.getShiften(dirRes);

        shiften.put(shift, data);

        File bestand = new File(dirRes.getAbsolutePath() + "/Data/Shiften.uaz");

        if (!bestand.exists()){
            try {
                bestand.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(bestand));

            for (String str : shiften.keySet()){
                List<String> lst = shiften.get(str);
                bw.write(str + ";" + lst.get(0) + ";" + lst.get(1) + ";" + lst.get(2) + ";" +
                        lst.get(3) + ";" + lst.get(4) + ";" + lst.get(5) + "\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeShift(String shift, File dirRes){
        if(shift.equals("")){
            throw new IllegalArgumentException("Shift cannot be empty");
        }

        Map<String, List<String>> shiften = TextReader.getShiften(dirRes);

        if (shiften.keySet().contains(shift)){
            shiften.remove(shift);
        }

        File bestand = new File(dirRes.getAbsolutePath() + "/Data/Shiften.uaz");

        if (!bestand.exists()){
            try {
                bestand.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(bestand));

            for (String str : shiften.keySet()){
                List<String> data = shiften.get(str);

                bw.write(str + ";" + data.get(0) + ";" + data.get(1) + ";" + data.get(2) + ";" + data.get(3) + ";" + data.get(4) + "\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void editShift(String oud, String nieuw, List<String> shiftData, File dirRes){
        Map<String, List<String>> shiften = TextReader.getShiften(dirRes);

        File bestandWerk = new File(dirRes.getAbsolutePath() + "/Werk/");
        File bestandWissel = new File(dirRes.getAbsolutePath() + "/Wissels/");
        File[] bestanden = new File[] {bestandWerk, bestandWissel};

        for (File bestand : bestanden){
            int intShiftLoc;
            if (bestand.getName().equals("Werk")){
                intShiftLoc = 1; //Locatie van shift in bestand Werk
            }else{
                intShiftLoc = 2; //Locatie van shift in bestand Wissel
            }

            for (File jaar : bestand.listFiles()){
                if (jaar.isDirectory()){
                    for (File maand : jaar.listFiles()){
                        if (maand.isFile()){
                            List<String> lijnen = new ArrayList<>();

                            try {
                                BufferedReader reader = new BufferedReader(new FileReader(maand));

                                String lijn = "";
                                while ((lijn = reader.readLine()) != null){
                                    String[] split = lijn.split(";");

                                    if (split[intShiftLoc].equals(oud)){
                                        split[intShiftLoc] = split[intShiftLoc].replaceFirst(oud, nieuw);

                                        lijn = "";
                                        for (String str : split){
                                            lijn += str + ";";
                                        }
                                        lijn = lijn.substring(0, lijn.length() - 1);
                                    }

                                    lijnen.add(lijn);
                                }
                                reader.close();

                                BufferedWriter writer = new BufferedWriter(new FileWriter(maand));

                                for (String str : lijnen){
                                    writer.write(str + "\n");
                                }

                                writer.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            }
        }

        for (String shift : shiften.keySet()) {
            if (shift.equals(oud)){
                TextWriter.removeShift(shift, dirRes);
                TextWriter.writeShift(nieuw, shiftData, dirRes);
            }
        }
    }
    public static void writeCollega(String nieuw, File dirRes){
        if(nieuw.equals("")){
            throw new IllegalArgumentException("Collegue cannot be empty");
        }

        ArrayList<String> collegas = TextReader.getCollegas(dirRes);

        collegas.add(nieuw);

        File bestand = new File(dirRes.getAbsolutePath() + "/Data/Collegas.uaz");

        if (!bestand.exists()){
            try {
                bestand.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(bestand));

            for (String str : collegas){
                bw.write(str + "\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeCollega(String collega, File dirRes){
        if(collega.equals("")){
            throw new IllegalArgumentException("Collegue cannot be empty");
        }

        ArrayList<String> collegas = TextReader.getCollegas(dirRes);

        if (collegas.contains(collega)){
            collegas.remove(collega);
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(dirRes.getAbsolutePath() + "/Data/Collegas.uaz"));

            for (String str : collegas){
                bw.write(str + "\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void editCollega(String oud, String nieuw, File dirRes){
        if(oud.equals("") || nieuw.equals("")){
            throw new IllegalArgumentException("Parameter cannot be empty");
        }
        Map<String, List<String>> collegas = TextReader.getShiften(dirRes);

        for (String collega : collegas.keySet()) {
            if (collega.equals(oud)){
                TextWriter.removeCollega(collega, dirRes);
                TextWriter.writeCollega(nieuw, dirRes);
            }
        }
    }

    public static void removeWerk(int dag, Maand maand, int jaar, File dirRes) {
        if(dag < 0 || dag > Methodes.getMaxDagenInMaand(maand.getNr(), jaar)){
            throw new IllegalArgumentException("Day is not valid");
        }
        Map<Integer, List<String>> shiftData = TextReader.getWerkData(maand, jaar, dirRes);

        if (!shiftData.keySet().contains(dag)){
            return;
        }

        shiftData.remove(dag);

        String data = "";

        for (int i : shiftData.keySet()){
            List<String> lst = shiftData.get(i);

            data += String.valueOf(i) + ";" + lst.get(0) + "\n";
        }

        TextWriter.writeWerkShiften(data, maand, jaar, dirRes);
    }

    private static void writeWerkShiften(String data, Maand maand, int jaar, File dirRes) {
        File bestand = new File(dirRes.getAbsolutePath() + "/Werk/" + String.valueOf(jaar) + "/" + maand.toString() + ".uaz");

        if (!bestand.getParentFile().exists()){
            bestand.getParentFile().mkdirs();
        }else{
            if (!bestand.getParentFile().isDirectory()){
                bestand.getParentFile().delete();
                bestand.getParentFile().mkdirs();
            }
        }

        if (!bestand.exists()){
            try {
                bestand.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(data.equals("")){
            bestand.delete();
            return;
        }

        if (bestand.exists()){
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(bestand));

                bw.write(data);

                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void addWerkShift(String shift, int dag, Maand maand, int jaar, File dirRes) {
        if(shift.equals("")){
            throw new IllegalArgumentException("Shift cannot be empty");
        }
        if(dag < 0 || dag > Methodes.getMaxDagenInMaand(maand.getNr(), jaar)){
            throw new IllegalArgumentException("Day is not valid");
        }

        Map<Integer, List<String>> werkData = TextReader.getWerkData(maand, jaar, dirRes);

        List<String> newData = new ArrayList<>();
        newData.add(shift);

        if (werkData.containsKey(dag)){
            werkData.remove(dag);
        }

        werkData.put(dag, newData);


        String data = "";

        for (int i : werkData.keySet()){
            List<String> lst = werkData.get(i);

            data += String.valueOf(i) + ";" + lst.get(0) + "\n";
        }

        TextWriter.writeWerkShiften(data, maand, jaar, dirRes);
    }

    public static void writeWissel(String shift, String collega, String richting, String ZO, int dag, Maand maand, int jaar, File dirRes) {
        Map<Integer, List<String>> shiftData = TextReader.getWisselData(maand, jaar, dirRes);

        shiftData.remove(dag);

        List<String> lstData = new ArrayList<>();
        lstData.add(collega);
        lstData.add(shift);
        lstData.add(richting);
        lstData.add(ZO);

        shiftData.put(dag, lstData);

        File bestand = new File(dirRes.getAbsolutePath() + "/Wissels/" + String.valueOf(jaar) + "/" + maand.toString() + ".uaz");

        if (!bestand.getParentFile().exists()){
            bestand.getParentFile().mkdirs();
        }else{
            if (!bestand.getParentFile().isDirectory()){
                bestand.getParentFile().delete();
                bestand.getParentFile().mkdirs();
            }
        }

        if (!bestand.exists()){
            try {
                bestand.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            if (bestand.exists()){
                BufferedWriter bw = new BufferedWriter(new FileWriter(bestand));
                for (int i : shiftData.keySet()){
                    List<String> lst = shiftData.get(i);

                    bw.write(String.valueOf(i) + ";" + lst.get(0) + ";" + lst.get(1) + ";" + lst.get(2) + ";" + lst.get(3) + "\n");
                }
                bw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void editWerkShift(String shift, int dag, Maand maand, int jaar, File dirRes) {
        Map<Integer, List<String>> werkData = TextReader.getWerkData(maand, jaar, dirRes);

        werkData.remove(dag);
        List<String> data = new ArrayList<>();
        data.add(shift);
        werkData.put(dag, data);

        File bestand = new File(dirRes.getAbsolutePath() + "/Werk/" + String.valueOf(jaar) + "/" + maand.toString() + ".uaz");

        if (!bestand.getParentFile().exists()){
            bestand.getParentFile().mkdirs();
        }else{
            if (!bestand.getParentFile().isDirectory()){
                bestand.getParentFile().delete();
                bestand.getParentFile().mkdirs();
            }
        }

        if (!bestand.exists()){
            try {
                bestand.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(bestand));
            for (int i : werkData.keySet()){
                bw.write(String.valueOf(i) + ";" + werkData.get(i).get(0) + "\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeWissel(int dag, Maand maand, int jaar, File dirRes) {
        Map<Integer, List<String>> wisselData = TextReader.getWisselData(maand, jaar, dirRes);

        if (!wisselData.containsKey(dag)){
            return;
        }

        wisselData.remove(dag);

        File bestand = new File(dirRes.getAbsolutePath() + "/Wissels/" + String.valueOf(jaar) + "/" + maand.toString() + ".uaz");

        if (!bestand.getParentFile().exists()){
            bestand.getParentFile().mkdirs();
        }else{
            if (!bestand.getParentFile().isDirectory()){
                bestand.getParentFile().delete();
                bestand.getParentFile().mkdirs();
            }
        }

        if (!bestand.exists()){
            try {
                bestand.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (wisselData.size() == 0){
            bestand.delete();
            return;
        }

        if (bestand.exists()){
            try{
                BufferedWriter bw = new BufferedWriter(new FileWriter(bestand));

                for (int i : wisselData.keySet()){
                    List<String> lst = wisselData.get(i);
                    bw.write(String.valueOf(i) + ";" + lst.get(0) + ";" + lst.get(1) + ";" + lst.get(2) +";" + lst.get(3) + "\n");
                }

                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writePersoonlijk(String activiteit, int dag, Maand maand, int jaar, File dirRes) {
        Map<Integer, List<String>> persoonlijkData = TextReader.getPersoonlijkData(maand, jaar, dirRes);

        List<String> lst = new ArrayList<>();
        lst.add(activiteit);

        persoonlijkData.put(dag, lst);

        File bestand = new File(dirRes.getAbsolutePath() + "/Persoonlijk/" + String.valueOf(jaar) + "/" + maand.toString() + ".uaz");

        if (!bestand.getParentFile().exists()){
            bestand.getParentFile().mkdirs();
        }else{
            if (!bestand.getParentFile().isDirectory()){
                bestand.getParentFile().delete();
                bestand.getParentFile().mkdirs();
            }
        }

        if (!bestand.exists()){
            try {
                bestand.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (bestand.exists()){
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(bestand));

                for (int i : persoonlijkData.keySet()){
                    bw.write(String.valueOf(i) + ";" + persoonlijkData.get(i).get(0) + "\n");
                }

                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void removePersoonlijk(int dag, Maand maand, int jaar, File dirRes) {
        if(dag < 0 || dag > Methodes.getMaxDagenInMaand(maand.getNr(), jaar)){
            throw new IllegalArgumentException("Day is not valid");
        }

        Map<Integer, List<String>> persoonlijkData = TextReader.getPersoonlijkData(maand, jaar, dirRes);

        if (!persoonlijkData.containsKey(dag)){
            return;
        }

        persoonlijkData.remove(dag);

        File bestand = new File(dirRes.getAbsolutePath() + "/Persoonlijk/" + String.valueOf(jaar) + "/" + maand.toString() + ".uaz");

        if (!bestand.getParentFile().exists()){
            bestand.getParentFile().mkdirs();
        }else{
            if (!bestand.getParentFile().isDirectory()){
                bestand.getParentFile().delete();
                bestand.getParentFile().mkdirs();
            }
        }

        if (!bestand.exists()){
            try {
                bestand.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (persoonlijkData.size() == 0){
            bestand.delete();
            return;
        }

        if (bestand.exists()){
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(bestand));

                for (int i : persoonlijkData.keySet()){
                    bw.write(String.valueOf(i) + ";" + persoonlijkData.get(i).get(0) + "\n");
                }

                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeProfiel(String gebDatum, String adres, String woonplaats, File dirRes){
        if(gebDatum.equals("")){
            throw new IllegalArgumentException("DoB cannot be empty");
        }
        if(adres.equals("")){
            throw new IllegalArgumentException("Address cannot be empty");
        }
        if(woonplaats.equals("")){
            throw new IllegalArgumentException("City cannot be empty");
        }

        try {
            File bestand = new File(dirRes.getAbsolutePath() + "/Data/" + "Profiel.uaz");
            if (!bestand.exists()){
                try {
                    bestand.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(dirRes.getAbsolutePath() + "/Data/" + "Profiel.uaz"));

            bw.write(gebDatum + ";" + adres + ";" + woonplaats);

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeVoorkeur(Voorkeur voorkeur, String kleur, File dirRes) {
        Map<Voorkeur, String> voorkeuren = TextReader.getVoorkeuren(dirRes);

        File bestand = new File(dirRes.getAbsolutePath() + "/Data/Voorkeuren.uaz");
        if (!bestand.exists()){
            try {
                bestand.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String data = "";

        for(Voorkeur v : voorkeuren.keySet()){
            if (v.toString().equals(voorkeur.toString())){
                data += kleur;
            }else{
                data += voorkeuren.get(v);
            }

            data += ";";
        }

        data = data.substring(0, data.length() - 1);

        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(bestand));

            bw.write(data);

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /*public static void writeVoorkeur(Voorkeur voorkeur, String waarde, Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences("Voorkeuren", Context.MODE_PRIVATE).edit();

        String strVoorkeur = voorkeur.toString();
        if (waarde.equals("")){
            waarde = "0";
        }
        editor.putString(strVoorkeur, waarde);
        editor.apply();
        editor.commit();
    }*/
}
