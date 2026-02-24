package com.dialysis.app.ui.intro

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dialysis.app.R
import com.dialysis.app.router.Router
import com.dialysis.app.ui.components.PrimaryButton
import kotlinx.coroutines.launch
import kotlin.math.abs

private val PageBackground = Color(0xFFFFFFFF)
private val TitleColor = Color(0xFF111111)
private val CardBackground = Color(0xFFF4F3F8)
private val AccentBlue = Color(0xFF1877F2)
private val BodyGray = Color(0xFF8E8E93)
private val MutedGray = Color(0xFFB8B8BD)
private val AccentPurple = Color(0xFF9C5ACD)
private val AccentYellow = Color(0xFFF2B705)
private val AccentGreen = Color(0xFF2FB15B)
private val AccentOrange = Color(0xFFF28A1A)

private val AccentBlueLight = Color(0xFFD6E6FA)
private val AccentPurpleLight = Color(0xFFE9D7F3)
private val AccentYellowLight = Color(0xFFF7E7B8)
private val AccentGreenLight = Color(0xFFD6F1DF)
private val AccentOrangeLight = Color(0xFFF7E2CC)

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun IntroScreen() {
    val items = listOf(
        IntroCardData(
            title = stringResource(R.string.intro_card_water_title),
            description = stringResource(R.string.intro_card_water_desc),
            accent = AccentBlue,
            accentLight = AccentBlueLight
        ),
        IntroCardData(
            title = stringResource(R.string.intro_card_rewards_title),
            description = stringResource(R.string.intro_card_rewards_desc),
            accent = AccentPurple,
            accentLight = AccentPurpleLight
        ),
        IntroCardData(
            title = stringResource(R.string.intro_card_notifications_title),
            description = stringResource(R.string.intro_card_notifications_desc),
            accent = AccentYellow,
            accentLight = AccentYellowLight
        ),
        IntroCardData(
            title = stringResource(R.string.intro_card_stats_title),
            description = stringResource(R.string.intro_card_stats_desc),
            accent = AccentGreen,
            accentLight = AccentGreenLight
        ),
        IntroCardData(
            title = stringResource(R.string.intro_card_profile_title),
            description = stringResource(R.string.intro_card_profile_desc),
            accent = AccentOrange,
            accentLight = AccentOrangeLight
        )
    )
    val pageCount = Int.MAX_VALUE
    val startIndex = 1000
    val pagerState = rememberPagerState(initialPage = startIndex) { pageCount }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = screenWidth * 0.7f
    val horizontalPadding = ((screenWidth - cardWidth) / 2f).coerceAtLeast(0.dp)
    val selectedIndex = ((pagerState.currentPage % items.size) + items.size) % items.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.intro_title),
            color = TitleColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            HorizontalPager(
                state = pagerState,
                pageSize = PageSize.Fixed(cardWidth),
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                pageSpacing = 18.dp,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val pageOffset =
                    abs(pagerState.currentPage - page + pagerState.currentPageOffsetFraction)
                val alpha = 1f - (pageOffset * 0.5f).coerceIn(0f, 0.5f)
                val clampedOffset = pageOffset.coerceIn(0f, 1f)
                val scale = 0.7f + (1f - clampedOffset) * 0.3f
                val itemIndex = ((page % items.size) + items.size) % items.size
                IntroCard(
                    data = items[itemIndex],
                    cardAlpha = alpha,
                    scale = scale,
                    cardWidth = cardWidth
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DotsIndicator(
                totalDots = items.size,
                selectedIndex = selectedIndex,
                onDotClick = { index ->
                    if (index == selectedIndex) return@DotsIndicator
                    val forward = (index - selectedIndex + items.size) % items.size
                    val backward = (selectedIndex - index + items.size) % items.size
                    val targetPage = if (forward <= backward) {
                        pagerState.currentPage + forward
                    } else {
                        pagerState.currentPage - backward
                    }
                    scope.launch {
                        pagerState.animateScrollToPage(targetPage)
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

        PrimaryButton(
            text = stringResource(R.string.intro_button_signup),
            onClick = { context.startActivity(Router.register(context)) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

            Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(R.string.intro_text_login),
            color = AccentBlue,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { context.startActivity(Router.home(context)) }
        )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.intro_text_skip),
                color = BodyGray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun IntroCard(
    data: IntroCardData,
    cardAlpha: Float,
    scale: Float,
    cardWidth: Dp
) {
    Card(
        modifier = Modifier
            .width(cardWidth)
            .fillMaxHeight()
            .alpha(cardAlpha)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(data.accentLight),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(data.accent)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = data.title,
                color = data.accent,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = data.description,
                color = BodyGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int,
    onDotClick: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalDots) { index ->
            val width = if (index == selectedIndex) 26.dp else 8.dp
            val color = if (index == selectedIndex) AccentBlue else MutedGray
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
                    .clickable { onDotClick(index) }
            )
        }
    }
}

private data class IntroCardData(
    val title: String,
    val description: String,
    val accent: Color,
    val accentLight: Color
)
