package com.dialysis.app.ui.home

import android.app.Activity
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.extensions.toast
import com.dialysis.app.notification.WaterReminderScheduler
import com.dialysis.app.ui.components.TextStyles
import com.dialysis.app.ui.daily.DailyReportViewModel
import com.dialysis.app.ui.home.tabs.SettingsViewModel
import com.dialysis.app.ui.home.tabs.SettingsScreen
import com.dialysis.app.ui.home.tabs.StatisticsScreen
import com.dialysis.app.ui.weight.WeightScreen
import com.dialysis.app.ui.weight.WeightViewModel
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dialysis.app.R
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : BaseActivity() {
    private val homeViewModel: HomeViewModel by viewModel()
    private val dailyReportViewModel: DailyReportViewModel by viewModel()
    private val weightViewModel: WeightViewModel by viewModel()
    private val settingsViewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WaterReminderScheduler.schedule(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    homeViewModel.flowOf { it.showSymptomSubmitSuccessToast }.collect { shouldShow ->
                        if (shouldShow) {
                            this@HomeActivity.toast(getString(R.string.symptom_submit_success))
                            homeViewModel.clearSymptomSubmitSuccessToast()
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun ContentView() {
        val pagerState = rememberPagerState(pageCount = { 4 })
        val scope = rememberCoroutineScope()
        val todayTotalMl by homeViewModel.todayTotalMlState.collectAsStateWithLifecycle()
        val weekTotalMl by homeViewModel.weekTotalMlState.collectAsStateWithLifecycle()
        val monthTotalMl by homeViewModel.monthTotalMlState.collectAsStateWithLifecycle()
        val weekDailyMl by homeViewModel.weekDailyMlState.collectAsStateWithLifecycle()
        val dailyTotals by homeViewModel.dailyTotalsState.collectAsStateWithLifecycle()
        val dailyWaterGoalMl by homeViewModel.dailyWaterGoalMlState.collectAsStateWithLifecycle()
        val isHistorySyncing by homeViewModel.isHistorySyncingState.collectAsStateWithLifecycle()
        val statusBarColor = if (pagerState.currentPage == 0) {
            Color(0xFF2D6FDD)
        } else {
            Color(0xFFF7F8FA)
        }

        ApplyStatusBarColor(
            color = statusBarColor,
            darkIcons = pagerState.currentPage != 0
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 72.dp)
            ) { page ->
                when (page) {
                    0 -> HomeScreen(
                        viewModel = homeViewModel,
                        dailyReportViewModel = dailyReportViewModel,
                        showBottomNav = false,
                        onStatisticsMoreClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        },
                        onWeightProgressClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    )
                    1 -> WeightScreen(
                        viewModel = weightViewModel,
                        showBottomNav = false
                    )
                    2 -> StatisticsScreen(
                        todayTotalMl = todayTotalMl,
                        dailyGoalMl = dailyWaterGoalMl,
                        weekTotalMl = weekTotalMl,
                        monthTotalMl = monthTotalMl,
                        weekDailyMl = weekDailyMl,
                        dailyTotals = dailyTotals,
                        dailyReportViewModel = dailyReportViewModel
                    )
                    3 -> SettingsScreen(viewModel = settingsViewModel)
                }
            }

            HomePagerBottomBar(
                selectedTab = pagerState.currentPage,
                onSelectTab = { page ->
                    scope.launch {
                        pagerState.animateScrollToPage(page)
                    }
                },
                onAddClick = {
                    homeViewModel.openDrinkListSheet()
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            if (isHistorySyncing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Text(text = stringResource(id = R.string.main_syncing))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplyStatusBarColor(
    color: Color,
    darkIcons: Boolean
) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = color.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkIcons
    }
}

@Composable
private fun HomePagerBottomBar(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.White)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PagerBottomItem(
                    item = BottomTabItem.Water,
                    active = selectedTab == BottomTabItem.Water.page,
                    onClick = { onSelectTab(BottomTabItem.Water.page) },
                    modifier = Modifier.weight(1f)
                )
                PagerBottomItem(
                    item = BottomTabItem.Weight,
                    active = selectedTab == BottomTabItem.Weight.page,
                    onClick = { onSelectTab(BottomTabItem.Weight.page) },
                    modifier = Modifier.weight(1f)
                )
                PagerAddButton(onClick = onAddClick)
                PagerBottomItem(
                    item = BottomTabItem.Statistics,
                    active = selectedTab == BottomTabItem.Statistics.page,
                    onClick = { onSelectTab(BottomTabItem.Statistics.page) },
                    modifier = Modifier.weight(1f)
                )
                PagerBottomItem(
                    item = BottomTabItem.Settings,
                    active = selectedTab == BottomTabItem.Settings.page,
                    onClick = { onSelectTab(BottomTabItem.Settings.page) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PagerBottomItem(
    item: BottomTabItem,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = stringResource(item.labelRes)
    val itemColor = if (active) TabActiveColor else TabInactiveColor

    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(item.iconRes),
                contentDescription = label,
                tint = itemColor,
                modifier = Modifier
                    .size(22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = itemColor,
                style = TextStyles.caption
            )
        }
    }
}

@Composable
private fun PagerAddButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(TabAddButtonSize)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = TabActiveColor.copy(alpha = 0.28f),
                spotColor = TabActiveColor.copy(alpha = 0.28f)
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF6CB7FF),
                        TabActiveColor,
                        Color(0xFF0B4FCB)
                    )
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            color = Color.White,
            style = TextStyles.titleMedium
        )
    }
}

private enum class BottomTabItem(
    val page: Int,
    val labelRes: Int,
    val iconRes: Int
) {
    Water(
        page = 0,
        labelRes = R.string.home_nav_water,
        iconRes = R.drawable.ic_tab_water
    ),
    Weight(
        page = 1,
        labelRes = R.string.home_nav_weight,
        iconRes = R.drawable.ic_tab_weight
    ),
    Statistics(
        page = 2,
        labelRes = R.string.home_nav_stats,
        iconRes = R.drawable.ic_tab_statistics
    ),
    Settings(
        page = 3,
        labelRes = R.string.home_nav_settings,
        iconRes = R.drawable.ic_tab_settings
    )
}

private val TabActiveColor = Color(0xFF1877F2)
private val TabInactiveColor = Color(0xFF8E8E93)
private val TabAddButtonSize = 44.dp
