package com.example.common.http.adapter;

import com.example.common.http.callback.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Converter;

import static kotlin.text.Charsets.UTF_8;

/**
 * Created by WangYanBin on 2020/8/25.
 */
final class CustomGsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final TypeAdapter<T> adapter;

    CustomGsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
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

        String response = value.string();
        //尝试转换，格式转换异常则包装一个对象返回
        try {
            gson.fromJson(response, Object.class);
        } catch (Exception e) {
            response = gson.toJson(new ApiResponse(-1, response, null));
        }

        MediaType contentType = value.contentType();
        Charset charset = contentType != null ? contentType.charset(UTF_8) : UTF_8;
        InputStream inputStream = new ByteArrayInputStream(response.getBytes());
        Reader reader = new InputStreamReader(inputStream, charset);
        JsonReader jsonReader = gson.newJsonReader(reader);
        try {
            return adapter.read(jsonReader);
        } finally {
            value.close();
        }
    }

}