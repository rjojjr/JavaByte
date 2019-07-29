package com.kirchnersolutions.database.exceptions;

import com.kirchnersolutions.database.Configuration.DevVars;
import com.kirchnersolutions.database.Configuration.SysVars;
import com.kirchnersolutions.database.dev.ExceptionLoggerService;
import com.kirchnersolutions.utilities.ByteTools;
import com.kirchnersolutions.utilities.CalenderConverter;
import org.springframework.beans.factory.annotation.Autowired;

import java.beans.ExceptionListener;
import java.io.File;


public class ExceptionListeningService implements ExceptionListener, Thread.UncaughtExceptionHandler {

    @Autowired
    private DevVars devVars;
    @Autowired
    private SysVars sysVars;
    @Autowired
    private ExceptionLoggerService exceptionLoggerService;

    @Override
    public void exceptionThrown(Exception e) {
        if(e instanceof DevelopmentException){
            if(devVars.isDevExceptions()){
                try{
                    exceptionLoggerService.writeDev(e.toString());
                }catch (Exception ex){
                    System.out.println("Failed to create dev exception log ");
                }
                System.out.println("Created dev exception log");
            }else{
                System.out.println(e.getClass().toString() + " exception thrown " + CalenderConverter.getMonthDayYearHourMinuteSecond(System.currentTimeMillis(), "/", ":"));
                System.out.println(e.getMessage());
            }
        }else{
            if(devVars.isDevExceptions()){
                System.out.println(e.getClass().toString() + " exception thrown " + CalenderConverter.getMonthDayYearHourMinuteSecond(System.currentTimeMillis(), "/", ":"));
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e){
        System.out.println(e.getClass().toString() + " uncaught exception thrown in thread " + t.getName() + " " + CalenderConverter.getMonthDayYearHourMinuteSecond(System.currentTimeMillis(), "/", ":"));
        System.out.println(e.getMessage());
        File log = new File(devVars.getUncaughtExceptionLogsDir(), "/" + CalenderConverter.getMonthDayYearHourMinuteSecond(System.currentTimeMillis(), "-", "_") + ".txt");
        String dump = e.getClass().toString() + " uncaught exception thrown in thread " + t.getName() + " " + CalenderConverter.getMonthDayYearHourMinuteSecond(System.currentTimeMillis(), "/", ":") + sysVars.getNewLineChar() + e.getMessage();
        try{
            log.createNewFile();
            ByteTools.writeBytesToFile(log, dump.getBytes("UTF-8"));
        }catch (Exception ex){
            System.out.println("Failed to log uncaught exception dump.");
        }

    }

}
