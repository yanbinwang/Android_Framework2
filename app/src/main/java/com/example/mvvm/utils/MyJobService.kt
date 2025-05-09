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
 * //开始调度
 * private fun scheduleJob() {
 * val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
 * //检查 Job 是否已调度
 * val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
 * //检查是否已调度
 * val isScheduled = jobScheduler.allPendingJobs.any { it.id == JOB_ID }
 * if (isScheduled) return
 * val componentName = ComponentName(this, MyJobService::class.java)
 * val builder = JobInfo.Builder(JOB_ID, componentName)
 *
 * //设置任务触发条件（根据需求选择）
 * builder.apply {
 *     setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // 需要网络
 *     setRequiresCharging(false) // 不需要充电
 *     setRequiresDeviceIdle(false) // 不需要设备空闲
 *     setMinimumLatency(5000) // 延迟5秒执行
 *     setOverrideDeadline(60000) // 最长60秒后必须执行
 *     // 若需周期性任务（API 24+）
 *     if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
 *         setPeriodic(15 * 60 * 1000) // 最低15分钟（系统限制）
 *     }
 *     // 添加额外数据（可选）
 *     val extras = Bundle().apply {
 *         putString("key", "value")
 *     }
 *     setExtras(extras)
 * }
 *
 * //获取 JobScheduler 并调度任务
 * val resultCode = jobScheduler.schedule(builder.build())
 * if (resultCode == JobScheduler.RESULT_SUCCESS) {
 *     Log.d("MainActivity", "Job scheduled successfully")
 * } else {
 *     Log.d("MainActivity", "Job scheduling failed")
 * }
 * }
 *
 * //取消任务（例如在 Activity 销毁时）
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
 *
 * setMinimumLatency()	设置任务最早执行时间（避免频繁调度，给系统缓冲时间）。
 * setOverrideDeadline()	设置任务最晚执行时间（超时后强制执行，即使条件不满足）。
 *
 * 1. 立即执行任务
 * 若需任务尽快执行（但非严格立即）：
 * .setMinimumLatency(1000)  // 延迟1秒，给系统喘息时间
 * .setOverrideDeadline(3000) // 最多延迟3秒
 * Android 系统会对 0 做特殊处理（如加入额外延迟），设置小值（如 1-3 秒）反而更接近 “立即执行”。
 *
 * 2. 延迟执行任务
 * 若任务不紧急（如批量数据同步）：
 * .setMinimumLatency(5 * 60 * 1000)  // 至少延迟5分钟
 * .setOverrideDeadline(30 * 60 * 1000) // 最多延迟30分钟
 * 系统会在 5-30 分钟内选择最优时机执行（如网络空闲、CPU 负载低）
 *
 * 3. 周期性任务
 * 若使用 setPeriodic()，建议搭配 setFlexPeriodMillis()（API 24+）：
 * .setPeriodic(15 * 60 * 1000)       // 周期15分钟（系统最小间隔）
 * .setFlexPeriodMillis(5 * 60 * 1000) // 在周期内的后5分钟内灵活执行
 * 避免所有应用同时触发任务，减少系统压力
 *
 * 4. 重量级任务（如备份）
 * 若任务消耗资源大，需在用户不活跃时执行：
 * .setMinimumLatency(0)
 * .setOverrideDeadline(24 * 60 * 60 * 1000) // 1天内执行
 * .setRequiresDeviceIdle(true)              // 仅设备空闲时执行
 * .setRequiresCharging(true)                // 仅充电时执行
 * 系统会在夜间充电且用户休眠时执行，避免影响体验
 *
 * enum class JobPriority {
 *     IMMEDIATE,      // 立即执行（如消息推送）
 *     HIGH,           // 高优先级（如支付回调）
 *     NORMAL,         // 普通优先级（如数据同步）
 *     LOW;            // 低优先级（如日志上传）
 *
 *     fun getMinimumLatency(): Long = when(this) {
 *         IMMEDIATE -> 1000
 *         HIGH -> 5000
 *         NORMAL -> 60000
 *         LOW -> 5 * 60 * 1000
 *     }
 *
 *     fun getOverrideDeadline(): Long = when(this) {
 *         IMMEDIATE -> 3000
 *         HIGH -> 30000
 *         NORMAL -> 30 * 60 * 1000
 *         LOW -> 24 * 60 * 60 * 1000
 *     }
 * }
 * object JobConfigBuilder {
 *     fun build(
 *         context: Context,
 *         jobId: Int,
 *         priority: JobPriority,
 *         networkType: Int = JobInfo.NETWORK_TYPE_ANY,
 *         requiresCharging: Boolean = false,
 *         requiresIdle: Boolean = false
 *     ): JobInfo {
 *         return JobInfo.Builder(jobId, ComponentName(context, MyJobService::class.java))
 *             .setMinimumLatency(priority.getMinimumLatency())
 *             .setOverrideDeadline(priority.getOverrideDeadline())
 *             .setRequiredNetworkType(networkType)
 *             .setRequiresCharging(requiresCharging)
 *             .setRequiresDeviceIdle(requiresIdle)
 *             .build()
 *     }
 * }
 * // 高优先级任务（如支付结果同步）
 * val jobInfo = JobConfigBuilder.build(
 *     context = this,
 *     jobId = JOB_ID_PAYMENT,
 *     priority = JobPriority.HIGH,
 *     networkType = JobInfo.NETWORK_TYPE_UNMETERED, // 仅 Wi-Fi
 *     requiresCharging = true
 * )
 * jobScheduler.schedule(jobInfo)
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