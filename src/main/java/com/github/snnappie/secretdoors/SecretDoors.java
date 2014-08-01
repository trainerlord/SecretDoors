/*
 * SecretDoors.java
 * Last modified: 2014 7 31
 *
 * In place of a legal notice,
 * here is the author's adaptation to the sqlite3 blessing:
 *
 * 	May you do good and not evil.
 * 	May you find forgiveness for yourself and forgive others.
 * 	May you share freely, never taking more than you give.
 *
 * 	May you love the Lord your God with all your heart,
 * 	with all your soul,
 * 	and with all your mind.
 */

package com.github.snnappie.secretdoors;

import com.github.snnappie.secretdoors.listeners.BlockListener;
import com.github.snnappie.secretdoors.listeners.PlayerListener;
import com.github.snnappie.secretdoors.listeners.PowerListener;
import com.github.snnappie.secretdoors.tasks.CloseDoorTask;
import com.github.snnappie.secretdoors.tasks.CloseTrapDoorTask;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SecretDoors extends JavaPlugin {
	private HashMap<Block, SecretDoor> doors = new HashMap<>();
	private HashMap<Block, SecretTrapdoor> trapdoors = new HashMap<>();

    private HashMap<SecretDoor, CloseDoorTask> doorTasks = new HashMap<>();
    private HashMap<SecretTrapdoor, CloseTrapDoorTask> trapdoorTasks = new HashMap<>();

    private int closeTime;
    // should these be static?  Seems ugly
    // blood tears everywhere
    public static List<Material> blacklist;
    public static List<Material> whitelist;

    // TODO: Still not sure if I'm happy about this re-refactoring
    /**
     * Permission strings
     */
    public static final String PERMISSION_SD_USE    = "secretdoors.use";
    public static final String PERMISSION_SD_CREATE = "secretdoors.create";

    /**
     * Config strings
     */
    public static final String CONFIG_PERMISSIONS_ENABLED   = "use-permissions";
    public static final String CONFIG_ENABLE_TIMERS         = "enable-timers";
    public static final String CONFIG_ENABLE_REDSTONE       = "enable-redstone";
    public static final String CONFIG_ENABLE_TRAPDOORS      = "enable-trapdoors";
    public static final String CONFIG_ENABLE_WHITELIST      = "enable-whitelist";
    public static final String CONFIG_CLOSE_TIME            = "close-time-seconds";


	public void onDisable() {
		for (Block door : this.doors.keySet()) {
			closeDoor(door);
		}
		
		for (Block ladder : trapdoors.keySet()) {
			closeTrapdoor(ladder);
		}
	}

	public void onEnable() {

        // listeners
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		getServer().getPluginManager().registerEvents(new PowerListener(this), this);
		getServer().getPluginManager().registerEvents(new BlockListener(this), this);

        // config
        getConfig().options().copyDefaults(true);
		saveConfig();
        loadConfig();
	}

    /**
     * Load the fields we need into memory
     */
    private void loadConfig() {

        this.closeTime = getConfig().getInt(CONFIG_CLOSE_TIME);
        if (this.closeTime < 0) this.closeTime = 0;

        blacklist = new ArrayList<>();
        for (String s : getConfig().getStringList("blacklist")) {
            blacklist.add(Material.getMaterial(s));
        }

        // can just use if whitelist != null
        // kind of ugly, but whatever
        if (getConfig().getBoolean(CONFIG_ENABLE_WHITELIST)) {
            whitelist = new ArrayList<>();
            for (String s : getConfig().getStringList("whitelist")) {
                whitelist.add(Material.getMaterial(s));
            }
        } else { // fix for my stupid hack to use the whitelist
            whitelist = null;
        }


    }

	// handles commands
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length != 1)
			return false;

		if (cmd.getName().equalsIgnoreCase("secretdoors")) {
			if (args[0].equalsIgnoreCase("reload")) {
				if (getConfig().getBoolean(CONFIG_PERMISSIONS_ENABLED)) {
					if (!sender.hasPermission("secretdoors.reload")) {
						return false;
					}
				}
				reloadConfig();
                loadConfig();

				sender.sendMessage(ChatColor.RED + "Secret Doors config reloaded");
				return true;
			}
		}
		return false;
	}

	public SecretDoor addDoor(SecretDoor door) {
		this.doors.put(door.getKey(), door);

        // add a task to close the door after the time
        if (getConfig().getBoolean(CONFIG_ENABLE_TIMERS)) {
            CloseDoorTask task =  new CloseDoorTask(this, door);
            task.runTaskLater(this, 20 * this.closeTime);
            doorTasks.put(door, task);
        }

		return door;
	}

	
	public boolean isSecretDoor(Block door) {
		return this.doors.containsKey(door);
	}

	public void closeDoor(Block door) {
		if (isSecretDoor(door)) {

            SecretDoor secretDoor = this.doors.remove(door);
            secretDoor.close();
            // remove and cancel the auto-task if the user manually closed the door
            if (getConfig().getBoolean(CONFIG_ENABLE_TIMERS))
                doorTasks.remove(secretDoor).cancel();
		}
	}

    public void closeDoorAuto(Block door) {
        if (isSecretDoor(door)) {

            SecretDoor secretDoor = this.doors.remove(door);
            secretDoor.autoClose();
            doorTasks.remove(secretDoor);
        }
    }
	
	
	
	public void addTrapdoor(SecretTrapdoor door) {
		if (door.getKey().getType() == Material.LADDER) {
			trapdoors.put(door.getKey(), door);

            // add a task to close the trapdoor
            if (getConfig().getBoolean(CONFIG_ENABLE_TIMERS)) {
                CloseTrapDoorTask task = new CloseTrapDoorTask(this, door);
                task.runTaskLater(this, 20 * this.closeTime);
                trapdoorTasks.put(door, task);
            }
		}
	}
	
	public void closeTrapdoor(Block ladder) {
		if (ladder.getType() == Material.LADDER) {
			if (isSecretTrapdoor(ladder)) {
                SecretTrapdoor trapdoor = trapdoors.remove(ladder);
                trapdoor.close();

                // cancel the task if the door was closed manually
                if (getConfig().getBoolean(CONFIG_ENABLE_TIMERS)) {
                    trapdoorTasks.remove(trapdoor).cancel();
                }
			}
		}
	}

    public void closeTrapdoorAuto(Block ladder) {
        if (ladder.getType() == Material.LADDER) {
            if (isSecretTrapdoor(ladder)) {
                SecretTrapdoor trapdoor = trapdoors.remove(ladder);
                trapdoor.close();
                trapdoorTasks.remove(trapdoor);
            }
        }
    }
	
	public boolean isSecretTrapdoor(Block ladder) {
        return ladder.getType() == Material.LADDER && trapdoors.containsKey(ladder);
    }
}