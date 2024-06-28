package com.example.crispycrumbs.ui;

import static com.example.crispycrumbs.model.DataManager.getUriFromResOrFile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.data.LoggedInUser;
import com.example.crispycrumbs.data.UserItem;
import com.example.crispycrumbs.model.DataManager;
import com.example.crispycrumbs.model.UserLogic;
import com.google.android.material.navigation.NavigationView;

public class MainPage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private static final String THEME_PREF_KEY = "app_theme";
    private static MainPage instance = null;
    private static DataManager dataManager = null;
    private static UserLogic userLogic = null;


    public static DataManager getDataManager() {
        return dataManager;
    }

    public static UserLogic getUserLogic() {
        return userLogic;
    }

    public static MainPage getInstance() {
        if (instance == null) {
            instance = new MainPage();
        }
        return instance;
    }

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
        instance = this;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        toolbar.setOnClickListener(v -> {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).addToBackStack(null).commit();
        });

        if (android.os.Build.VERSION.SDK_INT >= 34) {
            new AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("Your Android version is too high for this app. Some features may not work correctly.\n this app is built for Android 13 and lower.")
                    .setPositiveButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_Bar);

        navigationView.setNavigationItemSelectedListener(this);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        SharedPreferences sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        boolean isDarkTheme = sharedPrefs.getBoolean(THEME_PREF_KEY, false);
        applyTheme(isDarkTheme);

        userLogic = UserLogic.getInstance();
        dataManager = DataManager.getInstance();
        dataManager.loadVideosFromJson(this);
        dataManager.loadUsersFromJson(this);

        for (UserItem user : dataManager.getUserList()) {
            Log.d("User", "ID: " + user.getUserId() + ", Name: " + user.getUserName());
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        updateNavigationMenu();

        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(navigationView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        updateNavHeader();
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (currentFragment instanceof HomeFragment) {
            //dropDB_Changes
            dataManager = null;
            LoggedInUser.LogOut();

            Toast.makeText(this, "goodbye", Toast.LENGTH_SHORT).show();
            finish(); // Close the app
        } else if (currentFragment instanceof LoginFragment || currentFragment instanceof SignUpFragment) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack(); // Go back to the previous fragment
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                super.onBackPressed();
            }
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            //todo enable in next release
//        } else if (itemId == R.id.nav_settings) {
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).addToBackStack(null).commit();
        } else if (itemId == R.id.nav_profile) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).addToBackStack(null).commit();
        } else if (itemId == R.id.nav_my_videos) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PlayListFragment()).addToBackStack(null).commit();
        } else if (itemId == R.id.nav_login) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LoginFragment()).addToBackStack(null).commit();
        } else if (itemId == R.id.nav_logout) {
            LoggedInUser.LogOut();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LoginFragment()).addToBackStack(null).commit();
            updateNavigationMenu();
        } else if (itemId == R.id.nav_signup) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SignUpFragment()).addToBackStack(null).commit();
        } else if (itemId == R.id.nav_upload_video) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new UploadVideoFragment()).addToBackStack(null).commit();
        } else if (itemId == R.id.theme_setter) {
            boolean newThemeIsDark = toggleThemePreference();
            applyTheme(newThemeIsDark);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void updateNavigationMenu() {
        Menu menu = navigationView.getMenu();
        boolean isLoggedIn = LoggedInUser.getUser() != null;

        menu.findItem(R.id.nav_home).setVisible(true);
//        menu.findItem(R.id.nav_settings).setVisible(true);
        menu.findItem(R.id.theme_setter).setVisible(true);

        menu.findItem(R.id.nav_profile).setVisible(isLoggedIn);
        menu.findItem(R.id.nav_my_videos).setVisible(isLoggedIn);
        menu.findItem(R.id.nav_logout).setVisible(isLoggedIn);
        menu.findItem(R.id.nav_upload_video).setVisible(isLoggedIn);
        menu.findItem(R.id.nav_login).setVisible(!isLoggedIn);
        menu.findItem(R.id.nav_signup).setVisible(!isLoggedIn);
    }

    private boolean toggleThemePreference() {
        SharedPreferences sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        boolean currentThemeIsDark = sharedPrefs.getBoolean(THEME_PREF_KEY, false);
        boolean newThemeIsDark = !currentThemeIsDark;
        sharedPrefs.edit().putBoolean(THEME_PREF_KEY, newThemeIsDark).apply();
        return newThemeIsDark;
    }

    private void applyTheme(boolean isDarkTheme) {
        AppCompatDelegate.setDefaultNightMode(
                isDarkTheme ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    public void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        ImageView profilePicture = headerView.findViewById(R.id.profile_picture);
        TextView userName = headerView.findViewById(R.id.user_name);
        TextView userEmail = headerView.findViewById(R.id.user_email);

        UserItem currentUser = LoggedInUser.getUser();

        if (currentUser != null) {
            profilePicture.setImageURI(getUriFromResOrFile(currentUser.getProfilePhoto()));
            userName.setText(currentUser.getDisplayedName());
            userEmail.setText(currentUser.getEmail());
            profilePicture.setOnClickListener(v -> {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).addToBackStack(null).commit();
//                drawerLayout.closeDrawer(GravityCompat.START);
            });
        } else {
            profilePicture.setImageResource(R.drawable.default_profile_picture);
            userName.setText(R.string.guest);
            userEmail.setText("");
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the state of the LoggedInUser
        outState.putSerializable("LoggedInUser", LoggedInUser.getUser());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore the state of the LoggedInUser
        LoggedInUser.SetLoggedInUser((UserItem) savedInstanceState.getSerializable("LoggedInUser"));
    }

}
