package ftpmanagement.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

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
import utils.FileUtils;

public class TaskGetFile extends FtpTask {

    public TaskGetFile() throws IOException {
        super();
    }

    public Bitmap getImagePreview(String filename) throws IOException {
        InputStream inputStream;
        Bitmap buffer;
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
        if (inputStream != null)
            inputStream.close();
        if (!this.ftpClient.completePendingCommand())
            buffer = null;
        return buffer;
    }

    public File downloadFile(@NonNull String p_filename, @NonNull String p_destination) throws IOException {
        File fileDownloaded = null;
        String localFileName = p_filename.substring(p_filename.lastIndexOf("/") + 1);
        if (FileUtils.isDestinationReady(p_destination)) {
            File localFile = new File(p_destination + localFileName);
            if (localFile.createNewFile() || localFile.exists()) {
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(localFile));
                this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                boolean fileRetrieved = this.ftpClient.retrieveFile(p_filename, outputStream);
                outputStream.close();
                if (fileRetrieved) {
                    fileDownloaded = localFile;
                }
            }
        }
        return fileDownloaded;
    }
}
