plugins {
    id("plugin.android-common")
    id("com.apollographql.apollo3") version "3.8.4"
}

apollo {
    service("shopify") {
        packageName.set("com.kyobi.data.graphql")
        generateKotlinModels.set(true)
        srcDir("src/main/graphql/com/kyobi/data/graphql")
        schemaFile.set(file("src/main/graphql/com/kyobi/data/graphql/schema.json"))
    }
}

dependencies {
    CORE
    DOMAIN
}

android {
    namespace = "com.kyobi.data"
}
