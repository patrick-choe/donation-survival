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

package com.github.patrick.donationsurvival

import com.github.patrick.donationsurvival.inventory.Inventory
import com.github.patrick.donationsurvival.loader.load
import com.github.patrick.donationsurvival.plugin.DonationSurvivalPlugin
import org.bukkit.Material
import org.bukkit.entity.Player

internal lateinit var INSTANCE: DonationSurvivalPlugin

internal val INVENTORY = load(Inventory::class.java)

internal lateinit var ITEMS: MutableList<Material>

internal lateinit var LOCKED_ITEMS: MutableMap<Material, String>

internal var LOCK_PLAYER: Player? = null