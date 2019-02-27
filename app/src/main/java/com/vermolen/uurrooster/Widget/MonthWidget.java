package com.vermolen.uurrooster.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.vermolen.uurrooster.Classes.DirResSingleton;
import com.vermolen.uurrooster.Classes.Enums.Maand;
import com.vermolen.uurrooster.Classes.Enums.Voorkeur;
import com.vermolen.uurrooster.Classes.Methodes;
import com.vermolen.uurrooster.Classes.Reader;
import com.vermolen.uurrooster.Classes.TextReader;
import com.vermolen.uurrooster.Classes.UserSingleton;
import com.vermolen.uurrooster.DB.UserDao;
import com.vermolen.uurrooster.HomeActivity;
import com.vermolen.uurrooster.LoginActivity;
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
public class MonthWidget extends AppWidgetProvider {

    private static RemoteViews views;

    private static Map<Integer, List<String>> werkData;
    private static Map<Integer, List<String>> wisselData;
    private static Map<Integer, List<String>> persoonlijkData;
    private static Map<Voorkeur, String> voorkeuren;

    private static Map<Integer, List<String>> werkDataVorige;
    private static Map<Integer, List<String>> werkDataVolgende;

    private static Maand maand;
    private static int jaar;

    private static boolean hasWissel;

    private static boolean useBackgroundShiftChange;
    private static boolean useBackgroundShift;
    private static boolean showPersonalNotes;
    private static String alternatif;
    private static int firstDay;
    private static int intTextSize;
    private static User USER;
    private static UserDao userDao;
    private static SharedPreferences sharedPref;

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        Log.e("context", context.toString());
        views = new RemoteViews(context.getPackageName(), R.layout.month_widget);

        Intent intent = new Intent(context, HomeActivity.class);

        sharedPref = context.getApplicationContext().getSharedPreferences("Settings", MODE_PRIVATE);
        userDao = new UserDao();

        USER = null;

        if (USER == null){
            final int sharedUserId = context.getSharedPreferences("USER", MODE_PRIVATE).getInt("USER", -1);
            Log.e("shared", sharedUserId + "");

            if (sharedUserId == -1) {
                User user = new User();
                user.setUser_id(-1);
                UserSingleton.setInstance(user);
                USER = user;

                File dirRes = new File(context.getSharedPreferences("USER", MODE_PRIVATE).getString("DirRes", context.getApplicationContext().getFilesDir().getAbsolutePath() + "//Resources//"));
                DirResSingleton.setInstance(dirRes);
            }else {
                USER = userDao.getUserById(sharedUserId);
                Log.e("user", USER.getUser_id() + "");
                UserSingleton.setInstance(USER);
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        views.setOnClickPendingIntent(R.id.MonthWidget, pendingIntent);

        loadSettings(context);

        vulKalender(context, appWidgetManager, appWidgetId);
    }

    private static void loadSettings(Context context) {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences("Settings", MODE_PRIVATE);
        useBackgroundShift = sharedPref.getBoolean("backgroundShift", true);
        useBackgroundShiftChange = sharedPref.getBoolean("backgroundShiftChange", true);
        showPersonalNotes = sharedPref.getBoolean("showPersonalNotes", true);
        alternatif = sharedPref.getString("alternatief", context.getResources().getString(R.string.notes));
        firstDay = sharedPref.getInt("firstDay", 0);
        intTextSize = sharedPref.getInt("textSize", 10);
        intTextSize = 10;
    }

    private static void vulKalender(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId) {
        maand = Maand.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1);
        jaar = Calendar.getInstance().get(Calendar.YEAR);
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.MONTH, maand.getNr() - 1);
        cal.set(Calendar.YEAR, jaar);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                voorkeuren = Reader.getVoorkeuren();

                werkData = Reader.getWerkData(maand, jaar);
                wisselData = Reader.getWisselData(maand, jaar);
                persoonlijkData = Reader.getPersoonlijkData(maand, jaar);

                Maand vorigeMaand = Maand.valueOf(maand.getNr() - 1);
                int vorigJaar = jaar;

                if (vorigeMaand.getNr() == 12) {
                    vorigJaar--;
                }
                werkDataVorige = Reader.getWerkData(vorigeMaand, vorigJaar);

                Maand volgendeMaand = Maand.valueOf(maand.getNr() + 1);
                int volgendJaar = jaar;

                if (volgendeMaand.getNr() == 1){
                    volgendJaar++;
                }
                werkDataVolgende = Reader.getWerkData(volgendeMaand, volgendJaar);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                int intColor;
                try {
                    intColor = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_CALENDAR));
                    views.setInt(R.id.KalenderForm, "setBackgroundColor", intColor);
                } catch (Exception e) {
                    views.setInt(R.id.KalenderForm, "setBackgroundColor", Color.TRANSPARENT);
                }

                try {
                    intColor = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_CALENDAR_HEADER));
                    views.setInt(R.id.lblMaand, "setBackgroundColor", intColor);
                } catch (Exception e) {
                    views.setInt(R.id.lblMaand, "setBackgroundColor", Color.TRANSPARENT);
                }


                try {
                    intColor = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_WEEKDAYS));
                    views.setInt(R.id.header, "setBackgroundColor", intColor);
                } catch (Exception e) {
                    views.setInt(R.id.header, "setBackgroundColor", Color.GRAY);
                }

                try {
                    intColor = Integer.parseInt(voorkeuren.get(Voorkeur.TEXTCOLOR_TITELS));
                    views.setInt(R.id.lblMaand, "setTextColor", intColor);
                    views.setInt(R.id.lblHeadMA, "setTextColor", intColor);
                    views.setInt(R.id.lblHeadDI, "setTextColor", intColor);
                    views.setInt(R.id.lblHeadWO, "setTextColor", intColor);
                    views.setInt(R.id.lblHeadDO, "setTextColor", intColor);
                    views.setInt(R.id.lblHeadVR, "setTextColor", intColor);
                    views.setInt(R.id.lblHeadZA, "setTextColor", intColor);
                    views.setInt(R.id.lblHeadZO, "setTextColor", intColor);
                } catch (Exception e) {
                }

                views.setTextViewText(R.id.lblMaand, maand.toString(context) + " " + String.valueOf(jaar));

                int firstDayOfWeekThisMonth;

                switch (cal.get(Calendar.DAY_OF_WEEK)) {
                    case Calendar.MONDAY:
                        firstDayOfWeekThisMonth = 1;
                        break;
                    case Calendar.TUESDAY:
                        firstDayOfWeekThisMonth = 2;
                        break;
                    case Calendar.WEDNESDAY:
                        firstDayOfWeekThisMonth = 3;
                        break;
                    case Calendar.THURSDAY:
                        firstDayOfWeekThisMonth = 4;
                        break;
                    case Calendar.FRIDAY:
                        firstDayOfWeekThisMonth = 5;
                        break;
                    case Calendar.SATURDAY:
                        firstDayOfWeekThisMonth = 6;
                        break;
                    case Calendar.SUNDAY:
                        firstDayOfWeekThisMonth = 7;
                        break;
                    default:
                        firstDayOfWeekThisMonth = 1;
                        break;
                }

                if (firstDayOfWeekThisMonth == 1) {
                    firstDayOfWeekThisMonth = 8;
                }

                firstDayOfWeekThisMonth -= firstDay;
                if (firstDayOfWeekThisMonth < 1){
                    firstDayOfWeekThisMonth += 7;
                }

                firstDay += 2;
                if (firstDay >= 8){
                    firstDay -= 7;
                }

                views.setTextViewText(R.id.lblHeadMA, Methodes.getDagVerkort(firstDay++, context));
                views.setTextViewText(R.id.lblHeadDI, Methodes.getDagVerkort(firstDay++, context));
                views.setTextViewText(R.id.lblHeadWO, Methodes.getDagVerkort(firstDay++, context));
                views.setTextViewText(R.id.lblHeadDO, Methodes.getDagVerkort(firstDay++, context));
                views.setTextViewText(R.id.lblHeadVR, Methodes.getDagVerkort(firstDay++, context));
                views.setTextViewText(R.id.lblHeadZA, Methodes.getDagVerkort(firstDay++, context));
                views.setTextViewText(R.id.lblHeadZO, Methodes.getDagVerkort(firstDay, context));

                boolean firstDayFound = false;
                int huidigeDag = 1;
                int volgendeMaand = 1;

                for (int i = 1; i <= 42; i++) {
                    final int panelNr = i;
                    final int dagNr = huidigeDag;

                    Resources res = context.getResources();
                    int id = res.getIdentifier("dag" + i, "id", context.getPackageName());
                    views.removeAllViews(id);

                    //Kalender
                    LayoutInflater kalenderInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    RemoteViews kalenderView = new RemoteViews(context.getPackageName(), R.layout.dag_layout_widget);

                    // fill in any details dynamically here
            /*LinearLayout pnlDagInhoud = (LinearLayout) kalenderView.findViewById(R.id.pnlInhoudDag);

            TextView lblHeaderDag = (TextView) kalenderView.findViewById(R.id.lblHeaderDag);
            final TextView lblWerkData = (TextView) kalenderView.findViewById(R.id.lblInhoudDagWerk);
            final TextView lblPersoonlijkData = (TextView) kalenderView.findViewById(R.id.lblInhoudDagPersoonlijk);*/
                    kalenderView.setTextViewText(R.id.lblInhoudDagWerk, "");
                    kalenderView.setTextViewText(R.id.lblInhoudDagPersoonlijk, "");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        kalenderView.setTextViewTextSize(R.id.lblInhoudDagWerk, TypedValue.COMPLEX_UNIT_SP, intTextSize);
                        kalenderView.setTextViewTextSize(R.id.lblInhoudDagPersoonlijk, TypedValue.COMPLEX_UNIT_SP, intTextSize);
                    }

                    kalenderView.setViewVisibility(R.id.lblInhoudDagPersoonlijk, View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        //kalenderView.setTextViewTextSize(R.id.lblInhoudDagPersoonlijk, 10, 10);
                    }

                    // insert into main view
                    views.addView(id, kalenderView);

                    final int maxDagenInHuidigeMaand = Methodes.getMaxDagenInMaand(maand.getNr(), jaar);
                    final int maxDagenInVorigeMaand = Methodes.getMaxDagenInMaand(maand.getNr() - 1, jaar);

                    if (firstDayOfWeekThisMonth == i) {
                        firstDayFound = true;
                    }

                    if (firstDayFound) {
                        if (huidigeDag > maxDagenInHuidigeMaand) {
                            if (i == 36){
                                views.setViewVisibility(R.id.row6, View.GONE);
                            }

                            kalenderView.setTextViewText(R.id.lblHeaderDag, String.valueOf(volgendeMaand));
                            try{
                        /*Bitmap bitmap = createBackground(Color.GRAY);
                        kalenderView.setImageViewBitmap(R.id.imgAchtergrondDagHeader, bitmap);*/
                                kalenderView.setInt(R.id.lblHeaderDag, "setBackgroundColor", Color.LTGRAY);
                            }catch (Exception e){
                                //kalenderView.setInt(R.id.lblHeaderDag, "setBackgroundColor", Color.LTGRAY);
                            }

                            try {
                                intColor = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_DAYS));
                                try {
                                    Bitmap bitmap = createBackground(intColor);
                                    kalenderView.setImageViewBitmap(R.id.imgAchtergrondDag, bitmap);
                                } catch (Exception e) {
                                    Bitmap bitmap = createBackground(Color.GRAY);
                                    kalenderView.setImageViewBitmap(R.id.imgAchtergrondDag, bitmap);
                                }
                            } catch (Exception e) {
                                Bitmap bitmap = createBackground(Color.GRAY);
                                kalenderView.setImageViewBitmap(R.id.imgAchtergrondDag, bitmap);
                            }

                            try{
                                kalenderView.setTextViewText(R.id.lblInhoudDagWerk, werkDataVolgende.get(volgendeMaand++).get(0));
                            }catch (Exception e){ }
                        } else {

                            try {
                                intColor = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_DAYS));
                                try {
                                    Bitmap bitmap = createBackground(intColor);
                                    kalenderView.setImageViewBitmap(R.id.imgAchtergrondDag, bitmap);
                                } catch (Exception e) {
                                    Bitmap bitmap = createBackground(Color.LTGRAY);
                                    kalenderView.setImageViewBitmap(R.id.imgAchtergrondDag, bitmap);
                                }
                            } catch (Exception e) {
                                Bitmap bitmap = createBackground(Color.LTGRAY);
                                kalenderView.setImageViewBitmap(R.id.imgAchtergrondDag, bitmap);
                            }


                            Calendar currCal = Calendar.getInstance();
                            if (huidigeDag == (int) currCal.get(Calendar.DAY_OF_MONTH) && (maand.getNr() - 1) == (int) currCal.get(Calendar.MONTH) && jaar == currCal.get(Calendar.YEAR)) {
                                kalenderView.setTextViewText(R.id.lblHeaderDag, "*" + String.valueOf(huidigeDag) + "*");
                            } else {
                                kalenderView.setTextViewText(R.id.lblHeaderDag, String.valueOf(huidigeDag));
                            }

                            String shift = geefDataWerk(huidigeDag, true);
                            String activiteit = geefDataPersoonlijk(huidigeDag);

                            kalenderView.setTextViewText(R.id.lblInhoudDagWerk, shift);
                    /*if (!activiteit.equals("")) {
                        kalenderView.setTextViewText(R.id.lblInhoudDagPersoonlijk, "+");
                    }*/

                            if (!activiteit.equals("")) {
                                if (showPersonalNotes){
                                    kalenderView.setTextViewText(R.id.lblInhoudDagPersoonlijk, activiteit);
                                }else{
                                    kalenderView.setTextViewText(R.id.lblInhoudDagPersoonlijk, alternatif);
                                }
                            }

                            int kleur;
                            try {
                                kleur = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_DAYS_HEADER));
                        /*Bitmap bitmap = createBackground(kleur);
                        kalenderView.setImageViewBitmap(R.id.imgAchtergrondDagHeader, bitmap);*/
                                kalenderView.setInt(R.id.lblHeaderDag, "setBackgroundColor", kleur);
                            } catch (Exception e) {
                                kalenderView.setInt(R.id.lblHeaderDag, "setBackgroundColor", Color.GRAY);
                            }
                            try {
                                kleur = Integer.parseInt(voorkeuren.get(Voorkeur.TEXTCOLOR_DAY_HEADER));
                                kalenderView.setInt(R.id.lblHeaderDag, "setTextColor", kleur);
                            } catch (Exception e) {
                            }
                            try {
                                kleur = Integer.parseInt(voorkeuren.get(Voorkeur.TEXTCOLOR_DAY_CONTENT));
                                kalenderView.setInt(R.id.lblInhoudDagWerk, "setTextColor", kleur);
                                kalenderView.setInt(R.id.lblInhoudDagPersoonlijk, "setTextColor", kleur);
                            } catch (Exception e) {
                            }

                            if (!useBackgroundShiftChange){
                                hasWissel = false;
                            }

                            if (hasWissel) {
                                try {
                                    kleur = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_SHIFT_CHANGE));
                                    Bitmap bitmap = createBackground(kleur);
                                    kalenderView.setImageViewBitmap(R.id.imgAchtergrondDag, bitmap);
                                } catch (Exception e) {
                                    Bitmap bitmap = createBackground(Color.WHITE);
                                    kalenderView.setImageViewBitmap(R.id.imgAchtergrondDag, bitmap);
                                }
                            }

                            if (useBackgroundShift) {
                                List<String> shiftData = Reader.getShiften().get(shift);

                                if (shiftData != null) {
                                    kleur = Integer.parseInt(shiftData.get(5));
                                    if (kleur != 0) {
                                        Bitmap bitmap = createBackground(kleur);
                                        kalenderView.setImageViewBitmap(R.id.imgAchtergrondDag, bitmap);
                                    }
                                }
                            }
                        }

                        huidigeDag++;
                    } else {
                        if (i == 7){
                            views.setViewVisibility(R.id.row1, View.GONE);
                        }

                        int vorige = maxDagenInVorigeMaand - (firstDayOfWeekThisMonth - 1) + panelNr;
                        kalenderView.setTextViewText(R.id.lblHeaderDag, String.valueOf(vorige));

                        try{
                        /*Bitmap bitmap = createBackground(Color.GRAY);
                        kalenderView.setImageViewBitmap(R.id.imgAchtergrondDagHeader, bitmap);*/
                            kalenderView.setInt(R.id.lblHeaderDag, "setBackgroundColor", Color.LTGRAY);
                        }catch (Exception e){
                            //kalenderView.setInt(R.id.lblHeaderDag, "setBackgroundColor", Color.LTGRAY);
                        }

                        try {
                            intColor = Integer.parseInt(voorkeuren.get(Voorkeur.BACKGROUND_DAYS));
                            try {
                                Bitmap bitmap = createBackground(intColor);
                                kalenderView.setImageViewBitmap(R.id.imgAchtergrondDag, bitmap);
                            } catch (Exception e) {
                                Bitmap bitmap = createBackground(Color.GRAY);
                                kalenderView.setImageViewBitmap(R.id.imgAchtergrondDag, bitmap);
                            }
                        } catch (Exception e) {
                            Bitmap bitmap = createBackground(Color.GRAY);
                            kalenderView.setImageViewBitmap(R.id.imgAchtergrondDag, bitmap);
                        }

                        try{
                            kalenderView.setTextViewText(R.id.lblInhoudDagWerk, werkDataVorige.get(vorige).get(0));
                        }catch (Exception e){ }
                    }
                }

                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static Bitmap createBackground(int kleur) {
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
                            /*GradientDrawable gradientDrawable = (GradientDrawable) context.getDrawable(R.drawable.border_layout);
                            gradientDrawable.setColor(kleur);
                            gradientDrawable.draw(canvas);*/

        Paint mFillPaint = new Paint();
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setColor(kleur);

        RectF mRectF = new RectF();
        mRectF.set(0, 0, 200, 200);
        canvas.drawRoundRect(mRectF, 0, 0, mFillPaint);
        canvas.drawBitmap(bitmap, 0, 0, new Paint());//draw your image on bg

        return bitmap;
    }

    private static String geefDataWerk(int dag, boolean wisselHeeftVoorrang) {
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

    private static List<String> geefDataWissel(int dag) {
        List<String> lst = new ArrayList<>();
        lst = wisselData.get(dag);

        return lst;
    }

    private static String geefDataPersoonlijk(int dag) {
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
}

