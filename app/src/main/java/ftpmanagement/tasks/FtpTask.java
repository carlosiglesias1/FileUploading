package ftpmanagement.tasks;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

import ftpmanagement.FtpSesion;

public abstract class FtpTask {

    protected FTPClient ftpClient;

    public FtpTask() throws IOException, NullPointerException {
        this.ftpClient = FtpSesion.getInstance().getClienteFtp();
    }
}
