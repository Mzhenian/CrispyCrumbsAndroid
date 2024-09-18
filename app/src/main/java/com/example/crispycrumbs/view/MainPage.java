package com.example.crispycrumbs.view;

import static androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode;
import static com.example.crispycrumbs.model.DataManager.getUriFromResOrFile;

import android.content.res.Configuration;
import android.net.Uri;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.model.DataManager;
import com.example.crispycrumbs.model.UserLogic;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.google.android.material.navigation.NavigationView;

public class MainPage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static MainPage instance = null;
    private static DataManager dataManager = null;
    private static UserLogic userLogic = null;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;

    private Observer<UserItem> LoggedInUserObserver = null;

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

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_Bar);

        navigationView.setNavigationItemSelectedListener(this);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof VideoPlayerFragment) {
                    ((VideoPlayerFragment) currentFragment).hideMediaController();
                }
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        LoggedInUserObserver = getLoggedInUserObserver();
        LoggedInUser.getUser().observe(this, LoggedInUserObserver);
        LoggedInUser.setLoggedInUser(LoggedInUser.getUser().getValue());

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
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof VideoPlayerFragment) {
            ((VideoPlayerFragment) currentFragment).hideMediaController();
        }

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (currentFragment instanceof HomeFragment) {
            //dropDB_Changes
            dataManager = null;
            LoggedInUser.logOut();

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
        } else if (itemId == R.id.nav_profile) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).addToBackStack(null).commit();
        } else if (itemId == R.id.nav_my_videos) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PlayListFragment()).addToBackStack(null).commit();
        } else if (itemId == R.id.nav_login) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LoginFragment()).addToBackStack(null).commit();
        } else if (itemId == R.id.nav_logout) {
            LoggedInUser.logOut();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LoginFragment()).addToBackStack(null).commit();
        } else if (itemId == R.id.nav_signup) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SignUpFragment()).addToBackStack(null).commit();
        } else if (itemId == R.id.nav_upload_video) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new UploadVideoFragment()).addToBackStack(null).commit();
        } else if (itemId == R.id.theme_setter) {
            toggleDarkTheme();
        } else if (itemId == R.id.nav_edit_profile) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new EditProfileFragment()).addToBackStack(null).commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void toggleDarkTheme() {
        Configuration configuration = getResources().getConfiguration();
        int currentNightMode = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

                break;
        }
    }

    public Observer<UserItem> getLoggedInUserObserver() {
        Menu menu = navigationView.getMenu();

        NavigationView navigationView = findViewById(R.id.nav_Bar);
        View headerView = navigationView.getHeaderView(0);
        ImageView profilePicture = headerView.findViewById(R.id.profile_picture);
        TextView userName = headerView.findViewById(R.id.user_name);
        TextView userEmail = headerView.findViewById(R.id.user_email);

        return user -> {
            if (user != null) {
                Log.d("MainPage", "User detected in observer:");
                Log.d("MainPage", "User Name: " + user.getDisplayedName());
                Log.d("MainPage", "User Email: " + user.getEmail());
                Log.d("MainPage", "User Profile Photo: " + user.getProfilePhoto());

                // Check if profilePhoto is a content URI or server URL
                String profilePhoto = user.getProfilePhoto();
                if (profilePhoto != null && profilePhoto.startsWith("content://")) {
                    Log.d("MainPage", "Loading profile picture from content URI");
                    // Load directly from content URI (local image)
                    Glide.with(MainPage.this)
                            .load(Uri.parse(profilePhoto))
                            .placeholder(R.drawable.default_profile_picture)
                            .into(profilePicture);
                } else {
                    Log.d("MainPage", "Loading profile picture from server URL");
                    // Load from server URL
                    String userProfilePicUrl = ServerAPI.getInstance().constructUrl(profilePhoto);
                    Glide.with(MainPage.this)
                            .load(userProfilePicUrl)
                            .placeholder(R.drawable.default_profile_picture)
                            .into(profilePicture);
                }

                userName.setText(user.getDisplayedName());
                userEmail.setText(user.getEmail());

                menu.findItem(R.id.nav_profile).setVisible(true);
                menu.findItem(R.id.nav_my_videos).setVisible(true);
                menu.findItem(R.id.nav_logout).setVisible(true);
                menu.findItem(R.id.nav_upload_video).setVisible(true);
                menu.findItem(R.id.nav_edit_profile).setVisible(true);
                menu.findItem(R.id.nav_login).setVisible(false);
                menu.findItem(R.id.nav_signup).setVisible(false);

                Toast.makeText(MainPage.getInstance(), "Welcome back " + user.getDisplayedName(), Toast.LENGTH_SHORT).show();

            } else {
                Log.d("MainPage", "No user detected. Setting default guest values.");

                profilePicture.setImageResource(R.drawable.default_profile_picture);
                userName.setText(R.string.guest);
                userEmail.setText("");

                menu.findItem(R.id.nav_profile).setVisible(false);
                menu.findItem(R.id.nav_edit_profile).setVisible(false);
                menu.findItem(R.id.nav_my_videos).setVisible(false);
                menu.findItem(R.id.nav_logout).setVisible(false);
                menu.findItem(R.id.nav_upload_video).setVisible(false);
                menu.findItem(R.id.nav_login).setVisible(true);
                menu.findItem(R.id.nav_signup).setVisible(true);

                Toast.makeText(MainPage.getInstance(), "Goodbye", Toast.LENGTH_SHORT).show();
            }
        };
    }





}
