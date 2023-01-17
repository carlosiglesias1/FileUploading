package ftpmanagement;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

import persistence.Database;
import persistence.DatabaseAccess;
import persistence.entities.ClientData;

public class FtpSesion {
    private static FtpSesion instance;
    private ClientData clientData;
    private String rutaActualFtp;
    private FTPClient clienteFtp;

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
        Database mydb = DatabaseAccess.getInstance(applicationContext).getDatabase();
        this.clientData = mydb.ftpDao().getActiveFtp();
        mydb.close();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(alertDialog::dismiss);

    }

    public ClientData getClientData() throws NullPointerException {
        return clientData;
    }

    public void setClientData(ClientData clientData) {
        this.clientData = clientData;
    }

    public FTPClient getClienteFtp() throws IOException, NullPointerException {
        if (this.clienteFtp == null || !this.clienteFtp.isConnected()) {
            ClientData clientData = FtpSesion.getInstance().getClientData();
            if (this.clientData != null) {
                this.clienteFtp = new FTPClient();
                this.clienteFtp.setConnectTimeout(1200);
                this.clienteFtp.connect(clientData.FtpHostName, clientData.FtpPort);
                this.clienteFtp.setKeepAlive(true);
                this.clienteFtp.login(clientData.FtpUser, clientData.FtpPassword);
            }
        }
        return this.clienteFtp;
    }

    public String getRutaActualFtp() {
        return rutaActualFtp;
    }

    public void setRutaActualFtp(String rutaActualFtp) {
        this.rutaActualFtp = rutaActualFtp;
    }
}
