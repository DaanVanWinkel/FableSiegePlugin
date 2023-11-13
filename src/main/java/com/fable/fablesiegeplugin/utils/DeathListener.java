package com.fable.fablesiegeplugin.utils;

import com.fable.fablesiegeplugin.commands.MainCommand;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DeathListener implements Listener {

    @EventHandler
    public void onDeathRespawn(EntityDamageEvent e) {
        if (!MainCommand.isRunning()) {
            return;
        }

        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getEntity();

        if (player.getHealth() - e.getFinalDamage() > 0) {
            return;
        }

        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());


        e.setCancelled(true);
    }

}
