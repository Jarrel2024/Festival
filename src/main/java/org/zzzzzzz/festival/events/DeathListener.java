package org.zzzzzzz.festival.events;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import io.papermc.paper.event.entity.EntityMoveEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.zzzzzzz.festival.Festival;
import org.zzzzzzz.festival.tools.PlayerLogger;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class DeathListener implements Listener {
    private final Set<PlayerDeath> diedPlayers = new HashSet<>();
    private final static int respawnTime = 90 * 20;

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getPlayer();

        Location diedLoc = player.getLocation();

        player.spigot().respawn();

        player.teleport(diedLoc);

        setDeath(player);

        diedPlayers.add(new PlayerDeath(player,respawnTime,diedLoc));

        player.sendMessage(String.format("您已死亡，剩余复活时间:%d秒",respawnTime/20));
    }

    private static void setDeath(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setNoDamageTicks(respawnTime);

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, respawnTime,1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, respawnTime,1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,respawnTime,6));
    }

    private int tickCount = 0;
    @EventHandler
    public void onTick(ServerTickStartEvent event){
        tickCount+=1;
        if (tickCount < Bukkit.getServerTickManager().getTickRate()) return;
        tickCount=0;

        for(PlayerDeath playerDeath : diedPlayers){
            playerDeath.setRemainRespawnTicks(playerDeath.getRemainRespawnTicks()-20);
            Player player = playerDeath.getPlayer();
            if (player == null) continue;

            PlayerLogger.getLogger().Log(player,"Remain respawn time:"+playerDeath.getRemainRespawnTicks());

            if (playerDeath.getRemainRespawnTicks() <= 0){
                diedPlayers.remove(playerDeath);
                respawnPlayer(player);
                continue;
            }

            setDeath(player);

            int time = playerDeath.getRemainRespawnTicks() / 20;

            player.sendActionBar(Component.text(String.format("剩余复活时间:%d秒",time)));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void avoidPlayerInteract(PlayerInteractEvent event){
        if (notDied(event.getPlayer())) return;
        event.setCancelled(true);
        PlayerLogger.getLogger().Log(event.getPlayer(),"Avoid a Action:"+event.getAction());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void avoidPlayerAttack(PrePlayerAttackEntityEvent event){
        if (notDied(event.getPlayer())) return;
        event.setCancelled(true);
        PlayerLogger.getLogger().Log(event.getPlayer(), "Avoid player attack");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void avoidPlayerPickupItems(PlayerPickItemEvent event){
        if (notDied(event.getPlayer())) return;
        PlayerLogger.getLogger().Log(event.getPlayer(),"Avoid pick up items");
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void avoidPlayerPickItem(PlayerAttemptPickupItemEvent event){
        if (notDied(event.getPlayer())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void avoidPlayerPickExp(PlayerPickupExperienceEvent event){
        if (notDied(event.getPlayer())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void avoidEntityTargetDiedPlayer(EntityTargetEvent event){
        if (event.getTarget() instanceof Player player){
            if (notDied(player)) return;
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void avoidEntityAttackDiedPlayer(EntityMoveEvent event){
        if (event.getEntity() instanceof Mob mob){
            if (mob.getTarget() instanceof Player player){
                if (notDied(player)) return;
                mob.setTarget(null);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void avoidExpBallFollowPlayer(EntityMoveEvent event){
        if (event.getEntity() instanceof ExperienceOrb orb){
            orb.getTrackedBy().removeIf(x->diedPlayers.stream().anyMatch(y->y.getPlayer()==x));
        }
    }


    @EventHandler
    public void avoidOutOfDistance(PlayerMoveEvent event){
        Player player = event.getPlayer();

        if (notDied(player)) return;
        Optional<PlayerDeath> op = diedPlayers.stream().filter(x->x.getPlayer()==player).findFirst();
        if (op.isEmpty()) return;

        PlayerDeath playerDeath = op.get();

        final double maxDistance = 30;
        if (player.getLocation().distanceSquared(playerDeath.getDiedLocation())>maxDistance*maxDistance){
            PlayerLogger.getLogger().Log(player,"out of distance");
            player.sendMessage(Component.text(String.format("你不能超出死亡点距离%d米",(int)maxDistance)));
            player.teleport(playerDeath.getDiedLocation());
            player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT,0.7f,1f);
            player.getWorld().spawnParticle(Particle.PORTAL,player.getLocation(),350);
        }
    }

    @EventHandler
    public void avoidPortal(PlayerPortalEvent event){
        if (notDied(event.getPlayer())) return;
        event.setCancelled(true);
    }

    private boolean notDied(Player player){
        return diedPlayers.stream().noneMatch(x-> x.getPlayerName().equals(player.getName()));
    }

    private void respawnPlayer(Player player){
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setGameMode(GameMode.SURVIVAL);

        player.clearActivePotionEffects();
        player.setNoDamageTicks(0);

        if (player.getRespawnLocation() == null){
            player.teleport(Bukkit.getWorlds().getFirst().getSpawnLocation());
        }else{
            player.teleport(player.getRespawnLocation());
        }

        new BukkitRunnable(){
            @Override
            public void run() {
                player.showTitle(Title.title(
                        Component.text("复活了！！！"),
                        Component.empty()
                ));

                player.getInventory().addItem(
                        ItemStack.of(Material.COMPASS)
                );
            }
        }.runTaskLater(Festival.get(),20);
    }
}

class PlayerDeath{
    private String player;
    private int remainRespawnTicks;
    private Location diedLocation;

    public PlayerDeath(Player player,int time,Location diedLocation){
        this.player = player.getName();
        this.remainRespawnTicks = time;
        this.diedLocation = diedLocation;
    }

    public String getPlayerName() {
        return player;
    }

    public Player getPlayer(){
        return Bukkit.getPlayer(player);
    }

    public void setPlayer(Player player) {
        this.player = player.getName();
    }

    public int getRemainRespawnTicks() {
        return remainRespawnTicks;
    }

    public void setRemainRespawnTicks(int remainRespawnTicks) {
        this.remainRespawnTicks = remainRespawnTicks;
    }

    public Location getDiedLocation() {
        return diedLocation;
    }

    public void setDiedLocation(Location diedLocation) {
        this.diedLocation = diedLocation;
    }
}
