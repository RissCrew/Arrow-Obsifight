package fr.risscrew.arrow_obsifight.command.player;

import fr.risscrew.arrow_obsifight.command.admin.CommandSetLobbyArrow;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;


public class CommandJoinArrow implements CommandExecutor {

    private final CommandSetLobbyArrow commandSetLobbyArrow;
    private String prefix = ChatColor.GOLD+"[ArrowObsifight] "+ChatColor.RESET;

    public CommandJoinArrow(CommandSetLobbyArrow commandSetLobbyArrow)
    {
        this.commandSetLobbyArrow = commandSetLobbyArrow;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
    {
        if (commandSender instanceof ConsoleCommandSender)
        {
            commandSender.sendMessage("Indisponible depuis la console.");
            return true;
        }
        else
        {
            Player p = (Player) commandSender;
            Location lobbyLocation = commandSetLobbyArrow.getLobbyLocation();
            if(lobbyLocation == null) p.sendMessage(prefix+"Il n'existe pas de lobby arrow.");
            else
            {
                p.teleport(lobbyLocation);
                p.sendMessage(prefix+"Vous avez été téléporté au lobby arrow.");
            }
        }
        return true;
    }

}
