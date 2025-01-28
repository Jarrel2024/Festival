package org.zzzzzzz.festival;

import com.sk89q.worldedit.regions.Region;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.zzzzzzz.festival.commands.InitGameCommand;
import org.zzzzzzz.festival.commands.TopListCommand;
import org.zzzzzzz.festival.events.CompassListener;
import org.zzzzzzz.festival.events.DeathListener;
import org.zzzzzzz.festival.events.EnderDragonListener;
import org.zzzzzzz.festival.events.GoalEvent;

import java.util.logging.Logger;

public final class Festival extends JavaPlugin {
    private static Logger logger;
    public static Logger getLog(){
        return logger;
    }

    private static Festival festival;
    public static Festival get(){
        return festival;
    }

    private final Listener[] listeners={
            new CompassListener(),
            new DeathListener(),
            new EnderDragonListener(),
            new GoalEvent(),
    };

    @Override
    public void onEnable() {
        registerEvents();

        Festival.festival = this;

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(TopListCommand.createCommand().build());
            commands.registrar().register(InitGameCommand.createCommand().build());
        });

        Logger logger = getLogger();
        Festival.logger = logger;

        logger.fine("Festival is already loaded");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerEvents(){
        PluginManager manager = getServer().getPluginManager();
        for(Listener l : listeners){
            manager.registerEvents(l,this);
        }
    }
}
