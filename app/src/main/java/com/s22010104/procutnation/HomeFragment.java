package com.s22010104.procutnation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment implements ProjectsAdapter.ProjectInteractionListener {

    private RecyclerView projectsRecyclerView;
    private ProjectsAdapter projectsAdapter;
    private List<Project> projectList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ConstraintLayout emptyStateLayout;
    private FloatingActionButton fabAddTask;
    private ItemTouchHelper itemTouchHelper;
    private SearchView searchView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        projectsRecyclerView = view.findViewById(R.id.projectsRecyclerView);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        fabAddTask = view.findViewById(R.id.fabAddTask);
        searchView = view.findViewById(R.id.searchView);
        projectsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        projectList = new ArrayList<>();
        projectsAdapter = new ProjectsAdapter(getContext(), projectList, this);
        projectsRecyclerView.setAdapter(projectsAdapter);

        setupSearch();

        ItemTouchHelper.Callback callback = new ProjectMoveCallback();
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(projectsRecyclerView);

        loadProjects();

        fabAddTask.setOnClickListener(v -> showAddProjectDialog());
        view.findViewById(R.id.emptyStateAddButton).setOnClickListener(v -> showAddProjectDialog());

        return view;
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.trim().isEmpty()) {
                    searchForTasks(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // If you want live filtering of projects (not tasks), you could call:
                // projectsAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void searchForTasks(String query) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Searching...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .orderBy("taskName")
                .startAt(query.toLowerCase())
                .endAt(query.toLowerCase() + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressDialog.dismiss();
                    List<Task> searchResults = new ArrayList<>();
                    List<String> displayResults = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Task task = doc.toObject(Task.class);
                        searchResults.add(task);
                        displayResults.add(task.getTaskName() + " (in " + task.getProjectName() + ")");
                    }

                    if (searchResults.isEmpty()) {
                        Toast.makeText(getContext(), "No tasks found matching your search.", Toast.LENGTH_SHORT).show();
                    } else {
                        showSearchResultsDialog(searchResults, displayResults);
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showSearchResultsDialog(List<Task> searchResults, List<String> displayResults) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, displayResults);

        new AlertDialog.Builder(getContext())
                .setTitle("Search Results")
                .setAdapter(adapter, (dialog, which) -> {
                    Task selectedTask = searchResults.get(which);
                    Intent intent = new Intent(getContext(), TasksActivity.class);
                    intent.putExtra("PROJECT_ID", selectedTask.getProjectId());
                    intent.putExtra("PROJECT_NAME", selectedTask.getProjectName());
                    startActivity(intent);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void loadProjects() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        db.collection("users").document(userId).collection("projects")
                .orderBy("position", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        projectList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Project project = document.toObject(Project.class);
                            project.setProjectId(document.getId());
                            projectList.add(project);
                        }
                        projectsAdapter.notifyDataSetChanged();
                        updateUI();
                    }
                });
    }

    private void updateUI() {
        if (projectList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            projectsRecyclerView.setVisibility(View.GONE);
            fabAddTask.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            projectsRecyclerView.setVisibility(View.VISIBLE);
            fabAddTask.setVisibility(View.VISIBLE);
        }
    }

    private void showAddProjectDialog() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_project, null);
        final EditText editTextProjectName = dialogView.findViewById(R.id.editTextProjectNameDialog);

        new AlertDialog.Builder(getContext())
                .setTitle("Add New Project")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String projectName = editTextProjectName.getText().toString().trim();
                    if (!TextUtils.isEmpty(projectName)) {
                        saveProjectToFirestore(projectName);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveProjectToFirestore(String projectName) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        CollectionReference projectsRef = db.collection("users").document(userId).collection("projects");

        Map<String, Object> project = new HashMap<>();
        project.put("projectName", projectName);
        project.put("userId", userId);
        project.put("color", "#FFA500");
        project.put("position", projectList.size());
        project.put("createdAt", new Date());

        projectsRef.add(project).addOnSuccessListener(documentReference -> {
            Toast.makeText(getContext(), "Project added!", Toast.LENGTH_SHORT).show();
            loadProjects();
        });
    }

    @Override
    public void onProjectClicked(Project project) {
        Intent intent = new Intent(getContext(), TasksActivity.class);
        intent.putExtra("PROJECT_ID", project.getProjectId());
        intent.putExtra("PROJECT_NAME", project.getProjectName());
        startActivity(intent);
    }

    @Override
    public void onRenameProject(Project project) {
        final EditText input = new EditText(getContext());
        input.setText(project.getProjectName());
        new AlertDialog.Builder(getContext())
                .setTitle("Rename Project")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        db.collection("users").document(mAuth.getUid()).collection("projects")
                                .document(project.getProjectId()).update("projectName", newName)
                                .addOnSuccessListener(aVoid -> loadProjects());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDeleteProject(Project project) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Project")
                .setMessage("Are you sure you want to delete '" + project.getProjectName() + "'? All of its tasks will also be permanently deleted.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteProjectAndTasks(project);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProjectAndTasks(Project project) {
        String userId = mAuth.getUid();
        DocumentReference projectRef = db.collection("users").document(userId).collection("projects").document(project.getProjectId());

        db.collection("tasks").whereEqualTo("projectId", project.getProjectId())
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }
                    batch.delete(projectRef);
                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Project deleted.", Toast.LENGTH_SHORT).show();
                        loadProjects();
                    });
                });
    }

    @Override
    public void onChangeColor(Project project) {
        final String[] colorNames = {"Orange", "Yellow", "Green", "Blue", "Purple", "Red"};
        final String[] colorHexCodes = {"#FFA500", "#FFC107", "#4CAF50", "#2196F3", "#9C27B0", "#E91E63"};

        new AlertDialog.Builder(getContext())
                .setTitle("Choose a color")
                .setItems(colorNames, (dialog, which) -> {
                    String selectedColorHex = colorHexCodes[which];
                    db.collection("users").document(mAuth.getUid()).collection("projects")
                            .document(project.getProjectId()).update("color", selectedColorHex)
                            .addOnSuccessListener(aVoid -> loadProjects());
                })
                .show();
    }

    private class ProjectMoveCallback extends ItemTouchHelper.SimpleCallback {
        public ProjectMoveCallback() {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            projectsAdapter.onItemMove(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            updateProjectPositionsInFirestore();
        }
    }

    private void updateProjectPositionsInFirestore() {
        WriteBatch batch = db.batch();
        List<Project> currentList = projectsAdapter.getProjectList();
        for (int i = 0; i < currentList.size(); i++) {
            Project project = currentList.get(i);
            DocumentReference ref = db.collection("users").document(mAuth.getUid())
                    .collection("projects").document(project.getProjectId());
            batch.update(ref, "position", i);
        }
        batch.commit().addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save order.", Toast.LENGTH_SHORT).show());
    }
}
