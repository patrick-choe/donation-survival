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

package com.github.patrick.donationsurvival.inventory

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * The following codes are motivated from
 * [noonmaru's InvCaptive](https://github.com/noonmaru/inv-captive/blob/master/src/main/kotlin/com/github/noonmaru/invcaptive/plugin/InvCaptive.kt),
 * which follows GNU General Public License v3.0.
 */

interface Inventory {
    fun save(): YamlConfiguration

    fun load(yaml: YamlConfiguration)

    fun join(player: Player)

    fun add(itemStack: ItemStack)

    fun shuffle()

    fun deleteOne(): ItemStack?

    fun clear()
}