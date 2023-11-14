package com.fable.fablesiegeplugin.listeners;

import com.fable.fablesiegeplugin.Main;
import com.fable.fablesiegeplugin.commands.MainCommand;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DeathListener implements Listener {

    private final MainCommand mainCommand = Main.getInstance().getMainCommand();

    @EventHandler
    public void onDeathRespawn(EntityDamageEvent e) {
        if (mainCommand.isRunning()) {
            return;
        }

        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getEntity();

        if (player.getHealth() - e.getFinalDamage() > 0) {
            return;
        }

        if (mainCommand.getRespawns() == 0) {
            // TODO: End game
        } else {
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            mainCommand.setRespawns(mainCommand.getRespawns() - 1);
        }

        e.setCancelled(true);
    }

}
