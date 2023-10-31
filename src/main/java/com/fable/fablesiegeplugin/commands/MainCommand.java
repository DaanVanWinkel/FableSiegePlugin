package com.fable.fablesiegeplugin.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.fable.fablesiegeplugin.Main;
import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
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
    @Description("Load a config")
    public void load(CommandSender sender, String[] args) {
        sender.sendMessage("Loading config...");
    }

    @Subcommand("testCircle")
    public void testCircle(CommandSender sender, String[] args) {

        sender.sendMessage("Testing circle...");

        if (sender instanceof Player) {
            Player player = (Player) sender;
            Location loc = player.getLocation();
            loc.setY(loc.getY() - 1);

            BukkitRunnable runnable = new BukkitRunnable() {
                int timer = 0;
                @Override
                public void run() {
                    double radius = 2;
                    for (double t = 0; t <= 2*Math.PI*radius; t += 0.05) {
                        double x = (radius * Math.cos(t)) + loc.getX();
                        double z = (loc.getZ() + radius * Math.sin(t));
                        Location particle = new Location(player.getWorld(), x, loc.getY() + 1, z);
                        Color color = Color.fromRGB(0, 255, 0);
                        particleApi.LIST_1_8.REDSTONE
                                .packetColored(true, particle, color)
                                .sendTo(Bukkit.getOnlinePlayers());

                    }
                    timer++;
                    if (timer == 2 * 5) { // first number and period in runnable.runTaskTimer = 20 always
                        cancel();
                    }
                }
            };
            runnable.runTaskTimer(Main.getInstance(), 0L, 10L);
        }
    }
}
