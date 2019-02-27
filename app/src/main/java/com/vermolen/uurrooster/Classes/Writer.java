package com.vermolen.uurrooster.Classes;

import com.vermolen.uurrooster.Classes.Enums.Maand;
import com.vermolen.uurrooster.Classes.Enums.Voorkeur;
import com.vermolen.uurrooster.DB.CollegasDao;
import com.vermolen.uurrooster.DB.ShiftenDao;
import com.vermolen.uurrooster.DB.DataDao;

import java.util.List;

public class Writer {
    private final static ShiftenDao shiftenDao = new ShiftenDao();
    private final static CollegasDao collegasDao = new CollegasDao();
    private final static DataDao dataDao = new DataDao();

    public static void editShift(String oud, String nieuw, List<String> shiftData) {
        if(oud.equals("") || nieuw.equals("")){
            throw new IllegalArgumentException("Parameter cannot be empty");
        }

        if (UserSingleton.getInstance().getUser_id() == -1){
            TextWriter.editShift(oud, nieuw, shiftData, DirResSingleton.getInstance());
        }else{
            shiftenDao.editShift(oud, nieuw, shiftData);
        }
    }

    public static void writeShift(String shift, List<String> shiftData) {
        if (shift.equals("")){
            throw new IllegalArgumentException("Shift cannot be empty");
        }

        if (UserSingleton.getInstance().getUser_id() == -1){
            TextWriter.writeShift(shift, shiftData, DirResSingleton.getInstance());
        }else{
            shiftenDao.addShift(shift, shiftData);
        }
    }

    public static void removeShift(String shift) {
        if (shift.equals("")){
            throw new IllegalArgumentException("Shift cannot be empty");
        }

        if (UserSingleton.getInstance().getUser_id() == -1){
            TextWriter.removeShift(shift, DirResSingleton.getInstance());
        }else{
            shiftenDao.removeShift(shift);
        }
    }

    public static void editCollega(String oudeText, String nieuweText) {
        if (oudeText.equals("") || nieuweText.equals("")){
            throw new IllegalArgumentException("Parameters cannot be empty");
        }

        if (UserSingleton.getInstance().getUser_id() == -1){
            TextWriter.editCollega(oudeText, nieuweText, DirResSingleton.getInstance());
        }else{
            collegasDao.editCollega(oudeText, nieuweText);
        }
    }

    public static void removeCollega(String collega) {
        if (collega.equals("")){
            throw new IllegalArgumentException("Parameters cannot be empty");
        }

        if (UserSingleton.getInstance().getUser_id() == -1){
            TextWriter.removeCollega(collega, DirResSingleton.getInstance());
        }else{
            collegasDao.removeCollega(collega);
        }
    }

    public static void writeCollega(String nieuweText) {
        if (nieuweText.equals("")){
            throw new IllegalArgumentException("Parameters cannot be empty");
        }

        if (UserSingleton.getInstance().getUser_id() == -1){
            TextWriter.writeCollega(nieuweText, DirResSingleton.getInstance());
        }else{
            collegasDao.addCollega(nieuweText);
        }
    }

    public static void removeWerk(int dag, Maand maand, int jaar) {
        if(dag < 0 || dag > Methodes.getMaxDagenInMaand(maand.getNr(), jaar)){
            throw new IllegalArgumentException("Day is not valid");
        }

        if (UserSingleton.getInstance().getUser_id() == -1){
            TextWriter.removeWerk(dag, maand, jaar, DirResSingleton.getInstance());
        }else{
            dataDao.removeWerk(dag, maand, jaar);
        }
    }

    public static void addWerkShift(String shift, int dag, Maand maand, int jaar) {
        if(shift.equals("")){
            throw new IllegalArgumentException("Shift cannot be empty");
        }
        if(dag < 0 || dag > Methodes.getMaxDagenInMaand(maand.getNr(), jaar)){
            throw new IllegalArgumentException("Day is not valid");
        }

        if (UserSingleton.getInstance().getUser_id() == -1){
            TextWriter.addWerkShift(shift, dag, maand, jaar, DirResSingleton.getInstance());
        }else{
            dataDao.addWerkShift(shift, dag, maand, jaar);
        }
    }

    public static void writeWissel(String shift, String collega, String richting, String zo, int dag, Maand maand, int jaar) {
        if(shift.equals("")){
            throw new IllegalArgumentException("Shift cannot be empty");
        }
        if(collega.equals("")){
            throw new IllegalArgumentException("Collegue cannot be empty");
        }
        if(dag < 0 || dag > Methodes.getMaxDagenInMaand(maand.getNr(), jaar)){
            throw new IllegalArgumentException("Day is not valid");
        }

        if (UserSingleton.getInstance().getUser_id() == -1){
            TextWriter.writeWissel(shift, collega, richting, zo, dag, maand, jaar, DirResSingleton.getInstance());
        }else{
            dataDao.writeWissel(shift, collega, richting, zo, dag, maand, jaar);
        }
    }

    public static void removeWissel(int dag, Maand maand, int jaar) {
        if(dag < 0 || dag > Methodes.getMaxDagenInMaand(maand.getNr(), jaar)){
            throw new IllegalArgumentException("Day is not valid");
        }

        if (UserSingleton.getInstance().getUser_id() == -1){
            TextWriter.removeWissel(dag, maand, jaar, DirResSingleton.getInstance());
        }else{
            dataDao.removeWissel(dag, maand, jaar);
        }
    }

    public static void editWerkShift(String shift, int dag, Maand maand, int jaar) {
        if(shift.equals("")){
            throw new IllegalArgumentException("Shift cannot be empty");
        }
        if(dag < 0 || dag > Methodes.getMaxDagenInMaand(maand.getNr(), jaar)){
            throw new IllegalArgumentException("Day is not valid");
        }

        if (UserSingleton.getInstance().getUser_id() == -1){
            TextWriter.editWerkShift(shift, dag, maand, jaar, DirResSingleton.getInstance());
        }else{
            dataDao.editWerkShift(shift, dag, maand, jaar);
        }
    }

    public static void removePersoonlijk(int dag, Maand maand, int jaar) {
        if(dag < 0 || dag > Methodes.getMaxDagenInMaand(maand.getNr(), jaar)){
            throw new IllegalArgumentException("Day is not valid");
        }

        if (UserSingleton.getInstance().getUser_id() == -1){
            TextWriter.removePersoonlijk(dag, maand, jaar, DirResSingleton.getInstance());
        }else{
            dataDao.removePersoonlijk(dag, maand, jaar);
        }
    }

    public static void writePersoonlijk(String tekst, int dag, Maand maand, int jaar) {
        if(tekst.equals("")){
            throw new IllegalArgumentException("Collegue cannot be empty");
        }

        tekst = tekst.replace("\\", "/").replace("\n", "\\n");

        if (UserSingleton.getInstance().getUser_id() == -1){
            TextWriter.writePersoonlijk(tekst, dag, maand, jaar, DirResSingleton.getInstance());
        }else{
            dataDao.writePersoonlijk(tekst, dag, maand, jaar);
        }
    }

    public static void writeVoorkeur(Voorkeur voorkeur, String value) {
        if(voorkeur == null){
            throw new IllegalArgumentException("Preference cannot be empty");
        }

        if (UserSingleton.getInstance().getUser_id() == -1){
            TextWriter.writeVoorkeur(voorkeur, value, DirResSingleton.getInstance());
        }else{
            dataDao.writeVoorkeur(voorkeur, value);
        }
    }
}
