package ftpmanagement.tasks;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;

import ftpmanagement.FtpSesion;

public class TaskListDirectory extends FtpTask {

    private FTPFile[] fileList;

    public TaskListDirectory() throws IOException, NullPointerException {
        super();
    }

    public FTPFile[] getFileList() {
        FTPFile[] files = null;
        try {
            if (this.ftpClient.getDataConnectionMode() != FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE)
                this.ftpClient.enterLocalPassiveMode();
            files = this.ftpClient.listFiles(FtpSesion.getInstance().getRutaActualFtp());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }
}
