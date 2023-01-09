package ftpmanagement.tasks;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

public class TaskUploadFile extends FtpTask {
    private boolean fileUploaded;

    public TaskUploadFile() throws IOException {
        super();
    }

    public void uploadFile(File file, String locationInServer, boolean foldUp) {
        try {
            this.fileUploaded = false;
            FileInputStream inputStream = new FileInputStream(file);
            String filename = getFilename(file);
            String remotePath = setLocationInFtpStorage(file, locationInServer, foldUp);
            String remoteFile = remotePath + filename;
            checkDestinationFolders(remotePath);
            this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            if (this.ftpClient.getDataConnectionMode() != FTPClient.PASSIVE_REMOTE_DATA_CONNECTION_MODE)
                this.ftpClient.enterLocalPassiveMode();
            if (this.ftpClient.storeFile(remoteFile, inputStream)) {
                System.out.println("File uploaded");
                this.fileUploaded = true;
            } else {
                System.out.println("Error ftp: " + this.ftpClient.getReplyString());
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

    private String setLocationInFtpStorage(File file, String serverPath, boolean foldUp) {
        String location = serverPath;
        if (foldUp) {
            try {
                BasicFileAttributes fileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                String fileDate = fileAttributes.creationTime().toString();
                String[] fileDateSplit = fileDate.split("-");
                for (String fileDatePart : fileDateSplit) {
                    if (fileDatePart.length() == 4) {
                        location += fileDatePart + "/";
                        break;
                    }
                }
                System.out.println(fileDate);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return location; //this.getYear(filename) + "/" + filename;
    }

    private void checkDestinationFolders(String remoteFolder) throws IOException {
        String[] remoteFolderSplit = remoteFolder.split("/");
        String[] remoteFolders = remoteFolder.split("/");
        for (int i = 0; i < remoteFolders.length; i++) {
            String pathToFolder = "";
            for (int j = 0; j < i; j++) {
                pathToFolder += remoteFolderSplit[j] + "/";
            }
            remoteFolders[i] = pathToFolder + remoteFolders[i] + "/";

        }
        for (String directory : remoteFolders) {
            this.ftpClient.listFiles(directory);
            String reply = this.ftpClient.getReplyString();
            if (reply.toLowerCase().contains("error") || reply.contains("not found")) {
                this.ftpClient.makeDirectory(directory);
            }
        }
    }

    private String getFilename(File file) {
        String[] fileNameParts = file.getPath().split("/");
        return fileNameParts[fileNameParts.length - 1];
    }

    public boolean isFileUploaded() {
        return fileUploaded;
    }
}
