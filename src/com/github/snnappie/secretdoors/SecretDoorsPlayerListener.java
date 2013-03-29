package com.github.snnappie.secretdoors;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SecretDoorsPlayerListener implements Listener {
	private SecretDoors plugin = null;


	public SecretDoorsPlayerListener(SecretDoors plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent pie) {
		if (Action.RIGHT_CLICK_BLOCK.equals(pie.getAction()) && (SecretDoor.isAttachableItem(pie.getClickedBlock().getType()) || SecretDoor.isValidBlock(pie.getClickedBlock()))) {
			
			// exit if the user clicked on the top or bottom block of a door/secret door block
			if (pie.getBlockFace() == BlockFace.UP || pie.getBlockFace() == BlockFace.DOWN)
				return;
			// user wants to use permissions
			if (plugin.getConfig().getBoolean("use-permissions")) {
				if (!pie.getPlayer().hasPermission("secretdoors.use")) {
					return;
				}
			}
			
			Block clicked = pie.getClickedBlock();
			Block behind = clicked.getRelative(pie.getBlockFace().getOppositeFace());
			SecretDoor door = null;

			if ((Material.WOODEN_DOOR.equals(clicked.getType())) && (!SecretDoor.isDoubleDoor(clicked))) {
				if (this.plugin.isSecretDoor(SecretDoor.getKeyFromBlock(clicked))) {
					this.plugin.closeDoor(SecretDoor.getKeyFromBlock(clicked));
				} else if (!Material.AIR.equals(behind.getType())) {
					if (SecretDoor.canBeSecretDoor(clicked)) {
						door = new SecretDoor(clicked, behind, SecretDoor.Direction.DOOR_FIRST);
					}
				}
			} else if ((Material.WOODEN_DOOR.equals(behind.getType())) && (!SecretDoor.isDoubleDoor(behind))) {
				// TODO adapt this to support opening if there is already an attached item on the block
				if (!SecretDoor.isAttachableItem(pie.getMaterial())) {
					if (this.plugin.isSecretDoor(SecretDoor.getKeyFromBlock(behind))) {
						this.plugin.closeDoor(SecretDoor.getKeyFromBlock(behind));
					} else if (SecretDoor.canBeSecretDoor(behind)) {
						door = new SecretDoor(behind, clicked, SecretDoor.Direction.BLOCK_FIRST);
					}
				}
				
			} else if (SecretDoor.isAttachableItem(clicked.getType()) && SecretDoor.isValidBlock(behind)) {
				Block doorBlock = behind.getRelative(pie.getBlockFace().getOppositeFace());
				if (doorBlock.getType() == Material.WOODEN_DOOR && !SecretDoor.isDoubleDoor(doorBlock)) {
					if (this.plugin.isSecretDoor(SecretDoor.getKeyFromBlock(doorBlock))) {
						this.plugin.closeDoor(SecretDoor.getKeyFromBlock(doorBlock));
					} else if (SecretDoor.canBeSecretDoor(doorBlock)) {
						door = new SecretDoor(doorBlock, behind, SecretDoor.Direction.BLOCK_FIRST);
					}
				}
				
			}
			
			
			if (door != null) {
				this.plugin.addDoor(door).open();
			}
		}
	}
	
}