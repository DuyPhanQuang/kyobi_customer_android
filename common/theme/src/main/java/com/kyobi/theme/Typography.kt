package com.kyobi.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography = Typography(
    // Dùng các kiểu mặc định của Material 3 làm nền tảng
    displayMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 36.sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 24.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 24.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp,
    ),
)

// Extension properties để ánh xạ theo Tailwind/Figma style
// Heading
val Typography.heading5Xl: TextStyle
    get() = displayMedium.copy(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 48.sp,
        lineHeight = 48.sp,
    )

val Typography.heading4Xl: TextStyle
    get() = displayMedium.copy(
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 40.sp,
    )

val Typography.heading3Xl: TextStyle
    get() = displayMedium.copy(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
    )

val Typography.heading2Xl: TextStyle
    get() = headlineMedium.copy(
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 32.sp,
    )

val Typography.headingXl: TextStyle
    get() = headlineMedium.copy(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    )

val Typography.headingLg: TextStyle
    get() = headlineMedium.copy(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        lineHeight = 28.sp,
    )

// Label large
val Typography.label2Xl: TextStyle
    get() = labelLarge.copy(
        fontWeight = FontWeight.Bold,
        lineHeight = 32.sp,
    )

val Typography.labelXl: TextStyle
    get() = labelLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    )

val Typography.labelLg: TextStyle
    get() = labelLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 28.sp,
    )

val Typography.labelMd: TextStyle
    get() = labelLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )

val Typography.labelSm: TextStyle
    get() = labelLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )

val Typography.labelXs: TextStyle
    get() = labelLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )

// Label small
val Typography.labelSmall2Xl: TextStyle
    get() = label2Xl.copy(
        fontWeight = FontWeight.SemiBold,
    )

val Typography.labelSmallXl: TextStyle
    get() = labelXl.copy(
        fontWeight = FontWeight.SemiBold,
    )

val Typography.labelSmallLg: TextStyle
    get() = labelLg.copy(
        fontWeight = FontWeight.SemiBold,
    )

val Typography.labelSmallMd: TextStyle
    get() = labelMd.copy(
        fontWeight = FontWeight.SemiBold,
    )

val Typography.labelSmallSm: TextStyle
    get() = labelSm.copy(
        fontWeight = FontWeight.SemiBold,
    )

val Typography.labelSmallXs: TextStyle
    get() = labelXs.copy(
        fontWeight = FontWeight.SemiBold,
    )

// paragraph medium
val Typography.paragraph2Xl: TextStyle
    get() = bodyMedium.copy(
        fontWeight = FontWeight.Medium,
        lineHeight = 32.sp,
    )

val Typography.paragraphXl: TextStyle
    get() = bodyMedium.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    )

val Typography.paragraphLg: TextStyle
    get() = bodyMedium.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 28.sp,
    )

val Typography.paragraphMd: TextStyle
    get() = bodyMedium.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )

val Typography.paragraphSm: TextStyle
    get() = bodyMedium.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )

val Typography.paragraphXs: TextStyle
    get() = bodyMedium.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )

// paragraph regular
val Typography.paragraphRegular2Xl: TextStyle
    get() = paragraph2Xl.copy(
        fontWeight = FontWeight.Normal,
    )

val Typography.paragraphRegularXl: TextStyle
    get() = paragraphXl.copy(
        fontWeight = FontWeight.Normal,
    )

val Typography.paragraphRegularLg: TextStyle
    get() = paragraphLg.copy(
        fontWeight = FontWeight.Normal,
    )

val Typography.paragraphRegularMd: TextStyle
    get() = paragraphMd.copy(
        fontWeight = FontWeight.Normal,
    )

val Typography.paragraphRegularSm: TextStyle
    get() = paragraphSm.copy(
        fontWeight = FontWeight.Normal,
    )

val Typography.paragraphRegularXs: TextStyle
    get() = paragraphXs.copy(
        fontWeight = FontWeight.Normal,
    )

// small sale price
val Typography.smallTitle: TextStyle
    get() = labelSmallXs.copy(
        fontSize = 10.sp,
        lineHeight = 16.sp,
    )

val Typography.smallHeader: TextStyle
    get() = labelSmallXs.copy(
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold
    )


// navigation
val Typography.navigation: TextStyle
    get() = titleSmall