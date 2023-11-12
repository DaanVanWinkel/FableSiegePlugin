package com.fable.fablesiegeplugin.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.fable.fablesiegeplugin.Main;
import com.fable.fablesiegeplugin.config.DataManager;
import com.fable.fablesiegeplugin.utils.DrawCircle;
import com.fable.fablesiegeplugin.utils.GetListFromMapKeyset;
import com.fable.fablesiegeplugin.utils.GetNearbyLivingEntities;
import com.fable.fablesiegeplugin.utils.WinCelebration;
import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Color;

import java.util.*;

@CommandAlias("FableSiege")
@Description("Main command for FableSiege")
public class MainCommand extends BaseCommand {

    final ParticleNativeAPI particleApi = Main.getInstance().getParticleAPI();
    final DataManager dataManager = Main.getInstance().getDataManager();
    int counter = 0;
    String map = "";

    @HelpCommand
    @Private
    public void help(CommandSender sender, CommandHelp help) { help.showHelp(); }

    @Subcommand("create")
    @CommandPermission("fablesiege.edit")
    @Description("Creates a siege preset")
    @Syntax("<siegeName>")
    public void create(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            List<String> sieges = GetListFromMapKeyset.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges"));

            if (args.length != 1) {
                player.sendMessage("§cInvalid syntax (separate objectives with a comma no space). Please use /FableSiege create <siegeName> <objectives>");
                return;
            }

            if (sieges.contains(args[0])) {
                player.sendMessage("§cSiege already exists.");
                return;
            }

            dataManager.getConfig().set("Sieges." + args[0], "Objectives");
        }
    }

    @Subcommand("addObjective")
    @CommandPermission("fablesiege.edit")
    @Description("Adds an objective to a given siege")
    @CommandCompletion("@maps ")
    @Syntax("<siege> <objective> <captureTime> <objectiveNumber>")
    public void addObjective(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            List<String> sieges = GetListFromMapKeyset.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges"));
            List<String> objectives = GetListFromMapKeyset.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + args[0] + ".Objectives"));

            if (args.length != 4) {
                player.sendMessage("§cInvalid syntax. Please use /FableSiege addPoint <siege> <objective> <captureTime> <objectiveNumber>");
                return;
            }

            if (!sieges.contains(args[0])) {
                player.sendMessage("§cInvalid siege.");
                return;
            }

            if (objectives.contains(args[1])) {
                player.sendMessage("§cObjective already exists.");
                return;
            }

            if (args[2].matches("[a-zA-Z]+")) {
                player.sendMessage("§cInvalid capture time. Please use an integer.");
                return;
            }

            if (args[3].matches("[a-zA-Z]+")) {
                player.sendMessage("§cInvalid objective number. Please use an integer.");
                return;
            }

            dataManager.getConfig().set("Sieges." + args[0] + ".Objectives." + args[1] + ".CaptureTimer", args[2]);
            dataManager.getConfig().set("Sieges." + args[0] + ".Objectives." + args[1] + ".ObjectiveNr", args[3]);
        }
    }

    @Subcommand("addPoint")
    @CommandPermission("fablesiege.edit")
    @Description("Adds a point to a given objective in a given siege")
    @CommandCompletion("@maps ")
    @Syntax("<siege> <objective> <radius>")
    public void addPoint(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            List<String> sieges = GetListFromMapKeyset.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges"));
            List<String> objectives = GetListFromMapKeyset.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + args[0] + ".Objectives"));

            if (args.length != 3) {
                player.sendMessage("§cInvalid syntax. Please use /FableSiege addPoint <siege> <objective> <radius>");
                return;
            }

            if (!sieges.contains(args[0]) || !objectives.contains(args[1])) {
                player.sendMessage("§cInvalid siege or objective. Please use /FableSiege addPoint <siege> <objective> <radius>");
                return;
            }

            if (args[2].matches("[a-zA-Z]+")) {
                player.sendMessage("§cInvalid radius. Please use an integer.");
                return;
            }

            List<String> capturePoints = GetListFromMapKeyset.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + args[0] + ".Objectives." + args[1] + ".CapturePoints"));
            int radius = Integer.parseInt(args[2]);


            dataManager.getConfig().set("Sieges." + args[0] + ".Objectives." + args[1] + ".CapturePoints.Point" + (capturePoints.size() + 1) + ".Center.X", player.getLocation().getX());
            dataManager.getConfig().set("Sieges." + args[0] + ".Objectives." + args[1] + ".CapturePoints.Point" + (capturePoints.size() + 1) + ".Center.Y", player.getLocation().getY() - 1);
            dataManager.getConfig().set("Sieges." + args[0] + ".Objectives." + args[1] + ".CapturePoints.Point" + (capturePoints.size() + 1) + ".Center.Z", player.getLocation().getZ());
            dataManager.getConfig().set("Sieges." + args[0] + ".Objectives." + args[1] + ".CapturePoints.Point" + (capturePoints.size() + 1) + ".Radius", radius);
        }
    }

    // TODO: Team management
    // TODO: Editing sieges

    @Subcommand("load")
    @CommandPermission("fablesiege.load")
    @Description("Load a preset")
    @CommandCompletion("@maps @teams @teams ")
    public void load(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            map = args[0];

            // TODO: Change how we get what player what team. Ask Venturo how I wanna do
            String[] team1 = dataManager.getConfig().getStringList("Teams.Team1.players").toArray(new String[0]);
            String[] team2 = dataManager.getConfig().getStringList("Teams.Team2.players").toArray(new String[0]);

            List<Player> attacking = new ArrayList<>();
            List<Player> defending = new ArrayList<>();

            for (String name : team1) {
                attacking.add(Bukkit.getPlayer(name));
            }

            for (String name : team2) {
                defending.add(Bukkit.getPlayer(name));
            }

            startObjectives(player, map, attacking, defending);
        }
    }

    /////////////
    // Methods //
    /////////////

    public void startObjectives(Player player, String map, List<Player> attacking, List<Player> defending) {
        List<String> objectives = GetListFromMapKeyset.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + map + ".Objectives"));
        List<String> objectiveNames = new ArrayList<>();

        for (int i = 0; i + 1 <= objectives.size(); i++) {
            for (String objective : objectives) {
                if (dataManager.getConfig().getInt("Sieges." + map + ".Objectives." + objective + ".ObjectiveNr") == i + 1) {
                    objectiveNames.add(objective);
                }
            }
        }

        if (counter > objectiveNames.size() - 1) {
            counter = 0;
            return;
        }

        String objective = objectiveNames.get(counter);
        double captureTime = dataManager.getConfig().getInt("Sieges." + map + ".Objectives." + objective + ".CaptureTimer");
        List<String> capturePoints = GetListFromMapKeyset.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + map + ".Objectives." + objective + ".CapturePoints"));

        BukkitRunnable runnable = new BukkitRunnable() {
            double timer = 2 * captureTime; // first number and period in runnable.runTaskTimer = 20 always
            int timeRunnning = 0;
            String message = "";

            @Override
            public void run() {
                Color color = Color.fromRGB(0, 255, 0);

                double timerInPercent = (100 - Math.round((1 - (timer / 2) / captureTime) * 100));

                if (timerInPercent > 50) { // TODO: respawns
                    message = "§l§c" + objective + " §r- §l§2" + timerInPercent + "% §r- §l§cRespawns: ";
                } else if (timerInPercent > 20) {
                    message = "§l§c" + objective + " §r- §l§6 " + timerInPercent + "% §r- §l§cRespawns: ";
                    color = Color.fromRGB(255, 165, 0);
                } else {
                    message = "§l§c" + objective + " §r- §l§4 " + timerInPercent + "% §r- §l§cRespawns: ";
                    color = Color.fromRGB(255, 0, 0);
                }


                // For each capture point, draw a circle around it and count the amount of players per team in the circle
                for (String point : capturePoints) {
                    Location loc = new Location(player.getWorld(), dataManager.getConfig().getDouble("Sieges." + map + ".Objectives." + objective + ".CapturePoints." + point + ".Center.X"),
                            dataManager.getConfig().getDouble("Sieges." + map + ".Objectives." + objective + ".CapturePoints." + point + ".Center.Y"),
                            dataManager.getConfig().getDouble("Sieges." + map + ".Objectives." + objective + ".CapturePoints." + point + ".Center.Z"));
                    int amountAttacking = 0;
                    int amountDefending = 0;
                    Map<String, Player> targets = new Hashtable<>();
                    double radius = dataManager.getConfig().getDouble("Sieges." + map + ".Objectives." + objective + ".CapturePoints." + point + ".Radius");

                    DrawCircle.drawCircle(radius, loc, color, player, particleApi);

                    for (LivingEntity entity : GetNearbyLivingEntities.getNearbyLivingEntities(player, loc, radius)) {
                        if (entity instanceof Player) {
                            Player target = (Player) entity;

                            if (attacking.contains(target)) {
                                targets.put("attacking", target);
                            } else if (defending.contains(target)) {
                                targets.put("defending", target);
                            }
                        }
                    }

                    for (Map.Entry<String, Player> key : targets.entrySet()) {
                        if (key.getKey().equals("attacking")) {
                            amountAttacking++;
                        } else if (key.getKey().equals("defending")) {
                            amountDefending++;
                        }
                    }

                    if (amountAttacking > amountDefending) {
                        timer--;
                        if (timer == 0) {
                            WinCelebration.winCelebration(attacking, defending, objective, loc);
                            counter++;
                            startObjectives(player, map, attacking, defending);
                            cancel();
                        }
                    } else if (amountAttacking < amountDefending) {
                        if (timer < 2 * 30) {
                            if ((timeRunnning / 2) % 10 == 0) {
                                timer++;
                            }
                        }
                    } else if (amountAttacking == amountDefending && amountDefending != 0) {
                        message = "§eContested!";
                    }
                }

                for (Player target : attacking) {
                    if (target != null) {
                        target.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                    }
                }
                for (Player target : defending) {
                    if (target != null) {
                        target.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                    }
                }

                timeRunnning += 2;
            }
        };
        runnable.runTaskTimer(Main.getInstance(), 0L, 10L);
    }
}


//    @Subcommand("test")
//    @




