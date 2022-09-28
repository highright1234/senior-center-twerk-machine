@file:Suppress("DEPRECATION")

package io.github.highright1234.seniorcentertwerkmachine

import io.github.highright1234.seniorcentertwerkmachine.util.NpcUtil
import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.mojangapi.MojangAPI
import kotlinx.coroutines.runBlocking
import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player
import java.util.*

data class TwerkingMachine(
    val fakePlayer: FakeEntity<Player>,
    val skinProfile: MojangAPI.SkinProfile
): ConfigurationSerializable {
    val vehicle get() = fakePlayer.vehicle
    fun remove() {
        NpcUtil.removeNpc(this)
    }

    override fun serialize(): MutableMap<String, Any> {
        val out = mutableMapOf<String, Any>()
        out["location"] = fakePlayer.vehicle!!.location
        out["uuid"] = fakePlayer.bukkitEntity.uniqueId.toString()
        out["name"] = fakePlayer.bukkitEntity.displayName
        out["skin-owner"] = MojangAPI.Profile("", skinProfile.id).uuid().toString()
        return out
    }

    suspend fun clone(
        newName: String = fakePlayer.bukkitEntity.displayName,
        newLocation: Location = vehicle!!.location,
        newUUID: UUID? = null,
        newSkinProfile: MojangAPI.SkinProfile? = skinProfile,
        deltaY: Double = 0.0
    ): TwerkingMachine = runBlocking {
        NpcUtil.createNpc(
            newName,
            newLocation,
            newUUID,
            newSkinProfile,
            deltaY
        )
    }.getOrThrow()

    companion object {

        @Suppress("Unused")
        @JvmStatic
        fun deserialize(args: Map<String, Any>): TwerkingMachine {
            val location = args["location"] as Location
            val uuid = (args["uuid"] as String).toUUID()
            val name = (args["name"] as String)
            val skinOwner = (args["skin-owner"] as String).toUUID()

            val npc = runBlocking {
                NpcUtil.createNpc(
                    name,
                    location,
                    uuid,
                    MojangAPI.fetchSkinProfile(skinOwner),
                    0.0
                )
            }.getOrThrow()
            return npc
        }

        private fun String.toUUID() = UUID.fromString(this)
    }
}