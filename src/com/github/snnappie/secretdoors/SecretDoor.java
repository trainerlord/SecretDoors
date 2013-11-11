package com.github.snnappie.secretdoors;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.material.*;

/**
 * Original Author: MrChick, updated by dill01 and now updated by Snnappie
 * @author Snnappie
 *
 * Represents a SecretDoor object - contains info about the hidden blocks and anything attached to the blocks (e.g. torches, signs, etc)
 * Attached signs retain their text.
 */
public class SecretDoor {

	private Block doorBlock;
	private Block[] blocks = new Block[2];
	private Material[] materials = new Material[2];
	private byte[] data = new byte[2];
	
	private Block[] attachedBlocks = new Block[8];
	private Material[] attachedMats = new Material[8];
	private byte[] attachedData = new byte[8];
	private SecretDoorHelper.Direction direction = null;

	private int attachedCount = 0;
	
	private String[][] signText = new String[8][4];

    // TODO: review this for clean-up
	// 0 is top block/value, 1 is bottom
	public SecretDoor(Block door, Block other, SecretDoorHelper.Direction direction) {
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
				if (SecretDoorHelper.isAttachableItem(blocks[i].getRelative(face).getType())) {
					
					// if it is a simple attachable item
					Attachable sam = SecretDoorHelper.getSimpleAttachable(blocks[i].getRelative(face));
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
		
		if (this.direction == SecretDoorHelper.Direction.BLOCK_FIRST) {
			this.doorBlock.setData((byte) (this.doorBlock.getData() | 0x4));
			doorBlock.getWorld().playEffect(doorBlock.getLocation(), Effect.DOOR_TOGGLE, 0);
		}

	}

    // returns the key for this SecretDoor - used to store/retrieve the SecretDoor from a Map
	public Block getKey() {
		return doorBlock;
	}
}