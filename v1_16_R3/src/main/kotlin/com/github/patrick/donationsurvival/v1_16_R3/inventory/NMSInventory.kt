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

/*
 * Copyright (c) 2020 Noonmaru
 *
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.patrick.donationsurvival.v1_16_R3.inventory

import com.github.patrick.donationsurvival.inventory.Inventory
import com.google.common.collect.ImmutableList
import net.minecraft.server.v1_16_R3.ItemStack
import net.minecraft.server.v1_16_R3.Items
import net.minecraft.server.v1_16_R3.NonNullList
import net.minecraft.server.v1_16_R3.PlayerInventory
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack as BukkitItemStack

/**
 * The following codes are originated from
 * [noonmaru's InvCaptive](https://github.com/noonmaru/inv-captive/blob/master/src/main/kotlin/com/github/noonmaru/invcaptive/plugin/InvCaptive.kt),
 * which follows GNU General Public License v3.0.
 */
@Suppress("unused")
class NMSInventory : Inventory {
    private val items: NonNullList<ItemStack>

    private val armor: NonNullList<ItemStack>

    private val extraSlots: NonNullList<ItemStack>

    private val contents: List<NonNullList<ItemStack>>

    init {
        val inv = PlayerInventory(null)

        this.items = inv.items
        this.armor = inv.armor
        this.extraSlots = inv.extraSlots
        this.contents = ImmutableList.of(items, armor, extraSlots)
    }

    override fun save(): YamlConfiguration {
        val yaml = YamlConfiguration()

        yaml.setItemStackList(ITEMS, items)
        yaml.setItemStackList(ARMOR, armor)
        yaml.setItemStackList(EXTRA_SLOTS, extraSlots)

        return yaml
    }

    override fun load(yaml: YamlConfiguration) {
        yaml.loadItemStackList(ITEMS, items)
        yaml.loadItemStackList(ARMOR, armor)
        yaml.loadItemStackList(EXTRA_SLOTS, extraSlots)
    }

    override fun join(player: Player) {
        val entityPlayer = (player as CraftPlayer).handle
        val playerInv = entityPlayer.inventory

        playerInv.setField("items", items)
        playerInv.setField("armor", armor)
        playerInv.setField("extraSlots", extraSlots)
        playerInv.setField("f", contents)
    }

    override fun add(itemStack: BukkitItemStack) {
        val item = CraftItemStack.asNMSCopy(itemStack)

        items.forEachIndexed { index, curr ->
            if (curr.item == Items.AIR) {
                items[index] = item
                return
            }
        }

        extraSlots.forEachIndexed { index, curr ->
            if (curr.item == Items.AIR) {
                items[index] = item
                return
            }
        }
    }

    override fun shuffle() {
        val shuffled = (items + armor + extraSlots).shuffled().toMutableList()

        items.replaceAll {
            shuffled.removeFirst()
        }
        armor.replaceAll {
            shuffled.removeFirst()
        }
        extraSlots.replaceAll {
            shuffled.removeFirst()
        }
    }

    override fun deleteOne(): BukkitItemStack? {
        val size = items.count() + armor.count() + extraSlots.count()
        val shuffled = (0 until size).shuffled().toMutableList()

        repeat(size) {
            val slot = shuffled.removeFirst()

            when {
                slot < items.count() -> {
                    val itemStack = items[slot]

                    if (itemStack.item != Items.AIR) {
                        return items.set(slot, ItemStack.b)?.run {
                            CraftItemStack.asBukkitCopy(this)
                        }
                    }
                }
                slot < items.count() + armor.count() -> {
                    val itemStack = armor[slot - items.count()]

                    if (itemStack.item != Items.AIR) {
                        return armor.set(slot - items.count(), ItemStack.b)?.run {
                            CraftItemStack.asBukkitCopy(this)
                        }
                    }
                }
                else -> {
                    val itemStack = extraSlots[slot - items.count() - armor.count()]

                    if (itemStack.item != Items.AIR) {
                        return extraSlots.set(slot - items.count() - armor.count(), ItemStack.b)?.run {
                            CraftItemStack.asBukkitCopy(this)
                        }
                    }
                }
            }
        }
        return null
    }

    override fun clear() {
        items.replaceAll {
            ItemStack.b
        }
        armor.replaceAll {
            ItemStack.b
        }
        extraSlots.replaceAll {
            ItemStack.b
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun ConfigurationSection.loadItemStackList(name: String, list: NonNullList<ItemStack>) {
        val items = getMapList(name).map { args ->
            CraftItemStack.asNMSCopy(CraftItemStack.deserialize(args as Map<String, Any>))
        }

        for (i in 0 until minOf(list.count(), items.count())) {
            list[i] = items[i]
        }
    }

    private fun ConfigurationSection.setItemStackList(name: String, list: NonNullList<ItemStack>) {
        set(name, list.map { itemStack ->
            CraftItemStack.asCraftMirror(itemStack).serialize()
        })
    }

    private fun Any.setField(name: String, value: Any) {
        val field = javaClass.getDeclaredField(name).apply {
            isAccessible = true
        }

        field.set(this, value)
    }

    private companion object {
        private const val ITEMS = "items"
        private const val ARMOR = "armor"
        private const val EXTRA_SLOTS = "extraSlots"
    }
}