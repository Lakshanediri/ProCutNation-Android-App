package com.s22010104.procutnation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private final Context context;
    private final List<LeaderboardUser> userList;

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

        holder.rank.setText(String.valueOf(position + 4));
        holder.name.setText(user.getName());
        holder.xp.setText(user.getXpLevel() + " XP");
        holder.points.setText(user.getPoints() + " Points"); // Set points text

        // Set profile image
        if (user.getProfileImageBase64() != null && !user.getProfileImageBase64().isEmpty()) {
            byte[] decodedBytes = Base64.decode(user.getProfileImageBase64(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            holder.profileImage.setImageBitmap(bitmap);
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_account_circle);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView rank, name, xp, points; // Added points TextView
        CircleImageView profileImage;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            rank = itemView.findViewById(R.id.rankTextView);
            name = itemView.findViewById(R.id.nameTextView);
            xp = itemView.findViewById(R.id.xpTextView);
            points = itemView.findViewById(R.id.pointsTextView); // Initialize points TextView
            profileImage = itemView.findViewById(R.id.profileImageView);
        }
    }
}