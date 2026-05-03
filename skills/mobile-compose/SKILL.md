---
name: mobile-compose
description: >
  Mobile-first architecture patterns for Kotlin/Android with Jetpack Compose.
  Trigger: When developing mobile apps, implementing MVI, adaptive layouts, or Clean Architecture in Android.
scope: composeApp
license: MIT
metadata:
  author: gentleman-programming
  version: "1.0"
---

## Purpose

You are an expert in modern Android mobile development. When this skill is loaded, you MUST follow the mobile-first architecture patterns below for ALL code written in Kotlin/Android projects.

## Core Architecture Principles (Non-Negotiable)

### 1. MVI (Model-View-Intent) - The ONLY acceptable pattern

**MVI is mandatory** for all state management in Jetpack Compose. Do NOT use other patterns.

```
┌─────────────┐     Intent      ┌─────────────┐
│    View     │ ──────────────► │  ViewModel  │
│ (Compose)   │ ◄────────────── │  (Intent)   │
└─────────────┘    State        └─────────────┘
```

**Implementation must follow:**

```kotlin
// 1. UI State: Immutable data class representing ALL screen state
data class ScreenState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: String? = null,
    val selectedItem: Item? = null
)

// 2. Intent: Sealed class representing ALL possible user actions
sealed class ScreenIntent {
    data object LoadItems : ScreenIntent()
    data class SelectItem(val item: Item) : ScreenIntent()
    data class DeleteItem(val id: String) : ScreenIntent()
}

// 3. ViewModel: Processes intents and emits new state via StateFlow
@HiltViewModel
class ScreenViewModel @Inject constructor(
    private val repository: ItemRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ScreenState())
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    fun processIntent(intent: ScreenIntent) {
        when (intent) {
            is ScreenIntent.LoadItems -> loadItems()
            is ScreenIntent.SelectItem -> selectItem(intent.item)
            is ScreenIntent.DeleteItem -> deleteItem(intent.id)
        }
    }

    private fun loadItems() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val items = repository.getItems()
                _state.update { it.copy(items = items, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
```

**RULES:**
- NEVER expose MutableStateFlow outside ViewModel
- ALWAYS use `stateIn()` with `SharingStarted.WhileSubscribed(5000)` for proper lifecycle
- NEVER use `var` for state - only immutable data classes
- ALWAYS handle loading, error, and success states explicitly

### 2. Unidirectional Data Flow (UDF) - MANDATORY

State flows DOWN, events flow UP:

```kotlin
@Composable
fun Screen(
    viewModel: ScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // State flows DOWN - UI simply displays it
    if (state.isLoading) {
        CircularProgressIndicator()
    } else {
        // Events flow UP - pass lambdas, not state
        ItemList(
            items = state.items,
            onItemClick = { viewModel.processIntent(ScreenIntent.SelectItem(it)) },
            onDeleteClick = { viewModel.processIntent(ScreenIntent.DeleteItem(it.id)) }
        )
    }
}
```

**RULES:**
- Composables NEVER modify state directly
- Composables ONLY display state and emit intents
- Use `collectAsStateWithLifecycle()` from `androidx.lifecycle.compose`

### 3. Clean Architecture - Three Layers

```
┌─────────────────────────────────────────────────────┐
│                  UI LAYER                            │
│  Composables + ViewModels (MVI) + UI State          │
│  Depends on: Domain                                 │
└─────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────┐
│                DOMAIN LAYER                         │
│  Use Cases + Repository Interfaces + Entities      │
│  Depends on: Nothing (pure Kotlin)                 │
└─────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────┐
│                 DATA LAYER                          │
│  Repository Implementations + Data Sources         │
│  (Room, Retrofit, DataStore)                       │
│  Depends on: Domain + External APIs                │
└─────────────────────────────────────────────────────┘
```

**Structure:**

```
com.example.app/
├── ui/
│   ├── screen/
│   │   ├── composable/
│   │   ├── viewmodel/
│   │   └── state/
│   ├── components/
│   └── theme/
├── domain/
│   ├── model/
│   ├── repository/
│   │   └── Repository.kt (interface)
│   └── usecase/
│       └── GetItemsUseCase.kt
└── data/
    ├── repository/
    │   └── RepositoryImpl.kt
    ├── local/
    │   └── LocalDataSource.kt
    └── remote/
        └── RemoteDataSource.kt
```

**RULES:**
- Domain layer has ZERO Android dependencies
- Use Cases contain ONLY business logic, no data access
- Repository interfaces live in Domain, implementations in Data

### 4. Hilt Dependency Injection - MANDATORY

**Module definition:**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRepository(
        localDataSource: LocalDataSource,
        remoteDataSource: RemoteDataSource
    ): ItemRepository {
        return ItemRepositoryImpl(localDataSource, remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }
}
```

**ViewModel injection:**

```kotlin
@HiltViewModel
class ScreenViewModel @Inject constructor(
    private val getItemsUseCase: GetItemsUseCase,
    private val saveItemUseCase: SaveItemUseCase
) : ViewModel() {
    // ...
}
```

**RULES:**
- Use `@HiltViewModel` for ALL ViewModels
- Inject Use Cases, NOT raw repositories (cleaner API)
- Use `@androidx.hilt.navigation.compose.hiltViewModel()` in Composables
- NEVER instantiate ViewModels manually

### 5. StateFlow + Coroutines - The ONLY way to handle async

**Correct pattern:**

```kotlin
class ScreenViewModel @Inject constructor(
    private val repository: ItemRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ScreenState())
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    fun loadItems() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getItems()
                .onSuccess { items ->
                    _state.update { it.copy(items = items, isLoading = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }
}
```

**Use Result<T> for explicit error handling:**

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}
```

**RULES:**
- Use `viewModelScope.launch` for ALL coroutine launches
- ALWAYS update state before and after async operations
- Use `.onSuccess` and `.onFailure` extensions for Result
- NEVER use `lifecycleScope` in ViewModels - only `viewModelScope`

### 6. Adaptive Layouts - WindowSizeClass (MANDATORY for mobile-first)

**Implementation:**

```kotlin
@Composable
fun AppContent() {
    val windowSizeClass = calculateWindowSizeClass(context)

    val navigationType = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> NavigationType.BOTTOM_NAVIGATION
        WindowWidthSizeClass.Medium -> NavigationType.NAVIGATION_RAIL
        WindowWidthSizeClass.Expanded -> NavigationType.PERMANENT_NAVIGATION_DRAWER
        else -> NavigationType.BOTTOM_NAVIGATION
    }

    when (navigationType) {
        NavigationType.BOTTOM_NAVIGATION -> BottomNavigationLayout()
        NavigationType.NAVIGATION_RAIL -> NavigationRailLayout()
        NavigationType.PERMANENT_NAVIGATION_DRAWER -> PermanentDrawerLayout()
    }
}

enum class NavigationType {
    BOTTOM_NAVIGATION,
    NAVIGATION_RAIL,
    PERMANENT_NAVIGATION_DRAWER
}
```

**Use for list-detail layouts:**

```kotlin
@Composable
fun MasterDetailScreen() {
    val windowSizeClass = calculateWindowSizeClass(context)
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    if (isExpanded) {
        TwoPaneLayout() // Tablet/desktop
    } else {
        SinglePaneWithBackStack() // Phone
    }
}
```

**RULES:**
- ALWAYS use `calculateWindowSizeClass()` from WindowManager
- Use `windowSizeClass.isWidthAtLeastBreakpoint()` for specific breakpoints
- Implement BOTH phone AND tablet layouts from day one
- Test on multiple screen sizes during development

### 7. Navigation - Type-Safe Routing

**Setup:**

```kotlin
@Serializable
sealed class Screen(val route: String) {
    @Serializable
    data object Home : Screen("home")

    @Serializable
    data class Detail(val id: String) : Screen("detail/{id}") {
        companion object { fun create(id: String) = "detail/$id" }
    }

    @Serializable
    data class Settings(val tab: String? = null) : Screen("settings?tab={tab}") {
        companion object { fun create(tab: String? = null) =
            if (tab != null) "settings?tab=$tab" else "settings"
        }
    }
}
```

**Navigation graph:**

```kotlin
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable<Screen.Home> {
            HomeScreen(
                onNavigateToDetail = { id ->
                    navController.navigate(Screen.Detail.create(id))
                }
            )
        }

        composable<Screen.Detail> { backStackEntry ->
            val detailScreen = backStackEntry.toRoute<Screen.Detail>()
            DetailScreen(itemId = detailScreen.id)
        }

        composable<Screen.Settings> { backStackEntry ->
            val settingsScreen = backStackEntry.toRoute<Screen.Settings>()
            SettingsScreen(initialTab = settingsScreen.tab)
        }
    }
}
```

**RULES:**
- ALWAYS use `@Serializable` sealed classes for routes
- Use type-safe navigation with `toRoute<T>()` - NEVER parse strings manually
- Pass complex objects via `navController.navigate()` with proper serialization

### 8. Material Design 3 - Theming

**Theme definition:**

```kotlin
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Material You on Android 12+
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

**Use Material 3 components:**

```kotlin
// Instead of Button, use FilledTonalButton or FilledButton
FilledTonalButton(
    onClick = { /* action */ },
    enabled = !isLoading
) {
    Text("Save")
}

// For lists, use LazyColumn with proper Material spacing
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(items) { item ->
        ListItem(
            headlineContent = { Text(item.title) },
            supportingContent = { Text(item.description) },
            leadingContent = { Icon(item.icon) }
        )
    }
}
```

**RULES:**
- Use Material 3 components from `androidx.compose.material3`
- Enable dynamic colors on Android 12+ for Material You support
- Use proper elevation and tonal colors, NOT raw shadows

## Anti-Patterns (NEVER do these)

1. **NEVER use `var` for state** - Only immutable data classes
2. **NEVER expose MutableStateFlow** - Always expose `StateFlow`
3. **NEVER use `lifecycleScope` in ViewModels** - Use `viewModelScope`
4. **NEVER put Android dependencies in Domain layer** - Keep it pure Kotlin
5. **NEVER directly instantiate dependencies** - Always use Hilt injection
6. **NEVER use `remember` for business state** - Only for UI state
7. **NEVER do heavy computation in Composables** - Move to ViewModel or UseCase
8. **NEVER ignore WindowSizeClass** - Mobile-first means responsive from day one

## Project Structure Template

For a new feature, create:

```
feature-name/
├── domain/
│   ├── model/
│   │   └── FeatureNameModel.kt
│   ├── repository/
│   │   └── FeatureNameRepository.kt (interface)
│   └── usecase/
│       ├── GetFeatureNameUseCase.kt
│       └── SaveFeatureNameUseCase.kt
├── data/
│   ├── repository/
│   │   └── FeatureNameRepositoryImpl.kt
│   ├── local/
│   │   └── FeatureNameLocalDataSource.kt
│   └── mapper/
│       └── FeatureNameMapper.kt
└── ui/
    ├── screen/
    │   ├── FeatureNameContract.kt (State + Intent)
    │   ├── FeatureNameViewModel.kt
    │   └── FeatureNameScreen.kt
    └── components/
        └── FeatureNameComponent.kt
```

## Testing Requirements

For every feature:
1. Write unit tests for ViewModels (test state transitions)
2. Write unit tests for Use Cases (test business logic)
3. Use `createAndroidIntentForNavigation()` for navigation testing
4. Use `TestDispatcher` for coroutine testing

```kotlin
class FeatureNameViewModelTest {

    @Test
    fun `processIntent LoadItems emits loading then success`() = runTest {
        // Given
        val viewModel = FeatureNameViewModel(repository)

        // When
        viewModel.processIntent(FeatureNameIntent.LoadItems)

        // Then
        assertTrue(viewModel.state.value.isLoading)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(expectedItems, viewModel.state.value.items)
    }
}
```

## Important Gotchas

- Use `collectAsStateWithLifecycle()` instead of `collectAsState()` for lifecycle-aware collection
- When using Room with Flow, use `asDomain()` mapper at the repository level
- For navigation arguments, always use safe-args or type-safe routing
- When testing ViewModels with Hilt, use `hiltViewModel()` in test
- WindowSizeClass requires `@androidx.window:window:1.0.0` dependency
- Dynamic color requires `androidx.compose.material3:material3:1.1.0+` on Android 12+