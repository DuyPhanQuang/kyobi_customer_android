package com.kyobi.trend

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.kyobi.featurecommon.auth.AuthViewModel
import com.kyobi.trend.model.Reel
import com.kyobi.trend.ui.ReelList
import com.kyobi.trend.ui.ReelPlaybackViewModel

@OptIn(UnstableApi::class)
@Composable
fun TrendTab(
    navController: NavController,
    viewModel: TrendTabViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    reelPlaybackViewModel: ReelPlaybackViewModel = hiltViewModel(),
    topPadding: Dp,
    bottomPadding: Dp,
) {
    val authUiState by authViewModel.authUiState.collectAsStateWithLifecycle()
    val uiState = viewModel.trendTabUiState
    val recyclerViewRef = remember { mutableStateOf<RecyclerView?>(null) }

    val mockMoreData = listOf(
        Reel(
            id = "reel_2_3",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746195202763.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjE5NTIwMjc2My5tcDQiLCJpYXQiOjE3NDYxOTU0NDAsImV4cCI6MTc0ODc4NzQ0MH0.bWSOGNk7fbgdRuWwdeC9RyiGDiweaiwNibNAf_dEfS4",
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746372731253.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjM3MjczMTI1My5tcDQiLCJpYXQiOjE3NDYzNzI3NjksImV4cCI6MTc0ODk2NDc2OX0.1UJ_omCYHCd5aFby4KCt0gvsZBqcbyGMmcGRmV6sNlY",
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
            id = "reel_2_2_2",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1744563878330.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NDU2Mzg3ODMzMC5tcDQiLCJpYXQiOjE3NDYzNDA0NTIsImV4cCI6MTc0ODkzMjQ1Mn0.FxoglE6_2KVZPk4D-BxsBnw30_ve3dipdXpF5v044os",
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746372752350.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjM3Mjc1MjM1MC5tcDQiLCJpYXQiOjE3NDYzNzI3ODgsImV4cCI6MTc0ODk2NDc4OH0.C5LfkfqFYAk1PLQXdm8HKlTRc1dK91NsUd2VdPb93-g",
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
            id = "reel_2_4",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746195253655.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjE5NTI1MzY1NS5tcDQiLCJpYXQiOjE3NDYxOTU0NTEsImV4cCI6MTc0ODc4NzQ1MX0.DnEvpM3key29cVRaDtgOZh-S5yWTf_XZbydAIrnYS8U",
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746372731253.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjM3MjczMTI1My5tcDQiLCJpYXQiOjE3NDYzNzI3NjksImV4cCI6MTc0ODk2NDc2OX0.1UJ_omCYHCd5aFby4KCt0gvsZBqcbyGMmcGRmV6sNlY",
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
            id = "reel_2_5",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746195327007.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjE5NTMyNzAwNy5tcDQiLCJpYXQiOjE3NDYyMDMwOTAsImV4cCI6MTc0ODc5NTA5MH0.wKci3nP6zSml_wnK1zj4sv96eoFOhEoPEsGch9iu3xI",
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746372752350.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjM3Mjc1MjM1MC5tcDQiLCJpYXQiOjE3NDYzNzI3ODgsImV4cCI6MTc0ODk2NDc4OH0.C5LfkfqFYAk1PLQXdm8HKlTRc1dK91NsUd2VdPb93-g",
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
            id = "reel_2_6",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746195284343.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjE5NTI4NDM0My5tcDQiLCJpYXQiOjE3NDYyMDMxMTksImV4cCI6MTc0ODc5NTExOX0.C3s8BbMvSJKhtZ-mxANP-N8D6s6vOmG36YFUItpxPwo",
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746372731253.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjM3MjczMTI1My5tcDQiLCJpYXQiOjE3NDYzNzI3NjksImV4cCI6MTc0ODk2NDc2OX0.1UJ_omCYHCd5aFby4KCt0gvsZBqcbyGMmcGRmV6sNlY",
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
            id = "reel_2_7",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746195253655.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjE5NTI1MzY1NS5tcDQiLCJpYXQiOjE3NDYxOTU0NzcsImV4cCI6MTc0ODc4NzQ3N30.xRF73r8Ppm8-__j6_u-pxP8KaWL_RWr_cGb5xIigC38",
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746372752350.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjM3Mjc1MjM1MC5tcDQiLCJpYXQiOjE3NDYzNzI3ODgsImV4cCI6MTc0ODk2NDc4OH0.C5LfkfqFYAk1PLQXdm8HKlTRc1dK91NsUd2VdPb93-g",
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
            id = "reel_2_8",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746195253655.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjE5NTI1MzY1NS5tcDQiLCJpYXQiOjE3NDYxOTU0ODUsImV4cCI6MTc0ODc4NzQ4NX0.vpONg7aHq0y8JQ58AzceSFGMy6n9686VTe5sfsZwcMY",
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746372731253.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjM3MjczMTI1My5tcDQiLCJpYXQiOjE3NDYzNzI3NjksImV4cCI6MTc0ODk2NDc2OX0.1UJ_omCYHCd5aFby4KCt0gvsZBqcbyGMmcGRmV6sNlY",
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
            id = "reel_2_9",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746195284343.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjE5NTI4NDM0My5tcDQiLCJpYXQiOjE3NDYxOTU1MTEsImV4cCI6MTc0ODc4NzUxMX0.eCZOMt1OuoI4eJgZ-oUwQLiN2QOZNUzTIJhykhF10l4",
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746372752350.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjM3Mjc1MjM1MC5tcDQiLCJpYXQiOjE3NDYzNzI3ODgsImV4cCI6MTc0ODk2NDc4OH0.C5LfkfqFYAk1PLQXdm8HKlTRc1dK91NsUd2VdPb93-g",
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
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746195327007.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjE5NTMyNzAwNy5tcDQiLCJpYXQiOjE3NDYxOTU1MjgsImV4cCI6MTc0ODc4NzUyOH0.Rt7xuxHWLxISjt8KK63rwnqoxsSQbDjmOYkghLvPB_4",
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746372731253.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjM3MjczMTI1My5tcDQiLCJpYXQiOjE3NDYzNzI3NjksImV4cCI6MTc0ODk2NDc2OX0.1UJ_omCYHCd5aFby4KCt0gvsZBqcbyGMmcGRmV6sNlY",
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
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746372752350.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjM3Mjc1MjM1MC5tcDQiLCJpYXQiOjE3NDYzNzI3ODgsImV4cCI6MTc0ODk2NDc4OH0.C5LfkfqFYAk1PLQXdm8HKlTRc1dK91NsUd2VdPb93-g",
            status = "DRAFT",
            likeCount = 0,
            commentCount = 0,
            shareCount = 0,
            viewCount = 0,
            createdAt = "2025-04-17T12:00:00Z",
            thumbnailUrl = "https://media1.tenor.com/m/Gi3YSm0uDK0AAAAd/random-lol.gif",
            tags = emptyList()
        ),
        Reel(
            id = "reel_5",
            videoUrl = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746372731253.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjM3MjczMTI1My5tcDQiLCJpYXQiOjE3NDYzNzI3NjksImV4cCI6MTc0ODk2NDc2OX0.1UJ_omCYHCd5aFby4KCt0gvsZBqcbyGMmcGRmV6sNlY",
            status = "DRAFT",
            likeCount = 0,
            commentCount = 0,
            shareCount = 0,
            viewCount = 0,
            createdAt = "2025-04-17T12:00:00Z",
            thumbnailUrl = "https://media1.tenor.com/m/Gi3YSm0uDK0AAAAd/random-lol.gif",
            tags = emptyList()
        ),
        Reel(
            id = "reel_6",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746372752350.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjM3Mjc1MjM1MC5tcDQiLCJpYXQiOjE3NDYzNzI3ODgsImV4cCI6MTc0ODk2NDc4OH0.C5LfkfqFYAk1PLQXdm8HKlTRc1dK91NsUd2VdPb93-g",
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
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746372731253.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjM3MjczMTI1My5tcDQiLCJpYXQiOjE3NDYzNzI3NjksImV4cCI6MTc0ODk2NDc2OX0.1UJ_omCYHCd5aFby4KCt0gvsZBqcbyGMmcGRmV6sNlY",
            status = "DRAFT",
            likeCount = 0,
            commentCount = 0,
            shareCount = 0,
            viewCount = 0,
            createdAt = "2025-04-17T12:00:00Z",
            thumbnailUrl = "https://media1.tenor.com/m/Gi3YSm0uDK0AAAAd/random-lol.gif",
            tags = emptyList()
        )
    )

    ReelList(
        topSystemBarHeight = topPadding,
        bottomNavBarHeight = bottomPadding,
        viewModel = reelPlaybackViewModel,
    )

}