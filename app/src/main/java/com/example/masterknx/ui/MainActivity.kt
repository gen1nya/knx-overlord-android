package com.example.masterknx.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.masterknx.Application
import com.example.masterknx.databinding.ActivityMainBinding
import com.example.masterknx.domain.ApiRepository
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.*
import javax.inject.Inject
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var apiRepository: ApiRepository

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Application.appComponent.inject(this)
        val pagerAdapter = PagerAdapter(supportFragmentManager)
        binding.vpFragments.adapter = pagerAdapter
        binding.tabs.setupWithViewPager(binding.vpFragments)

    }



}