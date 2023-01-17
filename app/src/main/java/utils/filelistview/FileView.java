package utils.filelistview;

public class FileView {
    private int imageResId;
    private String filename;

    public FileView (int imageResId, String filename){
        this.imageResId  = imageResId;
        this.filename = filename;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getFilename() {
        return filename;
    }
}
