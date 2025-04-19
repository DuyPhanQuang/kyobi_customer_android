# Kyobi Customer Android - Technical Documentation

## 1. Giới thiệu chung về dự án

**Kyobi Customer Android** là ứng dụng Android dành cho khách hàng của Kyobi, được xây dựng theo kiến trúc **Clean Architecture** và **MVVM**. Ứng dụng sử dụng Jetpack Compose cho giao diện người dùng, Hilt cho dependency injection, Room cho local database, Retrofit cho REST API, và Apollo GraphQL cho Shopify API.

**Các công nghệ chính:**
- **Ngôn ngữ:** Kotlin
- **UI:** Jetpack Compose
- **Dependency Injection:** Hilt
- **Local Database:** Room
- **Network:** Retrofit (REST), Apollo GraphQL
- **Coroutines & Flow** cho bất đồng bộ
- **Timber** cho logging
- **Coil** cho tải ảnh

---

## 2. Cấu trúc Module và Dependency Graph

Dự án được chia thành các module sau:
- **buildSrc**: Quản lý dependencies và plugins chung.
- **:core**: Chứa các thành phần chung như coroutines extensions, network clients, token storage.
- **:domain**: Định nghĩa các use case, repository interfaces, domain models.
- **:data**: Implement repository, xử lý dữ liệu từ API và local database.
- **:feature:authentication**: Chứa logic và UI cho xác thực (đăng nhập, đăng ký, đăng xuất).
- **:feature:home**: Chứa logic và UI cho trang chủ (hiển thị sản phẩm).
- **:feature:profile**: Chứa logic và UI cho trang hồ sơ người dùng.

### Sơ đồ phụ thuộc giữa các module (UML Dependency Graph)

```plaintext
+----------------+     +----------------+     +----------------+
| :feature:home  |     | :feature:profile |     | :feature:authentication |
+----------------+     +----------------+     +----------------+
          |                      |                      |
          |                      |                      |
          v                      v                      v
+----------------+     +----------------+     +----------------+
|    :domain     |<----|    :data       |<----|    :core       |
+----------------+     +----------------+     +----------------+
          ^                      ^                      ^
          |                      |                      |
          |                      |                      |
          +----------------------+                      |
          |                                             |
          v                                             |
+----------------+                                      |
|   buildSrc     |--------------------------------------+
+----------------+
```

**Giải thích:**
- **buildSrc** cung cấp plugins và dependencies cho tất cả các module.
- **:core** cung cấp các thành phần chung như network clients, coroutines extensions, được **:data**, **:domain**, và các feature module phụ thuộc.
- **:domain** phụ thuộc vào **:core**, định nghĩa use cases, repository interfaces, và domain models.
- **:data** phụ thuộc vào **:core** và **:domain**, implement các repository và xử lý dữ liệu.
- Các feature module (**:home**, **:profile**, **:authentication**) phụ thuộc vào **:domain**, **:data**, và **:core**, nhưng **không phụ thuộc lẫn nhau**. Chúng tương tác thông qua `AuthStateProvider` (định nghĩa trong **:domain**, implement trong **:feature:authentication**) để chia sẻ trạng thái xác thực.

---

## 3. Cấu trúc Folder và File của từng Module

### 3.1. Module: buildSrc

**Cấu trúc folder:**
```
buildSrc/
├── build.gradle.kts
└── src/
    └── main/
        └── java/
            └── plugin/
                ├── android-common.gradle.kts
                ├── Libraries.kt
                └── DependencyHandler.kt
```

- **build.gradle.kts**: Cấu hình plugins và dependencies cho buildSrc.
- **android-common.gradle.kts**: Plugin chung cho các module Android (cấu hình Compose, Kotlin, Hilt, v.v.).
- **Libraries.kt**: Định nghĩa versions và dependencies (AndroidX, Compose, Room, Retrofit, v.v.).
- **DependencyHandler.kt**: Extension functions để thêm dependencies.

**Ví dụ từ `Libraries.kt`:**
```kotlin
object Versions {
    const val kotlin = "2.1.10"
    const val compose = "1.7.0"
}

object Libs {
    const val androidxCore = "androidx.core:core-ktx:1.13.1"
    const val composeUi = "androidx.compose.ui:ui:${Versions.compose}"
}
```

### 3.2. Module: core

**Cấu trúc folder:**
```
:core/
├── build.gradle.kts
└── src/
    └── main/
        └── java/
            └── com/kyobi/core/
                ├── coroutines/
                │   └── CoroutinesExtensions.kt
                ├── di/
                │   └── NetworkModule.kt
                ├── model/
                │   ├── NetworkResult.kt
                │   └── RestNetworkResult.kt
                ├── network/
                │   ├── ApolloClient.kt
                │   ├── RestClient.kt
                │   └── WithAuthHeaders.kt
                └── storage/
                    └── TokenStorage.kt
```

- **build.gradle.kts**: Áp dụng plugin `android-common`, namespace `com.kyobi.core`.
- **CoroutinesExtensions.kt**: Extensions cho Flow và CoroutineScope để xử lý loading và lỗi.
- **NetworkModule.kt**: Cung cấp OkHttpClient cho Shopify (GraphQL) và Kyobi (REST) API.
- **NetworkResult.kt**: Sealed class cho trạng thái mạng (Success, Error, Loading).

**Ví dụ từ `CoroutinesExtensions.kt`:**
```kotlin
fun <T> Flow<T>.withLoading(action: () -> Unit): Flow<T> {
    return onStart { action() }
}

fun <T> Flow<T>.handleErrors(action: (Throwable) -> Unit): Flow<T> {
    return catch { throwable -> action(throwable) }
}
```

### 3.3. Module: domain

**Cấu trúc folder:**
```
:domain/
├── build.gradle.kts
└── src/
    └── main/
        └── java/
            └── com/kyobi/domain/
                ├── di/
                │   └── UsecaseModule.kt
                ├── model/
                │   ├── LoggedInUser.kt
                │   ├── NetworkResult.kt
                │   ├── Product.kt
                │   └── request/
                │       ├── LoginRequest.kt
                │       └── SignUpRequest.kt
                ├── provider/
                │   └── auth/
                │       ├── AuthState.kt
                │       └── AuthStateProvider.kt
                ├── repository/
                │   ├── AuthRepository.kt
                │   └── ProductRepository.kt
                └── usecase/
                    ├── GetProductsUseCase.kt
                    ├── LoginUseCase.kt
                    ├── LogoutUseCase.kt
                    ├── SignupUseCase.kt
                    └── impl/
                        ├── GetProductsUseCaseImpl.kt
                        ├── LoginUseCaseImpl.kt
                        ├── LogoutUseCaseImpl.kt
                        └── SignupUseCaseImpl.kt
```

- **build.gradle.kts**: Phụ thuộc `:core`, namespace `com.kyobi.domain`.
- **UsecaseModule.kt**: Bind các implementation của use cases thông qua Hilt.
- **AuthStateProvider.kt**: Interface cung cấp `StateFlow<AuthUiState>` để chia sẻ trạng thái xác thực.

**Ví dụ từ `GetProductsUseCaseImpl.kt`:**
```kotlin
class GetProductsUseCaseImpl @Inject constructor(
    private val productRepository: ProductRepository
): GetProductsUseCase {
    override suspend operator fun invoke(): Flow<DomainNetworkResult<List<Product>>> {
        return flow {
            emit(DomainNetworkResult.Loading)
            when (val result = productRepository.getProductsFromShopify()) {
                is NetworkResult.Success -> emit(DomainNetworkResult.Success(result.data))
                is NetworkResult.Error -> emit(DomainNetworkResult.Error(result.exception))
                is NetworkResult.Loading -> emit(DomainNetworkResult.Loading)
            }
        }
    }
}
```

### 3.4. Module: data

**Cấu trúc folder:**
```
:data/
├── build.gradle.kts
└── src/
    └── main/
        ├── graphql/
        │   └── com/kyobi/data/graphql/
        │       ├── GetProductsQuery.graphql
        │       └── schema.json
        └── java/
            └── com/kyobi/data/
                ├── database/
                │   ├── dao/
                │   │   └── TokenDao.kt
                │   ├── entity/
                │   │   └── TokenEntity.kt
                │   └── AppDatabase.kt
                ├── di/
                │   ├── DatabaseModule.kt
                │   ├── DataModule.kt
                │   ├── RepositoryModule.kt
                │   └── StorageModule.kt
                ├── model/
                │   ├── AnonymousUserResponse.kt
                │   ├── LoginResponse.kt
                │   ├── AuthUserResponse.kt
                │   └── ProductResponse.kt
                ├── network/
                │   ├── impl/
                │   │   ├── KyobiApiServiceImpl.kt
                │   │   └── ShopifyApiServiceImpl.kt
                │   ├── KyobiApiService.kt
                │   └── ShopifyApiService.kt
                ├── repository/
                │   ├── AuthRepositoryImpl.kt
                │   └── ProductRepositoryImpl.kt
                └── storage/
                    └── TokenStorageImpl.kt
```

- **build.gradle.kts**: Phụ thuộc `:core` và `:domain`, sử dụng plugin Apollo GraphQL, namespace `com.kyobi.data`.
- **TokenStorageImpl.kt**: Lưu token vào `EncryptedSharedPreferences` và Room.

**Ví dụ từ `TokenStorageImpl.kt`:**
```kotlin
@Singleton
class TokenStorageImpl @Inject constructor(
    private val tokenDao: TokenDao,
    @ApplicationContext context: Context
) : TokenStorage {
    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(ACCESS_TOKEN_KEY, accessToken)
            .putString(REFRESH_TOKEN_KEY, refreshToken)
            .apply()
        tokenDao.insert(TokenEntity(accessToken = accessToken, refreshToken = refreshToken))
    }
}
```

### 3.5. Module: feature/authentication

**Cấu trúc folder:**
```
:feature:authentication/
├── build.gradle.kts
└── src/
    └── main/
        └── java/
            └── com/kyobi/feature/authentication/
                ├── di/
                │   └── AuthenticationModule.kt
                ├── login/
                │   ├── LoginScreen.kt
                │   ├── LoginState.kt
                │   └── LoginViewModel.kt
                ├── signup/
                │   ├── SignupScreen.kt
                │   ├── SignupState.kt
                │   └── SignupViewModel.kt
                └── AuthViewModel.kt
```

- **build.gradle.kts**: Phụ thuộc `:core`, `:data`, `:domain`, `COMMON_THEME`, `COMMON_COMPOSABLE`, namespace `com.kyobi.feature.authentication`.
- **AuthViewModel.kt**: Implement `AuthStateProvider`, quản lý trạng thái xác thực toàn cục.

**Ví dụ từ `AuthViewModel.kt`:**
```kotlin
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase
): ViewModel(), AuthStateProvider {
    private val _authUiState = MutableStateFlow(AuthUiState())
    override val authUiState = _authUiState.asStateFlow()

    init {
        initializeSession() // Khởi tạo session ẩn danh
    }
}
```

### 3.6. Module: feature/home

**Cấu trúc folder:**
```
:feature:home/
├── build.gradle.kts
└── src/
    └── main/
        └── java/
            └── com/kyobi/feature/home/
                ├── HomeTab.kt
                ├── HomeTabState.kt
                └── HomeTabViewModel.kt
```

- **build.gradle.kts**: Phụ thuộc `:core`, `:data`, `:domain`, `COMMON_THEME`, `COMMON_COMPOSABLE`, namespace `com.kyobi.feature.home`.
- **HomeTabViewModel.kt**: Quản lý logic lấy danh sách sản phẩm.

**Ví dụ từ `HomeTabViewModel.kt`:**
```kotlin
@HiltViewModel
class HomeTabViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCaseImpl
): ViewModel() {
    private val _uiState = MutableStateFlow(HomeTabUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            getProductsUseCase().collect { result ->
                _uiState.value = _uiState.value.copy(productsResult = result)
            }
        }
    }
}
```

### 3.7. Module: feature/profile

**Cấu trúc folder:**
```
:feature:profile/
├── build.gradle.kts
└── src/
    └── main/
        └── java/
            └── com/kyobi/feature/profile/
                ├── ProfileTab.kt
                ├── ProfileTabState.kt
                └── ProfileTabViewModel.kt
```

- **build.gradle.kts**: Phụ thuộc `:core`, `:data`, `:domain`, `COMMON_THEME`, `COMMON_COMPOSABLE`, namespace `com.kyobi.feature.profile`.
- **ProfileTab.kt**: Hiển thị thông tin người dùng hoặc nút đăng nhập/đăng ký.

**Ví dụ từ `ProfileTab.kt`:**
```kotlin
@Composable
fun ProfileTab(
    navController: NavController,
    viewModel: ProfileTabViewModel = hiltViewModel()
) {
    val authUiState by viewModel.authUiState.collectAsStateWithLifecycle()
    if (authUiState.isLoggedIn) {
        authUiState.currentUser?.let { user ->
            Text(text = "ID: ${user.id}")
            Button(onClick = { viewModel.submitLogout }) {
                Text("Đăng xuất")
            }
        }
    } else {
        Button(onClick = { navController.navigate("login") }) {
            Text("Đăng nhập")
        }
    }
}
```

---

## 4. Áp dụng Clean Architecture và MVVM

### 4.1. Clean Architecture

Dự án tuân theo **Clean Architecture** với 3 layer chính:
- **Presentation Layer**: Các feature module (:home, :profile, :authentication), chứa UI (Jetpack Compose) và ViewModel.
- **Domain Layer**: Module :domain, chứa use cases, repository interfaces, domain models.
- **Data Layer**: Module :data, implement repository, xử lý dữ liệu từ API và local database.

**Luồng dữ liệu:**
- UI (Presentation) -> ViewModel -> UseCase (Domain) -> Repository (Data) -> API/Database.

**Ví dụ từ `:feature:home`:**
- `HomeTabViewModel` gọi `GetProductsUseCase` (domain):
```kotlin
private fun fetchProducts() {
    viewModelScope.launch {
        getProductsUseCase().collect { result ->
            _uiState.value = _uiState.value.copy(productsResult = result)
        }
    }
}
```
- `GetProductsUseCaseImpl` gọi `ProductRepository` (data):
```kotlin
override suspend operator fun invoke(): Flow<DomainNetworkResult<List<Product>>> {
    return flow {
        emit(DomainNetworkResult.Loading)
        when (val result = productRepository.getProductsFromShopify()) {
            is NetworkResult.Success -> emit(DomainNetworkResult.Success(result.data))
            is NetworkResult.Error -> emit(DomainNetworkResult.Error(result.exception))
        }
    }
}
```

**Tương tác giữa các Feature Module:**
- Các feature module không phụ thuộc lẫn nhau mà sử dụng `AuthStateProvider` từ **:domain** để chia sẻ trạng thái xác thực.
- Ví dụ: `ProfileTabViewModel` và `HomeTabViewModel` inject `AuthStateProvider` để truy cập `authUiState`:
```kotlin
@HiltViewModel
class ProfileTabViewModel @Inject constructor(
    private val authStateProvider: AuthStateProvider
) : ViewModel() {
    val authUiState = authStateProvider.authUiState
}
```

### 4.2. MVVM

Mỗi feature module sử dụng **MVVM**:
- **View**: Composable functions (ví dụ: `HomeTab`, `LoginScreen`).
- **ViewModel**: Quản lý UI state và logic (ví dụ: `HomeTabViewModel`, `LoginViewModel`).
- **Model**: Domain models (ví dụ: `Product`, `LoggedInUser`).

**Ví dụ từ `:feature:authentication`:**
- `LoginScreen` tương tác với `LoginViewModel`:
```kotlin
@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {
    Button(onClick = { viewModel.submitLogin() }) {
        Text("Đăng nhập")
    }
}
```
- `LoginViewModel` gọi `LoginUseCase`:
```kotlin
fun submitLogin() {
    viewModelScope.launchOnIO {
        loginUseCase.login(loginUiState.email, loginUiState.password)
            .collect { result ->
                when (result) {
                    is DomainNetworkResult.Success -> {
                        authStateProvider.updateAuthState(result.data, isAnonymous = false)
                    }
                }
            }
    }
}
```

---

## 5. Sử dụng buildSrc và chia sẻ Libraries

**buildSrc** giúp quản lý dependencies và plugins chung:
- **Libraries.kt**: Định nghĩa versions và dependencies tập trung.
- **DependencyHandler.kt**: Cung cấp extension functions để thêm dependencies.

**Ví dụ từ `DependencyHandler.kt`:**
```kotlin
fun DependencyHandler.CORE() {
    implementation(project(":core"))
}

fun DependencyHandler.DATA() {
    implementation(project(":data"))
}
```

**Sử dụng trong module:**
- Trong `build.gradle.kts` của `:feature:home`:
```kotlin
dependencies {
    CORE
    DATA
    DOMAIN
    COMMON_THEME
    COMMON_COMPOSABLE
}
```

Điều này giúp:
- Quản lý phiên bản dependencies tập trung, dễ dàng cập nhật.
- Đảm bảo tính nhất quán giữa các module.

---

## 6. Hướng dẫn cài đặt và chạy dự án

1. **Clone repository**:
   ```bash
   git clone https://github.com/DuyPhanQuang/kyobi_customer_android.git
   ```
2. **Mở với Android Studio** (phiên bản Arctic Fox trở lên).
3. **Build dự án**:
   - Chọn `Build > Make Project` hoặc nhấn `Ctrl + F9`.
4. **Chạy ứng dụng**:
   - Chọn thiết bị hoặc emulator và nhấn `Run` hoặc `Shift + F10`.

**Yêu cầu:**
- Android Studio Arctic Fox hoặc mới hơn.
- Kotlin 2.1.10.
- Gradle 8.3.2.

---

## 7. Tài liệu tham khảo

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Hilt](https://dagger.dev/hilt/)
- [Room](https://developer.android.com/topic/libraries/architecture/room)
- [Retrofit](https://square.github.io/retrofit/)
- [Apollo GraphQL](https://www.apollographql.com/docs/kotlin/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

---

Tài liệu này cung cấp cái nhìn tổng quan và chi tiết về dự án **Kyobi Customer Android**. Nếu cần bổ sung hoặc điều chỉnh, hãy cho mình biết nhé!