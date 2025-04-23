
✅ Tài liệu ghi chú: Cài đặt build theo môi trường, keystore và Firebase SHA

### 1. Ảnh xác build variant theo flavor + mode

| Flavor   | Build Type | Variant        | Package Name             | Keystore                 | Mục đích |
|----------|-------------|----------------|---------------------------|---------------------------|-------------------------|
| `dev`    | `debug`     | `devDebug`     | `com.kyobi.customer.dev` | `kyobi_debug_keystore.jks` | Dùng trong phát triển
| `dev`    | `release`   | `devRelease`   | `com.kyobi.customer.dev` | `kyobi_release_keystore.jks` | Dùng test chức năng release
| `prod`   | `release`   | `prodRelease`  | `com.kyobi.customer`     | `kyobi_release_keystore.jks` | Phiên bản chính thức
| `dev`    | `profile`   | `devProfile`   | `com.kyobi.customer.dev` | `kyobi_debug_keystore.jks` | Benchmark/Test perf
| `prod`   | `debug`     | `prodDebug`    | `com.kyobi.customer`     | `kyobi_debug_keystore.jks` | Test nhanh flavor prod
| `prod`   | `profile`   | `prodProfile`  | `com.kyobi.customer`     | `kyobi_debug_keystore.jks` | Benchmark/Test perf


### 2. Kết cấu `signingConfigs` trong `build.gradle.kts`

- `debug` + `profile` sử dụng: `devSigning`
- `release` sử dụng: `prodSigning`

```kotlin
signingConfigs {
    create("devSigning") {
        storeFile = file(devKeystoreProps["storeFile"] as String)
        storePassword = devKeystoreProps["storePassword"] as String
        keyAlias = devKeystoreProps["keyAlias"] as String
        keyPassword = devKeystoreProps["keyPassword"] as String
    }
    create("prodSigning") {
        storeFile = file(prodKeystoreProps["storeFile"] as String)
        storePassword = prodKeystoreProps["storePassword"] as String
        keyAlias = prodKeystoreProps["keyAlias"] as String
        keyPassword = prodKeystoreProps["keyPassword"] as String
    }
}
```

### 3. Gán signingConfig theo buildType:
```kotlin
buildTypes {
    getByName("debug") {
        signingConfig = signingConfigs.getByName("devSigning")
    }
    getByName("release") {
        signingConfig = signingConfigs.getByName("prodSigning")
    }
    maybeCreate("profile").apply {
        initWith(getByName("debug"))
        signingConfig = signingConfigs.getByName("devSigning")
    }
}
```

### 4. Tích hợp Firebase:
- Mỗi Firebase project ứng với appId:
  - `com.kyobi.customer` (prod)
  - `com.kyobi.customer.dev` (dev)

- Mỗi Firebase project phải được add đầy đủ SHA-1 & SHA-256:

| Firebase App ID              | Keystore dùng              | Cần add SHA-1/SHA-256 |
|------------------------------|------------------------------|---------------------------|
| `com.kyobi.customer.dev`     | `kyobi_debug_keystore.jks`   | ✅ SHA debug
| `com.kyobi.customer.dev`     | `kyobi_release_keystore.jks` | ✅ SHA release (vì devRelease dùng release key)
| `com.kyobi.customer`         | `kyobi_release_keystore.jks` | ✅ SHA release


### 5. Vị trí file `google-services.json`

```
app/
├── src/
│   ├── dev/
│   │   └── google-services.json   ← Firebase dev (com.kyobi.customer.dev)
│   └── prod/
│       └── google-services.json ← Firebase prod (com.kyobi.customer)
```

Gradle plugin `com.google.gms.google-services` sẽ tự động lấy file tương ứng theo build variant.

---

### 6. Cách xem SHA-1/SHA-256 nhanh:

```bash
./gradlew signingReport
```

Lọc theo variant anh muốn (vd: devRelease, prodRelease), copy SHA-1/SHA-256 để add vào Firebase.

---

### 7. Kết luận:
- `buildType` quyết định keystore dùng
- `flavor` quyết định package name + google-services.json
- Firebase cần SHA đúng với keystore đã dùng build APK

Luôn đảm bảo build APK nào, Firebase App ID nào, thì keystore và SHA của nó được add đúng trong Firebase. ✅
