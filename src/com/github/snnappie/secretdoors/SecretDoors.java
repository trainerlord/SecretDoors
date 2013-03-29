package com.github.snnappie.secretdoors;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class SecretDoors extends JavaPlugin {
	private HashMap<Block, SecretDoor> doors = new HashMap<Block, SecretDoor>();

	public void onDisable() {
		for (Block door : this.doors.keySet()) {
			((SecretDoor) this.doors.get(door)).close();
		}
	}

	public void onEnable() {
		getServer().getPluginManager().registerEvents(new SecretDoorsPlayerListener(this), this);
		getServer().getPluginManager().registerEvents(new SecretDoorsPowerListener(this), this);
		getServer().getPluginManager().registerEvents(new SecretDoorsBlockListener(this), this);
		saveDefaultConfig();
		
	}

	// handles commands
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length != 1)
			return false;

		if (cmd.getName().equalsIgnoreCase("secretdoors")) {
			if (args[0].equalsIgnoreCase("reload")) {
				if (getConfig().getBoolean("use-permissions")) {
					if (!sender.hasPermission("secretdoors.reload")) {
						return false;
					}
				}
				reloadConfig();
				sender.sendMessage(ChatColor.RED + "Secret Doors config reloaded");
				return true;
			}
		}
		return false;
	}

	public SecretDoor addDoor(SecretDoor door) {
		this.doors.put(door.getKey(), door);
		return door;
	}

	public boolean isSecretDoor(Block door) {
		return this.doors.containsKey(door);
	}

	public void closeDoor(Block door) {
		if (isSecretDoor(door)) {
			((SecretDoor) this.doors.get(door)).close();
			this.doors.remove(door);
		}
	}
}