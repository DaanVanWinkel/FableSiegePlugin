package com.fable.fablesiegeplugin.listeners;

import com.fable.fablesiegeplugin.Main;
import com.fable.fablesiegeplugin.commands.MainCommand;
import com.fable.fablesiegeplugin.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DeathListener implements Listener {

    private final MainCommand mainCommand = Main.getInstance().getMainCommand();

    @EventHandler
    public void onDeathRespawn(EntityDamageEvent e) {
        int alive = 0;

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

        if (mainCommand.getAttacking().contains(player)) {
            if (mainCommand.getRespawns() == 0) {
                for (Player p : mainCommand.getAttacking()) {
                    if (p.getGameMode() != GameMode.SPECTATOR) {
                        alive++;
                    }
                }

                if (alive == 0) {
                    mainCommand.setDefendersWon(true);
                }
            } else {
                Utils.respawnPlayer(player, "Attacking", 3);
                mainCommand.setRespawns(mainCommand.getRespawns() - 1);
            }
        } else {
            Utils.respawnPlayer(player, "Defending", 3);
        }

        e.setCancelled(true);
    }
}
