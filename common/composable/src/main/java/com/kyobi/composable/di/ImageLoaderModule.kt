package com.kyobi.composable.di

import android.content.Context
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageLoaderModule {
    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.5) // Dùng 50% bộ nhớ cho cache
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.35) // Dùng 35% dung lượng disk
                    .build()
            }
            .components {
                // Decoder để hỗ trợ GIF
                add(ImageDecoderDecoder.Factory()) // Dùng ImageDecoderDecoder cho Android 9+
            }
            .respectCacheHeaders(false) // Bỏ qua cache headers từ server để ưu tiên cache cục bộ
            .build()
    }
}