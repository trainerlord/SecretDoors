/*
 * SecretDoors.java
 * Last modified: 2014 12 21
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

package com.development.trainerlord.secretdoors;

import com.development.trainerlord.secretdoors.listeners.BlockListener;
import com.development.trainerlord.secretdoors.listeners.PlayerListener;
import com.development.trainerlord.secretdoors.listeners.PowerListener;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Plugin entry point.  Keeps state of opened doors and timer tasks to close doors.
 */
public class SecretDoors extends JavaPlugin {

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

    // Used for enabling debug println's throughout the code - should always be false in a release.
    public static final boolean DEBUG = true;


    // Represents the collection of materials that *cannot* be used to create SecretOpenable
    // objects.  That is, a Block with Material type m, cannot be used to create a SecretOpenable object if m is
    // an element of blacklist.
    private Set<Material> blacklist;

    // If whitelist is defined (that is, whitelist != null), then only Blocks that have a Material type found within
    // whitelist can be used to create SecretOpenable objects.
    private Set<Material> whitelist;

    // Map from SecretOpenable keys (i.e. Blocks) to currently opened SecretOpenable objects.
    private Map<Block, SecretOpenable> doors = new HashMap<>();

    // Map of SecretOpenable keys (i.e. Blocks) to tasks for automatically closing them.
    private Map<Block, BukkitRunnable> doorTasks;

    // Represents the time in seconds in which SecretOpenable objects will automatically close.  This value cannot
    // be negative.
    private int closeTime;

    @Override
    public void onDisable() {
        // closeDoor modifies the set - could have problems with iteration if it is modified in place.
        // Creating a copy will ensure we close every element properly.
        Set<Block> keySet = new HashSet<>(this.doors.keySet());
        keySet.forEach(this::closeDoor);
        if (doorTasks != null)
            doorTasks.entrySet().forEach((e) -> e.getValue().cancel());
    }

    @Override
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

    // Handling loading (and re-loading) of the config objects into memory.
    // This consists of determining if the whitelist/blacklist are in use and loads them into memory.
    // Also handles the auto close options and timer.
    private void loadConfig() {

        this.closeTime = getConfig().getInt(CONFIG_CLOSE_TIME);
        if (this.closeTime < 0) this.closeTime = 0;

        blacklist = new HashSet<>();
        blacklist.addAll(getConfig().getStringList("blacklist").stream().map(Material::getMaterial).collect(Collectors.toSet()));

        // can just use if whitelist != null
        // kind of ugly, but whatever
        if (getConfig().getBoolean(CONFIG_ENABLE_WHITELIST)) {
            whitelist = new HashSet<>();
            whitelist.addAll(getConfig().getStringList("whitelist").stream().map(Material::getMaterial).collect(Collectors.toSet()));
        } else { // fix for my stupid hack to use the whitelist
            whitelist = null;
        }

        if (getConfig().getBoolean(CONFIG_ENABLE_TIMERS))
            doorTasks = new HashMap<>();
        else {
            // For updated config, if we had timers enabled before, cancel all running timers and null them out.
            if (doorTasks != null)
                doorTasks.entrySet().forEach((e) -> e.getValue().cancel());
            doorTasks = null;
        }
    }

    // Currently there is only one command to be run - `sd reload`
    // If in the future there is need for more complicated commands (closing all doors, etc.) we should
    // break this out into a separate class.
    @Override
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

    /**
     * Adds the received SecretOpenable instance to this.
     * If timers are enabled (i.e. getConfig().getBoolean(CONFIG_ENABLE_TIMERS) == true), then a task is created to
     * close {@code door} based on the configured duration.
     * @param door SecretOpenable to be added to this.
     * @return returns door.
     */
    public SecretOpenable addDoor(SecretOpenable door) {
        this.doors.put(door.getKey(), door);

        // add a task to close the door after the time
        if (getConfig().getBoolean(CONFIG_ENABLE_TIMERS)) {
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    closeDoorAuto(door.getKey());
                }
            };
            task.runTaskLater(this, 20 * this.closeTime);
            doorTasks.put(door.getKey(), task);
        }

        return door;
    }


    /**
     * Returns true if the received block's Material type is considered to be valid.
     * A Material type, m, is considered valid if:
     *  - m is not an "attachable" (see
     *  - whitelist is enabled for this instance and m is an element of the whitelist
     *  - whitelist is not enabled and m is not an element of the blacklist
     * @param block Block to be checked for validity
     */
    public boolean isValidBlock(Block block) {

        if (block != null) {

            if (SecretDoorHelper.isAttachableItem(block.getType())) {
                return false;
            }

            // first check if we're using the whitelist
            if (whitelist != null) {
                return whitelist.contains(block.getType());
            }

            // now check for the blacklist
            return !blacklist.contains(block.getType());
        }

        return false;
    }

    /**
     * Determines if the received {@code door} can be used to create
     * object.  More concretely, this method will return true iff:<br />
     * - door has Material type WOODEN_DOOR
     * - the two blocks directly in front of the door are considered valid blocks
     * - the door is closed
     */
    public boolean canBeSecretDoor(Block door) {
        if (!SecretDoorHelper.isValidDoor(door))
            return false;
        door = SecretDoorHelper.getKeyFromBlock(door);
        // If the door is already opened, return false.
        if (((Door) door.getBlockData()).isOpen())
            return false;
        BlockFace face = SecretDoorHelper.getDoorFace(door);

        Block bottom = door.getRelative(face);
        Block top    = bottom.getRelative(BlockFace.UP);
        // This is done to avoid creating a door with AIR blocks after a door is opened.
        // It's handled this way instead of adding Material.AIR to the black list so that doors can still be created
        // when only one block is used.
        if (bottom.getType() != Material.AIR || top.getType() != Material.AIR)
            if (isValidBlock(bottom) && isValidBlock(top)) // AIR is considered `valid` in this case
                return true;
        return false;

    }

    /**
     * Returns true iff door can be used to create a .
     * More concretely, canBeSecretTrapdoor returns true iff:<br />
     * - door has Material type TRAP_DOOR
     * - door is attached on the upper half of a block
     * - the block directly above door is considered a valid block
     */
    public boolean canBeSecretTrapdoor(Block door) {
        if (!SecretDoorHelper.isValidTrapDoor(door))
            return false;
        // If the door is already opened, return false.
        if (((TrapDoor)door.getBlockData()).isOpen())
            return false;

        Block above = door.getRelative(BlockFace.UP);
        // This is done to avoid creating a door with AIR blocks after a door is opened.
        // It's handled this way instead of adding Material.AIR to the black list so that doors can still be created
        // when only one block is used.
        if (above.getType() != Material.AIR)
            if (isValidBlock(above)) // AIR is considered `valid` in this case
                return true;
        return false;

    }

    /**
     * @return true if the received Block, door, is an opened SecretOpenable.
     */
    public boolean isSecretDoor(Block door) {
        return this.doors.containsKey(door);
    }

    /**
     * If the received Block, door, represents an opened SecretOpenable, then the object is closed and
     * removed from this.
     * @param door The key block to be closed.
     */
    public void closeDoor(Block door) {
        if (isSecretDoor(door)) {

            SecretOpenable secretDoor = this.doors.remove(door);
            Block key = secretDoor.getKey();
            secretDoor.close();
            // remove and cancel the auto-task if the user manually closed the door
            if (getConfig().getBoolean(CONFIG_ENABLE_TIMERS))
                doorTasks.remove(key).cancel();
        }
    }

    // Helper for timed closing of doors.
    // "Programmatically" closes the door without a player click.
    private void closeDoorAuto(Block door) {
        if (isSecretDoor(door)) {

            SecretOpenable secretDoor = this.doors.remove(door);
            Block key = secretDoor.getKey();
            secretDoor.close();
            // NOTE: bit of an awkward hack, really.  This is relying on likely unspecified behavior of doors and
            // trapdoors using the same bits to represented openness.  Should abstract this into the
            // SecretOpenable interface.
            key.setBlockData(key.getBlockData());//setData((byte) (key.getData() & ~0x4));
            key.getWorld().playEffect(key.getLocation(), Effect.DOOR_TOGGLE, 0);
            doorTasks.remove(key);
        }
    }

}