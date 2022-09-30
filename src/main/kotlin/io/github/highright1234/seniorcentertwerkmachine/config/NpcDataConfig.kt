package io.github.highright1234.seniorcentertwerkmachine.config

import io.github.highright1234.seniorcentertwerkmachine.TwerkingMachine
import io.github.monun.tap.config.Config
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object NpcDataConfig {
    /**
     * Tap Config 기능 쓸려고 했는데
     * 개같지만 ArrayList 안에 타입을 못찾아서
     * 그냥 직접 적은
     */
    private lateinit var file: File
    fun load(file: File) {
        this.file = file
        YamlConfiguration.loadConfiguration(file)
//        val config = YamlConfiguration.loadConfiguration(file)
//        println(config.getKeys(false).size)
//        config.getKeys(false).forEach {
//            npcData += config[it] as TwerkingMachine
//        }
        // 자동으로 로드되면서 추가됨
    }

    fun save() {
        var key = 0
        val config = YamlConfiguration()
        npcData.forEach {
            config.set("$key", it)
            key++
        }
        config.save(file)
    }

    @Config
    val npcData = arrayListOf<TwerkingMachine>()

}