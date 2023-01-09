package ftpmanagement.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ftpmanagement.FtpSesion;
import persistence.entities.ClientData;

public class TaskGetFile extends FtpTask {

    public TaskGetFile() throws IOException {
        super();
    }

    public Bitmap getImagePreview(String filename) throws IOException {
        InputStream inputStream;
        Bitmap buffer = null;
        if (!this.ftpClient.isConnected()) {
            ClientData clientData = FtpSesion.getInstance().getClientData();
            this.ftpClient.connect(clientData.FtpHostName, 21);
            this.ftpClient.login(clientData.FtpUser, clientData.FtpPassword);
        }
        if (this.ftpClient.getDataConnectionMode() != FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE)
            this.ftpClient.enterLocalPassiveMode();
        this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        inputStream = this.ftpClient.retrieveFileStream(filename);
        buffer = BitmapFactory.decodeStream(inputStream);
        inputStream.close();
        if (!this.ftpClient.completePendingCommand())
            buffer = null;
        return buffer;
    }

    public File downloadFile(String filename, Context context) throws IOException {
        String localFile = filename.substring(filename.lastIndexOf("/"));
        File temporalFile = new File(context.getCacheDir(), localFile);
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(temporalFile));
        this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        boolean fileRetrieved = this.ftpClient.retrieveFile(filename, outputStream);
        outputStream.close();
        if (fileRetrieved) {
            return temporalFile;
        } else {
            return null;
        }
    }
}
