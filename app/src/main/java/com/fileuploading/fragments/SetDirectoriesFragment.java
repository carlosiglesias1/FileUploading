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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.example.fileuploading.R;

import java.util.ArrayList;
import java.util.List;

import persistence.Database;
import persistence.entities.Directory;

/**
 * A simple {@link Fragment} subclass.
 */
public class SetDirectoriesFragment extends Fragment {
    private Button addDirectory;
    private ListView directoriesList;
    private Directory[] directories;
    private ArrayAdapter<String> adapter;
    private List<String> directoryNames;
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
                    Database database = Room.databaseBuilder(SetDirectoriesFragment.this.getContext(), Database.class, Database.DATABASE_NAME).build();
                    database.directoryDao().insertDirectory(directory);
                    database.close();
                });
                insertOnDatabase.start();
                try {
                    insertOnDatabase.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.loadDirectories();
            } else {
                Toast.makeText(this.getActivity(), "No se ha podido encontrar el directorio", Toast.LENGTH_SHORT).show();
            }
        }
    });

    @Override
    public void onStart() {
        super.onStart();
        this.addDirectory = this.getActivity().findViewById(R.id.AddDirectory);
        this.directoriesList = this.getActivity().findViewById(R.id.DirectoriesList);
        this.addDirectory.setOnClickListener(v -> {
            openDirectoryResult.launch(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE));
        });
        this.directoriesList.setOnItemClickListener((adapterView, view, i, l) -> {
            PopupMenu popupMenu = new PopupMenu(SetDirectoriesFragment.this.getActivity(), view);
            popupMenu.getMenuInflater().inflate(R.menu.directories_context_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                //Borrar de base de datos y recargar la lista
                Thread deleteFromDatabase = new Thread(() -> {
                    Database appdb = Room.databaseBuilder(this.getActivity(), Database.class, Database.DATABASE_NAME).build();
                    Directory directory = appdb.directoryDao().getDirectoryByName(SetDirectoriesFragment.this.directoryNames.get(i));
                    appdb.directoryDao().deleteDirectory(directory);
                    appdb.close();
                });
                deleteFromDatabase.start();
                try {
                    deleteFromDatabase.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SetDirectoriesFragment.this.directoryNames.remove(i);
                SetDirectoriesFragment.this.adapter.notifyDataSetChanged();
                return true;
            });
            popupMenu.show();
        });
        loadDirectories();
    }

    private void loadDirectories() {
        Thread loadFromDataBase = new Thread(() -> {
            Database appdb = Room.databaseBuilder(this.getActivity(), Database.class, Database.DATABASE_NAME).build();
            this.directories = appdb.directoryDao().getAllDirectories();
            appdb.close();
        });
        loadFromDataBase.start();
        try {
            loadFromDataBase.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (this.directories != null) {
            this.directoryNames = new ArrayList<>();
            for (int i = 0; i < this.directories.length; i++) {
                this.directoryNames.add(this.directories[i].phoneDirectory);
            }
            this.adapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, this.directoryNames);
            this.directoriesList.setAdapter(adapter);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_set_directories, container, false);
    }
}