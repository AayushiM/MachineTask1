package com.machinetask.utils

import java.util.*

/**
 * Class used to store XML hierarchy
 *
 */
class Tag /* package */ internal constructor(/* package */val path: String, /* package */
                                                          val name: String
) {
    /* package */ val children =
        ArrayList<Tag>()
    private var mContent: String? = null

    /* package */
    fun addChild(tag: Tag) {
        children.add(tag)
    }

    /* package */// checks that there is a relevant content (not only spaces or \n)/* package */
    var content: String?
        get() = mContent
        set(content) {
            // checks that there is a relevant content (not only spaces or \n)
            var hasContent = false
            if (content != null) {
                for (i in 0 until content.length) {
                    val c = content[i]
                    if (c != ' ' && c != '\n') {
                        hasContent = true
                        break
                    }
                }
            }
            if (hasContent) {
                mContent = content
            }
        }

    /* package */
    fun hasChildren(): Boolean {
        return children.size > 0
    }

    /* package */
    val childrenCount: Int
        get() = children.size

    /* package */
    fun getChild(index: Int): Tag? {
        return if (index >= 0 && index < children.size) {
            children[index]
        } else null
    }

    /* package */
    val groupedElements: HashMap<String, ArrayList<Tag>>
        get() {
            val groups =
                HashMap<String, ArrayList<Tag>>()
            for (child in children) {
                val key = child.name
                var group = groups[key]
                if (group == null) {
                    group = ArrayList()
                    groups[key] = group
                }
                group.add(child)
            }
            return groups
        }

    override fun toString(): String {
        return "Tag: " + name + ", " + children.size + " children, Content: " + mContent
    }

}