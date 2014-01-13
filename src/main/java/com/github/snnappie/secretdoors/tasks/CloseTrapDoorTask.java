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
