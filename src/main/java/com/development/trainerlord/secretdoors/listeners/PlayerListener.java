/*
 * PlayerListener.java
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

import com.development.trainerlord.secretdoors.SecretDoor;
import com.development.trainerlord.secretdoors.SecretDoorHelper;
import com.development.trainerlord.secretdoors.SecretDoors;
import com.development.trainerlord.secretdoors.SecretTrapdoor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * PlayerListener defines EventHandler methods for player interactions with SecretDoors and SecretTrapdoors.
 */
public class PlayerListener implements Listener {

    private SecretDoors plugin = null;

    public PlayerListener(SecretDoors plugin) {
        this.plugin = plugin;
    }


    /**
     * Handle when the user clicks on a door
     */
    @EventHandler
    public void onDoorClick(PlayerInteractEvent event) {

        // handle permissions
        if (plugin.getConfig().getBoolean(SecretDoors.CONFIG_PERMISSIONS_ENABLED)) {
            if (!event.getPlayer().hasPermission(SecretDoors.PERMISSION_SD_USE)) {
                return;
            }
        }

        Block door = event.getClickedBlock();
        // right click and is door
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && SecretDoorHelper.isValidDoor(door)) {

            // is a closed secret door
            if (plugin.canBeSecretDoor(door)) {
                BlockFace doorFace = SecretDoorHelper.getDoorFace(door);

                // get the blocks in-front of the door
                Block other = door.getRelative(doorFace);
                plugin.addDoor(new SecretDoor(door, other, SecretDoorHelper.Orientation.DOOR_FIRST)).open();
            }
            // is an opened secret door
            else if (plugin.isSecretDoor(SecretDoorHelper.getKeyFromBlock(door))) {
                plugin.closeDoor(SecretDoorHelper.getKeyFromBlock(door));
            }
        }
    }

    /**
     * Handle when the user clicks on the block part of a secret door
     */
    @EventHandler
    public void onDoorBlockClick(PlayerInteractEvent event) {

        // handle permissions
        if (plugin.getConfig().getBoolean(SecretDoors.CONFIG_PERMISSIONS_ENABLED)) {
            if (!event.getPlayer().hasPermission(SecretDoors.PERMISSION_SD_USE)) {
                return;
            }
        }

        Block clicked = event.getClickedBlock();

        if (Action.RIGHT_CLICK_BLOCK.equals(event.getAction())) {
            // Quick exit to prevent additional checks
            if (SecretDoorHelper.isValidDoor(clicked))
                return;

            // handle `attached blocks` (signs, torches, etc)
            if (SecretDoorHelper.isAttachableItem(clicked.getType())) {

                Directional item = SecretDoorHelper.getAttachableFromBlock(clicked);
                BlockFace face  = SecretDoorHelper.getAttachableface(item);//item.getAttachedFace();
                Block block     = clicked.getRelative(face);
                Block door      = clicked.getRelative(face, 2);

                if (plugin.isValidBlock(block) && plugin.canBeSecretDoor(door)) {
                    plugin.addDoor(new SecretDoor(door, block, SecretDoorHelper.Orientation.BLOCK_FIRST)).open();
                }
            }
            // handle regular blocks (non-attachables)
            else if (plugin.isValidBlock(clicked)) {

                // Case: user is holding an attachable item already (they are attempting to place it on the door blocks)
                ItemStack heldItem = event.getItem();
                if (heldItem != null && SecretDoorHelper.isAttachableItem(heldItem.getType())) {
                    return;
                }

                // Handle opening the door regularly.
                BlockFace face  = event.getBlockFace().getOppositeFace();
                Block door      = clicked.getRelative(face);

                if (plugin.canBeSecretDoor(door)) {
                    plugin.addDoor(new SecretDoor(door, clicked, SecretDoorHelper.Orientation.BLOCK_FIRST)).open();
                }
            }
        }
    }


    /**
     * Handle opening SecretTrapdoors
     */
    @EventHandler
    public void onTrapdoorClick(PlayerInteractEvent event) {
        if (!plugin.getConfig().getBoolean(SecretDoors.CONFIG_ENABLE_TRAPDOORS)) {
            return;
        }

        
        if (plugin.getConfig().getBoolean(SecretDoors.CONFIG_PERMISSIONS_ENABLED))
            if (!event.getPlayer().hasPermission(SecretDoors.PERMISSION_SD_USE))
                return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            Block clicked = event.getClickedBlock();
            SecretTrapdoor door = null;

            if (plugin.canBeSecretTrapdoor(clicked))
                door = new SecretTrapdoor(clicked, clicked.getRelative(BlockFace.UP), false);
            else if (plugin.canBeSecretTrapdoor(clicked.getRelative(BlockFace.DOWN)))
                door = new SecretTrapdoor(clicked.getRelative(BlockFace.DOWN), clicked, true);
            else if (plugin.isSecretDoor(clicked))
                plugin.closeDoor(clicked);


            if (door != null) {
                event.setCancelled(true);
                door.open();
                plugin.addDoor(door);
            }

        }
    }

}