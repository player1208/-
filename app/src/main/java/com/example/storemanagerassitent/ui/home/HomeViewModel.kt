package com.example.storemanagerassitent.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storemanagerassitent.data.HomeData
import com.example.storemanagerassitent.data.SalesInsightData
import com.example.storemanagerassitent.data.TimePeriod
import com.example.storemanagerassitent.data.CategoryOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 首页ViewModel
 */
class HomeViewModel : ViewModel() {
    
    // 当前选中的时间维度
    private val _selectedTimePeriod = MutableStateFlow(TimePeriod.WEEK)
    val selectedTimePeriod: StateFlow<TimePeriod> = _selectedTimePeriod.asStateFlow()
    
    // 当前选中的分类
    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()
    
    // 销售洞察数据
    private val _salesInsightData = MutableStateFlow(
        HomeData.getSalesInsightData(TimePeriod.WEEK, "all")
    )
    val salesInsightData: StateFlow<SalesInsightData> = _salesInsightData.asStateFlow()
    
    // 是否正在加载数据
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 分类下拉菜单是否展开
    private val _showCategoryDropdown = MutableStateFlow(false)
    val showCategoryDropdown: StateFlow<Boolean> = _showCategoryDropdown.asStateFlow()
    
    /**
     * 快速操作数据（静态）
     */
    val quickActions = HomeData.quickActions
    
    /**
     * 分类选项列表
     */
    val categoryOptions = HomeData.categoryOptions
    
    /**
     * 切换时间维度
     */
    fun selectTimePeriod(period: TimePeriod) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedTimePeriod.value = period
            
            // 模拟数据加载延迟
            kotlinx.coroutines.delay(300)
            
            _salesInsightData.value = HomeData.getSalesInsightData(period, _selectedCategory.value)
            _isLoading.value = false
        }
    }
    
    /**
     * 切换分类筛选
     */
    fun selectCategory(categoryId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedCategory.value = categoryId
            _showCategoryDropdown.value = false
            
            // 模拟数据加载延迟
            kotlinx.coroutines.delay(200)
            
            _salesInsightData.value = HomeData.getSalesInsightData(_selectedTimePeriod.value, categoryId)
            _isLoading.value = false
        }
    }
    
    /**
     * 切换分类下拉菜单显示状态
     */
    fun toggleCategoryDropdown() {
        _showCategoryDropdown.value = !_showCategoryDropdown.value
    }
    
    /**
     * 隐藏分类下拉菜单
     */
    fun hideCategoryDropdown() {
        _showCategoryDropdown.value = false
    }
    
    /**
     * 处理快速操作点击
     */
    fun onQuickActionClick(actionId: String) {
        // TODO: 根据actionId导航到对应页面
        when (actionId) {
            "sales" -> {
                // 导航到销售开单页面
            }
            "purchase" -> {
                // 导航到进货开单页面
            }
        }
    }
    
    /**
     * 处理次要操作点击（查看记录）
     */
    fun onSecondaryActionClick(actionId: String) {
        // TODO: 根据actionId导航到对应记录页面
        when (actionId) {
            "sales" -> {
                // 导航到销售记录页面
            }
            "purchase" -> {
                // 导航到进货记录页面
            }
        }
    }
    
    /**
     * 刷新数据
     */
    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // 模拟数据加载延迟
            kotlinx.coroutines.delay(300)
            
            _salesInsightData.value = HomeData.getSalesInsightData(_selectedTimePeriod.value, _selectedCategory.value)
            _isLoading.value = false
        }
    }
}