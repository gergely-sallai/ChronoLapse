package gergelysallai.app.chronolapse.media;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.io.Files.getFileExtension;

public class ImageSelector {

    public enum ImageType {
        GIF("gif"),
        PNG("png"),
        JPEG("jpeg"),
        JPG("jpg"),
        BMP("bmp");

        public static Set<String> supportedExtensions = ImmutableSet.of(GIF.extension, PNG.extension, JPEG.extension, JPG.extension, BMP.extension);
        public final String extension;

        private ImageType(String extension) {
            this.extension = extension.toLowerCase();
        }
    }

    private final Comparator<File> fileComparator;

    public ImageSelector(Comparator<File> fileComparator) {
        this.fileComparator = fileComparator;
    }

    public Collection<File> getFiles(final File folder, final ImageType imageType) {
        checkNotNull(folder, "File can not be null");
        checkNotNull(imageType, "Extension can not be null");
        checkArgument(folder.isDirectory(), "folder should be a directory!");
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return getFileExtension(name).equalsIgnoreCase(imageType.extension);
            }
        };
        File[] files = folder.listFiles(filenameFilter);
        Arrays.sort(files, fileComparator);
        return ImmutableList.copyOf(files);
    }

    public Collection<File> getFiles(List<File> files) {
        checkNotNull(files, "Files can not be null");
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return ImageType.supportedExtensions.contains(getFileExtension(name).toLowerCase());
            }
        };
        ImmutableList.Builder<File> compatibleFiles = new ImmutableList.Builder<File>();
        for(File file: files) {
            if (filenameFilter.accept(file.getParentFile(), file.getName())) {
                compatibleFiles.add(file);
            }
        }
        return compatibleFiles.build();
    }
}
