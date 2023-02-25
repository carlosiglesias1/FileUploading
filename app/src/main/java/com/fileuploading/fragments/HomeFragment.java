package com.fileuploading.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.fileuploading.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ftpmanagement.FtpSesion;
import ftpmanagement.FtpTaskFactory;
import ftpmanagement.tasks.TaskGetFile;
import listeners.OnSwipeTouchListener;
import utils.FileUtils;
import utils.MyFileProvider;
import utils.filelistview.FileListAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private final String ROOT_DIRECTORY = "/";
    private String LOCAL_FILES_PATH;

    private ListView fileList;
    private AlertDialog downloadingDialog;
    private FileListAdapter fileListAdapter;
    private ArrayList<String> fileListEntity;
    private ArrayList<FTPFile> files;
    private ImageView imageView;
    private RelativeLayout imagePreview;
    private Button nextImage;
    private Button prevImage;
    private Button goBack;
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
        this.LOCAL_FILES_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + this.getResources().getString(R.string.app_name) + File.separator;
        this.fileListEntity = new ArrayList<>();
        this.files = new ArrayList<>();
        this.fileListAdapter = new FileListAdapter(this.getContext(), this.files);
        this.goBack = this.getActivity().findViewById(R.id.GoBack);
        this.goBack.setOnClickListener(view -> {
            if (!FtpSesion.getInstance().getRutaActualFtp().equals(this.ROOT_DIRECTORY)) {
                String rutaFtp = FtpSesion.getInstance().getRutaActualFtp();
                rutaFtp = rutaFtp.substring(0, rutaFtp.lastIndexOf("/"));
                rutaFtp = rutaFtp.substring(0, rutaFtp.lastIndexOf("/") + 1);
                try {
                    FtpSesion.getInstance().setRutaActualFtp(rutaFtp);
                    this.loadFileList();
                    this.imageView.setImageBitmap(null);
                } catch (Exception ex) {
                    Toast.makeText(this.getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    ex.printStackTrace();
                }
            }
        });
        this.fileList = this.getActivity().findViewById(R.id.FileList);
        this.fileList.setAdapter(this.fileListAdapter);
        this.fileList.setOnItemClickListener((adapterView, view, i, l) -> {
            if (this.files.get(i).isDirectory()) {
                String rutaFtp = FtpSesion.getInstance().getRutaActualFtp();
                rutaFtp += fileListEntity.get(i) + "/";
                try {
                    FtpSesion.getInstance().setRutaActualFtp(rutaFtp);
                    this.loadFileList();
                    this.imageView.setImageBitmap(null);
                } catch (Exception ex) {
                    Toast.makeText(this.getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    ex.printStackTrace();
                }
            } else {
                if (canLoadPreview(files.get(i))) {
                    this.openFilePreview(i);
                } else {
                    FTPFile ftpFile = files.get(i);
                    String localFilePath = this.LOCAL_FILES_PATH + this.getFileDir(ftpFile.getName()) + ftpFile.getName();
                    File file = new File(localFilePath);
                    if (file.exists()) {
                        Uri fileUri = MyFileProvider.getUriForFile(this.getContext(), "com.example.fileuploading.fileprovider", file);
                        String mime = this.getContext().getContentResolver().getType(fileUri);
                        this.showFile(fileUri, mime);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
                        builder.setMessage("No hay previsualización disponible para este archivo.\nDesea descargarlo?\nRuta destino: " + this.LOCAL_FILES_PATH);
                        builder.setPositiveButton("Descargar", (dialogInterface, i12) -> {
                            if (!Environment.isExternalStorageManager()) {
                                Intent security = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                this.startActivity(security);
                            }
                            new Thread(() -> {
                                Handler myHandler = new Handler(Looper.getMainLooper());
                                try {
                                    myHandler.post(() -> {
                                        this.downloadingDialog = new AlertDialog.Builder(this.getContext()).setMessage("Descargando archivo").create();
                                        this.downloadingDialog.show();
                                    });
                                    FTPFile selectedFile = this.files.get(i);
                                    String filePath = this.LOCAL_FILES_PATH + getFileDir(selectedFile.getName());
                                    String remoteFileName = FtpSesion.getInstance().getRutaActualFtp() + selectedFile.getName();
                                    File downloadedFile = FtpTaskFactory.getTaskGetFile().downloadFile(remoteFileName, filePath);
                                    Uri fileUri = MyFileProvider.getUriForFile(this.getActivity(), "com.example.fileuploading.fileprovider", downloadedFile);
                                    String mime = getContext().getContentResolver().getType(fileUri);
                                    myHandler.post(this.downloadingDialog::dismiss);
                                    this.showFile(fileUri, mime);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (NullPointerException e) {
                                    myHandler.post(() -> new AlertDialog.Builder(HomeFragment.this.getContext()).setMessage("No se ha podido obtener el tipo de archivo").setPositiveButton("OK", (dialogInterface1, i1) -> dialogInterface1.dismiss()).show());
                                }
                            }).start();
                        });
                        builder.create().show();
                    }
                }
            }
        });
    }

    private boolean isPreviewLoaded() {
        return this.imagePreview.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.loadFileList();
    }

    private String getFileDir(String filename) {
        String[] filesDir = {"Others/", "Images/", "Documents/", "Video/"};
        return filesDir[FileUtils.getFileType(filename)];
    }

    private void showFile(Uri fileUri, String mime) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, mime);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Abrir archivo"));
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

        new Thread(() -> {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            //Vaciado de las listas de archivos
            FTPFile[] tempFiles = null;
            this.files.clear();
            this.fileListEntity.clear();
            try {
                tempFiles = FtpTaskFactory.getTaskListDir().getFileList();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
            if (tempFiles != null && tempFiles.length > 0) {
                for (FTPFile file : tempFiles) {
                    this.fileListEntity.add(file.getName());
                    this.files.add(file);
                }
                mainHandler.post(this.fileListAdapter::notifyDataSetChanged);
            } else {
                mainHandler.post(() -> Toast.makeText(HomeFragment.this.getContext(), "Error desconocido, puede que estes desconectado", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private boolean canLoadPreview(FTPFile file) {
        return FileUtils.getFileType(file.getName()) == FileUtils.FileTypes.IMAGE;
    }

    private void openFilePreview(int p_fileIndex) {
        new Thread(() -> {
            try {
                this.imageIndex = p_fileIndex;
                TaskGetFile downloadFile = FtpTaskFactory.getTaskGetFile();
                String filename = FtpSesion.getInstance().getRutaActualFtp() + HomeFragment.this.fileListEntity.get(p_fileIndex);
                Bitmap bufferedFile = downloadFile.getImagePreview(filename);
                Handler appHandler = new Handler(Looper.getMainLooper());
                if (bufferedFile != null) {
                    appHandler.post(() -> {
                        this.imageView.setImageBitmap(bufferedFile);
                        this.showImagePreview();
                        if (this.nextImage.getVisibility() != View.INVISIBLE)
                            this.showPreviewButtons(imageIndex);
                    });
                } else {
                    appHandler.post(() -> new AlertDialog.Builder(this.getActivity()).setMessage("No se ha podido cargar la imagen").setPositiveButton("OK", (dialogInterface, i1) -> dialogInterface.dismiss()).show());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @SuppressLint("ClickableViewAccessibility")
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
            if (HomeFragment.this.nextImage.getVisibility() == View.VISIBLE)
                HomeFragment.this.hidePreviewButtons();
            else HomeFragment.this.showPreviewButtons(this.imageIndex);
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

    private void showPreviewButtons(int index) {
        this.closePreview.setVisibility(View.VISIBLE);
        int begin = 0;
        int end = this.files.size() - 1;
        if (index < end) this.nextImage.setVisibility(View.VISIBLE);
        else
            this.nextImage.setVisibility(View.INVISIBLE);
        if (index > begin) this.prevImage.setVisibility(View.VISIBLE);
        else
            this.prevImage.setVisibility(View.INVISIBLE);
    }

    private void hidePreviewButtons() {
        this.nextImage.setVisibility(View.INVISIBLE);
        this.prevImage.setVisibility(View.INVISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}