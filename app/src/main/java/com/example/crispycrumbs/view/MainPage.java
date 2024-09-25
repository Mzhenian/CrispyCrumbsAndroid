package com.example.crispycrumbs.view;

import static androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode;

import static com.example.crispycrumbs.localDB.LoggedInUser.LIU_ID_KEY;
import static com.example.crispycrumbs.localDB.LoggedInUser.LIU_TOKEN_KEY;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.model.DataManager;
import com.example.crispycrumbs.model.UserLogic;
import com.example.crispycrumbs.repository.UserRepository;
import com.example.crispycrumbs.repository.VideoRepository;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

public class MainPage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static MainPage instance = null;
    private static DataManager dataManager = null;
    private static UserLogic userLogic = null;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private ImageButton connectToServerAlertIcon;
    private Animation flickerAnimation;
    private SharedPreferences sharedPreferences;
    private Observer<UserItem> LoggedInUserObserver = null;

    public static DataManager getDataManager() {
        return dataManager;
    }
    public static UserLogic getUserLogic() {
        return userLogic;
    }

    public static String THEME_KEY = "THEME_KEY";

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
        sharedPreferences = getPreferences(MODE_PRIVATE);

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

        connectToServerAlertIcon = findViewById(R.id.connectToServerAlertIcon);
        flickerAnimation = AnimationUtils.loadAnimation(this, R.anim.flicker_effect);
        connectToServerAlertIcon.setOnClickListener(v -> {
            showUpdateIPDialog();
        });

//        startConnectToServerAlert();

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

        initLoggedInUser();

        userLogic = UserLogic.getInstance();
        dataManager = DataManager.getInstance();
        dataManager.loadVideosFromJson(this);
        dataManager.loadUsersFromJson(this);

//        for (UserItem user : dataManager.getUserList()) {
//            Log.d("User", "ID: " + user.getUserId() + ", Name: " + user.getUserName());
//        }

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

        initDarkMode();
    }

    private void initLoggedInUser() {
        Application application = MainPage.getInstance().getApplication();
        AppDB db = AppDB.getDatabase(application);
        UserRepository userRepository = new UserRepository(db);

        LoggedInUserObserver = getLoggedInUserObserver();
        LoggedInUser.getUser().observe(this, LoggedInUserObserver);
        String userId = sharedPreferences.getString(LIU_ID_KEY, null);
        LoggedInUser.setLoggedInUser(userId);
        LoggedInUser.setToken(sharedPreferences.getString(LIU_TOKEN_KEY, ""));

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
        } else if (itemId == R.id.server_ip_setter) {
            showUpdateIPDialog();
        } else if (itemId == R.id.theme_setter) {
            toggleDarkTheme();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initDarkMode() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int savedNightMode = sharedPreferences.getInt(THEME_KEY, Configuration.UI_MODE_NIGHT_NO);
        if (currentNightMode != savedNightMode) {
            toggleDarkTheme();
        } else if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private void toggleDarkTheme() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                getWindow().getDecorView().setSystemUiVisibility(0);
                sharedPreferences.edit().putInt(THEME_KEY, Configuration.UI_MODE_NIGHT_YES).apply();
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                sharedPreferences.edit().putInt(THEME_KEY, Configuration.UI_MODE_NIGHT_NO).apply();
                break;
            default:
                setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                getWindow().getDecorView().setSystemUiVisibility(0);
                sharedPreferences.edit().putInt(THEME_KEY, Configuration.UI_MODE_NIGHT_YES).apply();
                break;

//            getWindow().getStatusBarColor(ContextCompat.getColor(this, R.color.crispy_orange));
//            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.crispy_orange));

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
            menu.findItem(R.id.nav_home).setVisible(true);
            menu.findItem(R.id.theme_setter).setVisible(true);
            String message;

            if (null != user) { // not guest
                String userProfilePicUrl = ServerAPI.getInstance().constructUrl(user.getProfilePhoto());
                Glide.with(MainPage.this)
                        .load(userProfilePicUrl)
                        .placeholder(R.drawable.default_profile_picture) // Optional: Add a placeholder
                        .skipMemoryCache(true)
                        .into(profilePicture);
                userName.setText(user.getDisplayedName());
                userEmail.setText(user.getEmail());
                profilePicture.setOnClickListener(v -> {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).addToBackStack(null).commit();
//                drawerLayout.closeDrawer(GravityCompat.START);
                });

                menu.findItem(R.id.nav_profile).setVisible(true);
                menu.findItem(R.id.nav_my_videos).setVisible(true);
                menu.findItem(R.id.nav_logout).setVisible(true);
                menu.findItem(R.id.nav_upload_video).setVisible(true);
                menu.findItem(R.id.nav_login).setVisible(false);
                menu.findItem(R.id.nav_signup).setVisible(false);
                message = "Welcome back " + user.getDisplayedName();
            } else { // guest
                if (userName.getText().equals(getResources().getString(R.string.guest))) {
                    message = "Welcome back";
                } else {
                    message = "Goodbye " + userName.getText();
                }

                profilePicture.setImageResource(R.drawable.default_profile_picture);
                userName.setText(R.string.guest);
                userEmail.setText("");

                menu.findItem(R.id.nav_profile).setVisible(false);
                menu.findItem(R.id.nav_my_videos).setVisible(false);
                menu.findItem(R.id.nav_logout).setVisible(false);
                menu.findItem(R.id.nav_upload_video).setVisible(false);
                menu.findItem(R.id.nav_login).setVisible(true);
                menu.findItem(R.id.nav_signup).setVisible(true);
            }
            Toast.makeText(MainPage.getInstance(), message, Toast.LENGTH_SHORT).show();
        };
    }

    public void showLoginSnackbar(View view) {
        Snackbar.make(view, "Please login to interact", Snackbar.LENGTH_LONG)
                .setAction("Login", v -> {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LoginFragment()).addToBackStack(null).commit();
                }).show();
    }

    public void startConnectToServerAlert() {
        connectToServerAlertIcon.setVisibility(View.VISIBLE);
        connectToServerAlertIcon.startAnimation(flickerAnimation);
    }
    public void stopConnectToServerAlert() {
        connectToServerAlertIcon.setVisibility(View.GONE);
        connectToServerAlertIcon.clearAnimation();
    }
    private void showUpdateIPDialog() {
        ServerAPI serverAPI =  ServerAPI.getInstance();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.update_ip_box, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText inputIP = dialogView.findViewById(R.id.ip_input);
        Button IPOKButton = dialogView.findViewById(R.id.ip_ok_button);
        Button IPDEFAULTButton = dialogView.findViewById(R.id.ip_default_button);

        inputIP.setHint("currently: " + serverAPI.getIP());

        IPOKButton.setOnClickListener(v -> {
            String content = inputIP.getText().toString();

            if (!content.isEmpty()) {
                serverAPI.setIP(content);
            }
            refreshfragment();
            dialog.dismiss();
            //request videos
            new VideoRepository(AppDB.getDatabase(this)).getMostViewedVideos();
        });

        IPDEFAULTButton.setOnClickListener(v -> {
            serverAPI.setIP(ServerAPI.DEFAULT_IP);
            refreshfragment();
            dialog.dismiss();
        });

        dialog.show();
    }
    private void refreshfragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        this.recreate();
    }
}
