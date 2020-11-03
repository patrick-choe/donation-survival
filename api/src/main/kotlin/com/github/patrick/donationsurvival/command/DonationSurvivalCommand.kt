package com.github.patrick.donationsurvival.command

import com.github.patrick.donationsurvival.LOCK_PLAYER
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class DonationSurvivalLock : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        when (args.count()) {
            0 -> sender.sendMessage("Requires player name")
            1 -> {
                val candidate = Bukkit.getPlayerExact(args[0])
                if (candidate != null) {
                    LOCK_PLAYER = candidate
                    Bukkit.getOnlinePlayers().forEach { player ->
                        player.inventory.heldItemSlot = candidate.inventory.heldItemSlot
                    }
                } else {
                    sender.sendMessage("Player not found: ${args[0]}")
                }
            }
            else -> sender.sendMessage("Too many args")
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        return if (args.count() == 1) {
            Bukkit.getOnlinePlayers().map(Player::getName).filter {
                it.toLowerCase().startsWith(args[0].toLowerCase())
            }
        } else {
            emptyList()
        }
    }
}

class DonationSurvivalUnlock : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        LOCK_PLAYER = null
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        return emptyList()
    }
}