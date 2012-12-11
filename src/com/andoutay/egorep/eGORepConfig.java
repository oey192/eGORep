package com.andoutay.egorep;

import org.bukkit.configuration.Configuration;

public class eGORepConfig
{
	private static Configuration config;
	
	public static int repPerHour;
	public static boolean useDS;	//DS = Dedicated Supporter
	public static String sqlURL, sqlHost, sqlPort, sqlTableName, sqlDBName, sqlUser, sqlPassword;
	
	eGORepConfig(eGORep plugin)
	{
		config = plugin.getConfig().getRoot();
		config.options().copyDefaults(true);
		plugin.saveConfig();
	}
	
	public static void onEnable()
	{
		loadConfigVals();
	}
	
	private static void loadConfigVals()
	{
		repPerHour = config.getInt("repPerHour");
		useDS = config.getBoolean("useDS");
		sqlHost = config.getString("mysql.host");
		sqlPort = config.getString("mysql.port");
		sqlDBName = config.getString("mysql.dbName");
		sqlUser = config.getString("mysql.user");
		sqlPassword = config.getString("mysql.password");
		sqlURL = "jdbc:mysql://" + sqlHost + ":" + sqlPort + "/" + sqlDBName;
	}
}
