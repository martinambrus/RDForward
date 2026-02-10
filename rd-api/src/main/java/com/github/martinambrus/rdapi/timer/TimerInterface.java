package com.github.martinambrus.rdapi.timer;

public interface TimerInterface {
    float getTicksPerSecond();
    void setTicksPerSecond( float tps );
    void advanceTime();
    void addTimerListener( TimerListener tl );
}
