package com.vermolen.uurrooster.Classes;

import android.Manifest;
import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;

import com.vermolen.uurrooster.R;
import com.vermolen.uurrooster.Widget.MonthWidget;
import com.vermolen.uurrooster.Widget.TodayWidget;
import com.vermolen.uurrooster.Widget.WeekToComeWidget;
import com.vermolen.uurrooster.Widget.WeekWidget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Brent on 21/11/2017.
 */

public class Methodes {
    public static int getMaxDagenInMaand(int intMaand, int jaar)
    {
        int intDagen = 30;
        if (intMaand == 0){
            intMaand = 12;
        }
        if (intMaand == 13){
            intMaand = 1;
        }

        switch (intMaand)
        {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                intDagen = 31;
                break;
            case 2:
                if (jaar % 4 == 0)
                { return 29; }
                else { return 28; }
            case 4:
            case 6:
            case 9:
            case 11:
                intDagen = 30;
                break;
        }

        return intDagen;
    }

    public static String getDag(int nr, Context context){
        if (nr >= 8){
            nr -= 7;
        }
        String dag = "";

        switch (nr){
            case 1:
                dag =  context.getResources().getString(R.string.sunday);
                break;
            case 2:
                dag =  context.getResources().getString(R.string.monday);
                break;
            case 3:
                dag =  context.getResources().getString(R.string.tuesday);
                break;
            case 4:
                dag =  context.getResources().getString(R.string.wednesday);
                break;
            case 5:
                dag =  context.getResources().getString(R.string.thursday);
                break;
            case 6:
                dag =  context.getResources().getString(R.string.friday);
                break;
            case 7:
                dag =  context.getResources().getString(R.string.saterday);
                break;
        }

        return dag;
    }public static String getDagVerkort(int nr, Context context){
        return getDag(nr, context).substring(0, 3).toUpperCase();
    }

    public static List<String> sorteerMaanden(List<String> lstMaanden)
    {
        List<String> maanden = new ArrayList<>();


        if (lstMaanden.contains("January"))
        {
            maanden.add("January");
        }

        if (lstMaanden.contains("February"))
        {
            maanden.add("February");
        }

        if (lstMaanden.contains("March"))
        {
            maanden.add("March");
        }

        if (lstMaanden.contains("April"))
        {
            maanden.add("April");
        }

        if (lstMaanden.contains("May"))
        {
            maanden.add("May");
        }

        if (lstMaanden.contains("June"))
        {
            maanden.add("June");
        }

        if (lstMaanden.contains("July"))
        {
            maanden.add("July");
        }

        if (lstMaanden.contains("August"))
        {
            maanden.add("August");
        }

        if (lstMaanden.contains("September"))
        {
            maanden.add("September");
        }

        if (lstMaanden.contains("October"))
        {
            maanden.add("October");
        }

        if (lstMaanden.contains("November"))
        {
            maanden.add("November");
        }

        if (lstMaanden.contains("December"))
        {
            maanden.add("December");
        }
        /*if (lstMaanden.contains(context.getResources().getString(R.string.january)))
        {
            maanden.add(context.getResources().getString(R.string.january));
        }

        if (lstMaanden.contains(context.getResources().getString(R.string.february)))
        {
            maanden.add(context.getResources().getString(R.string.february));
        }

        if (lstMaanden.contains(context.getResources().getString(R.string.march)))
        {
            maanden.add(context.getResources().getString(R.string.march));
        }

        if (lstMaanden.contains(context.getResources().getString(R.string.april)))
        {
            maanden.add(context.getResources().getString(R.string.april));
        }

        if (lstMaanden.contains(context.getResources().getString(R.string.may)))
        {
            maanden.add(context.getResources().getString(R.string.may));
        }

        if (lstMaanden.contains(context.getResources().getString(R.string.june)))
        {
            maanden.add(context.getResources().getString(R.string.june));
        }

        if (lstMaanden.contains(context.getResources().getString(R.string.july)))
        {
            maanden.add(context.getResources().getString(R.string.july));
        }

        if (lstMaanden.contains(context.getResources().getString(R.string.august)))
        {
            maanden.add(context.getResources().getString(R.string.august));
        }

        if (lstMaanden.contains(context.getResources().getString(R.string.september)))
        {
            maanden.add(context.getResources().getString(R.string.september));
        }

        if (lstMaanden.contains(context.getResources().getString(R.string.october)))
        {
            maanden.add(context.getResources().getString(R.string.october));
        }

        if (lstMaanden.contains(context.getResources().getString(R.string.november)))
        {
            maanden.add(context.getResources().getString(R.string.november));
        }

        if (lstMaanden.contains(context.getResources().getString(R.string.december)))
        {
            maanden.add(context.getResources().getString(R.string.december));
        }*/

        return maanden;
    }

    public static void updateWidgets(Context context, Application application) {
        int currDag = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int currMaand = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int currJaar = Calendar.getInstance().get(Calendar.YEAR);

        //Reload TodayWidget
        Intent intent = new Intent(context, TodayWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids = AppWidgetManager.getInstance(application).getAppWidgetIds(new ComponentName(application, TodayWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);

        //Reload WeekWidget
        Intent intent2 = new Intent(context, WeekWidget.class);
        intent2.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids2 = AppWidgetManager.getInstance(application).getAppWidgetIds(new ComponentName(application, WeekWidget.class));
        intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids2);
        context.sendBroadcast(intent2);

        //Reload WeekToComeWidget
        Intent intent3 = new Intent(context, WeekToComeWidget.class);
        intent3.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids3 = AppWidgetManager.getInstance(application).getAppWidgetIds(new ComponentName(application, WeekToComeWidget.class));
        intent3.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids3);
        context.sendBroadcast(intent3);

        //Reload Month
        Intent intent4 = new Intent(context, MonthWidget.class);
        intent4.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids4 = AppWidgetManager.getInstance(application).getAppWidgetIds(new ComponentName(application, MonthWidget.class));
        intent4.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids4);
        context.sendBroadcast(intent4);
    }

    public static Cursor getAllCalendars(Context context){
        final String[] EVENT_PROJECTION = new String[]{
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
        };

        final int PROJECTION_ID_INDEX = 0;
        final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
        final int PROJECTION_DISPLAY_NAME_INDEX = 2;
        final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

        Cursor cur = null;
        ContentResolver cr = context.getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
// Submit the query and get a Cursor object back.

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        cur = cr.query(uri, EVENT_PROJECTION, null, null, null);

        return cur;
    }
}
