package com.example.storemanagerassitent.ui.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.storemanagerassitent.data.Goods
import com.example.storemanagerassitent.data.PaymentMethod
import com.example.storemanagerassitent.data.PaymentType
import com.example.storemanagerassitent.data.SalesOrderFormatter
import com.example.storemanagerassitent.data.SalesOrderItem

/**
 * 销售订单主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesOrderScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SalesOrderViewModel = viewModel()
) {
    val salesOrderState by viewModel.salesOrderState.collectAsState()
    val showProductSelection by viewModel.showProductSelection.collectAsState()
    val showQuantityDialog by viewModel.showQuantityDialog.collectAsState()
    val selectedGoods by viewModel.selectedGoods.collectAsState()
    val quantityDialogAmount by viewModel.quantityDialogAmount.collectAsState()
    val showPriceEditDialog by viewModel.showPriceEditDialog.collectAsState()
    val editingOrderItem by viewModel.editingOrderItem.collectAsState()
    val priceEditAmount by viewModel.priceEditAmount.collectAsState()
    
    // 页面进入时重置UI状态
    LaunchedEffect(Unit) {
        viewModel.resetUIStates()
    }
    
    // 商品选择界面作为全屏覆盖
    if (showProductSelection) {
        InventoryStyleProductSelectionScreen(
            onDismiss = viewModel::hideProductSelection,
            onSelectProduct = viewModel::selectProduct,
            onAddConfirmedItems = viewModel::addConfirmedItems,
            getAllGoods = viewModel::getAllGoods
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "新建销售单",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
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
            // 顶部操作栏
            TopActionBar(
                onAddProduct = viewModel::showProductSelection,
                modifier = Modifier.padding(16.dp)
            )
            
            // 订单列表区
            OrderListArea(
                items = salesOrderState.items,
                onRemoveItem = viewModel::removeOrderItem,
                onUpdateQuantity = viewModel::updateOrderItemQuantity,
                onEditPrice = viewModel::showPriceEditDialog,
                modifier = Modifier.weight(1f)
            )
            
            // 结算信息区
            PaymentInfoArea(
                salesOrderState = salesOrderState,
                onSetPaymentMethod = viewModel::setPaymentMethod,
                onSetPaymentType = viewModel::setPaymentType,
                onSetDepositAmount = viewModel::setDepositAmount,
                onSetCustomerName = viewModel::setCustomerName,
                onSetCustomerPhone = viewModel::setCustomerPhone,
                onSetCustomerAddress = viewModel::setCustomerAddress,
                modifier = Modifier.padding(16.dp)
            )
            
            // 底部结算栏
            BottomSettlementBar(
                totalAmount = salesOrderState.totalAmount,
                canComplete = salesOrderState.canCompleteOrder,
                onComplete = viewModel::completeOrder,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // 数量确认对话框
        if (showQuantityDialog && selectedGoods != null) {
            QuantityConfirmDialog(
                goods = selectedGoods!!,
                quantity = quantityDialogAmount,
                onQuantityChange = viewModel::updateQuantity,
                onIncrease = viewModel::increaseQuantity,
                onDecrease = viewModel::decreaseQuantity,
                onConfirm = viewModel::confirmProductQuantity,
                onDismiss = viewModel::cancelProductQuantity
            )
        }
        
        // 价格编辑对话框
        if (showPriceEditDialog && editingOrderItem != null) {
            PriceEditDialog(
                orderItem = editingOrderItem!!,
                price = priceEditAmount,
                onPriceChange = viewModel::updatePriceEditAmount,
                onConfirm = viewModel::confirmPriceEdit,
                onDismiss = viewModel::hidePriceEditDialog
            )
        }
        }
    }
}

/**
 * 顶部操作栏
 */
@Composable
fun TopActionBar(
    onAddProduct: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onAddProduct,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "添加商品",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "添加商品",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 订单列表区
 */
@Composable
fun OrderListArea(
    items: List<SalesOrderItem>,
    onRemoveItem: (String) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onEditPrice: (SalesOrderItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "请点击上方按钮添加商品",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                OrderItemCard(
                    item = item,
                    onRemove = { onRemoveItem(item.id) },
                    onUpdateQuantity = { quantity -> onUpdateQuantity(item.id, quantity) },
                    onEditPrice = { onEditPrice(item) }
                )
            }
        }
    }
}

/**
 * 订单项卡片
 */
@Composable
fun OrderItemCard(
    item: SalesOrderItem,
    onRemove: () -> Unit,
    onUpdateQuantity: (Int) -> Unit,
    onEditPrice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 商品名称和删除按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "* ${item.displayName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 价格和数量行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧：价格
                Column {
                    Text(
                        text = "单价: ${SalesOrderFormatter.formatCurrency(item.unitPrice)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable { onEditPrice() }
                    )
                    Text(
                        text = "小计: ${SalesOrderFormatter.formatCurrency(item.subtotal)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // 右侧：数量调节器
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "* 数量: ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // 数量调节器
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        IconButton(
                            onClick = { if (item.quantity > 1) onUpdateQuantity(item.quantity - 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text(
                                text = "−",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = item.quantity.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        
                        IconButton(
                            onClick = { onUpdateQuantity(item.quantity + 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text(
                                text = "+",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 结算信息区
 */
@Composable
fun PaymentInfoArea(
    salesOrderState: com.example.storemanagerassitent.data.SalesOrderState,
    onSetPaymentMethod: (PaymentMethod) -> Unit,
    onSetPaymentType: (PaymentType) -> Unit,
    onSetDepositAmount: (Double) -> Unit,
    onSetCustomerName: (String) -> Unit,
    onSetCustomerPhone: (String) -> Unit,
    onSetCustomerAddress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 支付方式
            Column {
                Text(
                    text = "* 支付方式",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMethod.values().forEach { method ->
                        FilterChip(
                            selected = salesOrderState.paymentMethod == method,
                            onClick = { onSetPaymentMethod(method) },
                            label = { Text(method.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
            
            Divider()
            
            // 付款类型 (Radio Buttons)
            Column {
                Text(
                    text = "付款类型",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row {
                    PaymentType.values().forEach { type ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = salesOrderState.paymentType == type,
                                onClick = { onSetPaymentType(type) }
                            )
                            Text(
                                text = type.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                if (salesOrderState.paymentType == PaymentType.DEPOSIT) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = salesOrderState.depositAmount.toString(),
                        onValueChange = { value ->
                            val amount = value.toDoubleOrNull() ?: 0.0
                            onSetDepositAmount(amount)
                        },
                        label = { Text("定金金额") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Divider()
            
            // 客户信息
            Column {
                Text(
                    text = "客户信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = salesOrderState.customerName,
                    onValueChange = onSetCustomerName,
                    label = { Text("客户姓名") },
                    placeholder = { Text("选填") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = salesOrderState.customerPhone,
                    onValueChange = onSetCustomerPhone,
                    label = { Text("联系电话") },
                    placeholder = { Text("选填") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = salesOrderState.customerAddress,
                    onValueChange = onSetCustomerAddress,
                    label = { Text("收货地址") },
                    placeholder = { Text("选填") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 底部结算栏
 */
@Composable
fun BottomSettlementBar(
    totalAmount: Double,
    canComplete: Boolean,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：合计金额
            Column {
                Text(
                    text = "合计:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = SalesOrderFormatter.formatCurrency(totalAmount),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 右侧：收款按钮
            Button(
                onClick = onComplete,
                enabled = canComplete,
                modifier = Modifier
                    .height(48.dp)
                    .width(120.dp),
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = if (canComplete) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            ) {
                Text(
                    text = "收款",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (canComplete) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    }
} 