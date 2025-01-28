package org.zzzzzzz.festival.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;

public class InitGameCommand {
    public static int logic(CommandContext<CommandSourceStack> ctx){
        for(World world : Bukkit.getWorlds()){
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN,true);
        }

        return 0;
    }


    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("init-game").executes(InitGameCommand::logic);
    }
}
