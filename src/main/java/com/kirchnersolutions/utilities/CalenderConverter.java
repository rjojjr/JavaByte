/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirchnersolutions.utilities;

import java.util.Calendar;

/**
 *
 * @author rjojj
 */
public class CalenderConverter {
    
    public static String getMonthDayYearHourMinuteSecond(Long time, String dateSeperator, String hourMinSecSeperator){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String mYear = calendar.get(Calendar.YEAR) + "";
        String mMonth = "";
        if(calendar.get(Calendar.MONTH) + 1 < 10){
            mMonth = "0" + (calendar.get(Calendar.MONTH) + 1);
        }else {
            mMonth = "" + (calendar.get(Calendar.MONTH) + 1);
        }
        String mHour = "";
        if(calendar.get(Calendar.HOUR_OF_DAY) < 10){
            mHour = "0" + calendar.get(Calendar.HOUR_OF_DAY);
        }else {
            mHour = "" + calendar.get(Calendar.HOUR_OF_DAY);
        }
        String mMin = "";
        if(calendar.get(Calendar.MINUTE) < 10){
            mMin = "0" + calendar.get(Calendar.MINUTE);
        }else {
            mMin = "" + calendar.get(Calendar.MINUTE);
        }
        String mSec;
        if(calendar.get(Calendar.SECOND) < 10){
            mSec = "0" + calendar.get(Calendar.SECOND);
        }else {
            mSec = "" + calendar.get(Calendar.SECOND);
        }
        String mDay;
        if(calendar.get(Calendar.DAY_OF_MONTH) < 10){
            mDay = "0" + calendar.get(Calendar.DAY_OF_MONTH);
        }else {
            mDay = "" + calendar.get(Calendar.DAY_OF_MONTH);
        }
        return mMonth + dateSeperator + mDay + dateSeperator + mYear + hourMinSecSeperator  + mHour + hourMinSecSeperator  + mMin + hourMinSecSeperator  + mSec;
    }
    
    public static String getMonthDayYearHourMinute(Long time, String dateSeperator, String hourMinSecSeperator){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String mYear = calendar.get(Calendar.YEAR) + "";
        String mMonth = "";
        if(calendar.get(Calendar.MONTH) + 1 < 10){
            mMonth = "0" + (calendar.get(Calendar.MONTH) + 1);
        }else {
            mMonth = "" + (calendar.get(Calendar.MONTH) + 1);
        }
        String mDay;
        if(calendar.get(Calendar.DAY_OF_MONTH) < 10){
            mDay = "0" + calendar.get(Calendar.DAY_OF_MONTH);
        }else {
            mDay = "" + calendar.get(Calendar.DAY_OF_MONTH);
        }
        String mHour = "";
        if(calendar.get(Calendar.HOUR_OF_DAY) < 10){
            mHour = "0" + calendar.get(Calendar.HOUR_OF_DAY);
        }else {
            mHour = "" + calendar.get(Calendar.HOUR_OF_DAY);
        }
        String mMin = "";
        if(calendar.get(Calendar.MINUTE) < 10){
            mMin = "0" + calendar.get(Calendar.MINUTE);
        }else {
            mMin = "" + calendar.get(Calendar.MINUTE);
        }

        return mMonth + dateSeperator + mDay + dateSeperator + mYear + hourMinSecSeperator  + mHour + hourMinSecSeperator  + mMin;
    }
    
    public static String getMonthDayYearHour(Long time, String dateSeperator, String hourMinSecSeperator){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String mYear = calendar.get(Calendar.YEAR) + "";
        String mMonth = "";
        if(calendar.get(Calendar.MONTH) + 1 < 10){
            mMonth = "0" + (calendar.get(Calendar.MONTH) + 1);
        }else {
            mMonth = "" + (calendar.get(Calendar.MONTH) + 1);
        }
        String mDay;
        if(calendar.get(Calendar.DAY_OF_MONTH) < 10){
            mDay = "0" + calendar.get(Calendar.DAY_OF_MONTH);
        }else {
            mDay = "" + calendar.get(Calendar.DAY_OF_MONTH);
        }
        String mHour = "";
        if(calendar.get(Calendar.HOUR_OF_DAY) < 10){
            mHour = "0" + calendar.get(Calendar.HOUR_OF_DAY);
        }else {
            mHour = "" + calendar.get(Calendar.HOUR_OF_DAY);
        }
        return mMonth + dateSeperator + mDay + dateSeperator + mYear + hourMinSecSeperator + mHour;
    }
    
    public static String getMonthDayYear(Long time, String dateSeperator, String hourMinSecSeperator){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String mYear = calendar.get(Calendar.YEAR) + "";
        String mMonth = "";
        if(calendar.get(Calendar.MONTH) + 1 < 10){
            mMonth = "0" + (calendar.get(Calendar.MONTH) + 1);
        }else {
            mMonth = "" + (calendar.get(Calendar.MONTH) + 1);
        }
        String mDay;
        if(calendar.get(Calendar.DAY_OF_MONTH) < 10){
            mDay = "0" + calendar.get(Calendar.DAY_OF_MONTH);
        }else {
            mDay = "" + calendar.get(Calendar.DAY_OF_MONTH);
        }
        return mMonth + dateSeperator + mDay + dateSeperator + mYear;
    }
    
    public static String getHourMinuteSecond(Long time, String hourMinSecSeperator){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String mHour = "";
        if(calendar.get(Calendar.HOUR_OF_DAY) < 10){
            mHour = "0" + calendar.get(Calendar.HOUR_OF_DAY);
        }else {
            mHour = "" + calendar.get(Calendar.HOUR_OF_DAY);
        }
        String mMin = "";
        if(calendar.get(Calendar.MINUTE) < 10){
            mMin = "0" + calendar.get(Calendar.MINUTE);
        }else {
            mMin = "" + calendar.get(Calendar.MINUTE);
        }
        String mSec;
        if(calendar.get(Calendar.SECOND) < 10){
            mSec = "0" + calendar.get(Calendar.SECOND);
        }else {
            mSec = "" + calendar.get(Calendar.SECOND);
        }
        return mHour + hourMinSecSeperator  + mMin + hourMinSecSeperator  + mSec;
    }
    
    public static String getHourMinute(Long time, String hourMinSecSeperator){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String mHour = "";
        if(calendar.get(Calendar.HOUR_OF_DAY) < 10){
            mHour = "0" + calendar.get(Calendar.HOUR_OF_DAY);
        }else {
            mHour = "" + calendar.get(Calendar.HOUR_OF_DAY);
        }
        String mMin = "";
        if(calendar.get(Calendar.MINUTE) < 10){
            mMin = "0" + calendar.get(Calendar.MINUTE);
        }else {
            mMin = "" + calendar.get(Calendar.MINUTE);
        }
        return mHour + hourMinSecSeperator  + mMin;
    }
    
    public static String getHour(Long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String mHour = "";
        if(calendar.get(Calendar.HOUR_OF_DAY) < 10){
            mHour = "0" + calendar.get(Calendar.HOUR_OF_DAY);
        }else {
            mHour = "" + calendar.get(Calendar.HOUR_OF_DAY);
        }
        return mHour + "";
    }

    public static long getMillis(int month, int day, int year, int hour, int min, int sec){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, sec);
        return calendar.getTimeInMillis();
    }
    
}
