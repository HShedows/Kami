package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.ui.library.LibraryItem
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.model.MangaCover
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.util.plus

@Composable
internal fun LibraryList(
    items: List<LibraryItem>,
    pagedBrowsing: Boolean,
    manualRows: Int,
    entries: Int,
    containerHeight: Int,
    contentPadding: PaddingValues,
    selection: Set<Long>,
    onClick: (LibraryManga) -> Unit,
    onLongClick: (LibraryManga) -> Unit,
    onClickContinueReading: ((LibraryManga) -> Unit)?,
    searchQuery: String?,
    onGlobalSearchClicked: () -> Unit,
    categories: List<Category>,
    categoryIndex: Int,
    onSelectCategory: (Int) -> Unit,
    showHopper: Boolean,
    hopperOffsetX: androidx.compose.animation.core.Animatable<Float, *>,
    hopperInitialized: Boolean,
    onHopperInitialized: () -> Unit,
    hopperSavedPosition: Int,
    onHopperPositionChanged: (Int) -> Unit,
) {
    // "Items per column" slider (manualRows) controls how many list items
    // fit on screen, which also sets each item's height via MangaListItem's
    // entries/containerHeight parameters. When 0 (auto), fall back to
    // whatever the grid's column count passed as entries (usually 0 too,
    // which gives MangaListItem its default 56dp height).
    val effectiveEntries = if (manualRows > 0) manualRows else entries

    val density = LocalDensity.current

    val cell: @Composable (LibraryItem) -> Unit = { libraryItem ->
        val manga = libraryItem.libraryManga.manga
        MangaListItem(
            isSelected = manga.id in selection,
            title = manga.title,
            coverData = MangaCover(
                mangaId = manga.id,
                sourceId = manga.source,
                isMangaFavorite = manga.favorite,
                url = manga.thumbnailUrl,
                lastModified = manga.coverLastModified,
            ),
            badge = {
                DownloadsBadge(count = libraryItem.badges.downloadCount)
                UnreadBadge(count = libraryItem.badges.unreadCount)
                LanguageBadge(
                    isLocal = libraryItem.badges.isLocal,
                    sourceLanguage = libraryItem.badges.sourceLanguage,
                )
            },
            onLongClick = { onLongClick(libraryItem.libraryManga) },
            onClick = { onClick(libraryItem.libraryManga) },
            onClickContinueReading = if (onClickContinueReading != null && libraryItem.unreadCount > 0) {
                { onClickContinueReading(libraryItem.libraryManga) }
            } else {
                null
            },
            entries = effectiveEntries,
            containerHeight = containerHeight,
        )
    }

    if (pagedBrowsing) {
        // MangaListItem has an explicit fixed height(56.dp), so this is
        // exact rather than estimated.
        PagedLibraryGrid(
            items = items,
            columns = 1,
            manualRows = manualRows,
            contentPadding = contentPadding,
            // When manualRows > 0, item height = containerHeight / manualRows,
            // matching MangaListItem's own size formula. When 0, use the
            // default 56dp fixed height.
            cellHeightForWidth = { _ ->
                if (manualRows > 0 && containerHeight > 0) {
                    with(density) { (containerHeight / manualRows).toDp() }
                } else {
                    56.dp
                }
            },
            cell = cell,
            categories = categories,
            categoryIndex = categoryIndex,
            onSelectCategory = onSelectCategory,
            showHopper = showHopper,
            hopperOffsetX = hopperOffsetX,
            hopperInitialized = hopperInitialized,
            onHopperInitialized = onHopperInitialized,
            hopperSavedPosition = hopperSavedPosition,
            onHopperPositionChanged = onHopperPositionChanged,
        )
        return
    }

    FastScrollLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding + PaddingValues(vertical = 8.dp),
    ) {
        item {
            if (!searchQuery.isNullOrEmpty()) {
                GlobalSearchItem(
                    modifier = Modifier.fillMaxWidth(),
                    searchQuery = searchQuery,
                    onClick = onGlobalSearchClicked,
                )
            }
        }

        items(
            items = items,
            contentType = { "library_list_item" },
        ) { libraryItem -> cell(libraryItem) }
    }
}
