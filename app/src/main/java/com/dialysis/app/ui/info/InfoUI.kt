package com.dialysis.app.ui.info

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dialysis.app.R
import com.dialysis.app.ui.components.Loading
import com.dialysis.app.ui.components.PrimaryButton
import com.dialysis.app.ui.components.TextStyles
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Year

private val PageBackground = Color(0xFFFFFFFF)
private val TitleColor = Color(0xFF111111)
private val SubtitleColor = Color(0xFF9A9AA0)
private val AccentBlue = Color(0xFF1877F2)
private val AccentBlueLight = Color(0xFFD6E6FA)
private val AccentPurple = Color(0xFFCE7AE6)
private val CardBorder = Color(0xFFE1E1E6)
private val ProgressInactive = Color(0xFFE5E5EA)
private val TickColor = Color(0xFFD2D2D7)
private val IndicatorRed = Color(0xFFEA4D3D)

private const val TotalSteps = 8

@Composable
fun InfoScreen(viewModel: InfoViewModel = viewModel(), onBackClick: () -> Unit) {
    val currentStep by viewModel.currentStepState.collectAsStateWithLifecycle()
    val selectedGender by viewModel.genderState.collectAsStateWithLifecycle()
    val name by viewModel.nameState.collectAsStateWithLifecycle()
    val sessionsPerWeek by viewModel.dialysisFreqWeekState.collectAsStateWithLifecycle()
    val weightState by viewModel.weightState.collectAsStateWithLifecycle()
    val heightState by viewModel.heightState.collectAsStateWithLifecycle()
    val age by viewModel.ageState.collectAsStateWithLifecycle()
    val dialysisYear by viewModel.dialysisStartYearState.collectAsStateWithLifecycle()
    val urinePerDay by viewModel.dailyUrineMlState.collectAsStateWithLifecycle()
    val isCalculatingGoal by viewModel.isCalculatingGoalState.collectAsStateWithLifecycle()

    val weightValues = remember { (30..150).toList() }
    val heightValues = remember { (100..250).toList() }
    val ageValues = remember { (18..80).toList() }
    val urineValues = remember { (0..1500 step 25).toList() }
    val currentYear = Year.now().value
    val dialysisYearValues = remember { (1970..currentYear).toList() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            TopProgressRow(
                currentStep = currentStep,
                totalSteps = TotalSteps,
                onBack = { if (currentStep > 0) viewModel.prevStep() else onBackClick.invoke() }
            )

            Spacer(modifier = Modifier.height(80.dp))

            when (currentStep) {
                0 -> GenderStep(
                    selectedGender = selectedGender,
                    onGenderSelect = viewModel::updateGender
                )

                1 -> WeightStep(
                    weight = weightState,
                    values = weightValues,
                    onWeightChange = viewModel::updateWeight
                )

                2 -> HeightStep(
                    height = heightState,
                    values = heightValues,
                    onHeightChange = viewModel::updateHeight
                )

                3 -> AgeStep(
                    age = age,
                    values = ageValues,
                    onAgeChange = viewModel::updateAge
                )

                4 -> NameStep(name = name, onNameChange = viewModel::updateName)
                5 -> DialysisYearStep(
                    year = if (dialysisYear == 0) currentYear else dialysisYear,
                    values = dialysisYearValues,
                    onYearChange = viewModel::updateDialysisStartYear
                )

                6 -> SessionsStep(
                    value = if (sessionsPerWeek == 0) "" else sessionsPerWeek.toString(),
                    onValueChange = { input ->
                        val sanitized = input.filter { it.isDigit() }
                        viewModel.updateDialysisFreqWeek(sanitized.toIntOrNull() ?: 0)
                    }
                )

                7 -> UrineStep(
                    value = if (urinePerDay == 0) urineValues.first() else urinePerDay,
                    values = urineValues,
                    onValueChange = viewModel::updateDailyUrineMl
                )

                else -> UrineStep(
                    value = if (urinePerDay == 0) urineValues.first() else urinePerDay,
                    values = urineValues,
                    onValueChange = viewModel::updateDailyUrineMl
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = if (currentStep == TotalSteps - 1) {
                    "Done"
                } else {
                    stringResource(R.string.register_next)
                },
                onClick = {
                    if (isCalculatingGoal) return@PrimaryButton
                    if (currentStep < TotalSteps - 1) {
                        viewModel.nextStep()
                    } else {
                        viewModel.saveProfile()
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (isCalculatingGoal) {
            Loading()
        }
    }
}

@Composable
private fun TopProgressRow(
    currentStep: Int,
    totalSteps: Int,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_back),
            contentDescription = "Back",
            modifier = Modifier
                .size(24.dp)
                .padding(end = 12.dp)
                .clickable { onBack() }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(totalSteps) { index ->
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (index <= currentStep) AccentBlue else ProgressInactive)
                )
            }
        }
    }
}

@Composable
private fun GenderStep(
    selectedGender: Int,
    onGenderSelect: (Int) -> Unit
) {
    TitleBlock(
        title = stringResource(R.string.register_title),
        subtitle = stringResource(R.string.register_subtitle)
    )

    Spacer(modifier = Modifier.height(32.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GenderCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.register_gender_male),
            avatar = R.drawable.ic_male,
            isSelected = selectedGender == 1,
            accent = AccentBlue,
            onClick = { onGenderSelect(1) }
        )
        GenderCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.register_gender_female),
            avatar = R.drawable.ic_female,
            isSelected = selectedGender == 2,
            accent = AccentPurple,
            onClick = { onGenderSelect(2) }
        )
    }
}

@Composable
private fun WeightStep(
    weight: Int,
    values: List<Int>,
    onWeightChange: (Int) -> Unit
) {
    SelectionScale(
        title = stringResource(R.string.register_weight_title),
        value = weight,
        unit = stringResource(R.string.register_unit_kg),
        values = values,
        onValueChange = onWeightChange
    )
}

@Composable
private fun HeightStep(
    height: Int,
    values: List<Int>,
    onHeightChange: (Int) -> Unit
) {
    SelectionScale(
        title = stringResource(R.string.register_height_title),
        value = height,
        unit = stringResource(R.string.register_unit_cm),
        values = values,
        onValueChange = onHeightChange
    )
}

@Composable
private fun AgeStep(
    age: Int,
    values: List<Int>,
    onAgeChange: (Int) -> Unit
) {
    SelectionScale(
        title = stringResource(R.string.register_age_title),
        value = age,
        unit = stringResource(R.string.register_unit_age),
        values = values,
        onValueChange = onAgeChange
    )
}

@Composable
private fun NameStep(
    name: String,
    onNameChange: (String) -> Unit
) {
    TitleBlock(title = stringResource(R.string.register_name_title))

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun DialysisYearStep(
    year: Int,
    values: List<Int>,
    onYearChange: (Int) -> Unit
) {
    TitleBlock(title = stringResource(R.string.register_dialysis_year_title))

    Spacer(modifier = Modifier.height(24.dp))

    YearCircleScrollPicker(
        values = values,
        selectedYear = year,
        onYearChange = onYearChange
    )
}

@Composable
private fun SessionsStep(
    value: String,
    onValueChange: (String) -> Unit
) {
    TitleBlock(title = stringResource(R.string.register_sessions_title))

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun UrineStep(
    value: Int,
    values: List<Int>,
    onValueChange: (Int) -> Unit
) {
    SelectionScale(
        title = stringResource(R.string.register_urine_title),
        value = value,
        unit = stringResource(R.string.register_unit_ml),
        values = values,
        onValueChange = onValueChange
    )
}

@Composable
private fun SummaryStep(title: String, description: String) {
    TitleBlock(title = title, subtitle = description)
}

@Composable
private fun SelectionScale(
    title: String,
    value: Int,
    unit: String,
    values: List<Int>,
    onValueChange: (Int) -> Unit,
) {
    TitleBlock(title = title)

    Spacer(modifier = Modifier.height(26.dp))

    ValueDisplay(value = value.toString(), unit = unit)

    Spacer(modifier = Modifier.height(28.dp))

    ScrollSelector(
        values = values,
        selectedValue = value,
        onValueChange = onValueChange,
    )
}

@Composable
private fun YearCircleScrollPicker(
    values: List<Int>,
    selectedYear: Int,
    onYearChange: (Int) -> Unit
) {
    val itemSize = 74.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val selectedIndex = values.indexOf(selectedYear).coerceAtLeast(0)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex
    )
    val horizontalPadding = (screenWidth - itemSize) / 2f

    LaunchedEffect(selectedYear) {
        val index = values.indexOf(selectedYear).coerceAtLeast(0)
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(values.size) { index ->
            val year = values[index]
            val isSelected = year == selectedYear
            Box(
                modifier = Modifier
                    .size(itemSize)
                    .clip(CircleShape)
                    .background(if (isSelected) AccentBlue else Color(0xFFF3F3F7))
                    .border(1.dp, CardBorder, CircleShape)
                    .clickable { onYearChange(year) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = year.toString(),
                    color = if (isSelected) Color.White else TitleColor,
                    style = TextStyles.titleMedium
                )
            }
        }
    }
}


@Composable
private fun TitleBlock(title: String, subtitle: String? = null) {
    Text(
        text = title,
        color = TitleColor,
        style = TextStyles.titleMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    if (subtitle != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = subtitle,
            color = SubtitleColor,
            style = TextStyles.body,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ValueDisplay(value: String, unit: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = TitleColor,
            style = TextStyles.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = unit,
            color = SubtitleColor,
            style = TextStyles.body
        )
    }
}

@Composable
private fun ScrollSelector(
    values: List<Int>,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
) {
    val itemWidth = 72.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = (screenWidth - itemWidth - 48.dp) / 2f
    val selectedIndex = values.indexOf(selectedValue).coerceAtLeast(0)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex
    )
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    LaunchedEffect(listState, selectedValue) {
        snapshotFlow { listState.layoutInfo }
            .map { layoutInfo ->
                val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                val closest = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                    kotlin.math.abs((item.offset + item.size / 2) - center)
                }
                closest?.index
            }
            .distinctUntilChanged()
            .collect { index ->
                val safeIndex = index ?: return@collect
                if (safeIndex in values.indices) {
                    val newValue = values[safeIndex]
                    if (newValue != selectedValue) onValueChange(newValue)
                }
            }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(48.dp)
                .width(2.dp)
                .background(IndicatorRed)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            state = listState,
            flingBehavior = snapFlingBehavior,
            contentPadding = PaddingValues(horizontal = horizontalPadding),
        ) {
            items(values.size) { index ->
                val value = values[index]
                val isSelected = value == selectedValue
                Column(
                    modifier = Modifier.width(itemWidth),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .height(18.dp)
                            .width(2.dp)
                            .background(if (isSelected) IndicatorRed else TickColor)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = value.toString(),
                        color = if (isSelected) TitleColor else SubtitleColor,
                        style = if (isSelected) TextStyles.bodyMedium else TextStyles.body
                    )
                }
            }
        }
    }
}

@Composable
private fun CirclePicker(values: List<String>, selectedIndex: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        values.forEachIndexed { index, value ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) AccentBlue else Color(0xFFF3F3F7))
                    .border(1.dp, CardBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value,
                    color = if (isSelected) Color.White else TitleColor,
                    style = TextStyles.titleMedium
                )
            }
        }
    }
}

@Composable
private fun GenderCard(
    modifier: Modifier,
    title: String,
    @DrawableRes avatar: Int,
    isSelected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) accent else Color.White
    val textColor = if (isSelected) Color.White else TitleColor
    val borderColor = if (isSelected) accent else CardBorder
    val avatarColor = if (isSelected) accent.copy(alpha = 0.25f) else AccentBlueLight

    Column(
        modifier = modifier
            .height(190.dp)
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .background(containerColor, RoundedCornerShape(22.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(avatar),
                modifier = Modifier.fillMaxSize(),
                contentDescription = "avatar"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            color = textColor,
            style = TextStyles.titleMedium
        )
    }
}
