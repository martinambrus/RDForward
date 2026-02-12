package com.github.martinambrus.rdforward.android.game;

public interface LevelListener {
    void tileChanged(int x, int y, int z);
    void lightColumnChanged(int x, int z, int y0, int y1);
    void allChanged();
}
