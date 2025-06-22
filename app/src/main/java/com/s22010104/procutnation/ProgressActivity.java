package com.s22010104.procutnation;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

public class ProgressActivity extends AppCompatActivity {

    private String taskId;
    private int pomodoroMinutesCompleted;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProgressBar progressBarTask;
    private TextView textViewProgressPercent;
    private ImageView characterImageView;
    private static final int POINTS_PER_SESSION = 10;
    private static final int XP_PER_SESSION = 5;
    private static final String TAG = "ProgressActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        taskId = getIntent().getStringExtra("TASK_ID");
        pomodoroMinutesCompleted = getIntent().getIntExtra("POMODORO_MINUTES", 25);

        progressBarTask = findViewById(R.id.progressBarOverall);
        textViewProgressPercent = findViewById(R.id.textViewProgressPercent);
        characterImageView = findViewById(R.id.imageViewCharacter);

        Button btnShare = findViewById(R.id.btnShare);
        Button btnContinue = findViewById(R.id.btnContinue);

        btnShare.setOnClickListener(v -> shareProgress());
        btnContinue.setOnClickListener(v -> finish());

        updateProgressAndLogSession();
    }

    private void updateProgressAndLogSession() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || taskId == null) {
            Toast.makeText(this, "Error: User or Task not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        final DocumentReference userRef = db.collection("users").document(currentUser.getUid());
        final DocumentReference taskRef = db.collection("tasks").document(taskId);

        db.runTransaction((Transaction.Function<Integer>) transaction -> {
            DocumentSnapshot userSnap = transaction.get(userRef);
            DocumentSnapshot taskSnap = transaction.get(taskRef);

            if (!userSnap.exists() || !taskSnap.exists()) {
                throw new IllegalStateException("User or Task document does not exist!");
            }

            long currentPoints = userSnap.getLong("points") != null ? userSnap.getLong("points") : 0;
            long currentXp = userSnap.getLong("xpLevel") != null ? userSnap.getLong("xpLevel") : 0;
            transaction.update(userRef, "points", currentPoints + POINTS_PER_SESSION);
            transaction.update(userRef, "xpLevel", currentXp + XP_PER_SESSION);

            String activePetDrawableName = userSnap.getString("activePetDrawableName");
            runOnUiThread(() -> {
                if (activePetDrawableName != null && !activePetDrawableName.isEmpty()) {
                    int resId = getResources().getIdentifier(activePetDrawableName, "drawable", getPackageName());
                    if (resId != 0) characterImageView.setImageResource(resId);
                }
            });

            long taskTotalHours = taskSnap.getLong("taskHours") != null ? taskSnap.getLong("taskHours") : 0;
            long currentProgressPercent = taskSnap.getLong("progress") != null ? taskSnap.getLong("progress") : 0;
            long totalTaskMinutes = taskTotalHours * 60;
            if (totalTaskMinutes <= 0) totalTaskMinutes = pomodoroMinutesCompleted;
            long minutesAlreadyWorked = (totalTaskMinutes * currentProgressPercent) / 100;
            long newTotalMinutesWorked = minutesAlreadyWorked + pomodoroMinutesCompleted;
            int newProgressPercentage = (int) ((newTotalMinutesWorked * 100) / totalTaskMinutes);
            if (newProgressPercentage > 100) newProgressPercentage = 100;
            transaction.update(taskRef, "progress", newProgressPercentage);

            String projectId = taskSnap.getString("projectId");
            String projectName = taskSnap.getString("projectName");
            PomodoroSession session = new PomodoroSession(currentUser.getUid(), taskId, projectId, projectName, pomodoroMinutesCompleted);
            transaction.set(db.collection("pomodoro_sessions").document(), session);

            return newProgressPercentage;

        }).addOnSuccessListener(newProgress -> {
            Toast.makeText(this, "Progress saved! You earned " + POINTS_PER_SESSION + " points.", Toast.LENGTH_SHORT).show();
            progressBarTask.setProgress(newProgress);
            textViewProgressPercent.setText(newProgress + "%");
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Transaction failed: ", e);
            Toast.makeText(this, "Failed to update progress: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void shareProgress() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareBody = "I just completed a focus session with ProCutNation! Making progress on my goals.";
        String shareSub = "My Productivity Progress";
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(shareIntent, "Share using"));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}