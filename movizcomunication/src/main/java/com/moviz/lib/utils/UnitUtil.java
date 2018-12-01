/*  1:   */
package com.moviz.lib.utils;

/*  2:   */
/*  3:   */ public class UnitUtil
/*  4:   */ {
    /*  5:   */
    public static double km2mile(double km)
/*  6:   */ {
/*  7: 4 */
        return km * 0.62137D;
/*  8:   */
    }

    /*  9:   */
/* 10:   */
    public static double mile2km(double mile)
/* 11:   */ {
/* 12: 5 */
        return mile / 0.62137D;
/* 13:   */
    }

    /* 14:   */
/* 15:   */
    public static double cm2inch(double cm)
/* 16:   */ {
/* 17: 7 */
        return cm * 0.3937D;
/* 18:   */
    }

    /* 19:   */
/* 20:   */
    public static double inch2cm(double inch)
/* 21:   */ {
/* 22: 8 */
        return inch / 0.3937D;
/* 23:   */
    }

    /* 24:   */
/* 25:   */
    public static double kg2pound(double kg)
/* 26:   */ {
/* 27:10 */
        return kg * 2.20462D;
/* 28:   */
    }

    /* 29:   */
/* 30:   */
    public static double pound2kg(double pound)
/* 31:   */ {
/* 32:11 */
        return pound / 2.20462D;
/* 33:   */
    }
/* 34:   */
}



/* Location:           C:\Users\Fujitsu\Downloads\libPFHWApi-for-android-ver-20140122.jar

 * Qualified Name:     UnitUtil

 * JD-Core Version:    0.7.0.1

 */