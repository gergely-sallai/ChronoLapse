package gergelysallai.app.chronolapse.gui;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import gergelysallai.app.chronolapse.ProgressUpdateListener;
import gergelysallai.app.chronolapse.media.*;

import javax.swing.*;
import java.awt.event.*;

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
    private JLabel label1;
    private JProgressBar progressBarEncoder;
    private JProgressBar progressBarImageLoader;
    private JTextField textFieldOutputFilename;
    private JLabel outputFilename;
    private JCheckBox dateCheckBox;
    private JCheckBox timeCheckBox;
    private JLabel labelFileExists;
    private final SettableFuture<Void> encodingCompleted = SettableFuture.create();

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
                    if(textFieldOutputFilename.getText().equals("")) {
                        textFieldOutputFilename.setText(chooser.getSelectedFile().getName());
                    }
                    updateOutputFilename();
                } else {
                    logger.info("ERROR");
                }

            }
        });

        GOButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info(String.format("StartMagic: %s/%s", pathField.getText(), outputFilename.getText()));
                StartMagic( pathField.getText(), outputFilename.getText());
            }
        });

        textFieldOutputFilename.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                updateOutputFilename();
            }

            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
            }
        });

        dateCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateOutputFilename();
            }
        });

        timeCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateOutputFilename();
            }
        });


        updateOutputFilename();
        setVisible(true);
    }

    public void updateOutputFilename() {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
        final DateFormat timeFormat = new SimpleDateFormat("HH-mm-ss");
        final Date date = new Date();
        //String filaname = String.format("%s-%s.mp4", textFieldOutputFilename.getText(), dateFormat.format(date));
        String filename = textFieldOutputFilename.getText();
        if(dateCheckBox.isSelected()) {
            filename += "_" + dateFormat.format(date);
        }

        if(timeCheckBox.isSelected()) {
            filename += "_" + timeFormat.format(date);
        }
        filename += ".mp4";
        outputFilename.setText(filename);

        checkFileExists();
    }

    public void checkFileExists() {

        String filePathString = String.format("%s%s%s",  pathField.getText(),File.separator, outputFilename.getText());
        File f = new File(filePathString);
        if(f.exists() && !f.isDirectory()) {
            logger.warn("file exists");
            labelFileExists.setVisible(true);

        } else {
            labelFileExists.setVisible(false);
        }
    }



    public void StartMagic(String rootDir, String filename) {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        Futures.addCallback(encodingCompleted, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                logger.info("executor shutdown");
                executor.shutdown();
            }

            @Override
            public void onFailure(Throwable t) {
                executor.shutdown();
            }
        });

        File file = new File(rootDir);
        ImageSelector.ImageType imageType = ImageSelector.ImageType.JPG;
        ImageSize imageSize = new ImageSize(1280, 720);

        ProgressUpdateListener imageLoaderListener = new ProgressUpdateListener() {
            @Override
            public void onProgressUpdate(float percentCompleted) {
                logger.info(String.format("Images loaded and scaled: %.2f", percentCompleted));
                progressBarImageLoader.setValue((int) percentCompleted);
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
                progressBarEncoder.setValue((int) percentCompleted);
            }

            @Override
            public void onCompleted() {
                logger.info("Encoding completed");
                encodingCompleted.set(null);
            }
        };


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

        final String videoFile = String.format("%s/%s", path, filename);

        VideoCreator videoCreator = new VideoCreator(videoFile, imageSize, VideoCreator.FrameRate.FPS25, imageLoader, progressUpdateHandler);
        videoCreator.start();

        logger.info("Everything completed, shutting down. Video file: {}", videoFile);
    }

}
