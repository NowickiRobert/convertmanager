package com.mycompany.conver_manager;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ForkJoinPool;

import static java.lang.String.format;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.DoubleProperty;
import javafx.scene.control.Alert.AlertType;
import static javafx.scene.control.Alert.AlertType.ERROR;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;

public class BatchProcessingController implements Initializable {

    @FXML
    TableView<ImageProcessingJob> imageProcessingTable;
    @FXML
    TableColumn<ImageProcessingJob, String> imageNameColumn;
    @FXML
    TableColumn<ImageProcessingJob, Double> progressColumn;
    @FXML
    TableColumn<ImageProcessingJob, String> statusColumn;

    ObservableList<ImageProcessingJob> jobs = FXCollections.observableArrayList();

    File outputDir;
    File inputFile;
    List<File> selectedFiles;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        imageNameColumn.setCellValueFactory( //nazwa pliku
                p -> new SimpleStringProperty(p.getValue().getFile().getName()));
        statusColumn.setCellValueFactory( //status przetwarzania
                p -> p.getValue().getStatusProperty());
        progressColumn.setCellFactory( //wykorzystanie paska postępu
                ProgressBarTableCell.<ImageProcessingJob>forTableColumn());
        progressColumn.setCellValueFactory( //postęp przetwarzania
                p -> p.getValue().getProgressProperty().asObject());
        //...dalsze inicjalizacje...
        imageProcessingTable.setItems(jobs);
    }

    @FXML
    void addFile(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG images", "*.jpg"));
        selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles != null) {
            for (File f : selectedFiles) {
                jobs.add(new ImageProcessingJob(f, outputDir));
            }
        }
        imageProcessingTable.setItems(jobs);
    }

    @FXML
    void processing(ActionEvent event) {
        Window window = imageProcessingTable.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Wybierz katalog docelowy");
        outputDir = directoryChooser.showDialog(window);

        if (outputDir != null) {

            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Image converter");
            alert.setHeaderText(null);
            alert.setContentText("Choose the type of processing.");

            ButtonType buttonTypeOne = new ButtonType("Default Parallel");
            ButtonType buttonTypeTwo = new ButtonType("Personal Parallel");
            ButtonType buttonTypeThree = new ButtonType("Sequential");
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeThree, buttonTypeCancel);
            Optional<ButtonType> result = alert.showAndWait();

            Alert alertCompleted = new Alert(AlertType.INFORMATION);
            alertCompleted.setTitle("Processing has been completed");
            alertCompleted.setHeaderText(null);

            if (result.get() == buttonTypeOne) {
                //ForkJoinPool forkJoinPool = new ForkJoinPool();
                long start = System.currentTimeMillis(); //zwraca aktualny czas [ms]
                ForkJoinPool.commonPool().submit(() -> jobs.parallelStream().forEach(this::processing));
                Platform.runLater(() -> {
                    long duration = System.currentTimeMillis() - start; //czas przetwarzania [ms]
                    alertCompleted.setContentText("It takes " + Long.toString(duration));
                    alertCompleted.showAndWait();
                });

            } else if (result.get() == buttonTypeTwo) {
                int threadsNumber = 2;

                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Image converter");
                dialog.setHeaderText("How many threads do you want to use?");
                dialog.setContentText("Please enter your number:");

                // Traditional way to get the response value.
                Optional<String> resultInput = dialog.showAndWait();
                if (resultInput.isPresent()) {
                    threadsNumber = Integer.parseInt(resultInput.get());
                }

                alert.close();
                ForkJoinPool forkJoinPool = new ForkJoinPool(threadsNumber);
                long start = System.currentTimeMillis(); //zwraca aktualny czas [ms]
                forkJoinPool.submit(() -> jobs.parallelStream().forEach(this::processing));
                Platform.runLater(() -> {
                    long duration = System.currentTimeMillis() - start; //czas przetwarzania [ms]
                    alertCompleted.setContentText("It takes " + Long.toString(duration));
                    alertCompleted.showAndWait();
                });

            } else if (result.get() == buttonTypeThree) {
                ForkJoinPool forkJoinPool = new ForkJoinPool();
                long start = System.currentTimeMillis(); //zwraca aktualny czas [ms]
                forkJoinPool.submit(() -> jobs.stream().forEach(this::processing));
                //forkJoinPool.
                Platform.runLater(() -> {
                    long duration = System.currentTimeMillis() - start; //czas przetwarzania [ms]
                    alertCompleted.setContentText("It takes " + Long.toString(duration));
                    alertCompleted.showAndWait();
                });
            } else {
                alert.close();
            }

        }
    }

    private void processing(ImageProcessingJob job) {
        Platform.runLater(() -> job.setStatus(ImageProcessingJob.STATUS_INIT));

        File file = job.getFile();        
        convertToGrayscale(file, outputDir, job.getProgressProperty());
        //aktualizacja statusu na done
        Platform.runLater(() -> job.setStatus(ImageProcessingJob.STATUS_DONE));

    }

    private void showProcessingError(String filename) {
        Platform.runLater(() -> {
            Alert alert = new Alert(ERROR);
            alert.setTitle("Processing Error");
            alert.setHeaderText(null);
            alert.setContentText(format("Converting of %s file was unsuccessful", filename));
            alert.showAndWait();
        });
    }

    private void convertToGrayscale(File originalFile, File outputDir, DoubleProperty progressProp) {

        try {
            //wczytanie oryginalnego pliku do pamięci
            BufferedImage original = ImageIO.read(originalFile);

            //przygotowanie bufora na grafikę w skali szarości
            BufferedImage grayscale = new BufferedImage(
                    original.getWidth(), original.getHeight(), original.getType());
            //przetwarzanie piksel po pikselu
            for (int i = 0; i < original.getWidth(); i++) {
                for (int j = 0; j < original.getHeight(); j++) {
                    //pobranie składowych RGB
                    int red = new Color(original.getRGB(i, j)).getRed();
                    int green = new Color(original.getRGB(i, j)).getGreen();
                    int blue = new Color(original.getRGB(i, j)).getBlue();
                    //obliczenie jasności piksela dla obrazu w skali szarości
                    int luminosity = (int) (0.21 * red + 0.71 * green + 0.07 * blue);
                    //przygotowanie wartości koloru w oparciu o obliczoną jaskość
                    int newPixel
                            = new Color(luminosity, luminosity, luminosity).getRGB();
                    //zapisanie nowego piksela w buforze
                    grayscale.setRGB(i, j, newPixel);
                }
                //obliczenie postępu przetwarzania jako liczby z przedziału [0, 1]
                double progress = (1.0 + i) / original.getWidth();
                //aktualizacja własności zbindowanej z paskiem postępu w tabeli
                Platform.runLater(() -> progressProp.set(progress));
            }
            //przygotowanie ścieżki wskazującej na plik wynikowy
            Path outputPath
                    = Paths.get(outputDir.getAbsolutePath(), originalFile.getName());

            //zapisanie zawartości bufora do pliku na dysku
            ImageIO.write(grayscale, "jpg", outputPath.toFile());

        } catch (IOException ex) {
            showProcessingError(originalFile.getName());
            //translacja wyjątku
            throw new RuntimeException(ex);
        }
    }

}
