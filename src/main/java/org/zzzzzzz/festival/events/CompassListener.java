package org.zzzzzzz.festival.events;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CompassListener implements Listener {
    private final Map<Player,Location> TargetsMap = new HashMap<>();

    @EventHandler
    public void onTickUpdate(ServerTickEndEvent event){
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        for (Player player:players){
            double minDistance = Double.MAX_VALUE;
            Location minLocation = null;
            for (Player target:players){
                if (target == player) continue;
                if (target.getWorld() != player.getWorld()) continue;

                double distance = player.getLocation().distanceSquared(target.getLocation());
                if (distance < minDistance){
                    minDistance = distance;
                    minLocation = target.getLocation();
                }
            }

            if (minLocation == null) {
                TargetsMap.put(player,null);
                continue;
            }
            TargetsMap.put(player,minLocation);
            player.setCompassTarget(minLocation);
        }
    }

    @EventHandler
    public void onCompassClick(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.COMPASS) return;
        if (!event.getAction().isRightClick()) return;
        if (!TargetsMap.containsKey(player)) return;

        if (TargetsMap.get(player) == null){
            player.sendActionBar(Component.text("找不到玩家"));
        }
        else {
            double distance = TargetsMap.get(player).distance(player.getLocation());

            player.sendActionBar(Component.text(String.format("距离最近的玩家%f米",distance)));
        }
    }

    @EventHandler
    public void onPlayerEnterWorld(PlayerJoinEvent event){
        event.getPlayer().getInventory().addItem(
                ItemStack.of(Material.COMPASS)
        );
    }
}
