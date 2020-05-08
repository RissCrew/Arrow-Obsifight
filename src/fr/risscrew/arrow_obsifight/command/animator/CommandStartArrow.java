package fr.risscrew.arrow_obsifight.command.animator;

import fr.risscrew.arrow_obsifight.command.admin.CommandSetLobbyArrow;
import fr.risscrew.arrow_obsifight.listener.ListenerArrow;
import fr.risscrew.arrow_obsifight.utils.UpdateTimer;
import net.minecraft.util.com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CommandStartArrow implements CommandExecutor
{
    private final UpdateTimer timer;
    private final ListenerArrow listenerArrow;
    private List<Location> spawnLocation;
    private CommandSetLobbyArrow commandSetLobbyArrow;
    private String prefix = ChatColor.GOLD+"[ArrowObsifight] "+ChatColor.RESET;
    private Random rand = new Random();

    public CommandStartArrow(UpdateTimer timer, ListenerArrow listenerArrow, JSONObject config, CommandSetLobbyArrow commandSetLobbyArrow)
    {
        this.timer = timer;
        this.listenerArrow = listenerArrow;
        this.spawnLocation = getLocationsFromConfig(config);
        this.commandSetLobbyArrow = commandSetLobbyArrow;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
    {
        Player sender = (Player) commandSender;
        Location lobbyLocation = commandSetLobbyArrow.getLobbyLocation();
        if (commandSender instanceof ConsoleCommandSender)
        {
            commandSender.sendMessage("Indisponible depuis la console.");
        }
        else if (lobbyLocation == null)
        {
            sender.sendMessage(prefix+"Aucun lobby est existant, merci d'en définir un avec /setlobbyarrow");
        }
        else if(timer.isRunning())
        {
            sender.sendMessage(prefix+"Une partie est déjà en cours.");
        }
        else
        {
            for(Player p : lobbyLocation.getWorld().getPlayers())
            {
                if (p.getLocation().distance(lobbyLocation) > 50) return true;

                ItemStack bow = new ItemStack(Material.BOW, 1);
                bow.addUnsafeEnchantment(Enchantment.DURABILITY, 10);

                p.getInventory().clear();
                p.getInventory().setItem(0, bow);
                p.getInventory().setItem(1, new ItemStack(Material.ARROW, 1));

                listenerArrow.addPlayingplayers(p);
                listenerArrow.addCooldownToPlayer(p);

                teleportPlayerToGame(p);
                p.sendMessage(prefix+"Vous avez été téléporté dans l'arène");
            }
            timer.start();
            Bukkit.getServer().broadcastMessage(prefix+"Une partie de arrow a été lancée !");
        }
        return true;
    }

    private void teleportPlayerToGame(Player p)
    {
        double maxdistance = 0D;
        double distance;
        Location theplacetobe = p.getLocation();

        for(Location spawnpoints : spawnLocation)
        {
            for (Player player : listenerArrow.getPlayingPlayers())
            {
                if (player.equals(p)) continue;
                distance = spawnpoints.distance(player.getLocation());
                if (distance > maxdistance)
                {
                        maxdistance = distance;
                        theplacetobe = spawnpoints;
                }
            }
        }

        if (theplacetobe.equals(p.getLocation())) p.teleport(spawnLocation.get(rand.nextInt(spawnLocation.size())));
        else p.teleport(theplacetobe);
    }
    private List<Location> getLocationsFromConfig(JSONObject config)
    {
        if (config.isNull("spawn"))
        {
            config.put("spawn", new JSONArray());
            return new ArrayList<>();
        }
        else
        {
            List<Location> configlist = new ArrayList<>();
            JSONArray arraySpawns = config.getJSONArray("spawn");
            for(Object object : arraySpawns)
            {
                JSONObject jsonObject = (JSONObject) object;
                Location spawnLocation = new Location(Bukkit.getWorld(UUID.fromString(jsonObject.getString("world"))), jsonObject.getDouble("x"), jsonObject.getDouble("y"), jsonObject.getDouble("z"), jsonObject.getFloat("yaw"), jsonObject.getFloat("pitch"));
                configlist.add(spawnLocation);
            }
            return configlist;
        }

    }

    public JSONArray getLocationToConfig()
    {
        JSONArray array = new JSONArray();
        for (Location location : spawnLocation)
        {
            JSONObject newlocation = new JSONObject();

            newlocation.put("world", location.getWorld().getUID().toString());
            newlocation.put("x", location.getX());
            newlocation.put("y", location.getY());
            newlocation.put("z", location.getZ());
            newlocation.put("yaw", location.getYaw());
            newlocation.put("pitch", location.getPitch());

            array.put(newlocation);
        }

        return array;
    }

    public List<Location> getSpawnLocation() { return spawnLocation; }

    public void setSpawnLocation(List<Location> spawnLocation) { this.spawnLocation = spawnLocation; }

    public void addSpawnLocation(Location location) { spawnLocation.add(location); }

    public int sizeOfSpawnLocations() { return spawnLocation.size(); }


}
