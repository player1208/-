package com.example.storemanagerassitent.ui.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.storemanagerassitent.data.DateFilterType
import com.example.storemanagerassitent.data.SalesOrder
import com.example.storemanagerassitent.data.SalesOrderFormatter
import com.example.storemanagerassitent.data.SalesRecordSummary

/**
 * 销售记录主页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesRecordScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SalesRecordViewModel = viewModel()
) {
    val dateFilterState by viewModel.dateFilterState.collectAsState()
    val salesRecords by viewModel.salesRecords.collectAsState()
    val showDatePicker by viewModel.showDatePicker.collectAsState()
    val showOrderDetails by viewModel.showOrderDetails.collectAsState()
    val selectedOrder by viewModel.selectedOrder.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val bottomSheetState = rememberModalBottomSheetState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "销售记录",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::showDatePicker) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "日期筛选"
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
            // 日期筛选器显示条
            DateFilterBar(
                dateFilterState = dateFilterState,
                onDateFilterClick = viewModel::showDatePicker,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            
            // 销售记录列表
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (salesRecords.isEmpty()) {
                EmptyRecordsView(
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(salesRecords) { record ->
                        SalesRecordCard(
                            record = record,
                            onClick = { viewModel.showOrderDetails(record.orderId) }
                        )
                    }
                }
            }
        }
        
        // 日期选择器对话框
        if (showDatePicker) {
            DatePickerDialog(
                onDismiss = viewModel::hideDatePicker,
                onSelectPreset = viewModel::selectPresetDateFilter,
                onSelectCustomDate = viewModel::selectCustomDate,
                onSelectCustomMonth = viewModel::selectCustomMonth,
                onSelectCustomYear = viewModel::selectCustomYear
            )
        }
        
        // 订单详情底部面板
        if (showOrderDetails && selectedOrder != null) {
            ModalBottomSheet(
                onDismissRequest = viewModel::hideOrderDetails,
                sheetState = bottomSheetState
            ) {
                OrderDetailsPanel(
                    order = selectedOrder!!,
                    onDismiss = viewModel::hideOrderDetails
                )
            }
        }
    }
}

/**
 * 日期筛选器显示条
 */
@Composable
fun DateFilterBar(
    dateFilterState: com.example.storemanagerassitent.data.DateFilterState,
    onDateFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onDateFilterClick() },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateFilterState.getDisplayText(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = "选择日期",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 销售记录卡片
 */
@Composable
fun SalesRecordCard(
    record: SalesRecordSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 商品概要
                Text(
                    text = record.itemsSummary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                // 日期时间
                Text(
                    text = record.formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                // 客户信息（如果有）
                if (record.customerName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "客户: ${record.customerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 右侧金额
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = record.formattedAmount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = "查看详情",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

/**
 * 空记录视图
 */
@Composable
fun EmptyRecordsView(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "该时间段内暂无销售记录",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "选择其他日期范围查看更多记录",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 日期选择器对话框
 */
@Composable
fun DatePickerDialog(
    onDismiss: () -> Unit,
    onSelectPreset: (DateFilterType) -> Unit,
    onSelectCustomDate: (java.util.Date) -> Unit,
    onSelectCustomMonth: (Int, Int) -> Unit,
    onSelectCustomYear: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择日期范围",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                val presetOptions = listOf(
                    DateFilterType.TODAY,
                    DateFilterType.THIS_WEEK,
                    DateFilterType.THIS_MONTH,
                    DateFilterType.THIS_YEAR
                )
                
                presetOptions.forEach { filterType ->
                    TextButton(
                        onClick = { onSelectPreset(filterType) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = filterType.displayName,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text(
                    text = "自定义范围",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // 这里可以添加更多自定义日期选择选项
                // 为了简化，暂时只提供预设选项
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 订单详情面板
 */
@Composable
fun OrderDetailsPanel(
    order: SalesOrder,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // 标题栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "订单详情",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 订单号与时间
        OrderDetailsSection(title = "订单信息") {
            DetailRow("订单号", order.id)
            DetailRow("下单时间", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.CHINA).format(java.util.Date(order.createdAt)))
        }
        
        // 客户信息
        if (order.customerName.isNotBlank() || order.customerPhone.isNotBlank() || order.customerAddress.isNotBlank()) {
            OrderDetailsSection(title = "客户信息") {
                if (order.customerName.isNotBlank()) {
                    DetailRow("客户姓名", order.customerName)
                }
                if (order.customerPhone.isNotBlank()) {
                    DetailRow("联系电话", order.customerPhone)
                }
                if (order.customerAddress.isNotBlank()) {
                    DetailRow("收货地址", order.customerAddress)
                }
            }
        }
        
        // 商品清单
        OrderDetailsSection(title = "商品清单") {
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${SalesOrderFormatter.formatCurrency(item.unitPrice)} × ${item.quantity}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        text = SalesOrderFormatter.formatCurrency(item.subtotal),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        // 支付详情
        OrderDetailsSection(title = "支付详情") {
            DetailRow("支付方式", order.paymentMethod.displayName)
            DetailRow("付款类型", order.paymentType?.displayName ?: "未知")
            if (order.paymentType?.name == "DEPOSIT") {
                DetailRow("定金金额", SalesOrderFormatter.formatCurrency(order.depositAmount))
                DetailRow("待付余额", SalesOrderFormatter.formatCurrency(order.remainingAmount))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            DetailRow(
                "合计金额", 
                SalesOrderFormatter.formatCurrency(order.totalAmount),
                isTotal = true
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun OrderDetailsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(8.dp))
    content()
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Medium,
            color = if (isTotal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
    if (!isTotal) {
        Spacer(modifier = Modifier.height(4.dp))
    }
}