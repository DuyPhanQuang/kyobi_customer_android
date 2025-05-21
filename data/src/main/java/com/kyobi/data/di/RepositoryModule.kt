package com.kyobi.data.di

import com.kyobi.data.repository.AppConfigRepositoryImpl
import com.kyobi.data.repository.AuthRepositoryImpl
import com.kyobi.data.repository.CatalogRepositoryImpl
import com.kyobi.data.repository.CollectionRepositoryImpl
import com.kyobi.data.repository.MediaRepositoryImpl
import com.kyobi.data.repository.MetaobjectRepositoryImpl
import com.kyobi.data.repository.NotificationRepositoryImpl
import com.kyobi.data.repository.PageRepositoryImpl
import com.kyobi.data.repository.ProductRepositoryImpl
import com.kyobi.domain.repository.AppConfigRepository
import com.kyobi.domain.repository.AuthRepository
import com.kyobi.domain.repository.CatalogRepository
import com.kyobi.domain.repository.CollectionRepository
import com.kyobi.domain.repository.MediaRepository
import com.kyobi.domain.repository.MetaobjectRepository
import com.kyobi.domain.repository.NotificationRepository
import com.kyobi.domain.repository.PageRepository
import com.kyobi.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAppConfigRepository(impl: AppConfigRepositoryImpl): AppConfigRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindPageRepository(impl: PageRepositoryImpl): PageRepository

    @Binds
    @Singleton
    abstract fun bindCatalogRepository(impl: CatalogRepositoryImpl): CatalogRepository

    @Binds
    @Singleton
    abstract fun bindMetaobjectRepository(impl: MetaobjectRepositoryImpl): MetaobjectRepository

    @Binds
    @Singleton
    abstract fun bindCollectionRepository(impl: CollectionRepositoryImpl): CollectionRepository

    @Binds
    @Singleton
    abstract fun bindMediaRepository(impl: MediaRepositoryImpl): MediaRepository
}