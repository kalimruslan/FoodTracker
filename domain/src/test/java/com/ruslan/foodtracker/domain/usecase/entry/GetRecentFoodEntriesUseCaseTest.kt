package com.ruslan.foodtracker.domain.usecase.entry

import com.ruslan.foodtracker.domain.model.FoodEntry
import com.ruslan.foodtracker.domain.model.MealType
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.repository.FoodEntryRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class GetRecentFoodEntriesUseCaseTest {
    @MockK
    private lateinit var repository: FoodEntryRepository

    private lateinit var useCase: GetRecentFoodEntriesUseCase

    private val sampleEntry = FoodEntry(
        id = 1L,
        foodId = 10L,
        foodName = "Гречка",
        servings = 1.0,
        amountGrams = 100.0,
        calories = 343,
        protein = 13.2,
        carbs = 71.5,
        fat = 3.4,
        timestamp = LocalDateTime.of(2026, 2, 24, 12, 0),
        mealType = MealType.LUNCH,
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetRecentFoodEntriesUseCase(repository)
    }

    // ---- Test: invoke() delegates to repository.getRecentEntries with correct default limit ----

    @Test
    fun `invoke delegates to repository getRecentEntries with default limit of 20`() =
        runTest {
            val entries = listOf(sampleEntry)
            every { repository.getRecentEntries(20) } returns flowOf(NetworkResult.Success(entries))

            val results = useCase().toList()

            verify(exactly = 1) { repository.getRecentEntries(20) }
            assertEquals(1, results.size)
            val result = results.first()
            assertEquals(NetworkResult.Success(entries), result)
        }

    // ---- Test: invoke() with custom limit passes it through ----

    @Test
    fun `invoke with custom limit passes it through to repository`() =
        runTest {
            val customLimit = 5
            val entries = listOf(sampleEntry)
            every { repository.getRecentEntries(customLimit) } returns flowOf(NetworkResult.Success(entries))

            val results = useCase(limit = customLimit).toList()

            verify(exactly = 1) { repository.getRecentEntries(customLimit) }
            assertEquals(1, results.size)
            assertEquals(NetworkResult.Success(entries), results.first())
        }

    @Test
    fun `invoke with limit 1 returns only one entry from repository`() =
        runTest {
            val entries = listOf(sampleEntry)
            every { repository.getRecentEntries(1) } returns flowOf(NetworkResult.Success(entries))

            val results = useCase(limit = 1).toList()

            verify(exactly = 1) { repository.getRecentEntries(1) }
            assertEquals(NetworkResult.Success(entries), results.first())
        }

    @Test
    fun `invoke propagates loading state from repository`() =
        runTest {
            every { repository.getRecentEntries(20) } returns flowOf(
                NetworkResult.Loading,
                NetworkResult.Success(emptyList()),
            )

            val results = useCase().toList()

            assertEquals(2, results.size)
            assertEquals(NetworkResult.Loading, results[0])
            assertEquals(NetworkResult.Success(emptyList<FoodEntry>()), results[1])
        }

    @Test
    fun `invoke propagates error state from repository`() =
        runTest {
            val errorResult = NetworkResult.Error(
                com.ruslan.foodtracker.domain.error.DomainError.Database.FetchFailed,
            )
            every { repository.getRecentEntries(20) } returns flowOf(errorResult)

            val results = useCase().toList()

            assertEquals(1, results.size)
            assertEquals(errorResult, results.first())
        }

    @Test
    fun `invoke with large limit delegates correctly`() =
        runTest {
            val largeLimit = 100
            every { repository.getRecentEntries(largeLimit) } returns flowOf(NetworkResult.Success(emptyList()))

            useCase(limit = largeLimit).toList()

            verify(exactly = 1) { repository.getRecentEntries(largeLimit) }
        }
}
