package com.zeppelin.app.screens.courseDetail.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zeppelin.app.R
import com.zeppelin.app.screens._common.ui.CardWithTitle
import com.zeppelin.app.screens._common.ui.TextWithLoader
import com.zeppelin.app.screens.courseDetail.data.CourseDetailModulesUI
import com.zeppelin.app.screens.courseDetail.data.CourseDetailModulesUIState
import com.zeppelin.app.screens.courseDetail.data.CourseDetailWithModules
import com.zeppelin.app.screens.courseDetail.data.ModuleListUI
import com.zeppelin.app.ui.theme.ZeppelinTheme


@Composable
fun ModuleAccordion(
    courseDetailModulesUI: CourseDetailModulesUI,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onModuleClick: (ModuleListUI) -> Unit = {},
) {
    CardWithTitle(modifier, "Modulos") {
        if (isLoading)
            for(i in 0..2){
                ModuleItem(ModuleListUI("", i, 0, false, isLoading = true, content = listOf()), true) { }
        }

        courseDetailModulesUI.modules.forEach { module ->
            ModuleItem(
                module = module,
                isLoading = module.isLoading,
                onModuleClick = {onModuleClick(it)},
            )
        }
    }
}

@Composable
fun ModuleItem(module: ModuleListUI, isLoading: Boolean, onModuleClick: (ModuleListUI) -> Unit) {
    ModuleItemContent(
        module = module,
        isLoading = isLoading,
        onModuleClick = {onModuleClick(it)},
    ) { content ->
        ContentList(content)
    }
}

@Composable
fun ContentListOld(content: List<ModuleListUI.ContentItemUI>) {
    val density = LocalDensity.current
    var listHeight by remember { mutableStateOf(0.dp) }

    Box {
        VerticalDivider(
            modifier = Modifier
                .height(listHeight.times(0.9f))
                .padding(top = 8.dp)
                .offset(x = (8).dp)
        )
        LazyColumn(
            modifier = Modifier
                .onGloballyPositioned { coords ->
                    listHeight = with(density) { coords.size.height.toDp() }
                }
        ) {
            itemsIndexed(content) { index, item ->
                ContentItem(item, index < content.lastIndex)

            }
        }
    }
}

@Composable
fun ContentList(content: List<ModuleListUI.ContentItemUI>) {
    // KEY CHANGE: Replaced LazyColumn with a regular Column.
    // This prevents the nested scrolling crash.
    Column {
        content.forEachIndexed { index, item ->
            // Your VerticalDivider logic would need to be adapted here,
            // perhaps by wrapping ContentItem in a Row with a Divider.
            // For now, we just render the item.
            ContentItem(item, index < content.lastIndex)
        }
    }
}

@Composable
fun ContentItem(
    content: ModuleListUI.ContentItemUI,
    divider: Boolean,
    modifier: Modifier = Modifier
) {
    val (contentTypIcon, typeTint) = when (content.contentType) {
        CourseDetailWithModules.ContentType.QUIZ -> painterResource(R.drawable.baseline_quiz_24) to
                MaterialTheme.colorScheme.tertiary

        CourseDetailWithModules.ContentType.VIDEO -> painterResource(R.drawable.rounded_play_circle_24) to
                MaterialTheme.colorScheme.tertiary

        CourseDetailWithModules.ContentType.TEXT -> painterResource(R.drawable.rounded_docs_24) to
                MaterialTheme.colorScheme.tertiary
    }
    val (contentStatusIcon, tint) = when (content.contentStatus) {
        CourseDetailWithModules.ContentStatus.COMPLETED ->
            painterResource(R.drawable.rounded_check_circle_24) to
                    MaterialTheme.colorScheme.primaryContainer

        CourseDetailWithModules.ContentStatus.NOT_STARTED ->
            painterResource(R.drawable.rounded_pending_24) to
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

        CourseDetailWithModules.ContentStatus.IN_PROGRESS ->
            painterResource(R.drawable.rounded_schedule_24) to
                    MaterialTheme.colorScheme.secondaryContainer
    }

    Box {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = modifier,
            leadingContent = {
                Icon(
                    painter = contentTypIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(16.dp),
                    tint = typeTint
                )
            },
            headlineContent = {
                TextWithLoader(content.title, 16, MaterialTheme.typography.bodyMedium, false)
            },
            trailingContent = {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = contentStatusIcon,
                    contentDescription = null,
                    tint = tint
                )
            }
        )
        if (divider)
            HorizontalDivider(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(start = 16.dp)
            )
    }
}

@Composable
fun ModuleItemContent(
    module: ModuleListUI,
    isLoading: Boolean,
    onModuleClick: (ModuleListUI) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (content: List<ModuleListUI.ContentItemUI>) -> Unit
) {
    Column(modifier = modifier) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            tonalElevation = (0.5).dp,
            modifier = Modifier
                .clickable {
                    onModuleClick(module)
                },
            headlineContent = {
                TextWithLoader(
                    module.moduleName,
                    size = 20,
                    MaterialTheme.typography.titleMedium,
                    isLoading,
                )
            },
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextWithLoader(
                        text = "${module.contentCount}",
                        size = 16,
                        style = MaterialTheme.typography.labelMedium,
                        isLoading = isLoading
                    )
                    AnimatedContent(module.showContent) { show ->
                        if (show) Icon(
                            Icons.Rounded.KeyboardArrowUp,
                            contentDescription = "Collapse Module"
                        )
                        else Icon(
                            Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Expand Module"
                        )
                    }
                }
            }
        )
        AnimatedContent(
            targetState = module.showContent,
            label = "ModuleContentAnimation",
        ) { show ->
            if (show) {
                content(module.content)
            }
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
fun ModuleHeaderPreview() {
    ZeppelinTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ModuleItemContent(
                module = ModuleListUI(
                    content = listOf(
                        ModuleListUI.ContentItemUI(
                            contentId = "1",
                            title = "Sample Content 1",
                            contentType = CourseDetailWithModules.ContentType.QUIZ,
                            contentStatus = CourseDetailWithModules.ContentStatus.NOT_STARTED
                        ),
                        ModuleListUI.ContentItemUI(
                            contentId = "2",
                            title = "Sample Content 2",
                            contentType = CourseDetailWithModules.ContentType.VIDEO,
                            contentStatus = CourseDetailWithModules.ContentStatus.COMPLETED
                        ),
                        ModuleListUI.ContentItemUI(
                            contentId = "3",
                            title = "Sample Content 3",
                            contentType = CourseDetailWithModules.ContentType.TEXT,
                            contentStatus = CourseDetailWithModules.ContentStatus.IN_PROGRESS
                        )
                    ),
                    showContent = true,
                    moduleName = "Sample Module",
                    moduleIndex = 1,
                    contentCount = 2
                ),
                isLoading = false,
                onModuleClick = {}
            ) { content ->
                ContentList(content)
            }
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
fun ModuleHeaderPreviewNoShow() {
    ZeppelinTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ModuleItemContent(
                module = ModuleListUI(
                    content = listOf(),
                    showContent = false,
                    moduleName = "Sample Module",
                    moduleIndex = 1,
                    contentCount = 2
                ),
                isLoading = false,
                onModuleClick = {}
            ) { content ->
                ContentList(content)
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun ModuleAccordionPreview() {
    ZeppelinTheme {
        ModuleAccordion(
            CourseDetailModulesUI(
                modules = listOf(
                    ModuleListUI(
                        content = listOf(
                            ModuleListUI.ContentItemUI(
                                contentId = "12",
                                title = "Modulo 2",
                                contentType = CourseDetailWithModules.ContentType.TEXT,
                                contentStatus = CourseDetailWithModules.ContentStatus.IN_PROGRESS
                            )
                        ),
                        showContent = true,
                        moduleName = "Sample Module",
                        moduleIndex = 1,
                        contentCount = 2
                    ),
                    ModuleListUI(
                        content = listOf(
                            ModuleListUI.ContentItemUI(
                                contentId = "1",
                                title = "Sample Content 1",
                                contentType = CourseDetailWithModules.ContentType.QUIZ,
                                contentStatus = CourseDetailWithModules.ContentStatus.NOT_STARTED
                            ),
                            ModuleListUI.ContentItemUI(
                                contentId = "2",
                                title = "Sample Content 2",
                                contentType = CourseDetailWithModules.ContentType.VIDEO,
                                contentStatus = CourseDetailWithModules.ContentStatus.COMPLETED
                            ),
                            ModuleListUI.ContentItemUI(
                                contentId = "3",
                                title = "Sample Content 3",
                                contentType = CourseDetailWithModules.ContentType.TEXT,
                                contentStatus = CourseDetailWithModules.ContentStatus.IN_PROGRESS
                            )
                        ),
                        showContent = true,
                        moduleName = "Sample Module",
                        moduleIndex = 1,
                        contentCount = 2
                    ),

                    )
            )
        ) { }
    }
}


