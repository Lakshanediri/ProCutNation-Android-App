package com.s22010104.procutnation;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Collections;
import java.util.List;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder> {

    private final List<Project> projectList;
    private final Context context;
    private final ProjectInteractionListener listener;

    public interface ProjectInteractionListener {
        void onProjectClicked(Project project);
        void onRenameProject(Project project);
        void onDeleteProject(Project project);
        void onChangeColor(Project project);
    }

    public ProjectsAdapter(Context context, List<Project> projectList, ProjectInteractionListener listener) {
        this.context = context;
        this.projectList = projectList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projectList.get(position);
        holder.projectTextView.setText(project.getProjectName());

        if (project.getColor() != null && !project.getColor().isEmpty()) {
            holder.background.setBackgroundColor(Color.parseColor(project.getColor()));
        } else {
            holder.background.setBackgroundColor(Color.parseColor("#FFA500"));
        }

        holder.itemView.setOnClickListener(v -> listener.onProjectClicked(project));
        holder.settingsButton.setOnClickListener(v -> showOptionsMenu(v, project));
    }

    private void showOptionsMenu(View view, Project project) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.project_options_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_rename) {
                listener.onRenameProject(project);
            } else if (itemId == R.id.menu_change_color) {
                listener.onChangeColor(project);
            } else if (itemId == R.id.menu_delete) {
                listener.onDeleteProject(project);
            }
            return true;
        });
        popupMenu.show();
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    public List<Project> getProjectList() {
        return this.projectList;
    }

    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(projectList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView projectTextView;
        ImageView settingsButton;
        ConstraintLayout background;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            projectTextView = itemView.findViewById(R.id.projectTextView);
            settingsButton = itemView.findViewById(R.id.settingsButton);
            background = itemView.findViewById(R.id.project_background);
        }
    }
}