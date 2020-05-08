package fr.risscrew.arrow_obsifight.utils;

import fr.risscrew.arrow_obsifight.listener.ListenerArrow;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.Timer;
import java.util.TimerTask;

public class UpdateTimer
{
    private Timer timer;
    private boolean isRunning = false;
    private Scoreboard board;

    public void start() {
        startScoreboard();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                ListenerArrow.updateTimer();
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, 1000);
        isRunning = true;
    }

    public void stop()
    {
        timer.cancel();
        isRunning = false;
        board.getObjective("kill").unregister();
    }

    public boolean isRunning() {
        return isRunning;
    }

    private void startScoreboard()
    {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();
        Objective kills = board.registerNewObjective("kill", "dummy");
        kills.setDisplaySlot(DisplaySlot.SIDEBAR);
        kills.setDisplayName("Kills Arrow");
        for (Player online : Bukkit.getServer().getOnlinePlayers())
        {
            online.setScoreboard(board);
        }
    }

    public void addScoreToPlayer(Player p)
    {
        Score score = board.getObjective("kill").getScore(p.getName());
        score.setScore(score.getScore()+1);
    }

}
