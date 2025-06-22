package com.s22010104.procutnation;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddTaskActivity extends AppCompatActivity {

    private EditText editTextTaskName, editTextTaskHours, editTextMilestoneHours,
            editTextStartDate, editTextEndDate, editTextPomodoroSettings;
    private Spinner spinnerPriority;
    private Button btnAddTask;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private String projectId, projectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        projectId = getIntent().getStringExtra("PROJECT_ID");
        projectName = getIntent().getStringExtra("PROJECT_NAME");

        if (projectId == null || projectName == null) {
            Toast.makeText(this, "Error: Project details are missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupSpinner();
        setupDatePickers();
        setupAddTaskButton();
    }

    private void initializeViews() {
        editTextTaskName = findViewById(R.id.editTextTaskName);
        editTextTaskHours = findViewById(R.id.editTextTaskHours);
        editTextMilestoneHours = findViewById(R.id.editTextMilestoneHours);
        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextEndDate = findViewById(R.id.editTextEndDate);
        editTextPomodoroSettings = findViewById(R.id.editTextPomodoroSettings);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        btnAddTask = findViewById(R.id.btnAddTAsk);
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.priority_levels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(adapter);
    }

    private void setupDatePickers() {
        DatePickerDialog.OnDateSetListener startDateListener = (view, year, month, dayOfMonth) -> {
            startDateCalendar.set(Calendar.YEAR, year);
            startDateCalendar.set(Calendar.MONTH, month);
            startDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel(editTextStartDate, startDateCalendar);
        };

        DatePickerDialog.OnDateSetListener endDateListener = (view, year, month, dayOfMonth) -> {
            endDateCalendar.set(Calendar.YEAR, year);
            endDateCalendar.set(Calendar.MONTH, month);
            endDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel(editTextEndDate, endDateCalendar);
        };

        editTextStartDate.setOnClickListener(v -> new DatePickerDialog(AddTaskActivity.this, startDateListener,
                startDateCalendar.get(Calendar.YEAR), startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)).show());

        editTextEndDate.setOnClickListener(v -> new DatePickerDialog(AddTaskActivity.this, endDateListener,
                endDateCalendar.get(Calendar.YEAR), endDateCalendar.get(Calendar.MONTH),
                endDateCalendar.get(Calendar.DAY_OF_MONTH)).show());
    }

    private void updateLabel(EditText editText, Calendar calendar) {
        String format = "MM/dd/yy";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format, java.util.Locale.US);
        editText.setText(sdf.format(calendar.getTime()));
    }

    private void setupAddTaskButton() {
        btnAddTask.setOnClickListener(v -> saveTaskToFirestore());
    }

    private void saveTaskToFirestore() {
        String taskName = editTextTaskName.getText().toString().trim();
        if (TextUtils.isEmpty(taskName)) {
            Toast.makeText(this, "Task Name is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        String pomodoroString = editTextPomodoroSettings.getText().toString().trim();
        int pomodoroMinutes = 25;
        if (!pomodoroString.isEmpty()) {
            try {
                pomodoroMinutes = Integer.parseInt(pomodoroString);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number for timer settings.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        final int finalPomodoroMinutes = pomodoroMinutes;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int position = queryDocumentSnapshots.size();
                    Map<String, Object> task = new HashMap<>();
                    task.put("taskName", taskName);
                    task.put("projectName", projectName);
                    task.put("taskHours", Integer.parseInt(editTextTaskHours.getText().toString()));
                    task.put("milestoneHours", Integer.parseInt(editTextMilestoneHours.getText().toString()));
                    task.put("startDate", new Timestamp(startDateCalendar.getTime()));
                    task.put("endDate", new Timestamp(endDateCalendar.getTime()));
                    task.put("priority", spinnerPriority.getSelectedItem().toString());
                    task.put("pomodoroSettings", finalPomodoroMinutes);
                    task.put("isCompleted", false);
                    task.put("progress", 0);
                    task.put("userId", userId);
                    task.put("projectId", projectId);
                    task.put("color", "#FFA500");
                    task.put("position", position);

                    db.collection("tasks").add(task)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, "Task added successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error adding task", Toast.LENGTH_SHORT).show());
                });
    }
}

