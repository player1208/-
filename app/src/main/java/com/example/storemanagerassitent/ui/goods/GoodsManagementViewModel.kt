package com.example.storemanagerassitent.ui.goods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storemanagerassitent.data.Goods
import com.example.storemanagerassitent.data.GoodsCategory
import com.example.storemanagerassitent.data.OutboundReason
import com.example.storemanagerassitent.data.SampleData
import com.example.storemanagerassitent.data.SortOption
import com.example.storemanagerassitent.ui.components.GlobalSuccessMessage
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
    
    // 选中的商品 (详情面板)
    private val _selectedGoods = MutableStateFlow<Goods?>(null)
    val selectedGoods: StateFlow<Goods?> = _selectedGoods.asStateFlow()
    
    // 详情面板是否显示
    private val _showGoodsDetail = MutableStateFlow(false)
    val showGoodsDetail: StateFlow<Boolean> = _showGoodsDetail.asStateFlow()
    
    // 出库原因选择对话框是否显示
    private val _showOutboundReasonDialog = MutableStateFlow(false)
    val showOutboundReasonDialog: StateFlow<Boolean> = _showOutboundReasonDialog.asStateFlow()
    
    // 库存调整对话框是否显示
    private val _showStockAdjustmentDialog = MutableStateFlow(false)
    val showStockAdjustmentDialog: StateFlow<Boolean> = _showStockAdjustmentDialog.asStateFlow()
    
    // 最终确认对话框是否显示
    private val _showFinalConfirmDialog = MutableStateFlow(false)
    val showFinalConfirmDialog: StateFlow<Boolean> = _showFinalConfirmDialog.asStateFlow()
    
    // 出库数量
    private val _outboundQuantity = MutableStateFlow(1)
    val outboundQuantity: StateFlow<Int> = _outboundQuantity.asStateFlow()
    
    // 批量下架模式相关状态
    private val _isBatchDelistMode = MutableStateFlow(false)
    val isBatchDelistMode: StateFlow<Boolean> = _isBatchDelistMode.asStateFlow()
    
    // 选中的商品ID列表
    private val _selectedGoodsIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedGoodsIds: StateFlow<Set<String>> = _selectedGoodsIds.asStateFlow()
    
    // 问题商品ID列表（库存不为0无法下架的商品）
    private val _problemGoodsIds = MutableStateFlow<Set<String>>(emptySet())
    val problemGoodsIds: StateFlow<Set<String>> = _problemGoodsIds.asStateFlow()
    
    // 批量下架确认对话框
    private val _showBatchDelistConfirmDialog = MutableStateFlow(false)
    val showBatchDelistConfirmDialog: StateFlow<Boolean> = _showBatchDelistConfirmDialog.asStateFlow()
    
    // 批量下架取消确认对话框
    private val _showBatchCancelConfirmDialog = MutableStateFlow(false)
    val showBatchCancelConfirmDialog: StateFlow<Boolean> = _showBatchCancelConfirmDialog.asStateFlow()
    
    // 更多操作菜单是否显示
    private val _showMoreOptionsMenu = MutableStateFlow(false)
    val showMoreOptionsMenu: StateFlow<Boolean> = _showMoreOptionsMenu.asStateFlow()
    
    // 分类选择对话框是否显示
    private val _showCategorySelector = MutableStateFlow(false)
    val showCategorySelector: StateFlow<Boolean> = _showCategorySelector.asStateFlow()
    
    // 过滤后的商品列表（排除已下架商品）
    val filteredGoods: StateFlow<List<Goods>> = combine(
        _allGoods,
        _searchText,
        _selectedCategory,
        _selectedSortOption
    ) { goods, search, category, sort ->
        val availableGoods = goods.filter { !it.isDelisted }
        filterAndSortGoods(availableGoods, search, category, sort)
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
    
    /**
     * 选择商品 (显示详情面板)
     */
    fun selectGoods(goods: Goods) {
        _selectedGoods.value = goods
        _showGoodsDetail.value = true
    }
    
    /**
     * 关闭详情面板
     */
    fun closeGoodsDetail() {
        _showGoodsDetail.value = false
        _selectedGoods.value = null
    }
    
    /**
     * 显示出库原因选择对话框
     */
    fun showOutboundReasonDialog() {
        _showOutboundReasonDialog.value = true
    }
    
    /**
     * 隐藏出库原因选择对话框
     */
    fun hideOutboundReasonDialog() {
        _showOutboundReasonDialog.value = false
    }
    
    /**
     * 选择出库原因
     */
    fun selectOutboundReason(reason: OutboundReason) {
        _showOutboundReasonDialog.value = false
        when (reason) {
            OutboundReason.SOLD -> {
                // TODO: 未来实现售出功能
            }
            OutboundReason.INVENTORY_ERROR -> {
                _showStockAdjustmentDialog.value = true
            }
        }
    }
    
    /**
     * 显示库存调整对话框
     */
    fun showStockAdjustmentDialog() {
        _showStockAdjustmentDialog.value = true
        _outboundQuantity.value = 1
    }
    
    /**
     * 隐藏库存调整对话框
     */
    fun hideStockAdjustmentDialog() {
        _showStockAdjustmentDialog.value = false
    }
    
    /**
     * 更新出库数量
     */
    fun updateOutboundQuantity(quantity: Int) {
        if (quantity > 0) {
            _outboundQuantity.value = quantity
        }
    }
    
    /**
     * 增加出库数量
     */
    fun increaseOutboundQuantity() {
        _selectedGoods.value?.let { goods ->
            if (_outboundQuantity.value < goods.stockQuantity) {
                _outboundQuantity.value += 1
            }
        }
    }
    
    /**
     * 减少出库数量
     */
    fun decreaseOutboundQuantity() {
        if (_outboundQuantity.value > 1) {
            _outboundQuantity.value -= 1
        }
    }
    
    /**
     * 确认出库 (显示最终确认对话框)
     */
    fun confirmOutbound() {
        _showStockAdjustmentDialog.value = false
        _showFinalConfirmDialog.value = true
    }
    
    /**
     * 隐藏最终确认对话框
     */
    fun hideFinalConfirmDialog() {
        _showFinalConfirmDialog.value = false
    }
    
    /**
     * 执行出库操作
     */
    fun executeOutbound() {
        _selectedGoods.value?.let { goods ->
            val updatedGoods = goods.copy(
                stockQuantity = goods.stockQuantity - _outboundQuantity.value
            )
            
            // 更新商品列表
            val updatedGoodsList = _allGoods.value.map { item ->
                if (item.id == goods.id) updatedGoods else item
            }
            _allGoods.value = updatedGoodsList
            
            // 更新选中的商品
            _selectedGoods.value = updatedGoods
        }
        
        _showFinalConfirmDialog.value = false
        _outboundQuantity.value = 1
        
        // 显示成功提示
        GlobalSuccessMessage.showSuccess("库存已更正")
    }
    
    // === 批量下架相关方法 ===
    
    /**
     * 切换更多操作菜单显示状态
     */
    fun toggleMoreOptionsMenu() {
        _showMoreOptionsMenu.value = !_showMoreOptionsMenu.value
    }
    
    /**
     * 进入批量下架模式
     */
    fun enterBatchDelistMode() {
        _isBatchDelistMode.value = true
        _selectedGoodsIds.value = emptySet()
        _problemGoodsIds.value = emptySet()
        _showMoreOptionsMenu.value = false
    }
    
    /**
     * 退出批量下架模式
     */
    fun exitBatchDelistMode() {
        _isBatchDelistMode.value = false
        _selectedGoodsIds.value = emptySet()
        _problemGoodsIds.value = emptySet()
    }
    
    /**
     * 切换商品选中状态
     */
    fun toggleGoodsSelection(goodsId: String) {
        val currentSelected = _selectedGoodsIds.value.toMutableSet()
        if (currentSelected.contains(goodsId)) {
            currentSelected.remove(goodsId)
        } else {
            currentSelected.add(goodsId)
        }
        _selectedGoodsIds.value = currentSelected
        
        // 清除该商品的问题状态
        if (_problemGoodsIds.value.contains(goodsId)) {
            _problemGoodsIds.value = _problemGoodsIds.value - goodsId
        }
    }
    
    /**
     * 显示批量下架取消确认对话框
     */
    fun showBatchCancelConfirmDialog() {
        _showBatchCancelConfirmDialog.value = true
    }
    
    /**
     * 隐藏批量下架取消确认对话框
     */
    fun hideBatchCancelConfirmDialog() {
        _showBatchCancelConfirmDialog.value = false
    }
    
    /**
     * 确认取消批量下架
     */
    fun confirmCancelBatchDelist() {
        _showBatchCancelConfirmDialog.value = false
        exitBatchDelistMode()
    }
    
    /**
     * 显示批量下架确认对话框
     */
    fun showBatchDelistConfirmDialog() {
        if (_selectedGoodsIds.value.isNotEmpty()) {
            _showBatchDelistConfirmDialog.value = true
        }
    }
    
    /**
     * 隐藏批量下架确认对话框
     */
    fun hideBatchDelistConfirmDialog() {
        _showBatchDelistConfirmDialog.value = false
    }
    
    /**
     * 执行批量下架
     */
    fun executeBatchDelist() {
        val selectedIds = _selectedGoodsIds.value
        val allGoods = _allGoods.value
        val selectedGoods = allGoods.filter { it.id in selectedIds }
        
        // 检查哪些商品不能下架（库存不为0）
        val problemGoods = selectedGoods.filter { !it.canBeDelisted }
        
        if (problemGoods.isNotEmpty()) {
            // 有问题商品，标记并显示错误
            _problemGoodsIds.value = problemGoods.map { it.id }.toSet()
            _showBatchDelistConfirmDialog.value = false
            // TODO: 显示错误提示
        } else {
            // 所有商品都可以下架
            val updatedGoodsList = allGoods.map { goods ->
                if (goods.id in selectedIds) {
                    goods.copy(isDelisted = true)
                } else {
                    goods
                }
            }
            _allGoods.value = updatedGoodsList
            
            _showBatchDelistConfirmDialog.value = false
            exitBatchDelistMode()
            
            // 显示成功提示
            GlobalSuccessMessage.showSuccess("成功下架 ${selectedIds.size} 件商品")
        }
    }
    
    /**
     * 获取选中商品数量
     */
    fun getSelectedGoodsCount(): Int {
        return _selectedGoodsIds.value.size
    }
    
    /**
     * 检查商品是否被选中
     */
    fun isGoodsSelected(goodsId: String): Boolean {
        return _selectedGoodsIds.value.contains(goodsId)
    }
    
    /**
     * 检查商品是否是问题商品
     */
    fun isGoodsProblem(goodsId: String): Boolean {
        return _problemGoodsIds.value.contains(goodsId)
    }
    
    // === 分类修改相关方法 ===
    
    /**
     * 显示分类选择对话框
     */
    fun showCategorySelector() {
        _showCategorySelector.value = true
    }
    
    /**
     * 隐藏分类选择对话框
     */
    fun hideCategorySelector() {
        _showCategorySelector.value = false
    }
    
    /**
     * 更新商品分类
     */
    fun updateGoodsCategory(goodsId: String, newCategoryId: String) {
        // 更新选中商品的分类
        _selectedGoods.value?.let { goods ->
            if (goods.id == goodsId) {
                val updatedGoods = goods.copy(category = newCategoryId)
                _selectedGoods.value = updatedGoods
            }
        }
        
        // 更新全量商品列表中的数据
        val updatedGoodsList = _allGoods.value.map { goods ->
            if (goods.id == goodsId) {
                goods.copy(category = newCategoryId)
            } else {
                goods
            }
        }
        _allGoods.value = updatedGoodsList
        
        // 关闭分类选择对话框
        _showCategorySelector.value = false
        
        // 显示成功提示
        GlobalSuccessMessage.showSuccess("分类已更新")
    }
}