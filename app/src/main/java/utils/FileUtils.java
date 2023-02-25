package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtils {
    public static String getFileExtension(String filename) {
        String extension = null;
        int indexOfExtension = filename.lastIndexOf(".");
        if (!(indexOfExtension < 0))
            extension = filename.substring(indexOfExtension);
        return extension;
    }

    public static int getFileType(String filename) {
        int fileType = FileTypes.OTHER;
        String imageExtensions = ".jpg | .jpeg | .png | .webp";
        String pdfExtensions = ".pdf";
        String videoExtensions = ".mp4 | .avi | .wmv | .m4v";
        String fileExtension = getFileExtension(filename);
        if (fileExtension != null) {
            fileExtension = fileExtension.toLowerCase();
            if (imageExtensions.contains(fileExtension))
                fileType = FileTypes.IMAGE;
            if (pdfExtensions.contains(fileExtension))
                fileType = FileTypes.PDF;
            if (videoExtensions.contains(fileExtension))
                fileType = FileTypes.VIDEO;
        }
        return fileType;
    }

    public static boolean isDestinationReady(String p_destinationPath) {
        boolean ready = false;
        File destinationFile = new File(p_destinationPath);
        if (!destinationFile.exists()) {
            if (destinationFile.mkdirs())
                ready = true;
        } else {
            ready = true;
        }
        return ready;
    }

    public static String getFileDateString(File file) {
        String fileDate = "";
        try {
            BasicFileAttributes fileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            fileDate = fileAttributes.creationTime().toString().split("T")[0];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileDate;
    }

    public interface FileTypes {
        int OTHER = 0;
        int IMAGE = 1;
        int PDF = 2;
        int VIDEO = 3;
    }
}
