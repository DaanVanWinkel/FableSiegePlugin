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

    @HelpCommand
    @Private
    public void help(CommandSender sender, CommandHelp help) { help.showHelp(); }

    //////////////////////////
    // Management of Sieges //
    //////////////////////////

    @Subcommand("create")
    @CommandPermission("fablesiege.siegemanagement")
    @Description("Creates everything a siege needs to function")
    @CommandCompletion("Siege|Objective|Point|RespawnPoint @maps ")
    @Syntax("create <Siege|Objective|Point|RespawnPoint> <siege> [further arguments are optional/depend on the first argument]")
    public void create(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou must be a player to use this command.");
            return;
        }

        Player player = (Player) sender;
        List<String> sieges = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges"));
        List<String> objectives;

        switch (args[0].toUpperCase()) {
            case "SIEGE": // args = {Siege, siegeName, defaultRespawns}
                int defaultRespawns = 50;

                if (2 <= args.length && args.length <= 3) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege create siege <siegeName> <defaultRespawns>");
                    return;
                }

                if (sieges.contains(args[1])) {
                    player.sendMessage("§cSiege already exists.");
                    return;
                }

                if (args[2].matches("[a-zA-Z]+")) {
                    player.sendMessage("§cInvalid amount of respawns. Please use an integer.");
                    return;
                } else if (Integer.parseInt(args[2]) == 0) {
                    player.sendMessage("§cNo amount of respawns given. Defaulting to 50.");
                } else {
                    defaultRespawns = Integer.parseInt(args[2]);
                }

                dataManager.getConfig().set("Sieges." + args[1] + ".Respawns", defaultRespawns);
                player.sendMessage("§aPreset " + args[1] + " created.");
                break;

            case "OBJECTIVE": // args = {Objective, siegeName, objectiveName, captureTime, objectiveNumber}
                if (args.length != 5) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege create objective <siegeName> <objectiveName> <captureTime> <objectiveNumber>");
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

            case "POINT": // args = {Point, siegeName, objectiveName, pointName, radius}
                if (args.length != 5) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege create point <siegeName> <objectiveName> <pointName> <radius>");
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

                if (args[4].matches("[a-zA-Z]+")) {
                    player.sendMessage("§cInvalid radius. Please use an integer.");
                    return;
                }

                List<String> capturePoints = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + args[1] + ".Objectives." + args[2] + ".CapturePoints"));
                int radius = Integer.parseInt(args[4]);

                dataManager.getConfig().set("Sieges." + args[1] + ".Objectives." + args[2] + ".CapturePoints." + args[3] + ".Center.X", player.getLocation().getX());
                dataManager.getConfig().set("Sieges." + args[1] + ".Objectives." + args[2] + ".CapturePoints." + args[3] + ".Center.Y", player.getLocation().getY() - 1);
                dataManager.getConfig().set("Sieges." + args[1] + ".Objectives." + args[2] + ".CapturePoints." + args[3] + ".Center.Z", player.getLocation().getZ());
                dataManager.getConfig().set("Sieges." + args[1] + ".Objectives." + args[2] + ".CapturePoints." + args[3] + ".Radius", radius);

                player.sendMessage("§aCapture point created in " + args[2] + " for the preset " + args[1] + ".");
                break;

            case "RESPAWNPOINT": // args = {RespawnPoint, siegeName, attacking/defending}
                if (args.length != 3) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege create respawnpoint <siege> <attacking/defending>");
                    return;
                }

                if (!sieges.contains(args[1])) {
                    player.sendMessage("§cInvalid siege.");
                    return;
                }

                if (args[2].equals("attacking")) {
                    dataManager.getConfig().set("Sieges." + args[1] + ".AttackingRespawn." + ".X", player.getLocation().getX());
                    dataManager.getConfig().set("Sieges." + args[1] + ".AttackingRespawn." + ".Y", player.getLocation().getY());
                    dataManager.getConfig().set("Sieges." + args[1] + ".AttackingRespawn." + ".Z", player.getLocation().getZ());
                } else if (args[2].equals("defending")) {
                    dataManager.getConfig().set("Sieges." + args[1] + ".DefendingRespawn." + ".X", player.getLocation().getX());
                    dataManager.getConfig().set("Sieges." + args[1] + ".DefendingRespawn." + ".Y", player.getLocation().getY());
                    dataManager.getConfig().set("Sieges." + args[1] + ".DefendingRespawn." + ".Z", player.getLocation().getZ());
                } else {
                    player.sendMessage("§cInvalid team. Specify attacking or defending.");
                    return;
                }

                player.sendMessage("§aRespawn point created for " + args[2] + " in the preset " + args[1] + ".");
                break;
        }
    }

    @Subcommand("remove")
    @CommandPermission("fablesiege.siegemanagement")
    @Description("Removes a siege preset, and all data")
    @CommandCompletion("Siege|Objective|Point|RespawnPoint @maps ")
    @Syntax("remove <Siege|Objective|Point|RespawnPoint> <siege> [further arguments are optional/depend on the first argument]")
    public void remove(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou must be a player to use this command.");
            return;
        }

        Player player = (Player) sender;
        List<String> sieges = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges"));
        List<String> objectives;

        switch (args[0].toUpperCase()) {
            case "SIEGE": // args = {Siege, siegeName, confirmation}
                if (args.length > 2) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege remove siege <siegeName>");
                    return;
                }

                if (!sieges.contains(args[1])) {
                    player.sendMessage("§cInvalid siege.");
                    return;
                }

                if (args.length == 2) {
                    player.sendMessage("§cPlease confirm the removal of §lALL §r§cthe data in " + args[1] + " by using /FableSiege remove siege " + args[1] + " confirm");
                    return;
                }

                if (args[2].toUpperCase().equals( "CONFIRM")) {
                    dataManager.getConfig().remove("Sieges." + args[1]);
                    player.sendMessage("§aRemoved " + args[1] + ".");
                } else {
                    player.sendMessage("§cPlease confirm the removal of §lALL §r§cthe data in " + args[1] + " by using /FableSiege remove siege " + args[1] + " confirm");
                }
                break;

            case "OBJECTIVE": // args = {Objective, siegeName, objectiveName}
                if (args.length != 3) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege remove objective <siegeName> <objectiveName>");
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

            case "POINT": // args = {Point, siegeName, objectiveName, pointName}
                if (args.length != 4) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege remove point <siegeName> <objectiveName> <pointName>");
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

                List<String> capturePoints = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + args[1] + ".Objectives." + args[2] + ".CapturePoints"));

                if (!capturePoints.contains(args[3])) {
                    player.sendMessage("§cInvalid capture point.");
                    return;
                }

                dataManager.getConfig().remove("Sieges." + args[1] + ".Objectives." + args[2] + ".CapturePoints." + args[3]);
                player.sendMessage("§aRemoved " + args[3] + " from " + args[2] + " for the preset " + args[1] + ".");
                break;

            case "RESPAWNPOINT": // args = {RespawnPoint, siegeName, attacking/defending}
                if (args.length != 3) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege remove respawnpoint <siegeName> <attacking/defending>");
                    return;
                }

                if (!sieges.contains(args[1])) {
                    player.sendMessage("§cInvalid siege.");
                    return;
                }

                if (args[2].equals("attacking")) {
                    dataManager.getConfig().remove("Sieges." + args[1] + ".AttackingRespawn");
                } else if (args[2].equals("defending")) {
                    dataManager.getConfig().remove("Sieges." + args[1] + ".DefendingRespawn");
                } else {
                    player.sendMessage("§cInvalid team. Specify attacking or defending.");
                    return;
                }

                player.sendMessage("§aRemoved respawn point for " + args[2] + " in the preset " + args[1] + ".");
                break;
        }
    }

    ///////////////////
    // Listing stuff //
    ///////////////////

    @Subcommand("list")
    @CommandPermission("fablesiege.list")
    @Description("Lists all sieges, objectives, capture points")
    @CommandCompletion("Siege|Objective|Point|RespawnPoint|Teams|Players")
    @Syntax("list <Siege|Objective|Point|RespawnPoint|Teams|Players> [further arguments are optional/depend on the first argument]")
    public void list(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou must be a player to use this command.");
            return;
        }

        Player player = (Player) sender;
        List<String> sieges = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges"));
        List<String> objectives;

        switch (args[0].toUpperCase()) {
            case "SIEGE": // args = {Siege}
                if (args.length != 1) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege list siege");
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

            case "OBJECTIVE": // args = {Objective, siegeName}
                if (args.length != 2) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege list objective <siegeName>");
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

            case "POINT": // args = {Point, siegeName, objectiveName}
                if (args.length != 3) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege list point <siegeName> <objectiveName>");
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

                List<String> capturePoints = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges." + args[1] + ".Objectives." + args[2] + ".CapturePoints"));

                if (capturePoints.isEmpty()) {
                    player.sendMessage("§cNo capture points found.");
                    return;
                }

                player.sendMessage("§aCapture points for " + args[2] + " in " + args[1] + ":");
                for (String point : capturePoints) {
                    player.sendMessage("§a- " + point);
                }
                break;

            case "RESPAWNPOINT": // args = {RespawnPoint, siegeName}
                if (args.length != 2) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege list respawnpoint <siegeName>");
                    return;
                }

                if (!sieges.contains(args[1])) {
                    player.sendMessage("§cInvalid siege.");
                    return;
                }

                if (dataManager.getConfig().getMap("Sieges." + args[1] + ".AttackingRespawn").isEmpty()) {
                    player.sendMessage("§cNo respawn point found for attacking team.");
                } else {
                    player.sendMessage("§aRespawn point for attacking team in " + args[1] + ":");
                    player.sendMessage("§a- X: " + Math.round(dataManager.getConfig().getDouble("Sieges." + args[1] + ".AttackingRespawn.X") * 100) / 100.0);
                    player.sendMessage("§a- Y: " + Math.round(dataManager.getConfig().getDouble("Sieges." + args[1] + ".AttackingRespawn.Y") * 100) / 100.0);
                    player.sendMessage("§a- Z: " + Math.round(dataManager.getConfig().getDouble("Sieges." + args[1] + ".AttackingRespawn.Z") * 100) / 100.0);
                }

                if (dataManager.getConfig().getMap("Sieges." + args[1] + ".DefendingRespawn").isEmpty()) {
                    player.sendMessage("§cNo respawn point found for defending team.");
                } else {
                    player.sendMessage("§aRespawn point for defending team in " + args[1] + ":");
                    player.sendMessage("§a- X: " + Math.round(dataManager.getConfig().getDouble("Sieges." + args[1] + ".DefendingRespawn.X") * 100) / 100.0);
                    player.sendMessage("§a- Y: " + Math.round(dataManager.getConfig().getDouble("Sieges." + args[1] + ".DefendingRespawn.Y") * 100) / 100.0);
                    player.sendMessage("§a- Z: " + Math.round(dataManager.getConfig().getDouble("Sieges." + args[1] + ".DefendingRespawn.Z") * 100) / 100.0);
                }
                break;

            case "TEAMS": // args = {Team}
                if (args.length != 1) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege list team");
                    return;
                }

                List<String> teams = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Teams"));

                if (teams.isEmpty()) {
                    player.sendMessage("§cNo teams found.");
                    return;
                }

                player.sendMessage("§aTeams:");
                for (String team : teams) {
                    player.sendMessage("§a- " + team);
                }
                break;

            case "PLAYERS": // args = {Players, teamName}
                if (args.length != 2) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege list players <teamName>");
                    return;
                }

                teams = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Teams"));

                if (!teams.contains(args[1])) {
                    player.sendMessage("§cInvalid team.");
                    return;
                }

                List<String> players = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Teams." + args[1] + ".players"));

                if (players.isEmpty()) {
                    player.sendMessage("§cNo players found.");
                    return;
                }

                player.sendMessage("§aPlayers in " + args[1] + ":");
                for (String playerInTeam : players) {
                    player.sendMessage("§a- " + playerInTeam);
                }
                break;
        }
    }

    /////////////////////////
    // Management of Teams //
    /////////////////////////
    // TODO: Test teams

    @Subcommand("team")
    @CommandPermission("fablesiege.team")
    @Description("Creates a team")
    @Syntax("<teamName>")
    @CommandCompletion("CREATE|REMOVE|ADDPLAYER|REMOVEPLAYER @teams")
    public void team(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        List<String> teams = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Teams"));
        List<String> allTeams = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Teams"));

        switch (args[0].toUpperCase()) {
            case "CREATE": // args = {CREATE, teamName}
                if (args.length != 2) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege team create <teamName>");
                    return;
                }

                if (teams.contains(args[1])) {
                    player.sendMessage("§cTeam already exists.");
                    return;
                }

                dataManager.getConfig().set("Teams." + args[1] + ".players.value", "otherwise no map and shit breaks");
                player.sendMessage("§aTeam " + args[1] + " created.");
                break;

            case "REMOVE": // args = {REMOVE, teamName}
                if (args.length != 2) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege team remove <teamName>");
                    return;
                }

                if (!teams.contains(args[1])) {
                    player.sendMessage("§cInvalid team.");
                    return;
                }

                dataManager.getConfig().remove("Teams." + args[1]);
                player.sendMessage("§aTeam " + args[1] + " removed.");
                break;

            case "ADDPLAYER": // args = {ADD, teamName, playerName}
                if (args.length != 3) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege team add <teamName> <playerName>");
                    return;
                }

                if (!teams.contains(args[1])) {
                    player.sendMessage("§cInvalid team.");
                    return;
                }

                if (Bukkit.getPlayer(args[2]) == null) {
                    player.sendMessage("§cCould not find that player.");
                    return;
                }

//                for (String teamName : allTeams) {
//                    if (!Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Teams." + teamName)).contains(args[2])) {
//                        player.sendMessage("§c" + args[2] + " is already in " + teamName + ".");
//                        return;
//                    }
//                }

                List<String> team = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Teams." + args[1] + ".players"));

                dataManager.getConfig().set("Teams." + args[1] + ".players." + args[2] + ".id", team.size() + 1);
                player.sendMessage("§aAdded " + args[2] + " to " + args[1] + ".");
                break;

            case "REMOVEPLAYER": // args = {REMOVEPLAYER, teamName, playerName  }
                if (args.length != 3) {
                    player.sendMessage("§cInvalid syntax. Please use /FableSiege team remove <teamName> <playerName>");
                    return;
                }

                if (!teams.contains(args[1])) {
                    player.sendMessage("§cInvalid team.");
                    return;
                }

                if (Bukkit.getPlayer(args[2]) == null) {
                    player.sendMessage("§cCould not find that player.");
                    return;
                }

//                for (String teamName : allTeams) {
//                    if (!Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Teams." + teamName)).contains(args[2])) {
//                        player.sendMessage("§c" + args[2] + " is not in " + teamName + ".");
//                        return;
//                    }
//                }

                dataManager.getConfig().remove("Teams." + args[1] + ".players." + args[2]);
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
            Player player = (Player) sender;
            List<String> sieges = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges"));
            List<String> teams = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Teams"));

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
            List<String> team1 = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Teams." + args[1] + ".players"));
            List<String> team2 = Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Teams." + args[2] + ".players"));

            attackingTeamName = args[1];
            defendingTeamName = args[2];

            List<Player> attacking = new ArrayList<>();
            List<Player> defending = new ArrayList<>();

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
            startObjectives(player, map, attacking, defending);

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

    public void startObjectives(Player player, String map, List<Player> attacking, List<Player> defending) {
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

                if (timerInPercent > 50) { // TODO: respawns
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
                    Map<String, Player> targets = new Hashtable<>();
                    double radius = dataManager.getConfig().getDouble("Sieges." + map + ".Objectives." + objective + ".CapturePoints." + point + ".Radius");

                    Utils.drawCircle(radius, loc, color, player, particleApi);

                    for (LivingEntity entity : Utils.getNearbyLivingEntities(player, loc, radius)) {
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
                            if (objectives.size() - 1 == counter) {
                                Utils.teamWon(attackingTeamName, attacking, defending);
                                running = false;
                                cancel();
                                return;
                            }

                            Utils.pointCaptured(attacking, defending, objective, loc);
                            counter++;
                            startObjectives(player, map, attacking, defending);
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
}





