package com.kyobi.createreel.editor_video

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import ly.img.editor.EditorUiState
import ly.img.editor.HideLoading
import ly.img.editor.core.EditorContext
import ly.img.editor.core.event.EditorEvent

import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelCompat

// highlight-configuration-custom-state
data class OverlayCustomState(
    // hide default loading so we can use custom loading
    val baseState: EditorUiState = EditorUiState(showLoading = false),
    val showCustomLoading: Boolean = true,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        baseState = ParcelCompat.readParcelable(parcel, EditorUiState::class.java.classLoader, EditorUiState::class.java)!!,
        showCustomLoading = parcel.readByte() != 0.toByte(),
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
        parcel.writeParcelable(baseState, flags)
        parcel.writeByte(if (showCustomLoading) 1 else 0)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<OverlayCustomState> {
        override fun createFromParcel(parcel: Parcel): OverlayCustomState = OverlayCustomState(parcel)

        override fun newArray(size: Int): Array<OverlayCustomState?> = arrayOfNulls(size)
    }
}
// highlight-configuration-custom-state

@Composable
fun EditorVideoOverlay(
    editorContext: EditorContext,
    state: EditorUiState,
    isExporting: Boolean,
    animatedProgress: Float
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.kyobi_logo),
            contentDescription = "Kyobi Logo",
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp)
                .align(Alignment.TopStart)
        )

        // ProgressBar ở giữa màn hình
        if (isExporting) {
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
                TextButton(
                    onClick = {
                        editorContext.eventHandler.send(HideLoading)
                        editorContext.eventHandler.send(EditorEvent.Export.Cancel())
                        editorContext.eventHandler.send(EditorEvent.CloseEditor())
                    },
                ) {
                    Text(text = "Cancel")
                }
            }
        }

        // Thông báo khuyến khích ở góc dưới bên phải
        if (!state.sceneIsLoaded) {
            Text(
                text = "Tặng voucher 10% khi đăng Lookbook lên Kyobi!",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .background(
                        color = Color(0xFF00A1D6),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}