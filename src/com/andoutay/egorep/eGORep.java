package com.andoutay.egorep;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
//import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.andoutay.egorep.eGORepConfig;

public final class eGORep extends JavaPlugin
{
	public static Logger log = Logger.getLogger("Minecraft");
	public static String chPref = ChatColor.GREEN + "[" + ChatColor.RESET + "Rep" + ChatColor.GREEN + "] " + ChatColor.RESET;
	public static String logPref = "[eGORep] ";
	private eGORepCookieManager cManager = new eGORepCookieManager();
	//[green]Playername[/green] reputation increased to [green]#[/green]
	//[Rep] You have # reputation points left to use this hour
	
	
	@Override
	public void onEnable()
	{
		
		//new eGORepConfig(this);
		//eGORepConfig.onEnable();
		//PluginDescriptionFile pdf = this.getDescription();
		PluginManager pm = this.getServer().getPluginManager();
		
		//get stuff from db for all currently connected players - e.g. if it's a reload
		pm.registerEvents(cManager, this);
		
		for (Player p: getServer().getOnlinePlayers())
			cManager.loadPlayer(p.getName());
		
		log.info(logPref + "enabled successfully!");
	}
	
	@Override
	public void onDisable()
	{
		for (Player p: getServer().getOnlinePlayers())
			cManager.saveAndUnLoadPlayer(p.getName());
		
		getServer().getScheduler().cancelTasks(this);
		log.info(logPref + "disabled");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		Player player = null;
		if (sender instanceof Player)
			player = (Player)sender;
		
		if (isUp(cmd, player, args))
			return execRep("up", sender, args);
		else if (isDown(cmd, player, args))
			return execRep("down", sender, args);
		else if (isCheck(cmd, player, args))
			return checkRep(player, args);
		
		return false;
	}
	
	private static boolean isUp(Command cmd, Player p, String[] args)
	{
		return ((p != null && p.hasPermission("egorep.rep.up")) || p == null) && cmd.getName().equalsIgnoreCase("rep") && args.length == 2 && args[0].equalsIgnoreCase("up");
	}
	
	private static boolean isDown(Command cmd, Player p, String[] args)
	{
		return ((p != null && p.hasPermission("egorep.rep.down")) || p == null) && cmd.getName().equalsIgnoreCase("rep") && args.length == 2 && args[0].equalsIgnoreCase("down");
	}
	
	public static boolean isCheck(Command cmd, Player p, String[] args)
	{
		return ((p != null && p.hasPermission("egorep.rep.check")) || p == null) && cmd.getName().equalsIgnoreCase("rep") && args.length >= 1 && args.length <= 2 && args[0].equalsIgnoreCase("check");
	}
	
	public void tellAllPlayers(Player recipient, String str, int amt)
	{
		String msg = ChatColor.GREEN + recipient.getDisplayName() + ChatColor.WHITE + "'s reputation " + str + " to " + amt;
		String personalMsg = "Your reputation was " + str + " to " + amt;
		for(World w : getServer().getWorlds()){
		    for(Player p : w.getPlayers()){
		        if(p.hasPermission("egorep.show"))
		        	if (p.getName().equalsIgnoreCase(recipient.getName()))
		        		p.sendMessage(personalMsg);
		        	else
		        		p.sendMessage(msg);
		    }
		}
	}
	
	private boolean execRep(String direction, CommandSender sender, String[] args)
	{
		int newamt = -1;
		Player recipient = null;
		
		//get cookie rep level for args[1], ++ it
		recipient = getServer().getPlayer(args[1]);
		if (recipient == null)
			return playerNotFound(sender, args[1]);
		
		if (direction == "up")
		{
			newamt = cManager.incrCookieForName(sender.getName(), recipient.getName());
			direction = "increased";
		}
		else if (direction == "down")
		{
			newamt = cManager.decrCookieForName(sender.getName(), recipient.getName());
			direction = "decreased";
		}
		
		if (newamt >= 0)
		{
			tellAllPlayers(recipient, direction, newamt);
			log.info(logPref + sender.getName() + " " + direction + " " + recipient.getName() + "'s reputation to " + newamt);
			sender.sendMessage(chPref + "You have " + cManager.getPointsLeft(sender.getName()) + " reputation points left to use this hour");
		}
		else if (newamt == -1)
			sender.sendMessage(chPref + "You are out of reputation points to use\nYou must wait " + parseTime(cManager.getSecondsLeft(sender.getName())) + " before using /rep again");
		else if (newamt == -2)
			sender.sendMessage(chPref + "You may not modify your own reputation");
		
		return true;
	}
	
	private boolean checkRep(CommandSender sender, String[] args)
	{
		int rep = 0;
		String name;
		Player recipient;
		if (args.length == 1)
			name = sender.getName();
		else if (args.length == 2)
		{
			recipient = getServer().getPlayer(args[1]);
			if (recipient == null)
				return playerNotFound(sender, args[1]);
			name = recipient.getName();
		}
		else
			return false;
		
		rep = cManager.getCookieForName(name);
		if (sender.getName().equalsIgnoreCase(name))
			sender.sendMessage(chPref + "You have a reputation of " + rep);
		else
			sender.sendMessage(chPref + name + " has a reputation of " + rep);
		return true;
	}
	
	private String parseTime(Long timestamp)
	{
		String m, s;
		long min, sec;
		min = timestamp / 60;
		sec = timestamp - min * 60;
		m = (min == 1) ? "min" : "mins";
		s = (sec == 1) ? "sec" : "secs";
		return min + " " + m + " " + sec + " " + s;
	}
	
	private boolean playerNotFound(CommandSender sender, String name)
	{
		sender.sendMessage(chPref + "Player matching " + name + " not found");
		return true;
	}
}
