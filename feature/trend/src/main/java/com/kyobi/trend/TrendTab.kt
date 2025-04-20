package com.kyobi.trend

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.kyobi.featurecommon.auth.AuthViewModel
import com.kyobi.trend.model.Reel
import com.kyobi.trend.ui.ReelList

@OptIn(UnstableApi::class)
@Composable
fun TrendTab(
    navController: NavController,
    viewModel: TrendTabViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val authUiState by authViewModel.authUiState.collectAsStateWithLifecycle()
    val uiState = viewModel.trendTabUiState
    val mediaCache = viewModel.mediaCache

    val mockData = listOf(
        Reel(
            id = "reel_1",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1744549928872.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NDU0OTkyODg3Mi5tcDQiLCJpYXQiOjE3NDUxNDYyNjksImV4cCI6MTc0NTc1MTA2OX0.Ebac4L5rtd4bxIxCtL1WgC7KkeWF5riEkr0MtI3mYFo",
            status = "PUBLISHED",
            likeCount = 1200,
            commentCount = 150,
            shareCount = 50,
            viewCount = 5000,
            createdAt = "2025-04-20T10:00:00Z",
            tags = listOf("fashion", "trend")
        ),
        Reel(
            id = "reel_2",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1744549928872.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NDU0OTkyODg3Mi5tcDQiLCJpYXQiOjE3NDUxNDYyNjksImV4cCI6MTc0NTc1MTA2OX0.Ebac4L5rtd4bxIxCtL1WgC7KkeWF5riEkr0MtI3mYFo",
            status = "PUBLISHED",
            likeCount = 800,
            commentCount = 90,
            shareCount = 30,
            viewCount = 3200,
            createdAt = "2025-04-19T15:30:00Z",
            tags = listOf("style", "kyobi")
        ),
        Reel(
            id = "reel_3",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            status = "PUBLISHED",
            likeCount = 2000,
            commentCount = 300,
            shareCount = 100,
            viewCount = 10000,
            createdAt = "2025-04-18T09:00:00Z",
            tags = listOf("fashion", "sale")
        ),
        Reel(
            id = "reel_4",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            status = "DRAFT",
            likeCount = 0,
            commentCount = 0,
            shareCount = 0,
            viewCount = 0,
            createdAt = "2025-04-17T12:00:00Z",
            tags = null
        ),
    )
    ReelList(
        reels = mockData,
        mediaCache
    )
}