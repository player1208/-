package com.example.storemanagerassitent.ui.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import com.example.storemanagerassitent.data.Goods
import com.example.storemanagerassitent.data.SalesOrderFormatter
import com.example.storemanagerassitent.data.SalesOrderItem
import com.example.storemanagerassitent.data.SampleData
import com.example.storemanagerassitent.ui.components.GlobalSuccessMessage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun InventoryStyleProductSelectionScreen(
    onDismiss: () -> Unit,
    onSelectProduct: (Goods) -> Unit,
    onAddConfirmedItems: (List<SalesOrderItem>) -> Unit,
    getAllGoods: () -> List<Goods>,
    cartItems: List<SalesOrderItem>,
    onAddToCart: (SalesOrderItem) -> Unit,
    onRemoveFromCart: (String) -> Unit,
    onClearCart: () -> Unit
) {
    val goods = getAllGoods()
    val categories = SampleData.categories
    var selectedCategory by remember { mutableStateOf("all") }
    var searchText by remember { mutableStateOf("") }
    // 使用ViewModel的购物车状态，不再使用本地pendingItems
    var showQuantityDialog by remember { mutableStateOf(false) }
    var selectedGoods by remember { mutableStateOf<Goods?>(null) }
    var quantityAmount by remember { mutableStateOf(1) }
    var isUpdatingExistingItem by remember { mutableStateOf(false) } // 标识是否是更新现有商品
    var showPendingListDialog by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    
    // 筛选商品
    val filteredGoods = remember(goods, selectedCategory, searchText) {
        val categoryFiltered = if (selectedCategory == "all") {
            goods.filter { !it.isDelisted }
        } else {
            goods.filter { it.category == selectedCategory && !it.isDelisted }
        }
        
        if (searchText.isBlank()) {
            categoryFiltered
        } else {
            categoryFiltered.filter { 
                it.name.contains(searchText, ignoreCase = true) ||
                it.specifications.contains(searchText, ignoreCase = true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "为当前订单选择商品",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                // 智能退出确认逻辑
                                if (cartItems.isNotEmpty()) {
                                    // 有待确认商品，显示确认对话框
                                    showExitConfirmDialog = true
                                } else {
                                    // 无待确认商品，直接返回
                                    onDismiss()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 搜索栏
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("搜索商品名称/规格") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "搜索"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                )
                
                // 分类筛选栏
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            onClick = { selectedCategory = category.id },
                            label = { Text(category.name) },
                            selected = selectedCategory == category.id,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                
                // 商品列表
                if (filteredGoods.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchText.isNotBlank()) "未找到匹配的商品" else "该分类下暂无商品",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 按分类分组显示商品（仅在"全部"筛选时）
                        if (selectedCategory == "all") {
                            val groupedGoods = categories.filter { it.id != "all" }.mapNotNull { category ->
                                val categoryGoods = filteredGoods.filter { it.category == category.id }
                                if (categoryGoods.isNotEmpty()) {
                                    category to categoryGoods
                                } else null
                            }
                            
                            groupedGoods.forEach { (category, categoryGoods) ->
                                // 分类标题 - 吸顶效果
                                stickyHeader(key = "header_${category.id}") {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                                        shadowElevation = 4.dp
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // 分类颜色指示条
                                            Box(
                                                modifier = Modifier
                                                    .width(4.dp)
                                                    .height(20.dp)
                                                    .background(
                                                        color = Color(0xFF4CAF50),
                                                        shape = RoundedCornerShape(2.dp)
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "--- ${category.name} ---",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                
                                // 该分类下的商品列表
                                items(
                                    items = categoryGoods,
                                    key = { "goods_${it.id}" }
                                ) { goods ->
                                    ProductCard(
                                        goods = goods,
                                        onAddClick = {
                                            selectedGoods = goods
                                            // 智能判断商品是否已在购物车中
                                            val existingItem = cartItems.find { it.goodsId == goods.id }
                                            if (existingItem != null) {
                                                // 商品已存在，显示"继续增加数量"对话框
                                                quantityAmount = existingItem.quantity
                                                isUpdatingExistingItem = true
                                            } else {
                                                // 商品不存在，显示"添加商品"对话框
                                                quantityAmount = 1
                                                isUpdatingExistingItem = false
                                            }
                                            showQuantityDialog = true
                                        }
                                    )
                                }
                            }
                        } else {
                            // 显示特定分类时，直接显示商品列表，无分组标题
                            items(
                                items = filteredGoods,
                                key = { "goods_${it.id}" }
                            ) { goods ->
                                ProductCard(
                                    goods = goods,
                                    onAddClick = {
                                        selectedGoods = goods
                                        // 智能判断商品是否已在购物车中
                                        val existingItem = cartItems.find { it.goodsId == goods.id }
                                        if (existingItem != null) {
                                            // 商品已存在，显示"继续增加数量"对话框
                                            quantityAmount = existingItem.quantity
                                            isUpdatingExistingItem = true
                                        } else {
                                            // 商品不存在，显示"添加商品"对话框
                                            quantityAmount = 1
                                            isUpdatingExistingItem = false
                                        }
                                        showQuantityDialog = true
                                    }
                                )
                            }
                        }
                        
                        // 底部占位空间，避免被悬浮购物车遮挡
                        item {
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
        
        // 悬浮购物车 - 固定在屏幕最左侧，紧贴合计金额灰框
        LeftAlignedShoppingCart(
            itemCount = cartItems.size, // 显示真实的商品数量
            onCartClick = { showPendingListDialog = true }
        )
        
        // 数量确认对话框
        if (showQuantityDialog && selectedGoods != null) {
            AlertDialog(
                onDismissRequest = {
                    showQuantityDialog = false
                    selectedGoods = null
                },
                title = {
                    Text(
                        text = if (isUpdatingExistingItem) "继续增加数量" else "添加商品",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = selectedGoods!!.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "单价: ${SalesOrderFormatter.formatCurrency(selectedGoods!!.retailPrice)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 数量选择
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("数量:")
                            
                            // 减少按钮
                            IconButton(
                                onClick = { if (quantityAmount > 1) quantityAmount-- },
                                enabled = quantityAmount > 1
                            ) {
                                Text("-", style = MaterialTheme.typography.titleLarge)
                            }
                            
                            Text(
                                text = quantityAmount.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            // 增加按钮
                            IconButton(
                                onClick = { if (quantityAmount < selectedGoods!!.stockQuantity) quantityAmount++ },
                                enabled = quantityAmount < selectedGoods!!.stockQuantity
                            ) {
                                Text("+", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "小计: ${SalesOrderFormatter.formatCurrency(selectedGoods!!.retailPrice * quantityAmount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // 添加/更新商品到购物车（addToCart方法已自动处理重复项）
                            val newItem = SalesOrderItem(
                                goodsId = selectedGoods!!.id,
                                goodsName = selectedGoods!!.name,
                                specifications = selectedGoods!!.specifications,
                                unitPrice = selectedGoods!!.retailPrice,
                                quantity = quantityAmount,
                                category = selectedGoods!!.category
                            )
                            
                            onAddToCart(newItem)
                            
                            // 显示相应的成功提示
                            GlobalSuccessMessage.showSuccess(
                                if (isUpdatingExistingItem) "✓ 数量已更新" else "✓ 添加成功"
                            )
                            
                            // 关闭对话框
                            showQuantityDialog = false
                            selectedGoods = null
                        },
                        enabled = quantityAmount > 0 && quantityAmount <= selectedGoods!!.stockQuantity
                    ) {
                        Text(if (isUpdatingExistingItem) "更新数量" else "确认添加")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showQuantityDialog = false
                        selectedGoods = null
                    }) {
                        Text("取消")
                    }
                }
            )
        }
        
        // 待确认商品列表对话框
        if (showPendingListDialog) {
            AlertDialog(
                onDismissRequest = { showPendingListDialog = false },
                title = {
                    Text(
                        text = "已选商品 (${cartItems.size})",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    if (cartItems.isEmpty()) {
                        Text("暂无商品，请先添加商品")
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(cartItems) { item ->
                                CartItemRow(
                                    item = item,
                                    onQuantityChange = { newQuantity ->
                                        if (newQuantity > 0) {
                                            // 更新数量（addToCart方法已自动处理重复项）
                                            val updatedItem = item.copy(quantity = newQuantity)
                                            onAddToCart(updatedItem)
                                        }
                                    },
                                    onRemove = { onRemoveFromCart(item.id) },
                                    maxQuantity = getAllGoods().find { it.id == item.goodsId }?.stockQuantity ?: 99
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    if (cartItems.isNotEmpty()) {
                        Button(
                            onClick = {
                                onAddConfirmedItems(cartItems)
                                onClearCart() // 确认添加后清空购物车
                                showPendingListDialog = false
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text(
                                text = "完成添加 (${cartItems.size})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPendingListDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
        
        // 退出确认对话框
        if (showExitConfirmDialog) {
            ExitConfirmDialog(
                selectedCount = cartItems.size,
                onDismiss = { showExitConfirmDialog = false },
                onDiscardChanges = { 
                    showExitConfirmDialog = false
                    onClearCart() // 放弃选择时清空购物车
                    onDismiss() // 直接返回，不保存
                },
                onContinueSelection = { 
                    showExitConfirmDialog = false // 继续选择，留在当前页面
                },
                onSaveAndReturn = {
                    showExitConfirmDialog = false
                    // 保存到购物车中，不直接添加到订单
                    // 购物车状态会保持，用户稍后可以查看并决定是否添加
                    onDismiss() // 直接返回，商品保留在购物车中
                }
            )
        }
    }
}

/**
 * 退出确认对话框
 */
@Composable
fun ExitConfirmDialog(
    selectedCount: Int,
    onDismiss: () -> Unit,
    onDiscardChanges: () -> Unit,
    onContinueSelection: () -> Unit,
    onSaveAndReturn: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "要保存已选商品吗？",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("您已选择了 $selectedCount 件商品，是否将这些商品添加到订单中？")
        },
        confirmButton = {
            Button(
                onClick = onSaveAndReturn,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    text = "保存并返回",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDiscardChanges) {
                    Text(
                        text = "放弃选择",
                        color = Color.Red
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onContinueSelection) {
                    Text("继续选择")
                }
            }
        }
    )
}

/**
 * 左侧固定悬浮购物车 - 固定在屏幕最左侧，紧贴合计金额灰框
 */
@Composable
fun LeftAlignedShoppingCart(
    itemCount: Int,
    onCartClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 固定位置：屏幕最左侧，底部合计栏上方
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart) // 左下角对齐
                .offset(
                    x = 16.dp,    // 距离左边缘16dp
                    y = (-120).dp // 距离底部120dp，避开合计栏
                )
                .size(72.dp) // 大尺寸，醒目
                .background(
                    color = Color(0xFF4CAF50), // 鲜绿色
                    shape = CircleShape
                )
                .clickable { onCartClick() },
            contentAlignment = Alignment.Center
        ) {
            // 购物车图标
            Icon(
                imageVector = Icons.Filled.ShoppingCart,
                contentDescription = "购物车",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
            
            // 数量徽章 - 只有真正添加商品时才显示
            if (itemCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 14.dp, y = (-14).dp)
                        .size(26.dp)
                        .background(Color.Red, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = itemCount.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // 移除调试信息和箭头
    }
}

/**
 * 商品卡片组件
 */
@Composable
fun ProductCard(
    goods: Goods,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 分类颜色条
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .background(
                        color = Color(0xFF007BFF),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 商品信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goods.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "库存: ${goods.stockQuantity} 件",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "单价: ${SalesOrderFormatter.formatCurrency(goods.retailPrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // 添加按钮
            IconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "添加商品",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * 购物车商品项组件 - 支持数量调节
 */
@Composable
fun CartItemRow(
    item: SalesOrderItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    maxQuantity: Int,
    modifier: Modifier = Modifier
) {
    var showRemoveConfirmDialog by remember { mutableStateOf(false) }
    var showQuantityInputDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 商品名称和单价
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "单价: ${SalesOrderFormatter.formatCurrency(item.unitPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                // 删除按钮
                TextButton(
                    onClick = { showRemoveConfirmDialog = true }
                ) {
                    Text("删除", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 数量调节器和小计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 数量调节器
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "数量:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    // 减少按钮
                    IconButton(
                        onClick = {
                            if (item.quantity > 1) {
                                onQuantityChange(item.quantity - 1)
                            } else {
                                // 数量为1时点击减号，显示删除确认
                                showRemoveConfirmDialog = true
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (item.quantity > 1) Icons.Filled.KeyboardArrowDown else Icons.Filled.Delete,
                            contentDescription = if (item.quantity > 1) "减少数量" else "删除商品",
                            tint = if (item.quantity > 1) MaterialTheme.colorScheme.primary else Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    // 数量显示 - 可点击进行精确输入
                    Surface(
                        onClick = { showQuantityInputDialog = true },
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.clickable { showQuantityInputDialog = true }
                    ) {
                        Text(
                            text = item.quantity.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    // 增加按钮
                    IconButton(
                        onClick = {
                            if (item.quantity < maxQuantity) {
                                onQuantityChange(item.quantity + 1)
                            }
                        },
                        enabled = item.quantity < maxQuantity,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "增加数量",
                            tint = if (item.quantity < maxQuantity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // 小计
                Text(
                    text = SalesOrderFormatter.formatCurrency(item.subtotal),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
    
    // 删除确认对话框
    if (showRemoveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirmDialog = false },
            title = {
                Text(
                    text = "确认删除",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("是否要从清单中移除该商品？\n\n${item.displayName}")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRemove()
                        showRemoveConfirmDialog = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("删除", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 数量精确输入对话框
    if (showQuantityInputDialog) {
        var inputQuantity by remember { mutableStateOf(item.quantity.toString()) }
        
        AlertDialog(
            onDismissRequest = { showQuantityInputDialog = false },
            title = {
                Text(
                    text = "输入数量",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = item.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "库存: $maxQuantity 件",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = inputQuantity,
                        onValueChange = { inputQuantity = it.filter { char -> char.isDigit() } },
                        label = { Text("数量") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newQuantity = inputQuantity.toIntOrNull()
                        if (newQuantity != null && newQuantity > 0 && newQuantity <= maxQuantity) {
                            onQuantityChange(newQuantity)
                            showQuantityInputDialog = false
                        }
                    },
                    enabled = {
                        val quantity = inputQuantity.toIntOrNull()
                        quantity != null && quantity > 0 && quantity <= maxQuantity
                    }()
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuantityInputDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}