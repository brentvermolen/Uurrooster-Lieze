package com.vermolen.uurrooster.Classes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientCredentialsTokenRequest;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.apache.GoogleApacheHttpTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.json.MockJsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.vermolen.uurrooster.Classes.Enums.Maand;
import com.vermolen.uurrooster.Classes.Enums.Voorkeur;
import com.vermolen.uurrooster.DB.CollegasDao;
import com.vermolen.uurrooster.DB.ShiftenDao;
import com.vermolen.uurrooster.DB.DataDao;
import com.vermolen.uurrooster.InstellingenActivity;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Writer {
    private final static ShiftenDao shiftenDao = new ShiftenDao();
    private final static CollegasDao collegasDao = new CollegasDao();
    private final static DataDao dataDao = new DataDao();

    public static void editShift(String oud, String nieuw, List<String> shiftData) {
        if (oud.equals("") || nieuw.equals("")) {
            throw new IllegalArgumentException("Parameter cannot be empty");
        }

        if (UserSingleton.getInstance().getUser_id() == -1) {
            TextWriter.editShift(oud, nieuw, shiftData, DirResSingleton.getInstance());
        } else {
            shiftenDao.editShift(oud, nieuw, shiftData);
        }
    }

    public static void writeShift(String shift, List<String> shiftData) {
        if (shift.equals("")) {
            throw new IllegalArgumentException("Shift cannot be empty");
        }

        if (UserSingleton.getInstance().getUser_id() == -1) {
            TextWriter.writeShift(shift, shiftData, DirResSingleton.getInstance());
        } else {
            shiftenDao.addShift(shift, shiftData);
        }
    }

    public static void removeShift(String shift) {
        if (shift.equals("")) {
            throw new IllegalArgumentException("Shift cannot be empty");
        }

        if (UserSingleton.getInstance().getUser_id() == -1) {
            TextWriter.removeShift(shift, DirResSingleton.getInstance());
        } else {
            shiftenDao.removeShift(shift);
        }
    }

    public static void editCollega(String oudeText, String nieuweText) {
        if (oudeText.equals("") || nieuweText.equals("")) {
            throw new IllegalArgumentException("Parameters cannot be empty");
        }

        if (UserSingleton.getInstance().getUser_id() == -1) {
            TextWriter.editCollega(oudeText, nieuweText, DirResSingleton.getInstance());
        } else {
            collegasDao.editCollega(oudeText, nieuweText);
        }
    }

    public static void removeCollega(String collega) {
        if (collega.equals("")) {
            throw new IllegalArgumentException("Parameters cannot be empty");
        }

        if (UserSingleton.getInstance().getUser_id() == -1) {
            TextWriter.removeCollega(collega, DirResSingleton.getInstance());
        } else {
            collegasDao.removeCollega(collega);
        }
    }

    public static void writeCollega(String nieuweText) {
        if (nieuweText.equals("")) {
            throw new IllegalArgumentException("Parameters cannot be empty");
        }

        if (UserSingleton.getInstance().getUser_id() == -1) {
            TextWriter.writeCollega(nieuweText, DirResSingleton.getInstance());
        } else {
            collegasDao.addCollega(nieuweText);
        }
    }

    public static void removeWerk(final int dag, final Maand maand, final int jaar) {
        if (dag < 0 || dag > Methodes.getMaxDagenInMaand(maand.getNr(), jaar)) {
            throw new IllegalArgumentException("Day is not valid");
        }

        final long cal_id = CalendarSingletons.sharedPreferencesCalendar.getLong("cal_id", -1);

        if (cal_id == -1){
            return;
        }

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(jaar, maand.getNr() - 1, dag, 0, 0, 0);

                long event_id = CalendarSingletons.sharedPreferencesCalendar.getLong(jaar + "/" + maand + "/" + dag, -1);
                if (event_id == -1){
                    return null;
                }

                ContentResolver cr = CalendarSingletons.contentResolver;
                Uri deleteUri = null;
                deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event_id);
                int rows = cr.delete(deleteUri, null, null);

                return null;
            }
        }.execute();

        if (UserSingleton.getInstance().getUser_id() == -1) {
            TextWriter.removeWerk(dag, maand, jaar, DirResSingleton.getInstance());
        } else {
            dataDao.removeWerk(dag, maand, jaar);
        }
    }

    public static void addWerkShift(final String shift, final List<String> shiftData, final int dag, final Maand maand, final int jaar) {
        if (shift.equals("")) {
            throw new IllegalArgumentException("Shift cannot be empty");
        }
        if (dag < 0 || dag > Methodes.getMaxDagenInMaand(maand.getNr(), jaar)) {
            throw new IllegalArgumentException("Day is not valid");
        }

        final long cal_id = CalendarSingletons.sharedPreferencesCalendar.getLong("cal_id", -1);

        if (cal_id == -1){
            return;
        }

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(jaar, maand.getNr() - 1, dag, 0, 0, 0);
                long dagMillis = calendar.getTimeInMillis();

                calendar.set(jaar, maand.getNr() - 1, dag, Integer.parseInt(shiftData.get(1)), Integer.parseInt(shiftData.get(2)), 0);
                long startMillis = calendar.getTimeInMillis();

                calendar.set(jaar, maand.getNr() - 1, dag, Integer.parseInt(shiftData.get(3)), Integer.parseInt(shiftData.get(4)), 0);
                long endMillis = calendar.getTimeInMillis();

                ContentResolver cr = CalendarSingletons.contentResolver;
                ContentValues values = new ContentValues();
                values.put(CalendarContract.Events.DTSTART, startMillis);
                values.put(CalendarContract.Events.DTEND, endMillis);
                values.put(CalendarContract.Events.TITLE, shift);
                values.put(CalendarContract.Events.DESCRIPTION, shiftData.get(0));
                values.put(CalendarContract.Events.CALENDAR_ID, cal_id);
                values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Brussels");

                @SuppressLint("MissingPermission") Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                CalendarSingletons.sharedPreferencesCalendar.edit().putLong(jaar + "/" + maand + "/" + dag, Long.parseLong(uri.getLastPathSegment())).commit();
                return null;
            }
        }.execute();

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

    public static void editWerkShift(final String shift, final List<String> shiftData, final int dag, final Maand maand, final int jaar) {
        if(shift.equals("")){
            throw new IllegalArgumentException("Shift cannot be empty");
        }
        if(dag < 0 || dag > Methodes.getMaxDagenInMaand(maand.getNr(), jaar)){
            throw new IllegalArgumentException("Day is not valid");
        }

        final long cal_id = CalendarSingletons.sharedPreferencesCalendar.getLong("cal_id", -1);

        if (cal_id == -1){
            return;
        }

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(jaar, maand.getNr() - 1, dag, 0, 0, 0);

                long event_id = CalendarSingletons.sharedPreferencesCalendar.getLong(jaar + "/" + maand + "/" + dag, -1);
                if (event_id == -1){
                    return null;
                }

                calendar.set(jaar, maand.getNr() - 1, dag, Integer.parseInt(shiftData.get(1)), Integer.parseInt(shiftData.get(2)), 0);
                long startMillis = calendar.getTimeInMillis();

                calendar.set(jaar, maand.getNr() - 1, dag, Integer.parseInt(shiftData.get(3)), Integer.parseInt(shiftData.get(4)), 0);
                long endMillis = calendar.getTimeInMillis();

                ContentResolver cr = CalendarSingletons.contentResolver;
                ContentValues values = new ContentValues();
                values.put(CalendarContract.Events.DTSTART, startMillis);
                values.put(CalendarContract.Events.DTEND, endMillis);
                values.put(CalendarContract.Events.TITLE, shift);
                values.put(CalendarContract.Events.DESCRIPTION, shiftData.get(0));
                values.put(CalendarContract.Events.CALENDAR_ID, cal_id);
                values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Brussels");


                @SuppressLint("MissingPermission") Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event_id);
                int rows = cr.update(uri, values, null, null);
                CalendarSingletons.sharedPreferencesCalendar.edit().putLong(jaar + "/" + maand + "/" + dag, Long.parseLong(uri.getLastPathSegment())).commit();
                return null;
            }
        }.execute();

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
