package io.github.highright1234.seniorcentertwerkmachine.event

import io.github.highright1234.seniorcentertwerkmachine.TwerkingMachine
import io.github.monun.tap.fake.FakeEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class MachineInteractEvent(val player: Player, val machine: TwerkingMachine): Event() {
    companion object {
        private val handlerList = HandlerList()
        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }
    override fun getHandlers(): HandlerList = handlerList
}