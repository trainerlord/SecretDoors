/*
 * PowerListener.java
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

package com.development.trainerlord.secretdoors.listeners;

import com.development.trainerlord.secretdoors.SecretDoor;
import com.development.trainerlord.secretdoors.SecretDoorHelper;
import com.development.trainerlord.secretdoors.SecretDoors;
import com.development.trainerlord.secretdoors.SecretTrapdoor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;


/**
 * PowerListener handles closing and opening of doors if they are powered by Redstone.
 */
public class PowerListener implements Listener {

    private SecretDoors plugin;

    public PowerListener(SecretDoors plugin) {
        this.plugin = plugin;
    }

    // NOTE: This implementation does not currently support SecretTrapdoors
    @EventHandler
    public void onBlockPowered(BlockRedstoneEvent event) {
        Block door = event.getBlock();
        if (SecretDoorHelper.isValidDoor(door) &&
            plugin.getConfig().getBoolean(SecretDoors.CONFIG_ENABLE_REDSTONE))
        {

            if (SecretDoors.DEBUG) {
                System.out.println("Redstone handler called:\n" +
                        "\tisOpened == " + isOpenedDoor(door) +
                        "\n\tisPowered == " + door.isBlockPowered()
                );
            }

            Block key = SecretDoorHelper.getKeyFromBlock(door);

            // open the door
            if (!isOpenedDoor(door) && plugin.canBeSecretDoor(door) && !plugin.isSecretDoor(key)) {
                plugin.addDoor(new SecretDoor(door, door.getRelative(SecretDoorHelper.getDoorFace(door)),
                               SecretDoorHelper.Orientation.DOOR_FIRST
                )).open();
            }

            // close the door
            else if (isOpenedDoor(door) && plugin.isSecretDoor(key)) {
                plugin.closeDoor(key);
            }
        } else if (SecretDoorHelper.isValidTrapDoor(door) &&
                plugin.getConfig().getBoolean(SecretDoors.CONFIG_ENABLE_REDSTONE)) {

//////////////////////////////////////////////////////////////////////
            if (SecretDoors.DEBUG) {
                System.out.println("Redstone handler called:\n" +
                        "\tisOpened == " + isOpenedTrapDoor(door) +
                        "\n\tisPowered == " + door.isBlockPowered()
                );
            }

            Block key = SecretDoorHelper.getKeyFromBlock(door);

            // open the door
            if (!isOpenedTrapDoor(door) && plugin.canBeSecretTrapdoor(door) && !plugin.isSecretDoor(key)) {
                plugin.addDoor(new SecretTrapdoor(door, door.getRelative(BlockFace.UP),true)).open();
            }

            // close the door
            else if (isOpenedTrapDoor(door) && plugin.isSecretDoor(key)) {
                plugin.closeDoor(key);
            }
        }
    }

    // return Returns true if the door is opened.
    // Assumes that door has material type is a valid door
    private boolean isOpenedDoor(Block door) {
        return ((Door)SecretDoorHelper.getKeyFromBlock(door).getBlockData()).isOpen();
    }

    private boolean isOpenedTrapDoor(Block door) {
        return ((TrapDoor)SecretDoorHelper.getKeyFromTrapDoorBlock(door).getBlockData()).isOpen();
    }

}
