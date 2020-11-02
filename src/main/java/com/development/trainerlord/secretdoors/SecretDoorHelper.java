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

package com.development.trainerlord.secretdoors;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.Door;

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
            case OAK_DOOR:
            case ACACIA_DOOR:
            case BIRCH_DOOR:
            case DARK_OAK_DOOR:
            case JUNGLE_DOOR:
            case SPRUCE_DOOR:
            case CRIMSON_DOOR:
            case WARPED_DOOR:
                return true;
        }

        return false;
    }

    public static boolean isValidTrapDoor(Block door) {
        if (door == null)
            return false;
        switch (door.getType()) {
            case OAK_TRAPDOOR:
            case ACACIA_TRAPDOOR:
            case BIRCH_TRAPDOOR:
            case DARK_OAK_TRAPDOOR:
            case JUNGLE_TRAPDOOR:
            case SPRUCE_TRAPDOOR:
            case CRIMSON_TRAPDOOR:
            case WARPED_TRAPDOOR:
                return true;
        }

        return false;
    }


    /**
     * @return true if the received block is of type WOODEN_DOOR and is the top block of the door.
     */
    public static boolean isTopHalf(Block door) {
        return (isValidDoor(door)) && ((Door)door.getBlockData()).getHalf() == Bisected.Half.TOP;
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
    public static Block getKeyFromTrapDoorBlock(Block block) {
        Block door = null;

        if (isValidTrapDoor(block)) {
            door = block;
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
                case OAK_SIGN:
                case ACACIA_SIGN:
                case BIRCH_SIGN:
                case DARK_OAK_SIGN:
                case JUNGLE_SIGN:
                case SPRUCE_SIGN:
                case CRIMSON_SIGN:
                case WARPED_SIGN:
                case OAK_WALL_SIGN:
                case ACACIA_WALL_SIGN:
                case BIRCH_WALL_SIGN:
                case DARK_OAK_WALL_SIGN:
                case JUNGLE_WALL_SIGN:
                case SPRUCE_WALL_SIGN:
                case CRIMSON_WALL_SIGN:
                case WARPED_WALL_SIGN:
                case LEVER:
                case STONE_BUTTON:
                case OAK_BUTTON:
                case ACACIA_BUTTON:
                case BIRCH_BUTTON:
                case DARK_OAK_BUTTON:
                case JUNGLE_BUTTON:
                case SPRUCE_BUTTON:
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
    public static Directional getAttachableFromBlock(Block block) {
        return isAttachableItem(block.getType()) ? ((Directional) block.getBlockData()) : null;
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
        BlockFace data = ((Directional) key.getBlockData()).getFacing();
        switch (data) {
            case EAST: return BlockFace.WEST;
            case SOUTH: return BlockFace.NORTH;
            case WEST: return BlockFace.EAST;
            case NORTH: return BlockFace.SOUTH;
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


    public static BlockFace getAttachableface(Directional block) {
        switch (block.getFacing()) {
            case NORTH:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.NORTH;
            case EAST:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.EAST;
            default:
                return null;
        }
    }
}
