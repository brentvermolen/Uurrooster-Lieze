package com.vermolen.uurrooster.Classes.Enums;

import android.content.Context;

import com.vermolen.uurrooster.R;

/**
 * Created by Brent on 20/11/2017.
 */

public enum Maand {
    JANUARY(1),
    FEBRUARY(2),
    MARCH(3),
    APRIL(4),
    MAY(5),
    JUNE(6),
    JULY(7),
    AUGUST(8),
    SEPTEMBER(9),
    OCTOBER(10),
    NOVEMBER(11),
    DECEMBER(12);

    private int nr;

    Maand(int nr) {
        this.nr = nr;
    }

    public int getNr() {
        return nr;
    }

    public static Maand valueOf(int i) {
        if (i == 0){
            i = 12;
        }
        if (i == 13){
            i = 1;
        }

        for (Maand b : Maand.values()) {
            if (b.getNr() == i) { return b; }
        }
        return null;
    }

    public static Maand valueOfByString(String i, Context context) {

        for (Maand b : Maand.values()) {
            if (b.toString(context).toLowerCase().equals(i.toLowerCase())) { return b; }
        }
        return null;
    }

    @Override
    public String toString(){
        String eersteLetter = super.toString().substring(0, 1);
        int lengte = super.toString().length();
        String rest = super.toString().substring(1, lengte);

        return eersteLetter.toUpperCase() + rest.toLowerCase();
    }

    public String toString(Context context) {
        String maandNaam = "";
        switch (getNr()){
            case 1:
                maandNaam = context.getResources().getString(R.string.january);
                break;
            case 2:
                maandNaam = context.getResources().getString(R.string.february);
                break;
            case 3:
                maandNaam = context.getResources().getString(R.string.march);
                break;
            case 4:
                maandNaam = context.getResources().getString(R.string.april);
                break;
            case 5:
                maandNaam = context.getResources().getString(R.string.may);
                break;
            case 6:
                maandNaam = context.getResources().getString(R.string.june);
                break;
            case 7:
                maandNaam = context.getResources().getString(R.string.july);
                break;
            case 8:
                maandNaam = context.getResources().getString(R.string.august);
                break;
            case 9:
                maandNaam = context.getResources().getString(R.string.september);
                break;
            case 10:
                maandNaam = context.getResources().getString(R.string.october);
                break;
            case 11:
                maandNaam = context.getResources().getString(R.string.november);
                break;
            case 12:
                maandNaam = context.getResources().getString(R.string.december);
                break;
        }

        String eersteLetter = maandNaam.substring(0, 1);
        int lengte = maandNaam.length();
        String rest = maandNaam.substring(1, lengte);

        return eersteLetter.toUpperCase() + rest.toLowerCase();
    }
}

