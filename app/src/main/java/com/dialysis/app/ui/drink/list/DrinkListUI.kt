package com.dialysis.app.ui.drink.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dialysis.app.R
import com.dialysis.app.ui.drink.DrinkCatalog
import com.dialysis.app.ui.components.TextStyles

private val PageBackground = Color(0xFFFFFFFF)
private val CardBackground = Color(0xFFE9E9EE)
private val TextDark = Color(0xFF111111)
private val HandleColor = Color(0xFF4E4E59)
private val IconFrame = Color(0xFFF8F8FB)

@Composable
fun DrinkListScreen(
    onDrinkClick: (String) -> Unit = {}
) {
    val items = listOf(
        stringResource(R.string.drink_water),
        "Nước lọc",
        "Nước trà / chè",
        stringResource(R.string.drink_coffee),
        stringResource(R.string.drink_soda),
        stringResource(R.string.drink_fruit_water),
        stringResource(R.string.drink_smoothie),
        "Bia / rượu",
        stringResource(R.string.drink_milk),
        stringResource(R.string.drink_yogurt),
        "Cháo",
        "Súp / canh",
        stringResource(R.string.drink_other)
    ).map { name ->
        DrinkItemData(
            title = name,
            onClick = { onDrinkClick(name) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 62.dp, height = 6.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(HandleColor)
        )

        Spacer(modifier = Modifier.height(16.dp))

        HeaderBar()

        Spacer(modifier = Modifier.height(18.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(items) { item ->
                DrinkCard(item)
            }
        }
    }
}

@Composable
private fun HeaderBar() {
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFF0F1F5)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⚙",
                color = Color(0xFF7A7F89),
                style = TextStyles.body
            )
        }

        Text(
            text = stringResource(R.string.drink_list_title),
            color = TextDark,
            style = TextStyles.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun DrinkCard(item: DrinkItemData) {
    Card(
        modifier = Modifier
            .height(128.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        onClick = item.onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CardBackground)
                .padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(IconFrame),
                contentAlignment = Alignment.Center
            ) {
                val visual = DrinkCatalog.resolve(item.title)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(visual.tileGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = visual.icon,
                        color = visual.iconTextColor,
                        style = TextStyles.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.title,
                color = TextDark,
                style = TextStyles.body,
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class DrinkItemData(
    val title: String,
    val onClick: () -> Unit = {}
)
