package com.ralf.bottomsheet.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class RomUtils {

    private static final String ROM_MIUI = "MIUI";
    /**
     * 备注：需要兼容原始用法及对未来荣耀手机的兼容，方案如下：
     * 1. isEmui() Deprecated 维持原有逻辑不变
     * 2. isEmotion() 新增接口，华为手机返回true，荣耀手机返回false
     * 3. isMagic() 新增接口，荣耀手机返回true，华为手机返回false
     * 4. getName() Deprecated 维持原有逻辑不变
     * 5. getName2() 新增接口, 华为-EMOTION / 荣耀-MAGIC
     **/
    private static final String ROM_EMUI = "EMUI";
    private static final String ROM_EMOTION = "EMOTION";
    private static final String ROM_MAGIC = "MAGIC";
    private static final String ROM_FLYME = "FLYME";
    private static final String ROM_OPPO = "OPPO";
    private static final String ROM_SMARTISAN = "SMARTISAN";
    private static final String ROM_VIVO = "VIVO";
    private static final String ROM_QIKU = "QIKU";
    private static final String ROM_360 = "360";
    private static final String ROM_ONEPLUS = "OnePlus";
    private static final String ROM_SAMSUNG = "SAMSUNG";

    private static final String KEY_VERSION_MIUI = "ro.miui.ui.version.name";
    private static final String KEY_VERSION_EMUI = "ro.build.version.emui";
    //旧版ColorOs特有的rom key
    private static final String KEY_VERSION_OPPO = "ro.build.version.opporom";
    private static final String KEY_VERSION_SMARTISAN = "ro.smartisan.version";
    private static final String KEY_VERSION_VIVO = "ro.vivo.os.version";
    private static final String KEY_SYSTEM_MANUFACTURER = "ro.product.system.manufacturer";
    private static final String KEY_MANUFACTURER = "ro.product.manufacturer";
    //荣耀MagicUI 6.0以上新增的key
    private static final String KEY_VERSION_MAGIC = "ro.build.version.magic";
    //新版ColorOs特有的rom key
    private static final String KEY_VERSION_COLOR = "ro.build.version.oplusrom";

    private static final String PATTERN_EMOTIONUI = "EmotionUI";
    private static final String PATTERN_MAGICUI = "MagicUI";

    // 很多业务在主线程中直接调用 IO 相关操作，在Utils中增加cache，减少出现问题的几率;
    private static final ConcurrentHashMap<String, String> sProperties = new ConcurrentHashMap<>();

    /**
     * sName 超级麻烦的状态变量，不好做UnitTest。维持原有逻辑，在 sName == "EMUI" 的条件下，需要再次取值；
     * 这个函数不应该是一个高频函数，不建议做缓存
     **/
    private static String sName;
    private static String sVersion;
    private static String sManufacturer;

    @Deprecated
    public static boolean isEmui() {
        if (check(ROM_EMUI)) {
            return true;
        } // else
        return isEmotion() || isMagic();
    }

    public static boolean isEmotion() {
        return check(ROM_EMOTION);
    }

    public static boolean isMagic() {
        return check(ROM_MAGIC);
    }


    public static boolean isMiui() {
        return check(ROM_MIUI);
    }

    public static boolean isVivo() {
        return check(ROM_VIVO);
    }

    public static boolean isOppo() {
        return check(ROM_OPPO);
    }

    public static boolean isOnePlus() {
        return check(ROM_ONEPLUS);
    }

    public static boolean isFlyme() {
        return check(ROM_FLYME);
    }

    public static boolean is360() {
        return check(ROM_QIKU) || check(ROM_360);
    }

    public static boolean isSmartisan() {
        return check(ROM_SMARTISAN);
    }

    public static boolean isSamsung() {
        return check(ROM_SAMSUNG);
    }

    @Deprecated
    public static String getName() {
        if (sName == null) {
            setRomInfo();
        }
        return sName;
    }

    public static void clearCache() {
        sName = null;
        sProperties.clear();
    }

    public static String getName2() {
        if (sName == null) {
            setRomInfo();
        }
        //增加一个Magic UI 6.0之后的属性
        return getRealName(getCacheableProp(KEY_VERSION_EMUI), getCacheableProp(KEY_VERSION_MAGIC));
    }

    @NonNull
    private static String getRealName(@NonNull String buildVersionEmui, String buildVersionMagic) {
        if (sName == null) {
            return "";
        }

        // 老的华为/荣耀机型
        if (!sName.equals(ROM_EMUI)) {
            return sName;
        }
        // 2021后确认去HUAWEI的荣耀机型
        if (buildVersionEmui.contains(PATTERN_MAGICUI)
                // Magic UI 6.0之后
                || buildVersionMagic.contains(PATTERN_MAGICUI)) {
            return ROM_MAGIC;
        }
        // 2020 年的荣耀机型，如V30
        if (buildVersionEmui.contains(PATTERN_EMOTIONUI)) {
            String brand = getCacheableProp("ro.product.brand").toLowerCase();
            if (brand.equals("honor")) {
                return ROM_MAGIC;
            }
            // else 都认为是华为的
            return ROM_EMOTION;
        }

        return sName;
    }

    public static String getVersion() {
        if (sVersion == null) {
            setRomInfo();
        }
        return sVersion;
    }

    private static void setRomInfo() {
        if (!TextUtils.isEmpty(sVersion = getCacheableProp(KEY_VERSION_OPPO))) {
            sName = ROM_OPPO;
        } else if (!TextUtils.isEmpty(sVersion = getCacheableProp(KEY_VERSION_VIVO))) {
            sName = ROM_VIVO;
        } else if (!TextUtils.isEmpty(sVersion = getCacheableProp(KEY_VERSION_EMUI))) {
            sName = ROM_EMUI;
        } else if (!TextUtils.isEmpty(sVersion = getCacheableProp(KEY_VERSION_MAGIC))) {
            sName = ROM_MAGIC;
        } else if (!TextUtils.isEmpty(sVersion = getCacheableProp(KEY_VERSION_MIUI))) {
            sName = ROM_MIUI;
        } else if (!TextUtils.isEmpty(sVersion = getCacheableProp(KEY_SYSTEM_MANUFACTURER))
                && sVersion.equalsIgnoreCase("meizu")) {
            // 备注：这里使用"ro.product.system.manufacturer"通用字段作为 ONEPLUS 设备的判断是存在隐患的；
            // 但为了防止之前的一些特殊场景，暂时对有异常的 case 按最小改动的方案执行；
            sName = ROM_FLYME;
        } else if (!TextUtils.isEmpty(sVersion = getCacheableProp(KEY_VERSION_SMARTISAN))) {
            sName = ROM_SMARTISAN;
        } else if (!TextUtils.isEmpty(sManufacturer = getCacheableProp(KEY_MANUFACTURER))) {
            if (sManufacturer.equalsIgnoreCase(ROM_OPPO)) {
                sName = ROM_OPPO;
                sVersion = getCacheableProp(KEY_VERSION_COLOR);
            } else if (sManufacturer.equalsIgnoreCase(ROM_SAMSUNG)) {
                sName = ROM_SAMSUNG;
            } else if (sManufacturer.equalsIgnoreCase(ROM_ONEPLUS)) {
                sVersion = getCacheableProp(KEY_VERSION_COLOR);
                sName = ROM_ONEPLUS;
            } else {
                setDefaultValues();
            }
        } else {
            setDefaultValues();
        }
    }

    private static void setDefaultValues() {
        sVersion = Build.DISPLAY;
        if (sVersion.toUpperCase().contains(ROM_FLYME)) {
            sName = ROM_FLYME;
        } else {
            sVersion = Build.UNKNOWN;
            sName = Build.MANUFACTURER.toUpperCase();
        }
    }

    // 备注 rom 不可以为 null
    public static boolean check(@NonNull String rom) {
        if (sName == null) {
            setRomInfo();
        }
        // check rom name
        if (rom.equals(ROM_EMOTION) || rom.equals(ROM_MAGIC)) {
            String prop = getCacheableProp(KEY_VERSION_EMUI);
            //新荣耀换了system property
            String prop2 = getCacheableProp(KEY_VERSION_MAGIC);
            String realName = getRealName(prop, prop2);
            return realName.equals(rom);
        } // else

        return sName.equals(rom);
    }

    @SuppressLint("PrivateApi")
    @NonNull
    private static String getCacheableProp(@NonNull String key) {
        String prop = sProperties.get(key);
        if (prop == null) {
            prop = SystemProperties.get(key);
            prop = prop != null ? prop : "";
            sProperties.put(key, prop);
        }
        return prop;
    }

    @Deprecated
    @SuppressLint("PrivateApi")
    @NonNull
    public static String getProp(@NonNull String key) {
        String prop = SystemProperties.get(key);
        prop = prop != null ? prop : "";
        return prop;
    }

    /**
     * 备注：与系统接口单独拆出静态类方便 Mock
     **/
    @SuppressLint("PrivateApi")
    static class SystemProperties {
        public static String get(String key) {
            String properties = null;
            Class<?> clazz;
            try {
                clazz = Class.forName("android.os.SystemProperties");
                Method method = clazz.getDeclaredMethod("get", String.class);
                properties = (String) method.invoke(clazz, key);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            if (TextUtils.isEmpty(properties)) {
                properties = Processes.readFirstLine("getprop " + key);
            }
            return properties;
        }
    }

    public static boolean isMeitu() {
        try {
            return Build.MANUFACTURER.toUpperCase().contains("MEITU");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean isM5() {
        try {
            return Build.MODEL.toUpperCase().contains("M5");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isColorOs() {
        if (isOppo()) {
            return true;
        }
        if (isOnePlus()) {
            sVersion = getCacheableProp(KEY_VERSION_COLOR);
            return !TextUtils.isEmpty(sVersion);
        }
        return false;
    }
}
