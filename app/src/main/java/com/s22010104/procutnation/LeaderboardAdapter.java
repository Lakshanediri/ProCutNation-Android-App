package com.s22010104.procutnation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private List<LeaderboardUser> userList;
    private Context context;

    public LeaderboardAdapter(Context context, List<LeaderboardUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        LeaderboardUser user = userList.get(position);
        holder.rankTextView.setText(String.valueOf(position + 4));
        holder.nameTextView.setText(user.getName());
        // Updated to show XP Level
        holder.pointsTextView.setText(String.valueOf(user.getXpLevel()) + " XP");
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView rankTextView, nameTextView, pointsTextView;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            rankTextView = itemView.findViewById(R.id.rankTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            pointsTextView = itemView.findViewById(R.id.pointsTextView);
        }
    }
}


