package com.fable.fablesiegeplugin.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

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

//        for (Location circleCenter : circleCenters) {
//
//        }
    }

}
