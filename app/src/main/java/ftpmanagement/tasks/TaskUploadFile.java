package ftpmanagement.tasks;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;

public class TaskUploadFile {
    private FTPClient ftpClient;
    private File fileToUpload;

    public TaskUploadFile(FTPClient ftpClient, File fileToUpload){
        this.fileToUpload = fileToUpload;
        this.ftpClient = ftpClient;
    }
}
