package ftpmanagement.tasks;

import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;

import ftpmanagement.FtpSesion;

public class TaskListDirectory extends FtpTask {

    private FTPFile[] fileList;

    public TaskListDirectory() throws IOException {
        super();
    }

    public FTPFile[] getFileList() {
        FTPFile[] files = null;
        try {
            this.ftpClient.enterLocalPassiveMode();
            files = this.ftpClient.listFiles(FtpSesion.getInstance().getRutaActualFtp());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }
}
