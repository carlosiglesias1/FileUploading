package ftpmanagement;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;

import persistence.Database;
import persistence.DatabaseAccess;
import persistence.entities.ClientData;

public class FtpSesion {
    private static FtpSesion instance;
    private ClientData clientData;
    private String rutaActualFtp;
    private Thread loadFtpData;

    private FtpSesion() {
        this.rutaActualFtp = "/";
    }

    public static FtpSesion getInstance() {
        if (instance == null) {
            instance = new FtpSesion();
        }
        return instance;
    }

    /**
     * Carga los datos del cliente Ftp activo, como es una acción realizada en un hilo a parte
     * requiere del Dialog que debe ser cerrado al finalizar la carga.
     *
     * @param applicationContext
     * @param alertDialog
     */
    public void loadClientData(Context applicationContext, AlertDialog alertDialog) {
        loadFtpData = new Thread(() -> {
            Database mydb = DatabaseAccess.getInstance(applicationContext).getDatabase();
            this.clientData = mydb.ftpDao().getActiveFtp();
            mydb.close();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(alertDialog::dismiss);
        });
        loadFtpData.start();
        try {
            loadFtpData.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isSesionLoaded() {
        return loadFtpData != null && loadFtpData.getState().equals(Thread.State.TERMINATED);
    }

    public ClientData getClientData() throws NullPointerException {
        if (!this.isSesionLoaded()) {
            throw new NullPointerException("No se ha cargado la sesión");
        }
        return clientData;
    }

    public void setClientData(ClientData clientData) {
        this.clientData = clientData;
    }

    public String getRutaActualFtp() {
        return rutaActualFtp;
    }
}
