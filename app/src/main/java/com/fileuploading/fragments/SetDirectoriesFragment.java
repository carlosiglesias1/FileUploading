package com.fileuploading.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.fileuploading.R;

import java.util.ArrayList;
import java.util.List;

import persistence.Database;
import persistence.DatabaseAccess;
import persistence.entities.Directory;

public class SetDirectoriesFragment extends Fragment {
    private Button addDirectory;
    private ListView directoriesList;
    private Directory[] directories;
    private ArrayAdapter<String> adapter;
    private List<String> directoryNames;

    @Override
    public void onStart() {
        super.onStart();
        this.addDirectory = this.getActivity().findViewById(R.id.AddDirectory);
        this.directoriesList = this.getActivity().findViewById(R.id.DirectoriesList);
        this.addDirectory.setOnClickListener(v -> {
            DirectoryConfigFragment directoryConfigFragment = new DirectoryConfigFragment();
            FragmentTransaction configureNewFolder = this.getActivity().getSupportFragmentManager().beginTransaction();
            configureNewFolder.replace(R.id.FragmentViewer, directoryConfigFragment);
            configureNewFolder.commit();
        });
        this.directoriesList.setOnItemClickListener((adapterView, view, i, l) -> {
            PopupMenu popupMenu = new PopupMenu(SetDirectoriesFragment.this.getActivity(), view);
            popupMenu.getMenuInflater().inflate(R.menu.directories_context_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                //Borrar de base de datos y recargar la lista
                Thread deleteFromDatabase = new Thread(() -> {
                    Database appdb = DatabaseAccess.getInstance(this.getActivity()).getDatabase();
                    Directory directory = appdb.directoryDao().getDirectoryByName(SetDirectoriesFragment.this.directoryNames.get(i));
                    appdb.directoryDao().deleteDirectory(directory);
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
        this.loadDirectories();
    }

    protected void loadDirectories() {
        Thread loadFromDataBase = new Thread(() -> {
            Database myAppDb = DatabaseAccess.getInstance(this.getContext()).getDatabase();
            this.directories = myAppDb.directoryDao().getAllDirectories();
        });
        loadFromDataBase.start();
        try {
            loadFromDataBase.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (this.directories != null) {
            this.directoryNames = new ArrayList<>();
            for (Directory directory : this.directories) {
                this.directoryNames.add(directory.phoneDirectory);
            }
            this.adapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, this.directoryNames);
            this.directoriesList.setAdapter(adapter);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_set_directories, container, false);
    }
}