package org.zzzzzzz.festival.tools;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class PlayerLogger {
    private static final boolean enable = false;
    private static final PlayerLogger logger = new PlayerLogger();
    public static PlayerLogger getLogger(){
        return logger;
    }

    public void Log(Player player, String message){
        if (enable)
            player.sendMessage(Component.text(message));
    }
}
