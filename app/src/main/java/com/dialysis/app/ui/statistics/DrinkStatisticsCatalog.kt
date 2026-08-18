package com.dialysis.app.ui.statistics

import androidx.compose.ui.graphics.Color
import com.dialysis.app.ui.drink.DrinkCatalog

object DrinkStatisticsCatalog {
    val visuals = listOf(
        BeverageVisualUi("nuoc loc", "Nước lọc", "💧", Color(0xFF2F9BFF)),
        BeverageVisualUi("tra", "Trà", "🍵", Color(0xFFF2B705)),
        BeverageVisualUi("ca phe", "Cà phê", "☕", Color(0xFF8B5A3C)),
        BeverageVisualUi("soda", "Nước ngọt", "🥤", Color(0xFF8B5CF6)),
        BeverageVisualUi("nuoc hoa qua", "Nước hoa quả", "🧃", Color(0xFFFF8A00)),
        BeverageVisualUi("sinh to", "Sinh tố", "🍹", Color(0xFFFFC107)),
        BeverageVisualUi("bia", "Bia / rượu", "🍺", Color(0xFFD98A2B)),
        BeverageVisualUi("sua", "Sữa", "🥛", Color(0xFFB9BCC4)),
        BeverageVisualUi("sua chua", "Sữa chua", "🥣", Color(0xFF7C5A4A)),
        BeverageVisualUi("chao", "Cháo", "🍚", Color(0xFFE9A500)),
        BeverageVisualUi("sup", "Súp / canh", "🍲", Color(0xFFE2B23B)),
        BeverageVisualUi("khac", "Khác", "🥤", Color(0xFF9AA4B2))
    )

    fun resolve(name: String): BeverageVisualUi {
        val resolved = DrinkCatalog.resolve(name)
        return visuals.firstOrNull { it.key == resolved.key }
            ?: visuals.firstOrNull { resolved.key.contains(it.key) || it.key.contains(resolved.key) }
            ?: visuals.last()
    }
}
