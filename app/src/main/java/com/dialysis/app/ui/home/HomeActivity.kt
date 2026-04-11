package com.dialysis.app.ui.home

import android.app.Activity
import android.os.Bundle
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.notification.WaterReminderScheduler
import com.dialysis.app.ui.components.TextStyles
import com.dialysis.app.ui.daily.DailyReportViewModel
import com.dialysis.app.ui.home.tabs.SettingsScreen
import com.dialysis.app.ui.home.tabs.StatisticsScreen
import com.dialysis.app.ui.weight.WeightScreen
import com.dialysis.app.ui.weight.WeightViewModel
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : BaseActivity() {
    private val homeViewModel: HomeViewModel by viewModel()
    private val dailyReportViewModel: DailyReportViewModel by viewModel()
    private val weightViewModel: WeightViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WaterReminderScheduler.schedule(this)
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
                    .padding(bottom = 120.dp)
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
                        weekTotalMl = weekTotalMl,
                        monthTotalMl = monthTotalMl,
                        weekDailyMl = weekDailyMl,
                        dailyTotals = dailyTotals,
                        dailyReportViewModel = dailyReportViewModel
                    )
                    3 -> SettingsScreen()
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
            .fillMaxWidth()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PagerBottomItem(
                label = "Nước của tôi",
                active = selectedTab == 0,
                onClick = { onSelectTab(0) }
            )
            PagerBottomItem(
                label = "Cân nặng",
                active = selectedTab == 1,
                onClick = { onSelectTab(1) }
            )
            Button(
                onClick = onAddClick,
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    style = TextStyles.titleMedium
                )
            }
            PagerBottomItem(
                label = "Thống kê",
                active = selectedTab == 2,
                onClick = { onSelectTab(2) }
            )
            PagerBottomItem(
                label = "Cài đặt",
                active = selectedTab == 3,
                onClick = { onSelectTab(3) }
            )
        }
    }
}

@Composable
private fun PagerBottomItem(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Transparent)
            .padding(vertical = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (active) Color(0xFF1877F2) else Color(0xFFBFC3CB))
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                color = if (active) Color(0xFF1877F2) else Color(0xFF8E8E93),
                style = TextStyles.caption
            )
        }
    }
}
