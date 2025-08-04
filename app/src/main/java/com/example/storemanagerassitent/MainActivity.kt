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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.storemanagerassitent.ui.goods.GoodsManagementScreen
import com.example.storemanagerassitent.ui.home.HomeScreen
import com.example.storemanagerassitent.ui.profile.ProfileScreen
import com.example.storemanagerassitent.ui.theme.StoreManagerAssitentTheme

/**
 * 底部导航项
 */
data class NavigationItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

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
    
    Scaffold(
        bottomBar = {
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
    ) { paddingValues ->
        when (selectedIndex) {
            0 -> HomeScreen()
            1 -> GoodsManagementScreen()
            2 -> ProfileScreen()
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