package ftpmanagement.tasks;

public interface FtpTask {
    int FTP_TASK_UPLOAD = 0;
    int FTP_TASK_LISTDIR = 1;
    void run();
}
