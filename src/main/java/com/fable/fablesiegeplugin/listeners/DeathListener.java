package com.fable.fablesiegeplugin.listeners;

import com.fable.fablesiegeplugin.Main;
import com.fable.fablesiegeplugin.commands.MainCommand;
import com.fable.fablesiegeplugin.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathListener implements Listener {

    private final MainCommand mainCommand = Main.getInstance().getMainCommand();

    @EventHandler
    public void onDeathRespawn(EntityDamageEvent e) {
        if (!mainCommand.isRunning()) {
            return;
        }

        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getEntity();

        if (player.getHealth() - e.getFinalDamage() > 0) {
            return;
        }

        // TODO: Set gamemode to spectator for 3 seconds and then respawn, show countdown with title

        if (mainCommand.getRespawns() == 0) {
            mainCommand.setDefendersWon(true);
        } else {
            player.setGameMode(GameMode.SPECTATOR);
            BukkitRunnable runnable = new BukkitRunnable() {
                int counter = 3;
                @Override
                public void run() {
                    if (counter == 0) {
                        player.setGameMode(GameMode.SURVIVAL);
                        cancel();
                    } else {
                        player.sendTitle("§l§cRespawning in " + counter, "", 0, 15, 5);
                        counter--;
                    }
                }
            };
            runnable.runTaskTimer(Main.getInstance(), 0L, 20);

            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            mainCommand.setRespawns(mainCommand.getRespawns() - 1);
        }

        e.setCancelled(true);
    }

}
