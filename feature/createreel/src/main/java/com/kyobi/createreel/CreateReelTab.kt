package com.kyobi.createreel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kyobi.createreel.editor_video.EditorVideoViewModel
import com.kyobi.featurecommon.auth.AuthViewModel
import com.kyobi.featurecommon.routes.Screen
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReelTab(
    navController: NavController,
    sceneUri: Uri,
    authViewModel: AuthViewModel = hiltViewModel(),
    editorVideoViewModel: EditorVideoViewModel = hiltViewModel(),
) {
    val tag = "CreateReelTab"
    val authUiState = authViewModel.authUiState.collectAsStateWithLifecycle()
    val userId = authUiState.value.currentUser?.id
    val context = LocalContext.current
    val activity = context as? Activity ?: throw IllegalStateException("Context must be an Activity")

    // Trạng thái hiển thị bottom sheet
    var showBottomSheet by remember { mutableStateOf(true) }

    // Sử dụng LocalDensity để lấy density
    val density = LocalDensity.current
    // Khởi tạo SheetState với density
    val sheetState = remember {
        SheetState(skipPartiallyExpanded = true, density = density)
    }

    // Launcher để mở native gallery
    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Timber.tag(tag).d("Selected video URI: $uri")
                // Log giá trị của sceneUri ban đầu để kiểm tra
                Timber.tag(tag).d("Scene URI (original): $sceneUri")
                val route = Screen.EditorVideo.getRoute(
                    "sceneUri" to sceneUri.toString(),
                    "videoUri" to uri.toString(),
                    "userId" to userId
                )
                navController.navigate(route) {
                    popUpTo(Screen.VideoUi.routeScheme) {
                        inclusive = true
                    }
                }
            }
        }
    }

    // Hiển thị bottom sheet với 3 lựa chọn
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                navController.popBackStack()
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
                    Timber.tag(tag).d("Quay video clicked - Not implemented yet")
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Quay video")
            }

            Button(
                onClick = {
                    showBottomSheet = false
                    Timber.tag(tag).d("Chọn hình để đăng clicked - Not implemented yet")
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Chọn hình để đăng")
            }
        }
    }
}