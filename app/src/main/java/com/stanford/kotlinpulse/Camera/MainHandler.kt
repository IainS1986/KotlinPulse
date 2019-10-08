package com.stanford.kotlinpulse.Camera

import android.os.Handler
import android.os.Message
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.lang.ref.WeakReference

/**
 * Handler to send RGB values processed on backgroud thread back to the main
 * UI thread for plotting to a graph
 */
class MainHandler(lineseries: LineGraphSeries<DataPoint>) : Handler()
{
    private var MSG_SEND_DATAPOINT = 0

    private var _weakReference = WeakReference<LineGraphSeries<DataPoint>>(null)

    init {
        _weakReference = WeakReference(lineseries)
    }

    fun SendDataPointToGraph(point : DataPoint)
    {
        sendMessage(obtainMessage(MSG_SEND_DATAPOINT, point))
    }

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)

        when(msg.what)
        {
            MSG_SEND_DATAPOINT -> _weakReference.get()?.appendData(msg.obj as DataPoint, true, 5 * 30)
        }
    }
}