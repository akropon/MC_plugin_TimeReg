/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.akropon.daytimeregulator;

import java.util.Random;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.*;

/**
 *
 * @author akropon
 */
public abstract class Regulator implements Runnable {
        JavaPlugin plugin;
        World world;

        public Regulator(JavaPlugin plugin, World world) {
			this.plugin = plugin;
			this.world = world;
        }
		
		/** Converting time from [-24000;-1] | [0;23999] | [24000;47999]
		 *    to [0;23999]
		 * 
		 * 
		 * @param time
		 * @return converted time
		 */
		public static double normalizeNeighboringTime(double time) {
			if (time >= 24000) return time-24000;
			else if (time <= 0) return time+24000;
			else return time;
		}
}

