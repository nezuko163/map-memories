package com.nezuko.memorydetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nezuko.domain.model.Location
import com.nezuko.domain.model.Memory
import com.nezuko.domain.model.PhotoWithText
import com.nezuko.domain.model.User
import com.nezuko.domain.repository.MemoriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryDetailsViewModel @Inject constructor(
    private val memoriesRepository: MemoriesRepository
) : ViewModel() {
    private val _memory = MutableStateFlow<Memory?>(null)
    val memory = _memory.asStateFlow()

    fun load(id: Int) {
        viewModelScope.launch {
            val memory1 = memoriesRepository.getMemoryById(id)
            val memory = Memory(
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
                photosWithText = List(4) { PhotoWithText(name = "Шаг $it","Очень крутая подпись $it ".repeat(7), "https://cs8.livemaster.ru/storage/7a/79/57b03d6ebab5e881b452a9b6a54f.jpg") },
                tags = listOf("#футджоб", "#эщкере", "#люблю Юлечку"),
                createdAt = System.currentTimeMillis() - 86_400_000L
            )
            _memory.value = memory

        }
    }

    fun clear() {
        _memory.value = null
    }
}