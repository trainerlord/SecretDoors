/*
 * PlayerListener.java
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

package com.github.snnappie.secretdoors.listeners;

import com.github.snnappie.secretdoors.SecretDoor;
import com.github.snnappie.secretdoors.SecretDoorHelper;
import com.github.snnappie.secretdoors.SecretDoors;
import com.github.snnappie.secretdoors.SecretTrapdoor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;

public class PlayerListener implements Listener {
	private SecretDoors plugin = null;


	public PlayerListener(SecretDoors plugin) {
		this.plugin = plugin;
	}


    /*
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
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && door.getType() == Material.WOODEN_DOOR) {

            // is a closed secret door
            if (SecretDoorHelper.canBeSecretDoor(door)) {
                BlockFace doorFace = SecretDoorHelper.getDoorFace(door);

                // get the blocks in-front of the door
                Block other = door.getRelative(doorFace);
                plugin.addDoor(new SecretDoor(door,other, SecretDoorHelper.Direction.DOOR_FIRST)).open();
            }
            // is an opened secret door
            else if (plugin.isSecretDoor(SecretDoorHelper.getKeyFromBlock(door))) {
                plugin.closeDoor(SecretDoorHelper.getKeyFromBlock(door));
            }
        }
    }

    /*
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

            // handle `attached blocks` (signs, torches, etc)
            if (SecretDoorHelper.isAttachableItem(clicked.getType())) {

                Attachable item = SecretDoorHelper.getAttachableFromBlock(clicked);
                BlockFace face  = item.getAttachedFace();
                Block block     = clicked.getRelative(face);
                Block door      = clicked.getRelative(face, 2);

                if (SecretDoorHelper.isValidBlock(block) && SecretDoorHelper.canBeSecretDoor(door)) {
                    plugin.addDoor(new SecretDoor(door,block, SecretDoorHelper.Direction.BLOCK_FIRST)).open();
                }
            }
            // handle regular blocks (non-attachables)
            else if (SecretDoorHelper.isValidBlock(clicked)) {

                // special case: user is holding an attachable item already (they are attempting to place it on the door blocks)
                ItemStack heldItem = event.getItem();
                if (heldItem != null && SecretDoorHelper.isAttachableItem(heldItem.getType())) {
                    return;
                }

                BlockFace face  = event.getBlockFace().getOppositeFace();
                Block door      = clicked.getRelative(face);

                if (SecretDoorHelper.canBeSecretDoor(door)) {
                    plugin.addDoor(new SecretDoor(door, clicked, SecretDoorHelper.Direction.BLOCK_FIRST)).open();
                }
            }
        }
    }


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

            if (SecretDoorHelper.canBeSecretTrapdoor(clicked))
                door = new SecretTrapdoor(clicked, clicked.getRelative(BlockFace.UP), false);
            else if (SecretDoorHelper.canBeSecretTrapdoor(clicked.getRelative(BlockFace.DOWN)))
                door = new SecretTrapdoor(clicked.getRelative(BlockFace.DOWN), clicked, true);
            else if (plugin.isSecretTrapdoor(clicked))
                plugin.closeTrapdoor(clicked);


            if (door != null) {
                event.setCancelled(true);
                door.open();
                plugin.addTrapdoor(door);
            }

		}
	}
	
}