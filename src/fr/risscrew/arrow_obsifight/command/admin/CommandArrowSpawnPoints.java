package fr.risscrew.arrow_obsifight.command.admin;

import fr.risscrew.arrow_obsifight.command.animator.CommandStartArrow;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandArrowSpawnPoints implements CommandExecutor
{

    private final CommandStartArrow joinArrow;
    private final static String prefix = ChatColor.GOLD+"[ArrowObsifight] "+ChatColor.RESET;
    private final static String helpMessage = ChatColor.YELLOW+"--------------" +ChatColor.WHITE+" Aide ASP"+ChatColor.YELLOW+"--------------"+ChatColor.RESET+
            "\n\n"+ ChatColor.GOLD+"/asp "+ChatColor.DARK_AQUA+"help "+ ChatColor.WHITE+" : Vous affiche cette liste." +
            "\n"+ ChatColor.GOLD+"/asp "+ChatColor.DARK_AQUA+"add "+ChatColor.WHITE+": Ajoute un nouveau point de spawn." +
            "\n"+ ChatColor.GOLD+"/asp "+ChatColor.DARK_AQUA+"list "+ChatColor.WHITE+": Vous affiche une liste des spawnpoints." +
            "\n"+ ChatColor.GOLD+"/asp "+ChatColor.DARK_AQUA+"modify "+ChatColor.AQUA+"<n°>"+ChatColor.WHITE+" : Modifiez un point de spawn existant." +
            "\n"+ ChatColor.GOLD+"/asp "+ChatColor.DARK_AQUA+"remove "+ChatColor.AQUA+"<n°>"+ChatColor.WHITE+" : Enlevez un point de spawn";

    public CommandArrowSpawnPoints(CommandStartArrow joinArrow)
    {
        this.joinArrow = joinArrow;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args)
    {
        if (commandSender instanceof ConsoleCommandSender)
        {
            commandSender.sendMessage("Indisponible depuis la console.");
            return true;
        }

        Player p = (Player) commandSender;
        if (args.length < 1) p.sendMessage(helpMessage);
        else
        {
            switch (args[0])
            {
                case "help":
                case "?":
                    p.sendMessage(helpMessage);
                    break;

                case "add":
                    joinArrow.addSpawnLocation(p.getLocation());
                    p.sendMessage(prefix+"Vous avez ajouté un nouveau spawnpoint (Nombre de points : " + joinArrow.sizeOfSpawnLocations() + ").");
                    break;

                case "list":
                    int i = 0;

                    StringBuilder sb = new StringBuilder();
                    sb.append("Liste des spawnpoints: \n");

                    for (Location location : joinArrow.getSpawnLocation())
                    {
                        sb.append("    ").append(++i).append(": X:").append(location.getBlockX()).append(", Y:").append(location.getBlockY()).append(", Z:").append(location.getBlockZ()).append(" à +").append(Math.round(p.getLocation().distance(location))).append(" blocs.\n");
                    }
                    p.sendMessage(sb.toString());
                    break;

                case "remove":
                    if(args.length < 2)
                    {
                        p.sendMessage(prefix+"Merci d'indiquer le point que vous voulez supprimer (/arrowspawnpoints list).");
                    }
                    else
                    {
                        try
                        {
                            int index = Integer.parseInt(args[1]);
                            List<Location> locations = joinArrow.getSpawnLocation();
                            if (index <= 0 || index > locations.size()+1)
                            {
                                p.sendMessage(prefix+"Merci d'entrer un nombre correct (/arrowspawnpoints list).");
                                return true;
                            }
                            locations.remove(index-1);
                            joinArrow.setSpawnLocation(locations);
                            p.sendMessage(prefix+"Le spawnpoint a bien été enlevé !");
                        }
                        catch (NumberFormatException nfe)
                        {
                            p.sendMessage(prefix+"Merci d'entrer un nombre correct (/arrowspawnpoints list).");
                            return true;
                        }
                    }
                    break;

                case "modify":
                    if(args.length < 2)
                    {
                        p.sendMessage(prefix+"Merci d'indiquer le point que vous voulez modifier (/arrowspawnpoints list).");
                    }
                    else
                    {
                        try
                        {
                            int index = Integer.parseInt(args[1]);
                            List<Location> locations = joinArrow.getSpawnLocation();

                            if (index <= 0 || index > locations.size()+1)
                            {
                                p.sendMessage(prefix+"Merci d'entrer un nombre correct (/arrowspawnpoints list).");
                                return true;
                            }

                            locations.set(index-1, p.getLocation());
                            joinArrow.setSpawnLocation(locations);
                            p.sendMessage(prefix+"Le spawnpoint a bien été modifié !");
                        }
                        catch (NumberFormatException nfe)
                        {
                            p.sendMessage(prefix+"Merci d'entrer un nombre correct (/arrowspawnpoints list).");
                            return true;
                        }
                    }
                    break;

                default:
                    p.sendMessage(prefix+"Argument inconnu, faites /arrowspawnpoints help.");
                    break;
            }
        }
        return true;

    }
}
