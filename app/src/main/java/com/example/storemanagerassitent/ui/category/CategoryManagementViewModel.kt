package com.example.storemanagerassitent.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storemanagerassitent.data.Category
import com.example.storemanagerassitent.data.CategoryDeleteResult
import com.example.storemanagerassitent.data.CategoryOperationResult
import com.example.storemanagerassitent.data.CategoryRepository
import com.example.storemanagerassitent.ui.components.GlobalSuccessMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 分类管理ViewModel
 */
class CategoryManagementViewModel : ViewModel() {
    
    // 分类列表
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    // 是否显示新增分类对话框
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()
    
    // 新增分类名称输入
    private val _newCategoryName = MutableStateFlow("")
    val newCategoryName: StateFlow<String> = _newCategoryName.asStateFlow()
    
    // 是否显示编辑分类对话框
    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()
    
    // 编辑分类名称输入
    private val _editCategoryName = MutableStateFlow("")
    val editCategoryName: StateFlow<String> = _editCategoryName.asStateFlow()
    
    // 待编辑的分类
    private val _categoryToEdit = MutableStateFlow<Category?>(null)
    val categoryToEdit: StateFlow<Category?> = _categoryToEdit.asStateFlow()
    
    // 是否显示删除确认对话框
    private val _showDeleteConfirmDialog = MutableStateFlow(false)
    val showDeleteConfirmDialog: StateFlow<Boolean> = _showDeleteConfirmDialog.asStateFlow()
    
    // 是否显示删除失败对话框
    private val _showDeleteFailDialog = MutableStateFlow(false)
    val showDeleteFailDialog: StateFlow<Boolean> = _showDeleteFailDialog.asStateFlow()
    
    // 待删除的分类
    private val _categoryToDelete = MutableStateFlow<Category?>(null)
    val categoryToDelete: StateFlow<Category?> = _categoryToDelete.asStateFlow()
    
    // 删除失败的原因
    private val _deleteFailMessage = MutableStateFlow("")
    val deleteFailMessage: StateFlow<String> = _deleteFailMessage.asStateFlow()
    
    // 操作消息提示
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    
    // 是否正在加载
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadCategories()
    }
    
    /**
     * 加载分类列表
     */
    private fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _categories.value = CategoryRepository.getAllCategories()
            } catch (e: Exception) {
                _message.value = "加载分类失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 显示新增分类对话框
     */
    fun showAddCategoryDialog() {
        _newCategoryName.value = ""
        _showAddDialog.value = true
    }
    
    /**
     * 隐藏新增分类对话框
     */
    fun hideAddCategoryDialog() {
        _showAddDialog.value = false
        _newCategoryName.value = ""
    }
    
    /**
     * 更新新分类名称输入
     */
    fun updateNewCategoryName(name: String) {
        _newCategoryName.value = name
    }
    
    /**
     * 确认新增分类
     */
    fun confirmAddCategory() {
        viewModelScope.launch {
            _isLoading.value = true
            
            when (val result = CategoryRepository.addCategory(_newCategoryName.value.trim())) {
                is CategoryOperationResult.Success -> {
                    _showAddDialog.value = false
                    _newCategoryName.value = ""
                    loadCategories() // 刷新列表
                    
                    // 显示成功提示
                    GlobalSuccessMessage.showSuccess("新分类已添加")
                }
                is CategoryOperationResult.Error -> {
                    _message.value = result.message
                }
            }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 显示编辑分类对话框
     */
    fun showEditCategoryDialog(category: Category) {
        _categoryToEdit.value = category
        _editCategoryName.value = category.name
        _showEditDialog.value = true
    }
    
    /**
     * 隐藏编辑分类对话框
     */
    fun hideEditCategoryDialog() {
        _showEditDialog.value = false
        _editCategoryName.value = ""
        _categoryToEdit.value = null
    }
    
    /**
     * 更新编辑分类名称输入
     */
    fun updateEditCategoryName(name: String) {
        _editCategoryName.value = name
    }
    
    /**
     * 确认编辑分类
     */
    fun confirmEditCategory() {
        val category = _categoryToEdit.value ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            
            when (val result = CategoryRepository.editCategory(category.id, _editCategoryName.value.trim())) {
                is CategoryOperationResult.Success -> {
                    _showEditDialog.value = false
                    _editCategoryName.value = ""
                    _categoryToEdit.value = null
                    loadCategories() // 刷新列表
                    
                    // 显示成功提示
                    GlobalSuccessMessage.showSuccess("分类名称已更新")
                }
                is CategoryOperationResult.Error -> {
                    _message.value = result.message
                }
            }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 尝试删除分类
     */
    fun requestDeleteCategory(category: Category) {
        _categoryToDelete.value = category
        
        // 检查分类下是否有商品
        if (CategoryRepository.hasCategoryProducts(category.id)) {
            _deleteFailMessage.value = "无法删除该分类，请先将其中的所有商品转移到其他分类下。"
            _showDeleteFailDialog.value = true
        } else {
            _showDeleteConfirmDialog.value = true
        }
    }
    
    /**
     * 确认删除分类
     */
    fun confirmDeleteCategory() {
        val category = _categoryToDelete.value ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            
            when (val result = CategoryRepository.deleteCategory(category.id)) {
                is CategoryDeleteResult.Success -> {
                    _showDeleteConfirmDialog.value = false
                    _categoryToDelete.value = null
                    loadCategories() // 刷新列表
                    
                    // 显示成功提示
                    GlobalSuccessMessage.showSuccess("分类已删除")
                }
                is CategoryDeleteResult.HasProducts -> {
                    _deleteFailMessage.value = "无法删除该分类，请先将其中的 ${result.productCount} 个商品转移到其他分类下。"
                    _showDeleteConfirmDialog.value = false
                    _showDeleteFailDialog.value = true
                }
                is CategoryDeleteResult.Error -> {
                    _message.value = result.message
                    _showDeleteConfirmDialog.value = false
                }
            }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 取消删除分类
     */
    fun cancelDeleteCategory() {
        _showDeleteConfirmDialog.value = false
        _categoryToDelete.value = null
    }
    
    /**
     * 关闭删除失败对话框
     */
    fun dismissDeleteFailDialog() {
        _showDeleteFailDialog.value = false
        _categoryToDelete.value = null
    }
    
    /**
     * 清除消息提示
     */
    fun clearMessage() {
        _message.value = null
    }
    
    /**
     * 刷新数据
     */
    fun refreshData() {
        loadCategories()
    }
}