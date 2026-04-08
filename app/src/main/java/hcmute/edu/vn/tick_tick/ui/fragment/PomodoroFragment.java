package hcmute.edu.vn.tick_tick.ui.fragment;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import com.google.android.material.chip.Chip;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import hcmute.edu.vn.tick_tick.R;

public class PomodoroFragment extends Fragment {

    private static final int WORK_MINUTES = 25;
    private static final int SHORT_BREAK_MINUTES = 5;
    private static final int LONG_BREAK_MINUTES = 15;
    private static final int SESSIONS_BEFORE_LONG_BREAK = 4;

    private TextView tvTimer, tvSessionCount, tvMode, tvTip;
    private MaterialButton btnStart, btnReset, btnSkip;
    private CircularProgressIndicator progressBar;
    private ChipGroup chipGroup;

    private CountDownTimer timer;
    private boolean isRunning = false;
    private int currentMode = 0; // 0=work, 1=short, 2=long
    private int sessionsDone = 0;
    private long remainingMillis;
    private long totalMillis;
    private MediaPlayer mediaPlayer;
    private Ringtone endRingtone;
    private int selectedSoundId = 1; // default rain
    private Chip chipNone, chipRain, chipCafe, chipLofi, chipNature;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pomodoro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTimer = view.findViewById(R.id.tv_timer);
        tvSessionCount = view.findViewById(R.id.tv_session_count);
        tvMode = view.findViewById(R.id.tv_pomodoro_mode);
        tvTip = view.findViewById(R.id.tv_pomodoro_tip);
        tvTip.setText(R.string.pomodoro_tip);
        progressBar = view.findViewById(R.id.pomodoro_progress);
        btnStart = view.findViewById(R.id.btn_pomodoro_start);
        btnReset = view.findViewById(R.id.btn_pomodoro_reset);
        btnSkip = view.findViewById(R.id.btn_pomodoro_skip);
        chipGroup = view.findViewById(R.id.chip_group_pomodoro);

        setMode(0);

        btnStart.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnReset.setOnClickListener(v -> resetTimer());

        btnSkip.setOnClickListener(v -> skipToNext());

        chipGroup.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            int id = ids.get(0);
            if (id == R.id.chip_work) setMode(0);
            else if (id == R.id.chip_short_break) setMode(1);
            else if (id == R.id.chip_long_break) setMode(2);
        });

        setupSoundControls(view);
    }

    private void setupSoundControls(View view) {
        chipNone = view.findViewById(R.id.chip_sound_none);
        chipRain = view.findViewById(R.id.chip_sound_rain);
        chipCafe = view.findViewById(R.id.chip_sound_cafe);
        chipLofi = view.findViewById(R.id.chip_sound_lofi);
        chipNature = view.findViewById(R.id.chip_sound_nature);

        View.OnClickListener listener = v -> {
            int id = v.getId();
            int soundId = -1;
            if (id == R.id.chip_sound_rain) soundId = 1;
            else if (id == R.id.chip_sound_cafe) soundId = 2;
            else if (id == R.id.chip_sound_lofi) soundId = 3;
            else if (id == R.id.chip_sound_nature) soundId = 4;
            selectSound(soundId);
        };

        chipNone.setOnClickListener(listener);
        chipRain.setOnClickListener(listener);
        chipCafe.setOnClickListener(listener);
        chipLofi.setOnClickListener(listener);
        chipNature.setOnClickListener(listener);

        // Default to rain to avoid silent start
        selectSound(1);
    }

    private void selectSound(int soundId) {
        selectedSoundId = soundId;
        chipNone.setChecked(soundId == -1);
        chipRain.setChecked(soundId == 1);
        chipCafe.setChecked(soundId == 2);
        chipLofi.setChecked(soundId == 3);
        chipNature.setChecked(soundId == 4);

        stopSound();
        if (isRunning && selectedSoundId != -1) {
            playSound();
        }
    }

    private void setMode(int mode) {
        currentMode = mode;
        cancelTimer();
        isRunning = false;

        int minutes;
        String modeLabel;
        switch (mode) {
            case 1:
                minutes = SHORT_BREAK_MINUTES;
                modeLabel = getString(R.string.pomodoro_mode_short_break);
                chipGroup.check(R.id.chip_short_break);
                break;
            case 2:
                minutes = LONG_BREAK_MINUTES;
                modeLabel = getString(R.string.pomodoro_mode_long_break);
                chipGroup.check(R.id.chip_long_break);
                break;
            default:
                minutes = WORK_MINUTES;
                modeLabel = getString(R.string.pomodoro_mode_focus);
                chipGroup.check(R.id.chip_work);
                break;
        }

        totalMillis = minutes * 60 * 1000L;
        remainingMillis = totalMillis;
        tvMode.setText(modeLabel);
        updateTimerDisplay(remainingMillis);
        progressBar.setProgress(100);
        btnStart.setText(getString(R.string.pomodoro_start));
        updateSessionLabel();
    }

    private void startTimer() {
        isRunning = true;
        btnStart.setText(getString(R.string.pomodoro_pause));
        playSound();

        timer = new CountDownTimer(remainingMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMillis = millisUntilFinished;
                updateTimerDisplay(millisUntilFinished);
                int progress = (int) ((millisUntilFinished * 100) / totalMillis);
                progressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                isRunning = false;
                remainingMillis = 0;
                updateTimerDisplay(0);
                progressBar.setProgress(0);
                playEndTone();
                onSessionFinished();
            }
        }.start();
    }

    private void pauseTimer() {
        cancelTimer();
        isRunning = false;
        btnStart.setText(getString(R.string.pomodoro_resume));
        stopSound();
    }

    private void resetTimer() {
        cancelTimer();
        isRunning = false;
        remainingMillis = totalMillis;
        updateTimerDisplay(remainingMillis);
        progressBar.setProgress(100);
        btnStart.setText(getString(R.string.pomodoro_start));
        stopSound();
    }

    private void skipToNext() {
        cancelTimer();
        onSessionFinished();
    }

    private void onSessionFinished() {
        if (currentMode == 0) {
            sessionsDone++;
            if (sessionsDone % SESSIONS_BEFORE_LONG_BREAK == 0) {
                setMode(2);
            } else {
                setMode(1);
            }
        } else {
            setMode(0);
        }
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void updateTimerDisplay(long millis) {
        long minutes = millis / 60000;
        long seconds = (millis % 60000) / 1000;
        tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void updateSessionLabel() {
        int session = (sessionsDone % SESSIONS_BEFORE_LONG_BREAK) + 1;
        tvSessionCount.setText(getString(R.string.pomodoro_session_label, session, SESSIONS_BEFORE_LONG_BREAK));
    }

    private void playSound() {
        if (selectedSoundId == -1 || !isRunning) {
            stopSound();
            return;
        }
        
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) return;
                mediaPlayer.start();
            } else {
                int resId = 0;
                switch (selectedSoundId) {
                    case 1: resId = R.raw.sound_rain; break;
                    case 2: resId = R.raw.sound_cafe; break;
                    case 3: resId = R.raw.sound_lofi; break;
                    case 4: resId = R.raw.sound_nature; break;
                }
                
                if (resId != 0) {
                    mediaPlayer = MediaPlayer.create(getContext(), resId);
                    if (mediaPlayer != null) {
                        mediaPlayer.setLooping(true);
                        mediaPlayer.start();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Pomodoro", "Error playing sound", e);
        }
    }

    private void stopSound() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopEndTone();
    }

    private void playEndTone() {
        try {
            stopEndTone();
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (uri == null) {
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            if (uri == null) return;
            endRingtone = RingtoneManager.getRingtone(requireContext(), uri);
            if (endRingtone != null) {
                endRingtone.play();
            }
        } catch (Exception e) {
            Log.e("Pomodoro", "Error playing end tone", e);
        }
    }

    private void stopEndTone() {
        if (endRingtone != null && endRingtone.isPlaying()) {
            endRingtone.stop();
        }
        endRingtone = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelTimer();
        stopSound();
    }
}
