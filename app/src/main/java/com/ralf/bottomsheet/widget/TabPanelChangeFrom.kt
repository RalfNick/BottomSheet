package com.ralf.bottomsheet.widget

annotation class TabPanelChangeFrom {
    companion object {
        var TYPE_UNKNOWN = -1 //有些事件其他业务无需知道暂用unknown代替
        var TYPE_SHOW_ANIMATION = 0
        var TYPE_HIDE_ANIMATION = 1
        var TYPE_HIDE_ANIMATION_BY_DRAG = 2
    }
}