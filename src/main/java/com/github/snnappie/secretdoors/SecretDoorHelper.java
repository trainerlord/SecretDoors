/*
 * SecretDoorHelper.java
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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.*;

/**
 * Set of static utility helpers for operating on SecretDoors.
 * @author Snnappie
 */
public class SecretDoorHelper {

    private SecretDoorHelper() {}

    /**
     * The most commonly used BlockFace directions for door operations.
     */
    // Lack of trust for the JVM - make this static and allocate it once.  The JVM is probably smarter than me anyway,
    // but I don't trust it.
    public static BlockFace[] DIRECTIONS = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };

    /**
     * @return true if the received Block is considered a valid door type.
     */
    public static boolean isValidDoor(Block door) {
        if (door == null)
            return false;
        switch (door.getType()) {
            case WOODEN_DOOR:
            case ACACIA_DOOR:
            case BIRCH_DOOR:
            case DARK_OAK_DOOR:
            case JUNGLE_DOOR:
            case SPRUCE_DOOR:
                return true;
        }

        return false;
    }


    /**
     * @return true if the received block is of type WOODEN_DOOR and is the top block of the door.
     */
    public static boolean isTopHalf(Block door) {
        return (isValidDoor(door)) && (door.getData() & 0x8) == 0x8;
    }

    /**
     * @return the bottom half of the door block if {@code isValidDoor(block)}.  Returns {@code null}
     *         otherwise.
     */
    public static Block getKeyFromBlock(Block block) {
        Block door = null;

        if (isValidDoor(block)) {
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
     * Returns true if item is considered a valid attachable item.  Note that valid attachable items is a subset
     * of all attachable items.
     * @param item Material type to be checked.
     * @return true if item is a valid attachable item, false otherwise.
     */
    public static boolean isAttachableItem(Material item) {

        if (item != null) {
            switch (item) {
                case TORCH:
                case SIGN:
                case WALL_SIGN:
                case LEVER:
                case STONE_BUTTON:
                case WOOD_BUTTON:
                case LADDER:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    /**
     * Casts the received block to {@link org.bukkit.material.Attachable} if it is considered a valid attachable type.
     * Note that valid attachable types are defined by {@link #isAttachableItem(org.bukkit.Material)}.
     * If block is not a valid attachable type, then null is returned instead.
     * @param block Block to be cast to Attachable
     * @return block as an attachable, or null if it could not be cast.
     */
    public static Attachable getAttachableFromBlock(Block block) {
        return isAttachableItem(block.getType()) ? (Attachable) block.getState().getData() : null;
    }

    /**
     * Returns the {@link org.bukkit.block.BlockFace} that the door block will be facing while it is closed.
     * If door is not of type WOODEN_DOOR (that is, {@code !isValidDoor(door)}, then {@code null}
     * is returned instead.
     * @param door door block to determine it's facing.
     * @return Returns the direction the door is facing while closed or null if door is not a WOODEN_DOOR.
     */
    public static BlockFace getDoorFace(Block door) {
        Block key = getKeyFromBlock(door);
        // This abstraction is broken with the new doors - they are not linked to Door.class and thus cannot be
        // down cast to Directional.
        // Instead we will check the bits ourselves
//        return key != null ? ((Directional) key.getState().getData()).getFacing() : null;
        if (key == null)
            return null;
        byte data = (byte) (key.getData() & ~4);
        switch (data) {
            case 0: return BlockFace.WEST;
            case 1: return BlockFace.NORTH;
            case 2: return BlockFace.EAST;
            case 3: return BlockFace.SOUTH;
        }

        return null;
    }

    /**
     * Orientation represents the state of which side of the door was clicked.  That is, Orientation has
     * two enumerated values, BLOCK_FIRST and DOOR_FIRST.
     */
    public static enum Orientation {
        BLOCK_FIRST, DOOR_FIRST
    }
}
