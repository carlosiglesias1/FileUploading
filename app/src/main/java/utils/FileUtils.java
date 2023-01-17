package utils;

import java.io.File;

public class FileUtils {
    public static String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf("."));
    }

    public static int getFileType(String filename) {
        int fileType = FileTypes.OTHER;
        String imageExtensions = ".jpg | .jpeg | .png | .webp";
        String pdfExtensions = ".pdf";
        String videoExtensions = ".mp4 | .avi | .wmv | .m4v";
        if (imageExtensions.contains(getFileExtension(filename)))
            fileType = FileTypes.IMAGE;
        if (pdfExtensions.contains(getFileExtension(filename)))
            fileType = FileTypes.PDF;
        if (videoExtensions.contains(getFileExtension(filename)))
            fileType = FileTypes.VIDEO;
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

    public interface FileTypes {
        int OTHER = 0;
        int IMAGE = 1;
        int PDF = 2;
        int VIDEO = 3;
    }
}
