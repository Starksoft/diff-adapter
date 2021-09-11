package ru.starksoft.differ.adapter.viewholder

import ru.starksoft.differ.utils.hash.Cache

object ClickAction {

    private val cache by lazy { Cache() }

    fun getActionId(clazz: Class<*>, name: String): Int {
        return cache["${clazz.name}:$name"]
    }

    operator fun get(clazz: Class<*>, name: String): Int {
        return cache["${clazz.name}:$name"]
    }
}
