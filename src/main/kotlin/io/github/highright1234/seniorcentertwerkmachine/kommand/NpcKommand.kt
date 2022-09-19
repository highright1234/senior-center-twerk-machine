package io.github.highright1234.seniorcentertwerkmachine.kommand

import io.github.highright1234.seniorcentertwerkmachine.util.NpcUtil
import io.github.highright1234.seniorcentertwerkmachine.suspendExecutes
import io.github.monun.kommand.PluginKommand
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor

object NpcKommand {

    fun register(pluginKommand: PluginKommand) {
        pluginKommand.register("twerk", "t") {
            requires { isPlayer }
            then("npc-name" to string()) {
                suspendExecutes { kommandContext ->

                    player.sendMessage(text("Creating twerk machine").color(NamedTextColor.GREEN))
                    val result = NpcUtil.createNpc(kommandContext["npc-name"], player.location)

                    result.onSuccess { npc ->
                        player.sendMessage(
                            text()
                                .append(text("npc "))
                                .append(npc.bukkitEntity.displayName())
                                .append(text(" is successfully created"))
                                .color(NamedTextColor.GREEN)
                        )
                    }.onFailure {
                        player.sendMessage(text("Unknown player"))
                    }
                }
            }
        }
    }

}