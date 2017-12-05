package com.rax.sharedsecrets;


import org.joda.time.Instant;

public class Salter {

    private static final long MILLISECONDS_PER_5_MINUTES = 300_000;

    public static String getTimeSalt(){
        Instant instant = Instant.now();
        long millTime = instant.getMillis();
        long roundedMillTime = millTime - (millTime % MILLISECONDS_PER_5_MINUTES);

        return String.valueOf(roundedMillTime);
    }
}
