package com.andoutay.egorep;

import org.bukkit.configuration.Configuration;

public class eGORepConfig
{
	private static Configuration config;
	private static eGORep plugin;
	
	public static int repPerUnit, refreshSecs;
	public static boolean useAsync;	//DS = Dedicated Supporter
	public static String sqlURL, shortSQLURL, sqlHost, sqlPort, sqlDBName, sqlTableName, sqlLogTableName, sqlUser, sqlPassword;
	
	eGORepConfig(eGORep plugin)
	{
		eGORepConfig.plugin = plugin;
		config = plugin.getConfig().getRoot();
		if (config.getString("mysql.host") == null || config.getString("mysql.dbName") == null);
			config.options().copyDefaults(true);
		plugin.saveConfig();
	}
	
	public static void onEnable()
	{
		loadConfigVals();
	}
	
	public static void reload()
	{
		plugin.reloadConfig();
		config = plugin.getConfig().getRoot();
		onEnable();
	}
	
	private static void loadConfigVals()
	{
		repPerUnit = config.getInt("repPerUnit");
		refreshSecs = config.getInt("refreshSecs");
		useAsync = config.getBoolean("useAsync");
		sqlHost = config.getString("mysql.host");
		sqlPort = config.getString("mysql.port");
		sqlDBName = config.getString("mysql.dbName");
		sqlTableName = config.getString("mysql.table");
		sqlLogTableName = config.getString("mysql.logTable");
		sqlUser = config.getString("mysql.user");
		sqlPassword = config.getString("mysql.password");
		sqlURL = "jdbc:mysql://" + sqlHost + ":" + sqlPort + "/" + sqlDBName;
		shortSQLURL = "jdbc:mysql://" + sqlHost + ":" + sqlPort;
	}
}
