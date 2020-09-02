package com.example.common.http.repository

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

/**
 * Created by WangYanBin on 2020/9/2.
 */
class BaseRepository {

    suspend fun <T: Any> apiCall(call: suspend() -> ApiResponse<T>) : ApiResponse<T> {
        return withContext(IO) { call.invoke() }.apply {
            //请求编号特殊处理
            when (e) {
//                200->
//                99999 -> throw HttpException()
//                80000 -> throw TokenInvalidException()
            }
        }
    }

    class TokenInvalidException(msg : String = "token invalid"): Exception(msg)
}