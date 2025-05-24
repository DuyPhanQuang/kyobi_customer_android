package com.kyobi.composable.image

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.kyobi.composable.R
import com.kyobi.composable.skeleton.SkeletonContainer
import com.kyobi.theme.kyobiTheme
import timber.log.Timber

@Composable
fun AppImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    defaultImageRes: Int = R.drawable.default_image,
    errorImageRes: Int = R.drawable.error_image,
    isSkeletonEnabled: Boolean = true,
    contentDescription: String? = null,
    filterQuality: FilterQuality = FilterQuality.High,
    imageLoader: ImageLoader
) {
    val tag = "AppImage"
    val context = LocalContext.current

    val finalImageUrl = if (imageUrl.isNullOrEmpty()) defaultImageRes else imageUrl

    // Kiểm tra disk cache
//    val isInDiskCache = imageUrl?.let { url ->
//        imageLoader.diskCache?.openSnapshot(url)?.use { true } ?: false
//    } ?: false
//    Timber.tag(tag).d("Image in disk cache for URL: $finalImageUrl: $isInDiskCache")

    val errorImageRequest = ImageRequest.Builder(context).data(errorImageRes).build()

    val request = ImageRequest.Builder(context)
        .data(finalImageUrl)
        .crossfade(false)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .allowHardware(true)
        .build()

    val shapeTheme = MaterialTheme.kyobiTheme.shapes

    Box(modifier = modifier) {
        SubcomposeAsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = request,
            imageLoader = imageLoader,
            contentDescription = contentDescription,
            contentScale = contentScale,
            filterQuality = filterQuality,
            loading = { _ ->
                if (isSkeletonEnabled) {
                    SkeletonContainer(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(shapeTheme.extraSmall)
                    )
                }
            },
            success = { state ->
//                Timber.tag(tag).d("Image loaded successfully for URL: $finalImageUrl with result: memoryCache:${state.result.memoryCacheKey} diskCache:${state.result.diskCacheKey}")
                SubcomposeAsyncImageContent(
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            },
            error = { state ->
//                Timber.tag(tag).e("Image load failed for URL: $finalImageUrl, error: ${state.result.throwable}")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    SubcomposeAsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = errorImageRequest,
                        contentDescription = "Error Image",
                        contentScale = contentScale,
                    )
                }
            }
        )
    }
}