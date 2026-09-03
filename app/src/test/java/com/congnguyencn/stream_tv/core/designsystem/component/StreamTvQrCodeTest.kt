package com.congnguyencn.stream_tv.core.designsystem.component

import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.common.HybridBinarizer
import kotlin.math.ceil
import kotlin.math.floor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StreamTvQrCodeTest {
  @Test
  fun `encoding a pairing link yields a symbol with no quiet zone modules`() {
    val matrix = encodeMatrix(PairingUrl)

    // The smallest QR version is 21 modules per side; the plate draws the quiet zone as padding,
    // so the outermost module has to be part of the top-left finder pattern.
    assertTrue(matrix.size >= SmallestQrVersionModuleCount)
    assertTrue(matrix.isDark(column = 0, row = 0))
  }

  @Test
  fun `the same content always encodes to the same modules`() {
    val first = encodeMatrix(PairingUrl)
    val second = encodeMatrix(PairingUrl)

    assertEquals(first.size, second.size)
    val differingModules = (0 until first.size).flatMap { row ->
      (0 until first.size).filter { column ->
        first.isDark(column = column, row = row) != second.isDark(column = column, row = row)
      }
    }
    assertTrue(differingModules.isEmpty())
  }

  @Test
  fun `the drawn symbol scans back to the pairing link`() {
    val rendering = encodeMatrix(PairingUrl).render(modulePitch = FractionalModulePitch)

    val decoded = MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(rendering)))

    assertEquals(PairingUrl, decoded.text)
  }

  @Test
  fun `blank content encodes to nothing rather than an unscannable symbol`() {
    assertNull(encodeStreamTvQrMatrix(""))
    assertNull(encodeStreamTvQrMatrix("   "))
  }

  private fun encodeMatrix(content: String): StreamTvQrMatrix =
    requireNotNull(encodeStreamTvQrMatrix(content)) { "expected $content to encode" }

  /**
   * Rasterizes the symbol the way the composable draws it — a fractional module pitch snapped
   * outward — surrounded by the quiet zone the white plate contributes as padding. A deliberately
   * awkward pitch is used so that rounding mistakes in the drawing rule show up as a decode
   * failure rather than as a subtly misaligned symbol nobody notices until a phone rejects it.
   */
  private fun StreamTvQrMatrix.render(modulePitch: Float): LuminanceSource {
    val quietZonePixels = ceil(modulePitch * QuietZoneModuleCount).toInt()
    val symbolPixels = ceil(modulePitch * size).toInt()
    val imagePixels = symbolPixels + quietZonePixels * 2
    val luminances = ByteArray(imagePixels * imagePixels) { White }

    for (row in 0 until size) {
      for (column in 0 until size) {
        if (!isDark(column = column, row = row)) continue
        val left = floor(column * modulePitch).toInt()
        val top = floor(row * modulePitch).toInt()
        for (y in top until ceil((row + 1) * modulePitch).toInt()) {
          for (x in left until ceil((column + 1) * modulePitch).toInt()) {
            luminances[(y + quietZonePixels) * imagePixels + x + quietZonePixels] = Black
          }
        }
      }
    }

    return GrayscaleLuminanceSource(
      width = imagePixels,
      height = imagePixels,
      luminances = luminances,
    )
  }

  private class GrayscaleLuminanceSource(
    width: Int,
    height: Int,
    private val luminances: ByteArray,
  ) : LuminanceSource(width, height) {
    override fun getRow(y: Int, row: ByteArray?): ByteArray {
      val destination = if (row != null && row.size >= width) row else ByteArray(width)
      luminances.copyInto(destination, destinationOffset = 0, startIndex = y * width, endIndex = (y + 1) * width)
      return destination
    }

    override fun getMatrix(): ByteArray = luminances
  }

  private companion object {
    const val PairingUrl = "https://tv.streamtv.example.com/pair?code=XHSZ-QBKX"
    const val SmallestQrVersionModuleCount = 21
    const val QuietZoneModuleCount = 4
    const val FractionalModulePitch = 5.37f
    const val White: Byte = -1
    const val Black: Byte = 0
  }
}
