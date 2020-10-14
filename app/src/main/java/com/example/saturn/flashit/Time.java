package com.example.saturn.flashit;

/**
 * Created by ArpitA on 4/13/2017.
 */

public class Time {

    private int freq;

    public long getSleepTime() {
        return (long) Math.floor((11 - freq) * 50);
    }

    public void setSleepTime(int freq) {
        this.freq = freq;
    }

}
