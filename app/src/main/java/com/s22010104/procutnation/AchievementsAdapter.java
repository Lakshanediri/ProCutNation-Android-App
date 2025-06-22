package com.s22010104.procutnation;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.List;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.AchievementViewHolder> {

    private final List<DocumentSnapshot> inventoryList;
    private final Context context;
    private final AchievementInteractionListener listener;

    public interface AchievementInteractionListener {
        void onPetSelected(String drawableName);
    }

    public AchievementsAdapter(Context context, List<DocumentSnapshot> inventoryList, AchievementInteractionListener listener) {
        this.context = context;
        this.inventoryList = inventoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        DocumentSnapshot item = inventoryList.get(position);
        String itemName = item.getString("itemName");
        String drawableName = item.getString("drawableName");

        holder.nameTextView.setText(itemName);

        if (drawableName != null) {
            Resources resources = context.getResources();
            final int resourceId = resources.getIdentifier(drawableName, "drawable", context.getPackageName());
            if (resourceId != 0) {
                holder.imageView.setImageResource(resourceId);
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onPetSelected(drawableName));
    }

    @Override
    public int getItemCount() {
        return inventoryList.size();
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.achievementImageView);
            nameTextView = itemView.findViewById(R.id.achievementNameTextView);
        }
    }
}