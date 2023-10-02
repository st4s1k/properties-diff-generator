package com.st4s1k.propertiesdiffgenerator;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class PropertiesDiffGeneratorController {

    /*
     * Current properties
     */

    @FXML
    private TextField currentPropsTextField;

    @FXML
    private Button currentPropsImportFileButton;

    private FileChooser currentPropsFileChooser = new FileChooser();

    private File currentPropsFile;

    @FXML
    private TextArea currentPropsTextArea;

    /*
     * New properties
     */

    @FXML
    private TextField newPropsTextField;

    @FXML
    private Button newPropsImportFileButton;

    private FileChooser newPropsFileChooser = new FileChooser();

    private File newPropsFile;

    @FXML
    private TextArea newPropsTextArea;

    /*
     * Diff properties
     */

    @FXML
    private Button diffPropsSaveAsButton;

    private FileChooser diffPropsFileChooser = new FileChooser();

    private File diffPropsDirectory;

    @FXML
    private TextArea diffPropsTextArea;

    /*
     * Other controls
     */

    @FXML
    private Label generateStatusLabel;

    @FXML
    protected void onCurrentPropsImportFileButtonClick() {
        if (currentPropsTextField.getText().length() > 0) {
            currentPropsFileChooser.setInitialDirectory(currentPropsFile.getParentFile());
        }
        currentPropsFileChooser.setTitle("Import current properties file");
        currentPropsFile = currentPropsFileChooser.showOpenDialog(currentPropsImportFileButton.getScene().getWindow());
        if (currentPropsFile != null) {
            currentPropsTextField.setText(currentPropsFile.getPath());
            try {
                currentPropsTextArea.setText(new String(Files.readAllBytes(currentPropsFile.toPath())));
            } catch (IOException e) {
                showErrorDialog(e);
            }
        }
    }

    @FXML
    protected void onNewPropsImportFileButtonClick() {
        if (newPropsTextField.getText().length() > 0) {
            newPropsFileChooser.setInitialDirectory(newPropsFile.getParentFile());
        }
        newPropsFileChooser.setTitle("Import new properties file");
        newPropsFile = newPropsFileChooser.showOpenDialog(newPropsImportFileButton.getScene().getWindow());
        if (newPropsFile != null) {
            newPropsTextField.setText(newPropsFile.getPath());
            try {
                newPropsTextArea.setText(new String(Files.readAllBytes(newPropsFile.toPath())));
            } catch (IOException e) {
                showErrorDialog(e);
            }
        }
    }

    @FXML
    protected void onDiffPropsSaveAsButtonClick() {
        diffPropsFileChooser.setTitle("Save diff properties file as...");
        // .properties file
        diffPropsFileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Properties file", "*.properties"));
        diffPropsDirectory = diffPropsFileChooser.showSaveDialog(diffPropsSaveAsButton.getScene().getWindow());
        if (diffPropsDirectory != null) {
            try {
                Properties diffProperties = readPropertiesFromTextArea(diffPropsTextArea);
                writePropertiesToFile(diffProperties, diffPropsDirectory.getPath());
                generateStatusLabel.setVisible(true);
            } catch (IOException e) {
                showErrorDialog(e);
            }
        }
    }

    @FXML
    protected void onGenerateButtonClick() {
        generateDiffProperties();
    }

    private void generateDiffProperties() {
        try {
            Properties currentProperties = readPropertiesFromTextArea(currentPropsTextArea);
            Properties newProperties = readPropertiesFromTextArea(newPropsTextArea);
            Properties diffProperties = computeDiffProperties(currentProperties, newProperties);
            writePropertiesToTextArea(diffProperties, diffPropsTextArea);
        } catch (IOException e) {
            showErrorDialog(e);
        }
    }

    private Properties computeDiffProperties(
            Properties currentProperties,
            Properties newProperties
    ) {
        Properties diffProperties = new Properties();
        Map<Object, Object> diffPropertiesMap = newProperties.entrySet().stream()
                .filter(entry -> !currentProperties.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        diffProperties.putAll(diffPropertiesMap);
        return diffProperties;
    }

    private void writePropertiesToFile(Properties properties, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        try (OutputStream output = Files.newOutputStream(path)) {
            properties.store(output, null);
        }
    }

    private Properties readPropertiesFromTextArea(TextArea textArea) throws IOException {
        try (InputStream input = new ByteArrayInputStream(textArea.getText().getBytes())) {
            Properties prop = new Properties();
            prop.load(input);
            return prop;
        }
    }

    private void writePropertiesToTextArea(Properties properties, TextArea textArea) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        properties.store(baos, null);
        textArea.setText(baos.toString());
    }

    private void showErrorDialog(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    public void load() {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(getStateFilePath())) {
            properties.load(input);
            // Current
            currentPropsTextField.setText(properties.getProperty("currentPropsFile", ""));
            currentPropsTextArea.setText(properties.getProperty("currentPropsTextArea", ""));
            // New
            newPropsTextField.setText(properties.getProperty("newPropsFile", ""));
            newPropsTextArea.setText(properties.getProperty("newPropsTextArea", ""));
            // Diff
            diffPropsTextArea.setText(properties.getProperty("diffPropsTextArea", ""));
        } catch (IOException ignored) {
            // Ignore if the config file does not exist
        } catch (PDGException e) {
            showErrorDialog(e);
        }
    }

    public void save() {
        Properties properties = new Properties();
        // Current
        properties.setProperty("currentPropsFile", currentPropsTextField.getText());
        properties.setProperty("currentPropsTextArea", currentPropsTextArea.getText());
        // New
        properties.setProperty("newPropsFile", newPropsTextField.getText());
        properties.setProperty("newPropsTextArea", newPropsTextArea.getText());
        // Diff
        properties.setProperty("diffPropsTextArea", diffPropsTextArea.getText());
        try (OutputStream output = Files.newOutputStream(getStateFilePath())) {
            properties.store(output, null);
        } catch (IOException | PDGException e) {
            showErrorDialog(e);
        }
    }

    private Path getStateFilePath() throws PDGException {
        String stateFileName = "pdg-save-file.properties";
        File jarLocation = new File(
                PropertiesDiffGeneratorController.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .getPath()
        ).getParentFile();

        return Paths.get(jarLocation.getAbsolutePath(), stateFileName);
    }
}
