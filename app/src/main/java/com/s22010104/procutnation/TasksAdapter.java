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
import java.util.List;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TaskViewHolder> {
    private final List<Task> taskList;
    private final Context context;
    private final TaskInteractionListener listener;

    public interface TaskInteractionListener {
        void onPlayTask(Task task);
        void onEditTask(Task task);
        void onDeleteTask(Task task);
        void onChangeColor(Task task);
    }

    public TasksAdapter(Context context, List<Task> taskList, TaskInteractionListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskNameTextView.setText(task.getTaskName());
        if (task.getColor() != null && !task.getColor().isEmpty()) {
            holder.background.setBackgroundColor(Color.parseColor(task.getColor()));
        } else {
            holder.background.setBackgroundColor(Color.parseColor("#FFA500"));
        }
        holder.playButton.setOnClickListener(v -> listener.onPlayTask(task));
        holder.settingsButton.setOnClickListener(v -> showOptionsMenu(v, task));
    }

    private void showOptionsMenu(View view, Task task) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.task_options_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit_task) {
                listener.onEditTask(task);
                return true;
            } else if (itemId == R.id.menu_change_task_color) {
                listener.onChangeColor(task);
                return true;
            } else if (itemId == R.id.menu_delete_task) {
                listener.onDeleteTask(task);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskNameTextView;
        ImageView playButton, settingsButton;
        ConstraintLayout background;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskNameTextView = itemView.findViewById(R.id.textViewTaskName);
            playButton = itemView.findViewById(R.id.playButton);
            settingsButton = itemView.findViewById(R.id.settingsButton);
            background = itemView.findViewById(R.id.task_background);
        }
    }
}
