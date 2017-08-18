/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.akropon.daytimeregulator;

import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import jdk.nashorn.internal.parser.TokenType;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

/**
 *
 * @author akropon
 */
public class CommandHandler implements CommandExecutor{
	public static String CMD_DISABLE = "disable";
	public static String CMD_SETMODE = "setmode";
	public static String CMD_RELOAD = "reload";
	public static String CMD_SETDAYLIGHTCYCLE = "setdaylightcycle";
	public static String CMD_SETTIME = "settime";
	//public static String CMD_HELP = "help";
	public static String CMD_GETTIME = "gettime";
	public static String CMD_GETMODE = "getmode";
	public static String CMD_SETPARAM = "setparam";
	public static String CMD_INFO = "info";
	
	public static String USAGE_MAIN = "/timereg <info|setMode|getMode|reload\n"
			+ "|disable|setTime|getTime\n"
			+ "|setDaylightCycle|setParam> ...";
	
	MainPluginClass plugin;
	World world;

	public CommandHandler(MainPluginClass plugin, World world) {
		this.plugin = plugin;
		this.world = world;
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String errMsg = "Error.\nUsage: "
				+USAGE_MAIN;
		
		if (args.length<1) {
			commandNothing(sender, cmd, label, args);
			return true;
		}
		
		if (args[0].equalsIgnoreCase(CMD_DISABLE))
			commandDisable(sender, cmd, label, args);
		else if (args[0].equalsIgnoreCase(CMD_SETMODE))
			commandSetmode(sender, cmd, label, args);
		else if (args[0].equalsIgnoreCase(CMD_INFO))
			commandInfo(sender, cmd, label, args);
		else if (args[0].equalsIgnoreCase(CMD_RELOAD))
			commandReload(sender, cmd, label, args);
		else if (args[0].equalsIgnoreCase(CMD_SETDAYLIGHTCYCLE))
			commandSetdaylightcycle(sender, cmd, label, args);
		else if (args[0].equalsIgnoreCase(CMD_SETTIME))
			commandSettime(sender, cmd, label, args);
		/*else if (args[0].equalsIgnoreCase(CMD_HELP))
			commandHelp(sender, cmd, label, args);*/
		else if (args[0].equalsIgnoreCase(CMD_GETTIME))
			commandGettime(sender, cmd, label, args);
		else if (args[0].equalsIgnoreCase(CMD_GETMODE))
			commandGetmode(sender, cmd, label, args);
		else if (args[0].equalsIgnoreCase(CMD_SETPARAM))
			commandSetparam(sender, cmd, label, args);
		else {
			MessageManager.sendMessage(sender, true, ChatColor.RED, errMsg);
		}
		return true;
	}
	
	public boolean commandInfo(CommandSender sender, Command cmd, String label, String[] args) {
		MessageManager.sendMessage(sender, true, ChatColor.GREEN, "---------- INFO --------");
		MessageManager.sendMessage(sender, true, "Plugin name:  "+ChatColor.YELLOW+MainPluginClass.FULL_NAME);
		MessageManager.sendMessage(sender, true, "Author:       "+ChatColor.YELLOW+MainPluginClass.AUTHOR);
		MessageManager.sendMessage(sender, true, "Email:        "+ChatColor.YELLOW+MainPluginClass.EMAIL);
		MessageManager.sendMessage(sender, true, "Version:      "+ChatColor.YELLOW+MainPluginClass.VERSION);
		MessageManager.sendMessage(sender, true, "Release date: "+ChatColor.YELLOW+MainPluginClass.RELEASE_DATE);
		MessageManager.sendMessage(sender, true, "Description:  "+ChatColor.YELLOW+MainPluginClass.DESCRIPTION);
		return true;
	}
	
	public boolean commandNothing(CommandSender sender, Command cmd, String label, String[] args) {
		MessageManager.sendMessage(sender, true, ChatColor.YELLOW, " (= HELLO! =)");
		MessageManager.sendMessage(sender, true, ChatColor.YELLOW, "This command allows you to control me.");
		MessageManager.sendMessage(sender, true, ChatColor.GREEN, "Its usage: "+USAGE_MAIN);
		MessageManager.sendMessage(sender, true, ChatColor.YELLOW, " (= HAVE FUN! =)");
		return true;
	}
	
	public boolean commandDisable(CommandSender sender, Command cmd, String label, String[] args) {
		
		plugin.getConfig().set("mode", 0);
		plugin.saveConfig();
		plugin.reload(false);
		
		MessageManager.sendMessage(sender, true, ChatColor.GREEN, "Disabled");
		MessageManager.sendMessage(sender, true, 
				"(Advice) If you want to restore normal passage of time, use /timereg setdaylightcycle true");
		
		return true;
	}
	public boolean commandSetmode(CommandSender sender, Command cmd, String label, String[] args) {
		String errMessage = "Error."
				+"\nCorrect use: /timereg setmode <mode>,"
				+"\nwhere mode=:"
				+"\n  0 - off"
				+"\n  1 - static time"
				+"\n  2 - full time"
				+"\n  3 - pated time"
				+"\n  4 - chaos";
		
		if (args.length<2) {
			MessageManager.sendMessage(sender, true, ChatColor.RED, errMessage);
			return false;
		}
		if (isInt(args[1])==false) {
			MessageManager.sendMessage(sender, true, ChatColor.RED, errMessage);
			return false;
		}
		int newMode = Integer.parseInt(args[1]);
		if (newMode<0 || newMode>4) {
			MessageManager.sendMessage(sender, true, ChatColor.RED, errMessage);
			return false;
		}
		plugin.getConfig().set("mode", newMode);
		plugin.saveConfig();
		plugin.reload(false);
		MessageManager.sendMessage(sender, true, ChatColor.GREEN, "Success");
		return true;
	}
	public boolean commandReload(CommandSender sender, Command cmd, String label, String[] args) {
		plugin.reloadConfig();
		
		ArrayList<String> wrongValues = plugin.checkConfig(true);
		if (!wrongValues.isEmpty()) {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("Config.yml contained wrong values.")
					.append("\nTheir paths: [");
			for (String path : wrongValues) 
				errMsg.append(path).append(" ");
			errMsg.setCharAt(errMsg.length()-1, ']');
			MessageManager.sendMessage(sender, true, ChatColor.RED, errMsg.toString());
			MessageManager.sendMessage(sender, true, ChatColor.YELLOW, "Wrong values were replaced by defaults");
			plugin.saveConfig();
		}
		plugin.reload(false);
		
		plugin.reload(false);
		MessageManager.sendMessage(sender, true, ChatColor.GREEN, "Reloaded");
		return false;
	}
	public boolean commandSetdaylightcycle(CommandSender sender, Command cmd, String label, String[] args) {
		String errMessage = "Error. Correct use: "
				+ "/timereg setdaylightcycle <(1|true|yes)|(0|false|no)>";
		
		int mode = plugin.getConfig().getInt("mode");
		if (mode != 0) {
			MessageManager.sendMessage(sender, true, ChatColor.RED, "Couldn't use this command when mode="+mode+" is active."+
					"\nThis command is available only when mode=0 (disabled).");
			return false;
		}
		if (args.length<2) {
			MessageManager.sendMessage(sender, true, ChatColor.RED, errMessage);
			return false;
		}
		if (is1TrueYes0FalseNo(args[1])==false) {
			MessageManager.sendMessage(sender, true, ChatColor.RED, errMessage);
			return false;
		}
		boolean value = parse1TrueYes0FalseNo(args[1]);
		world.setGameRuleValue("doDaylightCycle", String.valueOf(value));
		MessageManager.sendMessage(sender, true, ChatColor.GREEN, "success");
		return true;
	}
	public boolean commandSettime(CommandSender sender, Command cmd, String label, String[] args) {
		String errMsg = "Error. "
				+"\nCorrect use: /timereg settime <time>,"
				+"\nwhere <time> is integer,"
				+"\ntime is measured in ticks,"
				+"\nreccomended values are in [0,24000]";
		
		int mode = plugin.getConfig().getInt("mode");
		if (mode != 0 && mode != 1) {
			MessageManager.sendMessage(sender, true, ChatColor.RED, "Couldn't use this command when mode="+mode+" is active."+
					"\nThis command is available only when mode= 0 or 1.");
			return false;
		}
		if (args.length<2) {
			MessageManager.sendMessage(sender, true, ChatColor.RED, errMsg);
			return false;
		}
		if (isInt(args[1])==false) {
			MessageManager.sendMessage(sender, true, ChatColor.RED, errMsg);
			return false;
		}
		int newTime = Integer.parseInt(args[1]);
		world.setTime(newTime);
		if (mode == 1) {
			plugin.getConfig().set("mode-static.time", newTime);
			plugin.saveConfig();
		}
		MessageManager.sendMessage(sender, true, ChatColor.GREEN, "Success");
		return true;
	}
	public boolean commandHelp(CommandSender sender, Command cmd, String label, String[] args) {
		//TODO
		return false;
	}
	public boolean commandGettime(CommandSender sender, Command cmd, String label, String[] args) {
		MessageManager.sendMessage(sender, true, "Current time: "+world.getTime()+" ticks");
		return true;
	}
	public boolean commandGetmode(CommandSender sender, Command cmd, String label, String[] args) {
		int modeNum = plugin.getConfig().getInt("mode");
		String modeName;
		switch(modeNum) {
			case 0: modeName="off"; break;
			case 1: modeName="static"; break;
			case 2: modeName="full"; break;
			case 3: modeName="parted"; break;
			case 4: modeName="chaos"; break;
			default: modeName = "error"; break;
		}
		MessageManager.sendMessage(sender, true, "Current mode: "+modeNum+" ("+modeName+")");
		return true;
	}
	public boolean commandSetparam(CommandSender sender, Command cmd, String label, String[] args) {
		String errMsg = "Error. "
				+"\nCorrect use: /timereg setparam <path> <value>,"
				+"\nwhere <path> is path to value"
				+"\nand <value> is value of necessary type."
				//+"\nFor Strings use double-quotes." //not needed in this version
				+"\nExample: /timereg setparam mode-static.time 6000"
				+"\nExample: /timereg setparam mode 2";
		
		if (args.length < 3)
			MessageManager.sendMessage(sender, true, ChatColor.RED, errMsg);
		
		int response = setValueToConfigWithAutoParsing(plugin.getConfig(),args[1], args[2]);
		switch (response) {
			case 0:
				plugin.saveConfig();
				plugin.reload(false);
				MessageManager.sendMessage(sender, true, ChatColor.GREEN, "Success");
				return true;
			case 1:
				MessageManager.sendMessage(sender, true, ChatColor.RED, "No such path in configuration");
				return false;
			case 2:
				MessageManager.sendMessage(sender, true, ChatColor.RED, "Illegal type or value of <value>="+args[2]);
				return false;
			default: 
				MessageManager.sendMessage(sender, true, ChatColor.RED, "UNEXPECTED ERROR 13A5."
						+" Please report to plugin author if you saw this message");
				return false;
		}
	}
	
	
	public boolean isInt(String str) {
		try { Integer.parseInt(str); return true;
		} catch (Exception e) { return false; }
	}
	public boolean isDouble(String str) {
		try { Double.parseDouble(str); return true;
		} catch (Exception e) { return false; }
	}
	public boolean isBoolean(String str) {
		try { Boolean.parseBoolean(str); return true;
		} catch (Exception e) { return false; }
	}
	
	public boolean is1TrueYes0FalseNo(String str) {
		if (str.equalsIgnoreCase("1")) return true;
		if (str.equalsIgnoreCase("0")) return true;
		if (str.equalsIgnoreCase("true")) return true;
		if (str.equalsIgnoreCase("false")) return true;
		if (str.equalsIgnoreCase("yes")) return true;
		if (str.equalsIgnoreCase("no")) return true;
		return false;
	}
	public boolean parse1TrueYes0FalseNo(String str) {
		if (str.equalsIgnoreCase("1")) return true;
		if (str.equalsIgnoreCase("0")) return false;
		if (str.equalsIgnoreCase("true")) return true;
		if (str.equalsIgnoreCase("false")) return false;
		if (str.equalsIgnoreCase("yes")) return true;
		return false;
	}
	
	
	/** Define type of new value, parse it and sets it to config by path.
	 *	Possible types: Integer, Double, Boolean, String
	 *  IMPORTANT:
	 *    1) Only existing parameter can be changed.
	 *    2) If type of new value is illegal, changes won't be made.
	 *    3) If value is illegal (too big, too small, etc), changes won't be made.
	 *    4) Changes will be made only in config-object. Physical config-file won't
	 *       be chanded anyway.
	 * @param config - config
	 * @param path - path
	 * @param strValue - new value in String format
	 * @return	0 - Success.
	 *			1 - No such path in configuration.
	 *			2 - Illegal type or value of 'strValue'.
	 */
	public int setValueToConfigWithAutoParsing(
			FileConfiguration config, String path, String strValue) {
		if (!config.contains(path))
			return 1;
		
		Object prevValue = config.get(path);
		
		if (isInt(strValue))
			config.set(path, Integer.parseInt(strValue));
		else if (isDouble(strValue))
			config.set(path, Double.parseDouble(strValue));
		else if (isBoolean(strValue))
			config.set(path, Boolean.parseBoolean(strValue));
		else config.set(path, strValue);
		
		ArrayList<String> wrongValues = plugin.checkConfig(false);
		if (wrongValues.size() != 0) {
			config.set(path, prevValue);
			return 2;
		}
		
		return 0;
	}
	
}
