package com.github.snnappie.secretdoors.listeners;

import com.github.snnappie.secretdoors.SecretDoorHelper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.github.snnappie.secretdoors.SecretDoors;
import org.bukkit.event.block.BlockPlaceEvent;

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
		if (block.getType() == Material.WOODEN_DOOR) {
			
			if (SecretDoorHelper.isTopHalf(block))
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

    /** Primary for handling permissions on creating SecretDoors and Secret Trapdoors. */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        Player player = event.getPlayer();
        // don't bother with other checks if they have permissions
        if (!plugin.getConfig().getBoolean(SecretDoors.CONFIG_PERMISSIONS_ENABLED) || player.hasPermission(SecretDoors.PERMISSION_SD_CREATE))
            return;

        Block block = event.getBlock();
        // check if they placed a door that could be a secret door
        if (SecretDoorHelper.canBeSecretDoor(block)) {
            player.sendMessage(SECRETDOOR_MSG);
            player.updateInventory();
            event.setCancelled(true);
        } else if (SecretDoorHelper.canBeSecretTrapdoor(block)) {
            // handle when a trapdoor is placed that could be a secret trap door
            player.sendMessage(TRAPDOOR_MSG);
            player.updateInventory();
            event.setCancelled(true);
        } else {
            // handle placement of a block near a door or trapdoor
            BlockFace[] directions = { BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST };
            // check trapdoor first
            if (SecretDoorHelper.canBeSecretTrapdoor(block.getRelative(BlockFace.DOWN))) {
                player.sendMessage(TRAPDOOR_MSG);
                player.updateInventory();
                event.setCancelled(true);
            } else {
                for (BlockFace f : directions) {
                    Block relative = block.getRelative(f);
                    if (SecretDoorHelper.canBeSecretDoor(relative)) {
                        player.sendMessage(SECRETDOOR_MSG);
                        player.updateInventory();
                        event.setCancelled(true);
                        break;
                    }
                }
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