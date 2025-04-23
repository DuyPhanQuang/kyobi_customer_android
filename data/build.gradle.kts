plugins {
    id("plugin.android-common")
    id("com.apollographql.apollo3") version "4.0.0-beta.7"
}

apollo {
    service("shopify") {
        packageName.set("com.kyobi.data.graphql")
        generateKotlinModels.set(true)
        srcDir("src/main/graphql/com/kyobi/data/graphql")
        schemaFiles.from(file("src/main/graphql/com/kyobi/data/graphql/schema.json"))
    }
}

dependencies {
    CORE
    DOMAIN
}

android {
    namespace = "com.kyobi.data"
}
