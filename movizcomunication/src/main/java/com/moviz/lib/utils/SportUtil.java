/*  1:   */
package com.moviz.lib.utils;

/*  2:   */
/*  3:   */ public class SportUtil
/*  4:   */ {
    /*  5:   */
    public static final int getTime(double distance, double speed)
/*  6:   */ {
/*  7:13 */
        return (int) Math.round(distance / speed * 60.0D * 60.0D);
/*  8:   */
    }

    /*  9:   */
/* 10:   */
    public static final double getCalorie(double distance, int weight)
/* 11:   */ {
/* 12:24 */
        return weight * distance * 30.0D / 24.0D;
/* 13:   */
    }
/* 14:   */
}



/* Location:           C:\Users\Fujitsu\Downloads\libPFHWApi-for-android-ver-20140122.jar

 * Qualified Name:     SportUtil

 * JD-Core Version:    0.7.0.1

 */