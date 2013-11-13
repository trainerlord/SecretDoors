package com.github.snnappie.secretdoors;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.*;

/**
 * @author Snnappie
 *         <p/>
 *         This class is essentially a set of static methods that are useful for SecretDoors.
 *         (I prefer it this way - I've been developing heavily in Scala and prefer the `companion object` concept to having static and non-static methods
 *         living in the same space)
 */
public class SecretDoorHelper {


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
                case WOODEN_DOOR:
                case IRON_DOOR:
                    return false;
                default:
                    return true;
            }
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
                case VINE:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    // TODO review the necessity of this method
    // used in constructor to determine if hanging blocks are attached to the door blocks
    public static Attachable getSimpleAttachable(Block item) {

        if (item != null) {
            int id = item.getTypeId();
            byte data = item.getData();
            switch (item.getType()) {
                case TORCH:
                    return new Torch(id, data);
                case LADDER:
                    return new Ladder(id, data);
                case LEVER:
                    return new Lever(id, data);
                case STONE_BUTTON:
                    return new Button(id, data);
                case WALL_SIGN:
                    return new Sign(id, data);
                default:
                    return null;
            }
        }

        return null;
    }

    /**
     * Checks if door block can be a secret door
     */
    public static boolean canBeSecretDoor(Block door) {
        if (door.getType() != Material.WOODEN_DOOR)
            return false;
        BlockFace face = getDoorFace(door);
        if (isTopHalf(door)) {
            door = door.getRelative(BlockFace.DOWN);
        }
        if (Material.AIR != door.getRelative(face).getType()
                || Material.AIR != door.getRelative(face).getRelative(BlockFace.UP).getType()) {
            if (isValidBlock(door.getRelative(face)) && isValidBlock(door.getRelative(face).getRelative(BlockFace.UP)))
                return true;
        }

        return false;

    }

    /**
     * Returns the direction the door is facing while closed
     */
    public static BlockFace getDoorFace(Block door) {

        byte data = isTopHalf(door) ? door.getRelative(BlockFace.DOWN).getData() : door.getData();
        if ((data & 0x3) == 0x3) return BlockFace.SOUTH;
        if ((data & 0x1) == 0x1) return BlockFace.NORTH;
        if ((data & 0x2) == 0x2) return BlockFace.EAST;
        return BlockFace.WEST;
    }

    /**
     * Determines if the Block was clicked or the Door was clicked.
     * TODO: review if this is even necessary - if so, consider re-naming.  `Direction` doesn't accurately describe it's use.
     */
    public static enum Direction {
        BLOCK_FIRST, DOOR_FIRST
    }
}
