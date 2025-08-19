package com.s22010104.procutnation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class LeaderboardFragment extends Fragment {

    private RecyclerView leaderboardRecyclerView;
    private LeaderboardAdapter adapter;
    private List<LeaderboardUser> userListForRecycler;
    private FirebaseFirestore db;

    private ProgressBar progressBar;
    private TextView emptyTextView;
    private ConstraintLayout podiumLayout;

    private TextView firstPlaceName, secondPlaceName, thirdPlaceName;
    private TextView firstPlaceXp, secondPlaceXp, thirdPlaceXp;
    private CircleImageView firstPlaceImage, secondPlaceImage, thirdPlaceImage;

    private static final String TAG = "LeaderboardFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        db = FirebaseFirestore.getInstance();
        userListForRecycler = new ArrayList<>();

        Toolbar toolbar = view.findViewById(R.id.toolbar_leaderboard);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        initializeViews(view);

        leaderboardRecyclerView = view.findViewById(R.id.leaderboardRecyclerView);
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LeaderboardAdapter(getContext(), userListForRecycler);
        leaderboardRecyclerView.setAdapter(adapter);

        loadLeaderboardData();

        return view;
    }

    private void initializeViews(View view) {
        progressBar = view.findViewById(R.id.progressBarLeaderboard);
        emptyTextView = view.findViewById(R.id.textViewEmpty);
        podiumLayout = view.findViewById(R.id.podiumLayout);

        firstPlaceName = view.findViewById(R.id.firstPlaceName);
        secondPlaceName = view.findViewById(R.id.secondPlaceName);
        thirdPlaceName = view.findViewById(R.id.thirdPlaceName);
        firstPlaceXp = view.findViewById(R.id.firstPlaceXp);
        secondPlaceXp = view.findViewById(R.id.secondPlaceXp);
        thirdPlaceXp = view.findViewById(R.id.thirdPlaceXp);

        firstPlaceImage = view.findViewById(R.id.firstPlaceImage);
        secondPlaceImage = view.findViewById(R.id.secondPlaceImage);
        thirdPlaceImage = view.findViewById(R.id.thirdPlaceImage);
    }

    private void loadLeaderboardData() {
        showLoading(true);

        db.collection("users")
                .orderBy("xpLevel", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<LeaderboardUser> allUsers = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                String name = document.getString("name");
                                Long xpLevelLong = document.getLong("xpLevel");
                                Long pointsLong = document.getLong("points"); // Fetch points
                                String profileImage = document.getString("profileImageBase64");

                                if (name == null) name = "Unknown User";
                                if (xpLevelLong == null) xpLevelLong = 0L;
                                if (pointsLong == null) pointsLong = 0L; // Default points to 0
                                if (profileImage == null) profileImage = "";

                                allUsers.add(new LeaderboardUser(name, xpLevelLong, pointsLong, profileImage));
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing user document: " + document.getId(), e);
                            }
                        }

                        if (allUsers.isEmpty()) {
                            emptyTextView.setVisibility(View.VISIBLE);
                            podiumLayout.setVisibility(View.GONE);
                            leaderboardRecyclerView.setVisibility(View.GONE);
                        } else {
                            emptyTextView.setVisibility(View.GONE);
                            podiumLayout.setVisibility(View.VISIBLE);
                            leaderboardRecyclerView.setVisibility(View.VISIBLE);
                            updatePodium(allUsers);
                            updateRecyclerView(allUsers);
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(getContext(), "Error loading leaderboard.", Toast.LENGTH_SHORT).show();
                        emptyTextView.setText("Could not load leaderboard.");
                        emptyTextView.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
            podiumLayout.setVisibility(View.GONE);
            leaderboardRecyclerView.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void updatePodium(List<LeaderboardUser> allUsers) {
        if (allUsers.size() > 0) {
            LeaderboardUser first = allUsers.get(0);
            firstPlaceName.setText(first.getName());
            firstPlaceXp.setText(first.getXpLevel() + " XP");
            setProfileImage(first.getProfileImageBase64(), firstPlaceImage);
        }
        if (allUsers.size() > 1) {
            LeaderboardUser second = allUsers.get(1);
            secondPlaceName.setText(second.getName());
            secondPlaceXp.setText(second.getXpLevel() + " XP");
            setProfileImage(second.getProfileImageBase64(), secondPlaceImage);
        }
        if (allUsers.size() > 2) {
            LeaderboardUser third = allUsers.get(2);
            thirdPlaceName.setText(third.getName());
            thirdPlaceXp.setText(third.getXpLevel() + " XP");
            setProfileImage(third.getProfileImageBase64(), thirdPlaceImage);
        }
    }

    private void updateRecyclerView(List<LeaderboardUser> allUsers) {
        userListForRecycler.clear();
        if (allUsers.size() > 3) {
            userListForRecycler.addAll(allUsers.subList(3, allUsers.size()));
        }
        adapter.notifyDataSetChanged();
    }

    private void setProfileImage(String base64, CircleImageView imageView) {
        if (base64 != null && !base64.isEmpty() && getContext() != null) {
            byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.ic_account_circle);
        }
    }
}