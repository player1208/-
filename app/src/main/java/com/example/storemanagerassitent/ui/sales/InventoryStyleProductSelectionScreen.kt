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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun InventoryStyleProductSelectionScreen(
    onDismiss: () -> Unit,
    onSelectProduct: (Goods) -> Unit,
    onAddConfirmedItems: (List<SalesOrderItem>) -> Unit,
    getAllGoods: () -> List<Goods>
) {
    val goods = getAllGoods()
    val categories = SampleData.categories
    var selectedCategory by remember { mutableStateOf("all") }
    var searchText by remember { mutableStateOf("") }
    var pendingItems by remember { mutableStateOf<List<SalesOrderItem>>(emptyList()) }
    var showQuantityDialog by remember { mutableStateOf(false) }
    var selectedGoods by remember { mutableStateOf<Goods?>(null) }
    var quantityAmount by remember { mutableStateOf(1) }
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
                                if (pendingItems.isNotEmpty()) {
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
                                            quantityAmount = 1
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
                                        quantityAmount = 1
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
            itemCount = pendingItems.size, // 显示真实的商品数量
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
                        text = "添加商品",
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
                            // 添加到待确认清单
                            val newItem = SalesOrderItem(
                                goodsId = selectedGoods!!.id,
                                goodsName = selectedGoods!!.name,
                                specifications = selectedGoods!!.specifications,
                                unitPrice = selectedGoods!!.retailPrice,
                                quantity = quantityAmount,
                                category = selectedGoods!!.category
                            )
                            
                            pendingItems = pendingItems + newItem
                            
                            // 关闭对话框
                            showQuantityDialog = false
                            selectedGoods = null
                        },
                        enabled = quantityAmount > 0 && quantityAmount <= selectedGoods!!.stockQuantity
                    ) {
                        Text("确认添加")
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
                        text = "已选商品 (${pendingItems.size})",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    if (pendingItems.isEmpty()) {
                        Text("暂无商品，请先添加商品")
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(pendingItems) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "数量: ${item.quantity} | 单价: ${SalesOrderFormatter.formatCurrency(item.unitPrice)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                    TextButton(onClick = { 
                                        pendingItems = pendingItems.filter { it.id != item.id }
                                    }) {
                                        Text("删除", color = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    if (pendingItems.isNotEmpty()) {
                        Button(
                            onClick = {
                                onAddConfirmedItems(pendingItems)
                                showPendingListDialog = false
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text(
                                text = "完成添加 (${pendingItems.size})",
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
                selectedCount = pendingItems.size,
                onDismiss = { showExitConfirmDialog = false },
                onDiscardChanges = { 
                    showExitConfirmDialog = false
                    onDismiss() // 直接返回，不保存
                },
                onContinueSelection = { 
                    showExitConfirmDialog = false // 继续选择，留在当前页面
                },
                onSaveAndReturn = {
                    showExitConfirmDialog = false
                    onAddConfirmedItems(pendingItems) // 保存并返回
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