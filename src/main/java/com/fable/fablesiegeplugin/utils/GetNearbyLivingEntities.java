package com.fable.fablesiegeplugin.utils;

import de.leonhard.storage.shaded.jetbrains.annotations.NotNull;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class GetNearbyLivingEntities {
    public static List<LivingEntity> getNearbyLivingEntities(Player player, Location loc, double radius) {
        return getNearbyLivingEntities(player, loc, t -> true, radius, radius, radius);
    }

    public static List<LivingEntity> getNearbyLivingEntities(Player player, @NotNull Location loc, Predicate<LivingEntity> predicate, double radius) {
        return getNearbyLivingEntities(player, loc, predicate, radius, radius, radius);
    }

    public static List<LivingEntity> getNearbyLivingEntities(Player player, @NotNull Location loc, Predicate<LivingEntity> predicate, double rx, double ry, double rz) {
        Object ArrayList;
        return loc.getWorld()
                .getNearbyEntities(loc, rx, ry, rz).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> !(entity instanceof ArmorStand))
//                .filter(entity -> !entity.equals(player)) I need player to be included
                .map(LivingEntity.class::cast)
                .filter(predicate)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
