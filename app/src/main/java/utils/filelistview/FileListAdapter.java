package utils.filelistview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fileuploading.R;

import org.apache.commons.net.ftp.FTPFile;

import java.util.List;

import utils.FileUtils;

public class FileListAdapter extends ArrayAdapter<FTPFile> {

    public FileListAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull FTPFile[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public FileListAdapter(@NonNull Context context, @NonNull List<FTPFile> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View currentItemView = convertView;
        if (currentItemView == null) {
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.file_list_element_layout, parent, false);
        }
        FTPFile currentNumberPosition = getItem(position);
        ImageView currentImage = currentItemView.findViewById(R.id.FileTypeIcon);
        String filename = currentNumberPosition.getName();
        if (!currentNumberPosition.isDirectory()) {
            switch (FileUtils.getFileType(filename)) {
                case FileUtils.FileTypes.IMAGE:
                    currentImage.setImageResource(R.drawable.image);
                    break;
                case FileUtils.FileTypes.VIDEO:
                    currentImage.setImageResource(R.drawable.video);
                    break;
                case FileUtils.FileTypes.OTHER:
                    currentImage.setImageResource(R.drawable.unknown_file_type);
                    break;
                case FileUtils.FileTypes.PDF:
                    currentImage.setImageResource(R.drawable.pdf);
                    break;
            }
        } else {
            currentImage.setImageResource(R.drawable.directory);
        }
        TextView currentTextView = currentItemView.findViewById(R.id.FileName);
        currentTextView.setText(filename);
        return currentItemView;
    }
}
