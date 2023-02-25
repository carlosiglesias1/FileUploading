package utils.filelistview;

import android.content.Context;
import android.graphics.drawable.Icon;
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
        Icon icon = null;
        if (!currentNumberPosition.isDirectory()) {
            switch (FileUtils.getFileType(filename)) {
                case FileUtils.FileTypes.IMAGE:
                    icon = Icon.createWithResource(this.getContext(), R.drawable.image);
                    icon.setTint(this.getContext().getColor(R.color.soft_green));
                    break;
                case FileUtils.FileTypes.VIDEO:
                    icon = Icon.createWithResource(this.getContext(), R.drawable.video);
                    icon.setTint(this.getContext().getColor(R.color.soft_orange));
                    break;
                case FileUtils.FileTypes.PDF:
                    icon = Icon.createWithResource(this.getContext(), R.drawable.pdf);
                    icon.setTint(this.getContext().getColor(R.color.soft_magenta));
                    break;

                default:
                    icon = Icon.createWithResource(this.getContext(), R.drawable.unknown_file_type);
                    icon.setTint(this.getContext().getColor(R.color.bold_blue));
                    break;
            }
        } else {
            icon = Icon.createWithResource(this.getContext(), R.drawable.directory);
            icon.setTint(this.getContext().getColor(R.color.light_purple));
        }
        currentImage.setImageIcon(icon);
        TextView currentTextView = currentItemView.findViewById(R.id.FileName);
        currentTextView.setText(filename);
        return currentItemView;
    }
}
