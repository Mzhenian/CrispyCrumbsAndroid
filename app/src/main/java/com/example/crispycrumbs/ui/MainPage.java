package com.example.crispycrumbs.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

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

import com.example.crispycrumbs.LoggedInUser;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.ui.AboutFragment;
import com.example.crispycrumbs.ui.HomeFragment;
import com.example.crispycrumbs.ui.LoginFragment;
import com.example.crispycrumbs.ui.SettingsFragment;
import com.example.crispycrumbs.ui.ShareFragment;
import com.google.android.material.navigation.NavigationView;

public class MainPage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;

    private static final String THEME_PREF_KEY = "app_theme";

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_main);

        Toolbar toolbar = findViewById(R.id.toolbar); // Ignore red line errors
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_Bar);

        navigationView.setNavigationItemSelectedListener(this);

        // Set up the navigation drawer toggle
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        // Set the drawer toggle as the drawer listener
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // In your Application class or MainActivity's onCreate
        SharedPreferences sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        boolean isDarkTheme = sharedPrefs.getBoolean(THEME_PREF_KEY, false);
        applyTheme(isDarkTheme);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                } else if (itemId == R.id.nav_settings) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
                } else if (itemId == R.id.nav_share) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ShareFragment()).commit();
                } else if (itemId == R.id.nav_about) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
                } else if (itemId == R.id.nav_login) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LoginFragment()).commit();
                } else if (itemId == R.id.nav_signup) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SignUpFragment()).commit();
                }else if (itemId == R.id.nav_logout) {
                    LoggedInUser.LogOut();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LoginFragment()).commit();

                } else if (itemId == R.id.theme_setter) {
                    // Toggle theme preference (see step 3)
                    boolean newThemeIsDark = toggleThemePreference();
                    applyTheme(newThemeIsDark);
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        } else if (itemId == R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
        } else if (itemId == R.id.nav_share) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ShareFragment()).commit();
        } else if (itemId == R.id.nav_about) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean toggleThemePreference() {
        SharedPreferences sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        boolean currentThemeIsDark = sharedPrefs.getBoolean(THEME_PREF_KEY, false); // Default to light theme
        boolean newThemeIsDark = !currentThemeIsDark;
        sharedPrefs.edit().putBoolean(THEME_PREF_KEY, newThemeIsDark).apply();
        return newThemeIsDark;
    }

    private void applyTheme(boolean isDarkTheme) {
        AppCompatDelegate.setDefaultNightMode(
                isDarkTheme ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
