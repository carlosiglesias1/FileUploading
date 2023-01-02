package com.fileuploading.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.example.fileuploading.R;

import java.io.File;
import java.util.Objects;

import ftpmanagement.FtpTaskFactory;
import ftpmanagement.tasks.FtpTask;
import persistence.Database;
import persistence.entities.Directory;

/**
 * A simple {@link Fragment} subclass.
 */
public class StartUploadFragment extends Fragment {

    private TextView directoriesSummary;
    private EditText remotePath;
    private Directory[] directoriesList;
    private Button startUpload;
    private CheckBox deleteDeviceElements;
    private boolean directoriesLoaded;
    private AlertDialog.Builder dialogBuilder;

    public StartUploadFragment() {
        this.directoriesLoaded = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.dialogBuilder = new AlertDialog.Builder(this.getActivity());
        this.directoriesSummary = this.getActivity().findViewById(R.id.DirectoriesSummary);
        this.startUpload = this.getActivity().findViewById(R.id.StartUpload);
        this.deleteDeviceElements = this.getActivity().findViewById(R.id.DeleteDeviceElements);
        this.remotePath = this.getActivity().findViewById(R.id.DestinyPath);
        this.startUpload.setOnClickListener(v -> {
            if (!hasPermissionToStart()) {
                if (!Environment.isExternalStorageManager()) {
                    Intent security = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    this.startActivity(security);
                }
                if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                    this.getActivity().requestPermissions(new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);
                }
            } else {
                for (Directory directory : this.directoriesList
                ) {
                    this.uploadFilesFromDirectory(directory.phoneDirectory);
                }
            }
        });
        this.loadDirectories();
    }

    private boolean hasPermissionToStart() {
        return Environment.isExternalStorageManager() && ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
    }

    private void uploadFilesFromDirectory(String p_directory) {
        System.out.println("Subiendo archivos de: " + p_directory);
        File folder = new File(p_directory);
        for (File file : Objects.requireNonNull(folder.listFiles())
        ) {
            if (file.isDirectory() && file.listFiles() != null && Objects.requireNonNull(file.listFiles()).length != 0) {
                uploadFilesFromDirectory(file.getPath());
            }

            Thread ftpThread = new Thread(() -> {
                FtpTaskFactory ftpUploadFile = new FtpTaskFactory(FtpTask.FTP_TASK_UPLOAD);
                String destiny = "/";
                if (!this.remotePath.getText().toString().isEmpty())
                    destiny = this.remotePath.getText().toString();
                ftpUploadFile.setFile(file, destiny);
                if (ftpUploadFile.isTaskCompleted()) {
                    System.out.println(file.getPath() + " archivo enviado");
                    if (this.deleteDeviceElements.isChecked() && file.delete()) {
                        System.out.println("Elemento eliminado");
                    }
                }
            });
            ftpThread.start();
        }
    }

    private void loadDirectories() {
        Thread loadDirectories = new Thread(() -> {
            Database appdb = Room.databaseBuilder(StartUploadFragment.this.getActivity(), Database.class, Database.DATABASE_NAME).build();
            StartUploadFragment.this.directoriesList = appdb.directoryDao().getAllDirectories();
            this.directoriesLoaded = true;
            appdb.close();
        });
        loadDirectories.start();
        AlertDialog dialog = dialogBuilder.setMessage("Cargando directorios...").setTitle("Iniciar subida").create();
        dialog.show();
        try {
            loadDirectories.join(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dialog.cancel();
        if (this.directoriesList != null) {
            for (Directory directory : this.directoriesList
            ) {
                this.directoriesSummary.append(directory.phoneDirectory + "\n");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start_upload, container, false);
    }
}