package de.canyumusak.androiddrop

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.text.style.LineHeightSpan
import android.util.DisplayMetrics
import androidx.annotation.Px

class EnumerationSpan(val context: Context, val leadingSymbol: String, gapWidth: Int, paragraphSpacing: Int) : LeadingMarginSpan, LineHeightSpan {

    @Px
    val gapWidth = gapWidth.dpToPixel(context)

    @Px
    val paragraphSpacing = paragraphSpacing.dpToPixel(context)

    override fun getLeadingMargin(first: Boolean) = gapWidth
    override fun drawLeadingMargin(
            canvas: Canvas,
            paint: Paint,
            x: Int,
            dir: Int,
            top: Int,
            baseline: Int,
            bottom: Int,
            text: CharSequence,
            start: Int,
            end: Int,
            first: Boolean,
            layout: Layout?
    ) {
        if ((text as Spanned).getSpanStart(this) == start) {
            canvas.drawText(leadingSymbol, x.toFloat(), baseline.toFloat(), paint)
        }
    }

    override fun chooseHeight(
        text: CharSequence?,
        start: Int,
        end: Int,
        spanstartv: Int,
        lineHeight: Int,
        fm: Paint.FontMetricsInt?
    ) {
        if ((text as Spanned).getSpanEnd(this) == end) {
            fm?.let {
                it.descent += paragraphSpacing
            }
        }
    }
}


fun List<String>.withLeadingEnumeration(context: Context, gapWidth: Int = 30, paragraphSpacing: Int = 5): SpannableStringBuilder {
    val spannableStringBuilder = SpannableStringBuilder()
    map {
        if (it != last()) {
            it.trim() + "\n"
        } else {
            it.trim()
        }
    }.forEachIndexed { index, item ->
        if (item.isNotBlank()) {
            val span = EnumerationSpan(context, "${index + 1}.", gapWidth, paragraphSpacing)
            spannableStringBuilder.appendWithSpan(item, span)
        } else {
            spannableStringBuilder.append(item)
        }
    }

    return spannableStringBuilder
}

fun SpannableStringBuilder.appendWithSpan(text: String, span: Any) {
    val initialLength = length
    append(text)
    setSpan(span, initialLength, initialLength + text.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
}

fun Int.dpToPixel(context: Context): Int {
    return Math.round(this * (context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
}
