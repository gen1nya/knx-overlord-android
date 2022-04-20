package com.example.masterknx.ui

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.masterknx.R
import com.example.masterknx.databinding.ScreenDevicesBinding
import com.example.masterknx.domain.Page
import com.example.masterknx.domain.SocketConnectionState
import me.dmdev.rxpm.base.PmFragment
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.passTo


class DevicesFragment : PmFragment<DevicesPm>(), DevicesAdapter.Listener {

    companion object {
        const val ARGS_PAGE = "args_page"

        fun newInstance(page: Page) = DevicesFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARGS_PAGE, page)
            }
        }
    }

    private var _binding: ScreenDevicesBinding? = null
    private val binding get() = _binding!!

    val adapter = DevicesAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ScreenDevicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAnalogValueChanged(item: DevicesAdapter.Item) {
        item passTo presentationModel.itemChanged
    }

    override fun onItemClick(item: DevicesAdapter.Item) {
        item passTo presentationModel.itemChanged
    }

    override fun providePresentationModel(): DevicesPm =
        DevicesPm(requireArguments().getSerializable(ARGS_PAGE) as Page)

    override fun onBindPresentationModel(pm: DevicesPm) {
        val layoutManager = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager(requireContext(), 6)
        } else {
            GridLayoutManager(requireContext(), 2)
        }

        layoutManager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int = adapter.getSpanCount(position)
        }
        binding.rvDevices.layoutManager = layoutManager
        binding.rvDevices.adapter = adapter

        pm.content.bindTo(adapter::submitList)
        pm.socketConnectionStatus.bindTo {
            when (it) {
                SocketConnectionState.CONNECTING -> {
                    binding.connectionStatus.setBackgroundColor(requireContext().getColor(R.color.yellow))
                }
                SocketConnectionState.CONNECTED -> {
                    binding.connectionStatus.setBackgroundColor(requireContext().getColor(R.color.green))
                }
                SocketConnectionState.DISCONNECTED -> {
                    binding.connectionStatus.setBackgroundColor(requireContext().getColor(R.color.red))
                }
            }
        }
    }
}