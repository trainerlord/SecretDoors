/*
 * CloseDoorTask.java
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

package com.github.snnappie.secretdoors.tasks;

import com.github.snnappie.secretdoors.SecretDoor;
import com.github.snnappie.secretdoors.SecretDoors;
import org.bukkit.scheduler.BukkitRunnable;


public class CloseDoorTask extends BukkitRunnable {


    private SecretDoor door;
    private SecretDoors plugin;
    public CloseDoorTask(SecretDoors plugin, SecretDoor door) { this.plugin = plugin; this.door = door; }
    @Override
    public void run() {
        plugin.closeDoorAuto(door.getKey());
    }
}
