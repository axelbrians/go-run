package com.machina.gorun.view.home.adapter

import android.icu.text.DecimalFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.machina.gorun.data.models.JoggingResult
import com.machina.gorun.databinding.ItemPastActivitiyBinding
import kotlin.random.Random

class PastActivitiesAdapter : RecyclerView.Adapter<ItemPastActivities>() {

    private val diffCallback = object : DiffUtil.ItemCallback<JoggingResult>() {
        override fun areItemsTheSame(oldItem: JoggingResult, newItem: JoggingResult): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: JoggingResult, newItem: JoggingResult): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.distanceTraveled == newItem.distanceTraveled &&
                    oldItem.caloriesBurned == newItem.caloriesBurned &&
                    oldItem.timeElapsed == newItem.timeElapsed &&
                    oldItem.timeStamp == newItem.timeStamp
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var dataSet: List<JoggingResult>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    private var onItemClick: ((Int) -> Unit)? = null

    fun setOnItemClick(listener: (Int) -> Unit) {
        onItemClick = listener
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemPastActivities {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPastActivitiyBinding.inflate(inflater, parent, false)

        return ItemPastActivities(binding)
    }

    override fun onBindViewHolder(holder: ItemPastActivities, position: Int) {
        holder.onBind(dataSet[position], onItemClick)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}

class ItemPastActivities(
    private val binding: ItemPastActivitiyBinding,
) : RecyclerView.ViewHolder(binding.root) {


    fun onBind(data: JoggingResult, onItemClick: ((Int) -> Unit)?) {
        binding.itemDistance.text = data.distanceTraveled
        binding.itemCalories.text = data.caloriesBurned
        binding.itemTime.text = data.timeElapsed
        binding.dateHistory.text = data.timeStamp

        binding.root.setOnClickListener { _ ->
            onItemClick?.let { it(data.id) }
        }
    }
}