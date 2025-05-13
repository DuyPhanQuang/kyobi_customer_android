package com.kyobi.createreel.editor_video.asset

import com.kyobi.domain.model.Assets
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.usecase.AssetSourceUseCase
import com.kyobi.domain.usecase.AssetUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import ly.img.engine.Asset
import ly.img.engine.AssetContext
import ly.img.engine.AssetCredits
import ly.img.engine.AssetLicense
import ly.img.engine.AssetPayload
import ly.img.engine.AssetSource
import ly.img.engine.AssetUTM
import ly.img.engine.BlockApi
import ly.img.engine.DesignBlock
import ly.img.engine.Engine
import ly.img.engine.FillType
import ly.img.engine.FindAssetsQuery
import ly.img.engine.FindAssetsResult
import ly.img.engine.Source
import javax.inject.Inject
import androidx.core.net.toUri
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber

const val tag = "RemoteAssetSource"

suspend fun Engine.addRemoteAssetSources(
    paths: Set<RemoteAssetSource.Path>,
    assetSourceUsecase: AssetSourceUseCase,
    assetUsecase: AssetUseCase
) = coroutineScope {
    paths
        .map { async { RemoteAssetSource(engine = this@addRemoteAssetSources, path = it, assetSourceUsecase, assetUsecase).create() } }
        .awaitAll()
        .forEach { source ->
            Timber.tag(tag).d("Adding source: ${source.sourceId}")
            asset.addSource(source)
            Timber.tag(tag).d("Source ${source.sourceId} added successfully")
        }
}

suspend fun Engine.addGiphyAssetSources(
    assetSourceUsecase: AssetSourceUseCase,
    assetUsecase: AssetUseCase
) = coroutineScope {
    val paths = setOf(RemoteAssetSource.Path.GiphyStickers)
    Timber.tag(tag).d("Adding Giphy asset sources")
    addRemoteAssetSources(paths, assetSourceUsecase, assetUsecase)
}

class RemoteAssetSource @Inject constructor(
    private val engine: Engine,
    private val path: Path,
    private val assetSourceUsecase: AssetSourceUseCase,
    private val assetUsecase: AssetUseCase
) {
    enum class Path(
        val pathString: String,
    ) {
        GiphyStickers("/giphy-stickers"),
    }

    suspend fun create(): AssetSource {
        Timber.tag(tag).d("Starting create() for ${path.pathString}")
        val domainManifestData = withContext(Dispatchers.IO) {
            Timber.tag(tag).d("Fetching manifest data for ${path.pathString}")
            val result = assetSourceUsecase.getGiphyAssetSource()
                .catch { e ->
                    Timber.tag(tag).e("Flow error while fetching manifest: ${e.message}")
                    throw e
                }
                .firstOrNull()

            if (result == null) {
                Timber.tag(tag).e("Asset source result is null")
                throw Exception("Asset source result is null")
            }

            Timber.tag(tag).d("Result from assetSourceUsecase: $result")
            when (result) {
                is DomainNetworkResult.Success -> {
                    val data = result.data
                    if (data.id != path.pathString.removePrefix("/")) {
                        Timber.tag(tag).e("Manifest id does not match path: expected ${path.pathString.removePrefix("/")}, got ${data.id}")
                        throw Exception("Manifest id does not match path")
                    }
                    Timber.tag(tag).d("Manifest data fetched: $data")
                    Timber.tag(tag).d("Manifest id: ${data.id}")
                    data
                }
                else -> {
                    Timber.tag(tag).e("Failed to fetch asset source manifest: $result")
                    throw Exception("Failed to fetch asset source manifest: $result")
                }
            }
        }

        Timber.tag(tag).d("Creating AssetSource with id: ${domainManifestData.id}")

        // Create AssetSource instance
        val assetSource = object : AssetSource(domainManifestData.id) {
            override suspend fun findAssets(query: FindAssetsQuery): FindAssetsResult {
                Timber.tag(tag).d("findAssets called with query: ${query.query}, page: ${query.page}, perPage: ${query.perPage}, locale: ${query.locale}")

                val result = assetUsecase.getGiphyAssets(
                    query = query.query,
                    page = if (query.page > 0) query.page else 1,
                    perPage = query.perPage,
                    locale = query.locale
                )
                    .catch { e ->
                        Timber.tag(tag).e("Flow error while fetching assets: ${e.message}")
                        throw e
                    }
                    .firstOrNull()

                return if (result == null) {
                    Timber.tag(tag).e("Assets result is null")
                    throw Exception("Assets result is null")
                } else {
                    when (result) {
                        is DomainNetworkResult.Success -> {
                            Timber.tag(tag).d("Assets fetched: ${result.data.assets.size} items")
                            val mappedResult = mapAssetsToFindAssetsResult(result.data, this)
                            Timber.tag(tag).d("Returning FindAssetsResult with ${mappedResult.assets.size} assets")
                            mappedResult
                        }
                        else -> {
                            Timber.tag(tag).e("Failed to fetch assets: $result")
                            throw Exception("Failed to fetch assets: $result")
                        }
                    }
                }
            }

            override suspend fun getGroups() = null

            override suspend fun applyAsset(asset: Asset): DesignBlock? {
                Timber.tag(tag).d("Applying asset: ${asset.id}")
                val block = engine.asset.defaultApplyAsset(asset) ?: return null
                engine.block.ensureAssetDuration(block, asset)
                engine.block.ensureMetadataKeys(block, asset, sourceId)
                return block
            }

            override suspend fun applyAsset(
                asset: Asset,
                block: DesignBlock,
            ) {
                Timber.tag(tag).d("Applying asset to block: ${asset.id}")
                engine.asset.defaultApplyAsset(asset, block)
                engine.block.ensureMetadataKeys(block, asset, sourceId)
            }

            override val supportedMimeTypes: List<String> = domainManifestData.supportedMimeTypes

            override val credits: AssetCredits = AssetCredits(
                name = domainManifestData.credits.name,
                uri = domainManifestData.credits.url.toUri()
            )

            override val license: AssetLicense = AssetLicense(
                name = domainManifestData.license.name,
                uri = domainManifestData.license.url.toUri()
            )
        }

        Timber.tag(tag).d("AssetSource created successfully for ${path.pathString}")
        return assetSource
    }

    private fun BlockApi.ensureMetadataKeys(
        designBlock: DesignBlock,
        asset: Asset,
        sourceId: String,
    ) {
        setMetadata(designBlock, "source/id", sourceId)
        setMetadata(designBlock, "source/externalId", asset.id)
    }

    private suspend fun BlockApi.ensureAssetDuration(
        designBlock: DesignBlock,
        asset: Asset,
    ) {
        if (asset.meta?.get("duration") != null || !supportsFill(designBlock)) return
        val fill = getFill(designBlock)
        if (FillType.getOrNull(getType(fill)) != FillType.Video) return
        forceLoadAVResource(fill)
        val duration = getAVResourceTotalDuration(fill)
        setDuration(designBlock, duration)
    }

    private suspend fun mapAssetsToFindAssetsResult(assets: Assets, assetSource: AssetSource): FindAssetsResult {
        val mappedAssets = assets.assets.map { asset ->
            val sourceSet = asset.payload.sourceSet.map { source ->
                val dataUri = try {
                    val imageData = withContext(Dispatchers.IO) {
                        java.net.URL(source.uri).openStream().use { it.readBytes() }
                    }
                    val base64 = android.util.Base64.encodeToString(imageData, android.util.Base64.DEFAULT)
                    val dataUri = "data:image/gif;base64,$base64"
                    dataUri
                } catch (e: Exception) {
                    Timber.tag(tag).e("Error converting to Data URI for ${asset.id}: ${e.message}")
                    source.uri
                }

                Source(
                    uri = dataUri.toUri(),
                    width = source.width,
                    height = source.height
                )
            }
            Asset(
                id = asset.id,
                context = AssetContext(assetSource.sourceId),
                label = asset.label,
                locale = asset.locale,
                tags = asset.tags.takeIf { it.isNotEmpty() },
                groups = asset.groups.takeIf { it.isNotEmpty() },
                meta = asset.meta.takeIf { it.isNotEmpty() },
                payload = AssetPayload(
                    sourceSet = sourceSet
                ),
                credits = AssetCredits(
                    name = asset.credits.name,
                    uri = asset.credits.url.toUri()
                ),
                license = AssetLicense(
                    name = asset.license.name,
                    uri = asset.credits.url.toUri()
                ),
                utm = AssetUTM(
                    source = asset.utm.source,
                    medium = asset.utm.medium
                )
            )
        }
        return FindAssetsResult(
            assets = mappedAssets,
            currentPage = assets.currentPage ?: -1,
            nextPage = assets.nextPage ?: -1,
            total = assets.total
        )
    }
}