package com.ruslan.foodtracker.feature.home.presenter

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

    private val testDate = LocalDate.of(2026, 2, 24)

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
    fun `onDeleteEntry calls DeleteFoodEntryUseCase with correct entry`() = runTest {
        initWithEntries(listOf(testEntry))
        advanceUntilIdle()

        coEvery { deleteFoodEntryUseCase(testEntry) } returns NetworkResult.Success(Unit)

        viewModel.onDeleteEntry(testEntry.id)
        advanceUntilIdle()

        coVerify(exactly = 1) { deleteFoodEntryUseCase(testEntry) }
    }

    @Test
    fun `onDeleteEntry after success sets showDeleteSnackbar true`() = runTest {
        initWithEntries(listOf(testEntry))
        advanceUntilIdle()

        coEvery { deleteFoodEntryUseCase(testEntry) } returns NetworkResult.Success(Unit)

        viewModel.onDeleteEntry(testEntry.id)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showDeleteSnackbar)
    }

    @Test
    fun `onDeleteEntry after success saves pendingDeleteEntry`() = runTest {
        initWithEntries(listOf(testEntry))
        advanceUntilIdle()

        coEvery { deleteFoodEntryUseCase(testEntry) } returns NetworkResult.Success(Unit)

        viewModel.onDeleteEntry(testEntry.id)
        advanceUntilIdle()

        assertEquals(testEntry, viewModel.uiState.value.pendingDeleteEntry)
    }

    @Test
    fun `onDeleteEntry with unknown entryId does nothing`() = runTest {
        initWithEntries(listOf(testEntry))
        advanceUntilIdle()

        viewModel.onDeleteEntry(999L)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showDeleteSnackbar)
        coVerify(exactly = 0) { deleteFoodEntryUseCase(any()) }
    }

    @Test
    fun `onDeleteEntry on error does not set showDeleteSnackbar`() = runTest {
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
    fun `onUndoDelete calls InsertFoodEntryUseCase with id zero`() = runTest {
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
    fun `onUndoDelete after success clears pendingDeleteEntry and snackbar`() = runTest {
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
    fun `onUndoDelete without pendingDeleteEntry does nothing`() = runTest {
        advanceUntilIdle()

        viewModel.onUndoDelete()
        advanceUntilIdle()

        coVerify(exactly = 0) { insertFoodEntryUseCase(any()) }
    }

    // ─── onDeleteSnackbarDismissed ────────────────────────────────────────────

    @Test
    fun `onDeleteSnackbarDismissed clears snackbar state`() = runTest {
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
    fun `onEditEntry sets editingEntry in state`() = runTest {
        initWithEntries(listOf(testEntry))
        advanceUntilIdle()

        viewModel.onEditEntry(testEntry.id)

        assertEquals(testEntry, viewModel.uiState.value.editingEntry)
    }

    @Test
    fun `onEditEntry with unknown entryId does not set editingEntry`() = runTest {
        initWithEntries(listOf(testEntry))
        advanceUntilIdle()

        viewModel.onEditEntry(999L)

        assertNull(viewModel.uiState.value.editingEntry)
    }

    @Test
    fun `onEditDismiss clears editingEntry`() = runTest {
        initWithEntries(listOf(testEntry))
        advanceUntilIdle()

        viewModel.onEditEntry(testEntry.id)
        viewModel.onEditDismiss()

        assertNull(viewModel.uiState.value.editingEntry)
    }

    // ─── onUpdateEntryAmount ──────────────────────────────────────────────────

    @Test
    fun `onUpdateEntryAmount calls UpdateFoodEntryUseCase with recalculated values`() = runTest {
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
    fun `onUpdateEntryAmount correctly recalculates calories via ratio`() = runTest {
        coEvery { updateFoodEntryUseCase(any()) } returns NetworkResult.Success(Unit)

        val newGrams = 200.0
        val expectedCalories = (testEntry.calories * (newGrams / testEntry.amountGrams)).roundToInt()

        viewModel.onUpdateEntryAmount(testEntry, newGrams)
        advanceUntilIdle()

        coVerify { updateFoodEntryUseCase(match { it.calories == expectedCalories }) }
    }

    @Test
    fun `onUpdateEntryAmount with zero newAmountGrams does nothing`() = runTest {
        viewModel.onUpdateEntryAmount(testEntry, 0.0)
        advanceUntilIdle()

        coVerify(exactly = 0) { updateFoodEntryUseCase(any()) }
    }

    @Test
    fun `onUpdateEntryAmount with negative newAmountGrams does nothing`() = runTest {
        viewModel.onUpdateEntryAmount(testEntry, -50.0)
        advanceUntilIdle()

        coVerify(exactly = 0) { updateFoodEntryUseCase(any()) }
    }

    @Test
    fun `onUpdateEntryAmount with zero original amountGrams does nothing`() = runTest {
        val entryWithZeroGrams = testEntry.copy(amountGrams = 0.0)

        viewModel.onUpdateEntryAmount(entryWithZeroGrams, 100.0)
        advanceUntilIdle()

        coVerify(exactly = 0) { updateFoodEntryUseCase(any()) }
    }

    @Test
    fun `onUpdateEntryAmount after success clears editingEntry`() = runTest {
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
    fun `loadEntriesForSelectedDate stores entries in allEntries state`() = runTest {
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
    fun `loadEntriesForSelectedDate creates FoodItemData with correct entryId`() = runTest {
        initWithEntries(listOf(testEntry))
        advanceUntilIdle()

        val lunchItems = viewModel.uiState.value.meals
            .find { it.mealType == MealType.LUNCH }
            ?.foodItems ?: emptyList()

        assertEquals(1, lunchItems.size)
        assertEquals(testEntry.id, lunchItems.first().entryId)
    }
}