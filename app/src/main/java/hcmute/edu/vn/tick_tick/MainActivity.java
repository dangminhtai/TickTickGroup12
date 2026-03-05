package hcmute.edu.vn.tick_tick;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import hcmute.edu.vn.tick_tick.ui.fragment.AllTasksFragment;
import hcmute.edu.vn.tick_tick.ui.fragment.CalendarFragment;
import hcmute.edu.vn.tick_tick.ui.fragment.HomeFragment;
import hcmute.edu.vn.tick_tick.ui.fragment.InboxFragment;
import hcmute.edu.vn.tick_tick.ui.fragment.SettingsFragment;
import hcmute.edu.vn.tick_tick.ui.sheet.AddTaskBottomSheet;
import hcmute.edu.vn.tick_tick.viewmodel.TaskViewModel;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private TaskViewModel viewModel;
    private int currentNavItem = R.id.nav_today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        FloatingActionButton fab = findViewById(R.id.fab_add_task);

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
            View badgeView = bottomNavigationView.findViewById(R.id.nav_today);
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
            } else if (id == R.id.nav_all_tasks) {
                loadFragment(new AllTasksFragment(), getString(R.string.nav_all_tasks));
            } else if (id == R.id.nav_calendar) {
                loadFragment(new CalendarFragment(), getString(R.string.calendar));
            } else if (id == R.id.nav_inbox) {
                loadFragment(new InboxFragment(), getString(R.string.inbox));
            } else if (id == R.id.nav_settings) {
                loadFragment(new SettingsFragment(), getString(R.string.settings));
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
        }

        // Back gesture: close drawer if open, otherwise defer to system
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);          // disable this callback
                    getOnBackPressedDispatcher().onBackPressed(); // let system handle
                    setEnabled(true);           // re-enable for next time
                }
            }
        });
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
        } else if (id == R.id.drawer_all_tasks) {
            loadFragment(new AllTasksFragment(), getString(R.string.nav_all_tasks));
            bottomNavigationView.setSelectedItemId(R.id.nav_all_tasks);
        } else if (id == R.id.drawer_inbox) {
            loadFragment(new InboxFragment(), getString(R.string.inbox));
            bottomNavigationView.setSelectedItemId(R.id.nav_inbox);
        } else if (id == R.id.drawer_upcoming) {
            loadFragment(new AllTasksFragment(), getString(R.string.nav_upcoming));
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

}