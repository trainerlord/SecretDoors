/*
 * CloseTrapDoorTask.java
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

import com.github.snnappie.secretdoors.SecretDoors;
import com.github.snnappie.secretdoors.SecretTrapdoor;
import org.bukkit.scheduler.BukkitRunnable;

public class CloseTrapDoorTask extends BukkitRunnable {

    private SecretDoors plugin;
    private SecretTrapdoor trapDoor;
    public CloseTrapDoorTask(SecretDoors plugin, SecretTrapdoor trapDoor) { this.plugin = plugin; this.trapDoor = trapDoor; }


    @Override
    public void run() {
        plugin.closeTrapdoorAuto(trapDoor.getKey());
    }
}
