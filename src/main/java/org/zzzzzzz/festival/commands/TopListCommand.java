package org.zzzzzzz.festival.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.zzzzzzz.festival.Festival;
import org.zzzzzzz.festival.datas.DataHolder;
import org.zzzzzzz.festival.datas.PlayerWin;

public class TopListCommand {
    public static int logic(CommandContext<CommandSourceStack> ctx) {
        TextComponent.Builder message = Component.text();
        message.append(Component.text("-----通关榜-----"));
        for (PlayerWin win : DataHolder.get().Winners){
            message.append(Component.newline());
            message.append(Component.text(String.format("(%d) %s ,用时 %d:%d:%d",
                    DataHolder.get().Winners.indexOf(win)+1,
                    win.player.getName(),
                    win.getHour(),win.getMinute(),win.getSecond()
            )));
        }

        if (ctx.getSource().getExecutor() instanceof Player player){
            player.sendMessage(message);
        }else {
            Festival.getLog().fine(message.content());
        }

        return 0;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("toplist").executes(TopListCommand::logic);
    }
}
