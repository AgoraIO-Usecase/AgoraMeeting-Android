package io.agora.meeting.common.model

import java.util.*

object Examples {

    const val GROUP_FRAGMENT = "Fragment(大布局)"
    const val GROUP_UI_CONTROLLER = "UiController(小组件)"

    val ITEM_MAP: MutableMap<String, MutableList<ExampleBean>> = HashMap()

    fun addItem(item: ExampleBean) {
        val group: String = item.group
        var list: MutableList<ExampleBean>? = ITEM_MAP[group]
        if (list == null) {
            list = ArrayList()
            ITEM_MAP[group] = list
        }
        list.add(item)
    }

    fun sortItem() {
        for ((key) in ITEM_MAP) {
            ITEM_MAP[key]?.let {
                Collections.sort(it, Comparator { o1, o2 -> o1.index - o2.index })
            }
        }
    }

}