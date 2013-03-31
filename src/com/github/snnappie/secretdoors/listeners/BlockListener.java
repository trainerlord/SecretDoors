package com.github.snnappie.secretdoors.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.github.snnappie.secretdoors.SecretDoors;

public class BlockListener implements Listener {
	private SecretDoors plugin = null;

	public BlockListener(SecretDoors plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent bbe) {
		Block block = bbe.getBlock();
		if (block.getType() == Material.WOODEN_DOOR) {
			
			if ((block.getData() & 0x8) == 0x8)
				block = block.getRelative(BlockFace.DOWN);
				if (this.plugin.isSecretDoor(block))
					this.plugin.closeDoor(block);
				
		} else if (block.getType() == Material.LADDER) {
			
			if (plugin.isSecretTrapdoor(block)) {
				bbe.setCancelled(true);
				plugin.closeTrapdoor(block);
			}
			
		} else {
			Block ladder = getAttachedLadder(block);
			if (ladder != null && plugin.isSecretTrapdoor(ladder)) {
				plugin.closeTrapdoor(ladder);
			}
		}
	}
	
	private Block getAttachedLadder(Block block) {
		BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
		for (BlockFace face : faces) {
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