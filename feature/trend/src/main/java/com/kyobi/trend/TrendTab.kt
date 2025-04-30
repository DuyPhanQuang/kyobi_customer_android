package com.kyobi.trend

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.kyobi.featurecommon.auth.AuthViewModel
import com.kyobi.trend.model.Reel
import com.kyobi.trend.ui.ReelList

@OptIn(UnstableApi::class)
@Composable
fun TrendTab(
    navController: NavController,
    viewModel: TrendTabViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    topPadding: Dp,
    bottomPadding: Dp,
) {
    val authUiState by authViewModel.authUiState.collectAsStateWithLifecycle()
    val uiState = viewModel.trendTabUiState
    val recyclerViewRef = remember { mutableStateOf<RecyclerView?>(null) }

    val mockData = listOf(
        Reel(
            id = "reel_1",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/9b52e1b2-59b8-4421-9c4c-395b35a311f7/video-1744688854378.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy85YjUyZTFiMi01OWI4LTQ0MjEtOWM0Yy0zOTViMzVhMzExZjcvdmlkZW8tMTc0NDY4ODg1NDM3OC5tcDQiLCJpYXQiOjE3NDU4MTEwOTcsImV4cCI6MTc0ODQwMzA5N30.P_6QrbrC933k5zuHGKaJ4V57aAvtwRGg0s7oYW6HlCE",
            status = "PUBLISHED",
            likeCount = 1200,
            commentCount = 150,
            shareCount = 50,
            viewCount = 5000,
            createdAt = "2025-04-20T10:00:00Z",
            thumbnailUrl = "https://media1.tenor.com/m/Gi3YSm0uDK0AAAAd/random-lol.gif",
            tags = listOf("fashion", "trend")
        ),
        Reel(
            id = "reel_2",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/9b52e1b2-59b8-4421-9c4c-395b35a311f7/video-1744688854378.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy85YjUyZTFiMi01OWI4LTQ0MjEtOWM0Yy0zOTViMzVhMzExZjcvdmlkZW8tMTc0NDY4ODg1NDM3OC5tcDQiLCJpYXQiOjE3NDU4MTEwOTcsImV4cCI6MTc0ODQwMzA5N30.P_6QrbrC933k5zuHGKaJ4V57aAvtwRGg0s7oYW6HlCE",
            status = "PUBLISHED",
            likeCount = 800,
            commentCount = 90,
            shareCount = 30,
            viewCount = 3200,
            createdAt = "2025-04-19T15:30:00Z",
            thumbnailUrl = "https://media1.tenor.com/m/Gi3YSm0uDK0AAAAd/random-lol.gif",
            tags = listOf("style", "kyobi")
        ),
        Reel(
            id = "reel_3",
            videoUrl = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
            status = "PUBLISHED",
            likeCount = 2000,
            commentCount = 300,
            shareCount = 100,
            viewCount = 10000,
            createdAt = "2025-04-18T09:00:00Z",
            thumbnailUrl = "https://media1.tenor.com/m/Gi3YSm0uDK0AAAAd/random-lol.gif",
            tags = listOf("fashion", "sale")
        ),
        Reel(
            id = "reel_4",
            videoUrl = "https://devstreaming-cdn.apple.com/videos/streaming/examples/adv_dv_atmos/main.m3u8",
            status = "DRAFT",
            likeCount = 0,
            commentCount = 0,
            shareCount = 0,
            viewCount = 0,
            createdAt = "2025-04-17T12:00:00Z",
            thumbnailUrl = "https://media1.tenor.com/m/Gi3YSm0uDK0AAAAd/random-lol.gif",
            tags = null
        ),
        Reel(
            id = "reel_5",
            videoUrl = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
            status = "DRAFT",
            likeCount = 0,
            commentCount = 0,
            shareCount = 0,
            viewCount = 0,
            createdAt = "2025-04-17T12:00:00Z",
            thumbnailUrl = "https://media1.tenor.com/m/Gi3YSm0uDK0AAAAd/random-lol.gif",
            tags = null
        ),
        Reel(
            id = "reel_6",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            status = "PUBLISHED",
            likeCount = 2000,
            commentCount = 300,
            shareCount = 100,
            viewCount = 10000,
            createdAt = "2025-04-18T09:00:00Z",
            thumbnailUrl = "https://media1.tenor.com/m/Gi3YSm0uDK0AAAAd/random-lol.gif",
            tags = listOf("fashion", "sale")
        ),
        Reel(
            id = "reel_7",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            status = "DRAFT",
            likeCount = 0,
            commentCount = 0,
            shareCount = 0,
            viewCount = 0,
            createdAt = "2025-04-17T12:00:00Z",
            thumbnailUrl = "https://media1.tenor.com/m/Gi3YSm0uDK0AAAAd/random-lol.gif",
            tags = null
        )
    )

    ReelList(
        reels = mockData,
        topSystemBarHeight = topPadding,
        bottomNavBarHeight = bottomPadding
    )
}