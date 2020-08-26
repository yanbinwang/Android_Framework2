package com.example.common.http.adapter

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import retrofit2.Converter
import java.io.OutputStreamWriter
import java.io.Writer
import java.nio.charset.StandardCharsets

/**
 * Created by WangYanBin on 2020/8/26.
 */
class CustomGsonRequestBodyConverter<T> : Converter<T, RequestBody> {
    private var gson: Gson? = null
    private var adapter: TypeAdapter<T>? = null
    private val UTF_8 = StandardCharsets.UTF_8
    private val MEDIA_TYPE = "application/json; charset=UTF-8".toMediaTypeOrNull()

    constructor(gson: Gson, adapter: TypeAdapter<T>) {
        this.gson = gson
        this.adapter = adapter
    }

    override fun convert(value: T): RequestBody? {
        val buffer = Buffer()
        val writer: Writer = OutputStreamWriter(buffer.outputStream(), UTF_8)
        val jsonWriter = gson?.newJsonWriter(writer)
        adapter?.write(jsonWriter, value)
        jsonWriter?.close()
        return buffer.readByteString().toRequestBody(MEDIA_TYPE)
    }

}