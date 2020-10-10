/*
 * BlockListener.java
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

package com.development.trainerlord.secretdoors.listeners;

import com.development.trainerlord.secretdoors.SecretDoorHelper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.development.trainerlord.secretdoors.SecretDoors;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * BlockListener defines EventHandler methods for placing and destroying blocks that are used in SecretDoors and
 * SecretTrapdoors.  Primarily closing of SecretOpenables when a door is broken and permissions on creating
 * SecretOpenables.
 */
public class BlockListener implements Listener {


    private static final String TRAPDOOR_MSG   = ChatColor.RED + "You do not have permission to create Secret Trapdoors!";
    private static final String SECRETDOOR_MSG = ChatColor.RED + "You do not have permission to create SecretDoors!";

    private SecretDoors plugin;

    public BlockListener(SecretDoors plugin) {
        this.plugin = plugin;
    }

    /** Close the door/trapdoor if a user breaks the door block. */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent bbe) {
        Block block = bbe.getBlock();
        Block key = SecretDoorHelper.getKeyFromBlock(block);
        // Was a wooden door in this case
        if (key != null && plugin.isSecretDoor(key))
            plugin.closeDoor(key);
        // This case is handled separately for the sake of preventing the ladder from appearing.
        // If the user broke the ladder used to store a secret trapdoor, we should cancel the break event.
        else if (plugin.isSecretDoor(block)) {
            plugin.closeDoor(block);
            bbe.setCancelled(true);
        } else {
            Block ladder = getAttachedLadder(block);
            if (ladder != null && plugin.isSecretDoor(ladder)) {
                plugin.closeDoor(ladder);
            }
        }
    }

    /** Primarily for handling permissions on creating SecretOpenable objects */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        Player player = event.getPlayer();
        // don't bother with other checks if they have permissions
        if (!plugin.getConfig().getBoolean(SecretDoors.CONFIG_PERMISSIONS_ENABLED) ||
            player.hasPermission(SecretDoors.PERMISSION_SD_CREATE))
            return;

        Block block = event.getBlock();
        // check if they placed a door that could be a secret door
        if (plugin.canBeSecretDoor(block)) {
            player.sendMessage(SECRETDOOR_MSG);
            player.updateInventory();
            event.setCancelled(true);
        } else if (plugin.canBeSecretTrapdoor(block)) {
            // handle when a trapdoor is placed that could be a secret trap door
            player.sendMessage(TRAPDOOR_MSG);
            player.updateInventory();
            event.setCancelled(true);
        } else {
            // Handle placement of a block near a door or trapdoor.

            // Check trapdoor first
            if (plugin.canBeSecretTrapdoor(block.getRelative(BlockFace.DOWN))) {
                player.sendMessage(TRAPDOOR_MSG);
                player.updateInventory();
                event.setCancelled(true);
            } else {
                // Check placing a block in front of a door.
                for (BlockFace f : SecretDoorHelper.DIRECTIONS) {
                    Block relative = block.getRelative(f);
                    if (plugin.canBeSecretDoor(relative)) {
                        player.sendMessage(SECRETDOOR_MSG);
                        player.updateInventory();
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        }
    }

    // Looks for a ladder block attached to the received Block.
    // Returns null if one is not found.
    private Block getAttachedLadder(Block block) {
        for (BlockFace face : SecretDoorHelper.DIRECTIONS) {
            if (block.getRelative(face).getType() == Material.LADDER && compareDirection(block.getRelative(face), face))
                return block.getRelative(face);
        }

        return null;
    }

    private boolean compareDirection(Block ladder, BlockFace direction) {
        byte data = ladder.getData();
        switch (direction) {
        case NORTH: return (data & 0x2) == 0x2;
        case SOUTH: return (data & 0x3) == 0x3;
        case WEST: return (data & 0x4) == 0x4;
        case EAST: return (data & 0x5) == 0x5;
        default: return false;
        }
    }

}