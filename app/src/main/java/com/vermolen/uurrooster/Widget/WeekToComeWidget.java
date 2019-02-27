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
public class WeekToComeWidget extends AppWidgetProvider {
    private static Map<Integer, List<String>> werkData;
    private static Map<Integer, List<String>> wisselData;
    private static Map<Integer, List<String>> persoonlijkData;
    private static User USER;
    private static UserDao userDao;
    private static SharedPreferences sharedPref;

    private static RemoteViews views;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        views = new RemoteViews(context.getPackageName(), R.layout.week_to_come_widget);

            Intent intent = new Intent(context, HomeActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            views.setOnClickPendingIntent(R.id.WeekToComeWidget, pendingIntent);

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
            int maand = currCal.get(Calendar.MONTH) + 1;
            int jaar = currCal.get(Calendar.YEAR);
            int maxInMaand = Methodes.getMaxDagenInMaand(maand, jaar);

            int dagVdWeek = currCal.get(Calendar.DAY_OF_WEEK);

            Maand m = Maand.valueOf(maand);
            werkData = Reader.getWerkData(m, jaar);
            wisselData = Reader.getWisselData(m, jaar);
            persoonlijkData = Reader.getPersoonlijkData(m, jaar);

            int vulDag = dag;
            int vulMaand = maand;
            int vulJaar = jaar;

            for (int i = 0; i < 7; i++) {
                if (vulDag > maxInMaand){
                    vulMaand++;
                    if (vulMaand == 13){
                        vulJaar++;
                        vulMaand = 1;
                    }
                    vulDag -= maxInMaand;
                    maxInMaand = Methodes.getMaxDagenInMaand(vulMaand, vulJaar);
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
                        vulEen(vulDag, vulMaand, dagVdWeek, context);
                        break;
                    case 2:
                        vulTwee(vulDag, vulMaand, dagVdWeek, context);
                        break;
                    case 3:
                        vulDrie(vulDag, vulMaand, dagVdWeek, context);
                        break;
                    case 4:
                        vulVier(vulDag, vulMaand, dagVdWeek, context);
                        break;
                    case 5:
                        vulVijf(vulDag, vulMaand, dagVdWeek, context);
                        break;
                    case 6:
                        vulZes(vulDag, vulMaand, dagVdWeek, context);
                        break;
                    case 7:
                        vulZeven(vulDag, vulMaand, dagVdWeek, context);
                        break;
                }

                dagVdWeek++;
                if (dagVdWeek == 8){
                    dagVdWeek = 1;
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

    private static void vulEen(int dag, int maand, int dagVanWeek, Context context){
        String strDagVWeek = Methodes.getDagVerkort(dagVanWeek, context);
        views.setTextViewText(R.id.lblWidWeekDagHead1, strDagVWeek + "\n" + String.valueOf(dag) + "/" + String.format("%02d", maand));

        String shift = geefDataWerk(dag, true);
        views.setTextViewText(R.id.lblWidWeekShift1, shift);

        String pers = geefDataPersoonlijk(dag);
        if (pers.equals("")){
            views.setTextViewText(R.id.lblWidWeekPers1, "");
        }else{
            views.setTextViewText(R.id.lblWidWeekPers1, "+");
        }
    }

    private static void vulTwee(int dag, int maand, int dagVanWeek, Context context){
        String strDagVWeek = Methodes.getDagVerkort(dagVanWeek, context);
        views.setTextViewText(R.id.lblWidWeekDagHead2, strDagVWeek + "\n" + String.valueOf(dag) + "/" + String.format("%02d", maand));

        String shift = geefDataWerk(dag, true);
        views.setTextViewText(R.id.lblWidWeekShift2, shift);

        String pers = geefDataPersoonlijk(dag);
        if (pers.equals("")){
            views.setTextViewText(R.id.lblWidWeekPers2, "");
        }else{
            views.setTextViewText(R.id.lblWidWeekPers2, "+");
        }
    }

    private static void vulDrie(int dag, int maand, int dagVanWeek, Context context){
        String strDagVWeek = Methodes.getDagVerkort(dagVanWeek, context);
        views.setTextViewText(R.id.lblWidWeekDagHead3, strDagVWeek + "\n" + String.valueOf(dag) + "/" + String.format("%02d", maand));

        String shift = geefDataWerk(dag, true);
        views.setTextViewText(R.id.lblWidWeekShift3, shift);

        String pers = geefDataPersoonlijk(dag);
        if (pers.equals("")){
            views.setTextViewText(R.id.lblWidWeekPers3, "");
        }else{
            views.setTextViewText(R.id.lblWidWeekPers3, "+");
        }
    }

    private static void vulVier(int dag, int maand, int dagVanWeek, Context context){
        String strDagVWeek = Methodes.getDagVerkort(dagVanWeek, context);
        views.setTextViewText(R.id.lblWidWeekDagHead4, strDagVWeek + "\n" + String.valueOf(dag) + "/" + String.format("%02d", maand));

        String shift = geefDataWerk(dag, true);
        views.setTextViewText(R.id.lblWidWeekShift4, shift);

        String pers = geefDataPersoonlijk(dag);
        if (pers.equals("")){
            views.setTextViewText(R.id.lblWidWeekPers4, "");
        }else{
            views.setTextViewText(R.id.lblWidWeekPers4, "+");
        }
    }

    private static void vulVijf(int dag, int maand, int dagVanWeek, Context context){
        String strDagVWeek = Methodes.getDagVerkort(dagVanWeek, context);
        views.setTextViewText(R.id.lblWidWeekDagHead5, strDagVWeek + "\n" + String.valueOf(dag) + "/" + String.format("%02d", maand));

        String shift = geefDataWerk(dag, true);
        views.setTextViewText(R.id.lblWidWeekShift5, shift);

        String pers = geefDataPersoonlijk(dag);
        if (pers.equals("")){
            views.setTextViewText(R.id.lblWidWeekPers5, "");
        }else{
            views.setTextViewText(R.id.lblWidWeekPers5, "+");
        }
    }

    private static void vulZes(int dag, int maand, int dagVanWeek, Context context){
        String strDagVWeek = Methodes.getDagVerkort(dagVanWeek, context);
        views.setTextViewText(R.id.lblWidWeekDagHead6, strDagVWeek + "\n" + String.valueOf(dag) + "/" + String.format("%02d", maand));

        String shift = geefDataWerk(dag, true);
        views.setTextViewText(R.id.lblWidWeekShift6, shift);

        String pers = geefDataPersoonlijk(dag);
        if (pers.equals("")){
            views.setTextViewText(R.id.lblWidWeekPers6, "");
        }else{
            views.setTextViewText(R.id.lblWidWeekPers6, "+");
        }
    }

    private static void vulZeven(int dag, int maand, int dagVanWeek, Context context){
        String strDagVWeek = Methodes.getDagVerkort(dagVanWeek, context);
        views.setTextViewText(R.id.lblWidWeekDagHead7, strDagVWeek + "\n" + String.valueOf(dag) + "/" + String.format("%02d", maand));

        String shift = geefDataWerk(dag, true);
        views.setTextViewText(R.id.lblWidWeekShift7, shift);

        String pers = geefDataPersoonlijk(dag);
        if (pers.equals("")){
            views.setTextViewText(R.id.lblWidWeekPers7, "");
        }else {
            views.setTextViewText(R.id.lblWidWeekPers7, "+");
        }
    }
}

