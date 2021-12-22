package com.machina.gorun.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.geometry.Point
import com.google.maps.android.projection.SphericalMercatorProjection
import com.machina.gorun.R
import com.machina.gorun.databinding.FragmentJoggingPathBinding
import com.machina.gorun.viewmodels.JoggingPathViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class JoggingPathFragment : Fragment() {

    private var _binding: FragmentJoggingPathBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<JoggingPathViewModel>()
    private val args by navArgs<JoggingPathFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentJoggingPathBinding.inflate(inflater, container, false)

        setupView()
        subscribeToObservables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

    private fun setupView() {
        viewModel.getJoggingPoints(args.id)
    }

    private fun subscribeToObservables() {
        viewModel.joggingPoints.observe(viewLifecycleOwner) { points ->

            lifecycleScope.launch {
                var width = 0
                var height = 0
                while (true) {
                    if (binding.fragmentJoggingPathMap.width > 0) {
                        width = binding.fragmentJoggingPathMap.width
                        height = binding.fragmentJoggingPathMap.height
                        Timber.d("view width ${binding.fragmentJoggingPathMap.width}")
                        Timber.d("view height ${binding.fragmentJoggingPathMap.height}")

                        break
                    }
                    delay(1000L)
                }


                val sphereProj = SphericalMercatorProjection(width.toDouble())

                var canvasPoints = mutableListOf<Point>()

                points.forEach {
                    canvasPoints.add(sphereProj.toPoint(LatLng(it.latitude, it.longitude)))
                }

                canvasPoints.forEach { Timber.d(it.toString()) }


                val bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bm)
                val mPaint = Paint().apply {
                    color = Color.BLACK
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    strokeWidth = 10F
                }

                var maxX = canvasPoints.maxOf { it.x }
                val minX = canvasPoints.minOf { it.x }
                var maxY = canvasPoints.maxOf { it.y }
                val minY = canvasPoints.minOf { it.y }


                val midX = (maxX + minX) / 2
                val midY = (maxY + minY) / 2
                var xtransform = 0.0
                if (maxX < width/2) {
                    xtransform = (width / 2 - maxX) + (maxX - midX)
                } else if (minX > width/2) {
                    xtransform = (width / 2 - minX) + (minX - midX)
                }

                var ytransform = 0.0
                if (maxY < height/2) {
                    ytransform = (height / 2 - maxY) + (maxY - midY)
                } else if (minY > width/2) {
                    ytransform = (height / 2 - minY) + (minY - midY)
                }

                canvasPoints = canvasPoints.map { point ->
                    Point(
                        point.x + xtransform,
                        point.y + ytransform
                    )
                }.toMutableList()

//            val offsetX = (maxX + minX) / 2
//            val offsetY = (maxY + minY) / 2

                val offsetX = width / 2
                val offsetY = height / 2

                val kv = height / (maxY - minY)
                val kh = width / (maxX - minX)

                val k = maxOf(kv, kh) / 2
                canvasPoints.forEach { Timber.d(it.toString()) }
                canvasPoints = canvasPoints.map { point ->
                    Point(
                        (k * (point.x - offsetX)) + offsetX,
                        (k * (point.y - offsetY)) + offsetY
                    )
                }.toMutableList()
                canvasPoints.forEach {
                    Timber.d("${it.x}, ${it.y}, ")
                }
                canvasPoints.forEachIndexed { index, point ->
                    if (index < canvasPoints.size - 1) {
                        canvas.drawLine(
                            canvasPoints[index].x.toFloat(),
                            canvasPoints[index].y.toFloat(),
                            canvasPoints[index + 1].x.toFloat(),
                            canvasPoints[index + 1].y.toFloat(),
                            mPaint)
                        Timber.d("draw line from $index to ${index + 1}")
                    }
                }

//            canvas.drawLine(1f, 1f, 1014f, 825f, mPaint)
//            val resizedBitmap = Bitmap.createScaledBitmap(bm, maxX.toInt(), maxY.toInt(), false)

                binding.fragmentJoggingPathMap.apply {
                    setImageBitmap(bm)
                }
            }

        }
    }

}