package com.s22010104.procutnation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TasksActivity extends AppCompatActivity implements TasksAdapter.TaskInteractionListener {

    private RecyclerView tasksRecyclerView;
    private TasksAdapter tasksAdapter;
    private List<Task> taskList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentProjectId, currentProjectName;
    private ItemTouchHelper itemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        currentProjectId = getIntent().getStringExtra("PROJECT_ID");
        currentProjectName = getIntent().getStringExtra("PROJECT_NAME");

        if (currentProjectId == null) {
            Toast.makeText(this, "Project not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView headerTitle = findViewById(R.id.headerTitle);
        headerTitle.setText(currentProjectName);

        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        tasksAdapter = new TasksAdapter(this, taskList, this);
        tasksRecyclerView.setAdapter(tasksAdapter);

        ItemTouchHelper.Callback callback = new TaskMoveCallback();
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);

        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(view -> {
            Intent intent = new Intent(TasksActivity.this, AddTaskActivity.class);
            intent.putExtra("PROJECT_ID", currentProjectId);
            intent.putExtra("PROJECT_NAME", currentProjectName);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .whereEqualTo("projectId", currentProjectId)
                .orderBy("position", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    taskList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Task task = doc.toObject(Task.class);
                        task.setTaskId(doc.getId());
                        taskList.add(task);
                    }
                    tasksAdapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onPlayTask(Task task) {
        Intent intent = new Intent(this, PomodoroActivity.class);
        intent.putExtra("TASK_ID", task.getTaskId());
        intent.putExtra("TASK_NAME", task.getTaskName());
        intent.putExtra("POMODORO_MINUTES", task.getPomodoroSettings());
        startActivity(intent);
    }

    @Override
    public void onEditTask(Task task) {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_task, null);

        EditText etName = dialogView.findViewById(R.id.editTextTaskName);
        EditText etTaskHours = dialogView.findViewById(R.id.editTextTaskHours);
        EditText etMilestoneHours = dialogView.findViewById(R.id.editTextMilestoneHours);
        Spinner spinnerPriority = dialogView.findViewById(R.id.spinnerPriority);
        Spinner spinnerPomodoro = dialogView.findViewById(R.id.spinnerPomodoroTime);

        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(this, R.array.priority_levels, android.R.layout.simple_spinner_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);

        ArrayAdapter<CharSequence> pomodoroAdapter = ArrayAdapter.createFromResource(this, R.array.pomodoro_times, android.R.layout.simple_spinner_item);
        pomodoroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPomodoro.setAdapter(pomodoroAdapter);

        etName.setText(task.getTaskName());
        etTaskHours.setText(String.valueOf(task.getTaskHours()));
        etMilestoneHours.setText(String.valueOf(task.getMilestoneHours()));

        if (task.getPriority() != null) {
            spinnerPriority.setSelection(priorityAdapter.getPosition(task.getPriority()));
        }
        spinnerPomodoro.setSelection(pomodoroAdapter.getPosition(String.valueOf(task.getPomodoroSettings())));

        new AlertDialog.Builder(this)
                .setTitle("Edit Task")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("taskName", etName.getText().toString());
                    updates.put("taskHours", Integer.parseInt(etTaskHours.getText().toString()));
                    updates.put("milestoneHours", Integer.parseInt(etMilestoneHours.getText().toString()));
                    updates.put("priority", spinnerPriority.getSelectedItem().toString());
                    updates.put("pomodoroSettings", Integer.parseInt(spinnerPomodoro.getSelectedItem().toString()));

                    db.collection("tasks").document(task.getTaskId()).update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Task updated.", Toast.LENGTH_SHORT).show();
                                loadTasks();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDeleteTask(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete '" + task.getTaskName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("tasks").document(task.getTaskId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Task deleted.", Toast.LENGTH_SHORT).show();
                                loadTasks();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onChangeColor(Task task) {
        final String[] colorNames = {"Orange", "Green", "Blue", "Purple", "Red"};
        final String[] colorHexCodes = {"#FFA500", "#4CAF50", "#2196F3", "#9C27B0", "#E91E63"};
        new AlertDialog.Builder(this)
                .setTitle("Choose a color")
                .setItems(colorNames, (dialog, which) -> {
                    String selectedColor = colorHexCodes[which];
                    db.collection("tasks").document(task.getTaskId()).update("color", selectedColor)
                            .addOnSuccessListener(aVoid -> loadTasks());
                })
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private class TaskMoveCallback extends ItemTouchHelper.SimpleCallback {
        TaskMoveCallback() {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            Collections.swap(taskList, fromPosition, toPosition);
            tasksAdapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            updateTaskPositionsInFirestore();
        }
    }

    private void updateTaskPositionsInFirestore() {
        WriteBatch batch = db.batch();
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);
            batch.update(db.collection("tasks").document(task.getTaskId()), "position", i);
        }
        batch.commit().addOnFailureListener(e -> Toast.makeText(this, "Failed to save order.", Toast.LENGTH_SHORT).show());
    }
}