# Reel Playback Technical Documentation

## Overview
This document provides a detailed explanation of the reel playback feature in the Kyobi app, focusing on the implementation of video playback, preloading, and resource management. The feature is primarily implemented across three main components: `ReelPlaybackViewModel`, `ReelList`, and `ReelAdapter`. These components work together to ensure smooth video playback in a vertical `ViewPager2`, supporting scenarios with and without network connectivity, efficient resource management, and surface readiness handling.

### Components
- **ReelPlaybackViewModel**: Manages the business logic for video playback, preloading, and player lifecycle.
- **ReelList**: A Jetpack Compose UI component that sets up the `ViewPager2` for displaying reels and coordinates playback with the ViewModel.
- **ReelAdapter**: A `RecyclerView.Adapter` used by `ViewPager2` to bind reel data to views and initialize `ExoPlayer` instances.

## Features and Implementation Details

### 1. Video Playback
#### Description
The app supports playing videos in a vertical scrolling list using `ViewPager2`. Only one video plays at a time, and playback is triggered when a reel becomes the currently selected page.

#### Implementation
- **ReelList**:
  - Uses a `ViewPager2` to display a vertical list of reels.
  - Registers an `OnPageChangeCallback` to detect page selection:
    ```kotlin
    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            // Trigger playback for the selected position
            playbackViewModel.playVideoAtPosition(position, player, isSurfaceReady)
        }
    })
    ```
  - Maintains a `pendingPlayPositions` set to queue playback requests for positions where the player is not yet ready:
    ```kotlin
    if (it.player != null) {
        playbackViewModel.playVideoAtPosition(position, it.player!!, it.isSurfaceReady)
        pendingPlayPositions.remove(position)
    } else {
        pendingPlayPositions.add(position)
    }
    ```
  - Ensures the first reel (position 0) is played when the list is initialized, if no video is currently playing.

- **ReelPlaybackViewModel**:
  - The `playVideoAtPosition` function handles the playback logic:
    ```kotlin
    fun playVideoAtPosition(position: Int, player: ExoPlayer, isSurfaceReady: Boolean) {
        playLock.lock()
        try {
            // Pause all other players
            activePlayers.forEach { (pos, p) ->
                if (pos != position && (p.isPlaying || p.playbackState == Player.STATE_READY)) {
                    p.pause()
                    p.volume = 0f
                }
            }
            activePlayers[position] = player
            surfaceReadyStates[position] = isSurfaceReady
            currentPlayingPosition = position
            preloadMediaItemsAroundPosition(position)
            managePlayersAroundPosition(position)
            playVideoAtPositionInternal(position, player)
        } finally {
            playLock.unlock()
        }
    }
    ```
  - Uses a `ReentrantLock` (`playLock`) to ensure thread safety during playback operations.
  - Calls `playVideoAtPositionInternal` to prepare and play the video, bypassing surface readiness checks to avoid delays (ExoPlayer will wait for the surface internally):
    ```kotlin
    private fun playVideoAtPositionInternal(position: Int, player: ExoPlayer) {
        val mediaItem = preparedMediaItems[position]
        if (mediaItem != null) {
            player.clearMediaItems()
            val mediaSource = startCreateMediaSource(mediaItem)
            player.setMediaSource(mediaSource)
            player.prepare()
            player.volume = 1f
            player.repeatMode = Player.REPEAT_MODE_ONE
            player.play()
        }
    }
    ```

- **ReelAdapter**:
  - Initializes an `ExoPlayer` instance for each reel in `onBindViewHolder`:
    ```kotlin
    coroutineScope.launch(Dispatchers.Default) {
        val player = ExoPlayer.Builder(context).build()
        withContext(Dispatchers.Main) {
            holder.player = player
            holder.playerView.player = player
            player.prepare()
            holder.playerView.requestLayout()
            holder.playerView.invalidate()
            onPlayerReady(position, player, holder.isSurfaceReady)
        }
    }
    ```
  - Notifies `ReelList` when the player is ready via the `onPlayerReady` callback, allowing queued playback to proceed.

#### Key Points
- Playback is triggered automatically when a page is selected.
- Only one video plays at a time; other players are paused.
- Playback does not wait for surface readiness, reducing delays (ExoPlayer handles surface availability internally).

---

### 2. Preloading Videos
#### Description
To reduce loading times, videos are preloaded in the background for positions near the currently playing reel. Preloading is skipped when there is no network connectivity.

#### Implementation
- **ReelPlaybackViewModel**:
  - The `preloadMediaItemsAroundPosition` function preloads videos within a range around the current position:
    ```kotlin
    private fun preloadMediaItemsAroundPosition(position: Int) {
        if (!networkMonitor.isConnected.value) {
            return
        }
        val start = maxOf(0, position - positionsToKeepRange)
        val end = minOf(reels.size - 1, position + positionsToKeepRange)
        coroutineScope.launch {
            for (index in start..end) {
                if (preparedMediaItems.containsKey(index)) continue
                val reel = reels[index]
                val mediaItem = MediaItem.fromUri(reel.videoUrl.toUri()).buildUpon()
                    .setMediaId(reel.videoUrl).build()
                preparedMediaItems[index] = mediaItem
                val mediaSource = startCreateMediaSource(mediaItem)
                withContext(Dispatchers.Main) {
                    preloadVideoDataIntoCache(mediaSource, index)
                }
            }
        }
    }
    ```
  - Uses a `SimpleCache` (via `MediaCache`) to cache video data:
    ```kotlin
    private fun startCreateMediaSource(mediaItem: MediaItem): MediaSource {
        val cache = mediaCache.getCache()
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context))
            .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE)
        return ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(mediaItem)
    }
    ```
  - Preloading runs on a Background thread (`Dispatchers.Default`), but the actual `ExoPlayer` preparation (`preloadVideoDataIntoCache`) must run on the Main thread to avoid "wrong thread" errors from ExoPlayer.

- **MediaCache**:
  - Manages a `SimpleCache` with a size limit of 200MB:
    ```kotlin
    cache = SimpleCache(
        cacheDir,
        LeastRecentlyUsedCacheEvictor(cacheSizeMb * 1024 * 1024L),
        StandaloneDatabaseProvider(context)
    )
    ```
  - Automatically clears old cache data if it exceeds 1 day.

#### Key Points
- Preloading occurs for positions within `positionsToKeepRange` (default: 2) from the current position.
- Skips preloading if there is no network, but `MediaItem` objects are still created for playback readiness.
- Uses `SimpleCache` to store preloaded data, improving playback performance.

---

### 3. Handling Network Changes
#### Description
The app supports playback in offline scenarios by creating `MediaItem` objects even when there is no network. When the network is restored, preloading resumes, and the current video is replayed if it failed to play.

#### Implementation
- **ReelPlaybackViewModel**:
  - Creates `MediaItem` objects in `setReels` even without a network:
    ```kotlin
    coroutineScope.launch {
        for (index in 0 until minOf(reels.size, positionsToKeepRange * 2 + 1)) {
            if (!preparedMediaItems.containsKey(index)) {
                val reel = reels[index]
                val mediaItem = MediaItem.fromUri(reel.videoUrl.toUri()).buildUpon()
                    .setMediaId(reel.videoUrl).build()
                preparedMediaItems[index] = mediaItem
            }
        }
    }
    ```
  - Listens for network changes using `NetworkMonitor`:
    ```kotlin
    viewModelScope.launch {
        networkMonitor.isConnected.collectLatest { isConnected ->
            if (isConnected && currentPlayingPosition >= 0) {
                preloadMediaItemsAroundPosition(currentPlayingPosition)
                checkAndReplayVideoAfterNetworkRestored()
            }
        }
    }
    ```
  - Replays the current video when the network is restored:
    ```kotlin
    private fun checkAndReplayVideoAfterNetworkRestored() {
        playLock.lock()
        try {
            activePlayers[currentPlayingPosition]?.let { player ->
                playVideoAtPositionInternal(currentPlayingPosition, player)
            }
        } finally {
            playLock.unlock()
        }
    }
    ```

#### Key Points
- Ensures `MediaItem` objects are always available, allowing playback attempts even without a network (though ExoPlayer may throw errors if data is not cached).
- Automatically resumes preloading and playback when the network is restored.

---

### 4. Surface Readiness Handling
#### Description
The app handles cases where the `PlayerView` surface is not ready by attempting playback immediately and replaying when the surface becomes available.

#### Implementation
- **ReelAdapter**:
  - Listens for surface changes using `onSurfaceSizeChanged`:
    ```kotlin
    player?.addListener(object : Player.Listener {
        override fun onSurfaceSizeChanged(width: Int, height: Int) {
            if (width > 0 && height > 0) {
                isSurfaceReady = true
                playbackViewModel.updateSurfaceReadyState(bindingAdapterPosition, true)
            }
        }
    })
    ```
  - Forces surface creation by calling `player.prepare()`, `requestLayout()`, and `invalidate()` during `onBindViewHolder`.

- **ReelPlaybackViewModel**:
  - Updates surface readiness state and replays the video if necessary:
    ```kotlin
    fun updateSurfaceReadyState(position: Int, isReady: Boolean) {
        playLock.lock()
        try {
            surfaceReadyStates[position] = isReady
            if (isReady) {
                activePlayers[position]?.let { player ->
                    if (!player.isPlaying) {
                        playVideoAtPositionInternal(position, player)
                    }
                }
            }
        } finally {
            playLock.unlock()
        }
    }
    ```
  - Attempts playback immediately in `playVideoAtPosition`, even if the surface is not ready, to avoid delays:
    ```kotlin
    playVideoAtPositionInternal(position, player)
    if (!isSurfaceReady) {
        Timber.tag(tag).d("Surface not ready at position $position, but attempted to play anyway")
    }
    ```

#### Key Points
- Playback does not wait for surface readiness, reducing delays.
- Replays the video when the surface becomes available if it was not playing.

---

### 5. Player Lifecycle and Resource Management
#### Description
The app manages `ExoPlayer` instances to prevent memory leaks and optimize resource usage. Players are released when they are no longer needed.

#### Implementation
- **ReelPlaybackViewModel**:
  - Maintains `activePlayers` for currently used players and `preloadPlayers` for preloaded videos.
  - Releases players outside the `positionsToKeepRange` in `managePlayersAroundPosition`:
    ```kotlin
    private fun managePlayersAroundPosition(position: Int) {
        val start = maxOf(0, position - positionsToKeepRange)
        val end = minOf(reels.size - 1, position + positionsToKeepRange)
        coroutineScope.launch {
            playLock.lock()
            try {
                val iterator = activePlayers.iterator()
                while (iterator.hasNext()) {
                    val (pos, player) = iterator.next()
                    if (pos < start || pos > end) {
                        player.release()
                        iterator.remove()
                        surfaceReadyStates.remove(pos)
                    }
                }
                val preloadIterator = preloadPlayers.iterator()
                while (preloadIterator.hasNext()) {
                    val (pos, preloadPlayer) = preloadIterator.next()
                    if (pos < start || pos > end) {
                        preloadPlayer.release()
                        preloadIterator.remove()
                    }
                }
            } finally {
                playLock.unlock()
            }
        }
    }
    ```
  - Releases all resources in `onCleared`:
    ```kotlin
    override fun onCleared() {
        playLock.lock()
        try {
            activePlayers.forEach { (_, player) -> player.release() }
            activePlayers.clear()
            surfaceReadyStates.clear()
            preparedMediaItems.clear()
            preloadPlayers.forEach { (_, player) -> player.release() }
            preloadPlayers.clear()
        } finally {
            playLock.unlock()
        }
    }
    ```

- **ReelAdapter**:
  - Pauses and resets surface readiness when a view is recycled:
    ```kotlin
    override fun onViewRecycled(holder: ReelViewHolder) {
        val position = holder.bindingAdapterPosition
        if (position != RecyclerView.NO_POSITION && position != playbackViewModel.getCurrentPlayingPosition()) {
            holder.player?.pause()
            holder.player?.volume = 0f
            holder.isSurfaceReady = false
            playbackViewModel.updateSurfaceReadyState(position, false)
        }
    }
    ```

#### Key Points
- Players are released when they are outside the `positionsToKeepRange` to save memory.
- Ensures proper cleanup during view recycling and ViewModel lifecycle events.

---

## Known Issues and Future Improvements
- **Skipped Frames**: The app still experiences `Skipped frames` due to heavy operations on the Main thread (e.g., preloading with `ExoPlayer`). Future improvements could involve reducing Main thread usage or optimizing preloading.
- **Error Handling**: Playback errors are logged but not yet displayed to the user. A UI component (e.g., Toast or error overlay) should be added to inform the user of playback issues.
- **Loading UI**: The app lacks a loading indicator during video buffering. This will be addressed in the next phase of development.

## Conclusion
The reel playback feature is fully functional, supporting video playback, preloading, network change handling, surface readiness, and resource management. The implementation ensures smooth playback in most scenarios, with proper resource cleanup and thread safety. Future improvements will focus on enhancing performance (reducing skipped frames) and improving the user experience (adding loading UI and error handling).