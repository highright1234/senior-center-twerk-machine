package io.github.highright1234.seniorcentertwerkmachine.util

import io.github.highright1234.seniorcentertwerkmachine.SeniorCenterTwerkMachine.Companion.plugin
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerQuitEvent

object ListeningPlayer {
    val listeningWaiting = mutableMapOf<Player, MutableList<Listener>>()
    fun activate() {
        plugin.server.pluginManager.registerEvents(PlayerUnregister, plugin)
    }

    fun <T: Event> register(clazz: Class<T>, player: Player, code: (T) -> Unit) {
        val listener: Listener = object: Listener {}
        plugin.server.pluginManager.registerEvent(
            clazz,
            listener,
            EventPriority.NORMAL,
            { _, event ->
                if (event is PlayerEvent && event.player == player) {
                    @Suppress("UNCHECKED_CAST")
                    runCatching {
                        code(event as T)
                    }
                    HandlerList.unregisterAll(listener)
                }
            },
            plugin,
            true
        )
    }

    private object PlayerUnregister: Listener {
        @EventHandler
        fun on(e: PlayerQuitEvent) {
            listeningWaiting[e.player]?.forEach(HandlerList::unregisterAll)
        }
    }
}

// withCancel 있는 이유는 Mc-coroutine 기술문제
inline fun<reified T: Event> Player.listenAsync(withCancel: Boolean = true): Deferred<T> {
    val completableDeferred = CompletableDeferred<T>()
    ListeningPlayer.register(T::class.java, this) {
        if (it is Cancellable && withCancel) {
            it.isCancelled = true
        }
        completableDeferred.complete(it)
    }
    return completableDeferred
}