public class GameDetectionService extends Service {
    private UsageStatsManager usageStatsManager;
    private Handler handler;
    private static final long CHECK_INTERVAL = 5000; // 5 detik
    
    @Override
    public void onCreate() {
        super.onCreate();
        usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        handler = new Handler();
        
        if (hasUsageStatsPermission()) {
            startMonitoring();
        } else {
            requestUsageStatsPermission();
        }
    }
    
    private void startMonitoring() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentApp = getForegroundApp();
                if (isGame(currentApp)) {
                    TurboModeManager.enableTurboMode(GameDetectionService.this);
                }
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        }, CHECK_INTERVAL);
    }
    
    private String getForegroundApp() {
        long endTime = System.currentTimeMillis();
        long beginTime = endTime - 10000; // 10 detik yang lalu
        
        UsageEvents.Event event = new UsageEvents.Event();
        UsageEvents usageEvents = usageStatsManager.queryEvents(beginTime, endTime);
        
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                return event.getPackageName();
            }
        }
        return "";
    }
    
    private boolean isGame(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            
            // Cek kategori aplikasi
            if ((appInfo.flags & ApplicationInfo.FLAG_IS_GAME) != 0) {
                return true;
            }
            
            // Cek kategori di Play Store
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (pm.getInstallReason(packageName, 
                    new android.os.UserHandle(UserHandle.myUserId())) == 
                    PackageManager.INSTALL_REASON_USER) {
                    String[] categories = pm.getInstalledPackages(
                        PackageManager.GET_META_DATA).categories;
                    if (categories != null) {
                        for (String category : categories) {
                            if (category.contains("GAME")) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("GameDetection", "Error checking game: " + e.getMessage());
        }
        return false;
    }
    
    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, 
            android.os.Process.myUid(), 
            getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
    
    private void requestUsageStatsPermission() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}