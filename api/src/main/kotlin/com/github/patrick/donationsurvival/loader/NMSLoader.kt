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

package com.github.patrick.donationsurvival.loader

import org.apache.commons.lang.reflect.ConstructorUtils
import org.bukkit.Bukkit
import java.lang.reflect.InvocationTargetException
import java.util.ArrayList

/**
 * The following codes are originated from
 * [noonmaru's Tap Library](https://github.com/noonmaru/tap/blob/master/api/src/main/java/com/github/noonmaru/tap/loader/LibraryLoader.java),
 * which follows GNU General Public License v3.0.
 */

@Suppress("UNCHECKED_CAST")
fun <T> load(type: Class<T>): T {
    val packageName = type.`package`.name
    val className = "NMS${type.simpleName}"
    val candidates = ArrayList<String>(2)
    candidates.add("$packageName.$bukkitVersion.$className")
    val lastDot = packageName.lastIndexOf('.')
    if (lastDot > 0) {
        val superPackageName = packageName.substring(0, lastDot)
        val subPackageName = packageName.substring(lastDot + 1)
        candidates.add("$superPackageName.$bukkitVersion.$subPackageName.$className")
    }

    return try {
        val nmsClass = candidates.mapNotNull { candidate ->
            try {
                Class.forName(candidate, true, type.classLoader).asSubclass(type)
            } catch (exception: ClassNotFoundException) {
                null
            }
        }.firstOrNull() ?: throw ClassNotFoundException("Not found nms library class: $candidates")
        val constructor = ConstructorUtils.getMatchingAccessibleConstructor(nmsClass, emptyArray())
                ?: throw UnsupportedOperationException("${type.name} does not have Constructor for []")
        constructor.newInstance() as T
    } catch (exception: ClassNotFoundException) {
        throw UnsupportedOperationException("${type.name} does not support this version: $minecraftVersion", exception)
    } catch (exception: IllegalAccessException) {
        throw UnsupportedOperationException("${type.name} constructor is not visible")
    } catch (exception: InstantiationException) {
        throw UnsupportedOperationException("${type.name} is abstract class")
    } catch (exception: InvocationTargetException) {
        throw UnsupportedOperationException("${type.name} has an error occurred while creating the instance", exception)
    }
}

private val bukkitVersion by lazy {
    with("v\\d+_\\d+_R\\d+".toPattern().matcher(Bukkit.getServer()::class.java.`package`.name)) {
        when {
            find() -> group()
            else -> throw NoSuchElementException("No such bukkit version exists")
        }
    }
}

private val minecraftVersion by lazy {
    with("(?<=\\(MC: )[\\d.]+?(?=\\))".toPattern().matcher(Bukkit.getVersion())) {
        when {
            find() -> group()
            else -> throw NoSuchElementException("No such minecraft version exists")
        }
    }
}