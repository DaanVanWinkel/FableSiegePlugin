package com.fable.fablesiegeplugin.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.fable.fablesiegeplugin.Main;
import com.fable.fablesiegeplugin.utils.GetNearbyLivingEntities;
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

@CommandAlias("FableSiege")
@Description("Main command for FableSiege")
public class MainCommand extends BaseCommand {

    final ParticleNativeAPI particleApi = Main.getInstance().getParticleAPI();

    @HelpCommand
    @Private
    public void help(CommandSender sender, CommandHelp help) { help.showHelp(); }

    @Subcommand("load")
    @CommandPermission("fablesiege.load")
    @Description("Load a preset")
    // TODO: Make it load a preset from the config
    // TODO: Make it work
    public void load(CommandSender sender, String[] args) {
        sender.sendMessage("Loading preset...");
    }

    @Subcommand("testCircle")
    public void testCircle(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Location loc = player.getLocation();
            loc.setY(loc.getY() - 1);

            BukkitRunnable runnable = new BukkitRunnable() {
//                int timer = 0;
                int timer = 2 * 30; // first number and period in runnable.runTaskTimer = 20 always
                String message = "";
                @Override
                public void run() {
                    // TODO: Get the radius from the config
                    double radius = 5;
                    Color color = Color.fromRGB(0, 255, 0);

                    if (timer > 2 * 15) {
                        message = "§2 " + timer / 2 + " seconds left";
                    }
                    else if (timer > 2 * 5) {
                        message = "§6 " + timer / 2 + " seconds left";
                        color = Color.fromRGB(255, 165, 0);
                    }
                    else {
                        message = "§4 " + timer / 2 + " seconds left";
                        color = Color.fromRGB(255, 0, 0);
                    }

                    for (double t = 0; t <= 2*Math.PI*radius; t += 0.05) {
                        double x = (radius * Math.cos(t)) + loc.getX();
                        double z = (loc.getZ() + radius * Math.sin(t));
                        Location particle = new Location(player.getWorld(), x, loc.getY() + 1, z);
                        particleApi.LIST_1_8.REDSTONE
                                .packetColored(true, particle, color)
                                .sendTo(Bukkit.getOnlinePlayers());

                    }
                    for (LivingEntity entity : GetNearbyLivingEntities.getNearbyLivingEntities(player, loc, radius)) {
                        if (entity instanceof Player) {
                            Player target = (Player) entity;

                            target.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));

                            timer--;
                            if (timer == 0) {
                                cancel();
                            }
                        }
                    }
                }
            };
            runnable.runTaskTimer(Main.getInstance(), 0L, 10L);
        }
    }
}
