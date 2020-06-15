package com.yabu.livechart.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.yabu.livechart.R
import com.yabu.livechart.model.Bounds
import com.yabu.livechart.model.Dataset
import com.yabu.livechart.utils.PublicApi
import com.yabu.livechart.view.LiveChartAttributes.CHART_END_PADDING
import com.yabu.livechart.view.LiveChartAttributes.TAG_PADDING
import com.yabu.livechart.view.LiveChartAttributes.TAG_WIDTH
import com.yabu.livechart.view.LiveChartAttributes.TEXT_HEIGHT
import kotlin.math.max
import kotlin.math.min

/**
 * A Live Chart displays a 2 Dimensional lined data points, with an optional live
 * subscription to push new data points to the end of the data set.
 *
 * The chart can have a baseline onto which the end point data set is compared to determine whether
 * it has positive or negative change, and highlights the data set accordingly with color.
 *
 * The end data point can be tagged with a label and draw a line across the chart, with highlighted
 * color according to the baseline.
 */
class LiveChartView : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        clipToOutline = false
    }

    private var chartBounds = Bounds(
        top = paddingTop.toFloat(),
        end = paddingEnd.toFloat(),
        bottom = paddingBottom.toFloat(),
        start = paddingLeft.toFloat()
    )

    /**
     * Baseline to determine paint color from data end point.
     */
    private var chartStyle: LiveChartStyle = LiveChartStyle()

    /**
     * Baseline to determine paint color from data end point.
     */
    private var baseline: Float = 0f

    /**
     * Dataset points to draw on chart.
     */
    private var dataset: Dataset = Dataset.new()

    /**
     * Second dataset
     */
    private var secondDataset: Dataset = Dataset.new()

    /**
     * Y Bounds display flag.
     */
    private var drawYBounds = false

    /**
     * Baseline display flag.
     */
    private var drawBaseline = false

    /**
     * Fill display flag.
     */
    private var drawFill = false

    /**
     * Label last point.
     */
    private var drawLastPointLabel = false

    /**
     * Disable baseline conditional.
     */
    private var drawBaselineConditionalColor = false

    /**
     * Manually set baseline flag.
     */
    private var manualBaseline = false

    /**
     * Second data set path color.
     */
    private var secondDatasetPathColor = ContextCompat.getColor(context,
        R.color.secondDataset)

    /**
     * Set the [dataset] of this chart.
     */
    @PublicApi
    fun setDataset(dataset: Dataset): LiveChartView {
        this.dataset = dataset
        return this
    }

    /**
     * Set the Second [dataset] of this chart.
     */
    @PublicApi
    fun setSecondDataset(dataset: Dataset): LiveChartView {
        this.secondDataset = dataset
        return this
    }

    /**
     * Draw baseline flag.
     */
    @PublicApi
    fun setLiveChartStyle(style: LiveChartStyle): LiveChartView {
        chartStyle = style

        // paint color
        datasetLinePaint.color = chartStyle.mainColor
        datasetFillPaint.color = Color.parseColor(chartStyle.mainFillColor)
        baselinePaint.color = chartStyle.baselineColor
        boundsTextPaint.color = chartStyle.textColor

        // stroke width
        datasetLinePaint.strokeWidth = chartStyle.pathStrokeWidth
        baselinePaint.strokeWidth = chartStyle.baselineStrokeWidth
        // baseline path effect
        if (chartStyle.baselineDashLineGap > 0f) {
            baselinePaint.pathEffect = DashPathEffect(floatArrayOf(
                chartStyle.baselineDashLineWidth,
                chartStyle.baselineDashLineGap
            ), 0f)
        }

        return this
    }

    /**
     * Draw baseline flag.
     */
    @PublicApi
    fun drawBaselineConditionalColor(): LiveChartView {
        drawBaselineConditionalColor = true

        return this
    }

    /**
     * Draw baseline flag.
     */
    @PublicApi
    fun drawBaseline(): LiveChartView {
        drawBaseline = true

        return this
    }

    /**
     * Draw Fill flag.
     */
    @PublicApi
    fun drawFill(): LiveChartView {
        drawFill = true

        return this
    }

    /**
     * Draw Y bounds flag.
     */
    @PublicApi
    fun drawYBounds(): LiveChartView {
        drawYBounds = true

        return this
    }

    /**
     * Draw last point label flag.
     */
    @PublicApi
    fun drawLastPointLabel(): LiveChartView {
        drawLastPointLabel = true

        return this
    }

    /**
     * Set [baseline] data point manually instead of determining from first dataset point.
     */
    @PublicApi
    fun setBaselineManually(baseline: Float): LiveChartView {
        manualBaseline = true
        this.baseline = baseline

        return this
    }

    /**
     * Set Second dataset path color.
     */
    @PublicApi
    fun setSecondDatasetColor(pathColor: Int): LiveChartView {
        secondDatasetPathColor = pathColor

        secondDatasetPaint.color = pathColor

        return this
    }

    /**
     * Helper to set the chart highlight color according to [baseline]
     */
    private fun Paint.setColor() {
        if (drawBaselineConditionalColor) {
            this.color = if (dataset.points.last().y > baseline) {
                Color.parseColor(chartStyle.positiveColor)
            } else {
                Color.parseColor(chartStyle.negativeColor)
            }
        } else {
            this.color = chartStyle.mainColor
        }
    }

    /**
     * Path generated from dataset points.
     */
    private var datasetPath = Path().apply {
        moveTo(chartBounds.start, baseline)
        dataset.points.forEach { point ->
            lineTo(point.x,
                baseline.yPointToPixels())
        }
    }

    /**
     * Line [Paint] for this chart.
     */
    private var datasetLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = chartStyle.pathStrokeWidth
        setColor()
        strokeCap = Paint.Cap.BUTT
        strokeJoin = Paint.Join.MITER
    }

    /**
     * Path generated from dataset points.
     */
    private var secondDatasetPath = Path().apply {
        moveTo(chartBounds.start, baseline)
        dataset.points.forEach { point ->
            lineTo(point.x,
                baseline.yPointToPixels())
        }
    }

    /**
     * Line [Paint] for this chart.
     */
    private var secondDatasetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = chartStyle.pathStrokeWidth
        color = secondDatasetPathColor
        strokeCap = Paint.Cap.BUTT
        strokeJoin = Paint.Join.MITER
    }

    /**
     * Path generated from dataset points.
     */
    private var datasetFillPath = Path().apply {
        moveTo(chartBounds.start, baseline)
        dataset.points.forEach { point ->
            lineTo(point.x,
                point.y)
        }
    }

    /**
     * Line [Paint] for this chart.
     */
    private var datasetFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        isDither = true
        setColor()
    }

    /**
     * Line [Paint] for this chart.
     */
    private var baselinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = chartStyle.baselineStrokeWidth
        if (chartStyle.baselineDashLineGap > 0f) {
            pathEffect = DashPathEffect(floatArrayOf(
                chartStyle.baselineDashLineWidth,
                chartStyle.baselineDashLineGap
            ), 0f)
        }
        color = chartStyle.baselineColor
        strokeCap = Paint.Cap.SQUARE
        strokeJoin = Paint.Join.ROUND
    }

    /**
     * Line [Paint] for this chart.
     */
    private var yBoundLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
        color = Color.GRAY
        strokeCap = Paint.Cap.SQUARE
    }

    /**
     * End Point Line [Paint] for this chart.
     */
    private var endPointLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        setColor()
        strokeCap = Paint.Cap.SQUARE
        strokeJoin = Paint.Join.ROUND
    }

    /**
     * End Point Tag [Paint] for this chart.
     */
    private var endPointTagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        typeface = Typeface.DEFAULT_BOLD
        setColor()
    }

    /**
     * End Point Tag [Paint] for this chart.
     */
    private var endPointTagTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.DEFAULT_BOLD
        textSize = TEXT_HEIGHT
    }

    /**
     * End Point Tag [Paint] for this chart.
     */
    private var boundsTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = chartStyle.textColor
        textSize = TEXT_HEIGHT
    }

    /**
     * Find the bounds data point to screen pixels ratio for the Y Axis.
     */
    private fun yBoundsToPixels(): Float {
        return if (secondDataset.hasData()) {
            (max(dataset.upperBound(),
                secondDataset.upperBound()) -
                    min(dataset.lowerBound(),
                        secondDataset.lowerBound())) / chartBounds.bottom
        } else {
            (dataset.upperBound() - dataset.lowerBound()) / chartBounds.bottom
        }
    }

    /**
     * Transform a Y Axis data point to screen pixels within bounds.
     */
    private fun Float.yPointToPixels(): Float {
        return if (secondDataset.hasData()) {
            chartBounds.bottom -
                    ((this - min(dataset.lowerBound(),
                        secondDataset.lowerBound())) / yBoundsToPixels())
        } else {
            chartBounds.bottom - ((this - dataset.lowerBound()) / yBoundsToPixels())
        }
    }

    /**
     * Find the bounds data point to screen pixels ratio for the X Axis.
     */
    private fun xBoundsToPixels(): Float {
        return dataset.points.last().x /
                (chartBounds.end - if (drawYBounds) CHART_END_PADDING else 0f)
    }

    /**
     * Transform a X Axis data point to screen pixels.
     */
    private fun Float.xPointToPixels(): Float {
        return this/xBoundsToPixels()
    }

    /**
     * Set the charts paints current color.
     */
    private fun setChartHighlightColor() {
        datasetLinePaint.setColor()
        endPointLinePaint.setColor()
        endPointTagPaint.setColor()
    }

    /**
     * Draw the current [dataset].
     */
    @PublicApi
    fun drawDataset(): LiveChartView {
        this.post {
            if (dataset.points.isNullOrEmpty()) {
                return@post
            }

            if (!manualBaseline) {
                baseline = dataset.points.first().y
            }

            datasetPath = Path().apply {
                dataset.points.forEachIndexed { index, point ->
                    // move path to first data point,
                    if (index == 0) {
                        moveTo(chartBounds.start + point.x.xPointToPixels(),
                            point.y.yPointToPixels())
                        return@forEachIndexed
                    }

                    lineTo(chartBounds.start + point.x.xPointToPixels(),
                        point.y.yPointToPixels())
                }
            }

            secondDatasetPath = Path().apply {
                secondDataset.points.forEachIndexed { index, point ->
                    // move path to first data point,
                    if (index == 0) {
                        moveTo(chartBounds.start, point.y.yPointToPixels())
                        return@forEachIndexed
                    }

                    lineTo(chartBounds.start + point.x.xPointToPixels(),
                        point.y.yPointToPixels())
                }
            }

            datasetFillPath = Path().apply {
                dataset.points.forEachIndexed { index, point ->
                    // move path to first data point,
                    if (index == 0) {
                        moveTo(chartBounds.start, baseline.yPointToPixels())
                        return@forEachIndexed
                    }

                    lineTo(chartBounds.start + point.x.xPointToPixels(),
                        point.y.yPointToPixels())
                }
                lineTo(chartBounds.start + dataset.points.last().x.xPointToPixels(),
                    chartBounds.bottom)
                lineTo(chartBounds.start,
                    chartBounds.bottom)
            }

            var fillColor = chartStyle.mainFillColor

            if (drawBaselineConditionalColor) {
                fillColor = if (dataset.points.last().y > baseline) {
                    chartStyle.positiveFillColor
                } else {
                    chartStyle.negativeFillColor
                }
            }

            datasetFillPaint.shader = LinearGradient(chartBounds.start,
                dataset.upperBound().yPointToPixels(),
                chartBounds.start,
                chartBounds.bottom,
                Color.parseColor(fillColor),
                Color.parseColor("#00000000"),
                Shader.TileMode.CLAMP)

            setChartHighlightColor()

            // invalidate view to call onDraw,
            invalidate()
        }

        return this
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!dataset.hasData()) {
            return
        }

        if (drawBaseline) {
            // draw baseline
            canvas.drawLine(chartBounds.start,
                baseline.yPointToPixels(),
                // account for end padding only if Y Bounds are visible
                chartBounds.end  - if (drawYBounds) CHART_END_PADDING else 0f,
                baseline.yPointToPixels(),
                baselinePaint)
        }

        if (secondDataset.points.size > 1) {
            canvas.drawPath(secondDatasetPath,
                secondDatasetPaint)
        }

        // draw dataset
        canvas.drawPath(datasetPath,
            datasetLinePaint)

        if (drawFill) {
            canvas.drawPath(datasetFillPath,
                datasetFillPaint)
        }

        if (drawYBounds) {
            // draw y Bounds line,
            canvas.drawLine(chartBounds.end - CHART_END_PADDING,
                chartBounds.top,
                chartBounds.end - CHART_END_PADDING,
                chartBounds.bottom,
                yBoundLinePaint)

            // LOWER BOUND
            canvas.drawText("%.2f".format(dataset.lowerBound()),
                chartBounds.end - TAG_WIDTH,
                chartBounds.bottom,
                boundsTextPaint)

            // UPPER BOUND
            canvas.drawText("%.2f".format(dataset.upperBound()),
                chartBounds.end - TAG_WIDTH,
                chartBounds.top,
                boundsTextPaint)

            // Last Point Label
            if (drawLastPointLabel) {
                // draw end tag line
                canvas.drawLine(chartBounds.start,
                    dataset.points.last().y.yPointToPixels(),
                    chartBounds.end - CHART_END_PADDING,
                    dataset.points.last().y.yPointToPixels(),
                    endPointLinePaint)

                // TAG
                canvas.drawRect(chartBounds.end - CHART_END_PADDING,
                    dataset.points.last().y.yPointToPixels() - TEXT_HEIGHT - TAG_PADDING,
                    chartBounds.end - CHART_END_PADDING + TAG_WIDTH,
                    dataset.points.last().y.yPointToPixels(),
                    endPointTagPaint)

                canvas.drawText("%.2f".format(dataset.points.last().y),
                    chartBounds.end - CHART_END_PADDING + TAG_PADDING,
                    dataset.points.last().y.yPointToPixels() - TAG_PADDING,
                    endPointTagTextPaint)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Account for padding
        val xpad = (paddingLeft + paddingRight).toFloat()
        val ypad = (paddingTop + paddingBottom).toFloat()

        val ww = w.toFloat() - xpad
        val hh = h.toFloat() - ypad

        // Figure out how big we can make the pie.
        chartBounds = Bounds(
            top = paddingTop.toFloat(),
            end = ww,
            bottom = hh,
            start = paddingLeft.toFloat()
        )
    }
}