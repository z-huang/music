package com.zionhuang.music

import androidx.glance.GlanceId
import androidx.glance.LocalContext
import androidx.glance.appwidget.provideContent
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.glance.Image
import androidx.glance.layout.fillMaxSize
import androidx.glance.session.GlanceSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.absolutePadding
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.size
import androidx.glance.layout.wrapContentSize
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.SuccessResult
import coil.transform.CircleCropTransformation
import com.zionhuang.music.playback.PlayerConnection
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

class TurntableWidget : GlanceAppWidget() {

    val sessionManager = GlanceSessionManager

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long running
        // operations.

        provideContent {
            // create your AppWidget here
            TurntableContent()
        }
    }

    private suspend fun Context.fetchImage(url: String): Bitmap? {
        val request = ImageRequest.Builder(this).data(url).build()
        return when (val result = imageLoader.execute(request)) {
            is ErrorResult -> throw result.throwable
            is SuccessResult -> result.drawable.toBitmapOrNull()
        }
    }
    private var playerConnection by mutableStateOf<PlayerConnection?>(null)

    @Composable
    private fun TurntableContent() {
        val context = LocalContext.current
        val thumbnailUrl = "https://picsum.photos/200/300"
        var thumbnail by remember(thumbnailUrl) { mutableStateOf<Bitmap?>(null) }

        LaunchedEffect(thumbnailUrl) {
            thumbnail = context.fetchImage(thumbnailUrl)
        }
        Box(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            if (thumbnail != null) {
                Image(
                    provider = ImageProvider(thumbnail!!),
                    contentDescription = "Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = GlanceModifier.fillMaxSize().cornerRadius(256.dp)
                )

            } else {
                CircularProgressIndicator()
            }

            Column(
                modifier = GlanceModifier.fillMaxSize()
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                ) {
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Button(
                        text = "TopRight",
                        onClick = actionStartActivity<MainActivity>(),
                        modifier = GlanceModifier.wrapContentSize()
                    )
                }
                Spacer(modifier = GlanceModifier.defaultWeight())
                Spacer(modifier = GlanceModifier.defaultWeight()) // This spacer pushes the bottom row to the bottom
                Row(
                    modifier = GlanceModifier.fillMaxWidth()
                ) {
                    Button(
                        text = "BottomLeft",
                        onClick = actionStartActivity<MainActivity>(),
                        modifier = GlanceModifier.wrapContentSize()
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                }
            }
        }
    }
}

class TurntableWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TurntableWidget()
}

class NowPlayingWidget : GlanceAppWidget() {

    val sessionManager = GlanceSessionManager

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long running
        // operations.

        provideContent {
            // create your AppWidget here
            NowPlayingContent()
        }
    }

    private suspend fun Context.fetchImage(url: String): Bitmap? {
        val request = ImageRequest.Builder(this).data(url).build()
        return when (val result = imageLoader.execute(request)) {
            is ErrorResult -> throw result.throwable
            is SuccessResult -> result.drawable.toBitmapOrNull()
        }
    }
    private var playerConnection by mutableStateOf<PlayerConnection?>(null)

    @Composable
    private fun NowPlayingContent() {
        val context = LocalContext.current
        val thumbnailUrl = "https://picsum.photos/200/300"
        var thumbnail by remember(thumbnailUrl) { mutableStateOf<Bitmap?>(null) }

        LaunchedEffect(thumbnailUrl) {
            thumbnail = context.fetchImage(thumbnailUrl)
        }
        if (thumbnail != null) {
            Image(
                provider = ImageProvider(thumbnail!!),
                contentDescription = "Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = GlanceModifier.fillMaxSize()
            )

        } else {
            CircularProgressIndicator()
        }
    }
}

class NowPlayingWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NowPlayingWidget()
}