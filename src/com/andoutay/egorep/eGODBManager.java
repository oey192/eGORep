package com.andoutay.egorep;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.sql.Statement;

public class eGODBManager {
	private String table;
	
	public eGODBManager()
	{
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
	
	public void setAll(String name, int rep, int points, Long timestamp)
	{
		setRep(name, rep);
		setRemPoints(name, points);
		setTime(name, timestamp);
	}

	public void setVal(String dbField, String name, long val)
	{
		Connection con = getSQLConnection();
		PreparedStatement stmt = null;
		String q = "UPDATE " + table + " SET `" + dbField + "` = ?";
		int success = 0;
		
		try
		{
			stmt = con.prepareStatement(q);
			stmt.setFloat(1, val);
			
			success = stmt.executeUpdate();
			stmt.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		if (success < 1)
		{
			// TODO Instead of updating user, add them to DB!
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
			e.printStackTrace();
		}
		
		if (success < 1 && dbField.equalsIgnoreCase("points"))
			ans = 3;
		
		return ans;
	}

	public void setRep(String name, int rep)
	{
		setVal("rep", name, rep);
	}
	
	public void setRemPoints(String name, int points)
	{
		setVal("points", name, points);
	}

	public void setTime(String name, Long timestamp)
	{
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
*/