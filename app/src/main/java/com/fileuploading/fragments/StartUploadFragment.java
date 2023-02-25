package com.fileuploading.fragments;

import android.Manifest;
import android.app.DatePickerDialog;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.fileuploading.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import ftpmanagement.FtpSesion;
import ftpmanagement.FtpTaskFactory;
import ftpmanagement.tasks.TaskUploadFile;
import persistence.Database;
import persistence.DatabaseAccess;
import persistence.entities.Directory;
import utils.FileUtils;

public class StartUploadFragment extends Fragment {

    private TextView directoriesSummary;
    private TextInputEditText remotePath;
    private Directory[] directoriesList;
    private ArrayList<String> filesToUpload;
    private SwitchMaterial deleteDeviceElements;
    private SwitchMaterial checkDirRecursive;
    private SwitchMaterial foldUp;
    private SwitchMaterial uploadAll;
    private LinearLayout fileDateFilterLayout;
    private TextInputLayout uploadFileFromDateLayout;
    private TextInputEditText uploadFileFromDate;
    private TextInputLayout uploadFileToDateLayout;
    private TextInputEditText uploadFileToDate;
    private AlertDialog dialog;

    @Override
    public void onStart() {
        super.onStart();
        this.findComponents();
        if (this.uploadAll.isChecked())
            this.fileDateFilterLayout.setVisibility(View.INVISIBLE);
        else if (this.fileDateFilterLayout.getVisibility() == View.INVISIBLE) {
            this.fileDateFilterLayout.setVisibility(View.VISIBLE);
        }
        this.uploadAll.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (!checked) {
                StartUploadFragment.this.fileDateFilterLayout.setVisibility(View.VISIBLE);
            } else {
                StartUploadFragment.this.fileDateFilterLayout.setVisibility(View.INVISIBLE);
            }
        });
        this.uploadFileFromDateLayout.setEndIconOnClickListener(view -> setDateText(this.uploadFileFromDate));
        this.uploadFileToDateLayout.setEndIconOnClickListener(view -> setDateText(this.uploadFileToDate));
        Button startUpload = this.getActivity().findViewById(R.id.StartUpload);
        startUpload.setOnClickListener(v -> {
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
                    if (!this.uploadAll.isChecked() && !this.uploadFileToDate.getText().toString().isEmpty()) {
                        this.getAllFilesToUpload();
                        if (this.filesToUpload != null)
                            this.uploadFiles();
                    } else
                        Toast.makeText(this.getContext(), "Debe incluír fecha \"Hasta\"", Toast.LENGTH_LONG).show();
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

    private void findComponents() {
        this.checkDirRecursive = this.getActivity().findViewById(R.id.GetFilesRecursive);
        this.directoriesSummary = this.getActivity().findViewById(R.id.DirectoriesSummary);
        this.deleteDeviceElements = this.getActivity().findViewById(R.id.DeleteDeviceElements);
        this.foldUp = this.getActivity().findViewById(R.id.FoldUpByDate);
        this.remotePath = this.getActivity().findViewById(R.id.DestinyPath);
        this.fileDateFilterLayout = this.getActivity().findViewById(R.id.FileDateFilterLayout);
        this.uploadAll = this.getActivity().findViewById(R.id.UploadAll);
        this.uploadFileFromDateLayout = this.getActivity().findViewById(R.id.UploadFileFromDateLayout);
        this.uploadFileFromDate = this.getActivity().findViewById(R.id.UploadFileFromDate);
        this.uploadFileToDateLayout = this.getActivity().findViewById(R.id.UploadFileToDateLayout);
        this.uploadFileToDate = this.getActivity().findViewById(R.id.UploadFileToDate);
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

        String dialogText = "Subiendo archivos:";

        this.dialog = new AlertDialog.Builder(this.getContext()).setTitle("Subiendo archivos").setMessage(dialogText + "0 de" + this.filesToUpload.size()).setView(linearLayout).create();

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
                        this.dialog.setMessage(dialogText + (this.filesToUpload.indexOf(filePath) + 1) + " de " + this.filesToUpload.size());
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
                    if (this.uploadAll.isChecked() || (!this.uploadAll.isChecked() &&
                            fileBetweenDates(FileUtils.getFileDateString(fileInDirectory))))
                        this.filesToUpload.add(fileInDirectory.getPath());
                } else if (this.checkDirRecursive.isChecked()) {
                    getFilesFromDirectory(fileInDirectory.getPath());
                }
            }
        }
    }

    private boolean fileBetweenDates(String fileDate) {
        LocalDate fromDateParsed = null;
        if (this.uploadFileFromDate.getText() != null && !this.uploadFileFromDate.getText().toString().isEmpty())
            fromDateParsed = LocalDate.parse(this.uploadFileFromDate.getText().toString(), DateTimeFormatter.ofPattern("dd/MM/uuuu"));
        LocalDate toDateParsed = LocalDate.parse(this.uploadFileToDate.getText().toString(), DateTimeFormatter.ofPattern("dd/MM/uuuu"));
        LocalDate fileDateParsed = LocalDate.parse(fileDate, DateTimeFormatter.ofPattern("uuuu-MM-dd"));
        return (fromDateParsed == null || fileDateParsed.compareTo(fromDateParsed) > 0) &&
                fileDateParsed.compareTo(toDateParsed) < 0;
    }

    private void loadDirectories() {
        this.dialog = new AlertDialog.Builder(this.getContext()).setMessage("Cargando directorios...").create();
        new Thread(() -> {
            Handler appHandler = new Handler(Looper.getMainLooper());
            appHandler.post(this.dialog::show);
            Database appdb = DatabaseAccess.getInstance(this.getActivity()).getDatabase();
            StartUploadFragment.this.directoriesList = appdb.directoryDao().getAllDirectories();
            appHandler.post(() -> this.directoriesSummary.setText(""));
            if (this.directoriesList != null && this.directoriesList.length > 0) {
                for (Directory directory : this.directoriesList
                ) {
                    appHandler.post(() -> this.directoriesSummary.append(directory.phoneDirectory + "\n"));
                }
            } else {
                appHandler.post(() -> this.directoriesSummary.append("No hay directorios\n"));
            }
            appHandler.post(this.dialog::dismiss);
        }).start();
    }

    private void setDateText(TextInputEditText view) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext());
        datePickerDialog.setOnDateSetListener((datePicker, year, month, day) -> {
            String dayString = Integer.toString(day);
            while (dayString.length() < 2) {
                dayString = "0" + dayString;
            }
            String monthString = Integer.toString(month + 1);
            while (monthString.length() < 2) {
                monthString = "0" + monthString;
            }
            view.setText(dayString + "/" + monthString + "/" + year);
        });
        datePickerDialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return inflater.inflate(R.layout.fragment_start_upload, container, false);
    }
}