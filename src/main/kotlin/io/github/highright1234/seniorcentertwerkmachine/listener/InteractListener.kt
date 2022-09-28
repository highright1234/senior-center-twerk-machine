package io.github.highright1234.seniorcentertwerkmachine.listener

import io.github.highright1234.seniorcentertwerkmachine.event.MachineInteractEvent
import io.github.highright1234.seniorcentertwerkmachine.invfx.EditInvfx
import io.github.highright1234.seniorcentertwerkmachine.util.NpcUtil
import io.github.monun.invfx.openFrame
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

object InteractListener: Listener {
    @EventHandler(priority = EventPriority.LOW)
    fun PlayerInteractEvent.onRightClick() {
        NpcUtil.raytraceMachines(
            player.eyeLocation,
            player.eyeLocation.direction,
        )?.let {
            if (player.isSneaking && action.isRightClick) {
                player.openFrame(EditInvfx.get(it))
            } else {
                Bukkit.getPluginManager().callEvent(MachineInteractEvent(player, it))
            }
        }
    }
}