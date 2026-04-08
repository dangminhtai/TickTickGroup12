package hcmute.edu.vn.tick_tick.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.util.ThemeUtil;
import hcmute.edu.vn.tick_tick.util.UiPreferences;

public class SettingsFragment extends Fragment {

    private SwitchMaterial darkModeSwitch;
    private SwitchMaterial nextUpSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupDarkMode(view);
        setupColorSelection(view);
        setupNextUpToggle(view);

        TextView tvVersion = view.findViewById(R.id.tv_version_name);
        tvVersion.setText(getVersionName());
    }

    private void setupDarkMode(View view) {
        View darkModeSetting = view.findViewById(R.id.setting_dark_mode);
        darkModeSwitch = view.findViewById(R.id.switch_dark_mode);

        darkModeSwitch.setChecked(ThemeUtil.isDarkTheme(requireContext()));
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeUtil.setDarkTheme(requireContext(), isChecked);
            requireActivity().recreate();
        });
        darkModeSetting.setOnClickListener(v -> darkModeSwitch.toggle());
    }

    private void setupNextUpToggle(View view) {
        View nextUpSetting = view.findViewById(R.id.setting_next_up_card);
        nextUpSwitch = view.findViewById(R.id.switch_next_up_card);

        if (nextUpSwitch != null && nextUpSetting != null) {
            boolean enabled = UiPreferences.isNextUpCardEnabled(requireContext());
            nextUpSwitch.setChecked(enabled);

            nextUpSwitch.setOnCheckedChangeListener((button, isChecked) -> {
                UiPreferences.setNextUpCardEnabled(requireContext(), isChecked);
            });

            nextUpSetting.setOnClickListener(v -> nextUpSwitch.toggle());
        }
    }

    private void setupColorSelection(View view) {
        View.OnClickListener listener = v -> {
            String theme = ThemeUtil.THEME_INDIGO;
            int id = v.getId();
            if (id == R.id.color_green) {
                theme = ThemeUtil.THEME_GREEN;
            } else if (id == R.id.color_red) {
                theme = ThemeUtil.THEME_RED;
            } else if (id == R.id.color_orange) {
                theme = ThemeUtil.THEME_ORANGE;
            }

            ThemeUtil.setTheme(requireContext(), theme);
            Toast.makeText(getContext(), getString(R.string.theme_change_toast), Toast.LENGTH_SHORT).show();
            requireActivity().recreate();
        };

        view.findViewById(R.id.color_indigo).setOnClickListener(listener);
        view.findViewById(R.id.color_green).setOnClickListener(listener);
        view.findViewById(R.id.color_red).setOnClickListener(listener);
        view.findViewById(R.id.color_orange).setOnClickListener(listener);

        view.findViewById(R.id.setting_accent_color).setOnClickListener(v ->
                Toast.makeText(getContext(), R.string.accent_color, Toast.LENGTH_SHORT).show());
    }

    private String getVersionName() {
        try {
            return requireActivity().getPackageManager()
                    .getPackageInfo(requireActivity().getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "1.0";
        }
    }
}
