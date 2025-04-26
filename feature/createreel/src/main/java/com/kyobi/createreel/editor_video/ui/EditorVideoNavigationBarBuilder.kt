package com.kyobi.createreel.editor_video.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import com.kyobi.composable.icon.Homeoutline
import com.kyobi.composable.icon.IconPack
import ly.img.editor.core.component.NavigationBar
import ly.img.editor.core.component.EditorComponent.ListBuilder
import ly.img.editor.core.component.EditorComponent.ListBuilder.Companion.modify
import ly.img.editor.core.component.closeEditor
import ly.img.editor.core.component.rememberCloseEditor

@Composable
fun buildEditorVideoNavigationBarList(
    baseNavigationBar: ListBuilder<NavigationBar.Item<*>, Alignment.Horizontal, Arrangement.Horizontal>,
): ListBuilder<NavigationBar.Item<*>, Alignment.Horizontal, Arrangement.Horizontal> = baseNavigationBar.modify {
    replace(id = NavigationBar.Button.Id.closeEditor) {
        NavigationBar.Button.rememberCloseEditor(
            vectorIcon = { IconPack.Homeoutline },
        )
    }
}