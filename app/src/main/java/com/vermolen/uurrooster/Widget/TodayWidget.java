package com.vermolen.uurrooster.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.vermolen.uurrooster.Classes.DirResSingleton;
import com.vermolen.uurrooster.Classes.Enums.Maand;
import com.vermolen.uurrooster.Classes.Methodes;
import com.vermolen.uurrooster.Classes.Reader;
import com.vermolen.uurrooster.Classes.TextReader;
import com.vermolen.uurrooster.Classes.UserSingleton;
import com.vermolen.uurrooster.DB.UserDao;
import com.vermolen.uurrooster.HomeActivity;
import com.vermolen.uurrooster.Model.User;
import com.vermolen.uurrooster.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Implementation of App Widget functionality.
 */
public class TodayWidget extends AppWidgetProvider {

    private static boolean hasWissel;
    private static Map<Integer, List<String>> werkData;
    private static Map<Integer, List<String>> wisselData;
    private static Map<Integer, List<String>> persoonlijkData;

    private static User USER;
    private static UserDao userDao;
    private static SharedPreferences sharedPref;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.today_widget);

        /*
        dirRes = new File(getIntent().getExtras().getString("dirRes"));

        dag = getIntent().getExtras().getInt("dag");
        maand = Maand.valueOf(getIntent().getExtras().getInt("maand"));
        jaar = getIntent().getExtras().getInt("jaar");
         */

        sharedPref = context.getApplicationContext().getSharedPreferences("Settings", MODE_PRIVATE);
        userDao = new UserDao();

        if (UserSingleton.getInstance() == null){
            final int sharedUserId = context.getSharedPreferences("USER", MODE_PRIVATE).getInt("USER", -1);

            if (sharedUserId == -1) {
                User user = new User();
                user.setUser_id(-1);
                UserSingleton.setInstance(user);
                USER = user;

                File dirRes = new File(context.getSharedPreferences("USER", MODE_PRIVATE).getString("DirRes", context.getApplicationContext().getFilesDir().getAbsolutePath() + "//Resources//"));
                DirResSingleton.setInstance(dirRes);
            }else {
                USER = userDao.getUserById(sharedUserId);
                UserSingleton.setInstance(USER);
            }
        }else{
            USER = UserSingleton.getInstance();
        }

        Calendar currCal = Calendar.getInstance();
        int dag = currCal.get(Calendar.DAY_OF_MONTH);
        String strDag = String.format("%02d", dag);
        int currDagWeek = currCal.get(Calendar.DAY_OF_WEEK);
        String strCurrDagWeek = Methodes.getDagVerkort(currDagWeek, context);

        views.setTextViewText(R.id.lblWidTodDagNr, strDag);
        views.setTextViewText(R.id.lblWidTodDagNaam, strCurrDagWeek);

        int maand = currCal.get(Calendar.MONTH) + 1;
        Maand m = Maand.valueOf(maand);
        int jaar = currCal.get(Calendar.YEAR);

        //Details van vandaag openen
            /* Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("dirRes", dirRes.getAbsolutePath());
            intent.putExtra("dag", dag);
            intent.putExtra("maand", maand);
            intent.putExtra("jaar", jaar);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.TodayWidget, pendingIntent);
             */
        //Kalender openen
        Intent intent = new Intent(context, HomeActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.TodayWidget, pendingIntent);

        werkData = Reader.getWerkData(m, jaar);
        wisselData = Reader.getWisselData(m, jaar);
        persoonlijkData = Reader.getPersoonlijkData(m, jaar);

        String shift = geefDataWerk(dag, true);
        views.setTextViewText(R.id.lblWidTodShift, shift);

        if (hasWissel){
            String origShift = geefDataWerk(dag, false);
            views.setTextViewText(R.id.lblWidTodOrigShift, "(" + origShift + ")");
            views.setViewVisibility(R.id.lblWidTodOrigShift, View.VISIBLE);
        }else{
            views.setViewVisibility(R.id.lblWidTodOrigShift, View.GONE);
        }

        String persoonlijk = geefDataPersoonlijk(dag);
        if (persoonlijk.equals("")){
            persoonlijk = context.getResources().getString(R.string.no_notes);
        }
        views.setTextViewText(R.id.lblWidTodPersoonlijk, persoonlijk);

        List<String> shiftData = Reader.getShiften().get(shift);

        String tekst = "";
        if (shiftData != null){
            if (shiftData.size() > 0){
                String shiftOmsc = shiftData.get(0);
                if (shiftOmsc.equals("")){
                    views.setTextViewText(R.id.lblWidTodShift, shift);
                }else {
                    views.setTextViewText(R.id.lblWidTodShift, shiftOmsc + " (" + shift + ")");
                }

                if (!shiftData.get(1).equals("0") && !shiftData.get(3).equals("0")){
                    tekst = String.format("\t" + context.getResources().getString(R.string.from) + " %02d:%02d " + context.getResources().getString(R.string.to) + " %02d:%02d", Integer.parseInt(shiftData.get(1)), Integer.parseInt(shiftData.get(2)),
                            Integer.parseInt(shiftData.get(3)), Integer.parseInt(shiftData.get(4)));
                }
            }
        }
        views.setTextViewText(R.id.lblWidTodVanTot, tekst);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static String geefDataWerk(int dag, boolean wisselHeeftVoorrang) {
        List<String> lst = new ArrayList<String>();
        lst = werkData.get(dag);

        if (wisselHeeftVoorrang) {
            List<String> wisselList = geefDataWissel(dag);
            if (wisselList != null) {
                String strWisselData = wisselList.get(1);/* + " " + wisselList[2] + " " + wisselList[0]*/
                ;

                if (!(strWisselData.equals(""))) {
                    hasWissel = true;
                    if (wisselList.get(3).equals("True")) {
                        return "ZO " + strWisselData;
                    } else {
                        return strWisselData;
                    }
                }
            }
            hasWissel = false;
        }

        if (lst == null) {
            return "";
        }

        return lst.get(0);
    }

    static List<String> geefDataWissel(int dag) {
        List<String> lst = new ArrayList<>();
        lst = wisselData.get(dag);

        return lst;
    }

    static String geefDataPersoonlijk(int dag) {
        List<String> lst = new ArrayList<>();
        lst = persoonlijkData.get(dag);

        if (lst == null) {
            return "";
        }

        if (!(lst.get(0).equals(""))) {
            return lst.get(0);
        }

        return "";
    }
}

