package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EarthAmber
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.LeafGreenAccent
import com.example.ui.theme.LeafGreenBright

/**
 * Custom inline logo featuring a pocket calculator outline where
 * two stylized green farm leaves sprout from the top-right display corner.
 */
@Composable
fun AgriCalcLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    calculatorColor: Color = ForestGreenPrimary,
    displayColor: Color = Color(0xFF10281C),
    leafColor: Color = LeafGreenAccent,
    leafHighlight: Color = LeafGreenBright,
    keyColor: Color = LeafGreenAccent
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = (w * 0.045f).coerceAtLeast(1.5f)

        // Calculator bounds: takes left ~75% and bottom ~85% of canvas to leave room for leaves sprouting top-right
        val calcLeft = w * 0.06f
        val calcTop = h * 0.22f
        val calcWidth = w * 0.70f
        val calcHeight = h * 0.74f
        val calcRadius = w * 0.08f

        // 1. Calculator Body (Filled with rounded rect)
        drawRoundRect(
            color = calculatorColor,
            topLeft = Offset(calcLeft, calcTop),
            size = Size(calcWidth, calcHeight),
            cornerRadius = CornerRadius(calcRadius, calcRadius),
            style = Fill
        )

        // Calculator Body Outline (Crisp high-contrast stroke)
        drawRoundRect(
            color = Color.White.copy(alpha = 0.35f),
            topLeft = Offset(calcLeft, calcTop),
            size = Size(calcWidth, calcHeight),
            cornerRadius = CornerRadius(calcRadius, calcRadius),
            style = Stroke(width = strokeWidth)
        )

        // 2. LCD Display Screen
        val dispLeft = calcLeft + calcWidth * 0.10f
        val dispTop = calcTop + calcHeight * 0.08f
        val dispWidth = calcWidth * 0.80f
        val dispHeight = calcHeight * 0.22f
        val dispRadius = calcRadius * 0.5f

        drawRoundRect(
            color = displayColor,
            topLeft = Offset(dispLeft, dispTop),
            size = Size(dispWidth, dispHeight),
            cornerRadius = CornerRadius(dispRadius, dispRadius),
            style = Fill
        )

        // Mini readout digits line inside display
        drawLine(
            color = leafHighlight,
            start = Offset(dispLeft + dispWidth * 0.45f, dispTop + dispHeight * 0.60f),
            end = Offset(dispLeft + dispWidth * 0.85f, dispTop + dispHeight * 0.60f),
            strokeWidth = strokeWidth * 0.9f,
            cap = StrokeCap.Round
        )

        // 3. Calculator Keypad (3 rows x 3 cols)
        val keypadTop = calcTop + calcHeight * 0.38f
        val keypadHeight = calcHeight * 0.54f
        val rows = 3
        val cols = 3
        val colStep = (calcWidth * 0.80f) / cols
        val rowStep = keypadHeight / rows
        val keySize = (colStep.coerceAtMost(rowStep)) * 0.68f

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cx = dispLeft + c * colStep + (colStep - keySize) / 2f
                val cy = keypadTop + r * rowStep + (rowStep - keySize) / 2f
                val isSpecialKey = (r == 2 && c == 2)
                drawRoundRect(
                    color = if (isSpecialKey) EarthAmber else keyColor.copy(alpha = 0.85f),
                    topLeft = Offset(cx, cy),
                    size = Size(keySize, keySize),
                    cornerRadius = CornerRadius(keySize * 0.28f, keySize * 0.28f),
                    style = Fill
                )
            }
        }

        // 4. Stylized Green Farm Leaves Sprouting from top-right display corner
        // Primary Leaf: emerges from (calcLeft + calcWidth * 0.85f, calcTop) towards canvas top-right
        val sproutBaseX = calcLeft + calcWidth * 0.80f
        val sproutBaseY = calcTop + calcHeight * 0.06f

        // Leaf 1 (Main large leaf curling up-right)
        val leaf1Path = Path().apply {
            moveTo(sproutBaseX, sproutBaseY)
            // Left curve to tip
            cubicTo(
                sproutBaseX - w * 0.08f, sproutBaseY - h * 0.16f,
                sproutBaseX + w * 0.02f, sproutBaseY - h * 0.26f,
                sproutBaseX + w * 0.18f, sproutBaseY - h * 0.22f // Tip
            )
            // Right curve back to base
            cubicTo(
                sproutBaseX + w * 0.20f, sproutBaseY - h * 0.08f,
                sproutBaseX + w * 0.12f, sproutBaseY - h * 0.01f,
                sproutBaseX, sproutBaseY
            )
            close()
        }

        // Leaf 1 Fill
        drawPath(
            path = leaf1Path,
            color = leafColor,
            style = Fill
        )

        // Leaf 1 Outline & Central Vein
        drawPath(
            path = leaf1Path,
            color = Color.White.copy(alpha = 0.4f),
            style = Stroke(width = strokeWidth * 0.8f, join = StrokeJoin.Round)
        )
        drawLine(
            color = leafHighlight,
            start = Offset(sproutBaseX, sproutBaseY),
            end = Offset(sproutBaseX + w * 0.15f, sproutBaseY - h * 0.19f),
            strokeWidth = strokeWidth * 0.7f,
            cap = StrokeCap.Round
        )

        // Leaf 2 (Second smaller secondary leaf branching left-up)
        val leaf2Path = Path().apply {
            moveTo(sproutBaseX, sproutBaseY)
            cubicTo(
                sproutBaseX + w * 0.01f, sproutBaseY - h * 0.10f,
                sproutBaseX + w * 0.12f, sproutBaseY - h * 0.16f,
                sproutBaseX + w * 0.22f, sproutBaseY - h * 0.08f // Tip
            )
            cubicTo(
                sproutBaseX + w * 0.18f, sproutBaseY - h * 0.02f,
                sproutBaseX + w * 0.06f, sproutBaseY + h * 0.02f,
                sproutBaseX, sproutBaseY
            )
            close()
        }

        drawPath(
            path = leaf2Path,
            color = leafHighlight,
            style = Fill
        )
        drawPath(
            path = leaf2Path,
            color = Color.White.copy(alpha = 0.35f),
            style = Stroke(width = strokeWidth * 0.6f, join = StrokeJoin.Round)
        )
    }
}
