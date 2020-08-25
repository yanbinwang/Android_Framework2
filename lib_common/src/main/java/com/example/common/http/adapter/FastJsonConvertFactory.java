package com.example.common.http.adapter;


import com.alibaba.fastjson.JSON;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by WangYanBin on 2020/8/25.
 */
public class FastJsonConvertFactory extends Converter.Factory {

    public static FastJsonConvertFactory create() {
        return new FastJsonConvertFactory();
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new FastJsonResponseBodyConverter<>(type);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return new FastJsonRequestBodyConverter();
    }

    private static class FastJsonRequestBodyConverter<T> implements Converter<T, RequestBody> {
        private static final Charset UTF_8 = StandardCharsets.UTF_8;
        private static final MediaType type = MediaType.parse("application/json; charset=UTF-8");

        @Override
        public RequestBody convert(@NotNull T value) throws IOException {
            return RequestBody.create(type, JSON.toJSONString(value).getBytes(UTF_8));
        }
    }

    private static class FastJsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
        private Type type;
        private static final Charset UTF_8 = StandardCharsets.UTF_8;

        public FastJsonResponseBodyConverter(Type type) {
            this.type = type;
        }

        @Override
        public T convert(ResponseBody value) throws IOException {
            InputStreamReader inputStreamReader;
            BufferedReader reader;

            inputStreamReader = new InputStreamReader(value.byteStream(), UTF_8);
            reader = new BufferedReader(inputStreamReader);

            StringBuilder sb = new StringBuilder();

            String line;
            if ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            inputStreamReader.close();
            reader.close();
            return JSON.parseObject(sb.toString(), type);
        }
    }

}
