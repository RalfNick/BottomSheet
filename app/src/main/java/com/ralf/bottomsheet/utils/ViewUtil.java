package com.ralf.bottomsheet.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.annotation.UiThread;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Field;
import java.util.LinkedList;

public class ViewUtil {

    @UiThread
    public static void setViewsVisibility(int visibility, View... views) {
        if (views == null || views.length == 0) {
            return;
        }
        for (View view : views) {
            if (view != null) {
                view.setVisibility(visibility);
            }
        }
    }

    /**
     * 设置TextView 附带icon ( drawable Left|Right|Top|Bottom ) 的大小
     *
     * @param textView 需设置icon大小的TextView
     * @param width    icon的宽度
     * @param height   icon的高度
     */
    @UiThread
    public static void setTextViewIconSize(@NonNull TextView textView, int width, int height) {
        Drawable[] drawables = textView.getCompoundDrawables();
        for (Drawable drawable : drawables) {
            if (drawable != null) {
                drawable.setBounds(0, 0, width, height);
            }
        }
        textView.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
    }

    public static boolean isFullScreen(Activity activity) {
        return isFullScreen(activity.getWindow());
    }

    public static boolean isFullScreen(Window window) {
        int flag = window.getAttributes().flags;
        if ((flag
                & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
            return true;
        }

        return false;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @UiThread
    public static void showSoftInput(final Context context, final View view, int delay) {
        if (delay > 0) {
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ViewUtil.showSoftInput(context, view, 0);
                }
            }, delay);
        } else {
            try {
                InputMethodManager imm =
                        (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @UiThread
    public static void showSoftInput(final Context context, final View view, boolean delay) {
        showSoftInput(context, view, delay ? 500 : 0);
    }

    @UiThread
    public static void hideSoftInput(Activity context) {
        try {
            View focus = context.getCurrentFocus();
            if (focus != null) {
                ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(
                                focus.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @UiThread
    public static void hideSoftInput(Window window) {
        try {
            View focus = window.getCurrentFocus();
            if (focus != null) {
                ((InputMethodManager) window.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @UiThread
    public static void hideSoftInput(Context context, IBinder windowToken) {
        try {
            if (windowToken != null) {
                ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(
                                windowToken, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int mStatusBarHeight;

    public static int getStatusBarHeight(@NonNull Context context) {
        if (mStatusBarHeight > 0) {
            return mStatusBarHeight;
        }

        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            mStatusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        } else {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object obj = c.newInstance();
                Field field = c.getField("status_bar_height");
                field.setAccessible(true);
                int x = Integer.parseInt(field.get(obj).toString());
                mStatusBarHeight = context.getResources().getDimensionPixelSize(x);
            } catch (Throwable e1) {
                e1.printStackTrace();
            }
        }

        if (mStatusBarHeight <= 0) {
            mStatusBarHeight = dip2px(context, 25);
        }

        return mStatusBarHeight;
    }

    /**
     * 优先使用 getDisplayWidth/getDisplayHeight
     */
    @Deprecated
    public static int getScreenHeight(@NonNull Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    /**
     * 优先使用 getDisplayWidth/getDisplayHeight
     */
    @Deprecated
    public static int getScreenWidth(@NonNull Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    /**
     * 优先使用 getDisplayWidth/getDisplayHeight
     */
    @Deprecated
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    /**
     * 优先使用 getDisplayWidth/getDisplayHeight
     */
    @Deprecated
    public static int getScreenRealHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealMetrics(displayMetrics);
        } else {
            wm.getDefaultDisplay().getMetrics(displayMetrics);
        }
        return displayMetrics.heightPixels;
    }

    /**
     * 优先使用 getDisplayWidth/getDisplayHeight
     */
    @Deprecated
    // TODO (yangkai) 想办法缓存屏幕宽高，因为 UI 绘制时频繁调用
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    @UiThread
    public static View getContentView(@NonNull Activity activity) {
        return getContentView(activity.getWindow());
    }

    @UiThread
    public static View getContentView(@NonNull Window window) {
        return window.getDecorView().findViewById(android.R.id.content);
    }

    /**
     * 获取Activity Content区域宽度.
     * note：在刚触发onConfigurationChanged事件时调用，会获取到配置变更之前的高宽
     */
    @UiThread
    public static int getDisplayWidth(@NonNull Activity activity) {
        View view = getContentView(activity);
        return view == null ? 0 : view.getWidth();
    }

    /**
     * 获取Activity Content区域宽度. 修正魅族pro 6s上可能出现的屏幕宽高颠倒的问题。
     * 方法是根据landscape模式比较宽高的大小
     * note： 使用者请慎重
     *
     * @param activity
     * @return
     */
    @UiThread
    public static int getFixedDisplayWidth(@NonNull Activity activity) {
        boolean isLandscape = ViewUtil.isLandscape(activity);
        int screenWidth = ViewUtil.getDisplayWidth(activity);
        int screenHeight = ViewUtil.getDisplayHeight(activity);
        if (screenWidth == 0) {
            screenWidth = getScreenWidthPx(activity);
        }
        if (screenHeight == 0) {
            screenHeight = getScreenHeightPx(activity);
        }
        if ((isLandscape && (screenWidth < screenHeight))
                || (!isLandscape && (screenWidth > screenHeight))) {
            screenWidth = screenHeight;
        }
        return screenWidth;
    }

    /**
     * 获取Activity Content区域高度.
     * note：在刚触发onConfigurationChanged事件时调用，会获取到配置变更之前的高宽
     */
    @UiThread
    public static int getDisplayHeight(@NonNull Activity activity) {
        View view = getContentView(activity);
        return view == null ? 0 : view.getHeight();
    }

    /**
     * 优先使用 getDisplayWidth/getDisplayHeight
     */
    @Deprecated
    public static int getScreenHeightPx(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        defaultDisplay.getMetrics(metrics);
        return metrics.heightPixels;
    }

    /**
     * 优先使用 getDisplayWidth/getDisplayHeight
     */
    public static int getScreenWidthPx(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        defaultDisplay.getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static float getDensity(@NonNull Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static int getMaxWidth(TextView view) {
        int maxWidth = 0;
        try {
            Field maxWidthField = TextView.class.getDeclaredField("mMaxWidth");
            maxWidthField.setAccessible(true);
            maxWidth = (int) maxWidthField.get(view);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return maxWidth;
    }

    private static boolean isNavigationBarOnRight(Point appUsableSize, Point realScreenSize) {
        // navigation bar on the right
        return appUsableSize.x < realScreenSize.x;
    }

    private static boolean isNavigationBarOnBottom(Point appUsableSize, Point realScreenSize) {
        // navigation bar on the bottom
        return appUsableSize.y < realScreenSize.y;
    }

    public static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    /**
     * 创建 View
     *
     * @param parent
     * @param resId
     * @param <T>    需要转换成的 View 的类型
     * @return
     */
    @UiThread
    public static <T extends View> T inflate(ViewGroup parent, int resId) {
        View view = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
        return (T) view;
    }

    /**
     * 创建 View
     *
     * @param context context
     * @param resId   resource id
     * @return view
     */
    @UiThread
    public static <T extends View> T inflate(Context context, int resId) {
        return (T) LayoutInflater.from(context).inflate(resId, null);
    }

    /**
     * 判断列表是否已经到底.
     */
    @UiThread
    public static boolean isLastItemAtListEnd(RecyclerView list, boolean isVertical) {
        if (list.getChildCount() == 0) {
            return true;
        }
        View lastChildInList = list.getChildAt(list.getChildCount() - 1);
        boolean isViewAtEnd = isVertical
                ? lastChildInList.getBottom() <= list.getHeight() - list.getPaddingBottom()
                : lastChildInList.getRight() <= list.getWidth() - list.getPaddingRight();
        return isViewAtEnd
                && list.getChildAdapterPosition(lastChildInList) == list.getAdapter().getItemCount() - 1;
    }

    @UiThread
    @Size(2)
    public static int[] getLocationOnScreen(@NonNull View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location;
    }

    @UiThread
    @Size(2)
    public static int[] getLocationInWindow(@NonNull View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        return location;
    }

    /**
     * 基于window的坐标返回控件的Rect.
     */
    @UiThread
    public static Rect getViewRawRect(View view, boolean forTouch) {
        int[] location = new int[2];
        if (forTouch) {
            view.getLocationOnScreen(location);
        } else {
            view.getLocationInWindow(location);
        }

        return new Rect(location[0], location[1], location[0] + view.getWidth(),
                location[1] + view.getHeight());
    }

    @UiThread
    public static Rect getViewRectOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Rect(location[0], location[1], location[0] + view.getWidth(),
                location[1] + view.getHeight());
    }

    /**
     * 两个Rect之间是否存在交集
     */
    public static boolean isRectCross(Rect rect1, Rect rect2) {
        return rect1.contains(rect2.left, rect2.top)
                || rect1.contains(rect2.right, rect2.top)
                || rect1.contains(rect2.left, rect2.bottom)
                || rect1.contains(rect2.right, rect2.bottom)
                || rect2.contains(rect1.left, rect1.top)
                || rect2.contains(rect1.right, rect1.top)
                || rect2.contains(rect1.left, rect1.bottom)
                || rect2.contains(rect1.right, rect1.bottom);
    }

    // 不要高频调用，这个背后是IPC通信，用KwaiApp.isLandscape()取代
    public static boolean isLandscape(Activity activity) {
        if (activity != null) {
            int requestedOrientation = activity.getRequestedOrientation();
            return requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        }
        return false;
    }

    /**
     * Returns the rotation of the screen from its "natural" orientation.
     * The returned value may be {@link Surface#ROTATION_0 Surface.ROTATION_0}
     * (no rotation), {@link Surface#ROTATION_90 Surface.ROTATION_90},
     * {@link Surface#ROTATION_180 Surface.ROTATION_180}, or
     * {@link Surface#ROTATION_270 Surface.ROTATION_270}. For
     * example, if a device has a naturally tall screen, and the user has
     * turned it on its side to go into a landscape orientation, the value
     * returned here may be either {@link Surface#ROTATION_90 Surface.ROTATION_90}
     * or {@link Surface#ROTATION_270 Surface.ROTATION_270} depending on
     * the direction it was turned. The angle is the rotation of the drawn
     * graphics on the screen, which is the opposite direction of the physical
     * rotation of the device. For example, if the device is rotated 90
     * degrees counter-clockwise, to compensate rendering will be rotated by
     * 90 degrees clockwise and thus the returned value here will be
     * {@link Surface#ROTATION_90 Surface.ROTATION_90}.
     */
    public static int getDisplayRotation(Activity activity) {
        if (activity != null) {
            WindowManager windowManager =
                    (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            return windowManager.getDefaultDisplay().getRotation();
        }
        return 0;
    }

    public static boolean isVerticalFlipped(Activity activity) {
        return activity.getWindowManager().getDefaultDisplay()
                .getRotation() == Surface.ROTATION_180;
    }

    public static boolean isTouchEventOutOfBounds(View view, MotionEvent event) {
        Rect rect = new Rect();
        int[] location = getLocationOnScreen(view);
        rect.set(location[0], location[1], location[0] + view.getWidth(),
                location[1] + view.getHeight());
        return !rect.contains((int) event.getRawX(), (int) event.getRawY());
    }

    public static Activity getActivity(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    /**
     * 测量文字的宽度
     */
    public static float measureTextWidth(String text, float textSize) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        return paint.measureText(text);
    }

    @UiThread
    public static void appendStatusBarHeight(View view) {
        if (view != null && view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).topMargin +=
                    getStatusBarHeight(view.getContext());
        }
    }

    @UiThread
    public static void setStatusBarHeight(View view) {
        if (view != null && view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).topMargin =
                    getStatusBarHeight(view.getContext());
        }
    }

    @UiThread
    public static void removeOnGlobalLayoutListener(View view, OnGlobalLayoutListener listener) {
        view.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
    }

    @SuppressWarnings("unchecked")
    @UiThread
    public static <T extends View> T select(View view, int id) {
        return (T) view.findViewById(id);
    }

    @UiThread
    public static void startAlphaAnim(View view, float startAlpha, float endAlpha, long duration) {
        view.setVisibility(View.VISIBLE);
        AlphaAnimation alphaAnimation = new AlphaAnimation(startAlpha, endAlpha);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setDuration(duration);
        view.startAnimation(alphaAnimation);
    }

    @UiThread
    public static void setPaddingTop(View view, int paddingInDp) {
        view.setPadding(view.getPaddingLeft(), dip2px(view.getContext(), paddingInDp),
                view.getPaddingRight(), view.getPaddingBottom());
    }

    @UiThread
    public static void setPaddingLeft(View view, int paddingInDp) {
        view.setPadding(dip2px(view.getContext(), paddingInDp), view.getPaddingTop(),
                view.getPaddingRight(), view.getPaddingBottom());
    }

    @UiThread
    public static void setPaddingRight(View view, int paddingInDp) {
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(),
                dip2px(view.getContext(), paddingInDp), view.getPaddingBottom());
    }

    @UiThread
    public static void setPaddingBottom(View view, int paddingInDp) {
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(),
                dip2px(view.getContext(), paddingInDp));
    }

    /**
     * 设置View本身和所有的子是否可以点击
     *
     * @param view
     * @param enabled true表示View和所有子的View可以被点击，false表示
     *                View和所有子View的不可以被点击
     */
    @UiThread
    public static void setEnabled(View view, boolean enabled) {
        if (view == null || view.isEnabled() == enabled) {
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            LinkedList<ViewGroup> queue = new LinkedList<>();
            queue.add(viewGroup);
            while (!queue.isEmpty()) {
                ViewGroup current = queue.removeFirst();
                current.setEnabled(enabled);
                for (int i = 0; i < current.getChildCount(); i++) {
                    if (current.getChildAt(i) instanceof ViewGroup) {
                        queue.addLast((ViewGroup) current.getChildAt(i));
                    } else {
                        current.getChildAt(i).setEnabled(enabled);
                    }
                }
            }
        } else {
            view.setEnabled(enabled);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @UiThread
    public static boolean isStatusBarTranslucent(Activity activity) {
        Window windoww = activity.getWindow();
        return windoww.getStatusBarColor() == Color.TRANSPARENT;
    }

    // 获取底部虚拟导航栏的高度
    public static int getNavigationBarHeight(Context context) {
        if (context == null || !deviceHasNavigationBar(context)) {
            return 0;
        }
        int resourceId = context.getResources().getIdentifier("navigation_bar_height",
                "dimen", "android");
        // 获取NavigationBar的高度
        return context.getResources().getDimensionPixelSize(resourceId);
    }

    // 判断底部是否有虚拟导航栏
    public static boolean deviceHasNavigationBar(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                Point realSize = new Point();
                display.getSize(size);
                display.getRealSize(realSize);
                return realSize.y != size.y;
            } else {
                boolean menu = ViewConfiguration.get(context).hasPermanentMenuKey();
                boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
                if (menu || back) {
                    return false;
                } else {
                    return true;
                }
            }
        } catch (Throwable ignore) {
            return false;
        }
    }

}
