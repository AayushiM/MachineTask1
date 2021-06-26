package com.machinetask.utils

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream
import java.io.StringReader
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Converts XML to JSON
 */
class XmlToJson private constructor(builder: Builder) {
    private var mIndentationPattern = DEFAULT_INDENTATION

    /**
     * Builder class to create a XmlToJson object
     */
    class Builder {
        var mStringSource: StringReader? = null
        var mInputStreamSource: InputStream? = null
        var mInputEncoding = DEFAULT_ENCODING
        val mForceListPaths =
            HashSet<String>()
        val mForceListPatterns =
            HashSet<Pattern>()
        val mAttributeNameReplacements =
            HashMap<String, String>()
        val mContentNameReplacements =
            HashMap<String, String>()
        val mForceClassForPath =
            HashMap<String, Class<*>>() // Integer, Long, Double, Boolean
        val mSkippedAttributes =
            HashSet<String>()
        val mSkippedTags =
            HashSet<String>()

        /**
         * Constructor
         *
         * @param xmlSource XML source
         */
        constructor(xmlSource: String) {
            mStringSource = StringReader(xmlSource)
        }

        /**
         * Constructor
         *
         * @param inputStreamSource XML source
         * @param inputEncoding     XML encoding format, can be null (uses UTF-8 if null).
         */
        constructor(inputStreamSource: InputStream, inputEncoding: String?) {
            mInputStreamSource = inputStreamSource
            mInputEncoding =
                inputEncoding ?: DEFAULT_ENCODING
        }

        /**
         * Force a XML Tag to be interpreted as a list
         *
         * @param path Path for the tag, with format like "/parentTag/childTag/tagAsAList"
         * @return the Builder
         */
        fun forceList(path: String): Builder {
            mForceListPaths.add(path)
            return this
        }

        /**
         * Force a XML Tag to be interpreted as a list, using a RegEx pattern for the path
         *
         * @param pattern Path for the tag using RegEx, like "*childTag/tagAsAList"
         * @return the Builder
         */
        fun forceListPattern(pattern: String): Builder {
            val pat =
                Pattern.compile(pattern, Pattern.DOTALL)
            mForceListPatterns.add(pat)
            return this
        }

        /**
         * Change the name of an attribute
         *
         * @param attributePath   Path for the attribute, using format like "/parentTag/childTag/childTagAttribute"
         * @param replacementName Name used for replacement (childTagAttribute becomes replacementName)
         * @return the Builder
         */
        fun setAttributeName(
            attributePath: String,
            replacementName: String
        ): Builder {
            mAttributeNameReplacements[attributePath] = replacementName
            return this
        }

        /**
         * Change the name of the key for a XML content
         * In XML there is no extra key name for a tag content. So a default name "content" is used.
         * This "content" name can be replaced with a custom name.
         *
         * @param contentPath     Path for the Tag that holds the content, using format like "/parentTag/childTag"
         * @param replacementName Name used in place of the default "content" key
         * @return the Builder
         */
        fun setContentName(
            contentPath: String,
            replacementName: String
        ): Builder {
            mContentNameReplacements[contentPath] = replacementName
            return this
        }

        /**
         * Force an attribute or content value to be a INTEGER. A default value is used if the content is missing.
         * @param path Path for the Tag content or Attribute, using format like "/parentTag/childTag"
         * @return the Builder
         */
        fun forceIntegerForPath(path: String): Builder {
            mForceClassForPath[path] = Int::class.java
            return this
        }

        /**
         * Force an attribute or content value to be a LONG. A default value is used if the content is missing.
         * @param path Path for the Tag content or Attribute, using format like "/parentTag/childTag"
         * @return the Builder
         */
        fun forceLongForPath(path: String): Builder {
            mForceClassForPath[path] = Long::class.java
            return this
        }

        /**
         * Force an attribute or content value to be a DOUBLE. A default value is used if the content is missing.
         * @param path Path for the Tag content or Attribute, using format like "/parentTag/childTag"
         * @return the Builder
         */
        fun forceDoubleForPath(path: String): Builder {
            mForceClassForPath[path] = Double::class.java
            return this
        }

        /**
         * Force an attribute or content value to be a BOOLEAN. A default value is used if the content is missing.
         * @param path Path for the Tag content or Attribute, using format like "/parentTag/childTag"
         * @return the Builder
         */
        fun forceBooleanForPath(path: String): Builder {
            mForceClassForPath[path] = Boolean::class.java
            return this
        }

        /**
         * Skips a Tag (will not be present in the JSON)
         *
         * @param path Path for the Tag, using format like "/parentTag/childTag"
         * @return the Builder
         */
        fun skipTag(path: String): Builder {
            mSkippedTags.add(path)
            return this
        }

        /**
         * Skips an attribute (will not be present in the JSON)
         *
         * @param path Path for the Attribute, using format like "/parentTag/childTag/ChildTagAttribute"
         * @return the Builder
         */
        fun skipAttribute(path: String): Builder {
            mSkippedAttributes.add(path)
            return this
        }

        /**
         * Creates the XmlToJson object
         *
         * @return a XmlToJson instance
         */
        fun build(): XmlToJson {
            return XmlToJson(this)
        }
    }

    private val mStringSource: StringReader?
    private val mInputStreamSource: InputStream?
    private val mInputEncoding: String
    private val mForceListPaths: HashSet<String>
    private var mForceListPatterns =
        HashSet<Pattern>()
    private val mAttributeNameReplacements: HashMap<String, String>
    private val mContentNameReplacements: HashMap<String, String>
    private val mForceClassForPath: HashMap<String, Class<*>>
    private var mSkippedAttributes =
        HashSet<String>()
    private var mSkippedTags = HashSet<String>()
    private val mJsonObject // Used for caching the result
            : JSONObject?

    /**
     * @return the JSONObject built from the XML
     */
    fun toJson(): JSONObject? {
        return mJsonObject
    }

    private fun convertToJSONObject(): JSONObject? {
        return try {
            val parentTag = Tag("", "xml")
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware =
                false // tags with namespace are taken as-is ("namespace:tagname")
            val xpp = factory.newPullParser()
            setInput(xpp)
            var eventType = xpp.eventType
            while (eventType != XmlPullParser.START_DOCUMENT) {
                eventType = xpp.next()
            }
            readTags(parentTag, xpp)
            unsetInput()
            convertTagToJson(parentTag, false)
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun setInput(xpp: XmlPullParser) {
        if (mStringSource != null) {
            try {
                xpp.setInput(mStringSource)
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
            }
        } else {
            try {
                xpp.setInput(mInputStreamSource, mInputEncoding)
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
            }
        }
    }

    private fun unsetInput() {
        mStringSource?.close()
        // else the InputStream has been given by the user, it is not our role to close it
    }

    private fun readTags(parent: Tag, xpp: XmlPullParser) {
        try {
            var eventType: Int
            do {
                eventType = xpp.next()
                if (eventType == XmlPullParser.START_TAG) {
                    val tagName = xpp.name
                    val path = parent.path + "/" + tagName
                    val skipTag = mSkippedTags.contains(path)
                    val child = Tag(path, tagName)
                    if (!skipTag) {
                        parent.addChild(child)
                    }

                    // Attributes are taken into account as key/values in the child
                    val attrCount = xpp.attributeCount
                    for (i in 0 until attrCount) {
                        var attrName = xpp.getAttributeName(i)
                        val attrValue = xpp.getAttributeValue(i)
                        val attrPath =
                            parent.path + "/" + child.name + "/" + attrName

                        // Skip Attributes
                        if (mSkippedAttributes.contains(attrPath)) {
                            continue
                        }
                        attrName = getAttributeNameReplacement(attrPath, attrName)
                        val attribute =
                            Tag(attrPath, attrName)
                        attribute.content = attrValue
                        child.addChild(attribute)
                    }
                    readTags(child, xpp)
                } else if (eventType == XmlPullParser.TEXT) {
                    val text = xpp.text
                    parent.content = text
                } else if (eventType == XmlPullParser.END_TAG) {
                    return
                } else if (eventType == XmlPullParser.END_DOCUMENT) {
                    return
                } else {
                    Log.i(
                        TAG,
                        "unknown xml eventType $eventType"
                    )
                }
            } while (eventType != XmlPullParser.END_DOCUMENT)
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    private fun convertTagToJson(
        tag: Tag,
        isListElement: Boolean
    ): JSONObject? {
        val json = JSONObject()

        // Content is injected as a key/value
        if (tag.content != null) {
            val path = tag.path
            val name =
                getContentNameReplacement(path, DEFAULT_CONTENT_NAME)
            putContent(path, json, name, tag.content)
        }
        try {
            val groups =
                tag.groupedElements // groups by tag names so that we can detect lists or single elements
            for (group in groups.values) {
                if (group.size == 1) {    // element, or list of 1
                    val child = group[0]
                    if (isForcedList(child)) {  // list of 1
                        val list = JSONArray()
                        list.put(convertTagToJson(child, true))
                        val childrenNames = child.name
                        json.put(childrenNames, list)
                    } else {    // stand alone element
                        if (child.hasChildren()) {
                            val jsonChild = convertTagToJson(child, false)
                            json.put(child.name, jsonChild)
                        } else {
                            val path = child.path
                            putContent(path, json, child.name, child.content)
                        }
                    }
                } else {    // list
                    val list = JSONArray()
                    for (child in group) {
                        list.put(convertTagToJson(child, true))
                    }
                    val childrenNames = group[0].name
                    json.put(childrenNames, list)
                }
            }
            return json
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    private fun putContent(
        path: String,
        json: JSONObject,
        tag: String,
        content: String?
    ) {
        var content = content
        try {
            // checks if the user wants to force a class (Int, Double... for a given path)
            val forcedClass = mForceClassForPath[path]
            if (forcedClass == null) {  // default behaviour, put it as a String
                if (content == null) {
                    content = DEFAULT_EMPTY_STRING
                }
                json.put(tag, content)
            } else {
                if (forcedClass == Int::class.java) {
                    try {
                        val number = content!!.toInt()
                        json.put(tag, number)
                    } catch (exception: NumberFormatException) {
                        json.put(tag, DEFAULT_EMPTY_INTEGER)
                    }
                } else if (forcedClass == Long::class.java) {
                    try {
                        val number = content!!.toLong()
                        json.put(tag, number)
                    } catch (exception: NumberFormatException) {
                        json.put(tag, DEFAULT_EMPTY_LONG)
                    }
                } else if (forcedClass == Double::class.java) {
                    try {
                        val number = content!!.toDouble()
                        json.put(tag, number)
                    } catch (exception: NumberFormatException) {
                        json.put(tag, DEFAULT_EMPTY_DOUBLE)
                    }
                } else if (forcedClass == Boolean::class.java) {
                    if (content == null) {
                        json.put(tag, DEFAULT_EMPTY_BOOLEAN)
                    } else if (content.equals("true", ignoreCase = true)) {
                        json.put(tag, true)
                    } else if (content.equals("false", ignoreCase = true)) {
                        json.put(tag, false)
                    } else {
                        json.put(tag, DEFAULT_EMPTY_BOOLEAN)
                    }
                }
            }
        } catch (exception: JSONException) {
            // keep continue in case of error
        }
    }

    private fun isForcedList(tag: Tag): Boolean {
        val path = tag.path
        if (mForceListPaths.contains(path)) {
            return true
        }
        for (pattern in mForceListPatterns) {
            val matcher = pattern.matcher(path)
            if (matcher.find()) {
                return true
            }
        }
        return false
    }

    private fun getAttributeNameReplacement(
        path: String,
        defaultValue: String
    ): String {
        val result = mAttributeNameReplacements[path]
        return result ?: defaultValue
    }

    private fun getContentNameReplacement(
        path: String,
        defaultValue: String
    ): String {
        val result = mContentNameReplacements[path]
        return result ?: defaultValue
    }

    override fun toString(): String {
        return mJsonObject?.toString()!!
    }

    /**
     * Format the Json with indentation and line breaks
     *
     * @param indentationPattern indentation to use, for example " " or "\t".
     * if null, use the default 3 spaces indentation
     * @return the formatted Json
     */
    fun toFormattedString(indentationPattern: String?): String? {
        mIndentationPattern = indentationPattern ?: DEFAULT_INDENTATION
        return toFormattedString()
    }

    /**
     * Format the Json with indentation and line breaks.
     * Uses the last intendation pattern used, or the default one (3 spaces)
     *
     * @return the Builder
     */
    fun toFormattedString(): String? {
        if (mJsonObject != null) {
            val indent = ""
            val builder = StringBuilder()
            builder.append("{\n")
            format(mJsonObject, builder, indent)
            builder.append("}\n")
            return builder.toString()
        }
        return null
    }

    private fun format(
        jsonObject: JSONObject,
        builder: StringBuilder,
        indent: String
    ) {
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            builder.append(indent)
            builder.append(mIndentationPattern)
            builder.append("\"")
            builder.append(key)
            builder.append("\": ")
            val value = jsonObject.opt(key)
            if (value is JSONObject) {
                builder.append(indent)
                builder.append("{\n")
                format(value, builder, indent + mIndentationPattern)
                builder.append(indent)
                builder.append(mIndentationPattern)
                builder.append("}")
            } else if (value is JSONArray) {
                formatArray(value, builder, indent + mIndentationPattern)
            } else {
                formatValue(value, builder)
            }
            if (keys.hasNext()) {
                builder.append(",\n")
            } else {
                builder.append("\n")
            }
        }
    }

    private fun formatArray(
        array: JSONArray,
        builder: StringBuilder,
        indent: String
    ) {
        builder.append("[\n")
        for (i in 0 until array.length()) {
            val element = array.opt(i)
            if (element is JSONObject) {
                builder.append(indent)
                builder.append(mIndentationPattern)
                builder.append("{\n")
                format(element, builder, indent + mIndentationPattern)
                builder.append(indent)
                builder.append(mIndentationPattern)
                builder.append("}")
            } else if (element is JSONArray) {
                formatArray(element, builder, indent + mIndentationPattern)
            } else {
                formatValue(element, builder)
            }
            if (i < array.length() - 1) {
                builder.append(",")
            }
            builder.append("\n")
        }
        builder.append(indent)
        builder.append("]")
    }

    private fun formatValue(value: Any, builder: StringBuilder) {
        if (value is String) {
            var string = value

            // Escape special characters
            string = string.replace("\\\\".toRegex(), "\\\\\\\\") // escape backslash
            string = string.replace(
                "\"".toRegex(),
                Matcher.quoteReplacement("\\\"")
            ) // escape double quotes
            string = string.replace("/".toRegex(), "\\\\/") // escape slash
            string = string.replace("\n".toRegex(), "\\\\n")
                .replace("\t".toRegex(), "\\\\t") // escape \n and \t
            string = string.replace("\r".toRegex(), "\\\\r") // escape \r
            builder.append("\"")
            builder.append(string)
            builder.append("\"")
        } else if (value is Long) {
            builder.append(value)
        } else if (value is Int) {
            builder.append(value)
        } else if (value is Boolean) {
            builder.append(value)
        } else if (value is Double) {
            builder.append(value)
        } else {
            builder.append(value.toString())
        }
    }

    companion object {
        private const val TAG = "XmlToJson"
        private const val DEFAULT_CONTENT_NAME = "content"
        private const val DEFAULT_ENCODING = "utf-8"
        private const val DEFAULT_INDENTATION = "   "

        // default values when a Tag is empty
        private const val DEFAULT_EMPTY_STRING = ""
        private const val DEFAULT_EMPTY_INTEGER = 0
        private const val DEFAULT_EMPTY_LONG: Long = 0
        private const val DEFAULT_EMPTY_DOUBLE = 0.0
        private const val DEFAULT_EMPTY_BOOLEAN = false
    }

    init {
        mStringSource = builder.mStringSource
        mInputStreamSource = builder.mInputStreamSource
        mInputEncoding = builder.mInputEncoding
        mForceListPaths = builder.mForceListPaths
        mForceListPatterns = builder.mForceListPatterns
        mAttributeNameReplacements = builder.mAttributeNameReplacements
        mContentNameReplacements = builder.mContentNameReplacements
        mForceClassForPath = builder.mForceClassForPath
        mSkippedAttributes = builder.mSkippedAttributes
        mSkippedTags = builder.mSkippedTags
        mJsonObject =
            convertToJSONObject() // Build now so that the InputStream can be closed just after
    }
}