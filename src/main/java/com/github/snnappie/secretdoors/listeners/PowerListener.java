package com.github.snnappie.secretdoors.listeners;

import com.github.snnappie.secretdoors.SecretDoorHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


import org.bukkit.event.block.BlockRedstoneEvent;

import com.github.snnappie.secretdoors.SecretDoor;
import com.github.snnappie.secretdoors.SecretDoors;


/**
 * 
 * @author Snnappie
 * 
 *
 */
public class PowerListener implements Listener {

	
	private SecretDoors plugin;
	
	public PowerListener(SecretDoors plugin) {
		this.plugin = plugin;
	}
	
	
	@EventHandler
	public void onBlockPowered(BlockRedstoneEvent event) {
		if (event.getBlock().getType() == Material.WOODEN_DOOR && plugin.getConfig().getBoolean(SecretDoors.CONFIG_ENABLE_REDSTONE)) {
			Block door = event.getBlock();
			
			// open the door
			if (!isOpened(door) && SecretDoorHelper.canBeSecretDoor(door)) {
				plugin.addDoor(new SecretDoor(door, door.getRelative(SecretDoorHelper.getDoorFace(door)), SecretDoorHelper.Direction.DOOR_FIRST)).open();
			}
			
			// close the door
			if (isOpened(door) && plugin.isSecretDoor(SecretDoorHelper.getKeyFromBlock(door))) {
				plugin.closeDoor(SecretDoorHelper.getKeyFromBlock(door));
			}
		}
	}
	/**
	 *
	 * @return Returns true if the door is opened
	 */
	private boolean isOpened(Block door) {
		if (SecretDoorHelper.isTopHalf(door)) {
			door = door.getRelative(BlockFace.DOWN);
		}

        byte data = door.getData();
		return ((data & 0x4) == 0x4);
	}
	
}
