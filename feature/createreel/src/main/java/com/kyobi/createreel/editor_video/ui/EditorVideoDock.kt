package com.kyobi.createreel.editor_video.ui

import androidx.compose.runtime.Composable
import ly.img.editor.core.component.Dock
import ly.img.editor.core.component.rememberAudiosLibrary
import ly.img.editor.core.component.rememberImglyCamera
import ly.img.editor.core.component.rememberOverlaysLibrary
import ly.img.editor.core.component.rememberShapesLibrary
import ly.img.editor.core.component.rememberStickersLibrary
import ly.img.editor.core.component.rememberSystemGallery
import ly.img.editor.core.component.rememberTextLibrary

@Composable
fun editorVideoDock() = Dock.ListBuilder.remember {
    add { Dock.Button.rememberSystemGallery() }
    add { Dock.Button.rememberImglyCamera() }
    add { Dock.Button.rememberAudiosLibrary() }
    add { Dock.Button.rememberTextLibrary() }
    add { Dock.Button.rememberStickersLibrary() }
    add { Dock.Button.rememberOverlaysLibrary() }
    add { Dock.Button.rememberShapesLibrary() }
}