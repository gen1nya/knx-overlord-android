package com.example.masterknx.ui

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.masterknx.R
import com.example.masterknx.databinding.ViewAnalogDeviceBinding
import com.example.masterknx.databinding.ViewDeviceBinding
import com.example.masterknx.domain.DeviceKind
import com.example.masterknx.domain.SmartHomeDevice
import com.example.masterknx.domain.SmartHomeDeviceType

class DeviceDiffCallback(): DiffUtil.ItemCallback<DevicesAdapter.Item>() {
    override fun areItemsTheSame(
        oldItem: DevicesAdapter.Item,
        newItem: DevicesAdapter.Item
    ): Boolean = oldItem.device.type == newItem.device.type

    override fun areContentsTheSame(
        oldItem: DevicesAdapter.Item,
        newItem: DevicesAdapter.Item
    ): Boolean {
        return oldItem.isEnabled == newItem.isEnabled &&
                oldItem.device.type == newItem.device.type &&
                oldItem.device.name == newItem.device.name &&
                oldItem.device.value == newItem.device.value &&
                oldItem.device.id == newItem.device.id

    }

}

class DevicesAdapter(
    private val listener: Listener
) : ListAdapter<DevicesAdapter.Item, DevicesAdapter.ViewHolder>(
    AsyncDifferConfig.Builder(DeviceDiffCallback()).build()
) {

    interface Listener  {
        fun onItemClick(item: Item)
        fun onAnalogValueChanged(item: Item)
    }

    data class Item(
        val device: SmartHomeDevice,
        var isEnabled: Boolean,
    )

    override fun getItemViewType(position: Int): Int {
        return getItem(position).device.type.id
    }

    abstract class ViewHolder(
        view: View
    ): RecyclerView.ViewHolder(view) {
        abstract fun bind(item: Item)
    }

    class SwitchViewHolder(
        private val binding: ViewDeviceBinding,
        private val listener: Listener
    ): ViewHolder(binding.root) {
        override fun bind(item: Item) {
            val context = binding.root.context
            Glide.with(binding.icon)
                .load(when(item.device.kind){
                    DeviceKind.LIGHT -> R.drawable.ic_light_bulb
                    DeviceKind.FAN -> R.drawable.ic_fan
                })
                .into(binding.icon)
            binding.tvId.text = "id: ${item.device.id}"
            binding.tvName.text = item.device.name
            binding.root.setOnClickListener {
                item.isEnabled = !item.isEnabled
                item.device.value = if (item.isEnabled) 1.0 else 0.0;
                listener.onItemClick(item)
                logic(item)
            }
            logic(item)

        }

        private fun logic(item: Item) {
            if (item.isEnabled) {
                binding.enableIndicator.animateAlpha(0F, 1F, startDelay = 200L)
                binding.shadow.visibility = View.VISIBLE
            } else {
                binding.enableIndicator.animateAlpha(1F, 0F)
                binding.shadow.visibility = View.INVISIBLE
            }
        }
    }

    class AnalogWithoutDimmingViewHolder(
        private val binding: ViewDeviceBinding,
        private val listener: Listener
    ): ViewHolder(binding.root) {
        override fun bind(item: Item) {
            val context = binding.root.context

            binding.tvId.text = "id: ${item.device.id}"
            binding.tvName.text = item.device.name
            binding.root.setOnClickListener {
                item.isEnabled = !item.isEnabled
                listener.onItemClick(item)
                item.device.value = if (item.isEnabled) 100.0 else 0.0;
                logic(item)
            }
            logic(item)

        }

        private fun logic(item: Item) {
            if (item.isEnabled) {
                binding.enableIndicator.animateAlpha(0F, 1F, startDelay = 200L)
                binding.shadow.visibility = View.VISIBLE
            } else {
                binding.enableIndicator.animateAlpha(1F, 0F)
                binding.shadow.visibility = View.INVISIBLE
            }
        }
    }

    class AnalogViewHolder(
        private val binding: ViewAnalogDeviceBinding,
        private val listener: Listener
    ): ViewHolder(binding.root) {
        override fun bind(item: Item) {
            val context = binding.root.context
            binding.tvId.text = "id: ${item.device.id}"
            binding.tvName.text = item.device.name
            binding.sbAnalogValue.progress = item.device.value.toInt()
            binding.sbAnalogValue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar, p1: Int, p2: Boolean) {
                    item.isEnabled = p0.progress > 0.0
                    if (item.isEnabled) {
                        binding.shadow.visibility = View.VISIBLE
                    } else {
                        binding.shadow.visibility = View.INVISIBLE
                    }
                }
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar) {
                    item.isEnabled = p0.progress > 0.0
                    item.device.value = p0.progress.toDouble()
                    listener.onAnalogValueChanged(item)
                    Log.d("seekbar value", p0.progress.toString())
                    if (item.isEnabled) {
                        binding.shadow.visibility = View.VISIBLE
                    } else {
                        binding.shadow.visibility = View.INVISIBLE
                    }
                }
            })

            if (item.isEnabled) {
                binding.shadow.visibility = View.VISIBLE
            } else {
                binding.shadow.visibility = View.INVISIBLE
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        when (viewType) {
            SmartHomeDeviceType.ANALOG.id -> {
                AnalogViewHolder(ViewAnalogDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false), listener)
            }
            SmartHomeDeviceType.SWITCH.id -> {
                SwitchViewHolder(ViewDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false), listener)
            }
            SmartHomeDeviceType.ANALOG_WITHOUT_DIMMING.id -> {
                AnalogWithoutDimmingViewHolder(ViewDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false), listener)
            }
            else -> throw IllegalStateException()
        }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getSpanCount(position: Int): Int = when (getItemViewType(position)) {
        SmartHomeDeviceType.ANALOG.id -> 2
        else -> 1
    }
}