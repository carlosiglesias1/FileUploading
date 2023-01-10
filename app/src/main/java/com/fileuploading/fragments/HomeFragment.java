package com.fileuploading.fragments;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.fileuploading.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.util.ArrayList;

import ftpmanagement.FtpSesion;
import ftpmanagement.FtpTaskFactory;
import ftpmanagement.tasks.TaskGetFile;
import handlers.OnSwipeTouchListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private final String DIRECTORIO_ANTERIOR = "<-";

    //private AlertDialog loadingFileListDialog;
    private ListView fileList;
    private ArrayAdapter<String> fileListAdapter;
    private ArrayList<String> fileListEntity;
    private ArrayList<FTPFile> files;
    private ImageView imageView;
    private RelativeLayout imagePreview;
    private FloatingActionButton nextImage;
    private FloatingActionButton prevImage;
    private Button closePreview;
    private int imageIndex;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        this.loadImagePreview();
        this.hideImagePreview();
        this.fileListEntity = new ArrayList<>();
        this.files = new ArrayList<>();
        this.fileListAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, this.fileListEntity);
        this.fileList = this.getActivity().findViewById(R.id.FileList);
        this.fileList.setAdapter(this.fileListAdapter);
        this.fileList.setOnItemClickListener((adapterView, view, i, l) -> {
            if (this.files.get(i).isDirectory()) {
                if (this.fileListEntity.get(i).equals(this.DIRECTORIO_ANTERIOR)) {
                    this.goBack();
                } else {
                    String rutaFtp = FtpSesion.getInstance().getRutaActualFtp();
                    rutaFtp += fileListEntity.get(i) + "/";
                    FtpSesion.getInstance().setRutaActualFtp(rutaFtp);
                }
                this.loadFileList();
                this.imageView.setImageBitmap(null);
            } else {
                if (this.fileListEntity.get(i).equals(this.DIRECTORIO_ANTERIOR)) {
                    this.imageView.setImageBitmap(null);
                    this.goBack();
                    this.loadFileList();
                } else {

                    Thread openPreview = new Thread(() -> {
                        try {
                            this.imageIndex = i;
                            TaskGetFile downloadFile = FtpTaskFactory.getTaskGetFile();
                            String filename = FtpSesion.getInstance().getRutaActualFtp() + HomeFragment.this.fileListEntity.get(i);
                            Bitmap bufferedFile = downloadFile.getImagePreview(filename);
                            Handler appHandler = new Handler(Looper.getMainLooper());
                            if (bufferedFile != null) {
                                appHandler.post(() -> {
                                    this.imageView.setImageBitmap(bufferedFile);
                                    this.showImagePreview();
                                    this.hidePreviewButtons();
                                });
                            } else {
                                appHandler.post(() ->
                                        new AlertDialog.Builder(this.getActivity()).setMessage("No se ha podido cargar la imagen").setPositiveButton("OK", (dialogInterface, i1) -> dialogInterface.dismiss()).show()
                                );
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    openPreview.start();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        this.loadFileList();
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

        CircularProgressIndicator progressIndicator = new CircularProgressIndicator(HomeFragment.this.getActivity());
        progressIndicator.setIndeterminate(true);
        progressIndicator.getHeight();
        progressIndicator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(progressIndicator);

        Thread showFileList = new Thread(() -> {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            //Vaciado de las listas de archivos
            FTPFile[] tempFiles = null;
            this.files.clear();
            this.fileListEntity.clear();

            try {
                tempFiles = FtpTaskFactory.getTaskListDir().getFileList();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!FtpSesion.getInstance().getRutaActualFtp().equals("/")) {
                this.fileListEntity.add(this.DIRECTORIO_ANTERIOR);
                this.files.add(new FTPFile());
            }
            if (tempFiles != null && tempFiles.length > 0) {
                for (FTPFile file : tempFiles) {
                    this.fileListEntity.add(file.getName());
                    this.files.add(file);
                }
                mainHandler.post(this.fileListAdapter::notifyDataSetChanged);
            } else {
                mainHandler.post(() ->
                        Toast.makeText(HomeFragment.this.getContext(), "Error desconocido, revise el log", Toast.LENGTH_LONG).show()
                );
            }
        });
        showFileList.start();
    }

    private void goBack() {
        String rutaFtp = FtpSesion.getInstance().getRutaActualFtp();
        rutaFtp = rutaFtp.substring(0, rutaFtp.lastIndexOf("/"));
        rutaFtp = rutaFtp.substring(0, rutaFtp.lastIndexOf("/") + 1);
        FtpSesion.getInstance().setRutaActualFtp(rutaFtp);
    }

    private void loadImagePreview() {
        this.imagePreview = this.getActivity().findViewById(R.id.ImagePreview);
        this.imageView = this.getActivity().findViewById(R.id.ImageViewer);
        this.closePreview = this.getActivity().findViewById(R.id.CloseImageViewer);
        this.nextImage = this.getActivity().findViewById(R.id.nextImage);
        this.prevImage = this.getActivity().findViewById(R.id.prevImage);
        this.hidePreviewButtons();
        this.closePreview.setOnClickListener(view -> HomeFragment.this.hideImagePreview());
        this.nextImage.setOnClickListener(view -> HomeFragment.this.fileList.performItemClick(HomeFragment.this.fileList, imageIndex + 1, imageIndex + 1));
        this.prevImage.setOnClickListener(view -> HomeFragment.this.fileList.performItemClick(HomeFragment.this.fileList, imageIndex - 1, imageIndex - 1));
        this.imageView.setOnClickListener(view -> {
            if (HomeFragment.this.closePreview.getVisibility() == View.VISIBLE)
                HomeFragment.this.hidePreviewButtons();
            else
                HomeFragment.this.showPreviewButtons();
        });
        this.imageView.setOnTouchListener(new OnSwipeTouchListener(this.getActivity()) {
            @Override
            public void onClick() {
                super.onClick();
                imageView.performClick();
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                HomeFragment.this.fileList.performItemClick(HomeFragment.this.fileList, imageIndex + 1, imageIndex + 1);
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                HomeFragment.this.fileList.performItemClick(HomeFragment.this.fileList, imageIndex - 1, imageIndex - 1);
            }
        });
    }

    private void showImagePreview() {
        this.imagePreview.setVisibility(View.VISIBLE);
        this.imageView.setVisibility(View.VISIBLE);
        this.closePreview.setVisibility(View.VISIBLE);
    }

    private void hideImagePreview() {
        this.imagePreview.setVisibility(View.INVISIBLE);
        this.imageView.setVisibility(View.INVISIBLE);
        this.closePreview.setVisibility(View.INVISIBLE);
    }

    private void showPreviewButtons() {
        this.closePreview.setVisibility(View.VISIBLE);
        this.nextImage.setVisibility(View.VISIBLE);
        this.prevImage.setVisibility(View.VISIBLE);
    }

    private void hidePreviewButtons() {
        this.closePreview.setVisibility(View.INVISIBLE);
        this.nextImage.setVisibility(View.INVISIBLE);
        this.prevImage.setVisibility(View.INVISIBLE);
    }
}