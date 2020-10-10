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
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;
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
                        "\tisOpened == " + isOpened(door) +
                        "\n\tisPowered == " + door.isBlockPowered()
                );
            }

            Block key = SecretDoorHelper.getKeyFromBlock(door);

            // open the door
            if (!isOpened(door) && plugin.canBeSecretDoor(door) && !plugin.isSecretDoor(key)) {
                plugin.addDoor(new SecretDoor(door, door.getRelative(SecretDoorHelper.getDoorFace(door)),
                               SecretDoorHelper.Orientation.DOOR_FIRST
                )).open();
            }

            // close the door
            else if (isOpened(door) && plugin.isSecretDoor(key)) {
                plugin.closeDoor(key);
            }
        }
    }

    // return Returns true if the door is opened.
    // Assumes that door has material type is a valid door
    private boolean isOpened(Block door) {
        return ((Door)SecretDoorHelper.getKeyFromBlock(door).getBlockData()).isOpen();
    }

}
