package io.github.highright1234.seniorcentertwerkmachine.invfx

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.highright1234.seniorcentertwerkmachine.SeniorCenterTwerkMachine.Companion.plugin
import io.github.highright1234.seniorcentertwerkmachine.TwerkingMachine
import io.github.highright1234.seniorcentertwerkmachine.config.TwerkingConfig
import io.github.highright1234.seniorcentertwerkmachine.util.listenAsync
import io.github.monun.invfx.InvFX.frame
import io.github.monun.invfx.frame.InvFrame
import io.github.monun.invfx.frame.InvSlot
import io.github.monun.tap.mojangapi.MojangAPI
import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

object EditInvfx {
    fun get(npc: TwerkingMachine) = frame(3, text("Edit twerking machine")) {
        val usedSlots = mutableListOf<Pair<Int, Int >>()
        fun InvSlot.add() {
            usedSlots += x to y
        }
        renameButton(npc).add()
        newSkinButton(npc).add()
        deleteButton(npc).add()
        val size = 3*9
        for (i in 0 until size) {
            if ((i%9 to i/9) in usedSlots) continue
            slot(i%9, i/9) {
                item = if (i % 2 == 0) {
                    ItemStack(Material.GRAY_STAINED_GLASS_PANE)
                } else {
                    ItemStack(Material.BLACK_STAINED_GLASS_PANE)
                }.apply {
                    itemMeta = itemMeta.apply { displayName(empty()) }
                }
            }
        }
    }

    private fun InvFrame.renameButton(npc: TwerkingMachine) = slot(1, 1) {
        item = ItemStack(Material.PAPER).apply {
            itemMeta = itemMeta.apply {
                displayName(
                    text("Rename")
                        .color(NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false)
                )
            }
        }
        onClick { event ->
            event.whoClicked.closeInventory()
            lateinit var job: Job
            job = plugin.launch {
                val player = event.whoClicked as Player
                player.sendMessage(text("Enter a name what you change into").color(NamedTextColor.GREEN))
                val timeOutJob = launch {
                    delay(TwerkingConfig.guiTimeOutDelay)
                    job.cancel()
                    player.sendMessage(text("Timed out").color(NamedTextColor.RED))
                }
                val chatEvent = player.listenAsync<AsyncChatEvent>().await()
                chatEvent.isCancelled = true
                val newName = chatEvent.message().string
                npc.remove()
                npc.clone(newName = newName)
                player.sendMessage(
                    text()
                        .append(text("The Machine's new name is "))
                        .append(text(newName))
                        .append(text("!"))
                        .color(NamedTextColor.GREEN)
                )
                timeOutJob.cancel()
            }
        }
    }

    private fun InvFrame.newSkinButton(npc: TwerkingMachine) = slot(4, 1) {
        item = ItemStack(Material.PLAYER_HEAD)
        plugin.launch {
            val skullMeta = item!!.itemMeta as SkullMeta
            val offlinePlayer = withContext(plugin.asyncDispatcher) {
                Bukkit.getOfflinePlayer(MojangAPI.Profile("", npc.skinProfile.id).uuid())
            }
            skullMeta.owningPlayer = offlinePlayer
            skullMeta.displayName(
                text("Edit skin")
                    .color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false)
            )
            item!!.itemMeta = skullMeta
        }
        onClick { event ->
            event.whoClicked.closeInventory()
            lateinit var job: Job
            job = plugin.launch {
                val player = event.whoClicked as Player
                player.sendMessage(text("Enter skin owner name"))
                val timeOutJob = launch {
                    delay(TwerkingConfig.guiTimeOutDelay)
                    job.cancel()
                    player.sendMessage(text("Timed out").color(NamedTextColor.RED))
                }
                val chatEvent = player.listenAsync<AsyncChatEvent>().await()
                chatEvent.isCancelled = true
                val newSkinOwnerName = chatEvent.message().string
                val skinProfile = withContext(plugin.asyncDispatcher) {
                    MojangAPI.fetchProfile(newSkinOwnerName)
                        ?.let { MojangAPI.fetchSkinProfile(it.uuid()) }
                }
                skinProfile ?: run {
                    player.sendMessage(
                        text("Unknown player")
                    )
                    timeOutJob.cancel()
                    return@launch
                }
                npc.remove()
                npc.clone(newSkinProfile = skinProfile)
                player.sendMessage(
                    text("The processing was successfully completed").color(NamedTextColor.GREEN)
                )
                timeOutJob.cancel()
            }
        }
    }

    private fun InvFrame.deleteButton(npc: TwerkingMachine) = slot(7, 1) {
        item = ItemStack(Material.BARRIER).apply {
            itemMeta = itemMeta.apply {
                displayName(
                    text("Delete")
                        .color(NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false)
                )
            }
        }
        onClick {
            npc.remove()
            it.whoClicked.closeInventory()
        }
    }

    private val Component.string
    get() = PlainTextComponentSerializer.plainText().serialize(this) // 이딴거 왜 해놨는지 이해 못하겠음
}