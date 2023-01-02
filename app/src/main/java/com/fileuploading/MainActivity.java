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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

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
                    transaction.replace(R.id.FragmentViewer, startUploadFragment);
                    break;
                case R.id.Settings:
                    transaction.replace(R.id.FragmentViewer, ftpConfiguration);
                    break;
                case R.id.SetDirectories:
                    transaction.replace(R.id.FragmentViewer, directoriesFragment);
                    break;
            }
            transaction.commit();
            return true;
        });
        AlertDialog loadingSessionApp = dialogBuilder.setTitle(R.string.app_name).setMessage("Cargando...").create();
        loadingSessionApp.show();
        FtpSesion.getInstance().loadClientData(this, loadingSessionApp);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Thread loadTask = new Thread(() -> {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Database mydb = DatabaseAccess.getInstance(this).getDatabase();
            transaction.add(R.id.FragmentViewer, this.homeFragment);
            if (!mydb.ftpDao().anyFtp()) {
                transaction.add(R.id.FragmentViewer, this.ftpConfiguration);
                transaction.addToBackStack(null);
            }
            mydb.close();
            transaction.commit();
        });
        loadTask.start();
    }

    public HomeFragment getHomeFragment() {
        return this.homeFragment;
    }

    public BottomNavigationView getNavigationView() {
        return navigationView;
    }

    private ArrayList<String> getDirectories(String selectedDirectories) {
        ArrayList<String> directories = new ArrayList<>(Arrays.asList(selectedDirectories.split("\n")));
        for (int i = 0; i < directories.size(); i++) {
            String directoryPath = directories.get(i);
            File folder = new File(directoryPath);
            if (!(folder.exists() && folder.isDirectory())) {
                File[] files = folder.listFiles();
                if (files != null && files.length < 1) {
                    i--;
                }
                directories.remove(directoryPath);
            }
        }
        return directories;
    }
}
