package com.kyobi.trend.ui

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import com.kyobi.trend.cache.MediaCache
import com.kyobi.trend.model.Reel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.DefaultHlsExtractorFactory
import androidx.media3.exoplayer.source.ConcatenatingMediaSource2
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class ReelPlaybackViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaCache: MediaCache,
) : ViewModel() {
    private val tag = "ReelPlaybackViewModel"
    private val _reels = mutableStateOf<List<Reel>>(emptyList())
    val reels: State<List<Reel>> = _reels
    private val _isFetching = mutableStateOf(false)
    val isFetching: State<Boolean> = _isFetching
    private val _mediaSources = mutableMapOf<String, MediaSource>()
    private var mainExoPlayer: ExoPlayer? = null
    private var backgroundExoPlayer: ExoPlayer? = null
    private var currentSettledPage = 0
    private val _firstFrameRendered = MutableStateFlow(-1) // -1: chưa render
    val firstFrameRendered = _firstFrameRendered.asStateFlow()

    private val mockData = listOf(
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

    init {
        setReels(mockData)
    }

    @OptIn(UnstableApi::class)
    fun initializeMainPlayer(mediaSources: List<MediaSource>) {
        if (mainExoPlayer != null) return
        val renderersFactory = DefaultRenderersFactory(context)
            .setEnableDecoderFallback(false)
            .forceDisableMediaCodecAsynchronousQueueing()
        val cacheDataSourceFactory = mediaCache.getMediaSourceFactory(shouldCache = true)
        mainExoPlayer = ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        10000, // minBufferMs: 10s, đủ preload shorten + full
                        30000, // maxBufferMs: 30s, preload vài page kế tiếp
                        2000,  // bufferForPlaybackMs: 2s, bắt đầu play nhanh
                        2000   // bufferForPlaybackAfterRebufferMs: 2s, ổn định sau rebuffer
                    )
                    .setTargetBufferBytes(-1)
                    .build()
            )
            .setMediaSourceFactory(cacheDataSourceFactory)
            .build().apply {
                if (mediaSources.isNotEmpty()) {
                    setMediaSources(mediaSources, 0, 0)
                    seekTo(0, 0)
                    repeatMode = Player.REPEAT_MODE_ONE
                    videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                    volume = 1f
                    playWhenReady = false
                }
                addListener(object : Player.Listener {
                    override fun onRenderedFirstFrame() {
                        Timber.tag(tag).d("First frame rendered for page $currentSettledPage")
                        _firstFrameRendered.value = currentSettledPage
                    }
                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                    ) {
                        if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                            Timber.tag(tag).d("Auto transition detected at page $currentSettledPage, periodIndex: ${newPosition.periodIndex}")
                            // Page hiện tại: shorten = 2 * page, full = 2 * page + 1
                            val fullPeriodIndex = 2 * currentSettledPage + 1 // Period của fullMediaSource
                            if (newPosition.periodIndex > fullPeriodIndex) {
                                Timber.tag(tag).d("Looping back to page $currentSettledPage")
                            }
                        }
                    }
                    override fun onPlayerError(error: PlaybackException) {
                        Timber.tag(tag).e(error, "Player error for page $currentSettledPage")
                    }
                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        Timber.tag(tag).d("Video size changed for page $currentSettledPage: ${videoSize.width}x${videoSize.height}")
                    }
                    override fun onPlaybackStateChanged(state: Int) {
                        Timber.tag(tag).d("Playback state changed for page $currentSettledPage: $state")
                    }
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        Timber.tag(tag).d("Media item transition to mediaId ${mediaItem?.mediaId} for page $currentSettledPage")
                    }
                    override fun onAudioAttributesChanged(audioAttributes: AudioAttributes) {
                        Timber.tag(tag).d("Audio attributes changed for page $currentSettledPage: contentType=${audioAttributes.contentType}, usage=${audioAttributes.usage}, flags=${audioAttributes.flags}")
                    }
                    override fun onAudioSessionIdChanged(audioSessionId: Int) {
                        Timber.tag(tag).d("Audio session ID changed for page $currentSettledPage: audioSessionId=$audioSessionId")
                    }
                    override fun onVolumeChanged(volume: Float) {
                        Timber.tag(tag).d("Volume changed for page $currentSettledPage: volume=$volume")
                    }
                    override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
                        Timber.tag(tag).d("Device volume changed for page $currentSettledPage: volume=$volume, muted=$muted")
                    }
                })
            }
        Timber.tag(tag).d("Initialized single ExoPlayer instance")
    }

    @OptIn(UnstableApi::class)
    private fun initializeBackgroundPlayer(mediaSources: List<MediaSource>) {
        if (backgroundExoPlayer != null) return
        val renderersFactory = DefaultRenderersFactory(context)
            .setEnableDecoderFallback(false)
            .forceDisableMediaCodecAsynchronousQueueing()
        val cacheDataSourceFactory = mediaCache.getMediaSourceFactory(shouldCache = true)
        backgroundExoPlayer = ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        10000, // minBufferMs: 10s, đủ preload shorten + full
                        30000, // maxBufferMs: 30s, preload vài page kế tiếp
                        2000,  // bufferForPlaybackMs: 2s, bắt đầu play nhanh
                        2000   // bufferForPlaybackAfterRebufferMs: 2s, ổn định sau rebuffer
                    )
                    .setTargetBufferBytes(-1)
                    .build()
            )
            .setMediaSourceFactory(cacheDataSourceFactory)
            .build().apply {
                if (mediaSources.isNotEmpty()) {
                    setMediaSources(mediaSources)
                    volume = 0f
                    for (page in mediaSources.indices) {
                        seekTo(page, 0)
                        prepare()
                        playWhenReady = true // Deep play để buffer
                        Timber.tag(tag).d("Background play for page $page")
                        Thread.sleep(2000)
                        playWhenReady = false // Tạm dừng sau 2s
                        Timber.tag(tag).d("Background pause for page $page")
                    }
                }
            }
        Timber.tag(tag).d("Initialized background ExoPlayer instance")
    }

    // Lấy ExoPlayer
    fun getPlayer(): ExoPlayer? = mainExoPlayer

    @OptIn(UnstableApi::class)
    fun setReels(newReels: List<Reel>) {
        Timber.tag(tag).d("Setting reels, size: ${newReels.size}")
        _reels.value = newReels
        preloadMediaSourceForRange(0, newReels.size) // Preload trước
        // Tạo danh sách shortenSources cho background player
        val shortenSources = newReels.mapIndexed { _, reel ->
            val shortenMediaSource = _mediaSources[reel.shortenUrl]
                ?: throw IllegalStateException("Shorten MediaSource for ${reel.shortenUrl} not preloaded")
            shortenMediaSource
        }
        // Tạo danh sách mergedSources cho main player
        val mergedSources = newReels.mapIndexed { index, reel ->
            val shortenMediaSource = _mediaSources[reel.shortenUrl]
                ?: throw IllegalStateException("Shorten MediaSource for ${reel.shortenUrl} not preloaded")
            val fullMediaSource = _mediaSources[reel.videoUrl]
                ?: throw IllegalStateException("Full MediaSource for ${reel.videoUrl} not preloaded")
            try {
                ConcatenatingMediaSource2.Builder()
                    .add(shortenMediaSource, 10_000L)
                    .add(fullMediaSource, 180_000L)
                    .build().also {
                        Timber.tag(tag).d("ConcatenatingMediaSource2 created for page $index")
                    }
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Failed to create ConcatenatingMediaSource2 for page $index")
                throw e
            }
        }
        initializeMainPlayer(mergedSources)
        initializeBackgroundPlayer(shortenSources)
        Timber.tag(tag).d("Preloaded and set ${newReels.size} media sources")
    }

    fun fetchMoreReels() {
        if (_isFetching.value) return
        _isFetching.value = true
        // Làm sau
        _isFetching.value = false
    }

    @OptIn(UnstableApi::class)
    fun updateSettledPage(page: Int, playerView: PlayerView) {
        currentSettledPage = page
        playerView.player = mainExoPlayer
    }

    private fun preloadMediaSourceForRange(startPage: Int, endPage: Int) {
        for (page in startPage until endPage) {
            if (page < _reels.value.size) {
                preloadShortenAndFullMediaSources(page)
            }
        }
    }

    private fun preloadShortenAndFullMediaSources(page: Int) {
        if (page >= _reels.value.size) return
        val reel = _reels.value[page]
        // Preload shortenUrl
        if (reel.shortenUrl.isNotEmpty()) {
            try {
                val mediaItem = MediaItem.fromUri(reel.shortenUrl).buildUpon()
                    .setMediaId(reel.shortenUrl).build()
                val source = startCreateMediaSource(mediaItem, shouldCache = true)
                _mediaSources[reel.shortenUrl] = source
                Timber.tag(tag).d("Preloaded shortenUrl MediaSource for page $page")
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Failed to preload shortenUrl MediaSource for page $page")
            }
        }
        // Preload videoUrl
        if (reel.videoUrl.isNotEmpty()) {
            try {
                val mediaItem = MediaItem.fromUri(reel.videoUrl).buildUpon()
                    .setMediaId(reel.videoUrl).build()
                val source = startCreateMediaSource(mediaItem, shouldCache = false)
                _mediaSources[reel.videoUrl] = source
                Timber.tag(tag).d("Preloaded videoUrl MediaSource for page $page")
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Failed to preload videoUrl MediaSource for page $page")
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun startCreateMediaSource(mediaItem: MediaItem, shouldCache: Boolean = true): MediaSource {
        try {
            val uri = mediaItem.localConfiguration?.uri.toString()
            Timber.tag(tag).d("Creating MediaSource for URI: $uri")
            // Chọn DataSource.Factory dựa trên shouldCache
            val dataSourceFactory = if (shouldCache) {
                mediaCache.createSharedCacheDataSourceFactory(context, mediaCache.getCache())
            } else {
                mediaCache.createNonCachedDataSourceFactory(context)
            }
            val path = uri.toUri().path ?: ""
            return if (path.endsWith(".m3u8")) {
                // Tạo DefaultHlsExtractorFactory và cấu hình
                val customExtractorFactory = DefaultHlsExtractorFactory().apply {
                    // Tắt kiểm tra codec không cần thiết, Không parse codec nào
                    experimentalSetCodecsToParseWithinGopSampleDependencies(0)
                    // Tùy chỉnh codec để parse sample dependencies Chỉ parse H.264 (tăng tốc seeking)
                    experimentalSetCodecsToParseWithinGopSampleDependencies(C.VIDEO_CODEC_FLAG_H264)
                }
                HlsMediaSource.Factory(dataSourceFactory)
                    .setAllowChunklessPreparation(true)
                    .setExtractorFactory(customExtractorFactory)
                    .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(3))
                    .createMediaSource(mediaItem)
            } else {
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(3))
                    .createMediaSource(mediaItem)
            }
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Failed to create mediaSource")
            throw e
        }
    }

    @OptIn(UnstableApi::class)
    fun startPlay(page: Int, playerView: PlayerView) {
        mainExoPlayer?.let { player ->
            player.seekTo(page, 0) // very important. giup giam first frame render
            player.prepare()
            player.playWhenReady = true
            Timber.tag(tag).d("Playing ExoPlayer for init page greater than 0")
        }
        playerView.player = mainExoPlayer
    }

    @OptIn(UnstableApi::class)
    fun startPause(page: Int, playerView: PlayerView) {
        mainExoPlayer?.let { player ->
            player.playWhenReady = false
            Timber.tag(tag).d("Paused ExoPlayer for page $page")
        }
        playerView.player = mainExoPlayer
    }

    private fun startMainRelease() {
        mainExoPlayer?.let { player ->
            player.seekTo(0)
            player.playWhenReady = false
            player.stop()
            player.clearMediaItems()
            player.release()
            Timber.tag(tag).d("Releasing Main ExoPlayer")
        }
    }

    private fun startBackgroundRelease() {
        backgroundExoPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            player.release()
            Timber.tag(tag).d("Releasing Background ExoPlayer")
        }
    }

    override fun onCleared() {
        Timber.tag(tag).d("ViewModel cleared, releasing resources")
        startMainRelease()
        startBackgroundRelease()
        super.onCleared()
    }
}