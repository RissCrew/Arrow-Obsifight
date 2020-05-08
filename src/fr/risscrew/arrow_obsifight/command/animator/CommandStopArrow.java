package fr.risscrew.arrow_obsifight.command.animator;

import fr.risscrew.arrow_obsifight.listener.ListenerArrow;
import fr.risscrew.arrow_obsifight.utils.UpdateTimer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandStopArrow implements CommandExecutor {

    private final UpdateTimer timer;
    private final ListenerArrow listenerArrow;

    public CommandStopArrow(UpdateTimer timer, ListenerArrow listenerArrow)
    {
        this.timer = timer;
        this.listenerArrow = listenerArrow;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
    {
        if (timer.isRunning())
        {
            timer.stop();
            listenerArrow.stopEvent();
        }
        else
        {
            commandSender.sendMessage(ChatColor.GOLD+"[ArrowObsifight] "+ChatColor.RESET+"Aucune partie n'est en cours.");
        }
        return true;
    }

}
