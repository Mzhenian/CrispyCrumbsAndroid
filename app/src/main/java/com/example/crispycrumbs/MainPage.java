package com.example.crispycrumbs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MainPage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private static final String THEME_PREF_KEY = "app_theme"; // Preference key for theme

    // Handle options item selection, particularly the navigation drawer toggle
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Activity creation and initialization
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this); // Enable edge-to-edge display
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_main); // Set the content view to the main page layout

        Toolbar toolbar = findViewById(R.id.toolbar); // Find the toolbar
        setSupportActionBar(toolbar); // Set the toolbar as the app bar

        drawerLayout = findViewById(R.id.drawer_layout); // Find the drawer layout
        NavigationView navigationView = findViewById(R.id.nav_Bar); // Find the navigation view

        navigationView.setNavigationItemSelectedListener(this); // Set navigation item selection listener

        // Set up the navigation drawer toggle
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle); // Add the drawer listener to the drawer layout
        drawerToggle.syncState(); // Synchronize the state of the drawer toggle

        // Load theme preference and apply the theme
        SharedPreferences sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        boolean isDarkTheme = sharedPrefs.getBoolean(THEME_PREF_KEY, false);
        applyTheme(isDarkTheme);

        DataManager dataManager = DataManager.getInstance();
        dataManager.loadVideosFromJson(this);

        // Load the default fragment if no saved instance state exists
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home); // Mark home as selected in the navigation view
        }

        // Handle navigation item selections
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Switch between fragments based on selected item
            if (itemId == R.id.nav_home) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            } else if (itemId == R.id.nav_settings) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
            } else if (itemId == R.id.nav_profile) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
            } else if (itemId == R.id.nav_about) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
            } else if (itemId == R.id.nav_login) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LoginFragment()).commit();
            } else if (itemId == R.id.nav_logout) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VideoPlayerFragment()).commit();
            } else if (itemId == R.id.theme_setter) {
                // Toggle theme preference
                boolean newThemeIsDark = toggleThemePreference();
                applyTheme(newThemeIsDark);
            }
            drawerLayout.closeDrawer(GravityCompat.START); // Close the drawer after selection
            return true;
        });

        // Adjust padding for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Handle back button press to close drawer if open
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Handle navigation item selections
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        // Switch between fragments based on selected item
        if (itemId == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        } else if (itemId == R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
        } else if (itemId == R.id.nav_profile) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
        } else if (itemId == R.id.nav_about) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
        }
        drawerLayout.closeDrawer(GravityCompat.START); // Close the drawer after selection
        return true;
    }

    // Toggle theme preference and return the new theme state
    private boolean toggleThemePreference() {
        SharedPreferences sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        boolean currentThemeIsDark = sharedPrefs.getBoolean(THEME_PREF_KEY, false); // Default to light theme
        boolean newThemeIsDark = !currentThemeIsDark;
        sharedPrefs.edit().putBoolean(THEME_PREF_KEY, newThemeIsDark).apply();
        return newThemeIsDark;
    }

    // Apply the selected theme
    private void applyTheme(boolean isDarkTheme) {
        AppCompatDelegate.setDefaultNightMode(
                isDarkTheme ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
