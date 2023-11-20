package com.fable.fablesiegeplugin.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.fable.fablesiegeplugin.Main;
import com.fable.fablesiegeplugin.config.DataManager;
import com.fable.fablesiegeplugin.utils.*;
import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Color;

import java.util.*;

@CommandAlias("FableSiege|fs|siege")
@Description("Main command for FableSiege")
public class MainCommand extends BaseCommand {

    @Getter
    boolean running = false;
    @Getter
    List<Player> attacking = new ArrayList<>();
    @Getter
    List<Player> defending = new ArrayList<>();
    @Getter @Setter
    int respawns = 50;
    @Getter @Setter
    boolean defendersWon = false;
    final ParticleNativeAPI particleApi = Main.getInstance().getParticleAPI();
    final DataManager dataManager = Main.getInstance().getDataManager();
    int counter = 0;
    String map = "";
    boolean forceStop = false;
    String attackingTeamName = "";
    String defendingTeamName = "";
    Player player;

    @HelpCommand
    @Private
    public void help(CommandSender sender, CommandHelp help) { help.showHelp(); }

    //////////////////////////
    // Management of Sieges //
    //////////////////////////

    @Subcommand("siege")
    @CommandPermission("fablesiege.siegemanagement")
    @CommandCompletion("create|remove|list")
    @Description("Creates a siege")
    @Syntax("siege <create | remove | list> siegeName")
    public void siege(CommandSender sender, String[] args) {
        if (!Utils.checkIfPlayer(sender)) { return; }

        player = (Player) sender;
        List<String> sieges = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges"));

        switch (args[0].toLowerCase()) {
            case "create": // args = {CREATE, siegeName}
                if (args.length != 2) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege siege create <siegeName>");
                    return;
                }

                if (sieges.contains(args[1])) {
                    player.sendMessage("§cSiege already exists.");
                    return;
                }

                dataManager.getConfig().set("Sieges." + args[1] + ".Respawns", 50);
                player.sendMessage("§aPreset " + args[1] + " created.");
                break;

            case "remove": // args = {REMOVE, siegeName}
                if (args.length != 2) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege siege remove <siegeName>");
                    return;
                }

                if (!sieges.contains(args[1])) {
                    player.sendMessage("§cInvalid siege.");
                    return;
                }

                dataManager.getConfig().remove("Sieges." + args[1]);
                player.sendMessage("§aRemoved " + args[1] + ".");
                break;

            case "list": // args = {LIST}
                if (args.length != 1) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege siege list");
                    return;
                }

                if (sieges.isEmpty()) {
                    player.sendMessage("§cNo sieges found.");
                    return;
                }

                player.sendMessage("§aSieges:");
                for (String siege : sieges) {
                    player.sendMessage("§a- " + siege);
                }
                break;
        }

    }

    @Subcommand("objective")
    @CommandPermission("fablesiege.siegemanagement")
    @Description("Creates an objective for a siege")
    @CommandCompletion("create|remove|list @maps")
    @Syntax("objective <create | remove | list> siegeName objectiveName captureTime objectiveNumber")
    public void objective(CommandSender sender, String[] args) {
        if (!Utils.checkIfPlayer(sender)) { return; }

        player = (Player) sender;
        List<String> sieges = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges"));
        List<String> objectives;

        switch (args[0].toLowerCase()) {
            case "create": // args = {CREATE, siegeName, objectiveName, captureTime, objectiveNumber}
                if (args.length != 5) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege objective create <siegeName> <objectiveName> <captureTime> <objectiveNumber>");
                    return;
                }

                if (!sieges.contains(args[1])) {
                    player.sendMessage("§cInvalid siege.");
                    return;
                }

                objectives = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + args[1] + ".Objectives"));

                if (objectives.contains(args[2])) {
                    player.sendMessage("§cObjective already exists.");
                    return;
                }

                if (args[3].matches("[a-zA-Z]+")) {
                    player.sendMessage("§cInvalid capture time. Please use an integer.");
                    return;
                }

                if (args[4].matches("[a-zA-Z]+")) {
                    player.sendMessage("§cInvalid objective number. Please use an integer.");
                    return;
                }

                dataManager.getConfig().set("Sieges." + args[1] + ".Objectives." + args[2] + ".CaptureTimer", args[3]);
                dataManager.getConfig().set("Sieges." + args[1] + ".Objectives." + args[2] + ".ObjectiveNr", args[4]);

                player.sendMessage("§aObjective created for the preset " + args[1] + ".");
                break;

            case "remove": // args = {REMOVE, siegeName, objectiveName}
                if (args.length != 3) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege objective remove <siegeName> <objectiveName>");
                    return;
                }

                if (!sieges.contains(args[1])) {
                    player.sendMessage("§cInvalid siege.");
                    return;
                }

                objectives = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + args[1] + ".Objectives"));

                if (!objectives.contains(args[2])) {
                    player.sendMessage("§cInvalid objective.");
                    return;
                }

                dataManager.getConfig().remove("Sieges." + args[1] + ".Objectives." + args[2]);
                player.sendMessage("§aRemoved " + args[2] + " from " + args[1] + ".");
                break;

            case "list": // args = {LIST, siegeName}
                if (args.length != 2) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege objective list <siegeName>");
                    return;
                }

                if (!sieges.contains(args[1])) {
                    player.sendMessage("§cInvalid siege.");
                    return;
                }

                objectives = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + args[1] + ".Objectives"));

                if (objectives.isEmpty()) {
                    player.sendMessage("§cNo objectives found.");
                    return;
                }

                player.sendMessage("§aObjectives for " + args[1] + ":");
                for (String objective : objectives) {
                    player.sendMessage("§a- " + objective);
                }
                break;
        }
    }

    @Subcommand("capturepoint")
    @CommandPermission("fablesiege.siegemanagement")
    @Description("Creates a capture point for an objective")
    @CommandCompletion("create|remove|list @maps")
    @Syntax("capturepoint <create | remove | list> siegeName objectiveName capturePointName radius")
    public void capturepoint(CommandSender sender, String[] args) {
        if (!Utils.checkIfPlayer(sender)) { return; }

        player = (Player) sender;
        List<String> sieges = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges"));
        List<String> objectives;
        List<String> capturePoints;

        switch (args[0].toLowerCase()) {
            case "create": // args = {create siegeName objectiveName capturePointName radius}
                if (args.length != 5) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege capturepoint create <siegeName> <objectiveName> <capturePointName> <radius>");
                    return;
                }

                if (!sieges.contains(args[1])) {
                    player.sendMessage("§cInvalid siege.");
                    return;
                }

                objectives = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + args[1] + ".Objectives"));

                if (!objectives.contains(args[2])) {
                    player.sendMessage("§cInvalid objective.");
                    return;
                }

                capturePoints = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + args[1] + ".Objectives." + args[2] + ".CapturePoints"));

                if (capturePoints.contains(args[3])) {
                    player.sendMessage("§cCapture point already exists.");
                    return;
                }

                if (args[4].matches("[a-zA-Z]+")) {
                    player.sendMessage("§cInvalid radius. Please use an integer.");
                    return;
                }

                int radius = Integer.parseInt(args[4]);

                dataManager.getConfig().set("Sieges." + args[1] + ".Objectives." + args[2] + ".CapturePoints." + args[3] + ".Center.X", player.getLocation().getX());
                dataManager.getConfig().set("Sieges." + args[1] + ".Objectives." + args[2] + ".CapturePoints." + args[3] + ".Center.Y", player.getLocation().getY() - 1);
                dataManager.getConfig().set("Sieges." + args[1] + ".Objectives." + args[2] + ".CapturePoints." + args[3] + ".Center.Z", player.getLocation().getZ());
                dataManager.getConfig().set("Sieges." + args[1] + ".Objectives." + args[2] + ".CapturePoints." + args[3] + ".Radius", radius);

                player.sendMessage("§aCapture point created in " + args[2] + " for the preset " + args[1] + ".");
                break;

            case "remove": // args = {remove siegeName objectiveName capturePointName}
                if (args.length != 4) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege capturepoint remove <siegeName> <objectiveName> <capturePointName>");
                    return;
                }

                if (!sieges.contains(args[1])) {
                    player.sendMessage("§cInvalid siege.");
                    return;
                }

                objectives = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + args[1] + ".Objectives"));

                if (!objectives.contains(args[2])) {
                    player.sendMessage("§cInvalid objective.");
                    return;
                }

                capturePoints = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + args[1] + ".Objectives." + args[2] + ".CapturePoints"));

                if (!capturePoints.contains(args[3])) {
                    player.sendMessage("§cInvalid capture point.");
                    return;
                }

                dataManager.getConfig().remove("Sieges." + args[1] + ".Objectives." + args[2] + ".CapturePoints." + args[3]);
                player.sendMessage("§aRemoved " + args[3] + " from " + args[2] + " for the siege " + args[1] + ".");
                break;

            case "list": // args = {list siegeName objectiveName}
                if (args.length != 3) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege capturepoint list <siegeName> <objectiveName>");
                    return;
                }

                if (!sieges.contains(args[1])) {
                    player.sendMessage("§cInvalid siege.");
                    return;
                }

                objectives = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + args[1] + ".Objectives"));

                if (!objectives.contains(args[2])) {
                    player.sendMessage("§cInvalid objective.");
                    return;
                }

                capturePoints = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + args[1] + ".Objectives." + args[2] + ".CapturePoints"));

                if (capturePoints.isEmpty()) {
                    player.sendMessage("§cNo capture points found.");
                    return;
                }

                player.sendMessage("§aCapture points for " + args[2] + " in " + args[1] + ":");
                for (String point : capturePoints) {
                    player.sendMessage("§a- " + point);
                    player.sendMessage("§a  -X: " + dataManager.getConfig().getDouble("Sieges." + args[1] + ".Objectives." + args[2] + ".CapturePoints." + point + ".Center.X"));
                    player.sendMessage("§a  -Y: " + dataManager.getConfig().getDouble("Sieges." + args[1] + ".Objectives." + args[2] + ".CapturePoints." + point + ".Center.Y"));
                    player.sendMessage("§a  -Z: " + dataManager.getConfig().getDouble("Sieges." + args[1] + ".Objectives." + args[2] + ".CapturePoints." + point + ".Center.Z"));
                }
                break;
        }
    }

    @Subcommand("respawnpoint")
    @CommandPermission("fablesiege.siegemanagement")
    @Description("Creates a respawn point for a team")
    @CommandCompletion("create|remove|list @maps attacking|defending")
    @Syntax("respawnpoint <create | remove | list> siegeName attacking|defending")
    public void respawnpoint(CommandSender sender, String[] args) {
        if (!Utils.checkIfPlayer(sender)) { return; }

        player = (Player) sender;
        List<String> sieges = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges"));
        String teamType = "";

        switch (args[0].toLowerCase()) {
            case "create": // args = {create siegeName teamName}
                if (args.length != 3) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege respawnpoint create <siegeName>");
                    return;
                }

                if (!sieges.contains(args[1])) {
                    player.sendMessage("§cInvalid siege.");
                    return;
                }

                if (args[2].equalsIgnoreCase("attacking")) {
                    teamType = "Attacking";
                } else if (args[2].equalsIgnoreCase("defending")) {
                    teamType = "Defending";
                } else {
                    player.sendMessage("§cInvalid team. Specify attacking or defending.");
                    return;
                }

                dataManager.getConfig().set("Sieges." + args[1] + "." + teamType + "Respawn.world", player.getLocation().getWorld().getName());
                dataManager.getConfig().set("Sieges." + args[1] + "." + teamType + "Respawn.x", player.getLocation().getX());
                dataManager.getConfig().set("Sieges." + args[1] + "." + teamType + "Respawn.y", player.getLocation().getY());
                dataManager.getConfig().set("Sieges." + args[1] + "." + teamType + "Respawn.z", player.getLocation().getZ());
                dataManager.getConfig().set("Sieges." + args[1] + "." + teamType + "Respawn.yaw", player.getLocation().getYaw());
                dataManager.getConfig().set("Sieges." + args[1] + "." + teamType + "Respawn.pitch", player.getLocation().getPitch());

                player.sendMessage("§aRespawn point created for " + args[2] + " in the preset " + args[1] + ".");
                break;

            case "remove": // args = {remove siegeName teamName}
                if (args.length != 3) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege respawnpoint remove <siegeName>");
                    return;
                }

                if (!sieges.contains(args[1])) {
                    player.sendMessage("§cInvalid siege.");
                    return;
                }

                if (args[2].equalsIgnoreCase("attacking")) {
                    dataManager.getConfig().remove("Sieges." + args[1] + ".AttackingRespawn");

                } else if (args[2].equalsIgnoreCase("defending")) {
                    dataManager.getConfig().remove("Sieges." + args[1] + ".DefendingRespawn");
                } else {
                    player.sendMessage("§cInvalid team. Specify attacking or defending.");
                    return;
                }

                player.sendMessage("§aRespawn point removed for " + args[2] + " in the preset " + args[1] + ".");
                break;

            case "list": // args = {list siegeName}
                if (args.length != 2) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege respawnpoint list <siegeName>");
                    return;
                }

                if (!sieges.contains(args[1])) {
                    player.sendMessage("§cInvalid siege.");
                    return;
                }

                player.sendMessage("§aRespawn points for " + args[1] + ":");
                player.sendMessage("§a- Attacking:");
                player.sendMessage("§a  -X: " + dataManager.getConfig().getDouble("Sieges." + args[1] + ".AttackingRespawn.x"));
                player.sendMessage("§a  -Y: " + dataManager.getConfig().getDouble("Sieges." + args[1] + ".AttackingRespawn.y"));
                player.sendMessage("§a  -Z: " + dataManager.getConfig().getDouble("Sieges." + args[1] + ".AttackingRespawn.z"));
                player.sendMessage("§a- Defending:");
                player.sendMessage("§a  -X: " + dataManager.getConfig().getDouble("Sieges." + args[1] + ".DefendingRespawn.x"));
                player.sendMessage("§a  -Y: " + dataManager.getConfig().getDouble("Sieges." + args[1] + ".DefendingRespawn.y"));
                player.sendMessage("§a  -Z: " + dataManager.getConfig().getDouble("Sieges." + args[1] + ".DefendingRespawn.z"));
                break;
        }
    }

    /////////////////////////
    // Management of Teams //
    /////////////////////////

    @Subcommand("team")
    @CommandPermission("fablesiege.team")
    @Description("Creates a team")
    @Syntax("<Create | Remove | AddPlayer | RemovePlayer> <teamName> [further arguments are optional/depend on the first argument]")
    @CommandCompletion("Create|Remove|AddPlayer|RemovePlayer @teams @players")
    public void team(CommandSender sender, String[] args) {
        player = (Player) sender;
        List<String> teams = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Teams"));
        List<String> players;

        switch (args[0].toLowerCase()) {
            case "create": // args = {CREATE, teamName}
                if (args.length != 2) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege team create <teamName>");
                    return;
                }

                if (teams.contains(args[1])) {
                    player.sendMessage("§cTeam already exists.");
                    return;
                }

                dataManager.getConfig().set("Teams." + args[1] + ".players", new ArrayList<String>());
                player.sendMessage("§aTeam " + args[1] + " created.");
                break;

            case "remove": // args = {REMOVE, teamName}
                if (args.length != 2) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege team remove <teamName>");
                    return;
                }

                if (!teams.contains(args[1])) {
                    player.sendMessage("§cInvalid team.");
                    return;
                }

                dataManager.getConfig().remove("Teams." + args[1]);
                player.sendMessage("§aRemoved " + args[1] + ".");
                break;

            case "addplayer": // args = {ADDPLAYER, teamName, playerName}
                if (args.length != 3) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege team addplayer <teamName> <playerName>");
                    return;
                }

                if (!teams.contains(args[1])) {
                    player.sendMessage("§cInvalid team.");
                    return;
                }

                players = (List<String>) dataManager.getConfig().getList("Teams." + args[1] + ".players");

                if (players.contains(args[2].toLowerCase())) {
                    player.sendMessage("§cPlayer already in " + args[1] + ".");
                    return;
                }

                players.add(args[2].toLowerCase());

                dataManager.getConfig().set("Teams." + args[1] + ".players", new ArrayList<>(players));
                player.sendMessage("§aAdded " + args[2] + " to " + args[1] + ".");
                break;

            case "removeplayer": // args = {REMOVEPLAYER, teamName, playerName}
                if (args.length != 3) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege team removeplayer <teamName> <playerName>");
                    return;
                }

                if (!teams.contains(args[1])) {
                    player.sendMessage("§cInvalid team.");
                    return;
                }

                players = (List<String>) dataManager.getConfig().getList("Teams." + args[1] + ".players");

                if (!players.contains(args[2].toLowerCase())) {
                    player.sendMessage("§cPlayer not in " + args[1] + ".");
                    return;
                }

                players.remove(args[2].toLowerCase());
                dataManager.getConfig().set("Teams." + args[1] + ".players", new ArrayList<>(players));

                player.sendMessage("§aRemoved " + args[2] + " from " + args[1] + ".");
                break;
        }
    }

    ////////////////////////
    // Starting of Sieges //
    ////////////////////////

    @Subcommand("load")
    @CommandPermission("fablesiege.startstop")
    @Description("Load a preset")
    @CommandCompletion("@maps @teams @teams ")
    @Syntax("<siege> <attackingTeam(s)> <defendingTeam(s)>")
    public void load(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            player = (Player) sender;
            List<String> sieges = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges"));
            List<String> teams = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Teams"));
            attacking = new ArrayList<>();
            defending = new ArrayList<>();

            if (args.length != 3) {
                player.sendMessage("§cInvalid syntax. Please use /FableSiege load <siege> <attackingTeam(s)> <defendingTeam(s)>");
                return;
            }

            if (!sieges.contains(args[0])) {
                player.sendMessage("§cInvalid siege.");
                return;
            }

            if (!teams.contains(args[1]) || !teams.contains(args[2])) {
                player.sendMessage("§cInvalid team(s).");
                return;
            }

            map = args[0];
            List<String> objectives = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + map + ".Objectives"));

            if (objectives.isEmpty()) {
                player.sendMessage("§cNo objectives found for " + map);
                return;
            }

            for (String objective : objectives) {
                List<String> capturePoints = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + map + ".Objectives." + objective + ".CapturePoints"));

                if (capturePoints.isEmpty()) {
                    player.sendMessage("§cNo capture points found for " + objective + " in " + map);
                    return;
                }
            }

            if (dataManager.getConfig().getMap("Sieges." + map + ".AttackingRespawn").isEmpty()) {
                player.sendMessage("§cNo respawn points found for attacking team in " + map);
                return;
            }

            if (dataManager.getConfig().getMap("Sieges." + map + ".DefendingRespawn").isEmpty()) {
                player.sendMessage("§cNo respawn points found for defending team in " + map);
                return;
            }

            List<String> team1 = (List<String>) dataManager.getConfig().getList("Teams." + args[1] + ".players");
            List<String> team2 = (List<String>) dataManager.getConfig().getList("Teams." + args[1] + ".players");

            if (team1.isEmpty()) {
                player.sendMessage("§cNo players found in " + args[1]);
                return;
            }

            if (team2.isEmpty()) {
                player.sendMessage("§cNo players found in " + args[2]);
                return;
            }

            attackingTeamName = args[1];
            defendingTeamName = args[2];

            for (String name : team1) {
                attacking.add(Bukkit.getPlayer(name));
            }

            for (String name : team2) {
                defending.add(Bukkit.getPlayer(name));
            }

            respawns = dataManager.getConfig().getInt("Sieges." + map + ".Respawns");
            defendersWon = false;
            forceStop = false;
            running = true;
            startObjectives(map, attacking, defending);

            player.sendMessage("§a" + map + " loaded.");
        }
    }

    @Subcommand("stop")
    @CommandPermission("fablesiege.startstop")
    @Description("Stops all sieges")
    public void stop(CommandSender sender) { forceStop = true; }

    /////////////
    // Methods //
    /////////////

    public void startObjectives(String map, List<Player> attacking, List<Player> defending) {
        List<String> objectives = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + map + ".Objectives"));
        List<String> objectiveNames = new ArrayList<>();
        running = true;

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
        List<String> capturePoints = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + map + ".Objectives." + objective + ".CapturePoints"));

        BukkitRunnable runnable = new BukkitRunnable() {
            double timer = 2 * captureTime; // first number and period in runnable.runTaskTimer = 20 always
            int timeRunnning = 0;
            String message = "";

            @Override
            public void run() {
                Color color = Color.fromRGB(0, 255, 0);

                double timerInPercent = (100 - Math.round((1 - (timer / 2) / captureTime) * 100));

                if (timerInPercent > 50) {
                    message = "§l§c" + objective + " §r- §l§2" + timerInPercent + "% §r- §l§cRespawns: "  + respawns;
                } else if (timerInPercent > 20) {
                    message = "§l§c" + objective + " §r- §l§6 " + timerInPercent + "% §r- §l§cRespawns: "  + respawns;
                    color = Color.fromRGB(255, 165, 0);
                } else {
                    message = "§l§c" + objective + " §r- §l§4 " + timerInPercent + "% §r- §l§cRespawns: "  + respawns;
                    color = Color.fromRGB(255, 0, 0);
                }


                // For each capture point, draw a circle around it and count the amount of players per team in the circle
                for (String point : capturePoints) {
                    Location loc = new Location(player.getWorld(), dataManager.getConfig().getDouble("Sieges." + map + ".Objectives." + objective + ".CapturePoints." + point + ".Center.X"),
                            dataManager.getConfig().getDouble("Sieges." + map + ".Objectives." + objective + ".CapturePoints." + point + ".Center.Y"),
                            dataManager.getConfig().getDouble("Sieges." + map + ".Objectives." + objective + ".CapturePoints." + point + ".Center.Z"));
                    int amountAttacking = 0;
                    int amountDefending = 0;
                    Map<Player, String> targets = new Hashtable<>();
                    double radius = dataManager.getConfig().getDouble("Sieges." + map + ".Objectives." + objective + ".CapturePoints." + point + ".Radius");

                    Utils.drawCircle(radius, loc, color, player, particleApi);

                    for (LivingEntity entity : Utils.getNearbyLivingEntities(player, loc, radius)) {
                        if (entity instanceof Player) {
                            Player target = (Player) entity;

                            if (target.getGameMode() == GameMode.SPECTATOR) {
                                continue;
                            }

                            if (attacking.contains(target)) {
                                targets.put(target, "attacking");
                            } else if (defending.contains(target)) {
                                targets.put(target, "defending");
                            }
                        }
                    }

                    for (Map.Entry<Player, String> key : targets.entrySet()) {
                        if (key.getValue().equals("attacking")) {
                            amountAttacking++;
                        } else if (key.getValue().equals("defending")) {
                            amountDefending++;
                        }
                    }

                    if (amountAttacking > amountDefending) {
                        timer--;
                        if (timer == 0) {
                            if (objectives.size() - 1 == counter) {
                                Utils.teamWon(attackingTeamName, attacking, defending);
                                running = false;
                                cancel();
                                return;
                            }

                            Utils.pointCaptured(attacking, defending, objective, loc);
                            counter++;
                            startObjectives(map, attacking, defending);
                            running = false;
                            cancel();
                        }
                    } else if (amountAttacking < amountDefending) {
                        if (timer < 2 * 30) {
                            if ((timeRunnning / 2) % 10 == 0) {
                                timer++;
                            }
                        }
                    } else if (amountAttacking == amountDefending && amountDefending != 0) {
                        message = "§l§c" + objective + " §r- §eContested! §r- §l§cRespawns: "  + respawns;
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

                if (defendersWon) {
                    Utils.teamWon(defendingTeamName, defending, attacking);
                    running = false;
                    cancel();
                }

                if (forceStop) {
                    running = false;
                    cancel();
                }

                timeRunnning += 2;
            }
        };
        runnable.runTaskTimer(Main.getInstance(), 0L, 10L);
    }

    public Location getRespawnPoint(String team) {
        return new Location(Bukkit.getWorld(dataManager.getConfig().getString("Sieges." + map + "." + team + "Respawn.world")),
                dataManager.getConfig().getDouble("Sieges." + map + "." + team + "Respawn.x"),
                dataManager.getConfig().getDouble("Sieges." + map + "." + team + "Respawn.y"),
                dataManager.getConfig().getDouble("Sieges." + map + "." + team + "Respawn.z"),
                dataManager.getConfig().getFloat("Sieges." + map + "." + team + "Respawn.yaw"),
                dataManager.getConfig().getFloat("Sieges." + map + "." + team + "Respawn.pitch"));
    }
}





