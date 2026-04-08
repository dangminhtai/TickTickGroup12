package hcmute.edu.vn.tick_tick.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.data.database.AppDatabase;
import hcmute.edu.vn.tick_tick.util.XPManager;
import hcmute.edu.vn.tick_tick.viewmodel.TaskViewModel;

public class StatsFragment extends Fragment {

    private TaskViewModel viewModel;
    private XPManager xpManager;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private TextView tvLevel, tvLevelTitle, tvTotalXP, tvXPLabel, tvStreak, tvTotalCompleted, tvHighPriority;
    private LinearProgressIndicator progressXP;
    private BarChart chartWeekly;
    private LinearLayout achievementsContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        xpManager = new XPManager(requireContext());
        db = AppDatabase.getInstance(requireContext());

        tvLevel = view.findViewById(R.id.tv_level);
        tvLevelTitle = view.findViewById(R.id.tv_level_title);
        tvTotalXP = view.findViewById(R.id.tv_total_xp);
        tvXPLabel = view.findViewById(R.id.tv_xp_progress_label);
        progressXP = view.findViewById(R.id.progress_xp);
        tvStreak = view.findViewById(R.id.tv_streak);
        tvTotalCompleted = view.findViewById(R.id.tv_total_completed);
        tvHighPriority = view.findViewById(R.id.tv_high_priority_done);
        chartWeekly = view.findViewById(R.id.chart_weekly);
        achievementsContainer = view.findViewById(R.id.achievements_container);

        loadXPSection();
        loadStats();
        loadWeeklyChart();
        loadAchievements();
    }

    private void loadXPSection() {
        int level = xpManager.getLevel();
        int totalXP = xpManager.getTotalXP();
        int progress = xpManager.getLevelProgress();
        int xpToNext = xpManager.getXPForNextLevel();
        int streak = xpManager.getStreakDays();

        tvLevel.setText("Cấp độ " + level);
        tvLevelTitle.setText(xpManager.getLevelTitle());
        tvTotalXP.setText(totalXP + " XP");
        tvXPLabel.setText(progress + "/100 XP tới cấp " + (level + 1));
        progressXP.setProgress(progress);
        tvStreak.setText(String.valueOf(streak == 0 ? 1 : streak));
    }

    private void loadStats() {
        db.taskDao().getTotalCompletedCount().observe(getViewLifecycleOwner(), count -> {
            tvTotalCompleted.setText(String.valueOf(count != null ? count : 0));
        });

        db.taskDao().getHighPriorityCompletedCount().observe(getViewLifecycleOwner(), count -> {
            tvHighPriority.setText(String.valueOf(count != null ? count : 0));
        });
    }

    private void loadWeeklyChart() {
        executor.execute(() -> {
            List<BarEntry> entries = new ArrayList<>();
            String[] labels = new String[7];
            SimpleDateFormat sdf = new SimpleDateFormat("E", Locale.getDefault());
            Calendar cal = Calendar.getInstance();

            for (int i = 6; i >= 0; i--) {
                cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_YEAR, -i);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long start = cal.getTimeInMillis();
                long end = start + 86400000L;

                int count = db.taskDao().getCompletedCountBetween(start, end);
                entries.add(new BarEntry(6 - i, count));
                labels[6 - i] = sdf.format(cal.getTime());
            }

            if (getView() == null) return;
            requireActivity().runOnUiThread(() -> renderChart(entries, labels));
        });
    }

    private void renderChart(List<BarEntry> entries, String[] labels) {
        BarDataSet dataSet = new BarDataSet(entries, "Công việc hoàn thành");
        dataSet.setColor(requireContext().getColor(R.color.color_primary));
        dataSet.setValueTextColor(requireContext().getColor(R.color.color_text_primary));
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(true);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        chartWeekly.setData(barData);
        chartWeekly.setFitBars(true);
        chartWeekly.getDescription().setEnabled(false);
        chartWeekly.getLegend().setEnabled(false);
        chartWeekly.setDrawGridBackground(false);
        chartWeekly.setDrawBarShadow(false);
        chartWeekly.getAxisLeft().setTextColor(requireContext().getColor(R.color.color_text_secondary));
        chartWeekly.getAxisLeft().setGridColor(requireContext().getColor(R.color.color_surface_variant));
        chartWeekly.getAxisLeft().setAxisMinimum(0f);
        chartWeekly.getAxisLeft().setGranularity(1f);
        chartWeekly.getAxisRight().setEnabled(false);

        XAxis xAxis = chartWeekly.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(requireContext().getColor(R.color.color_text_secondary));
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        chartWeekly.setBackgroundColor(Color.TRANSPARENT);
        chartWeekly.setTouchEnabled(false);
        chartWeekly.animateY(800);
        chartWeekly.invalidate();
    }

    private void loadAchievements() {
        int totalXP = xpManager.getTotalXP();
        int level = xpManager.getLevel();
        int streak = Math.max(1, xpManager.getStreakDays());

        addAchievement("🏅 Khởi đầu tốt", "Hoàn thành task đầu tiên", totalXP >= XPManager.XP_NONE);
        addAchievement("⚡ Cấp 5", "Đạt cấp độ 5", level >= 5);
        addAchievement("🔥 Chuỗi 3 ngày", "Hoàn thành task 3 ngày liên tiếp", streak >= 3);
        addAchievement("🔥 Chuỗi 7 ngày", "7 ngày liên tiếp không bỏ lỡ", streak >= 7);
        addAchievement("💎 Cấp 10", "Đạt cấp độ 10", level >= 10);
        addAchievement("🎖️ 500 XP", "Tích lũy 500 điểm kinh nghiệm", totalXP >= 500);
        addAchievement("🚀 Cấp 20", "Trở thành Bậc thầy – cấp độ 20", level >= 20);
        addAchievement("👑 1000 XP", "Tích lũy 1000 XP – Huyền thoại!", totalXP >= 1000);
    }

    private void addAchievement(String title, String desc, boolean unlocked) {
        if (getView() == null) return;

        View item = LayoutInflater.from(requireContext())
                .inflate(android.R.layout.simple_list_item_2, achievementsContainer, false);

        TextView tvTitle = item.findViewById(android.R.id.text1);
        TextView tvDesc = item.findViewById(android.R.id.text2);

        tvTitle.setText((unlocked ? "" : "🔒 ") + title);
        tvTitle.setTextColor(requireContext().getColor(
                unlocked ? R.color.color_text_primary : R.color.color_text_hint));
        tvTitle.setTextSize(15f);

        tvDesc.setText(desc);
        tvDesc.setTextColor(requireContext().getColor(
                unlocked ? R.color.color_primary : R.color.color_text_hint));
        tvDesc.setTextSize(12f);
        tvDesc.setAlpha(unlocked ? 1f : 0.5f);

        item.setAlpha(unlocked ? 1f : 0.5f);

        achievementsContainer.addView(item);

        // Divider
        if (achievementsContainer.getChildCount() < 8 * 2) {
            View divider = new View(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
            lp.setMargins(0, 4, 0, 4);
            divider.setLayoutParams(lp);
            divider.setBackgroundColor(requireContext().getColor(R.color.color_surface_variant));
            achievementsContainer.addView(divider);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executor.shutdown();
    }
}
