package fr.risscrew.arrow_obsifight.command.player;


import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class CommandHelpArrow implements CommandExecutor {

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
            Player p = (Player)commandSender;
            StringBuilder sb = new StringBuilder();
            sb.append(ChatColor.YELLOW + "--------------" +ChatColor.WHITE+" Aide "+ChatColor.YELLOW+"-------------"+ChatColor.RESET+
                    "\n"+ ChatColor.GOLD+"/helparrow"+ ChatColor.WHITE+": Vous affiche cette liste." +
                    "\n"+ ChatColor.GOLD+"/joinarrow"+ ChatColor.WHITE+": Vous permet de rejoindre l'arène arrow." +
                    "\n"+ ChatColor.GOLD+"/leavearrow"+ ChatColor.WHITE+": Vous permet de quitter l'arène arrow." );
            if (p.hasPermission("arrow-obsifight.animaor"))
            {
                sb.append("\n" + ChatColor.GOLD + "/startarrow" + ChatColor.WHITE + ": Vous permet de démarrer l'event de l'arène arrow.");
                sb.append("\n" + ChatColor.GOLD + "/stoparrow" + ChatColor.WHITE + ": Vous permet d'arrêter l'event de l'arène arrow.");
            }
            if (p.hasPermission("arrow-obsifight.op"))
            {
                sb.append("\n"+ ChatColor.GOLD+"/asp "+ ChatColor.WHITE+": Vous permet de gérer les points de spawn de l'arène arrow.");
                sb.append("\n"+ ChatColor.GOLD+"/setlobbyarrow "+ ChatColor.WHITE+": Vous permet de définir le lobby de l'arène arrow.");
            }

            p.sendMessage(sb.toString());
            return true;
        }
    }
}
