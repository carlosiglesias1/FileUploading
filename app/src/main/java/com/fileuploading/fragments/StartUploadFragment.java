package com.fileuploading.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.fileuploading.R;

import java.io.File;
import java.io.IOException;

import ftpmanagement.FtpSesion;
import ftpmanagement.FtpTaskFactory;
import ftpmanagement.tasks.TaskUploadFile;
import persistence.Database;
import persistence.DatabaseAccess;
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
    private CheckBox checkDirRecursive;
    private CheckBox foldUp;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    @Override
    public void onStart() {
        super.onStart();
        this.dialogBuilder = new AlertDialog.Builder(this.getActivity());
        this.checkDirRecursive = this.getActivity().findViewById(R.id.GetFilesRecursive);
        this.directoriesSummary = this.getActivity().findViewById(R.id.DirectoriesSummary);
        this.startUpload = this.getActivity().findViewById(R.id.StartUpload);
        this.deleteDeviceElements = this.getActivity().findViewById(R.id.DeleteDeviceElements);
        this.foldUp = this.getActivity().findViewById(R.id.FoldUpByDate);
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
                if (this.directoriesList.length > 0) {
                    for (Directory directory : this.directoriesList
                    ) {
                        this.uploadFilesFromDirectory(directory.phoneDirectory);
                    }
                } else {
                    Toast.makeText(this.getActivity(), "No hay directorios", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        this.loadDirectories();
    }

    private boolean hasPermissionToStart() {
        return Environment.isExternalStorageManager() && ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
    }

    private void uploadFilesFromDirectory(String p_directory) {
        System.out.println("Subiendo archivos de: " + p_directory);
        Thread ftpThread = new Thread(() -> {
            File folder = new File(p_directory);
            File[] filesInFolder = folder.listFiles();
            if (filesInFolder != null) {
                for (File file : filesInFolder
                ) {
                    if (file.isDirectory() && this.checkDirRecursive.isChecked()) {
                        File[] dirFiles = file.listFiles();
                        if (dirFiles != null && dirFiles.length > 0)
                            uploadFilesFromDirectory(file.getPath());
                    }
                    try {
                        TaskUploadFile taskUploadFile = FtpTaskFactory.getTaskUploadFile();
                        if (!this.remotePath.getText().toString().isEmpty() && !this.remotePath.getText().toString().equals(FtpSesion.getInstance().getRutaActualFtp()))
                            FtpSesion.getInstance().setRutaActualFtp(this.remotePath.getText().toString());
                        taskUploadFile.uploadFile(file, FtpSesion.getInstance().getRutaActualFtp(), this.foldUp.isChecked());
                        if (taskUploadFile.isFileUploaded()) {
                            System.out.println(file.getPath() + " archivo enviado");
                            if (this.deleteDeviceElements.isChecked() && file.delete()) {
                                System.out.println("Elemento eliminado");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        ftpThread.start();
    }

    private void loadDirectories() {
        this.dialog = this.dialogBuilder.setMessage("Cargando directorios...").create();
        Thread loadDirectories = new Thread(() -> {
            Handler appHandler = new Handler(Looper.getMainLooper());
            appHandler.post(this.dialog::show);
            Database appdb = DatabaseAccess.getInstance(this.getActivity()).getDatabase();
            StartUploadFragment.this.directoriesList = appdb.directoryDao().getAllDirectories();
            appHandler.post(() -> this.directoriesSummary.setText(""));
            if (this.directoriesList != null) {
                for (Directory directory : this.directoriesList
                ) {
                    appHandler.post(() -> {
                        this.directoriesSummary.append(directory.phoneDirectory + "\n");
                    });

                }
            }
            appHandler.post(this.dialog::dismiss);
        });
        loadDirectories.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start_upload, container, false);
    }
}