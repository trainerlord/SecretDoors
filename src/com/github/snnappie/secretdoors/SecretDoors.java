package com.github.snnappie.secretdoors;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.snnappie.secretdoors.listeners.BlockListener;
import com.github.snnappie.secretdoors.listeners.PlayerListener;
import com.github.snnappie.secretdoors.listeners.PowerListener;

public class SecretDoors extends JavaPlugin {
	private HashMap<Block, SecretDoor> doors = new HashMap<>();
	private HashMap<Block, SecretTrapdoor> trapdoors = new HashMap<>();

	public void onDisable() {
		for (Block door : this.doors.keySet()) {
			this.doors.get(door).close();
		}
		
		for (Block ladder : trapdoors.keySet()) {
			trapdoors.get(ladder).close();
		}
	}

	public void onEnable() {
		
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		getServer().getPluginManager().registerEvents(new PowerListener(this), this);
		getServer().getPluginManager().registerEvents(new BlockListener(this), this);
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
			this.doors.get(door).close();
			this.doors.remove(door);
		}
	}
	
	
	
	public void addTrapdoor(SecretTrapdoor door) {
		if (door.getKey().getType() == Material.LADDER) {
			trapdoors.put(door.getKey(), door);
		}
	}
	
	public void closeTrapdoor(Block ladder) {
		if (ladder.getType() == Material.LADDER) {
			if (isSecretTrapdoor(ladder)) {
				trapdoors.get(ladder).close();
				trapdoors.remove(ladder);
			}
		}
	}
	
	public boolean isSecretTrapdoor(Block ladder) {
        return ladder.getType() == Material.LADDER && trapdoors.containsKey(ladder);
    }
}