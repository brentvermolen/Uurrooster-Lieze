package com.vermolen.uurrooster.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
public class WeekWidget extends AppWidgetProvider {
    private static Map<Integer, List<String>> werkData;
    private static Map<Integer, List<String>> wisselData;
    private static Map<Integer, List<String>> persoonlijkData;

    private static int firstDay;

    private static RemoteViews views;

    private static User USER;
    private static UserDao userDao;
    private static SharedPreferences sharedPref;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        views = new RemoteViews(context.getPackageName(), R.layout.week_widget);

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

        firstDay = context.getSharedPreferences("Settings", Context.MODE_PRIVATE).getInt("firstDay", 0);
        firstDay += 2; //één voor zondag één voor index
        if (firstDay > 7){
            firstDay -= 7;
        }
        Intent intent = new Intent(context, HomeActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        views.setOnClickPendingIntent(R.id.WeekWidget, pendingIntent);


        Calendar currCal = Calendar.getInstance();
        int dag = currCal.get(Calendar.DAY_OF_MONTH);
        int maand = currCal.get(Calendar.MONTH) + 1;
        int jaar = currCal.get(Calendar.YEAR);
        int maxInMaand = Methodes.getMaxDagenInMaand(maand, jaar);

        int dagVdWeek = currCal.get(Calendar.DAY_OF_WEEK);
        //dagVdWeek -= 2; //Een voor zondag te verzetten en een voor de index
        dagVdWeek -= firstDay;
        while (dagVdWeek < 0){
            dagVdWeek += 7;
        }
        Maand m = Maand.valueOf(maand);
        werkData = Reader.getWerkData(m, jaar);
        wisselData = Reader.getWisselData(m, jaar);
        persoonlijkData = Reader.getPersoonlijkData(m, jaar);

        int vulDag = dag - dagVdWeek;
        int vulMaand = maand;
        int vulJaar = jaar;

        for (int i = 0; i < 7; i++) {
            if (vulDag < 1){
                vulMaand--;
                if (vulMaand == 0){
                    vulJaar--;
                    vulMaand = 12;
                }
                maxInMaand = Methodes.getMaxDagenInMaand(vulMaand, vulJaar);
                vulDag = maxInMaand + vulDag;
            }
            if (vulDag > maxInMaand){
                vulMaand++;
                if (vulMaand == 13){
                    vulJaar++;
                    vulMaand = 1;
                }
                vulDag = 1;
            }

            if (vulMaand != maand || vulJaar != jaar){
                m = Maand.valueOf(vulMaand);
                werkData = Reader.getWerkData(m, vulJaar);
                wisselData = Reader.getWisselData(m, vulJaar);
                persoonlijkData = Reader.getPersoonlijkData(m, vulJaar);
                maand = vulMaand;
                jaar = vulJaar;
            }

            switch (i + 1){
                case 1:
                    vulMaandag(vulDag, vulMaand, context);
                    break;
                case 2:
                    vulDinsdag(vulDag, vulMaand, context);
                    break;
                case 3:
                    vulWoensdag(vulDag, vulMaand, context);
                    break;
                case 4:
                    vulDonderdag(vulDag, vulMaand, context);
                    break;
                case 5:
                    vulVrijdag(vulDag, vulMaand, context);
                    break;
                case 6:
                    vulZaterdag(vulDag, vulMaand, context);
                    break;
                case 7:
                    vulZondag(vulDag, vulMaand, context);
                    break;
            }

            vulDag++;
        }

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

    static String geefDataWerk(int dag, boolean wisselHeeftVoorrang) {
        List<String> lst = new ArrayList<String>();
        lst = werkData.get(dag);

        if (wisselHeeftVoorrang) {
            List<String> wisselList = geefDataWissel(dag);
            if (wisselList != null) {
                String strWisselData = wisselList.get(1);/* + " " + wisselList[2] + " " + wisselList[0]*/
                ;

                if (!(strWisselData.equals(""))) {
                    if (wisselList.get(3).equals("True")) {
                        return "ZO " + strWisselData;
                    } else {
                        return strWisselData;
                    }
                }
            }
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

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private static void vulMaandag(int dag, int maand, Context context){
        views.setTextViewText(R.id.lblWidWeekDagHeadMA, Methodes.getDagVerkort(firstDay++, context) + "\n" + String.valueOf(dag) + "/" + String.format("%02d", maand));

        String shift = geefDataWerk(dag, true);
        views.setTextViewText(R.id.lblWidWeekShiftMA, shift);

        String pers = geefDataPersoonlijk(dag);
        if (pers.equals("")){
            views.setTextViewText(R.id.lblWidWeekPersMA, "");
        }else{
            views.setTextViewText(R.id.lblWidWeekPersMA, "+");
        }
    }

    private static void vulDinsdag(int dag, int maand, Context context){
        views.setTextViewText(R.id.lblWidWeekDagHeadDI, Methodes.getDagVerkort(firstDay++, context) + "\n" + String.valueOf(dag) + "/" + String.format("%02d", maand));

        String shift = geefDataWerk(dag, true);
        views.setTextViewText(R.id.lblWidWeekShiftDI, shift);

        String pers = geefDataPersoonlijk(dag);
        if (pers.equals("")){
            views.setTextViewText(R.id.lblWidWeekPersDI, "");
        }else{
            views.setTextViewText(R.id.lblWidWeekPersDI, "+");
        }
    }

    private static void vulWoensdag(int dag, int maand, Context context){
        views.setTextViewText(R.id.lblWidWeekDagHeadWO, Methodes.getDagVerkort(firstDay++, context) + "\n" + String.valueOf(dag) + "/" + String.format("%02d", maand));

        String shift = geefDataWerk(dag, true);
        views.setTextViewText(R.id.lblWidWeekShiftWO, shift);

        String pers = geefDataPersoonlijk(dag);
        if (pers.equals("")){
            views.setTextViewText(R.id.lblWidWeekPersWO, "");
        }else{
            views.setTextViewText(R.id.lblWidWeekPersWO, "+");
        }
    }

    private static void vulDonderdag(int dag, int maand, Context context){
        views.setTextViewText(R.id.lblWidWeekDagHeadDO, Methodes.getDagVerkort(firstDay++, context) + "\n" + String.valueOf(dag) + "/" + String.format("%02d", maand));

        String shift = geefDataWerk(dag, true);
        views.setTextViewText(R.id.lblWidWeekShiftDO, shift);

        String pers = geefDataPersoonlijk(dag);
        if (pers.equals("")){
            views.setTextViewText(R.id.lblWidWeekPersDO, "");
        }else{
            views.setTextViewText(R.id.lblWidWeekPersDO, "+");
        }
    }

    private static void vulVrijdag(int dag, int maand, Context context){
        views.setTextViewText(R.id.lblWidWeekDagHeadVR, Methodes.getDagVerkort(firstDay++, context) + "\n" + String.valueOf(dag) + "/" + String.format("%02d", maand));

        String shift = geefDataWerk(dag, true);
        views.setTextViewText(R.id.lblWidWeekShiftVR, shift);

        String pers = geefDataPersoonlijk(dag);
        if (pers.equals("")){
            views.setTextViewText(R.id.lblWidWeekPersVR, "");
        }else{
            views.setTextViewText(R.id.lblWidWeekPersVR, "+");
        }
    }

    private static void vulZaterdag(int dag, int maand, Context context){
        views.setTextViewText(R.id.lblWidWeekDagHeadZA, Methodes.getDagVerkort(firstDay++, context) + "\n" + String.valueOf(dag) + "/" + String.format("%02d", maand));

        String shift = geefDataWerk(dag, true);
        views.setTextViewText(R.id.lblWidWeekShiftZA, shift);

        String pers = geefDataPersoonlijk(dag);
        if (pers.equals("")){
            views.setTextViewText(R.id.lblWidWeekPersZA, "");
        }else{
            views.setTextViewText(R.id.lblWidWeekPersZA, "+");
        }
    }

    private static void vulZondag(int dag, int maand, Context context){
        views.setTextViewText(R.id.lblWidWeekDagHeadZO, Methodes.getDagVerkort(firstDay, context) + "\n" + String.valueOf(dag) + "/" + String.format("%02d", maand));

        String shift = geefDataWerk(dag, true);
        views.setTextViewText(R.id.lblWidWeekShiftZO, shift);

        String pers = geefDataPersoonlijk(dag);
        if (pers.equals("")){
            views.setTextViewText(R.id.lblWidWeekPersZO, "");
        }else{
            views.setTextViewText(R.id.lblWidWeekPersZO, "+");
        }
    }
}

