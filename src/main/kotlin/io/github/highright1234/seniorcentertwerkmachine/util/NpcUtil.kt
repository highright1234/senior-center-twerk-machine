package io.github.highright1234.seniorcentertwerkmachine.util

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.highright1234.seniorcentertwerkmachine.SeniorCenterTwerkMachine
import io.github.highright1234.seniorcentertwerkmachine.config.NpcDataConfig
import io.github.highright1234.seniorcentertwerkmachine.config.TwerkingConfig
import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.fake.setLocation
import io.github.monun.tap.mojangapi.MojangAPI
import io.github.monun.tap.protocol.PacketSupport
import io.github.monun.tap.protocol.sendPacket
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import java.util.UUID

object NpcUtil {
    suspend fun createNpc(
        name: String,
        location: Location,
        uuid: UUID? = null,
        skin: MojangAPI.SkinProfile? = null
    ) : Result<FakeEntity<Player>> {

        val profile = skin ?: profileOf(name) ?: run {
            return Result.failure(Exception("Not found profile of player"))
        }

        val armorStand = SeniorCenterTwerkMachine.fakeServer.spawnEntity(
            location.apply { y += TwerkingConfig.deltaY },
            ArmorStand::class.java
        ).apply { updateMetadata { isInvisible = true } } // 투명으로 아머스탠스 생성

        val npc = SeniorCenterTwerkMachine.fakeServer.spawnPlayer(
            location,
            name,
            profile.profileProperties().toSet(),
            uniqueId = uuid ?: UUID.randomUUID()
        ).apply {
            updateMetadata {
                isGliding = true
                isCollidable = false // 충돌 방진데 작동 안함 ㅋㅋ 망할
            }
            armorStand.addPassenger(this)
        } // 아머스탠드에 앉히며 겉날개로 나는 모션 생성

        npc.twerk()

        NpcDataConfig.npcData += NpcDataConfig.NpcInformation.fromNpc(npc, profile)
        return Result.success(npc)
    }

    private fun FakeEntity<Player>.twerk() = SeniorCenterTwerkMachine.plugin.launch {
        while (valid) {
            for (i in TwerkingConfig.pitchUpRange) {
                newPitch(i)
                delay(1)
            }
            delay(TwerkingConfig.delayBetweenPositionChanging)
            for (i in TwerkingConfig.pitchDownRange) {
                newPitch(i)
                delay(1)
            }
        }
    }

    private fun FakeEntity<Player>.newPitch(newPitch: Int) {

        val newLocation = location.clone()
            .apply { pitch = newPitch.toFloat() }
            .also { bukkitEntity.setLocation(it) }

        Bukkit.getOnlinePlayers().forEach { player ->
            player.sendPacket(
                PacketSupport.entityTeleport(bukkitEntity, newLocation)
            )
        }

    }

    private val profileImporter : MutableMap<String, Job> = mutableMapOf()
    private val cache : MutableMap<String, MojangAPI.SkinProfile?> = mutableMapOf()
    private val cacheRemover : MutableMap<String, Job> = mutableMapOf()

    private suspend fun profileOf(name: String): MojangAPI.SkinProfile? {

        profileCacheOf(name).onSuccess { return it } // 캐쉬에서 가져오기

        var profile : MojangAPI.SkinProfile? = null
        val job = SeniorCenterTwerkMachine.plugin.launch(
            SeniorCenterTwerkMachine.plugin.asyncDispatcher
        ) {
            MojangAPI.fetchProfile(name) // uuid 가져오기
                ?.uuid()
                ?.let(MojangAPI::fetchSkinProfile) // MojangAPI 로 스킨 가져오기
                .also { profile = it }
            cache[name] = profile
            profileImporter -= name
        }
        profileImporter[name] = job // 캐쉬로 다른 코드에서 가져올때 멈추는 용도
        job.join()
        delayRemoveCache(name)
        return profile

    }

    private suspend fun profileCacheOf(name: String) : Result<MojangAPI.SkinProfile?> {
        if (name !in cache) {
            profileImporter[name]?.join() ?: return Result.failure(IllegalStateException("Not found importer"))
        }
        delayRemoveCache(name)
        return Result.success(cache[name])
    }

    // 캐쉬 제거기 / 캐쉬 제거 딜레이
    private suspend fun delayRemoveCache(name: String) {
        SeniorCenterTwerkMachine.plugin.launch {
            delay(TwerkingConfig.cacheRemovingDelay)
            cache -= name
            cacheRemover -= name
        }.let {
            cacheRemover[name]?.cancel()
            cacheRemover[name] = it
        }
    }
}