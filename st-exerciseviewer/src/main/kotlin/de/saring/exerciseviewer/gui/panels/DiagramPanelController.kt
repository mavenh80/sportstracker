package de.saring.exerciseviewer.gui.panels

import de.saring.exerciseviewer.data.HeartRateLimit
import de.saring.exerciseviewer.data.Lap
import de.saring.exerciseviewer.gui.EVContext
import de.saring.exerciseviewer.gui.EVDocument
import de.saring.util.AppResources
import de.saring.util.gui.javafx.ColorUtils
import de.saring.util.gui.jfreechart.ChartUtils
import de.saring.util.gui.jfreechart.FixedRangeNumberAxis
import de.saring.util.unitcalc.ConvertUtils
import de.saring.util.unitcalc.FormatUtils
import de.saring.util.unitcalc.SpeedMode
import de.saring.util.unitcalc.UnitSystem
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.ChoiceBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.layout.HBox
import javafx.util.StringConverter
import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.AxisLocation
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.fx.ChartViewer
import org.jfree.chart.labels.StandardXYToolTipGenerator
import org.jfree.chart.plot.IntervalMarker
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.ValueMarker
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYAreaRenderer
import org.jfree.chart.renderer.xy.XYItemRenderer
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.chart.renderer.xy.XYDifferenceRenderer
import org.jfree.chart.ui.RectangleAnchor
import org.jfree.chart.ui.TextAnchor
import org.jfree.data.general.Series
import org.jfree.data.time.Second
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import org.jfree.data.xy.XYDataset
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.Int

import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.scene.paint.Color
import org.jfree.data.Range
import kotlin.math.sqrt

/**
 * Controller (MVC) class of the "Samples" panel, which displays the exercise graphically (heartrate, altitude, speed,
 * cadence, power and temperature).
 *
 * @constructor constructor for dependency injection
 * @param context the ExerciseViewer UI context
 * @param document the ExerciseViewer document / model
 *
 * @author Stefan Saring
 */
class DiagramPanelController(
        context: EVContext,
        document: EVDocument) : AbstractPanelController(context, document) {

    // the colors and strokes of the chart
    private val colorAxisLeft = java.awt.Color(255, 30, 30)
    private val colorAxisLeftPlot = java.awt.Color(255, 30, 30, 130)
    private val colorAxisRight = java.awt.Color(20, 75, 255)
    private val colorAxisRightPlot = java.awt.Color(20, 75, 255, 136)
    private val colorMarkerLap = java.awt.Color(110, 110, 120)
    private val colorMarkerHeartrate = java.awt.Color(204, 204, 204, 77)
    private val colorMarkerAverage = java.awt.Color(190, 75, 55)
    private val strokeMarker = java.awt.BasicStroke(1.5f)

    private val timeZoneGmt = TimeZone.getTimeZone("GMT")

    private lateinit var axisTypeStringConverter: AxisTypeStringConverter

    /** The viewer for the chart.  */
    private var chartViewer: ChartViewer? = null

    /** The exercise heartrate range to be highlighted (null for no highlighting).  */
    private var highlightHeartrateRange: HeartRateLimit? = null

    /** The size of the average range if smoothed charts are enabled (otherwise 0).  */
    private var averagedRangeSteps: Int = 0

    /** The list of colored altitude slope ranges. */
    private val altitudeSlopeRanges = listOf(
            AltitudeSlopeRange(0, 5),
            AltitudeSlopeRange(5, 7),
            AltitudeSlopeRange(7, 10),
            AltitudeSlopeRange(10, 15),
            AltitudeSlopeRange(15, Int.MAX_VALUE))

    @FXML
    private lateinit var spDiagramPanel: StackPane

    @FXML
    private lateinit var vbDiagramPanel: VBox

    @FXML
    private lateinit var hbSlopesLegendPanel: HBox

    @FXML
    private lateinit var spDiagram: StackPane

    @FXML
    private lateinit var cbLeftAxis: ChoiceBox<AxisType>
    @FXML
    private lateinit var cbRightAxis: ChoiceBox<AxisType>
    @FXML
    private lateinit var cbBottomAxis: ChoiceBox<AxisType>

    override val fxmlFilename = "/fxml/panels/DiagramPanel.fxml"

    /**
     * Updates the diagram and highlights the specified heartrate range.
     *
     * @param heartrateRange heartrate range to highlight
     */
    fun displayDiagramForHeartrateRange(heartrateRange: HeartRateLimit) {
        highlightHeartrateRange = heartrateRange

        // don't update the diagram when this panel was not initialized yet
        if (chartViewer != null) {
            updateDiagram()
        }
    }

    override fun setupPanel() {

        // setup the diagram if data is available
        if (isDiagramDataAvailable()) {
            setupAxisChoiceBoxes()
            computeAveragedFilterRange()
            updateDiagram()
        } else {
            // remove the diagram panel VBox, the StackPane now displays the label "No diagram data available")
            spDiagramPanel.children.remove(vbDiagramPanel)
        }
    }

    private fun isDiagramDataAvailable(): Boolean {
        val recordingMode = document.exercise.recordingMode
        return !document.exercise.sampleList.isEmpty() && (
                recordingMode.isHeartRate || recordingMode.isAltitude || recordingMode.isSpeed ||
                        recordingMode.isCadence || recordingMode.isPower || recordingMode.isTemperature)
    }

    private fun setupAxisChoiceBoxes() {
        val exercise = document.exercise

        // setup axis type name converter
        axisTypeStringConverter = AxisTypeStringConverter(context.resources, context.formatUtils, document.speedMode)
        cbLeftAxis.converter = axisTypeStringConverter
        cbRightAxis.converter = axisTypeStringConverter
        cbBottomAxis.converter = axisTypeStringConverter

        // fill axes with all possible types depending on the exercise recording mode
        cbRightAxis.items.addAll(AxisType.NOTHING)
        cbBottomAxis.items.add(AxisType.TIME)

        // add heartrate items if recorded
        if (exercise.recordingMode.isHeartRate) {
            cbLeftAxis.items.add(AxisType.HEARTRATE)
            cbRightAxis.items.add(AxisType.HEARTRATE)
        }

        // add altitude items if recorded
        if (exercise.recordingMode.isAltitude) {
            cbLeftAxis.items.add(AxisType.ALTITUDE)
            cbRightAxis.items.add(AxisType.ALTITUDE)
        }

        // add speed and distance items if recorded
        if (exercise.recordingMode.isSpeed) {
            cbLeftAxis.items.add(AxisType.SPEED)
            cbRightAxis.items.add(AxisType.SPEED)
            // distance is the preferred mode for the bottom axis
            cbBottomAxis.items.add(0, AxisType.DISTANCE)
        }

        // add cadence items if recorded
        if (exercise.recordingMode.isCadence) {
            cbLeftAxis.items.add(AxisType.CADENCE)
            cbRightAxis.items.add(AxisType.CADENCE)
        }

        // add power items if recorded
        if (exercise.recordingMode.isPower) {
            cbLeftAxis.items.add(AxisType.POWER)
            cbRightAxis.items.add(AxisType.POWER)
        }

        // add temperature items if recorded
        if (exercise.recordingMode.isTemperature) {
            cbLeftAxis.items.add(AxisType.TEMPERATURE)
            cbRightAxis.items.add(AxisType.TEMPERATURE)
        }

        // select initial axis types
        cbLeftAxis.selectionModel.select(0)
        cbRightAxis.selectionModel.select(0)
        cbBottomAxis.selectionModel.select(0)

        // do we need to display initially the second diagram too? (only possible when additional data is available)
        // => first 2 entries are Nothing and Heartrate, which is already displayed
        if (document.options.isDisplaySecondChart && cbRightAxis.items.size > 2) {
            cbRightAxis.selectionModel.select(2)
        }

        // set listeners for updating the diagram on selection changes
        cbLeftAxis.addEventHandler(ActionEvent.ACTION) { updateDiagram() }
        cbRightAxis.addEventHandler(ActionEvent.ACTION) { updateDiagram() }
        cbBottomAxis.addEventHandler(ActionEvent.ACTION) { updateDiagram() }
    }

    /**
     * Compute the number of steps to be used for the averaged filter is smoothed charts are enabled. The size of
     * the average range depends on the number of samples in the current exercise.
     */
    private fun computeAveragedFilterRange() {
        if (document.options.isDisplaySmoothedCharts) {
            val sampleListLength = document.exercise.sampleList.size
            // results seem to be best when sample count is divided by 800 (tested with many exercises)
            averagedRangeSteps = Math.max(1, Math.round(sampleListLength / 800f))
        } else {
            averagedRangeSteps = 0
        }
    }

    /**
     * Draws the diagram according to the current axis type selection and configuration settings.
     */
    private fun updateDiagram() {
        val exercise = document.exercise

        val axisTypeLeft = cbLeftAxis.value
        val axisTypeRight = cbRightAxis.value
        val axisTypeBottom = cbBottomAxis.value
        val fDomainAxisTime = axisTypeBottom == AxisType.TIME

        // remove slope legend, they will be added later if needed
        hideSlopesLegend()

        // create and fill data series according to axis type
        // (right axis only when user selected a different axis type)
        val sLeft = createSeries(fDomainAxisTime, "left")
        var sRight: Series? = null
        if (axisTypeRight != AxisType.NOTHING && axisTypeRight != axisTypeLeft) {
            sRight = createSeries(fDomainAxisTime, "right")
        }

        // fill data series with all recorded exercise samples
        if (!exercise.sampleList.isEmpty()) {
            for (index in 0 until exercise.sampleList.size) {

                val sample = exercise.sampleList[index]
                val valueLeft = getConvertedSampleValue(axisTypeLeft, index)
                val valueRight = getConvertedSampleValue(axisTypeRight, index)

                if (fDomainAxisTime) {
                    // calculate current second
                    sample.timestamp?.let { timestamp ->
                        val timeSeconds = (timestamp / 1000).toInt()
                        val second = createJFreeChartSecond(timeSeconds)
                        fillDataInTimeSeries(sLeft as TimeSeries, sRight as TimeSeries?, second, valueLeft, valueRight)
                    }
                } else {
                    // get current distance of this sample
                    sample.distance?.let { distance ->
                        var fDistance = (distance / 1000f).toDouble()
                        if (context.formatUtils.unitSystem != UnitSystem.METRIC) {
                            fDistance = ConvertUtils.convertKilometer2Miles(fDistance, false)
                        }
                        fillDataInXYSeries(sLeft as XYSeries, sRight as XYSeries?, fDistance, valueLeft, valueRight)
                    }
                }
            }
        } else if (!exercise.lapList.isEmpty()) {
            // some Polar models only record lap data. no samples (e.g. RS200SD)

            // data starts with first lap => add 0 values (otherwise not displayed)
            if (fDomainAxisTime) {
                fillDataInTimeSeries(sLeft as TimeSeries, sRight as TimeSeries?, createJFreeChartSecond(0), 0, 0)
            } else {
                fillDataInXYSeries(sLeft as XYSeries, sRight as XYSeries?, 0.0, 0, 0)
            }

            // fill data series with all recorded exercise laps
            for (lap in exercise.lapList) {

                val valueLeft = getLapValue(axisTypeLeft, lap)
                val valueRight = getLapValue(axisTypeRight, lap)

                if (fDomainAxisTime) {
                    // calculate current second
                    val timeSeconds = Math.round(lap.timeSplit / 10f)
                    val second = createJFreeChartSecond(timeSeconds)
                    fillDataInTimeSeries(sLeft as TimeSeries, sRight as TimeSeries?, second, valueLeft, valueRight)
                } else {
                    // get current distance of this sample
                    var fDistance = (lap.speed!!.distance / 1000f).toDouble()
                    if (context.formatUtils.unitSystem != UnitSystem.METRIC) {
                        fDistance = ConvertUtils.convertKilometer2Miles(fDistance, false)
                    }
                    fillDataInXYSeries(sLeft as XYSeries, sRight as XYSeries?, fDistance, valueLeft, valueRight)
                }
            }
        }

        val dataset = createDataSet(fDomainAxisTime, sLeft)

        // create chart depending on domain axis type
        val chart: JFreeChart = if (fDomainAxisTime) {
            ChartFactory.createTimeSeriesChart(null, // Title
                    axisTypeStringConverter.toString(axisTypeBottom), // Y-axis label
                    axisTypeStringConverter.toString(axisTypeLeft), // X-axis label
                    dataset, // primary dataset
                    false, // display legend
                    true, // display tooltips
                    false) // URLs
        } else {
            ChartFactory.createXYLineChart(null, // Title
                    axisTypeStringConverter.toString(axisTypeBottom), // Y-axis label
                    axisTypeStringConverter.toString(axisTypeLeft), // X-axis label
                    dataset, // primary dataset
                    PlotOrientation.VERTICAL, // plot orientation
                    false, // display legend
                    true, // display tooltips
                    false) // URLs
        }

        // enable crosshair helper for x and y axis
        val plot = chart.plot as XYPlot
        plot.isDomainCrosshairVisible = true
        plot.isRangeCrosshairVisible = true

        // setup axis ranges in case of X axis displays the distance (XYSeries), not needed for TimeSeries
        if (!fDomainAxisTime && sLeft is XYSeries) {

            /// use custom Y axis with fixed ranges to avoid e.g. altitude to start with 0
            // (don't do that when the minimum value is 0 (e.g. for speed), then there will be a useless margin below 0)
            if (sLeft.minY != 0.0) {
                // use a buffer of 10 on both range ends for avoiding display errors for exercises with flat altitude
                plot.rangeAxis = FixedRangeNumberAxis(plot.rangeAxis.label, Range(sLeft.minY - 10.0, sLeft.maxY + 10.0), true)
            }

            /// use custom X axis with fixed ranges to avoid empty space on end of the distance axis
            plot.domainAxis = FixedRangeNumberAxis(plot.domainAxis.label, Range(0.0, sLeft.maxX), false)
        }

        // setup left axis
        val axisLeft = plot.getRangeAxis(0)
        axisLeft.labelPaint = colorAxisLeft
        axisLeft.tickLabelPaint = colorAxisLeft

        // for altitude vs. distance, color graph with slope
        // (don't do when the right axis displays another value, the colors are modified and can't be mapped anymore)
        if (!fDomainAxisTime && axisTypeLeft == AxisType.ALTITUDE && sRight == null) {
            plotAltitudeSlopes(sLeft as XYSeries, plot, colorAxisLeftPlot)
            setTooltipGenerator(plot.getRenderer(0), axisTypeBottom, axisTypeLeft)
        }
        else{
            // set custom area renderer
            val rendererLeft = XYAreaRenderer()
            rendererLeft.setSeriesPaint(0, colorAxisLeftPlot)
            plot.setRenderer(0, rendererLeft)
            setTooltipGenerator(rendererLeft, axisTypeBottom, axisTypeLeft)
        }

        // setup right axis (when selected)
        if (sRight != null) {

            val axisRight = NumberAxis(axisTypeStringConverter.toString(axisTypeRight))
            axisRight.autoRangeIncludesZero = false
            plot.setRangeAxis(1, axisRight)
            plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT)
            axisRight.labelPaint = colorAxisRight
            axisRight.tickLabelPaint = colorAxisRight

            // create dataset for right axis
            val datasetRight = createDataSet(fDomainAxisTime, sRight)
            plot.setDataset(1, datasetRight)
            plot.mapDatasetToRangeAxis(1, 1)

            // set custom area renderer
            val rendererRight = XYAreaRenderer()
            rendererRight.setSeriesPaint(0, colorAxisRightPlot)
            plot.setRenderer(1, rendererRight)
            setTooltipGenerator(rendererRight, axisTypeBottom, axisTypeRight)
        }

        // use TimeZone GMT on the time axis, because all Date value are GMT based
        if (fDomainAxisTime) {
            val dateAxis = plot.domainAxis as DateAxis
            dateAxis.timeZone = timeZoneGmt
        }

        // highlight current selected (if set) heartrate range when displayed on left axis
        // (don't highlight percentual ranges (is not possible, the values are absolute and the maximum heartrate is unknown)
        if (axisTypeLeft == AxisType.HEARTRATE &&
                highlightHeartrateRange != null && highlightHeartrateRange!!.isAbsoluteRange) {

            val hrRangeMarker = IntervalMarker(highlightHeartrateRange!!.lowerHeartRate.toDouble(),
                    highlightHeartrateRange!!.upperHeartRate.toDouble())
            hrRangeMarker.paint = colorMarkerHeartrate
            plot.addRangeMarker(hrRangeMarker)
        }
        // otherwise draw a horizontal marker line for the average value (if there is one)
        else {
            val averageValue: Double? = getAverageValueForAxisType(axisTypeLeft)
            if (averageValue != null) {
                val avgMarker = ValueMarker(averageValue)
                avgMarker.paint = colorMarkerAverage
                avgMarker.stroke = strokeMarker
                plot.addRangeMarker(avgMarker)
            }
        }

        // draw a vertical marker line for each lap (not for the last)
        if (exercise.lapList.size > 0) {

            for (i in 0 until exercise.lapList.size - 1) {
                val lap = exercise.lapList[i]
                var lapSplitValue: Double

                // compute lap split value (different for time or distance mode)
                // (the value must be milliseconds for time domain axis)
                if (fDomainAxisTime) {
                    val lapSplitSeconds = lap.timeSplit / 10
                    lapSplitValue = createJFreeChartSecond(lapSplitSeconds).firstMillisecond.toDouble()
                } else {
                    if (lap.speed == null) {
                        // ignore laps without speed data, can happen when the lap contains just a break
                        continue
                    }
                    lapSplitValue = lap.speed!!.distance / 1000.0
                    if (context.formatUtils.unitSystem == UnitSystem.ENGLISH) {
                        lapSplitValue = ConvertUtils.convertKilometer2Miles(lapSplitValue, false)
                    }
                }

                // create domain marker
                val lapMarker = ValueMarker(lapSplitValue)
                lapMarker.paint = colorMarkerLap
                lapMarker.stroke = strokeMarker
                lapMarker.label = context.resources.getString("pv.diagram.lap", i + 1)
                lapMarker.labelAnchor = RectangleAnchor.TOP_LEFT
                lapMarker.labelTextAnchor = TextAnchor.TOP_RIGHT
                lapMarker.labelBackgroundColor = java.awt.Color.WHITE
                plot.addDomainMarker(lapMarker)
            }
        }

        ChartUtils.customizeChart(chart)

        // display chart in viewer (chart viewer will be initialized lazily)
        if (chartViewer == null) {
            chartViewer = ChartViewer(chart)
            spDiagram.children.addAll(chartViewer)
        } else {
            chartViewer!!.chart = chart
        }
    }

    /**
     * Creates the JFreeChart Second instance for the specified number of seconds.
     *
     * @param seconds the number of seconds
     * @return the created Second instance
     */
    private fun createJFreeChartSecond(seconds: Int): Second =
            Second(Date(seconds * 1000L))

    /**
     * Creates a data series object for the specified domain axis type.
     *
     * @param fDomainAxisTime true when domain axis is time of false when distance
     * @param name name of the data series
     * @return the created data series
     */
    private fun createSeries(fDomainAxisTime: Boolean, name: String): Series =
            if (fDomainAxisTime) TimeSeries(name) else XYSeries(name)

    /**
     * Creates a dataset for the specified series and the domain axis type.
     *
     * @param fDomainAxisTime true when domain axis is time of false when distance
     * @param series the series to be addet to the dataset
     * @return the created dataset
     */
    private fun createDataSet(fDomainAxisTime: Boolean, series: Series): XYDataset =
            if (fDomainAxisTime) TimeSeriesCollection(series as TimeSeries) else XYSeriesCollection(series as XYSeries)

    /**
     * Fills the specified data to the left and right time series.
     *
     * @param sLeft the left TimeSeries
     * @param sRight the right TimeSeries (optional, can be null)
     * @param second the second for the current values
     * @param valueLeft the value of the left time series
     * @param valueRight the value of the right time series
     */
    private fun fillDataInTimeSeries(sLeft: TimeSeries, sRight: TimeSeries?, second: Second,
                                     valueLeft: Number?, valueRight: Number?) {

        // don't add the data when the specified second was already added
        if (sLeft.getValue(second) == null) {
            if (valueLeft != null) {
                sLeft.add(second, valueLeft)
            }
            if (valueRight != null) {
                sRight?.add(second, valueRight)
            }
        }
    }

    /**
     * Fills the specified data to the left and right XY series.
     *
     * @param sLeft the left XYSeries
     * @param sRight the right XYSeries (optional, can be null)
     * @param valueBottom the value of the bottom domain axis
     * @param valueLeft the value of the left time series
     * @param valueRight the value of the right time series
     */
    private fun fillDataInXYSeries(sLeft: XYSeries, sRight: XYSeries?, valueBottom: Double,
                                   valueLeft: Number?, valueRight: Number?) {

        sLeft.add(valueBottom, valueLeft)
        sRight?.add(valueBottom, valueRight)
    }

    /**
     * Sets the tooltip generator for the specified renderer.
     *
     * @param renderer the renderer for the tooltip
     * @param domainAxis type of the domain axis
     * @param valueAxis type of the value axis
     */
    private fun setTooltipGenerator(renderer: XYItemRenderer, domainAxis: AxisType, valueAxis: AxisType) {

        val format = "${axisTypeStringConverter.toString(domainAxis)}: {1}, ${axisTypeStringConverter.toString(valueAxis)}: {2}"

        if (domainAxis == AxisType.TIME) {
            val timeFormat = SimpleDateFormat("HH:mm")
            // all time values are using timezone GMT, so the formatter needs too
            timeFormat.timeZone = timeZoneGmt
            renderer.defaultToolTipGenerator = StandardXYToolTipGenerator(format, timeFormat, DecimalFormat())
        } else {
            renderer.defaultToolTipGenerator = StandardXYToolTipGenerator(format, DecimalFormat(), DecimalFormat())
        }
    }

    /**
     * Returns the value specified by the axis type of the exercise sample. It also converts the value to the current
     * unit system and speed view.
     *
     * @param axisType the axis type to be displayed
     * @param sampleIndex index of the sample in the exercise
     * @return the requested value
     */
    private fun getConvertedSampleValue(axisType: AxisType, sampleIndex: Int): Number? {
        if (axisType == AxisType.NOTHING) {
            return null
        }

        val sampleValue = getSampleValue(axisType, sampleIndex) ?: return null
        return getConvertedValueForAxisType(axisType, sampleValue)
    }

    /**
     * Returns the value specified by the axis type of the exercise sample. If smoothed charts are enabled, then the
     * smoothed value will be calculated by using the average filter of the computed size.
     *
     * @param axisType the axis type to be displayed
     * @param sampleIndex index of the sample in the exercise
     * @return the requested value
     */
    private fun getSampleValue(axisType: AxisType, sampleIndex: Int): Double? {

        if (averagedRangeSteps <= 0) {
            // smoothing is disabled, just return the raw value
            return getRawSampleValue(axisType, sampleIndex)
        } else {

            // the value of 0 stays 0, otherwise short stops will not be visible
            val rawSampleValue = getRawSampleValue(axisType, sampleIndex)
            if (rawSampleValue == 0.0) {
                return 0.0
            }

            val rangeLength = 2 * averagedRangeSteps + 1
            val lastSampleIndex = document.exercise.sampleList.size - 1

            // create sum for all range values
            var valueSum = 0.0
            for (i in sampleIndex - averagedRangeSteps..sampleIndex + averagedRangeSteps) {
                // exclude indices out of range, use first or last sample instead
                var valueIndex = Math.max(0, i)
                valueIndex = Math.min(lastSampleIndex, valueIndex)

                // ignore range values of null for the average, use the specified index instead (or 0 if also missing)
                val valueAtIndex = getRawSampleValue(axisType, valueIndex) ?:
                getRawSampleValue(axisType, sampleIndex) ?: 0.0

                valueSum += valueAtIndex
            }

            return valueSum / rangeLength
        }
    }

    private fun getRawSampleValue(axisType: AxisType, sampleIndex: Int): Double? {
        val sample = document.exercise.sampleList[sampleIndex]

        when (axisType) {
            AxisType.HEARTRATE ->
                return sample.heartRate?.toDouble()
            AxisType.ALTITUDE ->
                return sample.altitude?.toDouble()
            AxisType.SPEED ->
                return sample.speed?.toDouble()
            AxisType.CADENCE ->
                return sample.cadence?.toDouble()
            AxisType.POWER ->
                return sample.power?.toDouble()
            AxisType.TEMPERATURE ->
                return sample.temperature?.toDouble()
            else ->
                throw IllegalArgumentException("Unknown axis type: $axisType!")
        }
    }

    /**
     * Returns the converted value of the specified value for the axis type. A conversion is not needed for all axis types.
     *
     * @param axisType the axis type of the passed value
     * @param value value to convert
     * @return the converted value
     */
    private fun getConvertedValueForAxisType(axisType: AxisType, value: Double): Number {
        val formatUtils = context.formatUtils

        when (axisType) {
            AxisType.HEARTRATE, AxisType.CADENCE, AxisType.POWER ->
                return value
            AxisType.ALTITUDE ->
                return if (formatUtils.unitSystem == UnitSystem.METRIC)
                    value
                else
                    ConvertUtils.convertMeter2Feet(value)
            AxisType.SPEED -> {
                return getConvertedSpeedValue(value, formatUtils)
            }
            AxisType.TEMPERATURE ->
                return if (formatUtils.unitSystem == UnitSystem.METRIC)
                    value
                else
                    ConvertUtils.convertCelsius2Fahrenheit(Math.round(value).toShort())
            else ->
                throw IllegalArgumentException("Unknown axis type: $axisType!")
        }
    }

    private fun getConvertedSpeedValue(speedValue: Double, formatUtils: FormatUtils): Double {

        var speed = speedValue
        if (formatUtils.unitSystem != UnitSystem.METRIC) {
            speed = ConvertUtils.convertKilometer2Miles(speed, false)
        }

        return if (document.speedMode == SpeedMode.PACE && speed != 0.0) {
            60 / speed
        } else {
            speed
        }
    }

    /**
     * Returns the value specified by the axis type of the exercise lap. It also converts the value to the current unit
     * system and speed view.
     *
     * @param axisType the axis type to be displayed
     * @param lap the exercise lap to display
     * @return the requested value
     */
    private fun getLapValue(axisType: AxisType, lap: Lap): Number? {

        val formatUtils = context.formatUtils

        when (axisType) {
            AxisType.HEARTRATE ->
                return lap.heartRateAVG
            AxisType.SPEED -> {
                var speed = lap.speed?.speedAVG
                if (speed != null) {
                    speed = getConvertedSpeedValue(speed.toDouble(), formatUtils).toFloat()
                }
                return speed
            }
            else ->
                return null
        }
    }

    /**
     * Returns the average value for the specified axis type, if present. The average value is already converted
     * for the current format options.
     *
     * @param axisType axis type
     * @return average value (can be null if not present)
     */
    private fun getAverageValueForAxisType(axisType: AxisType): Double? {

        val averageValue = when (axisType) {
            AxisType.HEARTRATE ->
                document.exercise.heartRateAVG?.toDouble()
            AxisType.ALTITUDE ->
                document.exercise.altitude?.altitudeAvg?.toDouble()
            AxisType.SPEED ->
                document.exercise.speed?.speedAvg?.toDouble()
            AxisType.CADENCE ->
                document.exercise.cadence?.cadenceAvg?.toDouble()
            AxisType.POWER ->
                document.exercise.power?.powerAvg?.toDouble()
            AxisType.TEMPERATURE ->
                document.exercise.temperature?.temperatureAvg?.toDouble()
            else ->
                null
        } ?: return null

        return getConvertedValueForAxisType(axisType, averageValue).toDouble()
    }

    /**
     * Returns the list of ids that resamples (subsample) the data series at the (minimal) interval sampleDist along X.
     * Picking output ids from the input series will thus give a series whose data spacing is as small as possible but
     * greater than sampleDist. Resulting data may not be evenly spaced.
     * The output ids list will necessarily contain the first ID (0) and the last ID of the series to preseve data length,
     * consequently the last sampling interval may be < sampleDist.
     *
     * @param sampleDist desired output sample interval in meters
     * @param series input series to resample, X in kilometers
     * @return list of ids to resample the series
     */
    private fun getXYSeriesSubSampleIds(sampleDist: Int, series: XYSeries): List<Int> {
        val outputIds = mutableListOf(0)
        for (index in 1 until series.itemCount - 1){
            if (series.getDataItem(index).x.toDouble() - sampleDist.toDouble() /
                    1000.0 > series.getDataItem(outputIds.last()).x.toDouble()) {
                outputIds.add(index)
            }
        }
        outputIds.add(series.itemCount - 1)
        return outputIds
    }

    /**
     * Returns a newly created series based on the input series, from which some data points are [removed/set to 0]
     * (=filtered) regarding the data slope. All data from the input series that doesn't have a slope between slopeMin
     * and slopeMax are removed or set to 0. The slope is computed from the series at subSampleIds positions only,
     * which means that each "no-data-zone" or "data-zone" length will be at least the sampling interval used to create
     * subSampleIds. In addition, both ends of the "no-data-zones" and "data-zones" are vertical to produce vertical
     * lines when plotted: there are two points at each "zone" end with the same X.
     *
     * @param slopeMin minimal slope to keep data
     * @param slopeMax maximal slope to keep data
     * @param series input series
     * @param subSampleIds ids list from the input series used to evaluate the slope
     * @return new series, based on the input series,
     */
    private fun getSeriesFilteredBySlope(slopeMin: Int, slopeMax: Int, series: XYSeries, subSampleIds: List<Int>): Series {

        val name = if (slopeMax == Int.MAX_VALUE) "> $slopeMin%" else "< $slopeMax%"
        val outputSeries = XYSeries(name, false, true)
        var previousPointFiltered = false
        for (i in 0 until subSampleIds.size - 1){
            val dataItem = series.getDataItem(subSampleIds[i])
            val nextDataItem = series.getDataItem(subSampleIds[i + 1])

            var deltaD = nextDataItem.x.toDouble() - dataItem.x.toDouble() // distance delta in km or miles
            var deltaY = nextDataItem.y.toDouble() - dataItem.y.toDouble() // altitude delta in meters or feet

            if (document.options.unitSystem == UnitSystem.ENGLISH) {
                // convert to metric units for calculation
                deltaD = ConvertUtils.convertMiles2Kilometer(deltaD)
                deltaY = ConvertUtils.convertFeet2Meter(deltaY)
            }
            deltaD *= 1000.0 // convert distance to meters (altitude is already in meters)

            val dX = sqrt(deltaD * deltaD - deltaY * deltaY)
            val slope = abs(deltaY / dX) * 100

            // filter data
            if (slope < slopeMin || slope > slopeMax) {
                // first point of a filtered sequence -> make a vertical "decreasing" line
                if (!previousPointFiltered){
                    outputSeries.add(dataItem)
                    outputSeries.add(dataItem.x,0.0)
                }
                previousPointFiltered = true
            }
            else{
                // don't filter, keep the data
                if (previousPointFiltered) {
                    // first point of an unfiltered sequence -> make a vertical "increasing" line...
                    outputSeries.add(dataItem.x,0.0)
                }
                for (j in subSampleIds[i] until subSampleIds[i + 1]) {
                    // ...then add all altitude points to the output until the next subsample id
                    outputSeries.add(series.getDataItem(j))
                }
                if (i == subSampleIds.size - 2){ // for the last element
                    outputSeries.add(nextDataItem)
                }
                previousPointFiltered = false
            }
        }
        return outputSeries
    }

    /**
     * Add the renderer for altitude (single line), and add to the plot the slope information
     * (area below the altitude plot is coloured)
     * @param dataSeries Series complete input XY data series
     * @param plot XYPlot to draw graphs
     * @param baseColor the main color to be used (! green ignored)
     */
    private fun plotAltitudeSlopes(dataSeries: XYSeries, plot: XYPlot, baseColor: java.awt.Color) {

        val renderer = XYLineAndShapeRenderer()
        renderer.setSeriesPaint(0, java.awt.Color(baseColor.red, baseColor.green, baseColor.blue, 255) )
        renderer.setSeriesLinesVisible(0, true)
        renderer.setSeriesShapesVisible(0, false)
        plot.setRenderer(plot.rendererCount, renderer)
        var greenComponent = 240
        val subSampleIds = getXYSeriesSubSampleIds(100, dataSeries)

        for (i in 0 until altitudeSlopeRanges.size) {
            val it = altitudeSlopeRanges[i]
            val series = getSeriesFilteredBySlope(it.minSlope, it.maxSlope, dataSeries, subSampleIds)
            val name = series.key.toString()
            val dataset = createDataSet(false, series)
            val color = java.awt.Color(baseColor.red, greenComponent, baseColor.blue, baseColor.alpha)
            addSlopeLegendItem(name, ColorUtils.toFxColor(color))

            // two points is not valid as its a vertical line
            if (dataset.getItemCount(0) > 2){
                plot.setDataset(plot.rendererCount, dataset)
                val slopeRenderer = XYDifferenceRenderer(color, java.awt.Color(0, 0, 0, 0), false)
                slopeRenderer.setSeriesPaint(0, java.awt.Color(0, 0, 0, 0))
                plot.setRenderer(plot.rendererCount, slopeRenderer)
            }
            greenComponent -= 240 / (altitudeSlopeRanges.size - 1)
        }
    }

    private fun hideSlopesLegend() {
        vbDiagramPanel.children.remove(hbSlopesLegendPanel)
        hbSlopesLegendPanel.children.clear()
    }

    private fun addSlopeLegendItem(name: String, fillColor: Color) {

        // re-add slopes legend if it has been removed
        if (!vbDiagramPanel.children.contains(hbSlopesLegendPanel)) {
            vbDiagramPanel.children.add(0, hbSlopesLegendPanel)
        }

        val textLegendItem = Text(name)
        val rectLegendItem = Rectangle(56.0, 24.0).apply {
            fill = fillColor
            stroke = Color.BLACK
        }

        val spLegendItem = StackPane()
        spLegendItem.children.addAll(rectLegendItem, textLegendItem)
        hbSlopesLegendPanel.children.add(spLegendItem)
    }

    /**
     * The list of possible value types to be shown on the diagram axes. This enum also provides the the localized
     * displayed enum names.
     */
    private enum class AxisType {
        NOTHING, HEARTRATE, ALTITUDE, SPEED, CADENCE, POWER, TEMPERATURE, TIME, DISTANCE
    }

    /**
     * StringConverter for the axis type choice boxes. It returns the name to be displayed for all the available axis
     * types.
     */
    private class AxisTypeStringConverter(
            private val appResources: AppResources,
            private val formatUtils: FormatUtils,
            private val speedMode: SpeedMode) : StringConverter<AxisType>() {

        override fun toString(axisType: AxisType): String = when (axisType) {
            AxisType.NOTHING ->
                appResources.getString("pv.diagram.axis.nothing")
            AxisType.HEARTRATE ->
                appResources.getString("pv.diagram.axis.heartrate")
            AxisType.ALTITUDE ->
                appResources.getString("pv.diagram.axis.altitude", formatUtils.getAltitudeUnitName())
            AxisType.SPEED ->
                appResources.getString("pv.diagram.axis.speed", formatUtils.getSpeedUnitName(speedMode))
            AxisType.CADENCE ->
                appResources.getString("pv.diagram.axis.cadence")
            AxisType.POWER ->
                appResources.getString("pv.diagram.axis.power")
            AxisType.TEMPERATURE ->
                appResources.getString("pv.diagram.axis.temperature", formatUtils.getTemperatureUnitName())
            AxisType.TIME ->
                appResources.getString("pv.diagram.axis.time")
            AxisType.DISTANCE ->
                appResources.getString("pv.diagram.axis.distance", formatUtils.getDistanceUnitName())
        }

        override fun fromString(string: String): AxisType =
                throw UnsupportedOperationException()
    }

    /**
     * Defines a colored altitude slope for a specific range.
     */
    private class AltitudeSlopeRange(
            val minSlope: Int,
            val maxSlope: Int)
}
