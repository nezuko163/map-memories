package com.nezuko.memorydetails

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.nezuko.domain.model.Location
import com.nezuko.domain.model.Memory
import com.nezuko.domain.model.PhotoWithText
import com.nezuko.domain.model.User
import com.nezuko.ui.components.CollapsingTopBar
import com.nezuko.ui.components.Image
import com.nezuko.ui.components.ImageWithRatio
import com.nezuko.ui.components.rememberCollapsingTopBarState
import com.nezuko.ui.theme.Spacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryDetailsRoute(
    id: Int,
    navigateBack: () -> Unit,
    vm: MemoryDetailsViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        vm.load(id)
    }

    val memoryState by vm.memory.collectAsState()
    if (memoryState == null) return
    val memory = memoryState!!

    MemoryDetailsScreen(memory)
    return

    val scrollBehavior = rememberCollapsingTopBarState()

    Scaffold(
        topBar = {
            CollapsingTopBar(state = scrollBehavior) {
                Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                        )
                    }
                    Text(
                        memory.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Favorite") },
                onClick = { /* TODO: favorite action */ },
                icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorite") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .nestedScroll(scrollBehavior.connection)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                Image(memory.photoUrl, Modifier.fillMaxSize())

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xAA000000)),
                                startY = 180f
                            )
                        )
                )

                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                        .clickable { },
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 6.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Image(
                            memory.author.photoUrl,
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = memory.author.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = formatDate(memory.createdAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // meta over image (location) bottom-left
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Предполагаемое имя локации",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = formatDate(memory.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row with quick actions (link to map, share, etc.)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { /* open map */ },
                    label = { Text("Предполагаемое имя локации") })
                AssistChip(
                    onClick = { /* open link */ },
                    leadingIcon = { Icon(Icons.Default.PlayArrow, null) },
                    label = { Text("Open") })
                AssistChip(
                    onClick = { },
                    leadingIcon = { Icon(Icons.Default.Share, null) },
                    label = { Text("Share") })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description with read-more
            var expanded by remember { mutableStateOf(false) }
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(text = "Описание", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = memory.description,
                    maxLines = if (expanded) Int.MAX_VALUE else 4,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.animateContentSize()
                )
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Свернуть" else "Читать полностью")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Photo gallery
            if (memory.photosUrls.isNotEmpty()) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 32.dp)) {
                    Text(text = "Фотогалерея", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        Modifier
                            .fillMaxWidth(),
                        state = rememberLazyListState(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            memory.photosUrls,
                            key = { index, url -> index })
                        { _, url ->
                            ImageWithRatio(
                                Modifier
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                url
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(epochMillis: Long): String {
    val instant = Instant.ofEpochMilli(epochMillis)
    val fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm").withZone(ZoneId.systemDefault())
    return fmt.format(instant)
}

@Composable
fun MemoryDetailsScreen(
    memory: Memory,
    navigateBack: () -> Unit = {},
    onFavorite: (Memory) -> Unit = {}
) {
    val state = rememberCollapsingTopBarState()
    val density = LocalDensity.current
    var showMap by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val visibleFraction = state.collapseFraction

    var fullscreenUrl by remember { mutableStateOf<String?>(null) }


    fullscreenUrl?.let { url ->
        Dialog(onDismissRequest = { fullscreenUrl = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                ImageWithRatio(
                    url = url,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                )
                IconButton(
                    onClick = { fullscreenUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CollapsingTopBar(state = state) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            memory.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = (18 * (1f - (1f - visibleFraction) * 0.12f)).sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (visibleFraction > 0.6f) {
                            Text(
                                "memory.location.name",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    ) {
        // Content column — attach nestedScroll to this scrollable container
        Column(
            modifier = Modifier
                .padding(it)
                .nestedScroll(state.connection)
                .verticalScroll(scrollState)
        ) {
            // Hero image with parallax
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                ImageWithRatio(
                    url = memory.photoUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(Spacing.small)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(24.dp)
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .padding(vertical = Spacing.small, horizontal = 12.dp)
                        .align(Alignment.TopStart),
                ) {
                    Image(
                        memory.author.photoUrl,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            memory.author.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            formatDate(memory.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        "memory.location.name",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                    Text(
                        formatDate(memory.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }


            Spacer(Modifier.height(16.dp))

            // Title + quick chips
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(memory.name, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "memory.location.name",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    OutlinedButton(onClick = { /* share */ }) { Text("Share") }
                }

                Spacer(Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(memory.tags, key = { index, tag -> index }) { index, tag ->
                        AssistChip(onClick = { }, label = { Text(tag) })
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = Spacing.medium)
            ) {
                itemsIndexed(memory.photosUrls) { _, url ->
                    ImageWithRatio(
                        url = url, modifier = Modifier
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }


            Spacer(Modifier.height(16.dp))

            // Description with simple read-more
            var expanded by remember { mutableStateOf(false) }
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Описание", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = memory.description,
                    maxLines = if (expanded) Int.MAX_VALUE else 4,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Свернуть" else "Читать полностью")
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Таймлайн", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                memory.photosWithText.forEach { photoWithText ->
                    // Each timeline item
                    var expandedDesc by remember { mutableStateOf(false) }

                    // image card
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .animateContentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { fullscreenUrl = photoWithText.photoUrl },
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        ImageWithRatio(
                            url = photoWithText.photoUrl,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            photoWithText.name,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            formatDate(photoWithText.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                        )

                        Text(
                            text = photoWithText.text,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = if (expandedDesc) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))

                        TextButton(onClick = { expandedDesc = !expandedDesc }) {
                            Text(if (expandedDesc) "Свернуть" else "Читать полностью")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun prev() {
    MaterialTheme {
        MemoryDetailsScreen(
            Memory(
                id = 1,
                name = "Зимняя прогулка",
                author = User(
                    1,
                    "Алексей Иванов",
                    "https://masterpiecer-images.s3.yandex.net/e6babcbf6ead11eeaab02aacdc0146ad:upscaled"
                ),
                photoUrl = "https://avatars.mds.yandex.net/i?id=6ddb0e238cb67cd072aab6de12476f66_l-7755611-images-thumbs&n=13",
                photosUrls = List(6) { "https://cs8.livemaster.ru/storage/7a/79/57b03d6ebab5e881b452a9b6a54f.jpg" },
                location = Location(55.75, 37.62),
                description = "Длинное и содержательное описание воспоминания — где вы были, что чувствовали, с кем были, и почему это важно. Здесь можно показать несколько строк, а затем раскрыть полностью по нажатию.",
                photosWithText = List(4) {
                    PhotoWithText(
                        name = "Шаг $it",
                        "Очень крутая подпись $it ".repeat(7),
                        "https://cs8.livemaster.ru/storage/7a/79/57b03d6ebab5e881b452a9b6a54f.jpg"
                    )
                },
                tags = listOf("#футджоб", "#эщкере", "#люблю Юлечку"),
                createdAt = System.currentTimeMillis() - 86_400_000L
            )
        )
    }
}