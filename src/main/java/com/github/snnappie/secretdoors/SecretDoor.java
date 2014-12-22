/*
 * SecretDoor.java
 * Last modified: 2014 12 21
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
public class SecretDoor implements SecretOpenable {

    private Block doorBlock;
    private Block[] blocks = new Block[2];
    private Material[] materials = new Material[2];
    private MaterialData[] data = new MaterialData[2];
    private SecretDoorHelper.Orientation orientation = null;

    //// Attached state

    // References to the attached block objects
    private Block[] attachedBlocks;
    // Material type of the blocks while the door is closed (i.e. before they get set to AIR).
    private Material[] attachedMats;
    // API for changing directions is non-functional in Bukkit/Spigot v 1.8 - 2014-12-21
//    private BlockFace[] attachedDirections = new BlockFace[8];
    // data values for each attached block
    private byte[] attachedData;
    // Count of the number of items attached to this door.
    private int attachedCount = 0;
    // Contains the text of every sign that is attached to this door.
    private String[][] signText;

    public SecretDoor(Block door, Block other, SecretDoorHelper.Orientation orientation) {

        if (SecretDoors.DEBUG) {
            System.out.println("Door constructed at location: " + door.getLocation());
            System.out.println("Door is facing: " + SecretDoorHelper.getDoorFace(door));
        }
        if (SecretDoorHelper.isValidDoor(door)) { // is door

            if (SecretDoorHelper.isTopHalf(door)) { // is upper half
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

        this.data[0] = this.blocks[0].getState().getData().clone();
        this.data[1] = this.blocks[1].getState().getData().clone();

        if (SecretDoors.DEBUG) {
            System.out.println("Door blocks:" +
                    "\n\tblocks[0] == " + blocks[0] +
                    "\n\tblocks[1] == " + blocks[1] +
                    "\n\tdata[0] == " + data[0] +
                    "\n\tdata[1] == " + data[1]
            );

        }

        this.orientation = orientation;

        // handle attached blocks (torches, signs, etc)
        BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN };
        for (BlockFace face : faces) {
            for (int i = 0; i < 2; i++) {
                Block attached = blocks[i].getRelative(face);

                // NOTE: special case: vines
                if (attached.getType() == Material.VINE) {

                    // if the vine isn't attached to the door block, skip it
                    if (((Vine) attached.getState()).isOnFace(face))
                        continue;
                }

                // if it is a simple attachable item
                Attachable sam = SecretDoorHelper.getAttachableFromBlock(attached);
                if (sam != null) {
                    // skip it if it isn't facing the same way
                    if (sam.getFacing() != face)
                        continue;

                    // First time we've been here, initialize the arrays
                    if (attachedCount == 0) {
                        attachedBlocks = new Block[8];
                        attachedMats = new Material[8];
                        attachedData = new byte[8];
                    }

                    // handle sign text
                    if (attached.getType() == Material.WALL_SIGN) {
                        handleSignText(attached);
                    }

                    attachedBlocks[attachedCount] = attached;
                    attachedMats[attachedCount] = attached.getType();
                    // Broken API -- See above.
//                    attachedDirections[attachedCount] = sam.getFacing();
                    attachedData[attachedCount] = attached.getData();
                    attachedCount++;
                }
            }
        }
    }

    private void handleSignText(Block block) {
        // If it is the first sign, initialize the array
        if (attachedCount == 0) {
            signText = new String[8][4];
        }
        Sign s = (Sign) (block.getState());
        signText[attachedCount] = s.getLines();
    }


    @Override
    public void close() {
        for (int i = 0; i < 2; i++) {
            if (SecretDoors.DEBUG) {
                System.out.println("Setting data[" + i + "] from " + this.blocks[i].getType() + " to " + this.materials[i]);
                System.out.println("Setting " + this.blocks[i].getState().getData() + " to " + this.data[i]);
            }

            this.blocks[i].setType(this.materials[i]);
            // API is non-functional as of Bukkit/Spigot 1.8 - 2014-12-21
//			this.blocks[i].getState().setData(this.data[i]);
            this.blocks[i].setData(this.data[i].getData());
        }
        // handle attached blocks
        for (int i = 0; i < attachedCount; i++) {

            attachedBlocks[i].setType(attachedMats[i]);

            if (SecretDoors.DEBUG) {
                System.out.println("attachedBlocks[" + i + "] is facing " + ((Attachable) attachedBlocks[i].getState().getData()).getFacing());
                System.out.println("attachedBlocks[" + i + "] data == " + attachedBlocks[i].getData());
            }

            // This API is non-functional as of Bukkit/Spigot 1.8 - 2014-12-21
//            ((Attachable) attachedBlocks[i].getState().getData()).setFacingDirection(attachedDirections[i]);
            attachedBlocks[i].setData(attachedData[i]);

            if (SecretDoors.DEBUG) {
                System.out.println("attachedBlocks[" + i + "] is facing " + ((Attachable) attachedBlocks[i].getState().getData()).getFacing());
                System.out.println("attachedBlocks[" + i + "] data == " + attachedBlocks[i].getData());
            }

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


    @Override
    public void open() {

        // handle attached blocks
        for (int i = 0; i < attachedCount; i++) {
            attachedBlocks[i].setType(Material.AIR);
        }

        for (int i = 0; i < 2; i++) {
            this.blocks[i].setType(Material.AIR);
        }

        if (this.orientation == SecretDoorHelper.Orientation.BLOCK_FIRST) {
            this.doorBlock.setData((byte) (this.doorBlock.getData() | 0x4));
            doorBlock.getWorld().playEffect(doorBlock.getLocation(), Effect.DOOR_TOGGLE, 0);
        }

    }

    @Override
    public Block getKey() {
        return doorBlock;
    }
}