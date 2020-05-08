package fr.risscrew.arrow_obsifight.listener;

import fr.risscrew.arrow_obsifight.Main;
import fr.risscrew.arrow_obsifight.command.admin.CommandSetLobbyArrow;
import fr.risscrew.arrow_obsifight.command.animator.CommandStopArrow;
import fr.risscrew.arrow_obsifight.utils.UpdateTimer;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ListenerArrow implements Listener
{
    private static final Map<Player, Integer> cooldownPlayer = new HashMap<>();
    private static final Map<Player, Integer> killingSpree = new HashMap<>();
    private static final Map<Player, Integer> arrowShield = new HashMap<>();
    private static final List<Player> disconnectedPlayers = new ArrayList<>();
    private static final String prefix = ChatColor.GOLD+"[ArrowObsifight] "+ChatColor.RESET;
    private final UpdateTimer timer;
    private final Map<UUID, Player> arrowFromPlayer = new HashMap<>();
    private final Main main;
    private final CommandSetLobbyArrow setLobbyArrow;
    private Map<Player, Integer> score = new HashMap<>();
    private static List<Player> playingPlayers = new ArrayList<>();

    public ListenerArrow(UpdateTimer timer, Main main, CommandSetLobbyArrow setLobbyArrow)
    {
        this.timer = timer;
        this.main = main;
        this.setLobbyArrow = setLobbyArrow;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        if (!timer.isRunning()) return;
        if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE))
        {
            if (arrowFromPlayer.containsKey(event.getDamager().getUniqueId()) && event.getEntity().getType().equals(EntityType.PLAYER))
            {
                Player damager = arrowFromPlayer.get(event.getDamager().getUniqueId());
                Player damaged = (Player) event.getEntity();

                if (damager == damaged) return;
                if (playingPlayers.contains(damaged) && playingPlayers.contains(damager))
                {
                    event.setDamage(0.0);
                    event.setCancelled(true);
                    if(arrowShield.getOrDefault(damaged,0) >= 1) playerShielded(damaged, damager);
                    else playerKilled(damaged, damager);
                }
            }
        }
        else
        {
            if(event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK))
            {
                if(event.getEntity().getType().equals(EntityType.PLAYER) && event.getDamager().getType().equals(EntityType.PLAYER))
                {
                    if(playingPlayers.contains((Player)event.getEntity()) && playingPlayers.contains((Player)event.getDamager()))
                    {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event)
    {
        if(!timer.isRunning()) return;
        if(event.getEntity().getType().equals(EntityType.ARROW))
        {
            if((event.getEntity()).getShooter().getType().equals(EntityType.PLAYER))
            {
                if (playingPlayers.contains((Player)event.getEntity().getShooter())) arrowFromPlayer.put(event.getEntity().getUniqueId(), (Player)(event.getEntity().getShooter()));
                event.getEntity().setMetadata("no_pickup", new FixedMetadataValue(main, true));
            }
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) { if(event.getItem().hasMetadata("no_pickup")) event.setCancelled(true); }

    @EventHandler
    public void onEntityShootBowEvent(EntityShootBowEvent event)
    {
        if (!timer.isRunning()) return;
        if(event.getEntity().getType().equals(EntityType.PLAYER))
        {
            Player player = (Player) event.getEntity();
            if (playingPlayers.contains(player)) player.getItemInHand().setDurability((short)(player.getItemInHand().getDurability() - 1));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (!timer.isRunning()) return;
        try { if (event.getClickedInventory().getHolder() instanceof Player) if(playingPlayers.contains((Player) event.getClickedInventory().getHolder())) event.setCancelled(true); }
        catch (NullPointerException ignored) {}
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (!timer.isRunning()) return;
        if (playingPlayers.contains(event.getPlayer()))
        {
            if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            {
                if (event.getItem() != null)
                {
                    if (!(event.getItem().getType().equals(Material.BOW))) {
                        if (event.getPlayer().getInventory().getItem(1) == null)
                            event.getPlayer().sendMessage(prefix + "Il vous reste " + cooldownPlayer.get(event.getPlayer()) + " secondes avant de recevoir une flèche.");
                        else if (event.getPlayer().getInventory().getItem(1).getAmount() < 2)
                            event.getPlayer().sendMessage(prefix + "Il vous reste " + cooldownPlayer.get(event.getPlayer()) + " secondes avant de recevoir une flèche.");
                        else event.getPlayer().sendMessage(prefix + "Vous avez le maximum de flèches.");
                    }
                }
                else {
                    event.getPlayer().sendMessage(prefix + "Il vous reste " + cooldownPlayer.get(event.getPlayer()) + " secondes avant de recevoir une flèche.");
                }
            }
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event)
    {
        if (!timer.isRunning()) return;
        if (event.getSource().getHolder() instanceof Player) if(playingPlayers.contains((Player) event.getSource().getHolder())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event)
    {
        if (!timer.isRunning()) return;
        if(playingPlayers.contains(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) { if (timer.isRunning() && playingPlayers.contains(event.getPlayer())) disconnectedPlayers.add(event.getPlayer()); }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) { if (timer.isRunning() && playingPlayers.contains(event.getPlayer())) disconnectedPlayers.add(event.getPlayer()); }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if (timer.isRunning() && disconnectedPlayers.contains(event.getPlayer()))
        {
            Player player = event.getPlayer();
            event.getPlayer().teleport(setLobbyArrow.getLobbyLocation());
            player.getInventory().clear();
            player.updateInventory();
            if (playingPlayers.contains(player)) playingPlayers.remove(player);
            if (killingSpree.containsKey(player)) killingSpree.put(player, 0);
        }
    }

    private void playerShielded(Player damaged, Player damager)
    {
        arrowShield.put(damaged, arrowShield.get(damaged)-1);
        damaged.playSound(damaged.getLocation(), Sound.ITEM_BREAK,1,1);
        damaged.sendMessage(prefix+"Vous avez résisté à une flèche grâce à votre bouclier, il lui reste "+arrowShield.get(damaged)+" charges.");

        damager.playSound(damager.getLocation(), Sound.ANVIL_LAND,1,1);
        damager.sendMessage(prefix+"Votre adversaire a résisté à la flèche.");
    }

    private void playerKilled(Player damaged, Player damager)
    {
        ItemStack arrow = damager.getInventory().getItem(1);

        damaged.teleport(setLobbyArrow.getLobbyLocation());
        damaged.getInventory().clear();
        damaged.updateInventory();
        playingPlayers.remove(damaged);
        killingSpree.put(damaged, 0);
        damaged.playSound(damaged.getLocation(), Sound.HURT_FLESH,1 ,1);
        damaged.sendMessage(prefix+ChatColor.RED+"Vous vous êtes fait tué par "+ChatColor.AQUA + damager.getName());

        if (arrow == null) damager.getInventory().setItem(1, new ItemStack(Material.ARROW,1));
        else arrow.setAmount(arrow.getAmount()+1);
        damager.updateInventory();
        addPointToPlayer(damager);
        timer.addScoreToPlayer(damager);
        killingSpree.put(damager, killingSpree.getOrDefault(damager, 0)+1);
        damager.playSound(damager.getLocation(), Sound.SUCCESSFUL_HIT,1,1);
        damager.sendMessage(prefix+"Vous avez tué " +ChatColor.AQUA+ damaged.getName());

        Bukkit.broadcastMessage(ChatColor.AQUA+damaged.getDisplayName() +ChatColor.WHITE+ " a été tué par " +ChatColor.AQUA+ damager.getDisplayName());

        if(playingPlayers.size() == 1)
        {
            timer.stop();
            stopEvent();
        }
        else
        {
            giveKillSpreeReward(damager);
        }

    }

    private void giveKillSpreeReward(Player damager)
    {
        int kSpree = killingSpree.get(damager);
        damager.sendMessage(prefix+"Vous êtes en killing spree de "+ChatColor.AQUA+kSpree+ChatColor.RESET+" kill(s).");
        if (kSpree%5 == 0)
        {
            arrowShield.put(damager, arrowShield.getOrDefault(damager,0)+1);
            damager.sendMessage("Vous gagnez un shield d'une arrow.");
        }
        else
        {
            damager.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (kSpree%5)*5*20,((int)Math.ceil(kSpree/5D))-1));
            damager.sendMessage("Vous gagnez Speed "+(int)Math.ceil(kSpree/5D)+ " pour " + (kSpree%5)*5 + " secondes.");
        }
    }

    public static void updateTimer()
    {
        for (Map.Entry<Player, Integer> entry : cooldownPlayer.entrySet())
        {
            if(!playingPlayers.contains(entry.getKey())) return;

            int cdplayer = entry.getValue();
            Player p = entry.getKey();
            ItemStack arrow = p.getInventory().getItem(1);

            if (cdplayer != 0 && arrow == null) cooldownPlayer.put(p, --cdplayer);
            else if (cdplayer !=0 && arrow.getAmount()!=2) cooldownPlayer.put(p, --cdplayer);
            else if (arrow == null)
            {
                p.getInventory().setItem(1, new ItemStack(Material.ARROW,1));
                cooldownPlayer.put(p, 10);
            }
            else if (arrow.getAmount()<2)
            {
                arrow.setAmount(arrow.getAmount()+1);
                cooldownPlayer.put(p, 10);
            }
        }

    }

    public void stopEvent()
    {
        StringBuilder score = new StringBuilder();
        Map<Player, Integer> scores = getScore();

        for (int j = 1; j < 6; j++)
        {
            if (j == 1) score.append("1er: ");
            if (j == 2) score.append("2ème: ");
            if (j == 3) score.append("3ème: ");

            List<Player> maxPlayers = new ArrayList<>();
            int scoreOfMaxPlayer = 0;
            for (Map.Entry<Player, Integer> entry : scores.entrySet())
            {
                if (entry.getValue()>=scoreOfMaxPlayer)
                {
                    if (entry.getValue()>scoreOfMaxPlayer) maxPlayers.clear();
                    scoreOfMaxPlayer = entry.getValue();
                    maxPlayers.add(entry.getKey());
                }
            }

            for(Player player : maxPlayers)
            {
                score.append(ChatColor.AQUA).append(player.getName()).append(ChatColor.RESET).append(", ");
                scores.remove(player);
            }
            score.replace(score.length()-2, score.length(), "");
            score.append(": ").append(scoreOfMaxPlayer).append(" point(s).\n");
            if (scores.isEmpty()) break;
        }
        Bukkit.broadcastMessage(ChatColor.GOLD+"[ArrowObsifight] "+ChatColor.RESET+"Fin de la partie ! Scores:\n" + score.toString());

        for (Map.Entry<Player, Integer> entry : getScore().entrySet()) entry.getKey().sendMessage(ChatColor.GOLD+"[ArrowObsifight] "+ChatColor.RESET+"Vous avez fait "+entry.getValue()+" point(s).");
        for (Player p : getPlayingPlayers())
        {
            p.teleport(setLobbyArrow.getLobbyLocation());
            p.getInventory().clear();
            p.updateInventory();
        }
        flushPlayingPlayers();
        flushPoints();
    }
    public List<Player> getPlayingPlayers() { return playingPlayers; }

    public void addPlayingplayers(Player player) { playingPlayers.add(player); }

    public void setPlayingPlayers(List<Player> playingPlayers) { ListenerArrow.playingPlayers = playingPlayers; }

    public void flushPlayingPlayers() { playingPlayers.clear(); }

    private int getPlayerScore(Player player) { return score.getOrDefault(player, 0); }

    public Map<Player, Integer> getScore() { return score; }

    private void addPointToPlayer(Player player) { score.put(player, getPlayerScore(player)+1); }

    public void flushPoints() { score.clear(); }

    public void addCooldownToPlayer(Player p) { cooldownPlayer.put(p, 10); }

}
