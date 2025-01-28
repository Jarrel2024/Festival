package org.zzzzzzz.festival.events;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.zzzzzzz.festival.Festival;
import org.zzzzzzz.festival.datas.DataHolder;
import org.zzzzzzz.festival.datas.PlayerWin;

import java.util.*;

public class EnderDragonListener implements Listener {
    private final List<PlayerWin> winners = new LinkedList<>();
    private final Random rng = new Random();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnderDragonDied(EntityDeathEvent event){
        if (event.getEntity() instanceof EnderDragon enderDragon){
            World world = enderDragon.getWorld();

            Player killer = enderDragon.getKiller();
            if (killer != null) {
                PlayerWin win =new PlayerWin(killer, killer.getPlayerTime());
                winners.add(win);
                makePlayerWin(win);
            }

            Festival.getLog().warning("龙死了");

            showMessage("末影龙还有30秒重生!");

            new BukkitRunnable(){
                @Override
                public void run() {
                    respawnDragon(world);
                }
            }.runTaskLater(Festival.get(),20*30L);
        }
    }

    private void respawnDragon(World world){
        if (world.getEnderDragonBattle() == null) Festival.getLog().warning("???龙无");

        DragonBattle battle = world.getEnderDragonBattle();

        Location loc = battle.getEndPortalLocation();
        if (loc == null) Festival.getLog().warning("门无???");
        List<EnderCrystal> crystals = new LinkedList<>();

        // 定义偏移量
        double[][] offsets = {
                {0, 1, 2.5},
                {0, 1, -2.5},
                {-2.5, 1, 0},
                {2.5, 1, 0}
        };

        // 使用循环生成并添加水晶
        for (double[] offset : offsets) {
            Location crystalLocation = loc.clone().add(offset[0], offset[1], offset[2]);
            EnderCrystal crystal = (EnderCrystal) world.spawnEntity(crystalLocation, EntityType.END_CRYSTAL);
            crystal.setInvulnerable(true);
            crystal.addScoreboardTag("in");
            crystals.add(crystal);
        }

        battle.resetCrystals();
        battle.initiateRespawn(crystals);
        battle.generateEndPortal(false);

        showMessage("末影龙重生！");
    }

    @EventHandler
    public void onPlayerAttackCrystal(PrePlayerAttackEntityEvent event){
        if (event.getAttacked() instanceof EnderCrystal crystal){
            if (crystal.getScoreboardTags().contains("in")){
                event.setCancelled(true);
            }
        }
    }


    private void makePlayerWin(PlayerWin win){
        Player player = win.player;
        player.setGameMode(GameMode.SPECTATOR);
        DataHolder.get().Winners.add(win);

        player.showTitle(Title.title(
                Component.text("恭喜通关！！"),
                Component.text(String.format("用时%d:%d:%d",win.getHour(),win.getMinute(),win.getSecond()))
        ));

        for (int i = 0; i < 5; i++) {
            Location loc = player.getLocation();
            loc.add(rng.nextDouble()-0.5,rng.nextDouble()+1,rng.nextDouble()-0.5);
            Firework firework = (Firework) player.getWorld().spawnEntity(loc,EntityType.FIREWORK_ROCKET);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.setPower(50);
            meta.addEffect(FireworkEffect.builder().withColor(Color.RED).withFade(Color.YELLOW).build());
        }

        player.setPlayerTime(0,true);

        showMessage(String.format("玩家%s用时%d:%d:%d通过了MC,位居第%d名",
                player.getName(),
                win.getHour(), win.getMinute(),win.getSecond(),
                winners.indexOf(win)+1)
        );
    }

    private void showMessage(String message){
        for (Player online : Bukkit.getOnlinePlayers()){
            online.sendMessage(Component.text(message));
        }
    }
}

