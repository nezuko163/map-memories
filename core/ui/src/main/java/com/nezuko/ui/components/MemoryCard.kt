package com.nezuko.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nezuko.domain.model.Memory
import com.nezuko.ui.theme.Spacing

@Composable
fun MemoryCard(
    modifier: Modifier = Modifier,
    memory: Memory,
    onCardClick: (Memory) -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick(memory) },
        elevation = CardDefaults.elevatedCardElevation(
            5.dp
        ),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        ImageWithRatio(
            url = memory.photoUrl,
            contentDescription = memory.name
        )
        Text(
            text = memory.name,
            modifier = Modifier
                .padding(Spacing.tiny)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}
