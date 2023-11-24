package com.fable.fablesiegeplugin.listeners;

import com.fable.fablesiegeplugin.Main;
import com.fable.fablesiegeplugin.commands.MainCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntity implements Listener {

    private final MainCommand mainCommand = Main.getInstance().getMainCommand();

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!mainCommand.isRunning()) {
            return;
        }

        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player attacker = (Player) event.getDamager();
        Player damaged = (Player) event.getEntity();

        if (mainCommand.getAttacking().contains(attacker) && mainCommand.getAttacking().contains(damaged)) {
            event.setCancelled(true);
        } else if (mainCommand.getDefending().contains(attacker) && mainCommand.getDefending().contains(damaged)) {
            event.setCancelled(true);
        }
    }

}
