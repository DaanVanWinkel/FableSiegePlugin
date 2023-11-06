package com.fable.fablesiegeplugin.utils;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import java.util.List;

public class WinCelebration {

    public static void winCelebration(List<Player> attacking, List<Player> defending, String objective, Location circleCenters) {
        for (Player target : attacking) {
            if (target != null) {
                target.sendTitle("§l§2" + objective + " completed", "", 10, 100, 20);
            }
        }
        for (Player target : defending) {
            if (target != null) {
                target.sendTitle("§l§c" + objective + " got captured", "", 10, 100, 20);
            }
        }

        Location loc = new Location(circleCenters.getWorld(), circleCenters.getX(), circleCenters.getY() + 10, circleCenters.getZ());

        Firework firework = (Firework) circleCenters.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();

        fireworkMeta.clearEffects();
        fireworkMeta.addEffect(FireworkEffect.builder()
                .flicker(true)
                .trail(true)
                .with(FireworkEffect.Type.BALL_LARGE)
                .withColor(Color.LIME)
                .withFade(Color.GREEN)
                .build());
        fireworkMeta.setPower(6); // 1-128, 1 power = 0.5s flight duration
        firework.setFireworkMeta(fireworkMeta);
        firework.detonate();
    }

}
