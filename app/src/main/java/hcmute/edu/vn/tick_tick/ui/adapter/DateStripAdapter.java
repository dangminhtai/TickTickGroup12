package hcmute.edu.vn.tick_tick.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.tick_tick.R;

public class DateStripAdapter extends RecyclerView.Adapter<DateStripAdapter.DateViewHolder> {

    private final List<Date> dates = new ArrayList<>();
    private final SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", new Locale("vi", "VN"));
    private final SimpleDateFormat dayNumberFormat = new SimpleDateFormat("d", Locale.getDefault());
    private long selectedDateMs;
    private OnDateSelectedListener listener;

    public interface OnDateSelectedListener {
        void onDateSelected(Date date);
    }

    public DateStripAdapter(OnDateSelectedListener listener) {
        this.listener = listener;
        generateDates();
        selectedDateMs = truncateTime(new Date()).getTime();
    }

    private void generateDates() {
        Calendar cal = Calendar.getInstance();
        // Reset time to start of day to ensure consistent date comparisons
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_YEAR, -14); // Start 2 weeks ago
        for (int i = 0; i < 60; i++) { // Show 2 months
            dates.add(cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    public void setSelectedDate(long dateMs) {
        this.selectedDateMs = truncateTime(new Date(dateMs)).getTime();
        notifyDataSetChanged();
    }

    private Date truncateTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_strip, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        Date date = dates.get(position);
        holder.bind(date);
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    public int getPositionForDate(long dateMs) {
        long target = truncateTime(new Date(dateMs)).getTime();
        for (int i = 0; i < dates.size(); i++) {
            if (truncateTime(dates.get(i)).getTime() == target) return i;
        }
        return -1;
    }

    class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName, tvDayNumber;
        View dot;
        MaterialCardView card;

        DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tv_day_name);
            tvDayNumber = itemView.findViewById(R.id.tv_day_number);
            dot = itemView.findViewById(R.id.dot_indicator);
            card = itemView.findViewById(R.id.card_date);
        }

        void bind(Date date) {
            tvDayName.setText(dayNameFormat.format(date));
            tvDayNumber.setText(dayNumberFormat.format(date));
            
            long time = truncateTime(date).getTime();
            boolean isSelected = (time == selectedDateMs);
            
            if (isSelected) {
                card.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.color_primary));
                card.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.color_surface_variant));
                tvDayNumber.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.color_primary));
                dot.setVisibility(View.VISIBLE);
            } else {
                card.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.color_divider));
                card.setCardBackgroundColor(Color.TRANSPARENT);
                tvDayNumber.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.color_text_primary));
                dot.setVisibility(View.INVISIBLE);
            }
            
            // Use truncated time to ensure consistent date selection
            final long truncatedTime = time;
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDateSelected(new Date(truncatedTime));
                }
                setSelectedDate(truncatedTime);
            });
        }
    }
}
