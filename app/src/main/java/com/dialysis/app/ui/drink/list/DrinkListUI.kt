package com.dialysis.app.ui.drink.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dialysis.app.R
import com.dialysis.app.router.Router

private val PageBackground = Color(0xFFFFFFFF)
private val CardBackground = Color(0xFFE9E9EE)
private val TextDark = Color(0xFF111111)
private val IconGray = Color(0xFFB9BCC4)

@Composable
fun DrinkListScreen() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        TopBar()

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.drink_list_title),
            color = TextDark,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        val items = listOf(
            DrinkItemData(stringResource(R.string.drink_water)),
            DrinkItemData(stringResource(R.string.drink_coffee)),
            DrinkItemData(stringResource(R.string.drink_tea)),
            DrinkItemData(stringResource(R.string.drink_soda)),
            DrinkItemData(stringResource(R.string.drink_fruit_water)),
            DrinkItemData(stringResource(R.string.drink_milk)),
            DrinkItemData(stringResource(R.string.drink_yogurt)),
            DrinkItemData(stringResource(R.string.drink_smoothie)),
            DrinkItemData(stringResource(R.string.drink_beer)),
            DrinkItemData(stringResource(R.string.drink_coconut)),
            DrinkItemData(stringResource(R.string.drink_soup)),
            DrinkItemData(
                stringResource(R.string.drink_other),
                onClick = { context.startActivity(Router.createDrink(context)) }
            )
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(items) { item ->
                DrinkCard(item)
            }
        }
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFF2F2F7)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⚙",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun DrinkCard(item: DrinkItemData) {
    Card(
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = item.onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CardBackground)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(IconGray)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                color = TextDark,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class DrinkItemData(
    val title: String,
    val onClick: () -> Unit = {}
)
