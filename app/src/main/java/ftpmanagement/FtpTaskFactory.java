package ftpmanagement;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

import ftpmanagement.tasks.FtpTask;
import persistence.entities.ClientData;

public class FtpTaskFactory {
    private File file;
    private boolean taskCompleted;
    private int task;
    private String serverPath;
    private FTPFile[] fileList;

    public FtpTaskFactory(int task) {
        this.task = task;
        this.taskCompleted = false;
    }

    public void run() {
        try {
            FTPClient client = new FTPClient();
            ClientData clientData = FtpSesion.getInstance().getClientData();
            client.setConnectTimeout(1200);
            client.connect(clientData.FtpHostName, 21);
            client.login(clientData.FtpUser, clientData.FtpPassword);
            if (client.isConnected()) {
                System.out.println("Conexión establecida: " + client.getReplyCode());
                switch (task) {
                    case FtpTask.FTP_TASK_UPLOAD:
                        this.uploadFile(client);
                        break;
                    case FtpTask.FTP_TASK_LISTDIR:
                        client.sendCommand("PWD");
                        System.out.println(client.getReplyString());
                        this.fileList = this.getFileList(client);
                        break;
                }
                client.logout();
                client.disconnect();
            } else {
                System.out.println("No se ha podido establecer conexion " + client.getReplyCode());
            }
        } catch (IOException ex) {
            String message = "Unhandled Exception: " + ex.getMessage();
            if (ex instanceof SocketException) {
                message = "Invalid Socket" + ex.getMessage();
            }
            System.out.println(message);
            ex.printStackTrace();
        } catch (NullPointerException exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
        }
    }

    private FTPFile[] getFileList(FTPClient client) {
        FTPFile[] files = new FTPFile[1];
        try {
            client.enterLocalPassiveMode();
            files = client.listFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    private void uploadFile(FTPClient client) {
        try {
            System.out.println("SYSTEM TYPE:\n " + client.getSystemType());
            FileInputStream inputStream = new FileInputStream(this.file);
            String remoteFile = setLocationInFtpStorage();
            client.setFileType(FTP.BINARY_FILE_TYPE);
            client.enterLocalPassiveMode();
            if (client.storeFile(remoteFile, inputStream)) {
                System.out.println("File uploaded");
                this.taskCompleted = true;
            } else {
                System.out.println("Error ftp: " + client.getReplyString());
            }
            inputStream.close();
        } catch (IOException ex) {
            String message = "Unhandled Exception: " + ex.getMessage();
            if (ex instanceof SocketException) {
                message = "Invalid Socket" + ex.getMessage();
            }
            System.out.println(message);
            ex.printStackTrace();
        }
    }

    private String setLocationInFtpStorage() {
        String location = this.serverPath;
        String[] fileNameParts = this.file.getPath().split("/");
        String filename = fileNameParts[fileNameParts.length - 1];
        try {
            BasicFileAttributes fileAttributes = Files.readAttributes(this.file.toPath(), BasicFileAttributes.class);
            String fileDate = fileAttributes.creationTime().toString();
            System.out.println(fileDate);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return location + filename; //this.getYear(filename) + "/" + filename;
    }

    private String getYear(String fileName) {
        String[] split = fileName.split("-");
        return split[1].substring(0, 3);
    }

    public boolean isTaskCompleted() {
        return this.taskCompleted;
    }

    public FtpTaskFactory setFile(File file, String remotePath) {
        this.file = file;
        this.serverPath = remotePath;
        return this;
    }

    public FTPFile[] getFiles() {
        return this.fileList;
    }
}
