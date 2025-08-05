package com.example.storemanagerassitent.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storemanagerassitent.data.Goods
import com.example.storemanagerassitent.data.PaymentMethod
import com.example.storemanagerassitent.data.PaymentType
import com.example.storemanagerassitent.data.SalesOrder
import com.example.storemanagerassitent.data.SalesOrderItem
import com.example.storemanagerassitent.data.SalesOrderState
import com.example.storemanagerassitent.data.SampleData
import com.example.storemanagerassitent.ui.components.GlobalSuccessMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 销售订单ViewModel
 */
class SalesOrderViewModel : ViewModel() {
    
    // 销售订单状态
    private val _salesOrderState = MutableStateFlow(SalesOrderState())
    val salesOrderState: StateFlow<SalesOrderState> = _salesOrderState.asStateFlow()
    
    // 商品选择状态
    private val _productSelectionState = MutableStateFlow(com.example.storemanagerassitent.data.ProductSelectionState())
    val productSelectionState: StateFlow<com.example.storemanagerassitent.data.ProductSelectionState> = _productSelectionState.asStateFlow()
    
    // 是否显示商品选择界面
    private val _showProductSelection = MutableStateFlow(false)
    val showProductSelection: StateFlow<Boolean> = _showProductSelection.asStateFlow()
    
    // 是否显示数量确认对话框
    private val _showQuantityDialog = MutableStateFlow(false)
    val showQuantityDialog: StateFlow<Boolean> = _showQuantityDialog.asStateFlow()
    
    // 当前选择的商品
    private val _selectedGoods = MutableStateFlow<Goods?>(null)
    val selectedGoods: StateFlow<Goods?> = _selectedGoods.asStateFlow()
    
    // 数量确认对话框的数量
    private val _quantityDialogAmount = MutableStateFlow(1)
    val quantityDialogAmount: StateFlow<Int> = _quantityDialogAmount.asStateFlow()
    
    // 是否显示价格编辑对话框
    private val _showPriceEditDialog = MutableStateFlow(false)
    val showPriceEditDialog: StateFlow<Boolean> = _showPriceEditDialog.asStateFlow()
    
    // 当前编辑价格的订单项
    private val _editingOrderItem = MutableStateFlow<SalesOrderItem?>(null)
    val editingOrderItem: StateFlow<SalesOrderItem?> = _editingOrderItem.asStateFlow()
    
    // 价格编辑对话框的价格
    private val _priceEditAmount = MutableStateFlow(0.0)
    val priceEditAmount: StateFlow<Double> = _priceEditAmount.asStateFlow()
    
    /**
     * 显示商品选择界面
     */
    fun showProductSelection() {
        _showProductSelection.value = true
        _productSelectionState.value = com.example.storemanagerassitent.data.ProductSelectionState(isSelectionMode = true)
    }
    
    /**
     * 隐藏商品选择界面
     */
    fun hideProductSelection() {
        _showProductSelection.value = false
        _productSelectionState.value = com.example.storemanagerassitent.data.ProductSelectionState()
    }
    
    /**
     * 重置UI状态（不包括订单数据）
     */
    fun resetUIStates() {
        _productSelectionState.value = com.example.storemanagerassitent.data.ProductSelectionState()
        _showProductSelection.value = false
        _showQuantityDialog.value = false
        _selectedGoods.value = null
        _quantityDialogAmount.value = 1
        _showPriceEditDialog.value = false
        _editingOrderItem.value = null
        _priceEditAmount.value = 0.0
    }
    
    /**
     * 重置整个订单（用于新建订单时）
     */
    fun resetOrder() {
        _salesOrderState.value = SalesOrderState()
        resetUIStates()
    }
    
    /**
     * 选择商品
     */
    fun selectProduct(goods: Goods) {
        _selectedGoods.value = goods
        _quantityDialogAmount.value = 1
        _showQuantityDialog.value = true
    }
    
    /**
     * 确认商品数量
     */
    fun confirmProductQuantity() {
        val goods = _selectedGoods.value ?: return
        val quantity = _quantityDialogAmount.value
        
        // 检查库存
        if (quantity > goods.stockQuantity) {
            // TODO: 显示库存不足提示
            return
        }
        
        // 创建订单项
        val orderItem = SalesOrderItem(
            goodsId = goods.id,
            goodsName = goods.name,
            specifications = goods.specifications,
            unitPrice = goods.purchasePrice, // 使用进货价作为默认售价
            quantity = quantity,
            category = goods.category
        )
        
        // 添加到订单
        val currentItems = _salesOrderState.value.items.toMutableList()
        currentItems.add(orderItem)
        _salesOrderState.value = _salesOrderState.value.copy(items = currentItems)
        
        // 关闭对话框
        _showQuantityDialog.value = false
        _selectedGoods.value = null
        
        // 显示成功提示
        GlobalSuccessMessage.showSuccess("添加成功")
    }
    
    /**
     * 批量添加商品到订单
     */
    fun addMultipleItems(items: List<SalesOrderItem>) {
        if (items.isEmpty()) return
        
        // 添加所有商品到订单
        val currentItems = _salesOrderState.value.items.toMutableList()
        currentItems.addAll(items)
        _salesOrderState.value = _salesOrderState.value.copy(items = currentItems)
        
        // 关闭商品选择界面
        hideProductSelection()
        
        // 显示成功提示
        val itemCount = items.size
        GlobalSuccessMessage.showSuccess("成功添加 $itemCount 件商品")
    }
    
    /**
     * 直接批量添加已确认的商品项（无需再次确认）
     */
    fun addConfirmedItems(items: List<SalesOrderItem>) {
        if (items.isEmpty()) return
        
        // 添加所有商品到订单
        val currentItems = _salesOrderState.value.items.toMutableList()
        currentItems.addAll(items)
        _salesOrderState.value = _salesOrderState.value.copy(items = currentItems)
        
        // 关闭商品选择界面
        hideProductSelection()
        
        // 显示成功提示
        val itemCount = items.size
        GlobalSuccessMessage.showSuccess("成功添加 $itemCount 件商品")
    }
    
    /**
     * 取消商品数量确认
     */
    fun cancelProductQuantity() {
        _showQuantityDialog.value = false
        _selectedGoods.value = null
    }
    
    /**
     * 增加数量
     */
    fun increaseQuantity() {
        val current = _quantityDialogAmount.value
        val goods = _selectedGoods.value
        if (goods != null && current < goods.stockQuantity) {
            _quantityDialogAmount.value = current + 1
        }
    }
    
    /**
     * 减少数量
     */
    fun decreaseQuantity() {
        val current = _quantityDialogAmount.value
        if (current > 1) {
            _quantityDialogAmount.value = current - 1
        }
    }
    
    /**
     * 更新数量
     */
    fun updateQuantity(quantity: Int) {
        _quantityDialogAmount.value = quantity
    }
    
    /**
     * 删除订单项
     */
    fun removeOrderItem(itemId: String) {
        val currentItems = _salesOrderState.value.items.toMutableList()
        currentItems.removeAll { it.id == itemId }
        _salesOrderState.value = _salesOrderState.value.copy(items = currentItems)
    }
    
    /**
     * 更新订单项数量
     */
    fun updateOrderItemQuantity(itemId: String, quantity: Int) {
        val currentItems = _salesOrderState.value.items.toMutableList()
        val index = currentItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = currentItems[index]
            currentItems[index] = item.copy(quantity = quantity)
            _salesOrderState.value = _salesOrderState.value.copy(items = currentItems)
        }
    }
    
    /**
     * 显示价格编辑对话框
     */
    fun showPriceEditDialog(orderItem: SalesOrderItem) {
        _editingOrderItem.value = orderItem
        _priceEditAmount.value = orderItem.unitPrice
        _showPriceEditDialog.value = true
    }
    
    /**
     * 隐藏价格编辑对话框
     */
    fun hidePriceEditDialog() {
        _showPriceEditDialog.value = false
        _editingOrderItem.value = null
    }
    
    /**
     * 确认价格编辑
     */
    fun confirmPriceEdit() {
        val orderItem = _editingOrderItem.value ?: return
        val newPrice = _priceEditAmount.value
        
        val currentItems = _salesOrderState.value.items.toMutableList()
        val index = currentItems.indexOfFirst { it.id == orderItem.id }
        if (index != -1) {
            val item = currentItems[index]
            currentItems[index] = item.copy(unitPrice = newPrice)
            _salesOrderState.value = _salesOrderState.value.copy(items = currentItems)
        }
        
        hidePriceEditDialog()
    }
    
    /**
     * 更新价格编辑金额
     */
    fun updatePriceEditAmount(amount: Double) {
        _priceEditAmount.value = amount
    }
    
    /**
     * 设置支付方式
     */
    fun setPaymentMethod(method: PaymentMethod) {
        _salesOrderState.value = _salesOrderState.value.copy(paymentMethod = method)
    }
    
    /**
     * 设置付款类型
     */
    fun setPaymentType(type: PaymentType) {
        _salesOrderState.value = _salesOrderState.value.copy(
            paymentType = type,
            depositAmount = if (type == PaymentType.FULL_PAYMENT) 0.0 else _salesOrderState.value.depositAmount
        )
    }
    
    /**
     * 设置定金金额
     */
    fun setDepositAmount(amount: Double) {
        _salesOrderState.value = _salesOrderState.value.copy(depositAmount = amount)
    }
    
    /**
     * 设置客户姓名
     */
    fun setCustomerName(name: String) {
        _salesOrderState.value = _salesOrderState.value.copy(customerName = name)
    }
    
    /**
     * 设置客户电话
     */
    fun setCustomerPhone(phone: String) {
        _salesOrderState.value = _salesOrderState.value.copy(customerPhone = phone)
    }
    
    /**
     * 设置客户地址
     */
    fun setCustomerAddress(address: String) {
        _salesOrderState.value = _salesOrderState.value.copy(customerAddress = address)
    }
    
    /**
     * 完成订单
     */
    fun completeOrder() {
        val state = _salesOrderState.value
        if (!state.canCompleteOrder) return
        
        viewModelScope.launch {
            // 创建销售订单
            val order = SalesOrder(
                items = state.items,
                paymentMethod = state.paymentMethod!!,
                paymentType = state.paymentType!!,
                depositAmount = state.depositAmount,
                customerName = state.customerName,
                customerPhone = state.customerPhone,
                customerAddress = state.customerAddress,
                totalAmount = state.totalAmount
            )
            
            // TODO: 保存订单到数据库
            // TODO: 更新库存
            
            // 显示成功提示
            GlobalSuccessMessage.showSuccess("收款成功，订单已生成")
            
            // 重置整个订单，准备下一个订单
            resetOrder()
        }
    }
    
    /**
     * 获取所有商品（用于商品选择）
     */
    fun getAllGoods(): List<Goods> {
        return SampleData.goods.filter { !it.isDelisted }
    }
} 