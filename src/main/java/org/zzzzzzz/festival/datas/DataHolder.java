package org.zzzzzzz.festival.datas;

import java.util.LinkedList;
import java.util.List;

public class DataHolder {
    private static final DataHolder instance = new DataHolder();
    public static DataHolder get(){
        return instance;
    }

    public List<PlayerWin> Winners = new LinkedList<>();
}
