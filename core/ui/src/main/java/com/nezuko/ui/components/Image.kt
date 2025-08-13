package com.nezuko.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "Image"

@Composable
fun Image(
    url: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null
) {
    val context = LocalContext.current

    val imageRequest = remember(url) {
        ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    AsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = ColorPainter(Color.LightGray)
    )
}

@Composable
fun ImageWithRatio(
    modifier: Modifier = Modifier,
    url: String,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop
) {
    val cached = remember { ImageRatioCache.get(url) }
    Log.i(TAG, "ImageWithRatio: ratio - $cached $url ")
    var ratio by rememberSaveable(url) { mutableFloatStateOf(cached ?: 1f) }

    SubcomposeAsyncImage(
        model = url,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
    ) {
        val state = painter.state
        if (state is AsyncImagePainter.State.Success) {
            val size = painter.intrinsicSize
            if (size.height > 0f) {
                val newRatio = size.width / size.height
                if (newRatio.isFinite() && newRatio > 0f && newRatio != ratio) {
                    ratio = newRatio
                    ImageRatioCache.put(url, newRatio)
                }
            }
            SubcomposeAsyncImageContent()
        }
    }
}

object ImageRatioCache {
    private const val MAX_ENTRIES = 500
    private val map = ConcurrentHashMap<String, Float>()

    fun get(key: String): Float? = map[key]

    fun put(key: String, ratio: Float) {
        map[key] = ratio
    }
}

