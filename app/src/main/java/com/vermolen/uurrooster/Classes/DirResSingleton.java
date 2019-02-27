package com.vermolen.uurrooster.Classes;

import java.io.File;

public class DirResSingleton {
    private static File dirRes;

    public static File getInstance(){
        return dirRes;
    }

    public static void setInstance(File dirRes){
        DirResSingleton.dirRes = dirRes;
    }
}
