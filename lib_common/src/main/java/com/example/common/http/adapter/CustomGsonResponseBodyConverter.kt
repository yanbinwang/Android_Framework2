package com.example.common.http.adapter

import com.example.common.http.repository.ApiResponse
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import okhttp3.ResponseBody
import retrofit2.Converter
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import kotlin.text.Charsets.UTF_8

/**
 * Created by WangYanBin on 2020/8/26.
 */
class CustomGsonResponseBodyConverter<T> : Converter<ResponseBody, T> {
    private var gson: Gson? = null
    private var adapter: TypeAdapter<T>? = null

    constructor(gson: Gson, adapter: TypeAdapter<T>) {
        this.gson = gson
        this.adapter = adapter
    }

    override fun convert(value: ResponseBody): T? {
//        String response = value.string();
//        try {
//            gson.fromJson(response, ApiResponse.class);
//        } catch (Exception e) {
//            response = gson.toJson(new ApiResponse(-1, response, null));
//        }

//        HttpStatus httpStatus = gson.fromJson(response, HttpStatus.class);
//        if (httpStatus.isCodeInvalid()) {
//            value.close();
//            throw new ApiException(httpStatus.getCode(), httpStatus.getMessage());
//        }

        //得到整体的消息体
        var response = value.string()
        //尝试转换，格式转换异常则包装一个对象返回
        try {
            gson?.fromJson(response, Any::class.java)
        } catch (e: Exception) {
            response = gson?.toJson(ApiResponse(-1, response, null))!!
        }

        val contentType = value.contentType()
        val charset = if (contentType != null) contentType.charset(UTF_8) else UTF_8
        val inputStream: InputStream = ByteArrayInputStream(response.toByteArray())
        val reader: Reader = InputStreamReader(inputStream, charset)
        val jsonReader = gson?.newJsonReader(reader)
        return value.use { adapter?.read(jsonReader) }
    }

}