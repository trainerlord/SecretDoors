package com.github.snnappie.secretdoors;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.material.Button;
import org.bukkit.material.Ladder;
import org.bukkit.material.Lever;
import org.bukkit.material.SimpleAttachableMaterialData;
import org.bukkit.material.Torch;
import org.bukkit.material.Vine;

/**
 * Original Author: MrChick, updated by dill01 and now updated by Snnappie
 * @author Snnappie
 *
 */
public class SecretDoor {
	
	private Block doorBlock;
	private Block[] blocks = new Block[2];
	private Material[] materials = new Material[2];
	private byte[] data = new byte[2];
	
	private Block[] attachedBlocks = new Block[8];
	private Material[] attachedMats = new Material[8];
	private byte[] attachedData = new byte[8];
	private Direction direction = null;

	private int attachedCount = 0;
	
	private String[][] signText = new String[8][4];
	
	// 0 is top block/value, 1 is bottom
	public SecretDoor(Block door, Block other, Direction direction) {
		if (door.getType() == Material.WOODEN_DOOR) { // is door
			
			if ((door.getData() & 0x8) == 0x8) { // is upper half
				doorBlock = door.getRelative(BlockFace.DOWN);

				this.blocks[0] = other;
				this.blocks[1] = other.getRelative(BlockFace.DOWN);

			} else {
				doorBlock = door;
				this.blocks[1] = other;
				this.blocks[0] = other.getRelative(BlockFace.UP);
			}
		}

		this.materials[0] = this.blocks[0].getType();
		this.materials[1] = this.blocks[1].getType();

		this.data[0] = this.blocks[0].getData();
		this.data[1] = this.blocks[1].getData();

		this.direction = direction;
		
		// handle attached blocks (torches, signs, etc)
		BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN };
		for (BlockFace face : faces) {
			for (int i = 0; i < 2; i++) {
				if (isAttachableItem(blocks[i].getRelative(face).getType())) {
					
					// if it is a simple attachable item
					SimpleAttachableMaterialData sam = getSimpleAttachable(blocks[i].getRelative(face));
					// skip it if it isn't facing the same way
					if (sam != null && sam.getFacing() != face)
						continue;
					// handle sign text
					if (blocks[i].getRelative(face).getType() == Material.WALL_SIGN) {
						
						// if the sign isn't attached to the door blocks, skip it
						org.bukkit.material.Sign sM = new org.bukkit.material.Sign(blocks[i]
								.getRelative(face).getTypeId(), blocks[i].getRelative(face).getData());
						if (sM.getFacing() != face)
							continue;
						Sign s = (Sign) (blocks[i].getRelative(face).getState());
						
						// get the text from the sign
						for (int j = 0; j < 4; j++) {
							if (s.getLine(j) != null) {
								signText[attachedCount][j] = s.getLine(j);
							} else {
								signText[attachedCount][j] = "";
							}
						}
					}
					
					// NOTE: special case: vines
					if (blocks[i].getRelative(face).getType() == Material.VINE) {
						Vine vine = new Vine(blocks[i].getRelative(face).getTypeId(),
								blocks[i].getRelative(face).getData());
						
						// if the vine isn't attached to the door block, skip it
						if (vine.isOnFace(face)) continue;
					}
					attachedBlocks[attachedCount] = blocks[i].getRelative(face);
					attachedMats[attachedCount] = attachedBlocks[attachedCount].getType();
					attachedData[attachedCount] = attachedBlocks[attachedCount].getData();
					attachedCount++;
				}
			}
		}
	}


	// closes the door
	public void close() {
		for (int i = 0; i < 2; i++) {
			this.blocks[i].setType(this.materials[i]);
			this.blocks[i].setData(this.data[i]);
		}
		// handle attached blocks
		for (int i = 0; i < attachedCount; i++) {
			
			attachedBlocks[i].setType(attachedMats[i]);
			attachedBlocks[i].setData(attachedData[i]);
			
			// handle sign text
			if (attachedBlocks[i].getType() == Material.WALL_SIGN || attachedBlocks[i].getType() == Material.SIGN_POST) {
				Sign s = (Sign) (attachedBlocks[i].getState());
				for (int j = 0; j < 4; j++) {
					s.setLine(j, signText[i][j]);
				}
				s.update(true);
			}
		}
	}

	// opens the door
	public void open() {
		
		// handle attached blocks
		for (int i = 0; i < attachedCount; i++) {
			attachedBlocks[i].setType(Material.AIR);
		}
		
		for (int i = 0; i < 2; i++) {
			this.blocks[i].setType(Material.AIR);
		}
		
		if (this.direction == Direction.BLOCK_FIRST) {
			this.doorBlock.setData((byte) (this.doorBlock.getData() | 0x4));
			doorBlock.getWorld().playEffect(doorBlock.getLocation(), Effect.DOOR_TOGGLE, 0);
		}

	}
	
	public Block getKey() {
		return doorBlock;
	}
	
	public static boolean isDoubleDoor(Block block) {
		boolean state = false;

		Block[] blocks = { block.getRelative(BlockFace.EAST),
				block.getRelative(BlockFace.NORTH),
				block.getRelative(BlockFace.WEST),
				block.getRelative(BlockFace.SOUTH) };
		for (Block b : blocks) {
			if (Material.WOODEN_DOOR.equals(b.getType()))
				state = true;
		}
		return state;
	}
	

	
	public static Block getKeyFromBlock(Block block) {
		Block door = null;

		if (block.getType() == Material.WOODEN_DOOR) {
			// return lower half only
			if ((block.getData() & 0x8) == 0x8)
				door = block.getRelative(BlockFace.DOWN);
			else {
				door = block;
			}
		}
		return door;
	}
	

	/*
	 * Blacklist of invalid blocks
	 * TODO adapt for a configurable list
	 */
	public static boolean isValidBlock(Block block) {

		if (block != null) {
			
			if (isAttachableItem(block.getType())) {
				return false;
			}
			switch (block.getType()) {
			case DISPENSER:
			case CHEST:
			case WORKBENCH:
			case FURNACE:
			case BURNING_FURNACE:
			case STONE_PLATE:
			case WOOD_PLATE:
			case REDSTONE:
			case REDSTONE_WIRE:
			case REDSTONE_TORCH_ON:
			case REDSTONE_TORCH_OFF:
				return false;
			default:
				return true;
			}
		}
		
		return false;
	}


	/*
	 * checks if the item can be attached to a block
	 */
	public static boolean isAttachableItem(Material item) {
		
		if (item != null) {
			switch (item) {
			case TORCH:
			case SIGN:
			//case SIGN_POST:
			case WALL_SIGN:
			//case REDSTONE_TORCH_ON:
			//case PAINTING:
			case LEVER:
			case STONE_BUTTON: // NOTE: for whatever reason, Material enum doesn't include wooden buttons
			case LADDER:
			//case WEB:
			case VINE:
//			case ITEM_FRAME:
				return true;
			default:
				return false;
			}
		}
		return false;
	}
	
	// TODO review the necessity of this method
	// used in constructor to determine if hanging blocks are attached to the door blocks
	private static SimpleAttachableMaterialData getSimpleAttachable(Block item) {

		if (item != null) {
			int id = item.getTypeId();
			byte data = item.getData();
			switch (item.getType()) {
			case TORCH: return new Torch(id, data);
			case LADDER: return new Ladder(id, data);
			case LEVER: return new Lever(id, data);
			case STONE_BUTTON: return new Button(id, data);
			default:
				return null;
			}
		}

		return null;
	}
	
	/*
	 * Checks if door block can be a secret door
	 */
	public static boolean canBeSecretDoor(Block door) {
		if (door.getType() != Material.WOODEN_DOOR)
			return false;
		BlockFace face = SecretDoor.getDoorFace(door);
		if ((door.getData() & 0x8) == 0x8) {
			door = door.getRelative(BlockFace.DOWN);
		}
		if (Material.AIR != door.getRelative(face).getType()
				|| Material.AIR != door.getRelative(face).getRelative(BlockFace.UP).getType()) {
			if (isValidBlock(door.getRelative(face))  && isValidBlock(door.getRelative(face).getRelative(BlockFace.UP)))
				return true;
		}
		
		return false;

	}
	
	/*
	 *  Returns the direction the door is facing while closed
	 */
	public static BlockFace getDoorFace(Block door) {
		byte data = door.getData();
		if ((data & 0x8) == 0x8) {
			door = door.getRelative(BlockFace.DOWN);
			data = door.getData();
		}
		
		if ((data & 0x3) == 0x3) return BlockFace.SOUTH;
		if ((data & 0x1) == 0x1) return BlockFace.NORTH;
		if ((data & 0x2) == 0x2) return BlockFace.EAST;
		if ((data & 0x0) == 0x0) return BlockFace.WEST;
		return null;
	}
	
	/*
	 * Old code
	 */
//	public static boolean isAdjacentDoor(Block doorBlock, BlockFace face) {
//		boolean state = false;
//
//		if (doorBlock.getType() == Material.WOODEN_DOOR) { // is door
//			byte d = doorBlock.getData();
//			if ((d & 0x8) == 0x8) {
//				doorBlock = doorBlock.getRelative(BlockFace.DOWN);
//				d = doorBlock.getData();
//			}
//			switch (face) {
//			case NORTH:
//				// state = BlockFace.SOUTH_EAST == door.getHingeCorner();
//				state = (d & 0x3) == 0x3; // facing south
//				break;
//			case SOUTH:
//				// state = BlockFace.NORTH_WEST == door.getHingeCorner();
//				state = (d & 0x1) == 0x1; // facing north
//				break;
//			case WEST:
//				// state = BlockFace.NORTH_EAST == door.getHingeCorner();
//				state = (d & 0x2) == 0x2; // facing east
//				break;
//			case EAST:
//				// state = BlockFace.SOUTH_WEST == door.getHingeCorner();
//				state = (d & 0x0) == 0x0; // facing west
//			default:
//				break;
//			}
//		}
//
//		return state;
//	}
	
	public static enum Direction {
		BLOCK_FIRST, DOOR_FIRST;
	}
}