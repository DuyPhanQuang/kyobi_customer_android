package com.kyobi.createreel.editor_video

import androidx.compose.runtime.Composable
import ly.img.editor.core.component.Dock
import ly.img.editor.core.component.rememberAudiosLibrary
import ly.img.editor.core.component.rememberOverlaysLibrary
import ly.img.editor.core.component.rememberStickersLibrary
import ly.img.editor.core.component.rememberSystemGallery
import ly.img.editor.core.component.rememberTextLibrary

@Composable
fun editorVideoDock() = Dock.ListBuilder.remember {
    add { Dock.Button.rememberSystemGallery() }
    add { Dock.Button.rememberOverlaysLibrary() }
    add { Dock.Button.rememberTextLibrary() }
    add { Dock.Button.rememberStickersLibrary() }
    add { Dock.Button.rememberAudiosLibrary() }
}