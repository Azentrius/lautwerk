// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.prototype

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable

/** Native equivalent of the supplied primary pill; no ripple or motion. */
internal class PrototypeButtonBackground(private val context: Context) : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun isStateful() = true
    override fun onStateChange(state: IntArray): Boolean { invalidateSelf(); return true }
    override fun draw(canvas: Canvas) {
        val enabled = android.R.attr.state_enabled in state
        paint.style = Paint.Style.FILL
        paint.color = context.getColor(when {
            !enabled -> R.color.az_tint_neutral
            android.R.attr.state_pressed in state -> R.color.az_primary_hover
            else -> R.color.az_primary
        })
        val rect = RectF(bounds)
        canvas.drawRoundRect(rect, rect.height()/2, rect.height()/2, paint)
        if (enabled && android.R.attr.state_focused in state) {
            // Focus is supplied by Android for keyboard/D-pad, not ordinary touch activation.
            val dp = context.resources.displayMetrics.density
            rect.inset(-3*dp, -3*dp)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2*dp
            paint.color = context.getColor(R.color.az_focus)
            canvas.drawRoundRect(rect, rect.height()/2, rect.height()/2, paint)
        }
    }
    override fun setAlpha(alpha: Int) { paint.alpha = alpha }
    override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
    @Suppress("DEPRECATION") override fun getOpacity() = PixelFormat.TRANSLUCENT
}
