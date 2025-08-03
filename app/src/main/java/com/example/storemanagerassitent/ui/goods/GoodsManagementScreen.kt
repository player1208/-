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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.storemanagerassitent.data.Goods
import com.example.storemanagerassitent.data.GoodsCategory
import com.example.storemanagerassitent.data.SampleData
import com.example.storemanagerassitent.data.SortOption
import com.example.storemanagerassitent.ui.theme.StoreManagerAssitentTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoodsManagementScreen(
    modifier: Modifier = Modifier,
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
    
    // 按分类分组
    val groupedGoods = remember(filteredGoods) {
        viewModel.getGroupedGoods(filteredGoods)
    }
    
    // 低库存商品数量
    val lowStockCount = remember(filteredGoods) {
        viewModel.getLowStockCount(filteredGoods)
    }
    
    Scaffold(
        topBar = {
            GoodsTopAppBar(
                isSearchExpanded = isSearchExpanded,
                searchText = searchText,
                onSearchTextChange = viewModel::updateSearchText,
                onSearchToggle = viewModel::toggleSearchExpanded,
                showSortMenu = showSortMenu,
                selectedSortOption = selectedSortOption,
                onSortMenuToggle = viewModel::toggleSortMenu,
                onSortOptionSelected = viewModel::selectSortOption,
                lowStockCount = lowStockCount
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: 导航到添加商品页面 */ },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "添加商品",
                    tint = Color.White
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 分类筛选栏
            CategoryFilterBar(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = viewModel::selectCategory
            )
            
            // 商品列表
            GoodsList(
                groupedGoods = groupedGoods,
                modifier = Modifier.fillMaxSize()
            )
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
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            if (isSearchExpanded) {
                TextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    placeholder = { Text("搜索商品名称、规格...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
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
                        imageVector = Icons.Filled.MoreVert,
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

@Composable
fun GoodsList(
    groupedGoods: Map<String, List<Goods>>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedGoods.forEach { (categoryName, goodsList) ->
            // 分类标题（粘性标题效果需要额外实现）
            item {
                CategoryHeader(categoryName = categoryName)
            }
            
            // 分类下的商品
            items(goodsList) { goods ->
                GoodsCard(goods = goods)
            }
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { /* TODO: 点击商品卡片的处理 */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 商品图片占位符
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = goods.name.take(2),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 商品信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = goods.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = goods.specifications,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    shape = RoundedCornerShape(8.dp)
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
                specifications = "型号: J-1022",
                stockQuantity = 3,
                lowStockThreshold = 5
            )
        )
    }
}