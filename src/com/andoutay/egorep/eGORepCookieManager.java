package com.andoutay.egorep;

import java.util.HashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class eGORepCookieManager implements Listener
{
	private HashMap<String, Integer> rep;
	private HashMap<String, Integer> points;
	private HashMap<String, Long> times;
	private eGODBManager dbManager;
	
	public eGORepCookieManager()
	{
		//initialize rep based on database
		rep = new HashMap<String, Integer>();
		points = new HashMap<String, Integer>();
		times = new HashMap<String, Long>();
		dbManager = new eGODBManager();
	}
	
	private int modCookieForName(String repper, String recipient, int amt)
	{
		//can give rep to self
		//if (repper == recipient)
		//	return -2;
		
		//initialize newval to 1 in case recipient isn't in database
		int newval = 1;
		//get recipient's rep if they're in the hashmap
		if (rep.containsKey(recipient))
			newval = rep.get(recipient);
		
		//repper is not in points and should be added
		if (!points.containsKey(repper))
			points.put(repper, 3);
		
		//check if player needs new rep (or )
		if (times.containsKey(repper) && getSecondsLeft(repper) == 0 && points.get(repper) == 0)
			points.put(repper, 3);
		
		//apply the reputation modification if repper is not out of points
		if (points.get(repper) > 0)
		{
			newval+=amt;
			points.put(repper, points.get(repper) - 1);
			
			if (points.get(repper) <= 0 && ((times.containsKey(repper) && getSecondsLeft(repper) == 0) || !times.containsKey(repper)))
				times.put(repper, unixTimeNow());
		}
		//player is out of rep points to use
		else
		{
			return -1;
		}
		
		//rep should never be negative
		if (newval < 0)
			newval = 0;
		rep.put(recipient, newval);
		return rep.get(recipient);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		String name = event.getPlayer().getName();;
		rep.put(name, dbManager.getRep(name));
		points.put(name, dbManager.getRemPoints(name));
		times.put(name, dbManager.getTime(name));
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerQuit(PlayerQuitEvent event)
	{	
		String name = event.getPlayer().getName();
		dbManager.setAll(name, rep.get(name), points.get(name), times.get(name));
		rep.remove(name);
		points.remove(name);
		times.remove(name);
	}
	
	private long unixTimeNow()
	{
		return System.currentTimeMillis() / 1000L;
	}
	
	public int incrCookieForName(String repper, String recipient)
	{
		return modCookieForName(repper, recipient, 1);
	}
	
	public int decrCookieForName(String repper, String recipient)
	{
		return modCookieForName(repper, recipient, -1);
	}
	
	public int getCookieForName(String name)
	{
		if (!rep.containsKey(name))
			rep.put(name, 0);
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
		
		ans = 3600 - (unixTimeNow() - times.get(name));
		if (ans < 0)
			ans = 0;
		
		return ans;
	}
	
}
