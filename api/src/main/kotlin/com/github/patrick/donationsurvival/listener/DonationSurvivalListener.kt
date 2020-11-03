package com.github.patrick.donationsurvival.listener

import com.github.patrick.donationsurvival.INSTANCE
import com.github.patrick.donationsurvival.INVENTORY
import com.github.patrick.donationsurvival.ITEMS
import com.github.patrick.donationsurvival.LOCKED_ITEMS
import com.github.patrick.donationsurvival.LOCK_PLAYER
import com.github.patrick.donationsurvival.plugin.DonationSurvivalPlugin
import com.github.patrick.twipe.event.AsyncTwipDonateEvent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.EntityType
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockCookEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.world.WorldSaveEvent
import org.bukkit.inventory.ItemStack

class DonationSurvivalListener : Listener {
    @EventHandler
    fun onTwipDonate(event: AsyncTwipDonateEvent) {
        val donator = event.nickname
        val amount = event.amount
        val playerName = event.streamer
        val message = event.comment

        val template = "${ChatColor.GOLD}${ChatColor.BOLD}${event.nickname}${ChatColor.RESET}님이 " +
                "${ChatColor.GRAY}${event.streamer}${ChatColor.RESET}님에게 " +
                "%s(을)를 선물했습니다! [${ChatColor.GOLD}${event.amount}${ChatColor.RESET}]"

        Bukkit.broadcastMessage(" ")

        when {
            amount >= 100_000 -> {
                INVENTORY.clear()

                Bukkit.getOnlinePlayers().forEach { player ->
                    player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.MASTER, 100F, 1F)
                }

                Bukkit.broadcastMessage(template.format("${ChatColor.RED}인벤토리 클리어${ChatColor.RESET}"))
                Bukkit.broadcastMessage(" \"$message\"")
            }
            amount >= 10_000 -> {
                val random = LOCKED_ITEMS.keys.randomOrNull()
                LOCKED_ITEMS.remove(random)

                Bukkit.broadcastMessage(template.format("${ChatColor.AQUA}레시피 복구${ChatColor.RESET}"))
                Bukkit.broadcastMessage(" \"$message\"")

                Bukkit.getOnlinePlayers().forEach { player ->
                    player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 100F, 1F)
                }

                if (random != null) {
                    ITEMS.add(random)

                    Bukkit.broadcastMessage("${ChatColor.AQUA}${ChatColor.BOLD}${
                        random.name.toLowerCase().split("_").joinToString(" ") { string ->
                            string.capitalize()
                        }
                    }${ChatColor.RESET} ${ChatColor.AQUA}레시피가 복구되었습니다.")
                } else {
                    Bukkit.broadcastMessage("${ChatColor.AQUA}복구할 레시피가 없습니다.")
                }
            }
            amount >= 5_000 -> {
                val random = ITEMS.randomOrNull()
                ITEMS.remove(random)

                Bukkit.getOnlinePlayers().forEach { player ->
                    player.playSound(player.location, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, SoundCategory.MASTER, 100F, 1F)
                }

                Bukkit.broadcastMessage(template.format("${ChatColor.LIGHT_PURPLE}레시피 삭제${ChatColor.RESET}"))
                Bukkit.broadcastMessage(" \"$message\"")

                if (random != null) {
                    LOCKED_ITEMS[random] = donator

                    Bukkit.broadcastMessage("${ChatColor.RED}${ChatColor.BOLD}${
                        random.name.toLowerCase().split("_").joinToString(" ") { string ->
                            string.capitalize()
                        }
                    }${ChatColor.RESET} ${ChatColor.RED}레시피가 삭제되었습니다.")
                } else {
                    Bukkit.broadcastMessage("${ChatColor.RED}삭제할 레시피가 없습니다.")
                }
            }
            amount >= 4_000 -> {
                val location = requireNotNull(Bukkit.getPlayer(playerName)).location
                Bukkit.getScheduler().runTask(INSTANCE, Runnable {
                    requireNotNull(location.world).spawnEntity(location, EntityType.PRIMED_TNT)
                })

                Bukkit.broadcastMessage(template.format("${ChatColor.DARK_RED}폭탄${ChatColor.RESET}"))
                Bukkit.broadcastMessage(" \"$message\"")
            }
            amount >= 3_000 -> {
                val result = INVENTORY.deleteOne()

                Bukkit.broadcastMessage(template.format("${ChatColor.DARK_GREEN}아이템 삭제${ChatColor.RESET}"))
                Bukkit.broadcastMessage(" \"$message\"")

                if (result != null) {
                    Bukkit.broadcastMessage("${
                        result.type.name.toLowerCase().split("_").joinToString(" ") { string ->
                            string.capitalize()
                        }
                    } x ${result.amount} 아이템이 인벤토리에서 제거되었습니다.")
                }
            }
            amount >= 2_000 -> {
                INVENTORY.shuffle()

                Bukkit.broadcastMessage(template.format("${ChatColor.GREEN}아이템 섞기${ChatColor.RESET}"))
                Bukkit.broadcastMessage(" \"$message\"")
            }
            amount >= 1_000 -> {
                INVENTORY.add(ItemStack(Material.ANVIL).apply {
                    itemMeta = requireNotNull(itemMeta).apply {
                        setDisplayName("${donator}의 깡")
                    }
                })

                Bukkit.getOnlinePlayers().forEach { player ->
                    player.playSound(player.location, Sound.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 100F, 1F)
                }

                Bukkit.broadcastMessage(template.format("${ChatColor.BLUE}깡${ChatColor.RESET}"))
                Bukkit.broadcastMessage(" \"$message\"")
            }
            else -> return
        }

    }

    @EventHandler
    fun onCraftItem(event: CraftItemEvent) {
        val type = event.recipe.result.type

        if (type in LOCKED_ITEMS) {
            event.isCancelled = true
            event.result = Event.Result.DENY

            Bukkit.broadcastMessage("${ChatColor.GOLD}${LOCKED_ITEMS[type]}${ChatColor.RESET}님이 삭제한 레시피입니다.")
        }
    }

    @EventHandler
    fun onBlockCook(event: BlockCookEvent) {
        val type = event.result.type

        if (type in LOCKED_ITEMS) {
            event.isCancelled = true
            val location = event.block.location.apply {
                add(0.5, 1.5, 0.5)
            }
            Bukkit.getScheduler().runTask(INSTANCE, Runnable {
                requireNotNull(location.world).spawnEntity(location, EntityType.PRIMED_TNT)
            })

            Bukkit.broadcastMessage("${ChatColor.GOLD}${LOCKED_ITEMS[type]}${ChatColor.RESET}님이 삭제한 레시피입니다.")
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        INVENTORY.join(event.player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        DonationSurvivalPlugin.save()
        if (event.player == LOCK_PLAYER) {
            LOCK_PLAYER = null
        }
    }

    @EventHandler
    @Suppress("UNUSED_PARAMETER")
    fun onWorldSave(event: WorldSaveEvent) {
        DonationSurvivalPlugin.save()
    }

    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (event.inventory.type in setOf(
                        InventoryType.CHEST, InventoryType.ENDER_CHEST, InventoryType.SHULKER_BOX, InventoryType.BARREL,
                        InventoryType.DISPENSER, InventoryType.DROPPER, InventoryType.HOPPER
                )) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        event.itemDrop.pickupDelay = Short.MAX_VALUE.toInt()
    }

    @EventHandler
    fun onInventoryMove(event: InventoryMoveItemEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onInventoryPickupItem(event: InventoryPickupItemEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerArmorStandManipulate(event: PlayerArmorStandManipulateEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onEntityPickupItem(event: EntityPickupItemEvent) {
        if (event.entityType != EntityType.PLAYER) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerSwapHandItems(event: PlayerSwapHandItemsEvent) {
        if (LOCK_PLAYER != null) {
            if (event.player != LOCK_PLAYER) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerItemHeld(event: PlayerItemHeldEvent) {
        if (LOCK_PLAYER != null) {
            if (event.player != LOCK_PLAYER) {
                event.isCancelled = true
            } else {
                Bukkit.getOnlinePlayers().forEach { player ->
                    player.inventory.heldItemSlot = event.newSlot
                }
            }
        }
    }
}