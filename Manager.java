public class TurboModeManager {
    private static final String CPU_GOVERNOR_PATH = "/sys/devices/system/cpu/cpu%d/cpufreq/scaling_governor";
    private static final String GPU_BOOST_PATH = "/sys/class/kgsl/kgsl-3d0/devfreq/governor";
    
    public static void enableTurboMode(Context context) {
        // 1. Optimasi CPU
        setCPUGovernor("performance");
        
        // 2. Optimasi GPU
        setGPUMode("performance");
        
        // 3. Nonaktifkan animasi sistem
        Settings.Global.putInt(
            context.getContentResolver(),
            Settings.Global.WINDOW_ANIMATION_SCALE, 0);
        Settings.Global.putInt(
            context.getContentResolver(),
            Settings.Global.TRANSITION_ANIMATION_SCALE, 0);
        Settings.Global.putInt(
            context.getContentResolver(),
            Settings.Global.ANIMATOR_DURATION_SCALE, 0);
        
        // 4. Tingkatkan prioritas thread
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
    }
    
    private static void setCPUGovernor(String governor) {
        int cpuCores = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < cpuCores; i++) {
            String path = String.format(Locale.getDefault(), CPU_GOVERNOR_PATH, i);
            if (new File(path).exists()) {
                try {
                    Runtime.getRuntime().exec("su -c echo " + governor + " > " + path);
                } catch (IOException e) {
                    Log.e("TurboMode", "Failed to set CPU governor: " + e.getMessage());
                }
            }
        }
    }
    
    private static void setGPUMode(String mode) {
        if (new File(GPU_BOOST_PATH).exists()) {
            try {
                Runtime.getRuntime().exec("su -c echo " + mode + " > " + GPU_BOOST_PATH);
            } catch (IOException e) {
                Log.e("TurboMode", "Failed to set GPU mode: " + e.getMessage());
            }
        }
    }
    
    public static void disableTurboMode(Context context) {
        // Kembalikan ke pengaturan normal
        setCPUGovernor("ondemand");
        setGPUMode("msm-adreno-tz");
        
        // Aktifkan kembali animasi sistem
        Settings.Global.putInt(
            context.getContentResolver(),
            Settings.Global.WINDOW_ANIMATION_SCALE, 1);
        Settings.Global.putInt(
            context.getContentResolver(),
            Settings.Global.TRANSITION_ANIMATION_SCALE, 1);
        Settings.Global.putInt(
            context.getContentResolver(),
            Settings.Global.ANIMATOR_DURATION_SCALE, 1);
    }
}