package com.congnguyencn.stream_tv.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

private val StreamTvDarkColorScheme = darkColorScheme(
  primary = StreamTvPrimary,
  onPrimary = StreamTvOnPrimary,
  primaryContainer = StreamTvPrimaryContainer,
  onPrimaryContainer = StreamTvOnPrimaryContainer,
  secondary = StreamTvSecondary,
  onSecondary = StreamTvOnSecondary,
  secondaryContainer = StreamTvSecondaryContainer,
  onSecondaryContainer = StreamTvOnSecondaryContainer,
  tertiary = StreamTvTertiary,
  onTertiary = StreamTvOnTertiary,
  tertiaryContainer = StreamTvTertiaryContainer,
  onTertiaryContainer = StreamTvOnTertiaryContainer,
  error = StreamTvError,
  onError = StreamTvOnError,
  errorContainer = StreamTvErrorContainer,
  onErrorContainer = StreamTvOnErrorContainer,
  background = StreamTvBackground,
  onBackground = StreamTvOnBackground,
  surface = StreamTvSurface,
  onSurface = StreamTvOnSurface,
  surfaceVariant = StreamTvSurfaceVariant,
  onSurfaceVariant = StreamTvOnSurfaceVariant,
  scrim = StreamTvScrim,
  inverseSurface = StreamTvInverseSurface,
  inverseOnSurface = StreamTvInverseOnSurface,
  inversePrimary = StreamTvInversePrimary,
)

private val LocalStreamTvTypography = staticCompositionLocalOf { DefaultStreamTvTypography }

@Composable
fun StreamTvTheme(content: @Composable () -> Unit) {
  CompositionLocalProvider(LocalStreamTvTypography provides DefaultStreamTvTypography) {
    MaterialTheme(
      colorScheme = StreamTvDarkColorScheme,
      content = content,
    )
  }
}

object StreamTvTheme {
  val typography: StreamTvTypography
    @Composable
    @ReadOnlyComposable
    get() = LocalStreamTvTypography.current
}
