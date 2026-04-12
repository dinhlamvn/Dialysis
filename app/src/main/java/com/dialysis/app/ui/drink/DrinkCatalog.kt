package com.dialysis.app.ui.drink

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class DrinkVisual(
    val key: String,
    val title: String,
    val icon: String,
    val tileGradient: Brush,
    val iconFrameColor: Color = Color(0xFFF8F8FB),
    val iconTextColor: Color = Color.White
)

object DrinkCatalog {
    val items = listOf(
        DrinkVisual("nuoc", "Nước", "🥛", Brush.verticalGradient(listOf(Color(0xFF66D6FF), Color(0xFF1D9BF0)))),
        DrinkVisual("nuoc loc", "Nước lọc", "🥛", Brush.verticalGradient(listOf(Color(0xFF66D6FF), Color(0xFF1D9BF0)))),
        DrinkVisual("tra", "Trà", "🍵", Brush.verticalGradient(listOf(Color(0xFFFFD766), Color(0xFFF1A500)))),
        DrinkVisual("nuoc tra / che", "Nước trà / chè", "🍵", Brush.verticalGradient(listOf(Color(0xFFFFD766), Color(0xFFF1A500)))),
        DrinkVisual("ca phe", "Cà phê", "☕", Brush.verticalGradient(listOf(Color(0xFF8B5A3C), Color(0xFF5E3725)))),
        DrinkVisual("soda", "Nước ngọt", "🥤", Brush.verticalGradient(listOf(Color(0xFFA37EFF), Color(0xFF7E57C2)))),
        DrinkVisual("nuoc hoa qua", "Nước hoa quả", "🧃", Brush.verticalGradient(listOf(Color(0xFFFFA24A), Color(0xFFFF6A00)))),
        DrinkVisual("sinh to", "Sinh tố", "🍹", Brush.verticalGradient(listOf(Color(0xFFFFD15A), Color(0xFFF3A51E)))),
        DrinkVisual("bia", "Bia", "🍺", Brush.verticalGradient(listOf(Color(0xFFD98A2B), Color(0xFF9E5A16)))),
        DrinkVisual("bia / ruou", "Bia / rượu", "🍺", Brush.verticalGradient(listOf(Color(0xFFD98A2B), Color(0xFF9E5A16)))),
        DrinkVisual("sua", "Sữa", "🥛", Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE7ECF4))), iconTextColor = Color(0xFFB9BCC4)),
        DrinkVisual("sua chua", "Sữa chua", "☕", Brush.verticalGradient(listOf(Color(0xFF8E6C5A), Color(0xFF64493D)))),
        DrinkVisual("chao", "Cháo", "🍵", Brush.verticalGradient(listOf(Color(0xFFFFD766), Color(0xFFF1A500)))),
        DrinkVisual("sup", "Súp", "🍵", Brush.verticalGradient(listOf(Color(0xFFFFD766), Color(0xFFF1A500)))),
        DrinkVisual("sup / canh", "Súp / canh", "🍵", Brush.verticalGradient(listOf(Color(0xFFFFD766), Color(0xFFF1A500)))),
        DrinkVisual("nuoc dua", "Nước dừa", "🥥", Brush.verticalGradient(listOf(Color(0xFF7FD9A6), Color(0xFF31A36A)))),
        DrinkVisual("khac", "Khác", "?", Brush.verticalGradient(listOf(Color(0xFFE3E8EF), Color(0xFFC5CDD8))), iconTextColor = Color(0xFF7A7F89))
    )

    fun resolve(name: String): DrinkVisual {
        val normalized = name.lowercase()
            .replace("đ", "d")
            .replace("á", "a")
            .replace("à", "a")
            .replace("ả", "a")
            .replace("ã", "a")
            .replace("ạ", "a")
            .replace("ă", "a")
            .replace("ắ", "a")
            .replace("ằ", "a")
            .replace("ẳ", "a")
            .replace("ẵ", "a")
            .replace("ặ", "a")
            .replace("â", "a")
            .replace("ấ", "a")
            .replace("ầ", "a")
            .replace("ẩ", "a")
            .replace("ẫ", "a")
            .replace("ậ", "a")
            .replace("é", "e")
            .replace("è", "e")
            .replace("ẻ", "e")
            .replace("ẽ", "e")
            .replace("ẹ", "e")
            .replace("ê", "e")
            .replace("ế", "e")
            .replace("ề", "e")
            .replace("ể", "e")
            .replace("ễ", "e")
            .replace("ệ", "e")
            .replace("í", "i")
            .replace("ì", "i")
            .replace("ỉ", "i")
            .replace("ĩ", "i")
            .replace("ị", "i")
            .replace("ó", "o")
            .replace("ò", "o")
            .replace("ỏ", "o")
            .replace("õ", "o")
            .replace("ọ", "o")
            .replace("ô", "o")
            .replace("ố", "o")
            .replace("ồ", "o")
            .replace("ổ", "o")
            .replace("ỗ", "o")
            .replace("ộ", "o")
            .replace("ơ", "o")
            .replace("ớ", "o")
            .replace("ờ", "o")
            .replace("ở", "o")
            .replace("ỡ", "o")
            .replace("ợ", "o")
            .replace("ú", "u")
            .replace("ù", "u")
            .replace("ủ", "u")
            .replace("ũ", "u")
            .replace("ụ", "u")
            .replace("ư", "u")
            .replace("ứ", "u")
            .replace("ừ", "u")
            .replace("ử", "u")
            .replace("ữ", "u")
            .replace("ự", "u")
            .replace("ý", "y")
            .replace("ỳ", "y")
            .replace("ỷ", "y")
            .replace("ỹ", "y")
            .replace("ỵ", "y")
        return items.firstOrNull { normalized.contains(it.key) } ?: items.last()
    }
}
