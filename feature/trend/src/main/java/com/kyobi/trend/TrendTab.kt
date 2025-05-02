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
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1744549928872.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NDU0OTkyODg3Mi5tcDQiLCJpYXQiOjE3NDYwMTkxNDEsImV4cCI6MTc0ODYxMTE0MX0.j_cW8FqxXWFFCGbJA4yBumOCuEdpummpunChJ5pjfKM",
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
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746194918762.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjE5NDkxODc2Mi5tcDQiLCJpYXQiOjE3NDYxOTUzODAsImV4cCI6MTc0ODc4NzM4MH0.YiKTG3sFBpv9zWqKJ4wyFVpGmCUs7xJdEpoRlfR36wg",
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
            id = "reel_2_1",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746194918762.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjE5NDkxODc2Mi5tcDQiLCJpYXQiOjE3NDYxOTU0MTEsImV4cCI6MTc0ODc4NzQxMX0.vr-kODKaKsWtk-ReFjI51lkoM08PaxCMYPpSvWCBO_E",
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
            id = "reel_2_2",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746194998454.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjE5NDk5ODQ1NC5tcDQiLCJpYXQiOjE3NDYxOTU0MjUsImV4cCI6MTc0ODc4NzQyNX0.JjdfjCGqwg0ZHO3sMmjyZAmFx4gzgs47S0_hEL2iJMA",
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
            id = "reel_2_3",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746195202763.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjE5NTIwMjc2My5tcDQiLCJpYXQiOjE3NDYxOTU0NDAsImV4cCI6MTc0ODc4NzQ0MH0.bWSOGNk7fbgdRuWwdeC9RyiGDiweaiwNibNAf_dEfS4",
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
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746195253655.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjE5NTI1MzY1NS5tcDQiLCJpYXQiOjE3NDYxOTU0NjAsImV4cCI6MTc0ODc4NzQ2MH0.HH0IwxmCvqSDSvyXnloEYpIjoVJCXBlX9KY9PA8Re-k",
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
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746195253655.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjE5NTI1MzY1NS5tcDQiLCJpYXQiOjE3NDYxOTU0NjgsImV4cCI6MTc0ODc4NzQ2OH0.WNha9wGciQ7bABf9F_4OFu_4I51cd7uAV3dPpLxrM4Q",
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