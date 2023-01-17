package com.fileuploading.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import com.example.fileuploading.R;
import com.fileuploading.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import ftpmanagement.FtpSesion;
import persistence.Database;
import persistence.DatabaseAccess;
import persistence.entities.ClientData;

/**
 * A simple {@link Fragment} subclass.
 */
public class FtpConfigFragment extends Fragment {

    private TextInputEditText FtpName;
    private EditText FtpHost;
    private EditText ftpPort;
    private EditText FtpUser;
    private EditText FtpPass;
    private ClientData clientData;
    private Button saveFtp;

    public FtpConfigFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        this.findComponents();
        this.loadData();
        this.saveFtp.setOnClickListener(view -> {
            this.clientData.FtpName = this.FtpName.getText().toString();
            this.clientData.FtpHostName = this.FtpHost.getText().toString();
            if (this.ftpPort.getText().toString().isEmpty()) {
                this.clientData.FtpPort = 21;
            } else {
                this.clientData.FtpPort = Integer.parseInt(this.ftpPort.getText().toString());
            }
            this.clientData.FtpUser = this.FtpUser.getText().toString();
            this.clientData.FtpPassword = this.FtpPass.getText().toString();
            this.clientData.IsActive = true;
            Thread saveDataTask = new Thread(() -> {
                Database appdb = DatabaseAccess.getInstance(this.getContext()).getDatabase();
                if (appdb.ftpDao().getClient(this.clientData.id) != null) {
                    appdb.ftpDao().updateClient(this.clientData);
                } else {
                    appdb.ftpDao().instertFtp(this.clientData);
                }
            });
            saveDataTask.start();
            try {
                saveDataTask.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            FtpSesion.getInstance().setClientData(this.clientData);
            Toast.makeText(this.getActivity(), "Se han guardado los cambios satisfactoriamente", Toast.LENGTH_LONG).show();
            BottomNavigationView navigationView = ((MainActivity) this.getActivity()).getNavigationView();
            navigationView.getMenu().getItem(0).setChecked(true);
            FragmentTransaction transaction = this.getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.FragmentViewer, ((MainActivity) this.getActivity()).getHomeFragment());
            transaction.commit();
        });
    }

    private void findComponents() {
        this.FtpName = this.getActivity().findViewById(R.id.FtpName);
        this.FtpHost = this.getActivity().findViewById(R.id.FtpHost);
        this.FtpUser = this.getActivity().findViewById(R.id.FtpUser);
        this.FtpPass = this.getActivity().findViewById(R.id.FtpPass);
        this.saveFtp = this.getActivity().findViewById(R.id.SaveFtp);
        this.ftpPort = this.getActivity().findViewById(R.id.FtpPort);
    }

    private void loadData() {
        Thread loadFtpData = new Thread(() -> {
            Database mydb = Room.databaseBuilder(getActivity().getApplicationContext(), Database.class, Database.DATABASE_NAME).build();
            this.clientData = mydb.ftpDao().getActiveFtp();
            mydb.close();
        });
        loadFtpData.start();
        try {
            loadFtpData.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (this.clientData != null) {
            this.FtpHost.setText(this.clientData.FtpHostName);
            this.FtpName.setText(this.clientData.FtpName);
            this.FtpUser.setText(this.clientData.FtpUser);
            this.FtpPass.setText(this.clientData.FtpPassword);
        } else {
            this.clientData = new ClientData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ftp_configuration, container, false);
    }

}