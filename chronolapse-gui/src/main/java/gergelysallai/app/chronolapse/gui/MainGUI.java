package gergelysallai.app.chronolapse.gui;

import gergelysallai.app.chronolapse.ProgressUpdateListener;
import gergelysallai.app.chronolapse.media.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainGUI extends JFrame {

    private JPanel rootPanel;
    private JButton GOButton;
    private JTextField pathField;
    private JButton folderButton;

    private static final Logger logger = LoggerFactory.getLogger(MainGUI.class.getName());

    public MainGUI() {
        super("ChronoLapse");
        logger.info("START MainUI");
        setContentPane(rootPanel);
        pack();

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        folderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("onClick");
                JFileChooser chooser = new JFileChooser();

                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setCurrentDirectory(new File("/home/tompi/ssd/fenykepek/teszt"));
                int returnVal = chooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    logger.info("You chose to open this file: " +
                            chooser.getSelectedFile().getAbsolutePath());
                    pathField.setText(chooser.getSelectedFile().getAbsolutePath());
                } else {
                    logger.info("ERROR");
                }

            }
        });

        GOButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("StartMagic: " + pathField.getText());
                //StartMagic(chooser.getSelectedFile().getAbsolutePath());
            }
        });


        setVisible(true);
    }


    public void StartMagic(String rootDir) {
        File file = new File(rootDir);
        ImageSelector.ImageType imageType = ImageSelector.ImageType.JPG;
        ImageSize imageSize = new ImageSize(1280, 720);

        ProgressUpdateListener imageLoaderListener = new ProgressUpdateListener() {
            @Override
            public void onProgressUpdate(float percentCompleted) {
                logger.info(String.format("Images loaded and scaled: %.2f", percentCompleted));
            }

            @Override
            public void onCompleted() {
                logger.info("Image load and scale completed");
            }
        };
        ProgressUpdateListener encodeListener = new ProgressUpdateListener() {
            @Override
            public void onProgressUpdate(float percentCompleted) {
                logger.info(String.format("Encoding progress: %.2f", percentCompleted));
            }

            @Override
            public void onCompleted() {
                logger.info("Encoding completed");
            }
        };
        ExecutorService executor = Executors.newSingleThreadExecutor();

        ImageSelector imageSelector = new ImageSelector(new Comparators.FileDateAscending());
        Collection<File> images = imageSelector.getFiles(file, imageType);
        ProgressUpdateHandler progressUpdateHandler = new ProgressUpdateHandler(images.size(), imageLoaderListener, encodeListener, executor);
        ImageLoader imageLoader = new ImageLoader(images, imageSize, 100, progressUpdateHandler);
        imageLoader.start();

        String path;
        if (file.isDirectory()) {
            path = file.getPath();
        } else {
            path = file.getParent();
        }

        final DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd HH-mm-ss");
        final Date date = new Date();
        final String videoFile = String.format("%s/Timelapse-%s.mp4", path, dateFormat.format(date));

        VideoCreator videoCreator = new VideoCreator(videoFile, imageSize, VideoCreator.FrameRate.FPS25, imageLoader, progressUpdateHandler);
        videoCreator.start();

        try {
            videoCreator.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();
        logger.info("Everything completed, shutting down. Video file: {}", videoFile);
    }

}
