package com.ralf.bottomsheet.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AppImmersiveUtils {

    /**
     * 由于标题栏基本是白色的，所以状态栏的字体和图标需要改成黑色，这个只有23以上才支持
     */
    public static boolean canStartImmersiveMode() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * 需要剔除全屏模式，目前看这种方法判断没啥问题
     */
    public static boolean isImmersiveMode(@NonNull Activity activity) {
        if (canStartImmersiveMode()) {
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            View decorView = getWindowDecorView(activity.getWindow());
            return decorView != null &&
                    (decorView.getSystemUiVisibility() & option) == option
                    && !ViewUtil.isFullScreen(activity);
        }
        return false;
    }

    /**
     * @param activity
     * @param color    状态栏背景色
     * @param dark     状态栏字体图标是否采用黑色
     */
    public static void startImmersiveMode(@NonNull Activity activity, int color, boolean dark) {
        startImmersiveMode(activity, color, dark, true);
    }

    /**
     * 此方法适用于背景全屏展示的页面
     *
     * @param activity
     * @param view     需要偏移的view
     * @param dark     状态栏字体图标是否采用黑色
     */
    public static void startImmersiveMode(@NonNull Activity activity, @NonNull View view,
                                          boolean dark) {
        if (canStartImmersiveMode()) {
            startImmersiveMode(activity, Color.TRANSPARENT, dark, true);
            view.setTranslationY(ViewUtil.getStatusBarHeight(activity));
        }
    }

    public static void adjustViewPaddingTop(@NonNull Context context, @NonNull View view) {
        view.setPadding(view.getPaddingLeft(), ViewUtil.getStatusBarHeight(context),
                view.getPaddingRight(), view.getPaddingBottom());
    }

    /**
     * @param activity
     * @param color               状态栏背景色
     * @param dark                状态栏字体图标是否采用黑色
     * @param customImmersiveMode 是否自定义沉浸式，否表示Content View会设置一个高度为状态栏高度的padding，
     *                            这是默认实现方案
     */
    public static void startImmersiveMode(@NonNull Activity activity, int color, boolean dark,
                                          boolean customImmersiveMode) {
        if (!canStartImmersiveMode()) {
            return;
        }
        adjustStatusBar(activity, color, dark);
        if (!customImmersiveMode) {
            View view = activity.findViewById(android.R.id.content);
            if (view != null) {
                view.setPadding(0, ViewUtil.getStatusBarHeight(activity), 0, 0);
            }
        }
    }

    /**
     * 真正实现沉浸式的代码
     *
     * @param activity
     * @param color
     * @param dark
     */
    public static void adjustStatusBar(@NonNull Activity activity, int color, boolean dark) {
        Window window = activity.getWindow();
        View decorView = getWindowDecorView(window);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                if (dark) {
                    option |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    if (RomUtils.isMiui()) {
                        setMIUILightStatusBar(activity, true);
                    } else if (RomUtils.isFlyme()) {
                        MeiZuStatusBarColorUtils.setStatusBarDarkIcon(activity, true);
                    }
                }
            }
            if (decorView != null) {
                decorView.setSystemUiVisibility(option);
            }
            window.setStatusBarColor(color);
            // forcedNavigationBarColor
            window.setNavigationBarColor(window.getNavigationBarColor());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            if (decorView != null) {
                decorView.setSystemUiVisibility(option);
            }
        }
    }

    public static void setStatusBarColor(Activity activity, @ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = getWindowDecorView(window);
                if (decorView != null) {
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (RomUtils.isMiui()) {
                setMIUILightStatusBar(activity, true);
            } else if (RomUtils.isFlyme()) {
                MeiZuStatusBarColorUtils.setStatusBarDarkIcon(activity, true);
            }
            window.setStatusBarColor(color);
        }
    }

    /**
     * MIUI实现沉浸式方法
     *
     * @param activity
     * @param dark
     * @return
     */
    public static boolean setMIUILightStatusBar(@NonNull Activity activity, boolean dark) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), dark ? darkModeFlag : 0, darkModeFlag);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 查看源码在某些版本通过 window#findViewById 如果找不到 view 会报异常，这里 findViewById 失败的原因未知，
     * 可能是 ROM id 修改了? 失败再通过 content 向上重新查找一次并做判空处理
     */
    @Nullable
    private static View getWindowDecorView(Window window) {
        View decorView = null;
        try {
            decorView = window.getDecorView();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (decorView != null) {
            return decorView;
        }
        decorView = window.findViewById(android.R.id.content);
        while (decorView != null && decorView.getParent() instanceof View) {
            decorView = (View) decorView.getParent();
        }
        return decorView;
    }

    public static void setNavigationBarColor(@Nullable Window window, int color) {
        if (window == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(color);
        }
        // 发现在三星和Nexus手机上，白色背景时NavigationBar上的三个按键看不清楚
        // 这里判断下当前背景是亮色还是暗色，然后修改Bar上面三个按键的颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            View decorView = window.getDecorView();
            int flags = decorView.getSystemUiVisibility();
            if (isColorLight(color)) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            decorView.setSystemUiVisibility(flags);
        }
    }

    /**
     * 设置导航栏，透明或不透明
     */
    public static void setNavigationBarColor(Window window, boolean shouldTransparent,
                                             int navBarColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (shouldTransparent) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            } else {
                setNavigationBarColor(window, navBarColor);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
        }
    }

    private static boolean isColorLight(int color) {
        double darkness = 1 -
                (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness < 0.5;
    }
}
