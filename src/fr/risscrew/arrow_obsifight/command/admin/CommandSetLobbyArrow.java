package fr.risscrew.arrow_obsifight.command.admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.util.UUID;

public class CommandSetLobbyArrow implements CommandExecutor {

    private Location lobbyLocation;
    private final static String prefix = ChatColor.GOLD+"[ArrowObsifight] "+ChatColor.RESET;


    public CommandSetLobbyArrow(JSONObject config)
    {
        this.lobbyLocation = getLobbyLocationFromConfig(config);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
    {
        if (commandSender instanceof ConsoleCommandSender) { commandSender.sendMessage("Indisponible depuis la console."); }
        else
        {
            Player p = (Player) commandSender;
            lobbyLocation = p.getLocation();
            p.sendMessage(prefix + "Vous avez définit un spawn pour le lobby.");
        }
        return true;
    }

    private Location getLobbyLocationFromConfig(JSONObject config)
    {
        if (config.isNull("lobby"))
        {
            config.put("lobby", new JSONObject());
            return null;
        }
        else
        {
            JSONObject lobby = config.getJSONObject("lobby");
            return new Location(Bukkit.getWorld(UUID.fromString(lobby.getString("world"))), lobby.getDouble("x"),lobby.getDouble("y"), lobby.getDouble("z"), lobby.getFloat("yaw"), lobby.getFloat("pitch"));
        }
    }

    public Location getLobbyLocation() { return lobbyLocation; }

    public JSONObject getLobbyToConfig()
    {
        JSONObject newlocation = new JSONObject();
        newlocation.put("world", lobbyLocation.getWorld().getUID().toString());
        newlocation.put("x", lobbyLocation.getX());
        newlocation.put("y", lobbyLocation.getY());
        newlocation.put("z", lobbyLocation.getZ());
        newlocation.put("yaw", lobbyLocation.getYaw());
        newlocation.put("pitch", lobbyLocation.getPitch());
        return newlocation;
    }

}
