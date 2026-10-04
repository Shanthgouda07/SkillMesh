package com.example.skillmesh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillmesh.theme.MatchedGreen
import com.example.skillmesh.theme.MatchedGreenContainer
import com.example.skillmesh.theme.MissingGrey
import com.example.skillmesh.theme.MissingGreyContainer

enum class ChipType {
    MATCHED,
    MISSING,
    NEUTRAL,
    EDITABLE
}

@Composable
fun SkillChip(
    text: String,
    type: ChipType = ChipType.NEUTRAL,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (type) {
        ChipType.MATCHED -> Pair(MatchedGreenContainer.copy(alpha = 0.6f), MatchedGreen)
        ChipType.MISSING -> Pair(MissingGreyContainer.copy(alpha = 0.4f), MissingGrey)
        ChipType.NEUTRAL -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        ChipType.EDITABLE -> Pair(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), MaterialTheme.colorScheme.primary)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (type == ChipType.MATCHED) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier
                        .size(14.dp)
                        .padding(end = 3.dp)
                )
            }

            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            if (type == ChipType.EDITABLE && onRemove != null) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove skill",
                    tint = textColor,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(14.dp)
                        .clickable { onRemove() }
                )
            }
        }
    }
}
