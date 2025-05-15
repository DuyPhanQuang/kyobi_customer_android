package com.kyobi.createreel.editor_video.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyobi.feature.createreel.R
import ly.img.editor.HideLoading
import ly.img.editor.core.EditorContext
import ly.img.editor.core.event.EditorEvent

@Composable
fun EditorVideoOverlay(
    editorContext: EditorContext,
    animatedProgress: Float,
    onExportCancelled: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.kyobi_logo),
            contentDescription = "Kyobi Logo",
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .background(
                    color = Color(0xFF1A1A1A).copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .size(width = 200.dp, height = 8.dp),
                color = Color(0xFF00A1D6),
                trackColor = Color(0xFFF5F5F5)
            )
            Text(
                text = "Exporting: ${(animatedProgress * 100).toInt()}%",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(text = "Please wait. If you want to cancel this process, click the button.")
            TextButton(
                onClick = {
                    editorContext.eventHandler.send(HideLoading)
                    editorContext.eventHandler.send(EditorEvent.Export.Cancel())
                    onExportCancelled()
                },
            ) {
                Text(text = "Cancel")
            }
        }

        Text(
            text = "Tặng voucher 10% khi đăng Lookbook lên Kyobi!",
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(
                    color = Color(0xFF00A1D6),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}