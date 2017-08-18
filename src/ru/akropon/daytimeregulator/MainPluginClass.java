package ru.akropon.daytimeregulator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.*;
import org.bukkit.configuration.Configuration;

/**
 *
 * @author akropon
 */

public class MainPluginClass extends JavaPlugin { // расширяем класс JavaPlugin
	public static boolean DEBUG_CH = false;
	public static boolean DEBUG_MM = false;
	public static boolean DEBUG_MPC = false;
	
	public static String FULL_NAME = "TimeRegulator";
	public static String SHORT_NAME = "TimeReg";
	public static String SHORT_NAME_CAPS = "TIMEREG";
	public static String SHORT_NAME_LOWS = "timereg";
	
	public static String AUTHOR = "akropon";
	public static String EMAIL = "akropon@mail.ru";
	public static String VERSION = "1.0.0";
	public static String COMMAND = "timereg";
	public static String RELEASE_DATE = "17.08.2017";
	public static String DESCRIPTION = 
		"This plugin allows you to control time."
		+"\nWith it you can stop the time, make it faster or \nslower, make days longer and nights shorter,"
		+"\nmake time turn in custom interval, change its\ndirection and even break it at all.";
	

	public boolean isRegulatorTaskActive;
	public int activeRegulatorTaskId;
	
	public Server server;
	public BukkitScheduler scheduler;
	public World world;

	@Override
	public void onEnable() // что то, что происходит при включении
	{
		MessageManager.init(this);
		MessageManager.writeConsole(true, ChatColor.GREEN, "Enabled");
		
		createFileHelp();
		
		this.saveDefaultConfig();

		server = getServer();
		scheduler = getServer().getScheduler();
		world = server.getWorlds().get(0);

		isRegulatorTaskActive = false;
		activeRegulatorTaskId = 0;

		/* informing about mistakes in config.yml*/
		ArrayList<String> wrongValues = checkConfig(true);
		if (wrongValues.size() != 0) {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append(ChatColor.RED)
					.append("Config.yml contained wrong values.")
					.append("\nTheir paths: [");
			for (String path : wrongValues) 
				errMsg.append(path).append(" ");
			errMsg.setCharAt(errMsg.length()-1, ']');
			MessageManager.writeConsole(true, ChatColor.RED, errMsg.toString());
			MessageManager.writeConsole(true, ChatColor.YELLOW, 
					"Wrong values were replaced by defaults");
			
			saveConfig();
		}
		this.reload(false);
		
		getCommand(COMMAND).setExecutor(new CommandHandler(this, world));
	}

	@Override
	public void onDisable() // что то, что происходит при выключении
	{
		if (isRegulatorTaskActive) {
			getServer().getScheduler().cancelTask(activeRegulatorTaskId);
			isRegulatorTaskActive = false;
		}
		MessageManager.writeConsole(true, ChatColor.GREEN, "Disabled");
	}
	
	public void reload(boolean doCheckAndFixConfig) {
		if (doCheckAndFixConfig) checkConfig(true);
		
		if (isRegulatorTaskActive) {
			getServer().getScheduler().cancelTask(activeRegulatorTaskId);
			isRegulatorTaskActive = false;
		}
		
		int mode = getConfig().getInt("mode");
		switch (mode) {
			case 1:
				world.setGameRuleValue("doDaylightCycle", "false");
				world.setTime(getConfig().getLong("mode-static.time"));
				break;
			case 2:
				world.setGameRuleValue("doDaylightCycle", "false");
				activeRegulatorTaskId
						= scheduler.scheduleSyncRepeatingTask(this,
								new Regulators.RegulatorFull(this, world),
								1L,
								1L);
				isRegulatorTaskActive = true;
				break;
			case 3:
				world.setGameRuleValue("doDaylightCycle", "false");
				activeRegulatorTaskId
						= scheduler.scheduleSyncRepeatingTask(this,
								new Regulators.RegulatorParted(this, world),
								1L,
								1L);
				isRegulatorTaskActive = true;
				break;
			case 4:
				world.setGameRuleValue("doDaylightCycle", "false");
				activeRegulatorTaskId
						= scheduler.scheduleSyncRepeatingTask(this,
								new Regulators.RegulatorChaos(this, world),
								1L,
								1L);
				isRegulatorTaskActive = true;
				break;
			default:
				break;
		}
	}
	
	public ArrayList<String> checkConfig(boolean doReplacingByDefaults) {
		Configuration defconf = getConfig().getDefaults();
		
		// todo реализовать и запихнуть куда-нить в загрузку (скорее всего в релоад)
		ArrayList<String> wrongPathsList = new ArrayList<>();
		boolean error;
		String path;
		
		path = "mode";
		error = true;
		if (getConfig().contains(path))
			if (getConfig().isSet(path))
				if  (getConfig().isInt(path))
					if (getConfig().getInt(path)>=0 && getConfig().getInt(path)<=4)
						error = false;
		if (error && doReplacingByDefaults) getConfig().set(path, defconf.get(path));
		if (error) wrongPathsList.add(path);
		
		path = "mode-static.time";
		error = true;
		if (getConfig().contains(path))
			if (getConfig().isSet(path))
				if  (getConfig().isInt(path))
					error = false;
		if (error && doReplacingByDefaults) getConfig().set(path, defconf.get(path));
		if (error) wrongPathsList.add(path);
		
		path = "mode-full.day-time-speed";
		error = true;
		if (getConfig().contains(path))
			if (getConfig().isSet(path))
				if  (getConfig().isInt(path)) {
					if (getConfig().getInt(path)>0)
						error = false;
				} else if (getConfig().isDouble(path)) {
					if (getConfig().getDouble(path)>0)
						error = false;
				}
		if (error && doReplacingByDefaults) getConfig().set(path, defconf.get(path));
		if (error) wrongPathsList.add(path);
		
		path = "mode-full.night-time-speed";
		error = true;
		if (getConfig().contains(path))
			if (getConfig().isSet(path))
				if  (getConfig().isInt(path)) {
					if (getConfig().getInt(path)>0)
						error = false;
				} else if (getConfig().isDouble(path)) {
					if (getConfig().getDouble(path)>0)
						error = false;
				}
		if (error && doReplacingByDefaults) getConfig().set(path, defconf.get(path));
		if (error) wrongPathsList.add(path);
		
		path = "mode-full.inverse-time-direction";
		error = true;
		if (getConfig().contains(path))
			if (getConfig().isSet(path))
				if  (getConfig().isBoolean(path))
					error = false;
		if (error && doReplacingByDefaults) getConfig().set(path, defconf.get(path));
		if (error) wrongPathsList.add(path);
		
		path = "mode-parted.start-time";
		error = true;
		if (getConfig().contains(path))
			if (getConfig().isSet(path))
				if  (getConfig().isInt(path))
					error = false;
		if (error && doReplacingByDefaults) getConfig().set(path, defconf.get(path));
		if (error) wrongPathsList.add(path);
		
		path = "mode-parted.finish-time";
		error = true;
		if (getConfig().contains(path))
			if (getConfig().isSet(path))
				if  (getConfig().isInt(path))
					error = false;
		if (error && doReplacingByDefaults) getConfig().set(path, defconf.get(path));
		if (error) wrongPathsList.add(path);
		
		path = "mode-parted.time-speed";
		error = true;
		if (getConfig().contains(path))
			if (getConfig().isSet(path))
				if  (getConfig().isInt(path)) {
					if (getConfig().getInt(path)>0)
						error = false;
				} else if (getConfig().isDouble(path)) {
					if (getConfig().getDouble(path)>0)
						error = false;
				}
		if (error && doReplacingByDefaults) getConfig().set(path, defconf.get(path));
		if (error) wrongPathsList.add(path);
		
		path = "mode-parted.inverse-time-direction";
		error = true;
		if (getConfig().contains(path))
			if (getConfig().isSet(path))
				if  (getConfig().isBoolean(path))
					error = false;
		if (error && doReplacingByDefaults) getConfig().set(path, defconf.get(path));
		if (error) wrongPathsList.add(path);
		
		path = "mode-parted.allow-inverse-time-return";
		error = true;
		if (getConfig().contains(path))
			if (getConfig().isSet(path))
				if  (getConfig().isBoolean(path))
					error = false;
		if (error && doReplacingByDefaults) getConfig().set(path, defconf.get(path));
		if (error) wrongPathsList.add(path);
		
		path = "mode-chaos.time-speed";
		error = true;
		if (getConfig().contains(path))
			if (getConfig().isSet(path))
				if  (getConfig().isInt(path)) {
					if (getConfig().getInt(path)>0)
						error = false;
				} else if (getConfig().isDouble(path)) {
					if (getConfig().getDouble(path)>0)
						error = false;
				}
		if (error && doReplacingByDefaults) getConfig().set(path, defconf.get(path));
		if (error) wrongPathsList.add(path);
		
		path = "mode-chaos.probability-turn-to-forward";
		error = true;
		if (getConfig().contains(path))
			if (getConfig().isSet(path))
				if (getConfig().isDouble(path))
					if (getConfig().getDouble(path)>0 && getConfig().getDouble(path)<1)
						error = false;
		if (error && doReplacingByDefaults) getConfig().set(path, defconf.get(path));
		if (error) wrongPathsList.add(path);
		
		path = "mode-chaos.probability-turn-to-back";
		error = true;
		if (getConfig().contains(path))
			if (getConfig().isSet(path))
				if (getConfig().isDouble(path))
					if (getConfig().getDouble(path)>0 && getConfig().getDouble(path)<1)
						error = false;
		if (error && doReplacingByDefaults) getConfig().set(path, defconf.get(path));
		if (error) wrongPathsList.add(path);
		
		path = "mode-chaos.smooth-inverting";
		error = true;
		if (getConfig().contains(path))
			if (getConfig().isSet(path))
				if  (getConfig().isBoolean(path))
					error = false;
		if (error && doReplacingByDefaults) getConfig().set(path, defconf.get(path));
		if (error) wrongPathsList.add(path);
		
		path = "mode-chaos.smooth-step";
		error = true;
		if (getConfig().contains(path))
			if (getConfig().isSet(path))
				if (getConfig().isDouble(path))
					if (getConfig().getDouble(path)>0 && getConfig().getDouble(path)<=1)
						error = false;
		if (error && doReplacingByDefaults) getConfig().set(path, defconf.get(path));
		if (error) wrongPathsList.add(path);
		
		path = "config-version";
		error = true;
		if (getConfig().contains(path))
			if (getConfig().isSet(path))
				if (getConfig().isString(path))
					if (getConfig().getString(path).compareTo(defconf.getString(path))==0)
						error = false;
		if (error && doReplacingByDefaults) getConfig().set(path, defconf.get(path));
		if (error) wrongPathsList.add(path);
		
		if (DEBUG_MPC) {
			path = "debug-value";
			if (!getConfig().contains(path)) 
				MessageManager.writeConsole(true, "debug-value doesn't exist in config");
			else {
				MessageManager.writeConsole(true, 
						"debug-value's type is String=%s,Int=%s,Long=%s,Double=%s,Boolean=%s",
						new Object[]{
							String.valueOf(getConfig().isString(path)),
							String.valueOf(getConfig().isInt(path)),
							String.valueOf(getConfig().isLong(path)),
							String.valueOf(getConfig().isDouble(path)),
							String.valueOf(getConfig().isBoolean(path))});
				MessageManager.writeConsole(true, 
						"debug-value is as String=%s,Int=%s,Long=%s,Double=%s,Boolean=%s",
						new Object[]{
							String.valueOf(getConfig().getString(path)),
							String.valueOf(getConfig().getInt(path)),
							String.valueOf(getConfig().getLong(path)),
							String.valueOf(getConfig().getDouble(path)),
							String.valueOf(getConfig().getBoolean(path))});
			}
		};
		
		return wrongPathsList;
	}
	
	private void createFileHelp() {
		File dataDirectory = getDataFolder();
		if (dataDirectory.exists() == false)
			dataDirectory.mkdir();
		
		File[] files = dataDirectory.listFiles();
		boolean helpFileFound = false;
		for (int i=0; i<files.length; i++)
			if (files[i].getName().equalsIgnoreCase("HELP.TXT")) {
				helpFileFound = true;
				break;
			}
		
		if (helpFileFound == false) {
			InputStream inputStream = null;
			OutputStream outputStream = null;
			
			try {
				//inputStream = getClass().getResourceAsStream(paths[i]+"help.txt");
				inputStream = getClass().getResourceAsStream("/help.txt");
				outputStream = new FileOutputStream(dataDirectory.getPath()+"/help.txt");
				byte[] buffer = new byte[1024];
				int length;
				while ((length = inputStream.read(buffer)) > 0) {
					outputStream.write(buffer, 0, length);
				}
				MessageManager.writeConsole(true, ChatColor.WHITE, "help.txt should be created.");
			} catch (Exception exc) {
				MessageManager.writeConsole(true, ChatColor.WHITE, "Unexpected error while "
						+ "creating help.txt. "
						+ "\nIts message:"+exc.getMessage());
			}
			
			try { 
				inputStream.close(); 
			} catch (Exception exc1) {}
			try { 
				outputStream.flush();
				outputStream.close(); 
			} catch (Exception exc2) {}
		} else {
			MessageManager.writeConsole(true, ChatColor.WHITE, "help.txt already exists.");
		}
	}
}
