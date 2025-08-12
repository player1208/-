package com.example.storemanagerassitent.ui.goods

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler
import com.example.storemanagerassitent.data.Goods
import com.example.storemanagerassitent.data.GoodsCategory
import com.example.storemanagerassitent.data.SampleData
import com.example.storemanagerassitent.data.SortOption
import com.example.storemanagerassitent.ui.components.EmptyStates
import com.example.storemanagerassitent.ui.components.InlineLoading
import com.example.storemanagerassitent.ui.components.ErrorState
import com.example.storemanagerassitent.ui.theme.StoreManagerAssitentTheme
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoodsManagementScreen(
    modifier: Modifier = Modifier,
    onNavigateToCategoryManagement: () -> Unit = {},
    onNavigateToSalesOrder: (Goods, Int) -> Unit = { _, _ -> },
    viewModel: GoodsManagementViewModel = viewModel()
) {
    // 收集ViewModel中的状态
    val categories by viewModel.categories.collectAsState()
    val filteredGoods by viewModel.filteredGoods.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isSearchExpanded by viewModel.isSearchExpanded.collectAsState()
    val selectedSortOption by viewModel.selectedSortOption.collectAsState()
    val showSortMenu by viewModel.showSortMenu.collectAsState()
    
    // 详情面板和对话框状态
    val selectedGoods by viewModel.selectedGoods.collectAsState()
    val showGoodsDetail by viewModel.showGoodsDetail.collectAsState()

    val showStockAdjustmentDialog by viewModel.showStockAdjustmentDialog.collectAsState()
    val showFinalConfirmDialog by viewModel.showFinalConfirmDialog.collectAsState()
    val outboundQuantity by viewModel.outboundQuantity.collectAsState()
    val showSaleQuantityDialog by viewModel.showSaleQuantityDialog.collectAsState()
    val saleQuantity by viewModel.saleQuantity.collectAsState()
    
    // 入库和库存编辑相关状态
    val showInboundQuantityDialog by viewModel.showInboundQuantityDialog.collectAsState()
    val inboundQuantity by viewModel.inboundQuantity.collectAsState()
    val showStockEditConfirmDialog by viewModel.showStockEditConfirmDialog.collectAsState()
    val showStockEditDialog by viewModel.showStockEditDialog.collectAsState()
    val stockEditQuantity by viewModel.stockEditQuantity.collectAsState()
    
    // 批量下架相关状态
    val isBatchDelistMode by viewModel.isBatchDelistMode.collectAsState()
    val selectedGoodsIds by viewModel.selectedGoodsIds.collectAsState()
    val problemGoodsIds by viewModel.problemGoodsIds.collectAsState()
    val showBatchDelistConfirmDialog by viewModel.showBatchDelistConfirmDialog.collectAsState()
    val showBatchCancelConfirmDialog by viewModel.showBatchCancelConfirmDialog.collectAsState()
    val showMoreOptionsMenu by viewModel.showMoreOptionsMenu.collectAsState()
    
    // 分类修改相关状态
    val showCategorySelector by viewModel.showCategorySelector.collectAsState()
    
    // 按分类分组
    val groupedGoods = remember(filteredGoods) {
        viewModel.getGroupedGoods(filteredGoods)
    }
    
    // 低库存商品数量
    val lowStockCount = remember(filteredGoods) {
        viewModel.getLowStockCount(filteredGoods)
    }
    
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current

    // 当搜索处于展开状态时，拦截系统返回键以先收起搜索
    BackHandler(enabled = isSearchExpanded) {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    // 规则二：键盘收起后搜索框联动收起（基于 WindowInsetsCompat）
    val rootView = LocalView.current.rootView
    DisposableEffect(isSearchExpanded) {
        if (!isSearchExpanded) return@DisposableEffect onDispose { }
        var hasShownKeyboard = false
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (imeVisible) {
                hasShownKeyboard = true
            } else if (hasShownKeyboard) {
                focusManager.clearFocus()
                viewModel.toggleSearchExpanded()
            }
            insets
        }
        rootView.requestApplyInsets()
        onDispose {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, null)
        }
    }

    Scaffold(
        topBar = {
            if (isBatchDelistMode) {
                BatchDelistTopAppBar(
                    selectedCount = selectedGoodsIds.size,
                    onCancel = viewModel::showBatchCancelConfirmDialog,
                    onConfirm = viewModel::showBatchDelistConfirmDialog
                )
            } else {
                GoodsTopAppBar(
                    isSearchExpanded = isSearchExpanded,
                    searchText = searchText,
                    onSearchTextChange = viewModel::updateSearchText,
                    onSearchToggle = viewModel::toggleSearchExpanded,
                    showSortMenu = showSortMenu,
                    selectedSortOption = selectedSortOption,
                    onSortMenuToggle = viewModel::toggleSortMenu,
                    onSortOptionSelected = viewModel::selectSortOption,
                    lowStockCount = lowStockCount,
                    showMoreOptionsMenu = showMoreOptionsMenu,
                    onMoreOptionsToggle = viewModel::toggleMoreOptionsMenu,
                    onBatchDelistClick = viewModel::enterBatchDelistMode,
                    onNavigateToCategoryManagement = onNavigateToCategoryManagement
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 分类筛选栏
                CategoryFilterBar(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = viewModel::selectCategory
                )

                // 商品列表
                Box(modifier = Modifier.fillMaxSize()) {
                    GoodsList(
                        groupedGoods = groupedGoods,
                        categories = categories,
                        isBatchDelistMode = isBatchDelistMode,
                        selectedGoodsIds = selectedGoodsIds,
                        problemGoodsIds = problemGoodsIds,
                        onGoodsClick = if (isBatchDelistMode) null else viewModel::selectGoods,
                        onGoodsToggleSelection = if (isBatchDelistMode) viewModel::toggleGoodsSelection else null,
                        onAddGoods = {
                            // TODO: 导航到添加商品页面
                            // 现在先显示一个提示
                            // GlobalMessageManager.showInfo("添加商品功能开发中")
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // 底部信息条（批量模式时显示）
                    if (isBatchDelistMode) {
                        BottomInfoBar(
                            selectedCount = selectedGoodsIds.size,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }

            // 点击内容区域空白处自动收起搜索
            if (isSearchExpanded) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                )
            }
        }
        
        // 详情面板
        selectedGoods?.let { goods ->
            if (showGoodsDetail) {
                GoodsDetailBottomSheet(
                    goods = goods,
                    categories = categories,
                    onDismiss = viewModel::closeGoodsDetail,
                    onInboundClick = viewModel::showInboundQuantityDialog,
                    onOutboundClick = viewModel::showOutboundReasonDialog,
                    onCategoryEditClick = viewModel::showCategorySelector,
                    onStockEditClick = viewModel::showStockEditConfirmDialog
                )
            }
        }
        

        
        // 库存调整对话框
        selectedGoods?.let { goods ->
            if (showStockAdjustmentDialog) {
                StockAdjustmentDialog(
                    goods = goods,
                    outboundQuantity = outboundQuantity,
                    onDismiss = viewModel::hideStockAdjustmentDialog,
                    onQuantityChange = viewModel::updateOutboundQuantity,
                    onIncrease = viewModel::increaseOutboundQuantity,
                    onDecrease = viewModel::decreaseOutboundQuantity,
                    onConfirm = viewModel::confirmOutbound
                )
            }
        }
        
        // 最终确认对话框
        selectedGoods?.let { goods ->
            if (showFinalConfirmDialog) {
                FinalConfirmDialog(
                    goods = goods,
                    outboundQuantity = outboundQuantity,
                    onDismiss = viewModel::hideFinalConfirmDialog,
                    onConfirm = viewModel::executeOutbound
                )
            }
        }
        
        // 销售数量输入对话框
        selectedGoods?.let { goods ->
            if (showSaleQuantityDialog) {
                SaleQuantityDialog(
                    goods = goods,
                    quantity = saleQuantity,
                    onDismiss = viewModel::hideSaleQuantityDialog,
                    onQuantityChange = viewModel::updateSaleQuantity,
                    onIncrease = viewModel::increaseSaleQuantity,
                    onDecrease = viewModel::decreaseSaleQuantity,
                    onConfirm = {
                        viewModel.confirmSaleAndNavigate(onNavigateToSalesOrder)
                    }
                )
            }
        }
        
        // 批量下架确认对话框
        if (showBatchDelistConfirmDialog) {
            BatchDelistConfirmDialog(
                selectedCount = selectedGoodsIds.size,
                onDismiss = viewModel::hideBatchDelistConfirmDialog,
                onConfirm = viewModel::executeBatchDelist
            )
        }
        
        // 批量下架取消确认对话框
        if (showBatchCancelConfirmDialog) {
            BatchCancelConfirmDialog(
                onDismiss = viewModel::hideBatchCancelConfirmDialog,
                onConfirm = viewModel::confirmCancelBatchDelist
            )
        }
        
        // 分类选择对话框
        selectedGoods?.let { goods ->
            if (showCategorySelector) {
                CategorySelectorDialog(
                    categories = categories,
                    currentCategoryId = goods.category,
                    onDismiss = viewModel::hideCategorySelector,
                    onCategorySelected = { newCategoryId ->
                        viewModel.updateGoodsCategory(goods.id, newCategoryId)
                    }
                )
            }
        }
        
        // 入库数量输入对话框
        selectedGoods?.let { goods ->
            if (showInboundQuantityDialog) {
                InboundQuantityDialog(
                    goods = goods,
                    quantity = inboundQuantity,
                    onDismiss = viewModel::hideInboundQuantityDialog,
                    onQuantityChange = viewModel::updateInboundQuantity,
                    onIncrease = viewModel::increaseInboundQuantity,
                    onDecrease = viewModel::decreaseInboundQuantity,
                    onConfirm = viewModel::confirmInbound
                )
            }
        }
        
        // 库存编辑确认对话框
        if (showStockEditConfirmDialog) {
            StockEditConfirmDialog(
                onDismiss = viewModel::hideStockEditConfirmDialog,
                onConfirm = viewModel::showStockEditDialog
            )
        }
        
        // 库存编辑对话框
        selectedGoods?.let { goods ->
            if (showStockEditDialog) {
                StockEditDialog(
                    goods = goods,
                    quantity = stockEditQuantity,
                    onDismiss = viewModel::hideStockEditDialog,
                    onQuantityChange = viewModel::updateStockEditQuantity,
                    onIncrease = viewModel::increaseStockEditQuantity,
                    onDecrease = viewModel::decreaseStockEditQuantity,
                    onConfirm = viewModel::confirmStockEdit
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoodsTopAppBar(
    isSearchExpanded: Boolean,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    showSortMenu: Boolean,
    selectedSortOption: SortOption,
    onSortMenuToggle: () -> Unit,
    onSortOptionSelected: (SortOption) -> Unit,
    lowStockCount: Int = 0,
    showMoreOptionsMenu: Boolean = false,
    onMoreOptionsToggle: () -> Unit = {},
    onBatchDelistClick: () -> Unit = {},
    onNavigateToCategoryManagement: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            val focusManager = LocalFocusManager.current
            val keyboardController = LocalSoftwareKeyboardController.current

            if (isSearchExpanded) {
                val focusRequester = remember { FocusRequester() }
                TextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    placeholder = { Text("搜索商品名称、规格...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { state ->
                            if (isSearchExpanded && !state.isFocused) {
                                keyboardController?.hide()
                            }
                        },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                // 确保在布局后请求焦点
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "我的货物",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (lowStockCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                text = lowStockCount.toString(),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        },
        actions = {
            // 搜索图标
            IconButton(onClick = onSearchToggle) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "搜索"
                )
            }
            
            // 排序图标和菜单
            Box {
                IconButton(onClick = onSortMenuToggle) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "排序"
                    )
                }
                
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { onSortMenuToggle() }
                ) {
                    SortOption.values().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.displayName) },
                            onClick = { onSortOptionSelected(option) },
                            leadingIcon = if (option == selectedSortOption) {
                                { Text("✓", color = MaterialTheme.colorScheme.primary) }
                            } else null
                        )
                    }
                }
            }
            
            // 更多操作图标和菜单
            Box {
                IconButton(onClick = onMoreOptionsToggle) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "更多操作"
                    )
                }
                
                DropdownMenu(
                    expanded = showMoreOptionsMenu,
                    onDismissRequest = { onMoreOptionsToggle() }
                ) {
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = "商品批量下架",
                                style = MaterialTheme.typography.bodySmall
                            ) 
                        },
                        onClick = onBatchDelistClick
                    )
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = "管理分类",
                                style = MaterialTheme.typography.bodySmall
                            ) 
                        },
                        onClick = onNavigateToCategoryManagement
                    )
                }
            }
        },
        modifier = modifier
    )
}

@Composable
fun CategoryFilterBar(
    categories: List<GoodsCategory>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            CategoryChip(
                category = category,
                isSelected = category.id == selectedCategory,
                onClick = { onCategorySelected(category.id) }
            )
        }
    }
}

@Composable
fun CategoryChip(
    category: GoodsCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isSelected) 0.dp else 4.dp
    ) {
        Text(
            text = category.name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) {
                Color.White
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDelistTopAppBar(
    selectedCount: Int,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = "选择要下架的商品",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
        },
        navigationIcon = {
            TextButton(onClick = onCancel) {
                Text("取消")
            }
        },
        actions = {
            TextButton(
                onClick = onConfirm,
                enabled = selectedCount > 0
            ) {
                Text(
                    text = "确认下架",
                    color = if (selectedCount > 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
            }
        },
        modifier = modifier
    )
}

@Composable
fun BottomInfoBar(
    selectedCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 8.dp
    ) {
        Text(
            text = "已选择 $selectedCount 件商品",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun GoodsList(
    groupedGoods: Map<String, List<Goods>>,
    categories: List<GoodsCategory>,
    isBatchDelistMode: Boolean = false,
    selectedGoodsIds: Set<String> = emptySet(),
    problemGoodsIds: Set<String> = emptySet(),
    onGoodsClick: ((Goods) -> Unit)? = null,
    onGoodsToggleSelection: ((String) -> Unit)? = null,
    onAddGoods: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (groupedGoods.isEmpty()) {
        // 显示空状态
        EmptyStates.NoGoods(
            onAddGoods = onAddGoods ?: {}
        )
    } else {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(0.dp)
        ) {
            groupedGoods.forEach { (categoryName, goodsList) ->
                // 退回普通标题（不使用 stickyHeader 以避免依赖)
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.background,
                        shadowElevation = 0.dp
                    ) {
                        CategoryHeader(categoryName = categoryName)
                    }
                }

                // 分类下的商品
                items(goodsList, key = { it.id }) { goods ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        GoodsCard(
                            goods = goods,
                            categories = categories,
                            isBatchDelistMode = isBatchDelistMode,
                            isSelected = selectedGoodsIds.contains(goods.id),
                            isProblem = problemGoodsIds.contains(goods.id),
                            onGoodsClick = onGoodsClick,
                            onToggleSelection = onGoodsToggleSelection
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun CategoryHeader(
    categoryName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = categoryName,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun GoodsCard(
    goods: Goods,
    categories: List<GoodsCategory>,
    isBatchDelistMode: Boolean = false,
    isSelected: Boolean = false,
    isProblem: Boolean = false,
    onGoodsClick: ((Goods) -> Unit)? = null,
    onToggleSelection: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // 获取分类颜色
    val categoryColor = remember(goods.category, categories) {
        categories.find { it.id == goods.category }?.colorHex?.let { colorHex ->
            try {
                Color(android.graphics.Color.parseColor(colorHex))
            } catch (e: IllegalArgumentException) {
                Color.Blue
            }
        } ?: Color.Blue
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { 
                if (isBatchDelistMode) {
                    onToggleSelection?.invoke(goods.id)
                } else {
                    onGoodsClick?.invoke(goods)
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isProblem -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                isSelected && isBatchDelistMode -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：复选框（批量模式）或分类颜色竖条
            if (isBatchDelistMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection?.invoke(goods.id) }
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .background(
                            color = categoryColor,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 商品信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = goods.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 库存信息
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Surface(
                    color = if (goods.isLowStock) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "库存: ${goods.stockQuantity} 件",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (goods.isLowStock) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (goods.isLowStock) {
                    Text(
                        text = "库存紧张!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GoodsManagementScreenPreview() {
    StoreManagerAssitentTheme {
        GoodsManagementScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun GoodsCardPreview() {
    StoreManagerAssitentTheme {
        GoodsCard(
            goods = Goods(
                id = "1",
                name = "九牧王单孔冷热龙头",
                category = "bathroom",
                specifications = "J-1022",
                stockQuantity = 3,
                lowStockThreshold = 5,
                purchasePrice = 85.0
            ),
            categories = SampleData.categories,
            onGoodsClick = {}
        )
    }
}