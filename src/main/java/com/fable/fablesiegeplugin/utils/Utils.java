package com.fable.fablesiegeplugin.utils;

import com.fable.fablesiegeplugin.Main;
import com.fable.fablesiegeplugin.commands.MainCommand;
import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import de.leonhard.storage.shaded.jetbrains.annotations.NotNull;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Utils {
    private static final MainCommand mainCommand = Main.getInstance().getMainCommand();

    //////////////////////////////////
    // Draw a circle around a point //
    //////////////////////////////////
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

    /////////////////////////////////////////
    // Get keys and put it in a stringlist //
    /////////////////////////////////////////

    public static List<String> getListFromMapKeyset(Map<?, ?> map) {
        List<?> list = new ArrayList<>(map.keySet());
        ArrayList<String> listOfKeys = new ArrayList<>();
        for (Object key : list) {
            listOfKeys.add(key.toString());
        }
        return listOfKeys;
    }

    ///////////////////////////////////////////
    // Celebration if objective get captured //
    ///////////////////////////////////////////

    public static void pointCaptured(List<Player> attacking, List<Player> defending, String objective, Location circleCenters) {
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
                .withColor(Color.GREEN)
                .withFade(Color.LIME)
                .build());
        fireworkMeta.setPower(1); // 1-128, 1 power = 0.5s flight duration
        firework.setFireworkMeta(fireworkMeta);
        firework.setVelocity(new Vector(0, 0.1, 0)); // Shoot it straight up
    }

    ///////////////////////////////////////////////////
    // Get entities in a given radius around a point //
    ///////////////////////////////////////////////////

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

    ///////////////////////////////////////////
    // Title and fireworks for team that won //
    ///////////////////////////////////////////

    public static void teamWon(String teamWon, List<Player> winningTeam, List<Player> losingTeam) {
        for (Player target : winningTeam) {
            if (target != null) {
                target.sendTitle("§l§2Victory!", "§6" + teamWon + " won!", 10, 150, 20);


                Location loc = target.getLocation();

                Firework firework = (Firework) target.getWorld().spawnEntity(loc, EntityType.FIREWORK);
                FireworkMeta fireworkMeta = firework.getFireworkMeta();

                fireworkMeta.clearEffects();
                fireworkMeta.addEffect(FireworkEffect.builder()
                        .flicker(true)
                        .trail(true)
                        .with(FireworkEffect.Type.BALL_LARGE)
                        .withColor(Color.GREEN)
                        .withFade(Color.LIME)
                        .build());
                fireworkMeta.setPower(1); // 1-128, 1 power = 0.5s flight duration
                firework.setFireworkMeta(fireworkMeta);
                firework.setVelocity(new Vector(0, 0.1, 0)); // Shoot it straight up
            }
        }

        for (Player target : losingTeam) {
            if (target != null) {
                target.sendTitle("§l§cDefeat!", "§6" + teamWon + " won!", 10, 150, 20);
            }
        }
    }

    public static void respawnPlayer(Player player, String team, int respawnTime) {
        player.setGameMode(GameMode.SPECTATOR);
        BukkitRunnable runnable = new BukkitRunnable() {
            int counter = respawnTime;

            @Override
            public void run() {
                if (counter == 0) {
                    player.teleport(mainCommand.getRespawnPoint(team));
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
    }

    public static boolean checkIfPlayer(CommandSender player) {
        if (player instanceof Player) {
            return true;
        } else {
            player.sendMessage("§cYou must be a player to use this command.");
            return false;
        }
    }
}
