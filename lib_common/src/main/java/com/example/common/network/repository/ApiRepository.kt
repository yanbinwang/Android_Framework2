package com.example.common.network.repository

import com.example.common.R
import com.example.common.base.bridge.BaseView
import com.example.common.network.repository.ApiCode.FAILURE
import com.example.common.network.repository.ApiCode.SUCCESS
import com.example.common.network.repository.ApiCode.TOKEN_EXPIRED
import com.example.common.utils.NetWorkUtil
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.resString
import com.example.common.utils.helper.AccountHelper
import com.example.common.utils.toJson
import com.example.framework.utils.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * 网络请求协程扩展
 * 1）如果几个以上的请求，使用当前请求类
 * 2）不做回调，直接得到结果，在不调用await（）方法时可以当一个参数写，调用了才会发起请求并拿到结果
 * //并发
 * launch{
 *   val req = MultiReqUtil(mView)
 *   val task1 = async { req.request({ CommonSubscribe.getVerificationApi(mapOf("key" to "value")) })?.apply {  } }
 *   val task2 = async{ req.request(model.getUserData() }
 *   req.end()
 *   //单个请求主动发起，处理对象
 *   task1.await()
 *   task2.await()
 *   //同时发起多个请求，list拿取对象
 *   val taskList = awaitAll(task1, task2)
 *   taskList.toObj<T>(0)
 *   taskList.toObj<T>(1)
 * }
 * //串行
 * launch{
 *    val req = MultiReqUtil(mView)
 *    val task1 = getUserDataAsync(req)
 *    val task2 = req.request({ model.getUserData() })
 *    req.end()
 * }
 *  private suspend fun getUserInfoAsync(req: MultiReqUtil): Deferred<UserInfoBean?> {
 *     return async { req.request({ CommonSubscribe.getUserInfoApi(hashMapOf("id" to AccountHelper.getUserId())) }) }
 *  }
 */
class MultiReqUtil(
    private var view: BaseView? = null,
    private val isShowDialog: Boolean = true,
    private val err: (e: Triple<Int?, String?, Exception?>?) -> Unit = {}
) {
    private var results = false//一旦有请求失败，就会为true
    private var loadingStarted = false//是否开始加载

    /**
     * 发起请求
     */
    suspend fun <T> request(
        coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>,
        err: (e: Triple<Int?, String?, Exception?>?) -> Unit = this.err
    ): T? {
        return requestLayer(coroutineScope, err)?.data
    }

    suspend fun <T> requestLayer(
        coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>,
        err: (e: Triple<Int?, String?, Exception?>?) -> Unit = this.err
    ): ApiResponse<T>? {
        start()
        var response: ApiResponse<T>? = null
        requestLayer({ coroutineScope() }, {
            response = it
        }, err, isShowToast = false)
        if (!response.successful()) results = true
        return response
    }

    suspend fun <T> requestAffair(
        coroutineScope: suspend CoroutineScope.() -> T,
        err: (e: Triple<Int?, String?, Exception?>?) -> Unit = this.err
    ): T? {
        start()
        var result: T? = null
        requestAffair({ coroutineScope() }, {
            result = it
        }, {
            results = true
            err.invoke(it)
        }, isShowToast = false)
        return result
    }

    /**
     * 请求开始前自动调取
     */
    private fun start() {
        if (isShowDialog && !loadingStarted) {
            view?.showDialog()
            loadingStarted = true
        }
    }

    /**
     * 请求结束主动调取
     */
    fun end() {
        if (isShowDialog) {
            view?.hideDialog()
            loadingStarted = false
        }
        view = null
        results = false
    }

    /**
     * 当串行请求多个接口的时候，如果开发需要知道这多个串行请求是否都成功
     * 在end()被调取之前，可通过当前方法判断
     */
    fun successful(): Boolean {
        return !results
    }

}

//------------------------------------针对协程返回的参数(协程只有成功和失败)------------------------------------
/**
 * 请求转换
 * map扩展，如果只需传入map则使用
 * hashMapOf("" to "")不需要写此扩展
 */
fun <K, V> HashMap<K, V>?.requestBody() =
    this?.toJson().orEmpty().toRequestBody("application/json; charset=utf-8".toMediaType())

fun reqBodyOf(vararg pairs: Pair<String, Any?>): RequestBody {
    val map = hashMapOf<String, Any>()
    pairs.forEach {
        it.second?.let { v ->
            map[it.first] = v
        }
    }
    return map.requestBody()
}

/**
 * 提示方法，根据接口返回的msg提示
 */
fun String?.responseToast() =
    (if (!NetWorkUtil.isNetworkAvailable()) resString(R.string.responseNetError) else {
        if (isNullOrEmpty()) resString(R.string.responseError) else this
    }).shortToast()

/**
 * 网络请求协程扩展-并行请求
 * 每个挂起方法外层都会套一个launch
 * requestLayer中已经对body做了处理，直接拿对象返回即可
 * 如果返回格式过于奇葩，放在body层级，则做特殊处理，其余情况不做改进！
 */
suspend fun <T> request(
    coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>,
    resp: (T?) -> Unit = {},
    err: (e: Triple<Int?, String?, Exception?>?) -> Unit = {},
    end: () -> Unit = {},
    isShowToast: Boolean = false
) {
    requestLayer(coroutineScope, { result ->
        //如果接口是成功的，但是body为空或者后台偷懒没给，我们在写Api时，给一个对象，让结果能够返回
        resp.invoke(result?.data.let {
            if (it is EmptyBean) {
                EmptyBean()
            } else {
                it
            } as? T
        })
    }, err, end, isShowToast)
}

suspend fun <T> requestLayer(
    coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>,
    resp: (ApiResponse<T>?) -> Unit = {},
    err: (e: Triple<Int?, String?, Exception?>?) -> Unit = {},
    end: () -> Unit = {},
    isShowToast: Boolean = false
) {
    //优先使用try/catch，冷流切换数据内存开销相对较大
    try {
        log("开始请求")
        //请求+响应数据
        withContext(IO) {
            log("发起请求")
            coroutineScope()
        }.let {
            log("处理结果")
            if (it.successful()) {
                resp(it)
            } else {
                //如果不是被顶号才会有是否提示的逻辑
                if (!it.tokenExpired()) if (isShowToast) it.msg.responseToast()
                //不管结果如何，失败的回调是需要执行的
                err(Triple(it.code, it.msg, null))
            }
        }
    } catch (e: Exception) {
        if (isShowToast) "".responseToast()
        //可根据具体异常显示具体错误提示,此处可能是框架/服务器报错（没有提供规定的json结构体）或者json结构解析错误
        err(Triple(FAILURE, "", e))
    } finally {
        log("结束请求")
        end()
    }
}

suspend fun <T> requestAffair(
    coroutineScope: suspend CoroutineScope.() -> T,
    resp: (T?) -> Unit = {},
    err: (e: Triple<Int?, String?, Exception?>?) -> Unit = {},
    end: () -> Unit = {},
    isShowToast: Boolean = false
) {
    try {
        resp(withContext(IO) { coroutineScope() })
    } catch (e: Exception) {
        if (isShowToast) "".responseToast()
        err(Triple(FAILURE, "", e))
    } finally {
        end()
    }
}

private fun log(msg: String) = "${msg}->当前线程：${Thread.currentThread().name}".logE("repository")

/**
 * 判断此次请求是否成功
 */
fun <T> ApiResponse<T>?.successful(): Boolean {
    if (this == null) return false
    return SUCCESS == code
}

/**
 * 判断此次请求是否token过期
 */
fun <T> ApiResponse<T>?.tokenExpired(): Boolean {
    if (this == null) return false
    if (TOKEN_EXPIRED == code) {
        AccountHelper.signOut()
        return true
    }
    return false
}