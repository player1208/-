package com.example.storemanagerassitent.ui.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storemanagerassitent.data.DataStoreManager
import com.example.storemanagerassitent.data.PurchaseOrderItem
import com.example.storemanagerassitent.ui.components.GlobalSuccessMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class PurchaseOrderViewModel : ViewModel() {
    
    // 待入库订单状态
    private val _purchaseItems = MutableStateFlow(listOf<PurchaseItem>())
    val purchaseItems: StateFlow<List<PurchaseItem>> = _purchaseItems.asStateFlow()
    
    // 草稿ID
    private var draftId: String = UUID.randomUUID().toString()
    
    // 数据存储管理器
    private var dataStoreManager: DataStoreManager? = null
    
    /**
     * 初始化数据存储管理器
     */
    fun initializeDataStore(dataStore: DataStoreManager) {
        dataStoreManager = dataStore
    }
    
    /**
     * 添加商品到待入库列表
     */
    fun addPurchaseItem(item: PurchaseItem) {
        val currentItems = _purchaseItems.value.toMutableList()
        
        // 检查是否已存在相同商品
        val existingIndex = currentItems.indexOfFirst { 
            it.goodsId == item.goodsId && it.purchasePrice == item.purchasePrice 
        }
        
        if (existingIndex >= 0) {
            // 更新现有商品数量
            val existingItem = currentItems[existingIndex]
            currentItems[existingIndex] = existingItem.copy(quantity = existingItem.quantity + item.quantity)
        } else {
            // 添加新商品
            currentItems.add(item)
        }
        
        _purchaseItems.value = currentItems
        saveDraft()
    }
    
    /**
     * 更新商品数量
     */
    fun updateItemQuantity(goodsId: String?, purchasePrice: Double, newQuantity: Int) {
        val currentItems = _purchaseItems.value.toMutableList()
        val index = currentItems.indexOfFirst { 
            it.goodsId == goodsId && it.purchasePrice == purchasePrice 
        }
        
        if (index >= 0) {
            currentItems[index] = currentItems[index].copy(quantity = newQuantity)
            _purchaseItems.value = currentItems
            saveDraft()
        }
    }
    
    /**
     * 移除商品
     */
    fun removeItem(goodsId: String?, purchasePrice: Double) {
        val currentItems = _purchaseItems.value.toMutableList()
        currentItems.removeAll { 
            it.goodsId == goodsId && it.purchasePrice == purchasePrice 
        }
        _purchaseItems.value = currentItems
        saveDraft()
    }
    
    /**
     * 清空待入库列表
     */
    fun clearItems() {
        _purchaseItems.value = emptyList()
        saveDraft()
    }
    
    /**
     * 获取总数量
     */
    fun getTotalQuantity(): Int {
        return _purchaseItems.value.sumOf { it.quantity }
    }
    
    /**
     * 获取总金额
     */
    fun getTotalAmount(): Double {
        return _purchaseItems.value.sumOf { it.purchasePrice * it.quantity }
    }
    
    /**
     * 获取商品种类数量
     */
    fun getItemTypesCount(): Int {
        return _purchaseItems.value.size
    }
    
    /**
     * 保存为草稿
     */
    private fun saveDraft() {
        viewModelScope.launch {
            dataStoreManager?.let { dataStore ->
                val draft = DataStoreManager.PurchaseDraft(
                    id = draftId,
                    items = _purchaseItems.value.map { item ->
                        PurchaseOrderItem(
                            goodsId = item.goodsId,
                            goodsName = item.displayName,
                            specifications = "",
                            purchasePrice = item.purchasePrice,
                            quantity = item.quantity,
                            category = ""
                        )
                    },
                    totalAmount = getTotalAmount(),
                    totalQuantity = getTotalQuantity(),
                    updatedAt = System.currentTimeMillis()
                )
                dataStore.savePurchaseDraft(draft)
            }
        }
    }
    
    /**
     * 保存草稿并显示提示
     */
    fun saveOrderAsDraft() {
        if (_purchaseItems.value.isNotEmpty()) {
            saveDraft()
            GlobalSuccessMessage.showSuccess("进货单已保存为草稿")
        }
    }
    
    /**
     * 清空订单数据
     */
    fun clearOrderData() {
        _purchaseItems.value = emptyList()
        draftId = UUID.randomUUID().toString()
        GlobalSuccessMessage.showSuccess("进货单数据已清空")
    }
    
    /**
     * 确认入库
     */
    fun confirmInbound() {
        if (_purchaseItems.value.isNotEmpty()) {
            // TODO: 这里可以实现实际的入库逻辑
            GlobalSuccessMessage.showSuccess("入库成功！")
            clearOrderData()
        }
    }
    
    /**
     * 加载草稿
     */
    fun loadDraft(draft: DataStoreManager.PurchaseDraft) {
        draftId = draft.id
        _purchaseItems.value = draft.items.map { item ->
            PurchaseItem(
                goodsId = item.goodsId,
                displayName = item.goodsName,
                quantity = item.quantity,
                purchasePrice = item.purchasePrice
            )
        }
        GlobalSuccessMessage.showSuccess("草稿已加载")
    }
    
    /**
     * 检查是否有待入库商品
     */
    fun hasItems(): Boolean {
        return _purchaseItems.value.isNotEmpty()
    }
}
