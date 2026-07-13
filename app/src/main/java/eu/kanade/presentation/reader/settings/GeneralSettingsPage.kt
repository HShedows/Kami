package eu.kanade.presentation.reader.settings

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import eu.kanade.tachiyomi.ui.reader.setting.ReaderPreferences
import eu.kanade.tachiyomi.ui.reader.setting.ReaderSettingsScreenModel
import eu.kanade.tachiyomi.ui.reader.setting.ReadingMode
import eu.kanade.tachiyomi.util.system.hasDisplayCutout
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.CheckboxItem
import tachiyomi.presentation.core.components.MultiSpinnerItem
import tachiyomi.presentation.core.components.SpinnerItem
import tachiyomi.presentation.core.components.SliderItem
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState

private val themes = listOf(
    MR.strings.black_background to 1,
    MR.strings.gray_background to 2,
    MR.strings.white_background to 0,
    MR.strings.automatic_background to 3,
)

private val flashColors = listOf(
    MR.strings.pref_flash_style_black to ReaderPreferences.FlashColor.BLACK,
    MR.strings.pref_flash_style_white to ReaderPreferences.FlashColor.WHITE,
    MR.strings.pref_flash_style_white_black to ReaderPreferences.FlashColor.WHITE_BLACK,
)

@Composable
internal fun ColumnScope.GeneralPage(screenModel: ReaderSettingsScreenModel) {
    val readerTheme by screenModel.preferences.readerTheme.collectAsState()

    val flashPageState by screenModel.preferences.flashOnPageChange.collectAsState()

    val flashMillisPref = screenModel.preferences.flashDurationMillis
    val flashMillis by flashMillisPref.collectAsState()

    val flashIntervalPref = screenModel.preferences.flashPageInterval
    val flashInterval by flashIntervalPref.collectAsState()

    val flashColorPref = screenModel.preferences.flashColor
    val flashColor by flashColorPref.collectAsState()

    val themeLabels = themes.map { stringResource(it.first) }.toTypedArray()
    val selectedThemeIndex = remember(readerTheme) { themes.indexOfFirst { it.second == readerTheme }.coerceAtLeast(0) }
    SpinnerItem(
        label = stringResource(MR.strings.pref_reader_theme),
        options = themeLabels,
        selectedIndex = selectedThemeIndex,
        onSelect = { screenModel.preferences.readerTheme.set(themes[it].second) },
    )

    CheckboxItem(
        label = stringResource(MR.strings.pref_show_page_number),
        pref = screenModel.preferences.showPageNumber,
    )

    val verticalNavigatorModes by screenModel.preferences.verticalNavigator.collectAsState()

    val verticalNavigatorOptions = ReadingMode.entries.filter { it != ReadingMode.DEFAULT }
    val verticalNavigatorLabels = verticalNavigatorOptions.map { stringResource(it.stringRes) }
    val selectedIndices = remember(verticalNavigatorModes) {
        verticalNavigatorOptions.mapIndexedNotNull { index, mode ->
            if (verticalNavigatorModes.contains(mode)) index else null
        }.toSet()
    }
    MultiSpinnerItem(
        label = stringResource(MR.strings.pref_vertical_navigator),
        options = verticalNavigatorLabels.toTypedArray(),
        selectedIndices = selectedIndices,
        onSelect = { index ->
            val mode = verticalNavigatorOptions[index]
            val newModes = if (verticalNavigatorModes.contains(mode)) {
                verticalNavigatorModes - mode
            } else {
                verticalNavigatorModes + mode
            }
            screenModel.preferences.verticalNavigator.set(newModes)
        },
    )

    if (verticalNavigatorModes.isNotEmpty()) {
        val verticalNavigatorHeightPref = screenModel.preferences.verticalNavigatorHeight
        val verticalNavigatorHeight by verticalNavigatorHeightPref.collectAsState()

        CheckboxItem(
            label = stringResource(MR.strings.pref_webtoon_vertical_navigator_on_left),
            pref = screenModel.preferences.verticalNavigatorOnLeft,
        )

        SliderItem(
            label = stringResource(MR.strings.pref_vertical_navigator_height),
            value = verticalNavigatorHeight,
            valueRange = 65..100,
            steps = 6,
            onChange = { verticalNavigatorHeightPref.set(it) },
        )
    }

    CheckboxItem(
        label = stringResource(MR.strings.pref_fullscreen),
        pref = screenModel.preferences.fullscreen,
    )

    val isFullscreen by screenModel.preferences.fullscreen.collectAsState()
    if (LocalActivity.current?.hasDisplayCutout() == true && isFullscreen) {
        CheckboxItem(
            label = stringResource(MR.strings.pref_cutout_short),
            pref = screenModel.preferences.drawUnderCutout,
        )
    }

    CheckboxItem(
        label = stringResource(MR.strings.pref_keep_screen_on),
        pref = screenModel.preferences.keepScreenOn,
    )

    CheckboxItem(
        label = stringResource(MR.strings.pref_read_with_long_tap),
        pref = screenModel.preferences.readWithLongTap,
    )

    CheckboxItem(
        label = stringResource(MR.strings.pref_always_show_chapter_transition),
        pref = screenModel.preferences.alwaysShowChapterTransition,
    )

    CheckboxItem(
        label = stringResource(MR.strings.pref_page_transitions),
        pref = screenModel.preferences.pageTransitions,
    )

    CheckboxItem(
        label = stringResource(MR.strings.pref_disable_swipe_between_pages),
        pref = screenModel.preferences.disableSwipeBetweenPages,
    )
    CheckboxItem(
        label = stringResource(MR.strings.pref_flash_page),
        pref = screenModel.preferences.flashOnPageChange,
    )
    if (flashPageState) {
        SliderItem(
            value = flashMillis / ReaderPreferences.MILLI_CONVERSION,
            valueRange = 1..15,
            label = stringResource(MR.strings.pref_flash_duration),
            valueString = stringResource(MR.strings.pref_flash_duration_summary, flashMillis),
            onChange = { flashMillisPref.set(it * ReaderPreferences.MILLI_CONVERSION) },
            pillColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
        SliderItem(
            value = flashInterval,
            valueRange = 1..10,
            label = stringResource(MR.strings.pref_flash_page_interval),
            valueString = pluralStringResource(MR.plurals.pref_pages, flashInterval, flashInterval),
            onChange = {
                flashIntervalPref.set(it)
            },
            pillColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
        val flashColorLabels = flashColors.map { stringResource(it.first) }.toTypedArray()
        val selectedFlashColorIndex = remember(flashColor) { flashColors.indexOfFirst { it.second == flashColor }.coerceAtLeast(0) }
        SpinnerItem(
            label = stringResource(MR.strings.pref_flash_with),
            options = flashColorLabels,
            selectedIndex = selectedFlashColorIndex,
            onSelect = { flashColorPref.set(flashColors[it].second) },
        )
    }
}
