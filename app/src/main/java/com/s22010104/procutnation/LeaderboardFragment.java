package com.s22010104.procutnation;

import android.os.Bundle;
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

public class LeaderboardFragment extends Fragment {

    private RecyclerView leaderboardRecyclerView;
    private LeaderboardAdapter adapter;
    private List<LeaderboardUser> userListForRecycler;
    private FirebaseFirestore db;

    // UI elements
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private ConstraintLayout podiumLayout;

    // Podium Views
    private TextView firstPlaceName, secondPlaceName, thirdPlaceName;
    private TextView firstPlaceXp, secondPlaceXp, thirdPlaceXp;

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

        // Initialize UI elements
        progressBar = view.findViewById(R.id.progressBarLeaderboard);
        emptyTextView = view.findViewById(R.id.textViewEmpty);
        podiumLayout = view.findViewById(R.id.podiumLayout);

        // Initialize podium views
        firstPlaceName = view.findViewById(R.id.firstPlaceName);
        secondPlaceName = view.findViewById(R.id.secondPlaceName);
        thirdPlaceName = view.findViewById(R.id.thirdPlaceName);
        firstPlaceXp = view.findViewById(R.id.firstPlaceXp);
        secondPlaceXp = view.findViewById(R.id.secondPlaceXp);
        thirdPlaceXp = view.findViewById(R.id.thirdPlaceXp);

        leaderboardRecyclerView = view.findViewById(R.id.leaderboardRecyclerView);
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LeaderboardAdapter(getContext(), userListForRecycler);
        leaderboardRecyclerView.setAdapter(adapter);

        loadLeaderboardData();

        return view;
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
                            allUsers.add(document.toObject(LeaderboardUser.class));
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
                        Toast.makeText(getContext(), "Error loading leaderboard. Check logs for details.", Toast.LENGTH_SHORT).show();
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
            firstPlaceName.setText(allUsers.get(0).getName());
            firstPlaceXp.setText(allUsers.get(0).getXpLevel() + " XP");
        }
        if (allUsers.size() > 1) {
            secondPlaceName.setText(allUsers.get(1).getName());
            secondPlaceXp.setText(allUsers.get(1).getXpLevel() + " XP");
        }
        if (allUsers.size() > 2) {
            thirdPlaceName.setText(allUsers.get(2).getName());
            thirdPlaceXp.setText(allUsers.get(2).getXpLevel() + " XP");
        }
    }

    private void updateRecyclerView(List<LeaderboardUser> allUsers) {
        userListForRecycler.clear();
        if (allUsers.size() > 3) {
            userListForRecycler.addAll(allUsers.subList(3, allUsers.size()));
        }
        adapter.notifyDataSetChanged();
    }
}