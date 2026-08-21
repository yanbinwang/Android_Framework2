package com.example.mvvm.bean

data class KLineBean(
    var Close: String? = null, // 收盘价 -> 该时间段内最后一笔成交的价格。决定蜡烛实体的上沿（阳线）或下沿（阴线）。最重要：几乎所有技术指标（MA/BOLL/MACD/RSI/KDJ）都用它来计算
    var Date: String? = null, // 时间戳 -> 这根K线代表的时间周期起点（如日线的交易日、分钟线的起始分钟）。决定了X轴的刻度
    var High: String? = null, // 最高价 -> 该时间段内所有成交中的最高价格。决定蜡烛上方影线的顶端
    var Low: String? = null, // 最低价 -> 该时间段内所有成交中的最低价格。决定蜡烛下方影线的底端
    var Open: String? = null, // 开盘价 -> 该时间段内第一笔成交的价格。决定蜡烛实体的下沿（阳线）或上沿（阴线）
    var Volume: String? = null // 成交量 -> 该时间段内的总成交数量（股数/手数）。用于绘制副图的成交量柱状图，以及计算均量线（MA5Vol/MA10Vol）
)