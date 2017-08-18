/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.akropon.daytimeregulator;

import java.util.regex.Pattern;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

/**
 *
 * @author akropon
 */
public class MessageManager {
	static MainPluginClass plugin;
	static boolean doFormatByNewlines = true;
	
	public static void init(MainPluginClass plugin) {
		MessageManager.plugin = plugin;
	}
	
	public static void sendMessage(CommandSender receiver,
			boolean prefix,
			ChatColor chatColor,
			String formatStr,
			Object[] args) {
		
		
		String[] lines = null;
		String line = null;
		String color = null;
		String newColor = null;
		if (chatColor!=null) color = chatColor.toString();
		
		if(doFormatByNewlines) {
			lines = formatStr.split(Pattern.quote("\n"));
		} else {   
			lines = new String[] {formatStr};
		}
		
		
		for (int i=0; i<lines.length; i++) {
			
			// If the prev line had color-identifiers inside itself,
			// hen the color for next line should be turned in the last color-identifier
			// of the prev line.
			if (i>0) {
				newColor = ChatColor.getLastColors(line);
				if (newColor.length()>0) color = newColor;
			}
			
			line = lines[i];
			
			if (prefix) {
				if (args == null) {
					if (color == null) {
						receiver.sendMessage(
								"[" + MainPluginClass.SHORT_NAME_CAPS + "] "
								+ line);
						if (MainPluginClass.DEBUG_MM) debugMSG(receiver, 0);
					} else {
						receiver.sendMessage(
								"[" + MainPluginClass.SHORT_NAME_CAPS + "] "
								+ color + line);
						if (MainPluginClass.DEBUG_MM) debugMSG(receiver, 1);
					}
				} else if (color == null) {
					receiver.sendMessage(String.format(
							"[" + MainPluginClass.SHORT_NAME_CAPS + "] "
							+ line, args));
						if (MainPluginClass.DEBUG_MM) debugMSG(receiver, 2);
				} else {
					receiver.sendMessage(String.format(
							"[" + MainPluginClass.SHORT_NAME_CAPS + "] "
							+ color + line, args));
						if (MainPluginClass.DEBUG_MM) debugMSG(receiver, 3);
				}
			} else if (args == null) {
				if (color == null) {
					receiver.sendMessage(
							line);
						if (MainPluginClass.DEBUG_MM) debugMSG(receiver, 4);
				} else {
					receiver.sendMessage(
							color + line);
						if (MainPluginClass.DEBUG_MM) debugMSG(receiver, 5);
				}
			} else if (color == null) {
				receiver.sendMessage(String.format(
						line, args));
						if (MainPluginClass.DEBUG_MM) debugMSG(receiver, 6);
			} else {
				receiver.sendMessage(String.format(
						color + line, args));
						if (MainPluginClass.DEBUG_MM) debugMSG(receiver, 7);
			}
		}
	}
	
	public static void debugMSG(CommandSender receiver, int code) {
		receiver.sendMessage("[" + MainPluginClass.SHORT_NAME_CAPS + "] [DEBUG] code="+code);
	}
	public static void debugMSG(CommandSender receiver, String msg) {
		receiver.sendMessage("[" + MainPluginClass.SHORT_NAME_CAPS + "] [DEBUG] "+msg);
	}

	public static void sendMessage(CommandSender receiver, 
			boolean prefix, 
			ChatColor color, 
			String str) {
		sendMessage(receiver, prefix, color, str, null);
	}
	public static void sendMessage(CommandSender receiver, 
			boolean prefix, 
			String formatStr, 
			Object[] args) {
		sendMessage(receiver, prefix, null, formatStr, args);
	}
	public static void sendMessage(CommandSender receiver, 
			boolean prefix, 
			String str) {
		sendMessage(receiver, prefix, null, str, null);
	}
	
	public static void writeConsole(boolean prefix, 
			ChatColor color, 
			String formatStr, 
			Object[] args) {
		sendMessage(plugin.getServer().getConsoleSender(), prefix, color, formatStr, args);
	}
	public static void writeConsole(boolean prefix, 
			ChatColor color, 
			String str) {
		sendMessage(plugin.getServer().getConsoleSender(), prefix, color, str, null);
	}
	public static void writeConsole(boolean prefix,
			String formatStr, 
			Object[] args) {
		sendMessage(plugin.getServer().getConsoleSender(), prefix, null, formatStr, args);
	}
	public static void writeConsole(boolean prefix,
			String str) {
		sendMessage(plugin.getServer().getConsoleSender(), prefix, null, str, null);
	}
	
}
