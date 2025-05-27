package com.kyobi.featurecommon.product.ui.product.header

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kyobi.composable.R
import com.kyobi.composable.button.AppIconButton
import com.kyobi.composable.space.SpaceY
import com.kyobi.composable.space.XsSpaceX
import com.kyobi.composable.space.XsSpaceY
import com.kyobi.composable.space.XxsSpaceX
import com.kyobi.featurecommon.product.ui.product.header.menu.ProductSectionPinnedHeaderMenuBarItem
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.paragraphXs

enum class PinnedHeaderMenuBarType { OVERVIEW, REVIEW, DETAILS, EXPLORE, MEASUREMENT }

@Composable
fun ProductSectionPinnedHeader(
    modifier: Modifier = Modifier,
    menuBarType: PinnedHeaderMenuBarType = PinnedHeaderMenuBarType.OVERVIEW,
    onBackClick: () -> Unit,
    onFavouriteClick: () -> Unit,
    onSearchClick: () -> Unit,
    onShareClick: () -> Unit,
    onMenuBarClick: (PinnedHeaderMenuBarType) -> Unit
) {
    val menuBarItems = PinnedHeaderMenuBarType.entries
    var selectedTabIndex by remember { mutableIntStateOf(menuBarItems.indexOf(menuBarType)) }

    val colorTheme = MaterialTheme.kyobiTheme.colors
    val spacing = MaterialTheme.kyobiTheme.spacing
    val shapeTheme = MaterialTheme.kyobiTheme.shapes

    Column(
        modifier = modifier
    ) {
        XsSpaceY()
        Row(
            modifier = Modifier
                .padding(horizontal = spacing.dp12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIconButton(
                icon = painterResource(id = R.drawable.ic_arrow_left),
                onClick = onBackClick,
                iconColor = colorTheme.onBackground,
            )
            XsSpaceX()
            SearchForm(
                modifier = Modifier.weight(1f),
                onSearchClick = onSearchClick
            )
            XsSpaceX()
            AppIconButton(
                icon = painterResource(id = R.drawable.ic_favorite),
                onClick = onFavouriteClick,
                iconColor = colorTheme.onBackground
            )
            XsSpaceX()
            AppIconButton(
                icon = painterResource(id = R.drawable.ic_share),
                onClick = onShareClick,
                iconColor = colorTheme.onBackground
            )
        }
        XsSpaceY()
        ScrollableTabRow(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = selectedTabIndex,
            edgePadding = spacing.dp12,
            containerColor = Color.Transparent,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .height(1.5.dp)
                        .clip(shapeTheme.extraSmall),
                    color = colorTheme.primary
                )
            },
            divider = {
                spacing.dp0.SpaceY()
            }
        ) {
            menuBarItems.forEachIndexed { index, menuBarType ->
                ProductSectionPinnedHeaderMenuBarItem(
                    menuBarType = menuBarType,
                    isSelected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        onMenuBarClick(menuBarType)
                    }
                )
            }
        }
    }
}

@Composable
private fun SearchForm(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit,
) {
    val height = MaterialTheme.kyobiTheme.height
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val shapeTheme = MaterialTheme.kyobiTheme.shapes
    val spacing = MaterialTheme.kyobiTheme.spacing
    val iconTheme = MaterialTheme.kyobiTheme.icon
    val typographyTheme = MaterialTheme.kyobiTheme.typography

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp40)
            .clip(shapeTheme.extraLarge)
            .background(colorTheme.bg.stone100)
            .clickable { onSearchClick() },
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = spacing.dp8,
                    vertical = spacing.dp8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search_normal),
                contentDescription = "Product search",
                modifier = Modifier.size(iconTheme.lg),
                tint = colorTheme.bg.stone500
            )
            XxsSpaceX()
            Text(
                modifier = Modifier.weight(1f),
                text = "Find everything",
                style = typographyTheme.paragraphXs,
                color = colorTheme.bg.stone400,
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_scan_similar),
                contentDescription = "Scan search",
                modifier = Modifier.size(iconTheme.lg),
                tint = colorTheme.bg.stone500
            )
        }
    }
}