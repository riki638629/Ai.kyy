public static boolean isDeviceRooted() {
    String buildTags = android.os.Build.TAGS;
    if (buildTags != null && buildTags.contains("test-keys")) {
        return true;
    }
    
    try {
        File file = new File("/system/app/Superuser.apk");
        if (file.exists()) {
            return true;
        }
    } catch (Exception e) {
        // Ignore
    }
    
    return new File("/system/bin/su").exists() || 
           new File("/system/xbin/su").exists();
}