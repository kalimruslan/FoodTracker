package com.ruslan.foodtracker.feature.home.presenter

import com.ruslan.foodtracker.domain.error.DomainError
import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.FoodEntry
import com.ruslan.foodtracker.domain.model.MealType
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.usecase.entry.GetRecentFoodEntriesUseCase
import com.ruslan.foodtracker.domain.usecase.entry.InsertFoodEntryUseCase
import com.ruslan.foodtracker.domain.usecase.food.GetFavoriteFoodsUseCase
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
class QuickAddViewModelTest {

    @MockK
    private lateinit var getFavoriteFoodsUseCase: GetFavoriteFoodsUseCase

    @MockK
    private lateinit var getRecentFoodEntriesUseCase: GetRecentFoodEntriesUseCase

    @MockK
    private lateinit var insertFoodEntryUseCase: InsertFoodEntryUseCase

    private lateinit var viewModel: QuickAddViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testFood = Food(
        id = 1L,
        name = "Гречка",
        calories = 343,
        protein = 13.2,
        carbs = 71.5,
        fat = 3.4,
        servingSize = 100.0,
        servingUnit = "г",
    )

    private val testDate = LocalDate.of(2026, 2, 24)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        every { getFavoriteFoodsUseCase() } returns flowOf(NetworkResult.Success(emptyList()))
        every { getRecentFoodEntriesUseCase(any()) } returns flowOf(NetworkResult.Success(emptyList()))

        viewModel = QuickAddViewModel(
            getFavoriteFoodsUseCase = getFavoriteFoodsUseCase,
            getRecentFoodEntriesUseCase = getRecentFoodEntriesUseCase,
            insertFoodEntryUseCase = insertFoodEntryUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---- Test 1: open() sets isVisible=true and correct mealType/date ----

    @Test
    fun `open sets isVisible true with correct mealType and date`() = runTest {
        val mealType = MealType.LUNCH
        every { getFavoriteFoodsUseCase() } returns flowOf(NetworkResult.Success(emptyList()))
        every { getRecentFoodEntriesUseCase(any()) } returns flowOf(NetworkResult.Success(emptyList()))

        viewModel.open(mealType, testDate)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isVisible)
        assertEquals(mealType, state.selectedMealType)
        assertEquals(testDate, state.date)
    }

    @Test
    fun `open resets to FOOD_SELECTION step`() = runTest {
        every { getFavoriteFoodsUseCase() } returns flowOf(NetworkResult.Success(emptyList()))
        every { getRecentFoodEntriesUseCase(any()) } returns flowOf(NetworkResult.Success(emptyList()))

        viewModel.open(MealType.DINNER, testDate)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(QuickAddStep.FOOD_SELECTION, state.step)
        assertNull(state.selectedFood)
    }

    // ---- Test 2: onFoodSelected() transitions to AMOUNT_INPUT step ----

    @Test
    fun `onFoodSelected transitions to AMOUNT_INPUT step with correct amountText`() = runTest {
        viewModel.open(MealType.BREAKFAST, testDate)
        advanceUntilIdle()

        viewModel.onFoodSelected(testFood)

        val state = viewModel.uiState.value
        assertEquals(QuickAddStep.AMOUNT_INPUT, state.step)
        assertEquals(testFood.servingSize.toInt().toString(), state.amountText)
        assertEquals(testFood, state.selectedFood)
    }

    // ---- Test 3: onFoodSelected() triggers recalculate - calculatedCalories is correct ----

    @Test
    fun `onFoodSelected triggers recalculate and calculatedCalories is correct`() = runTest {
        viewModel.open(MealType.BREAKFAST, testDate)
        advanceUntilIdle()

        viewModel.onFoodSelected(testFood)

        val state = viewModel.uiState.value
        val expectedServings = testFood.servingSize / testFood.servingSize.coerceAtLeast(1.0)
        val expectedCalories = (testFood.calories * expectedServings).roundToInt()
        assertEquals(expectedCalories, state.calculatedCalories)
    }

    // ---- Test 4: onAmountChanged() with valid number updates calculatedCalories ----

    @Test
    fun `onAmountChanged with valid number updates calculatedCalories`() = runTest {
        viewModel.open(MealType.LUNCH, testDate)
        advanceUntilIdle()
        viewModel.onFoodSelected(testFood)

        viewModel.onAmountChanged("200")

        val state = viewModel.uiState.value
        val expectedServings = 200.0 / testFood.servingSize.coerceAtLeast(1.0)
        val expectedCalories = (testFood.calories * expectedServings).roundToInt()
        assertEquals(expectedCalories, state.calculatedCalories)
        assertEquals("200", state.amountText)
    }

    @Test
    fun `onAmountChanged updates calculatedProtein carbs fat correctly`() = runTest {
        viewModel.open(MealType.LUNCH, testDate)
        advanceUntilIdle()
        viewModel.onFoodSelected(testFood)

        viewModel.onAmountChanged("150")

        val state = viewModel.uiState.value
        val factor = 150.0 / testFood.servingSize
        assertEquals(testFood.protein * factor, state.calculatedProtein, 0.001)
        assertEquals(testFood.carbs * factor, state.calculatedCarbs, 0.001)
        assertEquals(testFood.fat * factor, state.calculatedFat, 0.001)
    }

    // ---- Test 5: onAmountChanged() with invalid text does not crash ----

    @Test
    fun `onAmountChanged with invalid text does not crash and updates amountText`() = runTest {
        viewModel.open(MealType.SNACK, testDate)
        advanceUntilIdle()
        viewModel.onFoodSelected(testFood)

        // Previous calculatedCalories
        val prevCalories = viewModel.uiState.value.calculatedCalories

        viewModel.onAmountChanged("abc")

        val state = viewModel.uiState.value
        assertEquals("abc", state.amountText)
        // calculatedCalories should remain unchanged since text is invalid
        assertEquals(prevCalories, state.calculatedCalories)
    }

    @Test
    fun `onAmountChanged with empty string does not crash`() = runTest {
        viewModel.open(MealType.SNACK, testDate)
        advanceUntilIdle()
        viewModel.onFoodSelected(testFood)

        viewModel.onAmountChanged("")

        val state = viewModel.uiState.value
        assertEquals("", state.amountText)
    }

    // ---- Test 6: onBackToSelection() returns to FOOD_SELECTION step ----

    @Test
    fun `onBackToSelection returns to FOOD_SELECTION step and clears selectedFood`() = runTest {
        viewModel.open(MealType.BREAKFAST, testDate)
        advanceUntilIdle()
        viewModel.onFoodSelected(testFood)

        viewModel.onBackToSelection()

        val state = viewModel.uiState.value
        assertEquals(QuickAddStep.FOOD_SELECTION, state.step)
        assertNull(state.selectedFood)
        assertEquals("", state.amountText)
        assertNull(state.error)
    }

    // ---- Test 7: close() resets state to default ----

    @Test
    fun `close resets state to default`() = runTest {
        viewModel.open(MealType.DINNER, testDate)
        advanceUntilIdle()
        viewModel.onFoodSelected(testFood)

        viewModel.close()

        val state = viewModel.uiState.value
        assertFalse(state.isVisible)
        assertNull(state.selectedFood)
        assertEquals("", state.amountText)
        assertEquals(MealType.BREAKFAST, state.selectedMealType)
        assertEquals(QuickAddStep.FOOD_SELECTION, state.step)
    }

    // ---- Test 8: loadFavorites() - success case populates favoriteFoods ----

    @Test
    fun `loadFavorites success case populates favoriteFoods`() = runTest {
        val foods = listOf(testFood)
        every { getFavoriteFoodsUseCase() } returns flowOf(NetworkResult.Success(foods))
        every { getRecentFoodEntriesUseCase(any()) } returns flowOf(NetworkResult.Success(emptyList()))

        viewModel.open(MealType.BREAKFAST, testDate)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(foods, state.favoriteFoods)
    }

    @Test
    fun `loadFavorites error case does not update favoriteFoods`() = runTest {
        every { getFavoriteFoodsUseCase() } returns flowOf(
            NetworkResult.Error(DomainError.Database.FetchFailed),
        )
        every { getRecentFoodEntriesUseCase(any()) } returns flowOf(NetworkResult.Success(emptyList()))

        viewModel.open(MealType.BREAKFAST, testDate)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.favoriteFoods.isEmpty())
    }

    // ---- Test 9: loadRecent() - success case populates recentEntries ----

    @Test
    fun `loadRecent success case populates recentEntries`() = runTest {
        val entries = listOf(
            FoodEntry(
                id = 1L,
                foodId = 1L,
                foodName = "Гречка",
                servings = 1.0,
                amountGrams = 100.0,
                calories = 343,
                protein = 13.2,
                carbs = 71.5,
                fat = 3.4,
                timestamp = LocalDateTime.of(2026, 2, 24, 12, 0),
                mealType = MealType.LUNCH,
            ),
        )
        every { getFavoriteFoodsUseCase() } returns flowOf(NetworkResult.Success(emptyList()))
        every { getRecentFoodEntriesUseCase(any()) } returns flowOf(NetworkResult.Success(entries))

        viewModel.open(MealType.LUNCH, testDate)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(entries, state.recentEntries)
        assertFalse(state.isLoadingRecent)
    }

    @Test
    fun `loadRecent Loading state sets isLoadingRecent true then false on success`() = runTest {
        val entries = listOf(
            FoodEntry(
                id = 2L,
                foodId = 2L,
                foodName = "Рис",
                servings = 1.0,
                amountGrams = 150.0,
                calories = 520,
                protein = 10.0,
                carbs = 110.0,
                fat = 2.0,
                timestamp = LocalDateTime.of(2026, 2, 24, 8, 0),
                mealType = MealType.BREAKFAST,
            ),
        )
        every { getFavoriteFoodsUseCase() } returns flowOf(NetworkResult.Success(emptyList()))
        every { getRecentFoodEntriesUseCase(any()) } returns flowOf(
            NetworkResult.Loading,
            NetworkResult.Success(entries),
        )

        viewModel.open(MealType.BREAKFAST, testDate)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(entries, state.recentEntries)
        assertFalse(state.isLoadingRecent)
    }

    // ---- Test 10: saveEntry() - success case sets isSaved=true and isVisible=false ----

    @Test
    fun `saveEntry success case sets isSaved true and isVisible false`() = runTest {
        every { getFavoriteFoodsUseCase() } returns flowOf(NetworkResult.Success(emptyList()))
        every { getRecentFoodEntriesUseCase(any()) } returns flowOf(NetworkResult.Success(emptyList()))
        coEvery { insertFoodEntryUseCase(any()) } returns NetworkResult.Success(1L)

        viewModel.open(MealType.LUNCH, testDate)
        advanceUntilIdle()
        viewModel.onFoodSelected(testFood)
        viewModel.onAmountChanged("100")

        viewModel.saveEntry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSaved)
        assertFalse(state.isVisible)
    }

    // ---- Test 11: saveEntry() - error case sets error message ----

    @Test
    fun `saveEntry error case sets error message`() = runTest {
        every { getFavoriteFoodsUseCase() } returns flowOf(NetworkResult.Success(emptyList()))
        every { getRecentFoodEntriesUseCase(any()) } returns flowOf(NetworkResult.Success(emptyList()))
        coEvery { insertFoodEntryUseCase(any()) } returns NetworkResult.Error(
            DomainError.Database.InsertFailed,
        )

        viewModel.open(MealType.DINNER, testDate)
        advanceUntilIdle()
        viewModel.onFoodSelected(testFood)
        viewModel.onAmountChanged("150")

        viewModel.saveEntry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Ошибка сохранения", state.error)
        assertFalse(state.isLoading)
        assertFalse(state.isSaved)
    }

    // ---- Test 12: saveEntry() - invalid amount (0) sets error without calling insertUseCase ----

    @Test
    fun `saveEntry with zero amount sets error without calling insertUseCase`() = runTest {
        every { getFavoriteFoodsUseCase() } returns flowOf(NetworkResult.Success(emptyList()))
        every { getRecentFoodEntriesUseCase(any()) } returns flowOf(NetworkResult.Success(emptyList()))

        viewModel.open(MealType.SNACK, testDate)
        advanceUntilIdle()
        viewModel.onFoodSelected(testFood)
        viewModel.onAmountChanged("0")

        viewModel.saveEntry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Введите корректное количество", state.error)
        coVerify(exactly = 0) { insertFoodEntryUseCase(any()) }
    }

    @Test
    fun `saveEntry without selected food does nothing`() = runTest {
        every { getFavoriteFoodsUseCase() } returns flowOf(NetworkResult.Success(emptyList()))
        every { getRecentFoodEntriesUseCase(any()) } returns flowOf(NetworkResult.Success(emptyList()))

        viewModel.open(MealType.BREAKFAST, testDate)
        advanceUntilIdle()
        // Do not call onFoodSelected

        viewModel.saveEntry()
        advanceUntilIdle()

        coVerify(exactly = 0) { insertFoodEntryUseCase(any()) }
    }

    // ---- Test 13: onRecentEntrySelected() normalizes nutrients and transitions to AMOUNT_INPUT ----

    @Test
    fun `onRecentEntrySelected normalizes nutrients correctly and transitions to AMOUNT_INPUT`() = runTest {
        viewModel.open(MealType.LUNCH, testDate)
        advanceUntilIdle()

        val entry = FoodEntry(
            id = 10L,
            foodId = 5L,
            foodName = "Курица",
            servings = 1.5,
            amountGrams = 150.0,
            calories = 240,
            protein = 33.0,
            carbs = 0.0,
            fat = 12.0,
            timestamp = LocalDateTime.of(2026, 2, 24, 13, 0),
            mealType = MealType.LUNCH,
        )

        viewModel.onRecentEntrySelected(entry)

        val state = viewModel.uiState.value
        assertEquals(QuickAddStep.AMOUNT_INPUT, state.step)
        assertEquals(entry.amountGrams.toInt().toString(), state.amountText)

        val food = state.selectedFood
        assertTrue(food != null)
        assertEquals(entry.foodId, food!!.id)
        assertEquals(entry.foodName, food.name)

        // Verify normalization: nutrients per 100g
        val factor = 100.0 / entry.amountGrams
        val expectedCalories100g = (entry.calories * factor).roundToInt()
        assertEquals(expectedCalories100g, food.calories)
        assertEquals(entry.protein * factor, food.protein, 0.001)
        assertEquals(entry.carbs * factor, food.carbs, 0.001)
        assertEquals(entry.fat * factor, food.fat, 0.001)
        assertEquals(100.0, food.servingSize, 0.001)
    }

    @Test
    fun `onRecentEntrySelected with zero amountGrams uses factor of 1`() = runTest {
        viewModel.open(MealType.BREAKFAST, testDate)
        advanceUntilIdle()

        val entry = FoodEntry(
            id = 20L,
            foodId = 7L,
            foodName = "Яблоко",
            servings = 0.0,
            amountGrams = 0.0,
            calories = 52,
            protein = 0.3,
            carbs = 14.0,
            fat = 0.2,
            timestamp = LocalDateTime.of(2026, 2, 24, 9, 0),
            mealType = MealType.BREAKFAST,
        )

        viewModel.onRecentEntrySelected(entry)

        val state = viewModel.uiState.value
        val food = state.selectedFood
        assertTrue(food != null)
        // factor = 1.0 when amountGrams == 0
        assertEquals(entry.calories * 1, food!!.calories)
    }

    @Test
    fun `onRecentEntrySelected recalculates with entry amountGrams`() = runTest {
        viewModel.open(MealType.DINNER, testDate)
        advanceUntilIdle()

        val amountGrams = 200.0
        val entry = FoodEntry(
            id = 30L,
            foodId = 8L,
            foodName = "Рыба",
            servings = 2.0,
            amountGrams = amountGrams,
            calories = 400,
            protein = 50.0,
            carbs = 0.0,
            fat = 20.0,
            timestamp = LocalDateTime.of(2026, 2, 24, 19, 0),
            mealType = MealType.DINNER,
        )

        viewModel.onRecentEntrySelected(entry)

        val state = viewModel.uiState.value
        // The servingSize of the synthetic food is 100g
        // recalculate called with entry.amountGrams = 200
        // servings = 200 / 100 = 2
        val factor = 100.0 / amountGrams // normalization factor
        val caloriesPer100 = (entry.calories * factor).roundToInt()
        val expectedCalories = (caloriesPer100 * (amountGrams / 100.0)).roundToInt()
        assertEquals(expectedCalories, state.calculatedCalories)
    }

    // ---- Additional: onSavedHandled resets isSaved ----

    @Test
    fun `onSavedHandled resets isSaved to false`() = runTest {
        coEvery { insertFoodEntryUseCase(any()) } returns NetworkResult.Success(1L)

        viewModel.open(MealType.LUNCH, testDate)
        advanceUntilIdle()
        viewModel.onFoodSelected(testFood)
        viewModel.onAmountChanged("100")
        viewModel.saveEntry()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)

        viewModel.onSavedHandled()

        assertFalse(viewModel.uiState.value.isSaved)
    }

    // ---- Additional: isAmountValid computed property ----

    @Test
    fun `isAmountValid is true for positive number`() = runTest {
        viewModel.open(MealType.BREAKFAST, testDate)
        advanceUntilIdle()
        viewModel.onFoodSelected(testFood)
        viewModel.onAmountChanged("100")

        assertTrue(viewModel.uiState.value.isAmountValid)
    }

    @Test
    fun `isAmountValid is false for zero`() = runTest {
        viewModel.open(MealType.BREAKFAST, testDate)
        advanceUntilIdle()
        viewModel.onFoodSelected(testFood)
        viewModel.onAmountChanged("0")

        assertFalse(viewModel.uiState.value.isAmountValid)
    }

    @Test
    fun `isAmountValid is false for invalid text`() = runTest {
        viewModel.open(MealType.BREAKFAST, testDate)
        advanceUntilIdle()
        viewModel.onFoodSelected(testFood)
        viewModel.onAmountChanged("xyz")

        assertFalse(viewModel.uiState.value.isAmountValid)
    }
}
