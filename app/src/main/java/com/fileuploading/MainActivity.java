package com.fileuploading;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.fileuploading.R;
import com.fileuploading.fragments.FtpConfigFragment;
import com.fileuploading.fragments.HomeFragment;
import com.fileuploading.fragments.SetDirectoriesFragment;
import com.fileuploading.fragments.StartUploadFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import ftpmanagement.FtpSesion;
import persistence.Database;
import persistence.DatabaseAccess;

public class MainActivity extends AppCompatActivity {
    private HomeFragment homeFragment;
    private BottomNavigationView navigationView;
    private FtpConfigFragment ftpConfiguration;
    private StartUploadFragment startUploadFragment;
    private SetDirectoriesFragment directoriesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        this.ftpConfiguration = new FtpConfigFragment();
        this.homeFragment = new HomeFragment();
        this.directoriesFragment = new SetDirectoriesFragment();
        this.startUploadFragment = new StartUploadFragment();
        this.navigationView = findViewById(R.id.BottomNavigation);
        this.navigationView.setOnItemSelectedListener(item -> {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            switch (item.getItemId()) {
                case R.id.Home:
                    transaction.replace(R.id.FragmentViewer, this.homeFragment);
                    break;
                case R.id.StartUpload:
                    transaction.replace(R.id.FragmentViewer, this.startUploadFragment);
                    break;
                case R.id.Settings:
                    transaction.replace(R.id.FragmentViewer, this.ftpConfiguration);
                    break;
                case R.id.SetDirectories:
                    transaction.replace(R.id.FragmentViewer, this.directoriesFragment);
                    break;
            }
            transaction.commit();
            return true;
        });
        AlertDialog loadingSessionApp = dialogBuilder.setTitle(R.string.app_name).setMessage("Cargando...").create();
        loadingSessionApp.show();
        Thread loadTask = new Thread(() -> {
            FtpSesion.getInstance().loadClientData(this, loadingSessionApp);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Database appDb = DatabaseAccess.getInstance(this).getDatabase();
            transaction.add(R.id.FragmentViewer, this.homeFragment);
            if (!appDb.ftpDao().anyFtp()) {
                transaction.replace(R.id.FragmentViewer, this.ftpConfiguration);
                this.navigationView.setSelectedItemId(R.id.Settings);
            }
            transaction.commit();
        });
        loadTask.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        new Thread(() -> {
            DatabaseAccess.getInstance(this).close();
            try {
                if (FtpSesion.getInstance().getClienteFtp().isConnected()) {
                    FtpSesion.getInstance().getClienteFtp().logout();
                    FtpSesion.getInstance().getClienteFtp().disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public HomeFragment getHomeFragment() {
        return this.homeFragment;
    }

    public BottomNavigationView getNavigationView() {
        return navigationView;
    }

    public SetDirectoriesFragment getDirectoriesFragment(){
        return this.directoriesFragment;
    }
}