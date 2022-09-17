package io.github.highright1234.seniorcentertwerkmachine.listener

import io.github.highright1234.seniorcentertwerkmachine.SeniorCenterTwerkMachine
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

object JoinQuitListener : Listener {
    @EventHandler
    fun PlayerJoinEvent.on() {
        SeniorCenterTwerkMachine.fakeServer.addPlayer(player)
    }

    @EventHandler
    fun PlayerQuitEvent.on() {
        SeniorCenterTwerkMachine.fakeServer.removePlayer(player)
    }
}