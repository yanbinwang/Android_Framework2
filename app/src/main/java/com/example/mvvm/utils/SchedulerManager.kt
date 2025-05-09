package com.example.mvvm.utils

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import com.example.framework.utils.logD

/**
 * 新建一个job管理类
 */
class SchedulerManager(private val context: Context) {
    private val TAG = "SchedulerManager"
    private val jobScheduler by lazy { context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler }

    companion object {

        /**
         * 方法	      build()	               buildPeriodicJob()
         * 任务类型	  一次性任务（执行一次）	   周期性任务（循环执行）
         * 核心参数	  优先级、网络、充电状态等	   执行周期（intervalMillis）、灵活窗口（flexMillis）
         * 适用场景	  立即执行或延迟执行的任务	   定时同步、定期清理等循环任务
         * API 兼容性  全版本统一	               Android N（API 24）以上支持灵活窗口
         *
         * build()：创建一次性任务，聚焦 单次执行的精确控制。
         * buildPeriodicJob()：创建周期性任务，聚焦 循环执行的系统优化。
         */
        @JvmStatic
        fun build(
            jobId: Int,
            jobService: ComponentName,
            priority: JobPriority,
            networkType: Int = JobInfo.NETWORK_TYPE_ANY,
            requiresCharging: Boolean = false,// 仅充电时执行
            requiresIdle: Boolean = false// 仅设备空闲时执行
        ): JobInfo {
            return JobInfo.Builder(jobId, jobService)
                .setMinimumLatency(priority.getMinimumLatency())
                .setOverrideDeadline(priority.getOverrideDeadline())
                .setRequiredNetworkType(networkType)
                .setRequiresCharging(requiresCharging)
                .setRequiresDeviceIdle(requiresIdle)
                .build()
        }

        @JvmStatic
        fun buildPeriodicJob(
            jobId: Int,
            jobService: ComponentName,
            intervalMillis: Long,//控制任务执行的最小间隔，避免过度频繁。
            flexMillis: Long = intervalMillis / 3,//给系统灵活调整空间，通过批量处理任务降低电量消耗。
            priority: JobPriority = JobPriority.NORMAL
        ): JobInfo {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                JobInfo.Builder(jobId, jobService)
                    .setPeriodic(intervalMillis, flexMillis)
                    .setMinimumLatency(priority.getMinimumLatency())
                    .build()
            } else {
                JobInfo.Builder(jobId, jobService)
                    .setPeriodic(intervalMillis)
                    .setMinimumLatency(priority.getMinimumLatency())
                    .build()
            }
        }
    }

    /**
     * 构建job
     */
    private fun schedulePeriodicJob(jobId: Int, jobInfo: JobInfo): Boolean {
        if (isJobScheduled(jobId)) {
            "Job $jobId already scheduled, skipping".logD(TAG)
            return false
        }
        val result = jobScheduler.schedule(jobInfo)
        "Schedule job $jobId result: $result".logD(TAG)
        return result == JobScheduler.RESULT_SUCCESS
    }

    /**
     * 检测job是否已经开启
     */
    private fun isJobScheduled(jobId: Int): Boolean {
        return jobScheduler.allPendingJobs.any { it.id == jobId }
    }

    /**
     * 链式调用扩展
     */
    fun schedule(jobId: Int, jobInfo: JobInfo): SchedulerManager {
        schedulePeriodicJob(jobId, jobInfo)
        return this
    }

    /**
     * 取消某个job
     */
    fun cancelJob(jobId: Int): SchedulerManager {
        "Canceling job $jobId".logD(TAG)
        jobScheduler.cancel(jobId)
        return this
    }

    /**
     * 取消全部job
     */
    fun cancelAllJobs(): SchedulerManager {
        "Canceling all jobs".logD(TAG)
        jobScheduler.cancelAll()
        return this
    }

}

/**
 * 自定义优先级
 */
enum class JobPriority {
    IMMEDIATE,      // 立即执行（如消息推送）
    HIGH,           // 高优先级（如支付回调）
    NORMAL,         // 普通优先级（如数据同步）
    LOW;            // 低优先级（如日志上传）

    fun getMinimumLatency(): Long = when (this) {
        IMMEDIATE -> 1000
        HIGH -> 5000
        NORMAL -> 60000
        LOW -> 5 * 60 * 1000
    }

    fun getOverrideDeadline(): Long = when (this) {
        IMMEDIATE -> 3000
        HIGH -> 30000
        NORMAL -> 30 * 60 * 1000
        LOW -> 24 * 60 * 60 * 1000
    }
}