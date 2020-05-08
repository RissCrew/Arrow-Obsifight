package fr.risscrew.arrow_obsifight;

import fr.risscrew.arrow_obsifight.command.admin.CommandArrowSpawnPoints;
import fr.risscrew.arrow_obsifight.command.admin.CommandSetLobbyArrow;
import fr.risscrew.arrow_obsifight.command.player.CommandJoinArrow;
import fr.risscrew.arrow_obsifight.command.player.CommandHelpArrow;
import fr.risscrew.arrow_obsifight.command.animator.CommandStartArrow;
import fr.risscrew.arrow_obsifight.command.player.CommandLeaveArrow;
import fr.risscrew.arrow_obsifight.command.animator.CommandStopArrow;
import fr.risscrew.arrow_obsifight.listener.ListenerArrow;
import fr.risscrew.arrow_obsifight.utils.UpdateTimer;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

public class Main extends JavaPlugin
{
    private final UpdateTimer timer = new UpdateTimer();
    private CommandStartArrow startArrow;
    private CommandSetLobbyArrow setLobbyArrow;
    private ListenerArrow listenerArrow;
    private JSONObject config;

    @Override
    public void onEnable()
    {
        try
        {
            String text = new String(Files.readAllBytes(Paths.get("plugins/event-plugin/config-arrow.json")), StandardCharsets.UTF_8);
            config = new JSONObject(text);
        }
        catch (NoSuchFileException nsfe)
        {
            try
            {
                if (!Files.exists(Paths.get("plugins/event-plugin/config-arrow.json").getParent())) Files.createDirectories(Paths.get("plugins/event-plugin/config-arrow.json").getParent());
                JSONObject obj = new JSONObject();
                Files.write(Paths.get("plugins/event-plugin/config-arrow.json"), obj.toString().getBytes());
                config = obj;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        this.getLogger().info("Démarrage du plugin d'event Arrow");

        setLobbyArrow = new CommandSetLobbyArrow(config);
        listenerArrow = new ListenerArrow(timer, this, setLobbyArrow);
        startArrow = new CommandStartArrow(timer, listenerArrow, config, setLobbyArrow);

        getServer().getPluginManager().registerEvents(listenerArrow, this);
        this.getCommand("setlobbyarrow").setExecutor(setLobbyArrow);
        this.getCommand("joinarrow").setExecutor(new CommandJoinArrow(setLobbyArrow));
        this.getCommand("startarrow").setExecutor(startArrow);
        this.getCommand("stoparrow").setExecutor(new CommandStopArrow(timer, listenerArrow));
        this.getCommand("leavearrow").setExecutor(new CommandLeaveArrow(timer, listenerArrow));
        this.getCommand("arrowspawnpoints").setExecutor(new CommandArrowSpawnPoints(startArrow));
        this.getCommand("helparrow").setExecutor(new CommandHelpArrow());

    }

    @Override
    public void onDisable()
    {
        this.getLogger().info("Extinction du plugin d'event Arrow");
        if(timer.isRunning()) timer.stop();
        JSONObject output = new JSONObject();
        output.put("spawn", startArrow.getLocationToConfig());
        output.put("lobby", setLobbyArrow.getLobbyToConfig());
        try
        {
            Files.write(Paths.get("plugins/event-plugin/config-arrow.json"),output.toString(4).getBytes());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


}
