package com.fileuploading.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.fileuploading.R;
import com.fileuploading.MainActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import persistence.Database;
import persistence.DatabaseAccess;
import persistence.entities.Directory;

public class DirectoryConfigFragment extends Fragment {
    private FloatingActionButton openDocumentTree;

    private final ActivityResultLauncher<Intent> openDirectoryResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Intent intent = result.getData();
            if (intent != null) {
                Uri uri = intent.getData();
                String docId = DocumentsContract.getTreeDocumentId(uri.normalizeScheme());
                String[] split = docId.split(":");
                if (split[0].equals("primary")) {
                    docId = Environment.getExternalStorageDirectory().getPath() + "/" + split[1];
                }
                System.out.println(docId);
                Directory directory = new Directory();
                directory.phoneDirectory = docId;
                Thread insertOnDatabase = new Thread(() -> {
                    Database database = DatabaseAccess.getInstance(this.getContext()).getDatabase();
                    database.directoryDao().insertDirectory(directory);
                    FragmentTransaction exit = this.getActivity().getSupportFragmentManager().beginTransaction();
                    exit.replace(R.id.FragmentViewer, ((MainActivity) this.getActivity()).getDirectoriesFragment());
                    exit.commit();
                });
                insertOnDatabase.start();
            } else {
                Toast.makeText(this.getActivity(), "No se ha podido encontrar el directorio", Toast.LENGTH_SHORT).show();
            }
        }
    });

    @Override
    public void onStart() {
        super.onStart();
        this.openDocumentTree = getActivity().findViewById(R.id.OpenDocumentTree);
        this.openDocumentTree.setOnClickListener(view ->
                openDirectoryResult.launch(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_directory_config, container, false);
    }
}