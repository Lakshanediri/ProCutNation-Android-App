package com.s22010104.procutnation;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Locale;

public class PomodoroActivity extends AppCompatActivity {

    private TextView textViewTimer, textViewTaskNameHeader;
    private Button btnPauseContinue, btnStop;
    private ProgressBar progressBarTimer;

    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;

    private long mStartTimeInMillis;
    private long mTimeLeftInMillis;

    private String taskId, taskName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);

        taskId = getIntent().getStringExtra("TASK_ID");
        taskName = getIntent().getStringExtra("TASK_NAME");
        int pomodoroMinutes = getIntent().getIntExtra("POMODORO_MINUTES", 25);
        mStartTimeInMillis = pomodoroMinutes * 60 * 1000L;
        mTimeLeftInMillis = mStartTimeInMillis;

        Toolbar toolbar = findViewById(R.id.toolbar_pomodoro);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        textViewTimer = findViewById(R.id.textViewTimer);
        textViewTaskNameHeader = findViewById(R.id.textViewTaskNameHeader);
        btnPauseContinue = findViewById(R.id.btnPauseContinue);
        btnStop = findViewById(R.id.btnStop);
        progressBarTimer = findViewById(R.id.progressBarTimer);

        textViewTaskNameHeader.setText(taskName);

        btnPauseContinue.setOnClickListener(v -> {
            if (mTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnStop.setOnClickListener(v -> resetTimer());

        updateCountDownText();
    }

    private void startTimer() {
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
                navigateToProgressScreen();
            }
        }.start();

        mTimerRunning = true;
        btnPauseContinue.setText("Pause");
    }

    private void pauseTimer() {
        mCountDownTimer.cancel();
        mTimerRunning = false;
        btnPauseContinue.setText("Continue");
    }

    private void resetTimer() {
        if(mCountDownTimer != null){
            mCountDownTimer.cancel();
        }
        mTimerRunning = false;
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
        btnPauseContinue.setText("Start");
        Toast.makeText(this, "Timer stopped.", Toast.LENGTH_SHORT).show();
    }

    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        textViewTimer.setText(timeLeftFormatted);

        if (mStartTimeInMillis == 0) mStartTimeInMillis = 1;
        long progress = (mTimeLeftInMillis * 100) / mStartTimeInMillis;
        progressBarTimer.setProgress((int)progress);
    }

    private void navigateToProgressScreen() {
        Intent intent = new Intent(this, ProgressActivity.class);
        intent.putExtra("TASK_ID", taskId);
        // Send the original duration of the timer that was just completed
        intent.putExtra("POMODORO_MINUTES", (int) (mStartTimeInMillis / 60000));
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }
}

