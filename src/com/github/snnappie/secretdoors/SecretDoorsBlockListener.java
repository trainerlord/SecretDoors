package com.github.snnappie.secretdoors;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class SecretDoorsBlockListener implements Listener {
	private SecretDoors plugin = null;

	public SecretDoorsBlockListener(SecretDoors plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent bbe) {
		if (bbe.getBlock().getType() == Material.WOODEN_DOOR) {
			
			Block block = bbe.getBlock();
			if ((block.getData() & 0x8) == 0x8)
				block = block.getRelative(BlockFace.DOWN);
				if (this.plugin.isSecretDoor(block))
					this.plugin.closeDoor(block);
		}
	}
}