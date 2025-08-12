package com.example.storemanagerassitent.ui.goods

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.storemanagerassitent.data.Goods
import com.example.storemanagerassitent.data.GoodsCategory
import com.example.storemanagerassitent.data.OutboundReason
import java.text.DecimalFormat

/**
 * 商品详情与操作面板 (Bottom Sheet)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoodsDetailBottomSheet(
    goods: Goods,
    categories: List<GoodsCategory>,
    onDismiss: () -> Unit,
    onInboundClick: () -> Unit,
    onOutboundClick: () -> Unit,
    onCategoryEditClick: () -> Unit,
    onStockEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 获取分类信息
    val category = categories.find { it.id == goods.category }
    val categoryColor = category?.colorHex?.let { colorHex ->
        try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: IllegalArgumentException) {
            Color.Blue
        }
    } ?: Color.Blue
    
    val priceFormat = DecimalFormat("¥#0.00")
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // 商品名称
            Text(
                text = goods.displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 核心数据
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // 库存行（带编辑图标）
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "库存",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${goods.stockQuantity} 件",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "编辑库存",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onStockEditClick() },
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    DataRow("进价", priceFormat.format(goods.purchasePrice))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 商品分类行（可点击）
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategoryEditClick() }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "所属分类",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 分类颜色圆点
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(categoryColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = category?.name ?: "未知分类",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "编辑分类",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 入库按钮
                Button(
                    onClick = onInboundClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("入库", fontWeight = FontWeight.Medium)
                }
                
                // 出库按钮
                Button(
                    onClick = onOutboundClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("出库", fontWeight = FontWeight.Medium)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 数据行组件
 */
@Composable
private fun DataRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 出库原因选择对话框
 */
@Composable
fun OutboundReasonDialog(
    onDismiss: () -> Unit,
    onReasonSelected: (OutboundReason) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "请选择出库原因",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutboundReason.values().forEach { reason ->
                    TextButton(
                        onClick = { onReasonSelected(reason) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = reason.displayName,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        modifier = modifier
    )
}

/**
 * 库存调整对话框
 */
@Composable
fun StockAdjustmentDialog(
    goods: Goods,
    outboundQuantity: Int,
    onDismiss: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    var quantityText by remember(outboundQuantity) { mutableStateOf(outboundQuantity.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "库存盘点错误调整",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column {
                Text(
                    text = "商品：${goods.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "出库数量:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 减少按钮
                    IconButton(
                        onClick = onDecrease,
                        enabled = outboundQuantity > 1
                    ) {
                        Text(
                            text = "−",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // 数量输入框
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { newValue ->
                            quantityText = newValue
                            newValue.toIntOrNull()?.let { quantity ->
                                if (quantity > 0 && quantity <= goods.stockQuantity) {
                                    onQuantityChange(quantity)
                                }
                            }
                        },
                        modifier = Modifier.width(80.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Center
                        )
                    )
                    
                    // 增加按钮
                    IconButton(
                        onClick = onIncrease,
                        enabled = outboundQuantity < goods.stockQuantity
                    ) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Text(
                    text = "当前库存: ${goods.stockQuantity} 件",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("确认出库")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        modifier = modifier
    )
}

/**
 * 最终确认对话框
 */
@Composable
fun FinalConfirmDialog(
    goods: Goods,
    outboundQuantity: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "请再次确认",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "您确定要将 ${goods.displayName} 的库存减少 $outboundQuantity 件吗？此操作无法撤销。",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("我再想想")
            }
        },
        modifier = modifier
    )
}

/**
 * 分类选择对话框
 */
@Composable
fun CategorySelectorDialog(
    categories: List<GoodsCategory>,
    currentCategoryId: String,
    onDismiss: () -> Unit,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "请选择新的分类",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn {
                items(categories.filter { it.id != "all" }) { category ->
                    CategorySelectItem(
                        category = category,
                        isSelected = category.id == currentCategoryId,
                        onClick = { onCategorySelected(category.id) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        modifier = modifier
    )
}

/**
 * 分类选择项组件
 */
@Composable
private fun CategorySelectItem(
    category: GoodsCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category.colorHex))
    } catch (e: IllegalArgumentException) {
        Color.Blue
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 分类颜色圆点
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(categoryColor, CircleShape)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 分类名称
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        // 选中标记
        if (isSelected) {
            Text(
                text = "✓",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 批量下架确认对话框
 */
@Composable
fun BatchDelistConfirmDialog(
    selectedCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "确认批量下架",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "您确定要下架选中的 $selectedCount 件商品吗？",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("再想想")
            }
        },
        modifier = modifier
    )
}

/**
 * 批量下架取消确认对话框
 */
@Composable
fun BatchCancelConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "取消批量下架",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "您确定要取消本次操作吗？所有选择都将丢失。",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("确认取消")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("继续选择")
            }
        },
        modifier = modifier
    )
}

/**
 * 销售数量输入对话框
 */
@Composable
fun SaleQuantityDialog(
    goods: Goods,
    quantity: Int,
    onDismiss: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priceFormat = DecimalFormat("¥#0.00")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "请输入销售数量",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 商品信息
                Text(
                    text = goods.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "库存: ${goods.stockQuantity} 件",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Text(
                    text = "单价: ${priceFormat.format(goods.retailPrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 数量调节器
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "数量:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    // 减少按钮
                    IconButton(
                        onClick = onDecrease,
                        enabled = quantity > 1,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (quantity > 1) 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            text = "−",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (quantity > 1) 
                                MaterialTheme.colorScheme.primary
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    
                    // 数量显示
                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // 增加按钮
                    IconButton(
                        onClick = onIncrease,
                        enabled = quantity < goods.stockQuantity,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (quantity < goods.stockQuantity) 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (quantity < goods.stockQuantity) 
                                MaterialTheme.colorScheme.primary
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 小计显示
                Text(
                    text = "小计: ${priceFormat.format(goods.retailPrice * quantity)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = quantity > 0 && quantity <= goods.stockQuantity,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    text = "去开单",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        modifier = modifier
    )
}

/**
 * 入库数量输入对话框
 */
@Composable
fun InboundQuantityDialog(
    goods: Goods,
    quantity: Int,
    onDismiss: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "入库数量",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = goods.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 数量选择器
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 减少按钮
                    IconButton(
                        onClick = onDecrease,
                        enabled = quantity > 1,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = if (quantity > 1) 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            text = "−",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (quantity > 1) 
                                MaterialTheme.colorScheme.primary
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    
                    // 数量显示
                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // 增加按钮
                    IconButton(
                        onClick = onIncrease,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 当前库存显示
                Text(
                    text = "当前库存: ${goods.stockQuantity} 件",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "确认入库",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        modifier = modifier
    )
}

/**
 * 库存编辑确认对话框
 */
@Composable
fun StockEditConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "库存编辑",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "是否因库存清点错误而重新编辑库存数量？",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "确认编辑",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        modifier = modifier
    )
}

/**
 * 库存编辑对话框
 */
@Composable
fun StockEditDialog(
    goods: Goods,
    quantity: Int,
    onDismiss: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "编辑库存数量",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = goods.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 数量选择器
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 减少按钮
                    IconButton(
                        onClick = onDecrease,
                        enabled = quantity > 0,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = if (quantity > 0) 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            text = "−",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (quantity > 0) 
                                MaterialTheme.colorScheme.primary
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    
                    // 数量显示
                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // 增加按钮
                    IconButton(
                        onClick = onIncrease,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 当前库存显示
                Text(
                    text = "当前库存: ${goods.stockQuantity} 件",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "确认更新",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        modifier = modifier
    )
}