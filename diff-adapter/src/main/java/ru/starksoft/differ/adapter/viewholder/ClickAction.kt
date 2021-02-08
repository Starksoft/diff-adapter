package ru.starksoft.differ.adapter.viewholder

import ru.starksoft.differ.utils.hash.Cache

object ClickAction {

    private val CACHE by lazy { Cache() }

    fun getActionId(clazz: Class<*>, name: String): Int {
        return CACHE.getValue("${clazz.name}:$name")
    }
}
