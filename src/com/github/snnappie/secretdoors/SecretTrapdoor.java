package com.github.snnappie.secretdoors;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.TrapDoor;

public class SecretTrapdoor {

	private Block doorBlock;
	private byte doorData;
	private BlockFace direction;

	private Block above;
	private Material mat;
	private byte aboveData;

	public SecretTrapdoor(Block doorBlock, Block above) {
		if (doorBlock.getType() == Material.TRAP_DOOR) {
			this.doorBlock = doorBlock;
			direction = new TrapDoor(doorBlock.getType(), doorBlock.getData()).getAttachedFace().getOppositeFace();
			this.above = above;

			mat = this.above.getType();
			aboveData = this.above.getData();
			doorData = this.doorBlock.getData();
		}

	}

	public void open() {
		doorBlock.setType(Material.LADDER);
		doorBlock.setData(getDirectionData(doorBlock));

		above.setType(Material.AIR);
	}

	public void close() {
		doorBlock.setType(Material.TRAP_DOOR);
		doorBlock.setData(doorData);

		above.setType(mat);
		above.setData(aboveData);
		doorBlock.getWorld().playEffect(doorBlock.getLocation(), Effect.DOOR_TOGGLE, 0);
	}
	
	public Block getKey() {
		return doorBlock.getType() == Material.LADDER ? doorBlock : null;
	}
	
	private byte getDirectionData(Block block) {
		switch (direction) {
		case NORTH: return 0x2;
		case SOUTH: return 0x3;
		case WEST: return 0x4;
		case EAST: return 0x5;
		default: return 0;
		}
	}
}
