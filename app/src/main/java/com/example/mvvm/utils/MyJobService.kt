package com.example.mvvm.utils

import android.app.job.JobParameters
import android.app.job.JobService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 5.0+事物类
 * <service
 *     android:name=".MyJobService"
 *     android:permission="android.permission.BIND_JOB_SERVICE"
 *     android:exported="false" />
 *
 * android:exported 的作用
 * true	组件可被其他应用调用（无论是否设置了 intent-filter）。
 * false	组件只能被同一应用或具有相同用户 ID（UID）的应用调用。
 * 若组件声明了 intent-filter，则必须显式设置 android:exported，否则应用将无法安装
 *
 * 开始调度
 * private fun scheduleJob() {
 * val componentName = ComponentName(this, MyJobService::class.java)
 * val builder = JobInfo.Builder(JOB_ID, componentName)
 *
 * 设置任务触发条件（根据需求选择）
 * builder.apply {
 *     setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // 需要网络
 *     setRequiresCharging(false) // 不需要充电
 *     setRequiresDeviceIdle(false) // 不需要设备空闲
 *     setMinimumLatency(5000) // 延迟5秒执行
 *     setOverrideDeadline(60000) // 最长60秒后必须执行
 *
 *     // 若需周期性任务（API 24+）
 *     if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
 *         setPeriodic(15 * 60 * 1000) // 最低15分钟（系统限制）
 *     }
 *
 *     // 添加额外数据（可选）
 *     val extras = Bundle().apply {
 *         putString("key", "value")
 *     }
 *     setExtras(extras)
 * }
 *
 * 获取 JobScheduler 并调度任务
 * val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
 * val resultCode = jobScheduler.schedule(builder.build())
 *
 * if (resultCode == JobScheduler.RESULT_SUCCESS) {
 *     Log.d("MainActivity", "Job scheduled successfully")
 * } else {
 *     Log.d("MainActivity", "Job scheduling failed")
 * }
 *
 * 取消任务（例如在 Activity 销毁时）
 * override fun onDestroy() {
 *   super.onDestroy()
 *   val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
 *   jobScheduler.cancel(JOB_ID)
 * }
 *
 * JobInfo.Builder 参数
 * setRequiredNetworkType()	指定网络条件（如 NETWORK_TYPE_ANY, NETWORK_TYPE_UNMETERED）
 * setRequiresCharging()	是否需要设备充电
 * setRequiresDeviceIdle()	是否需要设备处于空闲状态
 * setMinimumLatency()	延迟执行的最小时间（毫秒）
 * setOverrideDeadline()	最长等待时间（毫秒），超时后强制执行
 * setPeriodic()	设置周期性任务（最低 15 分钟，受系统限制）
 * setPersisted()	任务是否在设备重启后保留（需 RECEIVE_BOOT_COMPLETED 权限）
 * setBackoffCriteria()	设置重试策略（如指数退避）
 *
 * 设备重启后任务丢失
 * 添加 setPersisted(true) 并声明 RECEIVE_BOOT_COMPLETED 权限
 * <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
 */
class MyJobService : JobService() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(IO + job)

    override fun onStartJob(params: JobParameters?): Boolean {
        // 执行异步任务（推荐使用协程）
        scope.launch {
//            try {
//                // 模拟耗时操作（如下载文件、同步数据）
//                doBackgroundWork(params)
//                // 任务完成后通知系统（API 24+），通知系统是否需要重试
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    jobFinished(params, false) // 第二个参数：是否需要重试
//                }
//            } catch (e: Exception) {
//                Log.e("MyJobService", "Job failed: ${e.message}")
//                // 任务失败，需要重试
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    jobFinished(params, true) // 请求系统重试
//                }
//            }
        }
        // 返回 true 表示任务是异步的（在后台线程执行）
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        // 取消正在执行的任务
        job.cancel()
        // 返回 true 表示系统应在条件满足时重新调度此任务
        return true
    }

    private suspend fun doBackgroundWork(params: JobParameters) {
//        // 获取任务参数（如果有）
//        val inputData = params.extras?.getString("key") ?: ""
//        // 执行具体任务...
//        for (i in 1..10) {
//            Log.d("MyJobService", "Working: $i/10")
//            kotlinx.coroutines.delay(1000)
//        }
    }

}