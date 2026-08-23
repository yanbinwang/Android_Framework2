package com.example.klinechart.widget.draw

import android.graphics.Canvas
import com.example.klinechart.utils.formatter.value.IValueFormatter
import com.example.klinechart.widget.BaseKLineChartView

/**
 * K线图指标绘制接口
 * @param T 数据实体类型（如 ICandle），解耦具体数据模型与绘制逻辑
 */
interface IChartDraw<T> {

    /**
     * 绘制相邻两点之间的图形元素（连线、柱体、填充区域等）
     * 该方法在每一帧渲染时都会被调用，属于热路径，应避免对象创建和复杂计算
     * @param canvas 绘图画布
     * @param view K线图 View 实例，用于获取配置、尺寸等上下文信息
     * @param position 当前点在数据集中的索引位置
     * @param lastPoint 上一个数据点，第一个点时为 null，实现时需判空处理
     * @param curPoint 当前数据点
     * @param lastX 上一个点在画布上的 X 坐标（已计算好，避免重复映射）
     * @param curX 当前点在画布上的 X 坐标（已计算好，避免重复映射）
     */
    fun drawTranslated(canvas: Canvas, view: BaseKLineChartView, position: Int, lastPoint: T?, curPoint: T?, lastX: Float, curX: Float)

    /**
     * 绘制当前点的指标数值文本标签
     * 通常仅在长按选择器激活时调用，与图形绘制分离以按需触发、减少开销
     * @param canvas 绘图画布
     * @param view K线图 View 实例
     * @param position 当前点在数据集中的索引位置
     * @param x 文本绘制的起始 X 坐标（通常为选择器十字线交点或图例锚点）
     * @param y 文本绘制的起始 Y 坐标（可能包含偏移以避免遮挡蜡烛）
     */
    fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float)

    /**
     * 获取单个数据点在该指标下的最大值
     * 用于 BaseKLineChartView 遍历可见区间收集全局极值，确定 Y 轴缩放范围，不同指标取不同字段（如 MA 取 maValue，BOLL 取 upperBand）
     * @param point 数据点，为 null 时应返回安全默认值（如 0f 或 Float.NaN）
     * @return 该点对应的指标最大值
     */
    fun getMaxValue(point: T?): Float

    /**
     * 获取单个数据点在该指标下的最小值
     * 用于 BaseKLineChartView 遍历可见区间收集全局极值，确定 Y 轴缩放范围，不同指标取不同字段（如 MA 取 maValue，BOLL 取 lowerBand）
     * @param point 数据点，为 null 时应返回安全默认值（如 0f 或 Float.NaN）
     * @return 该点对应的指标最小值
     */
    fun getMinValue(point: T?): Float

    /**
     * 获取该指标专用的数值格式化器
     * 不同指标的展示规则不同（价格2位小数、成交量万/亿缩写、百分比带%等），drawText 内部通过此格式化器将 Float 转为展示字符串
     * @return 该指标对应的 IValueFormatter 实例
     */
    fun getValueFormatter(): IValueFormatter

}