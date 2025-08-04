package com.example.storemanagerassitent.data

/**
 * 时间维度枚举
 */
enum class TimePeriod(val displayName: String, val key: String) {
    WEEK("本周", "week"),
    MONTH("本月", "month"),
    YEAR("本年", "year")
}

/**
 * 分类销售数据
 */
data class CategorySalesData(
    val categoryName: String,
    val salesCount: Int,
    val categoryColor: String = "#2196F3"
)

/**
 * 销售洞察数据
 */
data class SalesInsightData(
    val period: TimePeriod,
    val categorySales: List<CategorySalesData>
)

/**
 * 快速操作项
 */
data class QuickAction(
    val id: String,
    val title: String,
    val description: String,
    val secondaryActionText: String,
    val iconName: String = "default"
)

/**
 * 示例数据
 */
object HomeData {
    /**
     * 快速操作数据
     */
    val quickActions = listOf(
        QuickAction(
            id = "sales",
            title = "销售开单",
            description = "快速为客户创建销售单",
            secondaryActionText = "查看销售记录 →"
        ),
        QuickAction(
            id = "purchase",
            title = "进货开单", 
            description = "登记新到货的商品",
            secondaryActionText = "查看进货记录 →"
        )
    )
    
    /**
     * 示例销售洞察数据
     */
    fun getSalesInsightData(period: TimePeriod): SalesInsightData {
        val categorySales = when (period) {
            TimePeriod.WEEK -> listOf(
                CategorySalesData("卫浴洁具", 125, "#4CAF50"),
                CategorySalesData("手动工具", 89, "#2196F3"),
                CategorySalesData("电动工具", 67, "#FF9800"),
                CategorySalesData("五金配件", 45, "#9C27B0"),
                CategorySalesData("装修材料", 32, "#F44336")
            )
            TimePeriod.MONTH -> listOf(
                CategorySalesData("卫浴洁具", 485, "#4CAF50"),
                CategorySalesData("手动工具", 356, "#2196F3"),
                CategorySalesData("电动工具", 298, "#FF9800"),
                CategorySalesData("五金配件", 189, "#9C27B0"),
                CategorySalesData("装修材料", 167, "#F44336"),
                CategorySalesData("安全设备", 134, "#607D8B")
            )
            TimePeriod.YEAR -> listOf(
                CategorySalesData("卫浴洁具", 5420, "#4CAF50"),
                CategorySalesData("手动工具", 4230, "#2196F3"),
                CategorySalesData("电动工具", 3890, "#FF9800"),
                CategorySalesData("装修材料", 2340, "#F44336"),
                CategorySalesData("五金配件", 2180, "#9C27B0"),
                CategorySalesData("安全设备", 1890, "#607D8B"),
                CategorySalesData("园艺工具", 1456, "#009688")
            )
        }
        
        return SalesInsightData(
            period = period,
            categorySales = categorySales.sortedByDescending { it.salesCount }
        )
    }
}