package com.andoutay.egorep;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.andoutay.egorep.eGORepConfig;

public final class eGORep extends JavaPlugin
{
	public static Logger log = Logger.getLogger("Minecraft");
	public static String chPref = ChatColor.GREEN + "[" + ChatColor.RESET + "Rep" + ChatColor.GREEN + "] " + ChatColor.RESET;
	public static String logPref = "[Rep] ";
	private eGORepCookieManager cManager;
	
	public void onLoad()
	{
		new eGORepConfig(this);
		cManager = new eGORepCookieManager(this);
	}
	
	@Override
	public void onEnable()
	{
		PluginManager pm = this.getServer().getPluginManager();
		
		//get stuff from db for all currently connected players - e.g. if it's a reload
		pm.registerEvents(cManager, this);
		
		//load config
		eGORepConfig.onEnable();
		
		//load the data for any connected 
		for (Player p: getServer().getOnlinePlayers())
			cManager.loadPlayer(p.getName());
		
		log.info(logPref + "enabled successfully!");
	}
	
	@Override
	public void onDisable()
	{
		//stop the syncing from the database from executing in new threads since we need to assure it finishes before the server stops
		eGORepConfig.useAsync = false;
		for (Player p: getServer().getOnlinePlayers())
		{
			log.info(p.getName() + " is still here");
			cManager.saveAndUnLoadPlayer(p.getName());
		}
		
		getServer().getScheduler().cancelTasks(this);
		log.info(logPref + "disabled");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		Player player = null;

		
		if (isUp(cmd, player, args))
			return execRep("up", sender, args);
		else if (isDown(cmd, player, args))
			return execRep("down", sender, args);
		else if (isSetRep(cmd, sender, args))
			return setRep(sender, args);
		else if (isHelp(cmd.getName(), args))
			return help(sender);
		else if (isVersion(cmd.getName(), args))
			return version(sender);
		else if (isCheck(cmd, player, args))
			return checkRep(sender, args);
		
		return false;
	}
	
	private static boolean isUp(Command cmd, Player p, String[] args)
	{
		return cmd.getName().equalsIgnoreCase("rep") && args.length == 2 && args[0].equalsIgnoreCase("up");
	}
	
	private static boolean isDown(Command cmd, Player p, String[] args)
	{
		return cmd.getName().equalsIgnoreCase("rep") && args.length == 2 && args[0].equalsIgnoreCase("down");
	}
	
	private static boolean isCheck(Command cmd, Player p, String[] args)
	{
		return cmd.getName().equalsIgnoreCase("rep") && (args.length == 0 || (args.length == 1 && !args[0].equalsIgnoreCase("up") && !args[0].equalsIgnoreCase("down") && !args[0].equalsIgnoreCase("help") && !args[0].equalsIgnoreCase("?") && !args[0].equalsIgnoreCase("set")) || (args.length >= 1 && args.length <= 2 && args[0].equalsIgnoreCase("check")));
	}
	
	private static boolean isSetRep(Command cmd, CommandSender s, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase("rep") && args.length == 3 && args[0].equalsIgnoreCase("set"))
			if (!(s instanceof ConsoleCommandSender))
			{
				s.sendMessage(chPref + "Only the console may use that command");
				return false;
			}
			else
				return true;
		else
			return false;
	}
	
	private static boolean isHelp(String name, String[] args)
	{
		return name.equalsIgnoreCase("rep") && args.length == 1 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?"));
	}
	
	private static boolean isVersion(String name, String[] args)
	{
		return name.equalsIgnoreCase("rep") && args.length == 1 && args[0].equalsIgnoreCase("version");
	}
	
	public void tellAllPlayers(Player recipient, String str, int amt)
	{
		String msg = ChatColor.GREEN + recipient.getDisplayName() + ChatColor.WHITE + "'s reputation " + str + " to " + amt;
		String personalMsg = ChatColor.GREEN + "Your" + ChatColor.RESET + " reputation was " + str + " to " + amt;
		for(Player p : getServer().getOnlinePlayers())
			if(p.hasPermission("egorep.show"))
				if (p.getName().equalsIgnoreCase(recipient.getName()))
					p.sendMessage(personalMsg);
				else
					p.sendMessage(msg);
	}

	private boolean execRep(String direction, CommandSender sender, String[] args)
	{
		int newamt = 0, oldamt;
		boolean hasDS = false;
		Player player = null, recipient = null;
		if (sender instanceof Player)
			player = (Player)sender;
		
		if (player != null)
		{
			if (!player.hasPermission("egorep.rep." + direction))
				return noAccess(player);
			
			if (player.hasPermission("egorep.ds"))
				hasDS = true;
		}
		
		recipient = getPlayerForName(args[1]);
		if (recipient == null)
			return playerNotFound(sender, args[1]);
		
		
		oldamt = cManager.getCookieForName(recipient.getName());
		if (direction == "up")
		{
			newamt = cManager.incrCookieForName(sender, recipient.getName(), hasDS);
			direction = "increased";
		}
		else if (direction == "down")
		{
			newamt = cManager.decrCookieForName(sender, recipient.getName(), hasDS);
			direction = "decreased";
		}
		
		if (newamt - oldamt != 0)
		{
			tellAllPlayers(recipient, direction, newamt);
			log.info(logPref + sender.getName() + " " + direction + " " + recipient.getName() + "'s reputation to " + newamt);
			sender.sendMessage(chPref + "You have " + cManager.getPointsLeft(sender.getName()) + " reputation points left");
		}
		
		return true;
	}
	
	private boolean checkRep(CommandSender sender, String[] args)
	{
		Player player = null, other = null;
		boolean offlinePlayer = false;
		if (sender instanceof Player)
			player = (Player)sender;
		
		String name;
		int rep = 0;
		if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("check")))
		{
			if (player != null && !player.hasPermission("egorep.rep.check.self"))
				return noAccess(player);
			name = sender.getName();
		}
		else if (args.length == 1 || (args.length == 2 && args[0].equalsIgnoreCase("check")))
		{
			String recipName = null;
			if (player != null && !player.hasPermission("egorep.rep.check.others"))
				return noAccess(player);
			
			if (args.length == 1)
				recipName = args[0];
			else
				recipName = args[1];
			
			other = getPlayerForName(recipName);
			if (other == null)
			{
				//If a player with the name of recipName has not been on the server, they will have a lastPlayed time of 0
				if (getServer().getOfflinePlayer(recipName).getLastPlayed() > 0)
				{
					name = getServer().getOfflinePlayer(recipName).getName();
					offlinePlayer = true;
				}
				else
					return playerNotFound(sender, recipName);
			}
			else
				name = other.getName();
		}
		else
			return false;
		
		rep = (offlinePlayer) ? cManager.dbManager.getRep(name) : cManager.getCookieForName(name);
		if (sender.getName().equalsIgnoreCase(name))
			sender.sendMessage(chPref + "You have a reputation of " + rep);
		else
			sender.sendMessage(chPref + ((other == null) ? name : other.getDisplayName()) + " has a reputation of " + rep);
		return true;
	}
	
	private boolean setRep(CommandSender sender, String[] args)
	{
		int repVal = Integer.parseInt(args[2]);
		Player recipient = getPlayerForName(args[1]);
		if (recipient == null)
			return playerNotFound(sender, args[1]);
		
		cManager.setCookieForName(recipient.getName(), repVal);
		
		sender.sendMessage(logPref + "Set reputation of " + recipient.getName() + " to " + repVal);
		
		return true;
	}
	
	private boolean help(CommandSender s)
	{
		s.sendMessage(chPref + "Help:");
		s.sendMessage("Use /rep up <username> and /rep down <username> to give and take other players' reputation");
		s.sendMessage("Use /rep <username> or /rep check <username> to check the reputation of other players");
		s.sendMessage("Use /rep or /rep check to check your own reputation");
		s.sendMessage("Remember that Dedicated Supporters get two extra rep points to use every " + parseTime((long)eGORepConfig.refreshSecs));
		
		return true;
	}
	
	private boolean version(CommandSender s)
	{
		PluginDescriptionFile pdf = getDescription();
		String pref = null;
		if ((s instanceof Player) && ((Player)s).hasPermission("egorep.version"))
			pref = chPref;
		else if (s instanceof ConsoleCommandSender)
			pref = logPref;
		
		if (pref != null)
			s.sendMessage(pref + "Current version: " + pdf.getVersion());
		else
			return noAccess(s);
		return true;
	}
	
	private boolean playerNotFound(CommandSender sender, String name)
	{
		sender.sendMessage(chPref + "Player matching " + name + " not found");
		return true;
	}
	
	private boolean noAccess(Player player)
	{
		player.sendMessage(ChatColor.RED + "You do not have access to that command");
		return true;
	}
	
	private boolean noAccess(CommandSender s)
	{
		if (s instanceof Player)
			return noAccess((Player)s);
		s.sendMessage("You do not have access to that command");
		return true;
	}
	
	private Player getPlayerForName(String partial)
	{
		Player player = null;
		boolean found = false, foundMult = false;
		
		player = getServer().getPlayer(partial);
		
		if (player == null)
			for (Player p: getServer().getOnlinePlayers())
				if (p.getDisplayName().contains(partial))
				{
					if (found)
					{
						foundMult = true;
						break;
					}
					player = p;
					found = true;
				}
		
		if (foundMult)
			player = null;
		
		return player;
	}
	
	public static String parseTime(Long timestamp)
	{
		String h, m, s;
		long hour, min, sec;
		hour = timestamp / 3600;
		min = (timestamp - hour * 3600) / 60;
		sec = timestamp- hour * 3600 - min * 60;
		h = (hour == 1) ? "hour" : "hours";
		m = (min == 1) ? "min" : "mins";
		s = (sec == 1) ? "sec" : "secs";
		return hour + " " + h + " " + min + " " + m + " " + sec + " " + s;
	}
}
