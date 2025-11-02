public class MainActivity extends AppCompatActivity {
    private Button btnTurbo, btnCleanMemory, btnOptimize;
    private TextView tvStatus, tvMemoryInfo;
    private SwitchCompat swAutoBoost;
    private ProgressBar progressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Inisialisasi view
        btnTurbo = findViewById(R.id.btnTurbo);
        btnCleanMemory = findViewById(R.id.btnCleanMemory);
        btnOptimize = findViewById(R.id.btnOptimize);
        tvStatus = findViewById(R.id.tvStatus);
        tvMemoryInfo = findViewById(R.id.tvMemoryInfo);
        swAutoBoost = findViewById(R.id.swAutoBoost);
        progressBar = findViewById(R.id.progressBar);
        
        // Update info memori setiap 2 detik
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    tvMemoryInfo.setText(MemoryCleaner.getMemoryInfo(MainActivity.this));
                });
            }
        }, 0, 2000);
        
        // Button listeners
        btnTurbo.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            new Thread(() -> {
                TurboModeManager.enableTurboMode(MainActivity.this);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("Status: Mode Turbo Aktif");
                    showBoostResult();
                });
            }).start();
        });
        
        btnCleanMemory.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            new Thread(() -> {
                MemoryCleaner.cleanRAM(MainActivity.this);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("Status: Memori Dibersihkan");
                    tvMemoryInfo.setText(MemoryCleaner.getMemoryInfo(MainActivity.this));
                });
            }).start();
        });
        
        btnOptimize.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            new Thread(() -> {
                SystemOptimizer.optimizeSystem(MainActivity.this);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("Status: Sistem Dioptimalkan");
                });
            }).start();
        });
    }
    
    private void showBoostResult() {
        // Tampilkan dialog dengan hasil boosting
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Boost Result");
        builder.setMessage("Perangkat telah dioptimalkan untuk performa maksimal!\n\n" +
            "• CPU diatur ke mode performance\n" +
            "• GPU dioptimalkan\n" +
            "• Animasi sistem dinonaktifkan\n" +
            "• Prioritas thread ditingkatkan");
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}