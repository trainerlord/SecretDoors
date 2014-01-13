package com.github.snnappie.secretdoors;

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
