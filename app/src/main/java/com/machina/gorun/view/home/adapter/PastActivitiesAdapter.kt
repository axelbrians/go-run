package com.machina.gorun.view.home.adapter

import android.icu.text.DecimalFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.machina.gorun.databinding.ItemPastActivitiyBinding
import kotlin.random.Random

class PastActivitiesAdapter : RecyclerView.Adapter<ItemPastActivities>() {

    private val diffCallback = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var dataSet: List<String>
        get() = differ.currentList
        set(value) = differ.submitList(value)



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemPastActivities {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPastActivitiyBinding.inflate(inflater, parent, false)

        return ItemPastActivities(binding)
    }

    override fun onBindViewHolder(holder: ItemPastActivities, position: Int) {
        holder.onBind()
    }

    override fun getItemCount(): Int {
        return 100
    }
}

class ItemPastActivities(
    private val binding: ItemPastActivitiyBinding,
) : RecyclerView.ViewHolder(binding.root) {

    private val df: DecimalFormat = DecimalFormat("#.#")

    fun onBind() {
        val firstRandom = Random.nextFloat()

        val distance = df.format(firstRandom * Random.nextInt(200, 2000))
        val cal = df.format(firstRandom * Random.nextInt(1000, 10000))
        val time = Random.nextInt(1, 120)

        binding.itemDistance.text = "$distance M"
        binding.itemCalories.text = "$cal cal"
        binding.itemTime.text = "$time min"
    }
}