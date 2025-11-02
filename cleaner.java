public class MemoryCleaner {
    private static final String TAG = "MemoryCleaner";
    
    /**
     * Membersihkan RAM dengan beberapa teknik berbeda
     */
    public static void cleanRAM(Context context) {
        // 1. Tutup proses latar belakang
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        
        for (ActivityManager.RunningAppProcessInfo process : runningProcesses) {
            // Skip system processes and whitelisted apps
            if (process.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE && 
                !isWhitelisted(process.processName)) {
                android.os.Process.killProcess(process.pid);
            }
        }
        
        // 2. Bersihkan cache memori
        try {
            if (new File("/proc/sys/vm/drop_caches").exists()) {
                Runtime.getRuntime().exec("su -c echo 3 > /proc/sys/vm/drop_caches");
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to clear cache: " + e.getMessage());
        }
        
        // 3. Bersihkan cache aplikasi
        PackageManager pm = context.getPackageManager();
        Method[] methods = pm.getClass().getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().equals("freeStorage")) {
                try {
                    long desiredFreeStorage = Long.MAX_VALUE;
                    m.invoke(pm, desiredFreeStorage, null);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to clear app cache: " + e.getMessage());
                }
                break;
            }
        }
    }
    
    private static boolean isWhitelisted(String processName) {
        String[] whitelist = {
            "android",
            "com.android.systemui",
            "com.google.android.gms",
            context.getPackageName()
        };
        
        for (String whitelisted : whitelist) {
            if (processName.contains(whitelisted)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Mendapatkan informasi memori
     */
    public static String getMemoryInfo(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.getMemoryInfo(mi);
        
        double availableMegs = mi.availMem / 0x100000L;
        double totalMegs = mi.totalMem / 0x100000L;
        double percentUsed = 100 - (mi.availMem * 100.0 / mi.totalMem);
        
        return String.format(Locale.getDefault(), 
            "RAM: %.1fGB/%.1fGB (%.1f%% used)", 
            availableMegs/1024, totalMegs/1024, percentUsed);
    }
}