package hcmute.edu.vn.tick_tick;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import hcmute.edu.vn.tick_tick.ui.fragment.AllTasksFragment;
import hcmute.edu.vn.tick_tick.ui.fragment.CalendarFragment;
import hcmute.edu.vn.tick_tick.ui.fragment.DiscoverFragment;
import hcmute.edu.vn.tick_tick.ui.fragment.HomeFragment;
import hcmute.edu.vn.tick_tick.ui.fragment.InboxFragment;
import hcmute.edu.vn.tick_tick.ui.fragment.PomodoroFragment;
import hcmute.edu.vn.tick_tick.ui.fragment.SettingsFragment;
import hcmute.edu.vn.tick_tick.ui.fragment.StatsFragment;
import hcmute.edu.vn.tick_tick.ui.sheet.AddTaskBottomSheet;
import hcmute.edu.vn.tick_tick.viewmodel.TaskViewModel;
import hcmute.edu.vn.tick_tick.util.ThemeUtil;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private TaskViewModel viewModel;
    private int currentNavItem = R.id.nav_today;

    // Launcher for notification permission
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Đã bật thông báo nhắc nhở!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Thông báo bị từ chối. Bạn sẽ không nhận được nhắc nhở.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtil.applyTheme(this);
        ThemeUtil.applyNightMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        ExtendedFloatingActionButton fab = findViewById(R.id.fab_add_task);
        CoordinatorLayout coordinator = findViewById(R.id.coordinator);

        // Request notification permission for Android 13+
        askNotificationPermission();

        // Ensure FAB is above other views and receives clicks
        fab.setClickable(true);
        fab.post(() -> {
            fab.bringToFront();
            View parent = (View) fab.getParent();
            if (parent != null) parent.invalidate();
        });

        // Handle window insets (navigation bar) so FAB is always fully visible
        final CoordinatorLayout.LayoutParams fabLp = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        final int baseFabBottom = fabLp.bottomMargin;

        ViewCompat.setOnApplyWindowInsetsListener(coordinator, (v, insets) -> {
            Insets navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            int extraOffset = (int) (getResources().getDisplayMetrics().density * 8); // 8dp
            fabLp.setMargins(fabLp.leftMargin, fabLp.topMargin, fabLp.rightMargin, baseFabBottom + navInsets.bottom + extraOffset);
            fab.setLayoutParams(fabLp);
            return insets;
        });
        ViewCompat.requestApplyInsets(coordinator);

        // Drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.nav_my_lists, R.string.nav_my_lists);
        toggle.getDrawerArrowDrawable().setColor(getColor(R.color.white));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // Observer for task count badge
        viewModel.getTodayTaskCount().observe(this, count -> {
            if (count != null && count > 0) {
                bottomNavigationView.getOrCreateBadge(R.id.nav_today).setNumber(count);
            } else {
                bottomNavigationView.removeBadge(R.id.nav_today);
            }
        });

        // Bottom nav listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_today) {
                loadFragment(new HomeFragment(), getString(R.string.nav_today));
                viewModel.setSelectedDate(System.currentTimeMillis());
            } else if (id == R.id.nav_all_tasks) {
                loadFragment(new AllTasksFragment(), getString(R.string.nav_all_tasks));
                viewModel.setSelectedDate(0);
            } else if (id == R.id.nav_calendar) {
                loadFragment(new CalendarFragment(), getString(R.string.calendar));
            } else if (id == R.id.nav_stats) {
                loadFragment(new StatsFragment(), "Thống kê 📊");
                viewModel.setSelectedDate(0);
            }
            currentNavItem = id;
            return true;
        });

        // FAB opens Add Task bottom sheet
        fab.setOnClickListener(v -> {
            AddTaskBottomSheet sheet = new AddTaskBottomSheet();
            sheet.show(getSupportFragmentManager(), AddTaskBottomSheet.TAG);
        });

        // Default: Home fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), getString(R.string.nav_today));
            bottomNavigationView.setSelectedItemId(R.id.nav_today);
            viewModel.setSelectedDate(System.currentTimeMillis());
        }

        // Back gesture: close drawer if open, otherwise defer to system
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void loadFragment(Fragment fragment, String title) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .commit();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.drawer_today) {
            loadFragment(new HomeFragment(), getString(R.string.nav_today));
            bottomNavigationView.setSelectedItemId(R.id.nav_today);
            viewModel.setSelectedDate(System.currentTimeMillis());
        } else if (id == R.id.drawer_all_tasks) {
            loadFragment(new AllTasksFragment(), getString(R.string.nav_all_tasks));
            bottomNavigationView.setSelectedItemId(R.id.nav_all_tasks);
            viewModel.setSelectedDate(0);
        } else if (id == R.id.drawer_inbox) {
            loadFragment(new InboxFragment(), getString(R.string.inbox));
            bottomNavigationView.setSelectedItemId(R.id.nav_all_tasks);
            viewModel.setSelectedDate(0);
        } else if (id == R.id.drawer_upcoming) {
            loadFragment(new AllTasksFragment(), getString(R.string.nav_upcoming));
            viewModel.setSelectedDate(0);
        } else if (id == R.id.drawer_pomodoro) {
            loadFragment(new PomodoroFragment(), "Pomodoro 🍅");
        } else if (id == R.id.drawer_discover) {
            loadFragment(new DiscoverFragment(), "Thông tin ứng dụng ℹ️");
        } else if (id == R.id.drawer_settings) {
            loadFragment(new SettingsFragment(), "Cài đặt ⚙️");
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
