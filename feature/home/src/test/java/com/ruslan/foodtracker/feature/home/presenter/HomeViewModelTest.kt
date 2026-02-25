package com.ruslan.foodtracker.feature.home.presenter

import com.ruslan.foodtracker.core.common.util.DateTimeUtils
import com.ruslan.foodtracker.domain.error.DomainError
import com.ruslan.foodtracker.domain.model.FoodEntry
import com.ruslan.foodtracker.domain.model.MealType
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.usecase.entry.DeleteFoodEntryUseCase
import com.ruslan.foodtracker.domain.usecase.entry.GetEntriesByDateUseCase
import com.ruslan.foodtracker.domain.usecase.entry.InsertFoodEntryUseCase
import com.ruslan.foodtracker.domain.usecase.entry.UpdateFoodEntryUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToInt

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @MockK
    private lateinit var getEntriesByDateUseCase: GetEntriesByDateUseCase

    @MockK
    private lateinit var deleteFoodEntryUseCase: DeleteFoodEntryUseCase

    @MockK
    private lateinit var insertFoodEntryUseCase: InsertFoodEntryUseCase

    @MockK
    private lateinit var updateFoodEntryUseCase: UpdateFoodEntryUseCase

    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testEntry = FoodEntry(
        id = 1L,
        foodId = 10L,
        foodName = "Куриная грудка",
        servings = 1.5,
        amountGrams = 150.0,
        calories = 165,
        protein = 34.5,
        carbs = 0.0,
        fat = 2.7,
        timestamp = LocalDateTime.of(2026, 2, 24, 12, 0),
        mealType = MealType.LUNCH,
    )

    private val testEntry2 = FoodEntry(
        id = 2L,
        foodId = 20L,
        foodName = "Овсяная каша",
        servings = 1.0,
        amountGrams = 200.0,
        calories = 176,
        protein = 8.0,
        carbs = 30.0,
        fat = 4.0,
        timestamp = LocalDateTime.of(2026, 2, 24, 8, 0),
        mealType = MealType.BREAKFAST,
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        // Default: empty entries list
        every { getEntriesByDateUseCase(any()) } returns flowOf(NetworkResult.Success(emptyList()))

        viewModel = HomeViewModel(
            getEntriesByDateUseCase = getEntriesByDateUseCase,
            deleteFoodEntryUseCase = deleteFoodEntryUseCase,
            insertFoodEntryUseCase = insertFoodEntryUseCase,
            updateFoodEntryUseCase = updateFoodEntryUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── Вспомогательная функция: загрузить VM с тестовыми записями ──────────

    private fun initWithEntries(entries: List<FoodEntry>) {
        every { getEntriesByDateUseCase(any()) } returns flowOf(NetworkResult.Success(entries))
        viewModel = HomeViewModel(
            getEntriesByDateUseCase = getEntriesByDateUseCase,
            deleteFoodEntryUseCase = deleteFoodEntryUseCase,
            insertFoodEntryUseCase = insertFoodEntryUseCase,
            updateFoodEntryUseCase = updateFoodEntryUseCase,
        )
    }

    // ─── onDeleteEntry ────────────────────────────────────────────────────────

    @Test
    fun `onDeleteEntry calls DeleteFoodEntryUseCase with correct entry`() =
        runTest {
            initWithEntries(listOf(testEntry))
            advanceUntilIdle()

            coEvery { deleteFoodEntryUseCase(testEntry) } returns NetworkResult.Success(Unit)

            viewModel.onDeleteEntry(testEntry.id)
            advanceUntilIdle()

            coVerify(exactly = 1) { deleteFoodEntryUseCase(testEntry) }
        }

    @Test
    fun `onDeleteEntry after success sets showDeleteSnackbar true`() =
        runTest {
            initWithEntries(listOf(testEntry))
            advanceUntilIdle()

            coEvery { deleteFoodEntryUseCase(testEntry) } returns NetworkResult.Success(Unit)

            viewModel.onDeleteEntry(testEntry.id)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.showDeleteSnackbar)
        }

    @Test
    fun `onDeleteEntry after success saves pendingDeleteEntry`() =
        runTest {
            initWithEntries(listOf(testEntry))
            advanceUntilIdle()

            coEvery { deleteFoodEntryUseCase(testEntry) } returns NetworkResult.Success(Unit)

            viewModel.onDeleteEntry(testEntry.id)
            advanceUntilIdle()

            assertEquals(testEntry, viewModel.uiState.value.pendingDeleteEntry)
        }

    @Test
    fun `onDeleteEntry with unknown entryId does nothing`() =
        runTest {
            initWithEntries(listOf(testEntry))
            advanceUntilIdle()

            viewModel.onDeleteEntry(999L)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.showDeleteSnackbar)
            coVerify(exactly = 0) { deleteFoodEntryUseCase(any()) }
        }

    @Test
    fun `onDeleteEntry on error does not set showDeleteSnackbar`() =
        runTest {
            initWithEntries(listOf(testEntry))
            advanceUntilIdle()

            coEvery { deleteFoodEntryUseCase(testEntry) } returns NetworkResult.Error(
                DomainError.Database.FetchFailed,
            )

            viewModel.onDeleteEntry(testEntry.id)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.showDeleteSnackbar)
        }

    // ─── onUndoDelete ─────────────────────────────────────────────────────────

    @Test
    fun `onUndoDelete calls InsertFoodEntryUseCase with id zero`() =
        runTest {
            initWithEntries(listOf(testEntry))
            advanceUntilIdle()

            coEvery { deleteFoodEntryUseCase(testEntry) } returns NetworkResult.Success(Unit)
            coEvery { insertFoodEntryUseCase(any()) } returns NetworkResult.Success(5L)

            viewModel.onDeleteEntry(testEntry.id)
            advanceUntilIdle()

            viewModel.onUndoDelete()
            advanceUntilIdle()

            coVerify(exactly = 1) { insertFoodEntryUseCase(testEntry.copy(id = 0L)) }
        }

    @Test
    fun `onUndoDelete after success clears pendingDeleteEntry and snackbar`() =
        runTest {
            initWithEntries(listOf(testEntry))
            advanceUntilIdle()

            coEvery { deleteFoodEntryUseCase(testEntry) } returns NetworkResult.Success(Unit)
            coEvery { insertFoodEntryUseCase(any()) } returns NetworkResult.Success(5L)

            viewModel.onDeleteEntry(testEntry.id)
            advanceUntilIdle()

            viewModel.onUndoDelete()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.pendingDeleteEntry)
            assertFalse(viewModel.uiState.value.showDeleteSnackbar)
        }

    @Test
    fun `onUndoDelete without pendingDeleteEntry does nothing`() =
        runTest {
            advanceUntilIdle()

            viewModel.onUndoDelete()
            advanceUntilIdle()

            coVerify(exactly = 0) { insertFoodEntryUseCase(any()) }
        }

    // ─── onDeleteSnackbarDismissed ────────────────────────────────────────────

    @Test
    fun `onDeleteSnackbarDismissed clears snackbar state`() =
        runTest {
            initWithEntries(listOf(testEntry))
            advanceUntilIdle()

            coEvery { deleteFoodEntryUseCase(testEntry) } returns NetworkResult.Success(Unit)

            viewModel.onDeleteEntry(testEntry.id)
            advanceUntilIdle()

            viewModel.onDeleteSnackbarDismissed()

            assertFalse(viewModel.uiState.value.showDeleteSnackbar)
            assertNull(viewModel.uiState.value.pendingDeleteEntry)
        }

    // ─── onEditEntry / onEditDismiss ──────────────────────────────────────────

    @Test
    fun `onEditEntry sets editingEntry in state`() =
        runTest {
            initWithEntries(listOf(testEntry))
            advanceUntilIdle()

            viewModel.onEditEntry(testEntry.id)

            assertEquals(testEntry, viewModel.uiState.value.editingEntry)
        }

    @Test
    fun `onEditEntry with unknown entryId does not set editingEntry`() =
        runTest {
            initWithEntries(listOf(testEntry))
            advanceUntilIdle()

            viewModel.onEditEntry(999L)

            assertNull(viewModel.uiState.value.editingEntry)
        }

    @Test
    fun `onEditDismiss clears editingEntry`() =
        runTest {
            initWithEntries(listOf(testEntry))
            advanceUntilIdle()

            viewModel.onEditEntry(testEntry.id)
            viewModel.onEditDismiss()

            assertNull(viewModel.uiState.value.editingEntry)
        }

    // ─── onUpdateEntryAmount ──────────────────────────────────────────────────

    @Test
    fun `onUpdateEntryAmount calls UpdateFoodEntryUseCase with recalculated values`() =
        runTest {
            coEvery { updateFoodEntryUseCase(any()) } returns NetworkResult.Success(Unit)

            val newGrams = 300.0
            val ratio = newGrams / testEntry.amountGrams
            val expectedUpdated = testEntry.copy(
                amountGrams = newGrams,
                calories = (testEntry.calories * ratio).roundToInt(),
                protein = testEntry.protein * ratio,
                carbs = testEntry.carbs * ratio,
                fat = testEntry.fat * ratio,
            )

            viewModel.onUpdateEntryAmount(testEntry, newGrams)
            advanceUntilIdle()

            coVerify(exactly = 1) { updateFoodEntryUseCase(expectedUpdated) }
        }

    @Test
    fun `onUpdateEntryAmount correctly recalculates calories via ratio`() =
        runTest {
            coEvery { updateFoodEntryUseCase(any()) } returns NetworkResult.Success(Unit)

            val newGrams = 200.0
            val expectedCalories = (testEntry.calories * (newGrams / testEntry.amountGrams)).roundToInt()

            viewModel.onUpdateEntryAmount(testEntry, newGrams)
            advanceUntilIdle()

            coVerify { updateFoodEntryUseCase(match { it.calories == expectedCalories }) }
        }

    @Test
    fun `onUpdateEntryAmount with zero newAmountGrams does nothing`() =
        runTest {
            viewModel.onUpdateEntryAmount(testEntry, 0.0)
            advanceUntilIdle()

            coVerify(exactly = 0) { updateFoodEntryUseCase(any()) }
        }

    @Test
    fun `onUpdateEntryAmount with negative newAmountGrams does nothing`() =
        runTest {
            viewModel.onUpdateEntryAmount(testEntry, -50.0)
            advanceUntilIdle()

            coVerify(exactly = 0) { updateFoodEntryUseCase(any()) }
        }

    @Test
    fun `onUpdateEntryAmount with zero original amountGrams does nothing`() =
        runTest {
            val entryWithZeroGrams = testEntry.copy(amountGrams = 0.0)

            viewModel.onUpdateEntryAmount(entryWithZeroGrams, 100.0)
            advanceUntilIdle()

            coVerify(exactly = 0) { updateFoodEntryUseCase(any()) }
        }

    @Test
    fun `onUpdateEntryAmount after success clears editingEntry`() =
        runTest {
            initWithEntries(listOf(testEntry))
            advanceUntilIdle()

            coEvery { updateFoodEntryUseCase(any()) } returns NetworkResult.Success(Unit)

            viewModel.onEditEntry(testEntry.id)
            assertEquals(testEntry, viewModel.uiState.value.editingEntry)

            viewModel.onUpdateEntryAmount(testEntry, 200.0)
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.editingEntry)
        }

    // ─── allEntries в состоянии ───────────────────────────────────────────────

    @Test
    fun `loadEntriesForSelectedDate stores entries in allEntries state`() =
        runTest {
            val entries = listOf(testEntry, testEntry2)
            every { getEntriesByDateUseCase(any()) } returns flowOf(NetworkResult.Success(entries))

            viewModel = HomeViewModel(
                getEntriesByDateUseCase = getEntriesByDateUseCase,
                deleteFoodEntryUseCase = deleteFoodEntryUseCase,
                insertFoodEntryUseCase = insertFoodEntryUseCase,
                updateFoodEntryUseCase = updateFoodEntryUseCase,
            )
            advanceUntilIdle()

            assertEquals(entries, viewModel.uiState.value.allEntries)
        }

    @Test
    fun `loadEntriesForSelectedDate creates FoodItemData with correct entryId`() =
        runTest {
            initWithEntries(listOf(testEntry))
            advanceUntilIdle()

            val lunchItems = viewModel.uiState.value.meals
                .find { it.mealType == MealType.LUNCH }
                ?.foodItems ?: emptyList()

            assertEquals(1, lunchItems.size)
            assertEquals(testEntry.id, lunchItems.first().entryId)
        }

    // ─── TC-01..TC-03: onDaySelected ─────────────────────────────────────────

    @Test
    fun `TC-01 onDaySelected updates selectedDate to date from current week`() =
        runTest {
            // Arrange: set currentWeekStart to 2026-02-23 (Monday)
            val weekStart = LocalDate.of(2026, 2, 23)
            every { getEntriesByDateUseCase(any()) } returns flowOf(NetworkResult.Success(emptyList()))
            viewModel = HomeViewModel(
                getEntriesByDateUseCase = getEntriesByDateUseCase,
                deleteFoodEntryUseCase = deleteFoodEntryUseCase,
                insertFoodEntryUseCase = insertFoodEntryUseCase,
                updateFoodEntryUseCase = updateFoodEntryUseCase,
            )
            // Manually set currentWeekStart
            viewModel.onPreviousWeek() // go back to previous week (if today >= 2026-02-23)
            advanceUntilIdle()

            // Force state with a known weekStart from the past
            val testWeekStart = DateTimeUtils.weekStart(LocalDate.of(2026, 2, 16))
            // Use onPreviousWeek from that context; instead let's set up a known past week
            every { getEntriesByDateUseCase(any()) } returns flowOf(NetworkResult.Success(emptyList()))
            viewModel = HomeViewModel(
                getEntriesByDateUseCase = getEntriesByDateUseCase,
                deleteFoodEntryUseCase = deleteFoodEntryUseCase,
                insertFoodEntryUseCase = insertFoodEntryUseCase,
                updateFoodEntryUseCase = updateFoodEntryUseCase,
            )
            advanceUntilIdle()

            // Move to a past week so that all 7 days are in the past
            repeat(2) {
                viewModel.onPreviousWeek()
                advanceUntilIdle()
            }

            // Act: select Wednesday (index 2) in that past week
            viewModel.onDaySelected(2)
            advanceUntilIdle()

            // Assert: selectedDate matches Wednesday of that week
            val expectedDate = viewModel.uiState.value.currentWeekStart.plusDays(2)
            assertEquals(expectedDate, viewModel.uiState.value.selectedDate)
        }

    @Test
    fun `TC-02 onDaySelected calls getEntriesByDateUseCase with new date`() =
        runTest {
            // Move to a past week so index 1 is in the past
            repeat(2) {
                viewModel.onPreviousWeek()
                advanceUntilIdle()
            }
            val expectedDate = viewModel.uiState.value.currentWeekStart.plusDays(1)

            viewModel.onDaySelected(1)
            advanceUntilIdle()

            verify { getEntriesByDateUseCase(expectedDate) }
        }

    @Test
    fun `TC-03 onDaySelected ignores future date and does not change selectedDate`() =
        runTest {
            advanceUntilIdle()
            val today = LocalDate.now()
            val originalDate = viewModel.uiState.value.selectedDate

            // Try to select a day index that is in the future
            // currentWeekStart is this week's Monday; find a future index
            val currentWeekStart = viewModel.uiState.value.currentWeekStart
            val futureDayIndex = (0..6).firstOrNull { index ->
                currentWeekStart.plusDays(index.toLong()).isAfter(today)
            }

            if (futureDayIndex != null) {
                viewModel.onDaySelected(futureDayIndex)
                advanceUntilIdle()
                assertEquals(originalDate, viewModel.uiState.value.selectedDate)
            }
            // If today is Sunday (no future days this week), test passes trivially
        }

    // ─── TC-04..TC-08: onPreviousWeek ─────────────────────────────────────────

    @Test
    fun `TC-04 onPreviousWeek decreases currentWeekStart by 7 days`() =
        runTest {
            advanceUntilIdle()
            val oldWeekStart = viewModel.uiState.value.currentWeekStart

            viewModel.onPreviousWeek()
            advanceUntilIdle()

            assertEquals(oldWeekStart.minusWeeks(1), viewModel.uiState.value.currentWeekStart)
        }

    @Test
    fun `TC-05 onPreviousWeek sets showTodayButton to true`() =
        runTest {
            advanceUntilIdle()

            viewModel.onPreviousWeek()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.showTodayButton)
        }

    @Test
    fun `TC-06 onPreviousWeek preserves dayIndex when date is not in the future`() =
        runTest {
            advanceUntilIdle()
            // Move to a week 3 weeks ago so that all days are in the past
            repeat(3) {
                viewModel.onPreviousWeek()
                advanceUntilIdle()
            }
            // Select Tuesday (index 1) — it's in the past since we're 3 weeks back
            viewModel.onDaySelected(1)
            advanceUntilIdle()
            assertEquals(1, viewModel.uiState.value.selectedDayIndex)

            // Go one more week back — Tuesday is still in the past
            viewModel.onPreviousWeek()
            advanceUntilIdle()

            assertEquals(1, viewModel.uiState.value.selectedDayIndex)
        }

    @Test
    fun `TC-07 onPreviousWeek clamps date to today if selected dayIndex is in the future`() =
        runTest {
            advanceUntilIdle()

            viewModel.onPreviousWeek()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.selectedDate <= LocalDate.now())
        }

    @Test
    fun `TC-08 onPreviousWeek triggers data loading`() =
        runTest {
            advanceUntilIdle()

            viewModel.onPreviousWeek()
            advanceUntilIdle()

            verify(atLeast = 1) { getEntriesByDateUseCase(any()) }
        }

    // ─── TC-09..TC-11: onNextWeek ─────────────────────────────────────────────

    @Test
    fun `TC-09 onNextWeek increases currentWeekStart by 7 days`() =
        runTest {
            advanceUntilIdle()
            viewModel.onPreviousWeek()
            advanceUntilIdle()
            val oldWeekStart = viewModel.uiState.value.currentWeekStart

            viewModel.onNextWeek()
            advanceUntilIdle()

            assertEquals(oldWeekStart.plusWeeks(1), viewModel.uiState.value.currentWeekStart)
        }

    @Test
    fun `TC-10 onNextWeek sets showTodayButton to false when returning to current week`() =
        runTest {
            advanceUntilIdle()
            viewModel.onPreviousWeek()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.showTodayButton)

            viewModel.onNextWeek()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.showTodayButton)
        }

    @Test
    fun `TC-11 onNextWeek ignores call when current week is today's week`() =
        runTest {
            advanceUntilIdle()
            val originalWeekStart = viewModel.uiState.value.currentWeekStart

            viewModel.onNextWeek()
            advanceUntilIdle()

            assertEquals(originalWeekStart, viewModel.uiState.value.currentWeekStart)
        }

    // ─── TC-12..TC-15: onTodayClicked ────────────────────────────────────────

    @Test
    fun `TC-12 onTodayClicked sets selectedDate to today`() =
        runTest {
            advanceUntilIdle()
            viewModel.onPreviousWeek()
            advanceUntilIdle()

            viewModel.onTodayClicked()
            advanceUntilIdle()

            assertEquals(LocalDate.now(), viewModel.uiState.value.selectedDate)
        }

    @Test
    fun `TC-13 onTodayClicked sets showTodayButton to false`() =
        runTest {
            advanceUntilIdle()
            viewModel.onPreviousWeek()
            advanceUntilIdle()

            viewModel.onTodayClicked()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.showTodayButton)
        }

    @Test
    fun `TC-14 onTodayClicked sets selectedDayIndex to today dayOfWeek minus 1`() =
        runTest {
            advanceUntilIdle()
            viewModel.onPreviousWeek()
            advanceUntilIdle()

            viewModel.onTodayClicked()
            advanceUntilIdle()

            assertEquals(LocalDate.now().dayOfWeek.value - 1, viewModel.uiState.value.selectedDayIndex)
        }

    @Test
    fun `TC-15 onTodayClicked sets currentWeekStart to start of current week`() =
        runTest {
            advanceUntilIdle()
            viewModel.onPreviousWeek()
            advanceUntilIdle()

            viewModel.onTodayClicked()
            advanceUntilIdle()

            assertEquals(DateTimeUtils.weekStart(LocalDate.now()), viewModel.uiState.value.currentWeekStart)
        }

    // ─── TC-16..TC-19: Cache ──────────────────────────────────────────────────

    @Test
    fun `TC-16 repeated selection of same date does not call usecase again`() =
        runTest {
            // Go 3 weeks back so indices 1 and 2 are both in the past
            repeat(3) {
                viewModel.onPreviousWeek()
                advanceUntilIdle()
            }

            viewModel.onDaySelected(2) // first call — loads from DB
            advanceUntilIdle()

            viewModel.onDaySelected(1) // move to different day
            advanceUntilIdle()

            val dateWednesday = viewModel.uiState.value.currentWeekStart.plusDays(2)
            // Clear invocations count before the repeated call
            every { getEntriesByDateUseCase(any()) } returns flowOf(NetworkResult.Success(emptyList()))

            viewModel.onDaySelected(2) // return to cached Wednesday
            advanceUntilIdle()

            // The cache should serve the data; usecase should not be called for wednesday again
            verify(exactly = 0) { getEntriesByDateUseCase(dateWednesday) }
        }

    @Test
    fun `TC-17 after onDeleteEntry cache for selectedDate is invalidated`() =
        runTest {
            initWithEntries(listOf(testEntry))
            advanceUntilIdle()

            coEvery { deleteFoodEntryUseCase(testEntry) } returns NetworkResult.Success(Unit)

            viewModel.onDeleteEntry(testEntry.id)
            advanceUntilIdle()

            // After delete, cache for selected date should be cleared, triggering reload
            val selectedDate = viewModel.uiState.value.selectedDate
            assertFalse(viewModel.uiState.value.entriesCache.containsKey(selectedDate))
        }

    @Test
    fun `TC-18 after onUndoDelete cache for selectedDate is invalidated`() =
        runTest {
            initWithEntries(listOf(testEntry))
            advanceUntilIdle()

            coEvery { deleteFoodEntryUseCase(testEntry) } returns NetworkResult.Success(Unit)
            coEvery { insertFoodEntryUseCase(any()) } returns NetworkResult.Success(5L)

            viewModel.onDeleteEntry(testEntry.id)
            advanceUntilIdle()

            viewModel.onUndoDelete()
            advanceUntilIdle()

            // After undo, cache for selected date should be cleared, triggering reload
            val selectedDate = viewModel.uiState.value.selectedDate
            assertFalse(viewModel.uiState.value.entriesCache.containsKey(selectedDate))
        }

    @Test
    fun `TC-19 after onUpdateEntryAmount cache for selectedDate is invalidated`() =
        runTest {
            initWithEntries(listOf(testEntry))
            advanceUntilIdle()

            coEvery { updateFoodEntryUseCase(any()) } returns NetworkResult.Success(Unit)

            viewModel.onUpdateEntryAmount(testEntry, 200.0)
            advanceUntilIdle()

            val selectedDate = viewModel.uiState.value.selectedDate
            assertFalse(viewModel.uiState.value.entriesCache.containsKey(selectedDate))
        }
}
