Reel List Features Documentation
1. Video Playback with ExoPlayer

File: VideoPlayer.kt
Implemented video playback using ExoPlayer for each reel in a vertical pager.
Configured ExoPlayer with DefaultRenderersFactory to enable decoder fallback and disable asynchronous queueing.
Set video scaling mode to SCALE_TO_FIT and repeat mode to REPEAT_MODE_ALL.
Added listener for first frame rendering, player errors, video size changes, and playback state changes.

2. Pre-init and Preload Optimization

File: VideoPlayer.kt
Pre-initialized ExoPlayer for the current and next page to reduce playback delay.
Preloaded MediaSource for up to 5 upcoming pages in a background thread pool (Executors.newFixedThreadPool(2)).
Avoided duplicate MediaSource creation by tracking preloaded sources in createdMediaSources.

3. Resource Management

File: VideoPlayer.kt
Managed ExoPlayer lifecycle: pause/stop/release players for non-visible pages.
Disposed ExoPlayer instances for pages outside a range of ±2 from the current page with a 500ms debounce.
Used setPriority to prioritize playback for the current page (C.PRIORITY_PLAYBACK) and preloading for others (C.PRIORITY_PLAYBACK_PRELOAD).

4. Caching with MediaCache

File: MediaCache.kt
Implemented a SimpleCache with a 200MB limit using LeastRecentlyUsedCacheEvictor.
Added cache clearing logic based on age (1 day) and usage state.
Configured CacheDataSource with flags FLAG_BLOCK_ON_CACHE and FLAG_IGNORE_CACHE_ON_ERROR for better cache performance.

5. ViewModel for Reel Data

File: ReelPlaybackViewModel.kt
Managed reel data with a State<List<Reel>> in the ViewModel.
Provided setReels and fetchMoreReels to update and append reel data.
Created MediaSource for videos, supporting both HLS (.m3u8) and progressive streams with caching.

6. Vertical Pager for Reel Navigation

File: ReelList.kt
Used VerticalPager for scrolling through reels with a custom fling behavior.
Configured fling animation with FastOutSlowInEasing, 250ms duration, and snap threshold of 0.35f.
Triggered fetchMoreReels when nearing the end of the list (every 3rd page from the end).

7. Thumbnail Display

File: VideoPlayer.kt
Displayed a thumbnail (AsyncImage) until the first frame of the video is rendered.
Hid the thumbnail when the video starts playing on the current page.

8. Lifecycle Handling

File: VideoPlayer.kt
Paused/resumed ExoPlayer based on lifecycle events (ON_STOP, ON_START) for the active page.
Released ExoPlayer instances when the composable is disposed.

9. User Interaction

File: VideoPlayer.kt, ReelList.kt
Added tap gesture to toggle play/pause state of the video.
Kept screen on during playback using PlayerView.keepScreenOn.

