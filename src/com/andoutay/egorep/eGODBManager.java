package com.andoutay.egorep;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.scheduler.BukkitScheduler;

public class eGODBManager {
	private String table;
	private eGORep plugin;
	private BukkitScheduler scheduler;
	
	public eGODBManager(eGORep plugin)
	{
		this.plugin = plugin;
		this.scheduler = plugin.getServer().getScheduler();
		table = "egorep";
	}
	
	public static Connection getSQLConnection()
	{
		Connection con;
		
		try
		{
			con = DriverManager.getConnection(eGORepConfig.sqlURL, eGORepConfig.sqlDBName, eGORepConfig.sqlPassword);
			
			return con;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void setAll(final String name, final int rep, final int points, final Long timestamp)
	{
		if (eGORepConfig.useAsync)
		{
			scheduler.runTaskAsynchronously(plugin, new Runnable() {
				public void run() {
					setRep(name, rep);
				}
			});
			scheduler.runTaskAsynchronously(plugin, new Runnable() {
				public void run() {
					setRemPoints(name, points);
				}
			});
			scheduler.runTaskAsynchronously(plugin, new Runnable() {
				public void run() {
					setTime(name, timestamp);
				}
			});
		}
		else
		{
			setRep(name, rep);
			setRemPoints(name, points);
			setTime(name, timestamp);
		}
	}

	public void setVal(String dbField, String name, long val)
	{
		Connection con = getSQLConnection();
		PreparedStatement stmt = null;
		String q = "UPDATE " + table + " SET " + dbField + " = ? WHERE `IGN` = ?";
		int success = 0;
		
		try
		{
			stmt = con.prepareStatement(q);
			stmt.setLong(1, val);
			stmt.setString(2, name);
			
			success = stmt.executeUpdate();
			stmt.close();
		}
		catch (SQLException e)
		{
			//e.printStackTrace();
		}
		
		if (success < 1)
		{
			eGORep.log.info("Adding " + name +" to the database");
			if (dbField.equalsIgnoreCase("points"))
				val = 3;
			q = "INSERT INTO " + table + " (`IGN`, `" + dbField + "`) VALUES (?, ?)";
			try
			{
				stmt = con.prepareStatement(q);
				stmt.setString(1, name);
				stmt.setLong(2,  val);
				
				success = stmt.executeUpdate();
				stmt.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public Long getLong(String dbField, String name)
	{
		Connection con = getSQLConnection();
		PreparedStatement stmt = null;
		String q = "SELECT " + dbField + " FROM " + table + " WHERE `IGN` = ?";
		ResultSet result;
		Long ans = (long)0;
		
		try
		{
			stmt = con.prepareStatement(q);
			stmt.setString(1, name);
			
			result = stmt.executeQuery();
			if (result.next())
				ans = result.getLong(dbField);
			stmt.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return ans;
	}
	
	public int getInt(String dbField, String name)
	{
		Connection con = getSQLConnection();
		PreparedStatement stmt = null;
		String q = "SELECT " + dbField + " FROM " + table + " WHERE `IGN` = ?";
		ResultSet result;
		int ans = 0, success = 0;
		
		try
		{
			stmt = con.prepareStatement(q);
			stmt.setString(1, name);
			
			result = stmt.executeQuery();
			if (result.next())
				success = 1;
			ans = result.getInt(dbField);
			stmt.close();
		}
		catch (SQLException e)
		{
			//e.printStackTrace();
		}
		
		if (success < 1)
		{
			eGORep.log.info("Adding " + name +" to the database");
			int insVal = 0;
			if (dbField.equalsIgnoreCase("points"))
				insVal = 3;
			
			q = "INSERT INTO " + table + " (`IGN`, `" + dbField + "`) VALUES (?, ?)";
			try
			{
				stmt = con.prepareStatement(q);
				stmt.setString(1, name);
				stmt.setLong(2, insVal);
				
				success = stmt.executeUpdate();
				stmt.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		
		return ans;
	}

	public void setRep(final String name, final int rep)
	{
		if (eGORepConfig.useAsync)
		{
			scheduler.runTaskAsynchronously(plugin, new Runnable() {
				public void run() {
					setVal("rep", name, rep);					
				}
			});
		}
		else
			setVal("rep", name, rep);
	}
	
	public void setRemPoints(final String name, final int points)
	{
		if (eGORepConfig.useAsync)
		{
			scheduler.runTaskAsynchronously(plugin, new Runnable() {
				public void run() {
					setVal("points", name, points);					
				}
			});
		}
		else
			setVal("points", name, points);
	}

	public void setTime(final String name, final Long timestamp)
	{
		if (eGORepConfig.useAsync)
		{
			scheduler.runTaskAsynchronously(plugin, new Runnable() {
				public void run() {
					setVal("time", name, timestamp);					
				}
			});
		}
		else
			setVal("time", name, timestamp);
	}

	public int getRep(String name) {
		return getInt("rep", name);
	}

	public int getRemPoints(String name) {
		return getInt("points", name);
	}

	public Long getTime(String name) {
		// TODO get timestamp from db and return it
		return getLong("time", name);
	}
}

/*
 * user: mcPluginTest 
 * pass: 29cXaC4YuZFb4yLa
 * 
 * Code to create table if necessary

CREATE TABLE  `mcPluginTest`.`egorep` (
`IGN` VARCHAR( 128 ) NOT NULL ,
`rep` INT NOT NULL ,
`points` INT NOT NULL ,
`time` BIGINT NOT NULL ,
PRIMARY KEY (  `IGN` )
) ENGINE = MYISAM ;

CREATE TABLE IF NOT EXISTS `egorep` (
  `IGN` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `rep` int(11) NOT NULL DEFAULT '0',
  `points` int(11) NOT NULL DEFAULT '3',
  `time` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`IGN`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
*/