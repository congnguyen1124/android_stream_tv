package com.congnguyencn.stream_tv.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

private object StreamTvQrCodeDefaults {
  val Shape = RoundedCornerShape(10.dp)
  val QuietZone = 10.dp
  val PreviewSize = 132.dp

  /** Zero, because the quiet zone is drawn as layout padding on the white plate instead. */
  const val EncoderMargin = 0
  const val CharacterSet = "UTF-8"
}

/** One encoded QR symbol: [size] modules per side, row-major dark-module flags. */
internal class StreamTvQrMatrix(val size: Int, private val darkModules: BooleanArray) {
  fun isDark(column: Int, row: Int): Boolean = darkModules[row * size + column]
}

/**
 * A scannable QR symbol for [content], drawn as dark modules on a white plate.
 *
 * Encoding is pure computation with no Android or network dependency, so this renders in previews
 * and stays identical for identical [content]. Unencodable content leaves the plate blank rather
 * than showing a symbol a phone camera would reject.
 */
@Composable
fun StreamTvQrCode(content: String, contentDescription: String?, modifier: Modifier = Modifier) {
  val matrix = remember(content) { encodeStreamTvQrMatrix(content) }

  Box(
    modifier = modifier
      .clip(StreamTvQrCodeDefaults.Shape)
      .background(StreamTvColors.NeutralWhite)
      .padding(StreamTvQrCodeDefaults.QuietZone)
      .semantics { if (contentDescription != null) this.contentDescription = contentDescription },
  ) {
    if (matrix != null) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val modulePitch = min(size.width, size.height) / matrix.size
        for (row in 0 until matrix.size) {
          for (column in 0 until matrix.size) {
            if (!matrix.isDark(column = column, row = row)) continue
            // Snapping every edge outward keeps adjacent modules seam-free at TV densities, where
            // a module lands on a fractional pixel more often than not.
            val left = floor(column * modulePitch)
            val top = floor(row * modulePitch)
            drawRect(
              color = StreamTvColors.NeutralBlack,
              topLeft = Offset(x = left, y = top),
              size = Size(
                width = ceil((column + 1) * modulePitch) - left,
                height = ceil((row + 1) * modulePitch) - top,
              ),
            )
          }
        }
      }
    }
  }
}

/** Encodes [content] at the smallest QR version that holds it, or null when it cannot be encoded. */
internal fun encodeStreamTvQrMatrix(content: String): StreamTvQrMatrix? {
  if (content.isBlank()) return null

  val bitMatrix = try {
    // The two zeros are the requested pixel width and height: asking for none makes the writer
    // return the module matrix itself, which is what the Canvas scales to the plate.
    QRCodeWriter().encode(
      content,
      BarcodeFormat.QR_CODE,
      0,
      0,
      mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
        EncodeHintType.MARGIN to StreamTvQrCodeDefaults.EncoderMargin,
        EncodeHintType.CHARACTER_SET to StreamTvQrCodeDefaults.CharacterSet,
      ),
    )
  } catch (error: WriterException) {
    return null
  } catch (error: IllegalArgumentException) {
    return null
  }

  val moduleCount = bitMatrix.width
  val darkModules = BooleanArray(moduleCount * moduleCount)
  for (row in 0 until moduleCount) {
    for (column in 0 until moduleCount) {
      darkModules[row * moduleCount + column] = bitMatrix.get(column, row)
    }
  }
  return StreamTvQrMatrix(size = moduleCount, darkModules = darkModules)
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun StreamTvQrCodePreview() {
  StreamTvTheme {
    StreamTvQrCode(
      content = "https://tv.streamtv.example.com/pair?code=XHSZ-QBKX",
      contentDescription = null,
      modifier = Modifier.size(StreamTvQrCodeDefaults.PreviewSize),
    )
  }
}
