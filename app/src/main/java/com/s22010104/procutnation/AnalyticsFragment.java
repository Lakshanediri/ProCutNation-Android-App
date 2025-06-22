package com.s22010104.procutnation;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsFragment extends Fragment {

    private BarChart weeklyBarChart, dailyBarChart;
    private PieChart pieChart;
    private TextView barChartEmptyText, pieChartEmptyText, dailyBarChartEmptyText;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static final String TAG = "AnalyticsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = view.findViewById(R.id.toolbar_analytics);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        weeklyBarChart = view.findViewById(R.id.barChart);
        dailyBarChart = view.findViewById(R.id.dailyBarChart);
        pieChart = view.findViewById(R.id.pieChart);
        barChartEmptyText = view.findViewById(R.id.barChartEmptyText);
        pieChartEmptyText = view.findViewById(R.id.pieChartEmptyText);
        dailyBarChartEmptyText = view.findViewById(R.id.dailyBarChartEmptyText);

        setupCharts();
        loadAnalyticsData();

        return view;
    }

    private void setupCharts() {
        setupBarChartStyle(weeklyBarChart);
        setupBarChartStyle(dailyBarChart);

        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.setHoleRadius(45f);
        Legend legend = pieChart.getLegend();
        legend.setTextColor(Color.WHITE);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        pieChart.animateY(1500);
    }

    private void setupBarChartStyle(BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisLeft().setAxisMinimum(0f);
        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        chart.setDrawGridBackground(false);
        chart.animateY(1500);
    }

    private void loadAnalyticsData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        Date startOfWeek = cal.getTime();

        db.collection("pomodoro_sessions")
                .whereEqualTo("userId", currentUser.getUid())
                .whereGreaterThanOrEqualTo("completionTimestamp", startOfWeek)
                .orderBy("completionTimestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> processWeeklyData(queryDocumentSnapshots.getDocuments()))
                .addOnFailureListener(e -> handleFailure(e, "weekly"));

        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startOfDay = cal.getTime();

        cal.add(Calendar.DAY_OF_YEAR, 1);
        Date endOfDay = cal.getTime();

        db.collection("pomodoro_sessions")
                .whereEqualTo("userId", currentUser.getUid())
                .whereGreaterThanOrEqualTo("completionTimestamp", startOfDay)
                .whereLessThan("completionTimestamp", endOfDay)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> processDailyData(queryDocumentSnapshots.getDocuments()))
                .addOnFailureListener(e -> handleFailure(e, "daily"));
    }

    private void processWeeklyData(List<DocumentSnapshot> documents) {
        if (documents.isEmpty()) {
            weeklyBarChart.setVisibility(View.GONE);
            pieChart.setVisibility(View.GONE);
            barChartEmptyText.setVisibility(View.VISIBLE);
            pieChartEmptyText.setVisibility(View.VISIBLE);
            return;
        }

        weeklyBarChart.setVisibility(View.VISIBLE);
        pieChart.setVisibility(View.VISIBLE);
        barChartEmptyText.setVisibility(View.GONE);
        pieChartEmptyText.setVisibility(View.GONE);

        Map<Integer, Float> dailyMinutes = new HashMap<>();
        Map<String, Float> projectMinutes = new HashMap<>();
        for (int i = 0; i < 7; i++) dailyMinutes.put(i, 0f);

        for (DocumentSnapshot doc : documents) {
            PomodoroSession session = doc.toObject(PomodoroSession.class);
            if (session.getCompletionTimestamp() == null) continue;

            Calendar cal = Calendar.getInstance();
            cal.setTime(session.getCompletionTimestamp());
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
            dailyMinutes.put(dayOfWeek, dailyMinutes.getOrDefault(dayOfWeek, 0f) + session.getMinutesCompleted());

            String projectName = session.getProjectName() != null ? session.getProjectName() : "Other";
            projectMinutes.put(projectName, projectMinutes.getOrDefault(projectName, 0f) + session.getMinutesCompleted());
        }
        populateWeeklyBarChart(dailyMinutes);
        populatePieChart(projectMinutes);
    }

    private void processDailyData(List<DocumentSnapshot> documents) {
        if (documents.isEmpty()) {
            dailyBarChart.setVisibility(View.GONE);
            dailyBarChartEmptyText.setVisibility(View.VISIBLE);
            return;
        }

        dailyBarChart.setVisibility(View.VISIBLE);
        dailyBarChartEmptyText.setVisibility(View.GONE);

        Map<Integer, Float> hourlyMinutes = new HashMap<>();
        for(int i = 0; i < 24; i++) hourlyMinutes.put(i, 0f);

        for (DocumentSnapshot doc : documents) {
            PomodoroSession session = doc.toObject(PomodoroSession.class);
            if(session.getCompletionTimestamp() == null) continue;

            Calendar cal = Calendar.getInstance();
            cal.setTime(session.getCompletionTimestamp());
            int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
            hourlyMinutes.put(hourOfDay, hourlyMinutes.getOrDefault(hourOfDay, 0f) + session.getMinutesCompleted());
        }
        populateDailyBarChart(hourlyMinutes);
    }

    private void handleFailure(Exception e, String chartType) {
        Log.e(TAG, "Error loading " + chartType + " data: ", e);
        Toast.makeText(getContext(), "Error: Check Logcat for Firestore index link.", Toast.LENGTH_LONG).show();
        if (chartType.equals("weekly")) {
            weeklyBarChart.setVisibility(View.GONE);
            pieChart.setVisibility(View.GONE);
            barChartEmptyText.setText("Error loading data.");
            pieChartEmptyText.setText("Error loading data.");
            barChartEmptyText.setVisibility(View.VISIBLE);
            pieChartEmptyText.setVisibility(View.VISIBLE);
        } else if (chartType.equals("daily")) {
            dailyBarChart.setVisibility(View.GONE);
            dailyBarChartEmptyText.setText("Error loading data.");
            dailyBarChartEmptyText.setVisibility(View.VISIBLE);
        }
    }

    private void populateWeeklyBarChart(Map<Integer, Float> dailyMinutes) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        String[] days = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, dailyMinutes.getOrDefault(i, 0f)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Daily Focus (Minutes)");
        dataSet.setColor(Color.parseColor("#03A9F4"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        weeklyBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(days));
        weeklyBarChart.setData(barData);
        weeklyBarChart.invalidate();
    }

    private void populateDailyBarChart(Map<Integer, Float> hourlyMinutes) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        String[] hours = new String[24];
        for (int i = 0; i < 24; i++) {
            entries.add(new BarEntry(i, hourlyMinutes.getOrDefault(i, 0f)));
            hours[i] = i + ":00";
        }

        BarDataSet dataSet = new BarDataSet(entries, "Hourly Focus (Minutes)");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);

        dailyBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(hours));
        dailyBarChart.getXAxis().setLabelCount(6);
        dailyBarChart.setData(barData);
        dailyBarChart.invalidate();
    }

    private void populatePieChart(Map<String, Float> projectMinutes) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : projectMinutes.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(14f);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));
        dataSet.setSliceSpace(2f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }
}

