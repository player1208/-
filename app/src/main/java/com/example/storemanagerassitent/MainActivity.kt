package com.example.storemanagerassitent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.storemanagerassitent.ui.goods.GoodsManagementScreen
import com.example.storemanagerassitent.ui.home.HomeScreen
import com.example.storemanagerassitent.ui.profile.ProfileScreen
import com.example.storemanagerassitent.ui.category.CategoryManagementScreen
import com.example.storemanagerassitent.ui.components.GlobalSuccessSnackbarHost
import com.example.storemanagerassitent.ui.theme.StoreManagerAssitentTheme

/**
 * 底部导航项
 */
data class NavigationItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

/**
 * 应用页面定义
 */
sealed class AppScreen {
    object Home : AppScreen()
    object Inventory : AppScreen()
    object Profile : AppScreen()
    object CategoryManagement : AppScreen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StoreManagerAssitentTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navigationItems = listOf(
        NavigationItem("首页", Icons.Filled.Home, "home"),
        NavigationItem("库存", Icons.AutoMirrored.Filled.List, "inventory"),
        NavigationItem("我的", Icons.Filled.Person, "profile")
    )

    var selectedIndex by remember { mutableIntStateOf(0) }
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
    
    // 全局成功提示的SnackbarHost状态
    val snackbarHostState = remember { SnackbarHostState() }

    // 处理导航逻辑
    fun navigateToScreen(screen: AppScreen) {
        currentScreen = screen
    }

    fun navigateBack() {
        when (currentScreen) {
            AppScreen.CategoryManagement -> {
                currentScreen = AppScreen.Inventory
            }
            else -> {
                // 其他页面不处理返回，由底部导航控制
            }
        }
    }

    // 根据底部导航更新当前页面
    when (selectedIndex) {
        0 -> currentScreen = AppScreen.Home
        1 -> currentScreen = AppScreen.Inventory
        2 -> currentScreen = AppScreen.Profile
    }

    Scaffold(
        bottomBar = {
            // 只在主要页面显示底部导航栏
            if (currentScreen != AppScreen.CategoryManagement) {
                NavigationBar {
                    navigationItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(text = item.label)
                            },
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index }
                        )
                    }
                }
            }
        },
        snackbarHost = {
            GlobalSuccessSnackbarHost(snackbarHostState = snackbarHostState)
        }
    ) { paddingValues ->
        when (currentScreen) {
            AppScreen.Home -> HomeScreen(
                onPurchaseOrderClick = {
                    // TODO: 实现进货开单功能，导航到进货开单页面
                    // 这里将来可以导航到具体的进货开单页面
                }
            )
            AppScreen.Inventory -> GoodsManagementScreen(
                modifier = Modifier.padding(paddingValues),
                onNavigateToCategoryManagement = {
                    navigateToScreen(AppScreen.CategoryManagement)
                }
            )
            AppScreen.Profile -> ProfileScreen()
            AppScreen.CategoryManagement -> CategoryManagementScreen(
                onNavigateBack = { navigateBack() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    StoreManagerAssitentTheme {
        MainScreen()
    }
}