package com.yabu.livechartdemoapp

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yabu.livechart.model.DataPoint
import com.yabu.livechart.view.LiveChart
import com.yabu.livechart.view.LiveChartStyle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var livechartNegative: LiveChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        livechartNegative = findViewById(R.id.main_negative_live_chart)
        val negativeDataset = SampleData.createSampleData()
        livechartNegative.setDataset(negativeDataset)
            .drawYBounds()
            .drawBaseline()
            .drawFill()
            .stickyOverLay()
            .setOnTouchCallbackListener(object : LiveChart.OnTouchCallback {
                @SuppressLint("SetTextI18n")
                override fun onTouchCallback(point: DataPoint) {
                    livechartNegative.parent
                        .requestDisallowInterceptTouchEvent(true)
                    main_simple_data_point.text = "(${"%.2f".format(point.x)}, ${"%.2f".format(point.y)})"

                }

                override fun onTouchFinished() {
                    livechartNegative.parent
                        .requestDisallowInterceptTouchEvent(false)
                }
            })
            .drawDataset()
    }
}