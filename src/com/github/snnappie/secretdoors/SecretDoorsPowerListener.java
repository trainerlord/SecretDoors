package com.github.snnappie.secretdoors;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


import org.bukkit.event.block.BlockRedstoneEvent;

import com.github.snnappie.secretdoors.SecretDoor.Direction;


/**
 * 
 * @author Snnappie
 * 
 *
 */
public class SecretDoorsPowerListener implements Listener {

	
	private SecretDoors plugin;
	
	public SecretDoorsPowerListener(SecretDoors plugin) {
		this.plugin = plugin;
	}
	
	
	@EventHandler
	public void onBlockPowered(BlockRedstoneEvent event) {
		if (event.getBlock().getType() == Material.WOODEN_DOOR && !SecretDoor.isDoubleDoor(event.getBlock()) && plugin.getConfig().getBoolean("enable-redstone")) {
			Block door = event.getBlock();
			
			// open the door
			if (!isOpened(door) && SecretDoor.canBeSecretDoor(door)) {
				plugin.addDoor(new SecretDoor(door, door.getRelative(SecretDoor.getDoorFace(door)), Direction.DOOR_FIRST)).open();
			}
			
			// close the door
			if (isOpened(door) && plugin.isSecretDoor(SecretDoor.getKeyFromBlock(door))) {
				plugin.closeDoor(SecretDoor.getKeyFromBlock(door));
			}
		}
	}
	/**
	 * 
	 * @param door
	 * @return Returns true if the door is opened
	 */
	private boolean isOpened(Block door) {
		byte data = door.getData();
		if ((data & 0x8) == 0x8) {
			door = door.getRelative(BlockFace.DOWN);
			data = door.getData();
		}
		
		return ((data & 0x4) == 0x4);
	}
	
}
