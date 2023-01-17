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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.fileuploading.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
    private TextInputEditText remotePath;
    private Directory[] directoriesList;
    private ArrayList<String> filesToUpload;
    private Button startUpload;
    private CheckBox deleteDeviceElements;
    private CheckBox checkDirRecursive;
    private CheckBox foldUp;
    private AlertDialog dialog;

    @Override
    public void onStart() {
        super.onStart();
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
                    this.getAllFilesToUpload();
                    if (this.filesToUpload != null)
                        this.uploadFiles();
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

    private void uploadFiles() {
        LinearLayout linearLayout = new LinearLayout(this.getContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearProgressIndicator progressIndicator = new LinearProgressIndicator(this.getContext());
        progressIndicator.setProgress(0);
        progressIndicator.setMax(100);
        progressIndicator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(progressIndicator);

        this.dialog = new AlertDialog.Builder(this.getContext()).setTitle("Subiendo archivos").setView(linearLayout).create();

        new Thread(() -> {
            Handler appHandler = new Handler(Looper.getMainLooper());
            appHandler.post(() -> {
                this.dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCELAR", (dialogInterface, i) -> {
                    Thread.currentThread().interrupt();
                    dialogInterface.dismiss();
                });
                this.dialog.show();
            });
            for (String filePath : this.filesToUpload
            ) {
                File file = new File(filePath);
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
                    appHandler.post(() -> {
                        int progress = this.filesToUpload.indexOf(filePath) * 100 / this.filesToUpload.size();
                        progressIndicator.setProgress(progress);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            appHandler.post(this.dialog::dismiss);
        }).start();
    }

    private void getAllFilesToUpload() {
        this.filesToUpload = new ArrayList<>();
        for (Directory directory : this.directoriesList) {
            this.getFilesFromDirectory(directory.phoneDirectory);
        }
    }

    private void getFilesFromDirectory(String directory) {
        File file = new File(directory);
        File[] filesInDirectory = file.listFiles();
        if (filesInDirectory != null) {
            for (File fileInDirectory : filesInDirectory) {
                if (!fileInDirectory.isDirectory()) {
                    this.filesToUpload.add(fileInDirectory.getPath());
                } else if (this.checkDirRecursive.isChecked()) {
                    getFilesFromDirectory(fileInDirectory.getPath());
                }
            }
        }
    }

    private void loadDirectories() {
        this.dialog = new AlertDialog.Builder(this.getContext()).setMessage("Cargando directorios...").create();
        new Thread(() -> {
            Handler appHandler = new Handler(Looper.getMainLooper());
            appHandler.post(this.dialog::show);
            Database appdb = DatabaseAccess.getInstance(this.getActivity()).getDatabase();
            StartUploadFragment.this.directoriesList = appdb.directoryDao().getAllDirectories();
            appHandler.post(() -> this.directoriesSummary.setText(""));
            if (this.directoriesList != null) {
                for (Directory directory : this.directoriesList
                ) {
                    appHandler.post(() -> this.directoriesSummary.append(directory.phoneDirectory + "\n"));
                }
            }
            appHandler.post(this.dialog::dismiss);
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start_upload, container, false);
    }
}