/*
 * SecretDoorHelper.java
 * Last modified: 2014 7 31
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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.*;

/**
 * @author Snnappie
 *         This class is essentially a set of static methods that are useful for SecretDoors.
 *         (I prefer it this way - I've been developing heavily in Scala and prefer the `companion object` concept to having static and non-static methods
 *         living in the same space)
 */
public class SecretDoorHelper {


    // no instantiating singleton objects
    private SecretDoorHelper() {}


    public static boolean isTopHalf(Block door) {
        if (door.getType() != Material.WOODEN_DOOR)
            throw new IllegalArgumentException("Incorrect Block type, expected WOODEN_DOOR but got " + door.getType());
        return (door.getData() & 0x8) == 0x8;
    }

    public static Block getKeyFromBlock(Block block) {
        Block door = null;

        if (block.getType() == Material.WOODEN_DOOR) {
            // return lower half only
            if (isTopHalf(block))
                door = block.getRelative(BlockFace.DOWN);
            else {
                door = block;
            }
        }
        return door;
    }

    /**
     * Blacklist of invalid blocks
     */
    public static boolean isValidBlock(Block block) {

        if (block != null) {

            if (isAttachableItem(block.getType())) {
                return false;
            }

            // first check if we're using the whitelist
            if (SecretDoors.whitelist != null) {
                return SecretDoors.whitelist.contains(block.getType());
            }

            // now check for the blacklist
            return !SecretDoors.blacklist.contains(block.getType());
        }

        return false;
    }

    /**
     * checks if the item can be attached to a block
     */
    public static boolean isAttachableItem(Material item) {

        if (item != null) {
            switch (item) {
                case TORCH:
                case SIGN:
                case WALL_SIGN:
                case LEVER:
                case STONE_BUTTON: // NOTE: for whatever reason, Material enum doesn't include wooden buttons
                case LADDER:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    public static Attachable getAttachableFromBlock(Block block) {

        return isAttachableItem(block.getType()) ? (Attachable) block.getState().getData() : null;
    }

    /**
     * Checks if door block can be a secret door
     */
    public static boolean canBeSecretDoor(Block door) {
        if (door.getType() != Material.WOODEN_DOOR)
            return false;
        BlockFace face = getDoorFace(door);
        door = getKeyFromBlock(door);

        Block bottom = door.getRelative(face);
        Block top    = bottom.getRelative(BlockFace.UP);
        // This is done to avoid creating a door with AIR blocks after a door is opened.
        // It's handled this way instead of adding Material.AIR to the black list so that doors can still be created
        // when only one block is used.
        if (bottom.getType() != Material.AIR || top.getType() != Material.AIR)
            if (isValidBlock(bottom) && isValidBlock(top)) // AIR is considered `valid` in this case
                return true;
        return false;

    }

    /**
     * Checks if a Trapdoor can be a Secret Trapdoor
     * Currently only allowed if the trapdoor is placed on the upper half of the block.
     */
    public static boolean canBeSecretTrapdoor(Block door) {
        Block relative = door.getRelative(BlockFace.UP);
        // limit it to being the upper side of the block
        return door.getType() == Material.TRAP_DOOR
                && ((door.getData() & 0x8) == 0x8)
                && relative.getType() != Material.AIR
                && isValidBlock(relative);
    }

    /**
     * Returns the direction the door is facing while closed
     */
    public static BlockFace getDoorFace(Block door) {
        return ((Directional) getKeyFromBlock(door).getState().getData()).getFacing();
    }

    /**
     * Determines if the Block was clicked or the Door was clicked.
     * TODO: review if this is even necessary - if so, consider re-naming.  `Direction` doesn't accurately describe it's use.
     */
    public static enum Direction {
        BLOCK_FIRST, DOOR_FIRST
    }
}
