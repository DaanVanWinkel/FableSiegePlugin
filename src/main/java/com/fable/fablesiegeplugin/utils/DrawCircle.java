package com.fable.fablesiegeplugin.utils;

import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DrawCircle {

    public static boolean drawCircle(double radius, Location loc, Color color, Player player, ParticleNativeAPI particleApi) {
        for (double t = 0; t <= 2*Math.PI*radius; t += 0.05) {
            double x = (radius * Math.cos(t)) + loc.getX();
            double z = (loc.getZ() + radius * Math.sin(t));
            Location particle = new Location(player.getWorld(), x, loc.getY() + 1, z);
            particleApi.LIST_1_8.REDSTONE
                    .packetColored(true, particle, color)
                    .sendTo(Bukkit.getOnlinePlayers());
        }
        return true;
    }

}
