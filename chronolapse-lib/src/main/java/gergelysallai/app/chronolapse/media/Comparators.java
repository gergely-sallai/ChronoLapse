package gergelysallai.app.chronolapse.media;

import java.io.File;
import java.util.Comparator;

public class Comparators {

    private Comparators() {
        // prevent instantiation
    }

    public static class FileDateAscending implements Comparator<File> {

        @Override
        public int compare(File f1, File f2) {
            return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
        }
    }

    public static class FileDateDescending implements Comparator<File> {

        @Override
        public int compare(File f1, File f2) {
            return -1 * Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
        }
    }
}
