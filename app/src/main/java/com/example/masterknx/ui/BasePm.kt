package com.example.masterknx.ui

import me.dmdev.rxpm.Command
import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.navigation.NavigationMessage
import me.dmdev.rxpm.navigation.NavigationalPm

open class BasePm: PresentationModel(), NavigationalPm {

    override val navigationMessages: Command<NavigationMessage> = command()

    val actionBack = action<Unit>{
        doOnNext {

        }
    }
}