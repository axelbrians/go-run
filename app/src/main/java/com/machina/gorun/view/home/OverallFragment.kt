package com.machina.gorun.view.home

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.maps.android.projection.SphericalMercatorProjection
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.geometry.Point
import com.machina.gorun.core.MyHelper
import com.machina.gorun.data.sources.shared_prefs.UserSharedPrefs
import com.machina.gorun.databinding.FragmentOverallBinding
import com.machina.gorun.view.InsertDialog
import com.machina.gorun.viewmodels.OverallViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import android.graphics.BitmapFactory
import android.util.Base64


@AndroidEntryPoint
class OverallFragment : Fragment() {

    private var _binding: FragmentOverallBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<OverallViewModel>()

    @Inject lateinit var myHelper: MyHelper
    @Inject lateinit var prefs: UserSharedPrefs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOverallBinding.inflate(inflater, container, false)

        setupView()
        subscribeToObservables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.fragmentOverallWeightLinearLayout.setOnClickListener {
            val dialog = InsertDialog("Enter your weight", "Weight (Kg)",
            object : InsertDialog.InsertDialogInterface {
                override fun onInsert(dialogFragment: DialogFragment, data: String) {
                    val weight = data.toDouble()
                    prefs.setUserWeight(weight)
                    dialogFragment.dismiss()
                    revalidateSetting()
                }

            })

            dialog.show(parentFragmentManager, "dialog")
        }

        binding.fragmentOverallHeightLinearLayout.setOnClickListener {
            val dialog = InsertDialog("Enter your height", "Height (Cm)",
                object : InsertDialog.InsertDialogInterface {
                    override fun onInsert(dialogFragment: DialogFragment, data: String) {
                        val height = data.toDouble()
                        prefs.setUserHeight(height)
                        dialogFragment.dismiss()
                        revalidateSetting()
                    }

                })

            dialog.show(parentFragmentManager, "dialog")
        }


        val latLngs = listOf(
            LatLng(-6.9380, 107.6180),
            LatLng(-6.9380, 107.6162),
            LatLng(-6.9369, 107.6160),
            LatLng(-6.9370, 107.6146),
            LatLng(-6.9361, 107.6145),
            LatLng(-6.9369, 107.6137),
        )




        /*
        lifecycleScope.launch {
            var width = 0
            var height = 0
            while (true) {
                if (binding.testSphereical.width > 0) {
                    width = binding.testSphereical.width
                    height = binding.testSphereical.height
                    Timber.d("view width ${binding.testSphereical.width}")
                    Timber.d("view height ${binding.testSphereical.height}")

                    break
                }
                delay(1000L)
            }


            val sphereProj = SphericalMercatorProjection(width.toDouble())

            var canvasPoints = mutableListOf<Point>()

            latLngs.forEach { canvasPoints.add(sphereProj.toPoint(it)) }

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
            Timber.d("maxX $maxX")
            Timber.d("minX $minX")
            Timber.d("maxY $maxY")
            Timber.d("minY $minY")
            Timber.d("xMedian $offsetX")
            Timber.d("yMedian $offsetY")
            Timber.d("scale factor $k")

//            canvas.drawLine(1f, 1f, 1014f, 825f, mPaint)
//            val resizedBitmap = Bitmap.createScaledBitmap(bm, maxX.toInt(), maxY.toInt(), false)

            binding.testSphereical.apply {
                setImageBitmap(bm)
            }

        }
        */
    }


    private fun setupView() {
        revalidateSetting()
    }

    private fun revalidateSetting() {
        val weight = prefs.getUserWeight()
        val height = prefs.getUserHeight()

        binding.fragmentOverallWeight.text = String.format("%.1f Kg", weight)
        binding.fragmentOverallHeight.text = String.format("%.1f Cm", height)
    }

    private fun subscribeToObservables() {
        viewModel.monthActivities.observe(viewLifecycleOwner) {
            with(binding) {
                fragmentOverallDistance.text = it.distance
                fragmentOverallCalories.text = it.calories
                fragmentOverallTimeElapsed.text = it.timeElapsed
            }
        }

        viewModel.getPastMonthJoggingResult()
    }

}