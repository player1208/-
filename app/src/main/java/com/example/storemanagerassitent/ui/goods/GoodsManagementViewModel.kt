package com.example.storemanagerassitent.ui.goods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storemanagerassitent.data.Goods
import com.example.storemanagerassitent.data.GoodsCategory
import com.example.storemanagerassitent.data.SampleData
import com.example.storemanagerassitent.data.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * 商品管理页面的ViewModel
 * 管理搜索、筛选、排序等状态和逻辑
 */
class GoodsManagementViewModel : ViewModel() {
    
    // 原始商品数据
    private val _allGoods = MutableStateFlow(SampleData.goods)
    val allGoods: StateFlow<List<Goods>> = _allGoods.asStateFlow()
    
    // 分类数据
    private val _categories = MutableStateFlow(SampleData.categories)
    val categories: StateFlow<List<GoodsCategory>> = _categories.asStateFlow()
    
    // 搜索文本
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    
    // 选中的分类
    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()
    
    // 排序选项
    private val _selectedSortOption = MutableStateFlow(SortOption.STOCK_ASC)
    val selectedSortOption: StateFlow<SortOption> = _selectedSortOption.asStateFlow()
    
    // 搜索栏是否展开
    private val _isSearchExpanded = MutableStateFlow(false)
    val isSearchExpanded: StateFlow<Boolean> = _isSearchExpanded.asStateFlow()
    
    // 排序菜单是否显示
    private val _showSortMenu = MutableStateFlow(false)
    val showSortMenu: StateFlow<Boolean> = _showSortMenu.asStateFlow()
    
    // 过滤后的商品列表
    val filteredGoods: StateFlow<List<Goods>> = combine(
        _allGoods,
        _searchText,
        _selectedCategory,
        _selectedSortOption
    ) { goods, search, category, sort ->
        filterAndSortGoods(goods, search, category, sort)
    }.let { flow ->
        val stateFlow = MutableStateFlow<List<Goods>>(emptyList())
        viewModelScope.launch {
            flow.collect { stateFlow.value = it }
        }
        stateFlow.asStateFlow()
    }
    
    /**
     * 更新搜索文本
     */
    fun updateSearchText(text: String) {
        _searchText.value = text
    }
    
    /**
     * 切换搜索栏展开状态
     */
    fun toggleSearchExpanded() {
        _isSearchExpanded.value = !_isSearchExpanded.value
        // 如果收起搜索栏，清空搜索文本
        if (!_isSearchExpanded.value) {
            _searchText.value = ""
        }
    }
    
    /**
     * 选择分类
     */
    fun selectCategory(categoryId: String) {
        _selectedCategory.value = categoryId
    }
    
    /**
     * 选择排序选项
     */
    fun selectSortOption(option: SortOption) {
        _selectedSortOption.value = option
        _showSortMenu.value = false
    }
    
    /**
     * 切换排序菜单显示状态
     */
    fun toggleSortMenu() {
        _showSortMenu.value = !_showSortMenu.value
    }
    
    /**
     * 过滤和排序商品
     */
    private fun filterAndSortGoods(
        goods: List<Goods>,
        searchText: String,
        selectedCategory: String,
        sortOption: SortOption
    ): List<Goods> {
        // 分类筛选
        val filteredByCategory = if (selectedCategory == "all") {
            goods
        } else {
            goods.filter { it.category == selectedCategory }
        }
        
        // 搜索筛选
        val filteredBySearch = if (searchText.isEmpty()) {
            filteredByCategory
        } else {
            filteredByCategory.filter { 
                it.name.contains(searchText, ignoreCase = true) || 
                it.specifications.contains(searchText, ignoreCase = true)
            }
        }
        
        // 排序
        return when (sortOption) {
            SortOption.STOCK_ASC -> filteredBySearch.sortedBy { it.stockQuantity }
            SortOption.NAME_A_Z -> filteredBySearch.sortedBy { it.name }
            SortOption.LAST_UPDATED -> filteredBySearch.sortedByDescending { it.lastUpdated }
        }
    }
    
    /**
     * 按分类分组商品
     */
    fun getGroupedGoods(goods: List<Goods>): Map<String, List<Goods>> {
        return goods.groupBy { goodsItem ->
            _categories.value.find { it.id == goodsItem.category }?.name ?: "未知分类"
        }
    }
    
    /**
     * 获取低库存商品数量
     */
    fun getLowStockCount(goods: List<Goods>): Int {
        return goods.count { it.isLowStock }
    }
}