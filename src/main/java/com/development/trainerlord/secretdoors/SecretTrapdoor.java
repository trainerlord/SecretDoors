/*
 * SecretTrapdoor.java
 * Last modified: 2014 12 20
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

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

public class SecretTrapdoor implements SecretOpenable {

    private Block doorBlock;
    private BlockData doorData;
    private BlockFace direction;

    private Block above;
    private Material mat;
    private BlockData aboveData;
    private boolean fromAbove;

    public SecretTrapdoor(Block doorBlock, Block above, boolean fromAbove) {

        switch (doorBlock.getType()) {
            case OAK_TRAPDOOR:
            case ACACIA_TRAPDOOR:
            case BIRCH_TRAPDOOR:
            case DARK_OAK_TRAPDOOR:
            case JUNGLE_TRAPDOOR:
            case SPRUCE_TRAPDOOR:
                this.doorBlock = doorBlock;
                this.fromAbove = fromAbove;
                direction = ((Directional) doorBlock.getBlockData()).getFacing().getOppositeFace();//new TrapDoor(doorBlock.getType(), doorBlock.getData()).getAttachedFace().getOppositeFace();
                this.above = above;

                mat = this.above.getType();
                aboveData = this.above.getBlockData();
                doorData = this.doorBlock.getBlockData();
        }

    }

    @Override
    public void open() {
        doorBlock.setType(Material.LADDER);
        doorBlock.setBlockData(getKey().getBlockData());//setData(getDirectionData());

        above.setType(Material.AIR);

        if (fromAbove)
            doorBlock.getWorld().playEffect(doorBlock.getLocation(), Effect.DOOR_TOGGLE, 0);
    }

    @Override
    public void close() {
        doorBlock.setType(Material.OAK_TRAPDOOR);//TODO Remember Type
        doorBlock.setBlockData(doorData);

        above.setType(mat);
        above.setBlockData(aboveData);
        doorBlock.getWorld().playEffect(doorBlock.getLocation(), Effect.DOOR_TOGGLE, 0);
    }

    @Override
    public Block getKey() {
        return doorBlock.getType() == Material.LADDER ? doorBlock : null;
    }

    private byte getDirectionData() {
        switch (direction) {
            case NORTH: return 0x2;
            case SOUTH: return 0x3;
            case WEST: return 0x4;
            case EAST: return 0x5;
            default: return 0;
        }
    }
}
