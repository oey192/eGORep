package com.andoutay.egorep;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;

import org.bukkit.scheduler.BukkitScheduler;

public class eGODBManager
{
	private eGORep plugin;
	private BukkitScheduler scheduler;
	
	public eGODBManager(eGORep plugin)
	{
		this.plugin = plugin;
		this.scheduler = plugin.getServer().getScheduler();
	}
	
	public static Connection getSQLConnection()
	{
		Connection con;
		
		try
		{
			con = DriverManager.getConnection(eGORepConfig.sqlURL, eGORepConfig.sqlUser, eGORepConfig.sqlPassword);
			return con;
		}
		catch (SQLSyntaxErrorException e)
		{
			eGORep.log.info(eGORep.logPref + "Falling back on DB URL w/out database");
			
			try
			{
				con = DriverManager.getConnection(eGORepConfig.shortSQLURL, eGORepConfig.sqlUser, eGORepConfig.sqlPassword);
				return con;
			}
			catch (SQLException f)
			{
				f.printStackTrace();
			}
		}
		catch (SQLException e)
		{
			
		}
		
		return null;
	}
	
	public void setAll(final String name, final int rep, final int points, final Long timestamp)
	{
			setRep(name, rep);
			setRemPoints(name, points);
			setTime(name, timestamp);
	}

	public void setVal(String dbField, String name, long val)
	{
		Connection con = getSQLConnection();
		PreparedStatement stmt = null;
		String q = "UPDATE " + eGORepConfig.sqlTableName + " SET " + dbField + " = ? WHERE `IGN` = ?";
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
			
		}
		catch (NullPointerException e)
		{
			if (dbField.equalsIgnoreCase("rep")) eGORep.log.severe(eGORep.logPref + "Could not connect to database. Please check that the MySQL Server is running");
			success = 1;
		}
		
		if (success < 1)
		{
			fixFringeCases(con, name, dbField);
		}
	}
	
	public Long getLong(String dbField, String name)
	{
		Connection con = getSQLConnection();
		PreparedStatement stmt = null;
		String q = "SELECT " + dbField + " FROM " + eGORepConfig.sqlTableName + " WHERE `IGN` = ?";
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
			
		}
		catch (NullPointerException e)
		{
			
		}
		
		return ans;
	}
	
	public int getInt(String dbField, String name)
	{
		Connection con = getSQLConnection();
		PreparedStatement stmt = null;
		String q = "SELECT " + dbField + " FROM " + eGORepConfig.sqlTableName + " WHERE `IGN` = ?";
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
			
		}
		catch (NullPointerException e)
		{
			if (dbField.equalsIgnoreCase("rep")) eGORep.log.severe(eGORep.logPref + "Could not connect to database. Please check that the MySQL Server is running");
			success = 1;
		}
		
		if (success < 1)
			fixFringeCases(con, name, dbField);
		
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

	public int getRep(final String name)
	{
		return getInt("rep", name);
	}

	public int getRemPoints(String name)
	{
		return getInt("points", name);
	}

	public Long getTime(String name)
	{
		return getLong("time", name);
	}
	
	
	private void fixFringeCases(Connection con, String name, String dbField)
	{
		if (eGORepConfig.sqlDBName.equalsIgnoreCase(""))
			eGORep.log.severe(eGORep.logPref + "Database name is blank! Edit config.yml to include a database name");
		else if (eGORepConfig.sqlTableName.equalsIgnoreCase(""))
			eGORep.log.severe(eGORep.logPref + "Table name is blank! Edit config.yml to include a table name");
		else
		{
			PreparedStatement stmt = null;
			int success = 0, i, j;
			String q;
			int insVal = 0;
			if (dbField.equalsIgnoreCase("points"))
				insVal = 3;

			for (i = 0; i < 2; i++)
			{
				success = 0;
				q = "INSERT INTO " + eGORepConfig.sqlTableName + " (`IGN`, `" + dbField + "`) VALUES (?, ?)";
				try
				{
					stmt = con.prepareStatement(q);
					stmt.setString(1, name);
					stmt.setLong(2, insVal);

					success = stmt.executeUpdate();
					stmt.close();
					
					//break out of loop - we're done
					i = 2;
				}
				catch (SQLException e)
				{
					eGORep.log.warning(eGORep.logPref + "Could not add " + name + " to the database");
				}

				if (success < 1 && i < 1)
				{
					for (j = 0; j < 2; j++)
					{
						success = 0;
						eGORep.log.info(eGORep.logPref + "Attempting to create table " + eGORepConfig.sqlTableName);
						q = "CREATE TABLE IF NOT EXISTS `" + eGORepConfig.sqlTableName + "` (`IGN` varchar(128) COLLATE utf8_unicode_ci NOT NULL, `rep` int(11) NOT NULL DEFAULT '0', `points` int(11) NOT NULL DEFAULT '3', `time` bigint(20) NOT NULL DEFAULT '0', PRIMARY KEY (`IGN`)) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
						try
						{
							stmt = con.prepareStatement(q); 
							success = stmt.executeUpdate();
							stmt.close();
							
							eGORep.log.info("Created table " + eGORepConfig.sqlTableName);
							
							//break out of loop -- we're done
							j = 2;
						}
						catch (SQLException e)
						{
							eGORep.log.warning(eGORep.logPref + "Could not create table " + eGORepConfig.sqlTableName + ".");
						}
						
						if (success < 1 && j < 1)
						{
							success = 0;
							eGORep.log.info(eGORep.logPref + "Attempting to create database " + eGORepConfig.sqlDBName);
							q = "CREATE DATABASE IF NOT EXISTS  `" + eGORepConfig.sqlDBName + "` ;";
							
							try
							{
								stmt = con.prepareStatement(q);
								
								success = stmt.executeUpdate();
								stmt.close();
								
								eGORep.log.info(eGORep.logPref + "Created database " + eGORepConfig.sqlDBName);
								
								//re-get the connection so it will have the database this time
								con = getSQLConnection();
							}
							catch (SQLException e)
							{
								eGORep.log.severe(eGORep.logPref + "Could not create user, table or database. Please check your config.yml and database setup.");
								e.printStackTrace();
							}
							
							//nothing we tried worked - may as well exit the loops
							if (success < 1)
							{
								i = 2;
								j = 2;
							}
						}
					}
				}
				else
					i = 2;
			}
		}
	}
}

/*
 * Code to create table if necessary

CREATE TABLE IF NOT EXISTS `egorep` (
  `IGN` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `rep` int(11) NOT NULL DEFAULT '0',
  `points` int(11) NOT NULL DEFAULT '3',
  `time` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`IGN`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
*/