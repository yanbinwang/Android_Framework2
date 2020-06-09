package com.example.common.utils.analysis

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

import java.io.IOException

class BooleanTypeAdapter : TypeAdapter<Boolean>() { //接管【Stringa】类型的序列化和反序列化过程

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Boolean?) {
        out.value(value.toString())
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): Boolean? {
        return try {
            val value = `in`.nextString()
            "Y" == value || "1" == value || "true" == value
        } catch (e: NullPointerException) {
            false
        }
    }

}
