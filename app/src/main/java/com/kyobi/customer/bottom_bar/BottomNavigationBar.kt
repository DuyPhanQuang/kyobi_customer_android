package com.kyobi.customer.bottom_bar

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import androidx.compose.animation.AnimatedContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kyobi.createreel.Secrets
import com.kyobi.createreel.editor_video.SelectMediaType
import com.kyobi.customer.R
import com.kyobi.featurecommon.auth.AuthViewModel
import com.kyobi.featurecommon.routes.Screen
import com.kyobi.theme.Colors
import com.kyobi.theme.kyobiTheme
import ly.img.camera.core.CameraConfiguration
import ly.img.camera.core.CameraMode
import ly.img.camera.core.CameraResult
import ly.img.camera.core.CaptureVideo
import ly.img.camera.core.EngineConfiguration
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

enum class NavBarItemType {
    HOME,
    LOOKBOOK,
    TREND,
    PROFILE
}

data class BottomNavItem(
    val route: String,
    val iconResId: Int,
    val label: String,
    val badgeCount: Int? = null
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = "home",
        iconResId = R.drawable.ic_home_tab,
        label = "Home"
    ),
    BottomNavItem(
        route = "create-reel",
        iconResId = R.drawable.ic_category_tab,
        label = "Lookbook"
    ),
    BottomNavItem(
        route = "trend",
        iconResId = R.drawable.ic_category_tab,
        label = "Trend"
    ),
    BottomNavItem(
        route = "profile",
        iconResId = R.drawable.ic_profile_tab,
        label = "Profile",
        badgeCount = 3
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val tag = "BottomNavigationBar"
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var showBottomSheet by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val sheetState = remember { SheetState(skipPartiallyExpanded = true, density = density) }

    val authViewModel: AuthViewModel = hiltViewModel()
    val authUiState = authViewModel.authUiState.collectAsStateWithLifecycle()
    val userId = authUiState.value.currentUser?.id

    // Launcher để mở native gallery
    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Timber.tag(tag).d("video URI (original): $uri")
                val route = Screen.EditorVideo.getRoute(
                    "selectType" to SelectMediaType.VIDEO.toString(),
                    "uri" to uri.toString(),
                    "userId" to userId
                )
                navController.navigate(route)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(contract = CaptureVideo()) { result ->
        result ?: run { return@rememberLauncherForActivityResult }
        when (result) {
            is CameraResult.Record -> {
                Timber.tag(tag).d("cameraResult (original): $result")
                navController.currentBackStackEntry?.savedStateHandle?.set("recording", result)
                val route = Screen.EditorVideo.getRoute(
                    "selectType" to SelectMediaType.CAMERA_RECORD.toString(),
                    "userId" to userId
                )
                navController.navigate(route)
            }
            else -> {}
        }
    }

    NavigationBar(
        modifier = modifier.background(MaterialTheme.kyobiTheme.colors.surface),
        containerColor = MaterialTheme.kyobiTheme.colors.surface,
        contentColor = MaterialTheme.kyobiTheme.colors.onSurface
    ) {
        // Danh sách các item theo thứ tự: Home, Category, Lookbook, Trend, Profile
        listOf(
            NavBarItemType.HOME,
            NavBarItemType.LOOKBOOK,
            NavBarItemType.TREND,
            NavBarItemType.PROFILE
        ).forEach { itemType ->
            when (itemType) {
                NavBarItemType.LOOKBOOK -> {
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            showBottomSheet = true
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_category_tab),
                                contentDescription = "Lookbook",
                                modifier = Modifier.size(MaterialTheme.kyobiTheme.icon.lg),
                            )
                        },
                        label = {
                            Text(
                                text = "Lookbook",
                                style = MaterialTheme.kyobiTheme.typography.labelSmall,
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.kyobiTheme.colors.text.neutral950,
                            unselectedIconColor = MaterialTheme.kyobiTheme.colors.text.neutral500,
                            selectedTextColor = MaterialTheme.kyobiTheme.colors.text.neutral950,
                            unselectedTextColor = MaterialTheme.kyobiTheme.colors.text.neutral500,
                        )
                    )
                }
                else -> {
                    // Các item khác (Home, Category, Trend, Profile)
                    val index = when (itemType) {
                        NavBarItemType.HOME -> 0
                        NavBarItemType.TREND -> 2
                        NavBarItemType.PROFILE -> 3
                        else -> 0 // Không xảy ra
                    }
                    val item = bottomNavItems[index]
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            AnimatedContent(
                                targetState = isSelected,
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(300)) + fadeIn()).togetherWith(
                                        fadeOut(animationSpec = tween(300))
                                    )
                                },
                                label = "Badge ${item.label}"
                            ) { selected ->
                                BadgedBox(badge = {
                                    if (item.badgeCount != null) {
                                        Badge {
                                            Text(
                                                text = item.badgeCount.toString(),
                                                color = MaterialTheme.kyobiTheme.colors.text.neutral50
                                            )
                                        }
                                    }
                                }) {
                                    Icon(
                                        painter = painterResource(id = item.iconResId),
                                        contentDescription = item.label,
                                        modifier = Modifier.size(
                                            if (selected) MaterialTheme.kyobiTheme.icon.lg
                                            else MaterialTheme.kyobiTheme.icon.lg
                                        ),
                                    )
                                }
                            }
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.kyobiTheme.typography.labelSmall,
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.kyobiTheme.colors.text.neutral950,
                            unselectedIconColor = MaterialTheme.kyobiTheme.colors.text.neutral500,
                            selectedTextColor = MaterialTheme.kyobiTheme.colors.text.neutral950,
                            unselectedTextColor = MaterialTheme.kyobiTheme.colors.text.neutral500,
                        )
                    )
                }
            }
        }
    }

    // Hiển thị bottom sheet với 2 lựa chọn
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState
        ) {
            Text(
                text = "Tạo Reel mới",
                modifier = Modifier.padding(16.dp),
                style = androidx.compose.material3.Typography().titleLarge
            )

            Button(
                onClick = {
                    showBottomSheet = false
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                    pickVideoLauncher.launch(intent)
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Chọn video để đăng")
            }

            Button(
                onClick = {
                    showBottomSheet = false
                    val cameraInput = CaptureVideo.Input(
                        engineConfiguration = EngineConfiguration(license = Secrets.license, userId = userId),
                        cameraMode = CameraMode.Standard(),
                        cameraConfiguration = CameraConfiguration(
                            recordingColor = Colors().logo,
                            maxTotalDuration = 180.seconds,
                            allowExceedingMaxDuration = false
                        )
                        )
                    cameraLauncher.launch(cameraInput)
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Quay video")
            }
        }
    }
}