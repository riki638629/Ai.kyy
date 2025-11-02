public class SystemOptimizer {
    public static void optimizeSystem(Context context) {
        // 1. Optimasi jaringan
        optimizeNetworkSettings(context);
        
        // 2. Bersihkan file sampah
        cleanJunkFiles(context);
        
        // 3. Optimasi database
        optimizeDatabases(context);
        
        // 4. Nonaktifkan services yang tidak perlu
        disableBackgroundServices(context);
    }
    
    private static void optimizeNetworkSettings(Context context) {
        try {
            // Aktifkan TCP buffer optimization
            SystemProperties.set("net.tcp.buffersize.default", "4096,87380,256960,4096,16384,256960");
            SystemProperties.set("net.tcp.buffersize.wifi", "4096,87380,256960,4096,16384,256960");
            SystemProperties.set("net.tcp.buffersize.umts", "4096,87380,256960,4096,16384,256960");
            
            // Optimasi DNS
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            LinkProperties lp = cm.getLinkProperties(cm.getActiveNetwork());
            if (lp != null) {
                List<InetAddress> dnsServers = lp.getDnsServers();
                if (dnsServers.size() >= 2) {
                    SystemProperties.set("net.dns1", dnsServers.get(0).getHostAddress());
                    SystemProperties.set("net.dns2", dnsServers.get(1).getHostAddress());
                }
            }
        } catch (Exception e) {
            Log.e("SystemOptimizer", "Network optimization failed: " + e.getMessage());
        }
    }
    
    private static void cleanJunkFiles(Context context) {
        // Bersihkan cache aplikasi
        PackageManager pm = context.getPackageManager();
        for (ApplicationInfo app : pm.getInstalledApplications(0)) {
            try {
                pm.deleteApplicationCacheFiles(app.packageName, null);
            } catch (Exception e) {
                Log.w("SystemOptimizer", "Failed to clear cache for " + app.packageName);
            }
        }
        
        // Bersihkan direktori cache umum
        deleteRecursive(new File(context.getCacheDir(), ""));
        deleteRecursive(new File(context.getExternalCacheDir(), ""));
    }
    
    private static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }
    
    private static void optimizeDatabases(Context context) {
        File databasesDir = new File(context.getApplicationInfo().dataDir, "databases");
        if (databasesDir.exists()) {
            for (File dbFile : databasesDir.listFiles()) {
                if (dbFile.getName().endsWith(".db")) {
                    try {
                        SQLiteDatabase db = SQLiteDatabase.openDatabase(
                            dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
                        db.execSQL("VACUUM;");
                        db.execSQL("ANALYZE;");
                        db.close();
                    } catch (Exception e) {
                        Log.w("SystemOptimizer", "Failed to optimize DB: " + dbFile.getName());
                    }
                }
            }
        }
    }
    
    private static void disableBackgroundServices(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(100);
        
        for (ActivityManager.RunningServiceInfo service : services) {
            if (service.foreground || isSystemService(service.service.getPackageName())) {
                continue;
            }
            
            try {
                ActivityManagerCompat.stopService(context, service.service);
            } catch (Exception e) {
                Log.w("SystemOptimizer", "Failed to stop service: " + service.service.getClassName());
            }
        }
    }
    
    private static boolean isSystemService(String packageName) {
        return packageName.startsWith("android") || 
               packageName.startsWith("com.android") ||
               packageName.startsWith("com.google.android") ||
               packageName.startsWith("com.qualcomm");
    }
}