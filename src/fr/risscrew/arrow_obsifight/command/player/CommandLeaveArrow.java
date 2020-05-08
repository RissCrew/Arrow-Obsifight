package fr.risscrew.arrow_obsifight.command.player;

import fr.risscrew.arrow_obsifight.listener.ListenerArrow;
import fr.risscrew.arrow_obsifight.utils.UpdateTimer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandLeaveArrow implements CommandExecutor
{

    private final UpdateTimer timer;
    private final ListenerArrow listenerArrow;
    private final Map<Player, Boolean> isteleporting = new HashMap<>();
    private final String prefix = ChatColor.GOLD+"[ArrowObsifight] "+ChatColor.RESET;

    public CommandLeaveArrow(UpdateTimer timer, ListenerArrow listenerArrow)
    {
        this.timer = timer;
        this.listenerArrow = listenerArrow;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
    {
        if (commandSender instanceof ConsoleCommandSender)
        {
            commandSender.sendMessage("Indisponible depuis la console.");
            return true;
        }

        if (!timer.isRunning()) commandSender.sendMessage(prefix+"Il n'y a pas de partie en cours !");
        else
        {
            Player p = (Player) commandSender;

            if(!listenerArrow.getPlayingPlayers().contains(p)) p.sendMessage(prefix+"Vous n'êtes pas dans l'arène !");
            else if(isteleporting.containsKey(p)) p.sendMessage(prefix+"Vous vous téléportez déjà.");
            else
            {
                p.sendMessage(prefix+"Téléportation en cours, veuillez ne pas bouger pendant 3 secondes");
                isteleporting.put(p, true);

                Thread waiting = new Thread(() ->
                {
                    boolean success = true;
                    Location plocation = p.getLocation();

                    for (int i = 0; i < 4; i++)
                    {
                        try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
                        if (plocation.getX() != (p.getLocation().getX()) || plocation.getY() != (p.getLocation().getY()) || plocation.getZ() != (p.getLocation().getZ()))
                        {
                            success=false;
                            break;
                        }
                    }

                    if (success)
                    {
                        p.sendMessage(prefix+" Téléporté en dehors de l'arène");
                        p.teleport(new Location(p.getLocation().getWorld(), -40,99, 2));
                        p.getInventory().clear();
                        p.updateInventory();

                        List<Player> playerslistenerArrow = listenerArrow.getPlayingPlayers();
                        playerslistenerArrow.remove(p);
                        listenerArrow.setPlayingPlayers(playerslistenerArrow);
                    }
                    else
                    {
                        p.sendMessage(prefix+ChatColor.RED+"Téléportation interrompu");
                    }
                    isteleporting.put(p, false);
                });
                waiting.start();
            }
        }
        return true;
    }
}
