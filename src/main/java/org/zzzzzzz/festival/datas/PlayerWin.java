package org.zzzzzzz.festival.datas;

import org.bukkit.entity.Player;

public class PlayerWin{
    public double time;
    public Player player;

    public PlayerWin(Player player,double time){
        this.player = player;
        this.time = time;
    }

    public int getHour(){
        return (int) (time / 3600);
    }

    public int getMinute(){
        return  (int) ((time % 3600) / 60);
    }

    public int getSecond(){
        return (int) (time % 60);
    }
}
