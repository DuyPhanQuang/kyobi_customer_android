package com.kyobi.trend

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

    val mockData = listOf(
        Reel(
            id = "reel_1",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1744549928872.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NDU0OTkyODg3Mi5tcDQiLCJpYXQiOjE3NDYwMTkxNDEsImV4cCI6MTc0ODYxMTE0MX0.j_cW8FqxXWFFCGbJA4yBumOCuEdpummpunChJ5pjfKM",
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746372731253.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjM3MjczMTI1My5tcDQiLCJpYXQiOjE3NDYzNzI3NjksImV4cCI6MTc0ODk2NDc2OX0.1UJ_omCYHCd5aFby4KCt0gvsZBqcbyGMmcGRmV6sNlY",
            status = "PUBLISHED",
            likeCount = 1200,
            commentCount = 150,
            shareCount = 50,
            viewCount = 5000,
            createdAt = "2025-04-20T10:00:00Z",
            thumbnailUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/thumbnails/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/reel-1746444377793-thumbnail.webp?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJ0aHVtYm5haWxzLzQ2OGI0ZDM2LTRkNDMtNDhmMy04ZjMyLTBkNzk2ZTJiMmVjZC9yZWVsLTE3NDY0NDQzNzc3OTMtdGh1bWJuYWlsLndlYnAiLCJpYXQiOjE3NDY0NDQzNzksImV4cCI6MTc3Nzk4MDM3OX0.OBtARv4roCRe02jgq2Vc2krZ5NBpnNMwd5pqyEgIsdU",
            tags = listOf("fashion", "trend")
        ),
        Reel(
            id = "reel_1_-1",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/reel-1746523286632/full/full.m3u8?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvcmVlbC0xNzQ2NTIzMjg2NjMyL2Z1bGwvZnVsbC5tM3U4IiwiaWF0IjoxNzQ2NTIzMzIxLCJleHAiOjE3NzgwNTkzMjF9.KaS-YLFEmCs0RC3HPRTtq3ENpQ62xhcvtuhBBFkBupA",
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/reel-1746523286632/shorten/shorten.m3u8?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvcmVlbC0xNzQ2NTIzMjg2NjMyL3Nob3J0ZW4vc2hvcnRlbi5tM3U4IiwiaWF0IjoxNzQ2NTIzMzIxLCJleHAiOjE3NzgwNTkzMjF9.0CBRkGfBSX-TBkZlppuLwwR_yL63jL0uskwPbturg2M",
            status = "PUBLISHED",
            likeCount = 1200,
            commentCount = 150,
            shareCount = 50,
            viewCount = 5000,
            createdAt = "2025-04-20T10:00:00Z",
            thumbnailUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/thumbnails/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/reel-1746523286632-thumbnail.webp?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJ0aHVtYm5haWxzLzQ2OGI0ZDM2LTRkNDMtNDhmMy04ZjMyLTBkNzk2ZTJiMmVjZC9yZWVsLTE3NDY1MjMyODY2MzItdGh1bWJuYWlsLndlYnAiLCJpYXQiOjE3NDY1MjMyODksImV4cCI6MTc3ODA1OTI4OX0.ltQiG3CdyYG_S4ZKYgJA7tN9hBCAJMB42RDDgnAlhsw",
            tags = listOf("fashion", "trend")
        ),
        Reel(
            id = "reel_1_0_hls",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/reel-1746462876876/full/full.m3u8?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvcmVlbC0xNzQ2NDYyODc2ODc2L2Z1bGwvZnVsbC5tM3U4IiwiaWF0IjoxNzQ2NDYyOTAxLCJleHAiOjE3Nzc5OTg5MDF9.5Cx_la0TwUfIH-XagoeIZX9T3JQM3wrmyDHSsyi8cgc",
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/reel-1746462876876/shorten/shorten.m3u8?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvcmVlbC0xNzQ2NDYyODc2ODc2L3Nob3J0ZW4vc2hvcnRlbi5tM3U4IiwiaWF0IjoxNzQ2NDYyOTAxLCJleHAiOjE3Nzc5OTg5MDF9.TEZIE47UO6ETinbUUXP8g69_RiLUJaDUJI4uC8mFE7w",
            status = "PUBLISHED",
            likeCount = 1200,
            commentCount = 150,
            shareCount = 50,
            viewCount = 5000,
            createdAt = "2025-04-20T10:00:00Z",
            thumbnailUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/thumbnails/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/reel-1746462876876-thumbnail.webp?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJ0aHVtYm5haWxzLzQ2OGI0ZDM2LTRkNDMtNDhmMy04ZjMyLTBkNzk2ZTJiMmVjZC9yZWVsLTE3NDY0NjI4NzY4NzYtdGh1bWJuYWlsLndlYnAiLCJpYXQiOjE3NDY0NjI4NzgsImV4cCI6MTc3Nzk5ODg3OH0.JcjryYU9Rk7RNTsibMndfMxDu48YSF26Pg5ey-lojEU",
            tags = listOf("fashion", "trend")
        ),
        Reel(
            id = "reel_1_1_hls",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/reel-1746444377793/full/full.m3u8?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvcmVlbC0xNzQ2NDQ0Mzc3NzkzL2Z1bGwvZnVsbC5tM3U4IiwiaWF0IjoxNzQ2NDQ0Mzk3LCJleHAiOjE3Nzc5ODAzOTd9.QGT0ceFcYim7vnY2iUOM0X-xKoiYxBtHkzSFFdGBxKM",
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/reel-1746444377793/shorten/shorten.m3u8?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvcmVlbC0xNzQ2NDQ0Mzc3NzkzL3Nob3J0ZW4vc2hvcnRlbi5tM3U4IiwiaWF0IjoxNzQ2NDQ0Mzk3LCJleHAiOjE3Nzc5ODAzOTd9.czfpkVJT-8BkLdauW0B7h8kAy6vEx5hNSM9gZrIDbtg",
            status = "PUBLISHED",
            likeCount = 1200,
            commentCount = 150,
            shareCount = 50,
            viewCount = 5000,
            createdAt = "2025-04-20T10:00:00Z",
            thumbnailUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/thumbnails/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/reel-1746444377793-thumbnail.webp?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJ0aHVtYm5haWxzLzQ2OGI0ZDM2LTRkNDMtNDhmMy04ZjMyLTBkNzk2ZTJiMmVjZC9yZWVsLTE3NDY0NDQzNzc3OTMtdGh1bWJuYWlsLndlYnAiLCJpYXQiOjE3NDY0NDQzNzksImV4cCI6MTc3Nzk4MDM3OX0.OBtARv4roCRe02jgq2Vc2krZ5NBpnNMwd5pqyEgIsdU",
            tags = listOf("fashion", "trend")
        ),
        Reel(
            id = "reel_1_2_hls",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/reel-1746449182211/full/full.m3u8?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvcmVlbC0xNzQ2NDQ5MTgyMjExL2Z1bGwvZnVsbC5tM3U4IiwiaWF0IjoxNzQ2NDQ5MTg3LCJleHAiOjE3Nzc5ODUxODd9.-RWzsW4dzlnOJLF2GWl3AT_yTFuQBYSgXvBXcy-jC50",
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/reel-1746449182211/shorten/shorten.m3u8?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvcmVlbC0xNzQ2NDQ5MTgyMjExL3Nob3J0ZW4vc2hvcnRlbi5tM3U4IiwiaWF0IjoxNzQ2NDQ5MTg3LCJleHAiOjE3Nzc5ODUxODd9.GyM7DXgfp0BkpC15ou6eOqV_xP3W90QIcSUc9waJCnk",
            status = "PUBLISHED",
            likeCount = 1200,
            commentCount = 150,
            shareCount = 50,
            viewCount = 5000,
            createdAt = "2025-04-20T10:00:00Z",
            thumbnailUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/thumbnails/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/reel-1746449182211-thumbnail.webp?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJ0aHVtYm5haWxzLzQ2OGI0ZDM2LTRkNDMtNDhmMy04ZjMyLTBkNzk2ZTJiMmVjZC9yZWVsLTE3NDY0NDkxODIyMTEtdGh1bWJuYWlsLndlYnAiLCJpYXQiOjE3NDY0NDkxODQsImV4cCI6MTc3Nzk4NTE4NH0.Rui-58Itl-qRjtNVeLf-4vc9QnLdfzjJuxRRT4dw728",
            tags = listOf("fashion", "trend")
        ),
        Reel(
            id = "reel_1_3_hls",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/reel-1746449369113/full/full.m3u8?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvcmVlbC0xNzQ2NDQ5MzY5MTEzL2Z1bGwvZnVsbC5tM3U4IiwiaWF0IjoxNzQ2NDQ5Mzk5LCJleHAiOjE3Nzc5ODUzOTl9.T1k6wHtcTZnwN6h6qQV0yDGBH3wwQs77ddWwn9_yvAs",
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/reel-1746449369113/shorten/shorten.m3u8?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvcmVlbC0xNzQ2NDQ5MzY5MTEzL3Nob3J0ZW4vc2hvcnRlbi5tM3U4IiwiaWF0IjoxNzQ2NDQ5Mzk5LCJleHAiOjE3Nzc5ODUzOTl9.xBNYrUOoH5SGFfIqXxmsTXzNuP8XRjDN-gilSp8Ec-g",
            status = "PUBLISHED",
            likeCount = 1200,
            commentCount = 150,
            shareCount = 50,
            viewCount = 5000,
            createdAt = "2025-04-20T10:00:00Z",
            thumbnailUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/thumbnails/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/reel-1746449369113-thumbnail.webp?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJ0aHVtYm5haWxzLzQ2OGI0ZDM2LTRkNDMtNDhmMy04ZjMyLTBkNzk2ZTJiMmVjZC9yZWVsLTE3NDY0NDkzNjkxMTMtdGh1bWJuYWlsLndlYnAiLCJpYXQiOjE3NDY0NDkzNzAsImV4cCI6MTc3Nzk4NTM3MH0.1gt8V2nvmz9-KtsfVKAvJmlrprVCFKK2RSd8OveaHAI",
            tags = listOf("fashion", "trend")
        ),
        Reel(
            id = "reel_1_4_hls",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/reel-1746449295660/full/full.m3u8?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvcmVlbC0xNzQ2NDQ5Mjk1NjYwL2Z1bGwvZnVsbC5tM3U4IiwiaWF0IjoxNzQ2NDQ5MzAzLCJleHAiOjE3Nzc5ODUzMDN9.ydpwnd8hMNKPd_JzazvhyR8-LN0y8zWfiTg5HR9c2lk",
            shortenUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/reel-1746449295660/shorten/shorten.m3u8?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvcmVlbC0xNzQ2NDQ5Mjk1NjYwL3Nob3J0ZW4vc2hvcnRlbi5tM3U4IiwiaWF0IjoxNzQ2NDQ5MzAzLCJleHAiOjE3Nzc5ODUzMDN9.WGtzf2JD0E79B72OLv7zkffAmEeOh5QcvfVc4c20ZqY",
            status = "PUBLISHED",
            likeCount = 1200,
            commentCount = 150,
            shareCount = 50,
            viewCount = 5000,
            createdAt = "2025-04-20T10:00:00Z",
            thumbnailUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/thumbnails/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/reel-1746449295660-thumbnail.webp?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJ0aHVtYm5haWxzLzQ2OGI0ZDM2LTRkNDMtNDhmMy04ZjMyLTBkNzk2ZTJiMmVjZC9yZWVsLTE3NDY0NDkyOTU2NjAtdGh1bWJuYWlsLndlYnAiLCJpYXQiOjE3NDY0NDkyOTYsImV4cCI6MTc3Nzk4NTI5Nn0.TOVBk-xaxV067RRodXtKS7jeGzEEqeW9Ahii4QLbIaY",
            tags = listOf("fashion", "trend")
        ),
        Reel(
            id = "reel_2",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746194918762.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjE5NDkxODc2Mi5tcDQiLCJpYXQiOjE3NDYxOTUzODAsImV4cCI6MTc0ODc4NzM4MH0.YiKTG3sFBpv9zWqKJ4wyFVpGmCUs7xJdEpoRlfR36wg",
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
            id = "reel_2_1",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1744549928872.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NDU0OTkyODg3Mi5tcDQiLCJpYXQiOjE3NDYzNDA0MDcsImV4cCI6MTc0ODkzMjQwN30.uJl1iedItiAdWgaEpHrEK8hUDhWPq55Hf0YH3SIqfKM",
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
            id = "reel_2_2",
            videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1746194998454.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NjE5NDk5ODQ1NC5tcDQiLCJpYXQiOjE3NDYxOTU0MjUsImV4cCI6MTc0ODc4NzQyNX0.JjdfjCGqwg0ZHO3sMmjyZAmFx4gzgs47S0_hEL2iJMA",
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
    )

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
            tags = null
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
            tags = null
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
            tags = null
        )
    )

    LaunchedEffect(Unit) {
        reelPlaybackViewModel.setReels(mockData)
    }

    ReelList(
        initReels = mockData,
        moreReels = mockMoreData,
        topSystemBarHeight = topPadding,
        bottomNavBarHeight = bottomPadding,
        viewModel = reelPlaybackViewModel,
    )

}