 package com.andoutay.egorep;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class eGORepCookieManager implements Listener
{
	private HashMap<String, Double> rep;
	private HashMap<String, Integer> points;
	private HashMap<String, Long> times;
	public eGODBManager dbManager;
	
	public eGORepCookieManager(eGORep plugin)
	{
		//initialize rep based on database
		rep = new HashMap<String, Double>();
		points = new HashMap<String, Integer>();
		times = new HashMap<String, Long>();
		dbManager = new eGODBManager(plugin);
	}
	
	private double modCookieForName(CommandSender sender, String recipient, boolean hasDS, double amt)
	{
		int totRepPoints = eGORepConfig.repPerUnit + ((hasDS) ? 2 : 0);
		String repper = sender.getName();
		//can't give rep to self
		if (repper == recipient)
			sender.sendMessage(eGORep.chPref + "You may not modify your own reputation");
		else
		{
			amt = correctAmt(amt, repper, recipient);
			if (amt == 0)
				sender.sendMessage(eGORep.chPref + ChatColor.RED + "You must modify reputation of other players before modifying " + recipient + "'s reputation that way");
			
			//initialize newval to 1 in case recipient isn't in database
			double newval = 1;

			//get recipient's rep if they're in the hashmap
			if (rep.containsKey(recipient))
				newval = rep.get(recipient);

			//repper is not in points and should be added
			if (!points.containsKey(repper))
				points.put(repper, eGORepConfig.repPerUnit);

			//check if player needs new rep
			if (getSecondsLeft(repper) == 0 && points.get(repper) <= 0)
				points.put(repper, totRepPoints);

			//apply the reputation modification if repper is not out of points
			if (points.get(repper) > 0)
			{
				newval = eGORepUtils.round1Decimal(newval + amt);
				points.put(repper, points.get(repper) - 1);

				if (points.get(repper) == (totRepPoints - 1) && getSecondsLeft(repper) == 0)
					times.put(repper, unixTimeNow());
			}
			//player is out of rep points to use
			else
				sender.sendMessage(eGORep.chPref + "You are out of reputation points to use\nYou must wait " + eGORepUtils.parseTime(getSecondsLeft(repper)) + " before using /rep up or down again");

			rep.put(recipient, newval);
		}
		return rep.get(recipient);
	}
	
	private double correctAmt(double amt, String repper, String recipient)
	{
		String direction = (amt == 1 ? "up" : "down");
		String lines[] = eGODBManager.getLogEntry(repper, direction, 0).split("\n");
		double dec = - 0.1 * amt; 
		
		for (String line : lines) 
			if (line.contains("r" + repper) && line.contains(" " + recipient))
				amt += dec;
		
		return eGORepUtils.round1Decimal(amt);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		loadPlayer(event.getPlayer().getName());
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		saveAndUnLoadPlayer(event.getPlayer().getName());
	}
	
	public void loadPlayer(String name)
	{
		rep.put(name, dbManager.getRep(name));
		points.put(name, dbManager.getRemPoints(name));
		times.put(name, dbManager.getTime(name));
	}
	
	public void saveAndUnLoadPlayer(String name)
	{
		dbManager.setAll(name, rep.get(name), points.get(name), times.get(name));
		rep.remove(name);
		points.remove(name);
		times.remove(name);
	}
	
	private long unixTimeNow()
	{
		return System.currentTimeMillis() / 1000L;
	}
	
	public double incrCookieForName(CommandSender sender, String recipient, boolean hasDS)
	{
		return modCookieForName(sender, recipient, hasDS, 1);
	}
	
	public double decrCookieForName(CommandSender sender, String recipient, boolean hasDS)
	{
		return modCookieForName(sender, recipient, hasDS, -1);
	}
	
	public void setCookieForName(String name, double val)
	{
		rep.put(name, val);
	}
	
	public double getCookieForName(String name)
	{
		if (!rep.containsKey(name)) rep.put(name, 0.0);
		return rep.get(name);
	}
	
	public int getPointsLeft(String name)
	{
		if (!points.containsKey(name))
			points.put(name, 3);
		return points.get(name);
	}
	
	public long getSecondsLeft(String name)
	{
		long ans = 0;
		if (!times.containsKey(name))
			times.put(name, (long)0);
		
		ans = eGORepConfig.refreshSecs - (unixTimeNow() - times.get(name));
		if (ans < 0)
			ans = 0;
		
		return ans;
	}
}
