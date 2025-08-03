package com.example.storemanagerassitent.data

/**
 * 商品数据模型
 */
data class Goods(
    val id: String,
    val name: String,
    val category: String,
    val specifications: String, // 规格/型号
    val stockQuantity: Int,
    val lowStockThreshold: Int = 5, // 低库存阈值，默认5件
    val imageUrl: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * 是否低库存
     */
    val isLowStock: Boolean
        get() = stockQuantity <= lowStockThreshold
}

/**
 * 商品分类
 */
data class GoodsCategory(
    val id: String,
    val name: String
)

/**
 * 排序选项
 */
enum class SortOption(val displayName: String) {
    STOCK_ASC("按库存(从少到多)"),
    NAME_A_Z("按名称(A-Z)"),
    LAST_UPDATED("按更新时间")
}

/**
 * 样本数据
 */
object SampleData {
    val categories = listOf(
        GoodsCategory("all", "全部"),
        GoodsCategory("bathroom", "卫浴洁具"),
        GoodsCategory("manual_tools", "手动工具"),
        GoodsCategory("power_tools", "电动工具"),
        GoodsCategory("pipes", "管材管件"),
        GoodsCategory("screws", "螺丝螺母")
    )
    
    val goods = listOf(
        // 卫浴洁具
        Goods(
            id = "1",
            name = "九牧王单孔冷热龙头",
            category = "bathroom",
            specifications = "型号: J-1022",
            stockQuantity = 12,
            lowStockThreshold = 5
        ),
        Goods(
            id = "2",
            name = "科勒浴缸水龙头",
            category = "bathroom",
            specifications = "型号: K-45102T",
            stockQuantity = 3,
            lowStockThreshold = 5
        ),
        Goods(
            id = "3",
            name = "汉斯格雅花洒套装",
            category = "bathroom",
            specifications = "型号: HG-2680",
            stockQuantity = 8,
            lowStockThreshold = 5
        ),
        
        // 手动工具
        Goods(
            id = "4",
            name = "世达螺丝刀套装",
            category = "manual_tools",
            specifications = "规格: 10件套",
            stockQuantity = 15,
            lowStockThreshold = 8
        ),
        Goods(
            id = "5",
            name = "德国进口扳手",
            category = "manual_tools",
            specifications = "规格: 8-24mm",
            stockQuantity = 4,
            lowStockThreshold = 6
        ),
        Goods(
            id = "6",
            name = "钢丝钳",
            category = "manual_tools",
            specifications = "规格: 8寸",
            stockQuantity = 20,
            lowStockThreshold = 10
        ),
        
        // 电动工具
        Goods(
            id = "7",
            name = "博世电钻",
            category = "power_tools",
            specifications = "型号: GSB120-LI",
            stockQuantity = 6,
            lowStockThreshold = 3
        ),
        Goods(
            id = "8",
            name = "牧田角磨机",
            category = "power_tools",
            specifications = "型号: GA5030",
            stockQuantity = 2,
            lowStockThreshold = 4
        ),
        
        // 管材管件
        Goods(
            id = "9",
            name = "PVC管",
            category = "pipes",
            specifications = "规格: DN110×3m",
            stockQuantity = 25,
            lowStockThreshold = 15
        ),
        Goods(
            id = "10",
            name = "铜管接头",
            category = "pipes",
            specifications = "规格: 20mm",
            stockQuantity = 50,
            lowStockThreshold = 30
        ),
        
        // 螺丝螺母
        Goods(
            id = "11",
            name = "304不锈钢螺丝",
            category = "screws",
            specifications = "规格: M6×25",
            stockQuantity = 200,
            lowStockThreshold = 100
        ),
        Goods(
            id = "12",
            name = "镀锌螺母",
            category = "screws",
            specifications = "规格: M8",
            stockQuantity = 80,
            lowStockThreshold = 50
        )
    )
}