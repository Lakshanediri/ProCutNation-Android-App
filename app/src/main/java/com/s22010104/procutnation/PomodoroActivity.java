package com.s22010104.procutnation;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.Locale;

public class PomodoroActivity extends AppCompatActivity implements SensorEventListener {

    private TextView textViewTimer, textViewTaskNameHeader;
    private Button btnPauseContinue, btnStop;
    private ProgressBar progressBarTimer;
    private SwitchMaterial focusModeSwitch;

    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;
    private long mStartTimeInMillis;
    private long mTimeLeftInMillis;

    private String taskId, taskName;

    // --- SENSOR AND FOCUS MODE VARIABLES ---
    private SensorManager sensorManager;
    private Sensor accelerometer, lightSensor;
    private NotificationManager notificationManager;
    private boolean isFocusModeActive = false;
    private boolean isDeviceStable = true;
    private float[] lastSensorValues = new float[3];
    private float originalBrightness = -1; // To store the user's original brightness
    private static final float MOTION_THRESHOLD = 1.5f;
    private static final float DARK_THRESHOLD = 20.0f; // Light level (lux) to consider "dark"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);

        // --- SENSOR & NOTIFICATION SETUP ---
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        initializeViews();

        taskId = getIntent().getStringExtra("TASK_ID");
        taskName = getIntent().getStringExtra("TASK_NAME");
        int pomodoroMinutes = getIntent().getIntExtra("POMODORO_MINUTES", 25);
        mStartTimeInMillis = pomodoroMinutes * 60 * 1000L;
        mTimeLeftInMillis = mStartTimeInMillis;

        textViewTaskNameHeader.setText(taskName);

        setupListeners();
        updateCountDownText();
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_pomodoro);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        textViewTimer = findViewById(R.id.textViewTimer);
        textViewTaskNameHeader = findViewById(R.id.textViewTaskNameHeader);
        btnPauseContinue = findViewById(R.id.btnPauseContinue);
        btnStop = findViewById(R.id.btnStop);
        progressBarTimer = findViewById(R.id.progressBarTimer);
        focusModeSwitch = findViewById(R.id.focusModeSwitch);
    }

    private void setupListeners() {
        btnPauseContinue.setOnClickListener(v -> {
            if (mTimerRunning) pauseTimer();
            else startTimer();
        });
        btnStop.setOnClickListener(v -> resetTimer());

        focusModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isFocusModeActive = isChecked;
            if (isChecked) {
                checkDndPermission();
                Toast.makeText(this, "Focus Mode ON. Place phone on a flat surface.", Toast.LENGTH_LONG).show();
            } else {
                // When turning off focus mode, ensure everything resets
                stopFocusMode();
                Toast.makeText(this, "Focus Mode OFF.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- SENSOR EVENT HANDLING ---
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Ignore sensor events if focus mode isn't active or the timer isn't running
        if (!mTimerRunning || !isFocusModeActive) return;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            handleAccelerometer(event);
        } else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            handleLightSensor(event);
        }
    }

    private void handleAccelerometer(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float deltaX = Math.abs(lastSensorValues[0] - x);
        float deltaY = Math.abs(lastSensorValues[1] - y);
        float deltaZ = Math.abs(lastSensorValues[2] - z);

        lastSensorValues[0] = x;
        lastSensorValues[1] = y;
        lastSensorValues[2] = z;

        if (isDeviceStable && (deltaX > MOTION_THRESHOLD || deltaY > MOTION_THRESHOLD || deltaZ > MOTION_THRESHOLD)) {
            isDeviceStable = false;
            pauseTimer();
            Toast.makeText(this, "Timer paused. Stay focused!", Toast.LENGTH_SHORT).show();
        }
    }

    //  EYE PROTECTION LOGIC
    private void handleLightSensor(SensorEvent event) {
        float lux = event.values[0];
        if (lux < DARK_THRESHOLD) {
            setBrightness(0.8f); // Dim the screen to 80%
        } else {
            restoreBrightness(); // Restore to original brightness
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // --- DND, SCREEN PINNING, AND BRIGHTNESS LOGIC ---
    private void checkDndPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted()) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("To block distractions, this app needs permission to control Do Not Disturb mode.")
                    .setPositiveButton("OK", (dialog, which) -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)))
                    .setNegativeButton("Cancel", (dialog, which) -> focusModeSwitch.setChecked(false))
                    .show();
        }
    }

    private void setDndMode(boolean enable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notificationManager.isNotificationPolicyAccessGranted()) {
            notificationManager.setInterruptionFilter(enable ? NotificationManager.INTERRUPTION_FILTER_PRIORITY : NotificationManager.INTERRUPTION_FILTER_ALL);
        }
    }

    private void setBrightness(float value) {
        // Only save the original brightness once per session
        if (originalBrightness == -1) {
            originalBrightness = getWindow().getAttributes().screenBrightness;
        }
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = value;
        getWindow().setAttributes(layout);
    }

    private void restoreBrightness() {
        if (originalBrightness != -1) {
            WindowManager.LayoutParams layout = getWindow().getAttributes();
            layout.screenBrightness = originalBrightness;
            getWindow().setAttributes(layout);
            // Reset so it can be saved again next time
            originalBrightness = -1;
        }
    }


    // --- TIMER LOGIC ---
    private void startTimer() {
        isDeviceStable = true;
        if (isFocusModeActive) {
            setDndMode(true);
            // This now correctly calls the public method from the parent Activity class
            startLockTask();
        }
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }
            @Override
            public void onFinish() {
                mTimerRunning = false;
                btnPauseContinue.setText("Start");
                stopFocusMode();
                navigateToProgressScreen();
            }
        }.start();
        mTimerRunning = true;
        btnPauseContinue.setText("Pause");
    }

    private void pauseTimer() {
        if (mCountDownTimer != null) mCountDownTimer.cancel();
        mTimerRunning = false;
        btnPauseContinue.setText("Continue");
        stopFocusMode();
    }

    private void resetTimer() {
        if (mCountDownTimer != null) mCountDownTimer.cancel();
        mTimerRunning = false;
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
        btnPauseContinue.setText("Start");
        stopFocusMode();
        Toast.makeText(this, "Timer stopped.", Toast.LENGTH_SHORT).show();
    }

    private void stopFocusMode() {
        setDndMode(false);
        // This now correctly calls the public method from the parent Activity class
        stopLockTask();
        restoreBrightness();
    }

    // --- BACK BUTTON OVERRIDE ---
    @Override
    public void onBackPressed() {
        if (mTimerRunning && isFocusModeActive) {
            new AlertDialog.Builder(this)
                    .setTitle("Quit Session?")
                    .setMessage("Are you sure you want to stop your focus session? Your progress will be lost.")
                    .setPositiveButton("Quit", (dialog, which) -> {
                        resetTimer();
                        super.onBackPressed();
                    })
                    .setNegativeButton("Stay", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    // --- LIFECYCLE & HELPER METHODS ---
    @Override
    protected void onResume() {
        super.onResume();
        // Register both sensors when the activity is visible
        if (accelerometer != null) sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        if (lightSensor != null) sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister to save battery when the activity is not visible
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Failsafe to turn off focus mode features if the activity is destroyed
        stopFocusMode();
        if (mCountDownTimer != null) mCountDownTimer.cancel();
    }

    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        textViewTimer.setText(timeLeftFormatted);
        if (mStartTimeInMillis > 0) {
            progressBarTimer.setProgress((int) (mTimeLeftInMillis * 100 / mStartTimeInMillis));
        }
    }

    private void navigateToProgressScreen() {
        Intent intent = new Intent(this, ProgressActivity.class);
        intent.putExtra("TASK_ID", taskId);
        intent.putExtra("POMODORO_MINUTES", (int) (mStartTimeInMillis / 60000));
        startActivity(intent);
        finish();
    }
}
