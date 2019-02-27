package com.vermolen.uurrooster.Classes.Enums;

import android.content.Context;

import com.vermolen.uurrooster.R;

/**
 * Created by Brent on 21/11/2017.
 */

public enum Voorkeur {
    BACKGROUND_SHIFT_CHANGE(0),
    BACKGROUND_CALENDAR_HEADER(1),
    BACKGROUND_DAYS(2),
    BACKGROUND_DAYS_HEADER(3),
    BACKGROUND_CALENDAR(4),
    LETTERTYPE_HEADERS(5),
    LETTERTYPE_DAG_HEADERS(6),
    LETTERTYPE_DAG_INHOUD(7),
    TEXTCOLOR_TITELS(8),
    TEXTCOLOR_DAYS(9),
    TEXTCOLOR_DAY_HEADER(10),
    TEXTCOLOR_DAY_CONTENT(11),
    BACKGROUND_WEEKDAYS(12),
    LETTERTYPE_WEEKDAGEN(13),
    DETAILS_ACHTERGROND(14),
    DETAILS_LETTERTYPE_DATUM(15),
    DETAILS_LETTERTYPE_TITELS(16),
    DETAILS_LETTERTYPE_INHOUD(17),
    DETAILS_KLEUR_DATUM(18),
    DETAILS_KLEUR_TITELS(19),
    DETAILS_KLEUR_INHOUD(20),
    DETAILS_ACHTERGROND_DATUM(21),
    DETAILS_ACHTERGROND_TITELS(22),
    DETAILS_ACHTERGROND_INHOUD(23);

    /*BACKGROUND_CALENDAR,
    BACKGROUND_CALENDAR_HEADER,
    BACKGROUND_WEEKDAYS,
    BACKGROUND_DAYS_HEADER,
    BACKGROUND_DAYS,
    BACKGROUND_SHIFT_CHANGE;*/

    private int nr;

    public int getNr(){
        return nr;
    }

    Voorkeur(int nr){
        this.nr = nr;
    }

    @Override
    public String toString() {
        return super.toString().replace("_", " ");
    }

    public static Voorkeur valueOfNr(int i) {
        for (Voorkeur v : Voorkeur.values()){
            if (v.getNr() == i){
                return v;
            }
        }
        return null;
    }

    public String toString(Context context){
        int id = context.getResources().getIdentifier(super.toString(), "string", context.getPackageName());
        return context.getResources().getString(id).replace("_", " ");
    }
}
