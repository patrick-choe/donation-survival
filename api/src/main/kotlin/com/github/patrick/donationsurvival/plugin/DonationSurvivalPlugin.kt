/*
 * Copyright (C) 2020 PatrickKR
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact me on <mailpatrickkr@gmail.com>
 */

package com.github.patrick.donationsurvival.plugin

import com.github.patrick.donationsurvival.INSTANCE
import com.github.patrick.donationsurvival.INVENTORY
import com.github.patrick.donationsurvival.ITEMS
import com.github.patrick.donationsurvival.LOCKED_ITEMS
import com.github.patrick.donationsurvival.command.DonationSurvivalLock
import com.github.patrick.donationsurvival.command.DonationSurvivalUnlock
import com.github.patrick.donationsurvival.listener.DonationSurvivalListener
import com.google.common.collect.ImmutableList
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.EnumMap

class DonationSurvivalPlugin : JavaPlugin() {
    override fun onLoad() {
        INSTANCE = this

        dataFolder.mkdir()

        INVENTORY_FILE = File(dataFolder, "inventory.yml").apply {
            createNewFile()
        }

        ITEMS_FILE = File(dataFolder, "items.json").apply {
            createNewFile()
        }

        val json = ITEMS_FILE.readText()
        LOCKED_ITEMS = if (json.count() > 0) {
            Json.decodeFromString<Map<String, String>>(json).mapKeys { entry ->
                requireNotNull(Material.getMaterial(entry.key))
            }.toMutableMap()
        } else {
            EnumMap(org.bukkit.Material::class.java)
        }

        ITEMS = ArrayList(ImmutableList.copyOf(Bukkit.recipeIterator()).map { recipe ->
            recipe.result.type
        }.distinct().filterNot { material ->
            material.name.startsWith("LEGACY_")
        }).apply {
            removeAll(LOCKED_ITEMS.keys)
        }

        val yaml = YamlConfiguration.loadConfiguration(INVENTORY_FILE)
        INVENTORY.load(yaml)

        Bukkit.getOnlinePlayers().forEach { player ->
            INVENTORY.join(player)
        }
    }

    @Suppress("UsePropertyAccessSyntax")
    override fun onEnable() {
        server.pluginManager.registerEvents(DonationSurvivalListener(), this)
        requireNotNull(getCommand("lock")).run {
            setExecutor(DonationSurvivalLock())
            setTabCompleter(DonationSurvivalLock())
        }
        requireNotNull(getCommand("unlock")).run {
            setExecutor(DonationSurvivalUnlock())
            setTabCompleter(DonationSurvivalUnlock())
        }
    }

    override fun onDisable() {
        save()
    }

    internal companion object {
        private lateinit var INVENTORY_FILE: File

        private lateinit var ITEMS_FILE: File

        internal fun save() {
            val yaml = INVENTORY.save()
            yaml.save(INVENTORY_FILE)

            ITEMS_FILE.writeText(Json.encodeToString(LOCKED_ITEMS.mapKeys { entry ->
                entry.key.name
            }))
        }
    }
}