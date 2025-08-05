package com.example.storemanagerassitent.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.storemanagerassitent.data.DateFilterState
import com.example.storemanagerassitent.data.DateFilterType
import com.example.storemanagerassitent.data.SalesOrder
import com.example.storemanagerassitent.data.SalesRecordData
import com.example.storemanagerassitent.data.SalesRecordSummary
import java.util.Date

/**
 * 销售记录ViewModel
 */
class SalesRecordViewModel : ViewModel() {
    
    // 日期筛选状态
    private val _dateFilterState = MutableStateFlow(DateFilterState())
    val dateFilterState: StateFlow<DateFilterState> = _dateFilterState.asStateFlow()
    
    // 销售记录列表
    private val _salesRecords = MutableStateFlow<List<SalesRecordSummary>>(emptyList())
    val salesRecords: StateFlow<List<SalesRecordSummary>> = _salesRecords.asStateFlow()
    
    // 是否显示日历选择器
    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker.asStateFlow()
    
    // 是否显示订单详情
    private val _showOrderDetails = MutableStateFlow(false)
    val showOrderDetails: StateFlow<Boolean> = _showOrderDetails.asStateFlow()
    
    // 当前查看的订单详情
    private val _selectedOrder = MutableStateFlow<SalesOrder?>(null)
    val selectedOrder: StateFlow<SalesOrder?> = _selectedOrder.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        // 初始化时加载今天的销售记录
        loadSalesRecords()
    }
    
    /**
     * 加载销售记录
     */
    private fun loadSalesRecords() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // 模拟网络延迟
            kotlinx.coroutines.delay(300)
            
            val records = SalesRecordData.getSalesRecordSummaries(_dateFilterState.value)
            _salesRecords.value = records
            
            _isLoading.value = false
        }
    }
    
    /**
     * 设置日期筛选
     */
    fun setDateFilter(filterState: DateFilterState) {
        _dateFilterState.value = filterState
        loadSalesRecords()
    }
    
    /**
     * 显示日历选择器
     */
    fun showDatePicker() {
        _showDatePicker.value = true
    }
    
    /**
     * 隐藏日历选择器
     */
    fun hideDatePicker() {
        _showDatePicker.value = false
    }
    
    /**
     * 选择预设的日期筛选
     */
    fun selectPresetDateFilter(filterType: DateFilterType) {
        setDateFilter(DateFilterState(filterType = filterType))
        hideDatePicker()
    }
    
    /**
     * 选择自定义日期
     */
    fun selectCustomDate(date: Date) {
        setDateFilter(
            DateFilterState(
                filterType = DateFilterType.CUSTOM_DATE,
                customDate = date
            )
        )
        hideDatePicker()
    }
    
    /**
     * 选择自定义月份
     */
    fun selectCustomMonth(year: Int, month: Int) {
        setDateFilter(
            DateFilterState(
                filterType = DateFilterType.CUSTOM_MONTH,
                customYear = year,
                customMonth = month
            )
        )
        hideDatePicker()
    }
    
    /**
     * 选择自定义年份
     */
    fun selectCustomYear(year: Int) {
        setDateFilter(
            DateFilterState(
                filterType = DateFilterType.CUSTOM_YEAR,
                customYear = year
            )
        )
        hideDatePicker()
    }
    
    /**
     * 显示订单详情
     */
    fun showOrderDetails(orderId: String) {
        viewModelScope.launch {
            val order = SalesRecordData.getSalesOrderById(orderId)
            if (order != null) {
                _selectedOrder.value = order
                _showOrderDetails.value = true
            }
        }
    }
    
    /**
     * 隐藏订单详情
     */
    fun hideOrderDetails() {
        _showOrderDetails.value = false
        _selectedOrder.value = null
    }
    
    /**
     * 刷新数据
     */
    fun refresh() {
        loadSalesRecords()
    }
}