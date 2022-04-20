package com.example.masterknx.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.masterknx.domain.Page


class PagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    companion object {
        private const val NUM_ITEMS = 2
    }

    override fun getCount(): Int = NUM_ITEMS

    private val fragments = HashMap<Int, DevicesFragment>()

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> {
            if (fragments[0] == null) {
                fragments[0] = DevicesFragment.newInstance(Page.MASTER_BEDROOM)
            }
            fragments[0]!!
        }
        1 -> {
            if (fragments[1] == null) {
                fragments[1] = DevicesFragment.newInstance(Page.LOUNGE)
            }
            fragments[1]!!
        }
        else -> throw IllegalStateException()
    }

    override fun getPageTitle(position: Int): CharSequence =
        when (position) {
            0 -> Page.MASTER_BEDROOM.toString()
            1 -> Page.LOUNGE.toString()
            else -> throw IllegalStateException()
        }

}