package com.zionhuang.music

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text

class TurntableWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {

        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long running
        // operations.

        provideContent {
            // create your AppWidget here
            TurntableContent()
        }
    }

    @Composable
    private fun TurntableContent() {
//        Column(
//            modifier = GlanceModifier.fillMaxSize(),
//            verticalAlignment = Alignment.Top,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(text = "Where to?", modifier = GlanceModifier.padding(12.dp))
//            Row(horizontalAlignment = Alignment.CenterHorizontally) {
//                Button(
//                    text = "Home",
//                    onClick = actionStartActivity<MainActivity>()
//                )
//                Button(
//                    text = "Work",
//                    onClick = actionStartActivity<MainActivity>()
//                )
//            }
//        }
    }
}

class TurntableWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TurntableWidget()
}