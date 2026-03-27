package com.carlos.autoflow.foundation.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.carlos.autoflow.foundation.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoundationTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    showNavigationIcon: Boolean = true,
    onNavigationClick: () -> Unit = {},
    navigationContentDescription: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val navDescription = navigationContentDescription
        ?: stringResource(R.string.foundation_top_app_bar_back)

    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = if (showNavigationIcon) {
            {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = navDescription
                    )
                }
            }
        } else {
            {}
        },
        actions = actions,
        modifier = modifier
    )
}
