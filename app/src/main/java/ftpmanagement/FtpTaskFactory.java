package ftpmanagement;

import java.io.IOException;

import ftpmanagement.tasks.TaskGetFile;
import ftpmanagement.tasks.TaskListDirectory;
import ftpmanagement.tasks.TaskUploadFile;

public class FtpTaskFactory {

    public static TaskListDirectory getTaskListDir() throws IOException {
        return new TaskListDirectory();
    }

    public static TaskUploadFile getTaskUploadFile() throws IOException {
        return new TaskUploadFile();
    }

    public static TaskGetFile getTaskGetFile() throws IOException {
        return new TaskGetFile();
    }
}
