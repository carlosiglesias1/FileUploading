package com.fileuploading.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.fileuploading.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.apache.commons.net.ftp.FTPFile;

import java.util.ArrayList;

import ftpmanagement.FtpSesion;
import ftpmanagement.tasks.FtpTask;
import ftpmanagement.FtpTaskFactory;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private AlertDialog loadingFileListDialog;
    private ListView fileList;
    private ArrayAdapter<String> fileListAdapter;
    private ArrayList<String> fileListEntity;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        this.fileListEntity = new ArrayList<>();
        this.fileListAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, this.fileListEntity);
        this.fileList = this.getActivity().findViewById(R.id.FileList);
        this.fileList.setAdapter(this.fileListAdapter);
        this.fileList.setOnItemClickListener((adapterView, view, i, l) -> {
            //EMPTY METHOD
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FtpSesion.getInstance().getClientData() != null) {
            loadFileList();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    private void loadFileList() {
        LinearLayout linearLayout = new LinearLayout(this.getActivity());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        CircularProgressIndicator progressIndicator = new CircularProgressIndicator(this.getActivity());
        progressIndicator.setIndeterminate(true);
        progressIndicator.getHeight();
        progressIndicator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(progressIndicator);

        Thread showFileList = new Thread(() -> {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.getActivity());
            dialogBuilder.setTitle("Cargar Archivos");
            dialogBuilder.setView(linearLayout);
            dialogBuilder.setNegativeButton("Cancelar", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                Thread.currentThread().interrupt();
            });
            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(() -> {
                this.loadingFileListDialog = dialogBuilder.create();
                this.loadingFileListDialog.show();
            });
            FtpTaskFactory loadFilesTask = new FtpTaskFactory(FtpTask.FTP_TASK_LISTDIR);
            loadFilesTask.run();
            FTPFile[] ftpFiles = loadFilesTask.getFiles();
            if (ftpFiles != null && ftpFiles.length > 0) {
                for (FTPFile file : ftpFiles) {
                    this.fileListEntity.add(file.getName());
                }
                mainHandler.post(this.fileListAdapter::notifyDataSetChanged);
            } else {
                mainHandler.post(() -> {
                    Toast.makeText(this.getActivity(), "Error desconocido, revise el log", Toast.LENGTH_LONG).show();
                });
            }
            mainHandler.post(this.loadingFileListDialog::dismiss);
        });
        showFileList.start();
    }
}