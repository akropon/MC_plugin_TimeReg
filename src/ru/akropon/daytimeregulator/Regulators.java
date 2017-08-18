/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.akropon.daytimeregulator;

import java.util.Random;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author akropon
 */
public class Regulators {

	public static class RegulatorFull extends Regulator {

		double dayTimeSpeed;
		double nightTimeSpeed;
		double currentTime;

		int counter;

		public RegulatorFull(JavaPlugin plugin, World world) {
			super(plugin, world);

			dayTimeSpeed = plugin.getConfig().getDouble("mode-full.day-time-speed");
			nightTimeSpeed = plugin.getConfig().getDouble("mode-full.night-time-speed");
			if (plugin.getConfig().getBoolean("mode-full.inverse-time-direction")) {
				dayTimeSpeed = -dayTimeSpeed;
				nightTimeSpeed = -nightTimeSpeed;
			}
			currentTime = world.getTime();
		}

		@Override
		public void run() {
			if (currentTime < 12000) {
				currentTime += dayTimeSpeed;
			} else {
				currentTime += nightTimeSpeed;
			}
			currentTime = Regulator.normalizeNeighboringTime(currentTime);
			world.setTime((long)currentTime);

			counter++;
			if (counter > 60) {
				counter = 0;
			}
		}
	}

	public static class RegulatorParted extends Regulator {
		long startTime;
		long endTime;
		double currentTime;
		double timeIncrement;
		boolean inverseTimeDirection;
		boolean allowInverseTimeReturn;
		boolean isForward; // направление времени
		int amountOfSteps; // кол-во итераций
		int currentStep; // текущая итерация

		public RegulatorParted(JavaPlugin plugin, World world) {
			super(plugin, world);
			startTime = plugin.getConfig().getLong("mode-parted.start-time");
			endTime = plugin.getConfig().getLong("mode-parted.finish-time");
			allowInverseTimeReturn = plugin.getConfig().getBoolean("mode-parted.allow-inverse-time-return");
			inverseTimeDirection = plugin.getConfig().getBoolean("mode-parted.inverse-time-direction");
			timeIncrement = plugin.getConfig().getDouble("mode-parted.time-speed");
			
			if (startTime < endTime)
				amountOfSteps = (int)((endTime-startTime) / timeIncrement);
			else 
				amountOfSteps = (int)((endTime+24000-startTime) / timeIncrement); //24k ticks in day
			
			if (inverseTimeDirection) {
				currentTime = endTime;
				timeIncrement = -timeIncrement;
				isForward = false;
				currentStep = amountOfSteps-1;
			}
			else {
				currentTime = startTime;
				isForward = true;
				currentStep = 0;
			}
		}

		@Override
		public void run() {
			currentTime += timeIncrement;
			if (isForward) currentStep++;
			else currentStep--;
			
			
			if (allowInverseTimeReturn) {
				if (isForward) {
					if (currentStep == amountOfSteps) {
						isForward = false;
						timeIncrement = -timeIncrement;
					}
				}
				else {
					if (currentStep == 0) {
						isForward = true;
						timeIncrement = -timeIncrement;
					}
				}
			} 
			else { // allowInverseTimeReturn == false
				if (inverseTimeDirection) {
					if (currentStep < 0) {
						currentStep = amountOfSteps;
						currentTime = endTime;
					}
				} 
				else { // inverseTimeDirection == false
					if (currentStep > amountOfSteps) {
						currentStep = 0;
						currentTime = startTime;
					}
				}
			}
			
			currentTime = Regulator.normalizeNeighboringTime(currentTime);
			world.setTime((long)currentTime);
		}
	}
	
	public static class RegulatorChaos extends Regulator {

		double currentTime;		// координата времени
		double timeIncrement;	// скорость координаты
		double targetTimeIncrement;    // желаемый вектор скорости
		double incrementIncrement;		// максимальное ускорение (изменение вектора скорости за 1 такт)
		boolean smoothInverting;
		Random random;
		double probabilityTurnToForward;
		double probabilityTurnToBack;

		public RegulatorChaos(JavaPlugin plugin, World world) {
			super(plugin, world);
			currentTime = world.getTime();
			targetTimeIncrement = plugin.getConfig().getDouble("mode-chaos.time-speed");
			timeIncrement = targetTimeIncrement;
			incrementIncrement = (2*targetTimeIncrement) 
					* plugin.getConfig().getDouble("mode-chaos.smooth-step");
			smoothInverting = plugin.getConfig().getBoolean("mode-chaos.smooth-inverting");
			probabilityTurnToForward = plugin.getConfig().getDouble("mode-chaos.probability-turn-to-forward");
			probabilityTurnToBack = plugin.getConfig().getDouble("mode-chaos.probability-turn-to-back");
			random = new Random(world.getTime());
		}


		@Override
		public void run() {
			if (smoothInverting) {
				currentTime += timeIncrement;
				if (timeIncrement != targetTimeIncrement) {
					if (timeIncrement < targetTimeIncrement) {
						timeIncrement += incrementIncrement;
						if (timeIncrement > targetTimeIncrement)
							timeIncrement = targetTimeIncrement;
					} else { // timeIncrement > targetTimeIncrement
						timeIncrement -= incrementIncrement;
						if (timeIncrement < targetTimeIncrement)
							timeIncrement = targetTimeIncrement;
					}
				}
			} 
			else {
				currentTime += targetTimeIncrement;
			}
			
			if (targetTimeIncrement > 0) {
				if (random.nextDouble() < probabilityTurnToBack)
					targetTimeIncrement = -targetTimeIncrement;
			}
			else {
				if (random.nextDouble() < probabilityTurnToForward)
					targetTimeIncrement = -targetTimeIncrement;
			}
			
			
			currentTime = Regulator.normalizeNeighboringTime(currentTime);
			world.setTime((long)currentTime);
		}
	}

	/*public static class RegulatorFull implements Runnable {
                JavaPlugin plugin;
                double currentTime;
                int dayDuration;
                double tickIncrement;
                Random random;
                
                
                public RegulatorFull(JavaPlugin plugin, int currentTime, int dayDuration) {
                        super();
                        this.plugin = plugin;
                        this.currentTime = currentTime;
                        this.dayDuration = dayDuration;
                        tickIncrement = 24000.0 / dayDuration;
                        random = new Random(currentTime);
                }

                @Override
                public void run() {
                       Server server = plugin.getServer();
                       World world = server.getWorlds().get(0);
                       currentTime += tickIncrement;
                       
                       // это полная жесть))))
                       if (tickIncrement > 0) {
                               if (random.nextInt(40) == 1) tickIncrement = -tickIncrement; 
                       }
                       else {
                               if (random.nextInt(20) == 1) tickIncrement = -tickIncrement;     
                       }
                       
                       
                       world.setTime((int)currentTime);
                }
        }
	 */
}
