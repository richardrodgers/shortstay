/**
 * Copyright 2024 Richard Rodgers
 * SPDX-Licence-Identifier: Apache-2.0
 */
package schengen.shortstay;

import java.nio.file.Paths;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import org.eclipse.store.storage.embedded.types.EmbeddedStorage;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;

public class Controller {

    @FXML public TextField nameField;
    @FXML public DatePicker startPicker;
    @FXML public DatePicker endPicker;
    @FXML public Button addButton;
    @FXML public Label statusLabel;
    @FXML public TableView<Stay> stayTable;
    @FXML public Button removeButton;
    @FXML public Button clearButton;

    @FXML public ResourceBundle resources;

    private Plan plan;
    private EmbeddedStorageManager storage;

    public void initialize() {        
        storage = EmbeddedStorage.start(Paths.get("stay-data"));
        plan = (Plan)storage.root();
        if (plan == null) { // nothing stored yet
            plan = new Plan();
            storage.setRoot(plan);
            storage.storeRoot();
        } else {
            // wrap stayList in observable
            ObservableList<Stay> stayList = FXCollections.observableArrayList(plan.stayList());
            stayTable.setItems(stayList);
        }

        TableColumn<Stay, String> nameCol = new TableColumn<>(resources.getString("tvcStay"));
        nameCol.setCellValueFactory(
            s -> new SimpleStringProperty(s.getValue().name())
        );
        nameCol.setMinWidth(240.0);
        stayTable.getColumns().add(nameCol);

        TableColumn<Stay, String> startCol = new TableColumn<>(resources.getString("tvcStart"));
        startCol.setCellValueFactory(
            s -> new SimpleStringProperty(s.getValue().startDate().toString())
        );
        startCol.setStyle("-fx-alignment: CENTER;");
        stayTable.getColumns().add(startCol);

        TableColumn<Stay, String> endCol = new TableColumn<>(resources.getString("tvcEnd"));
        endCol.setCellValueFactory(
            s -> new SimpleStringProperty(s.getValue().endDate().toString())
        );
        endCol.setStyle("-fx-alignment: CENTER;");
        stayTable.getColumns().add(endCol);

        TableColumn<Stay, Integer> countCol = new TableColumn<>(resources.getString("tvcDays"));
        countCol.setCellValueFactory(
            s -> new SimpleIntegerProperty(s.getValue().length()).asObject()
        );
        countCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        stayTable.getColumns().add(countCol);

        stayTable.getSelectionModel().selectedIndexProperty().addListener(
            (_, _, newVal) -> removeButton.setDisable(newVal.intValue() == -1)
        );

        addButton.setDisable(true);
        addButton.setOnAction(_ -> addStay());
        removeButton.setDisable(true);
        removeButton.setOnAction(_ -> removeStay());
        nameField.textProperty().addListener (_ -> checkInputs());
        startPicker.setOnAction(_ -> checkInputs());
        endPicker.setOnAction(_ -> checkInputs());
        clearButton.setDisable(true);
        clearButton.setOnAction(_ -> clearInputs());
    }

    private void checkInputs() {
        statusLabel.setText(null);
        var complete = ! nullOrEmpty(nameField.getText())
                       && startPicker.getValue() != null
                       && endPicker.getValue() != null;
        if (complete) {
            var created = createStay();
            if (created.stay != null) {
                var result = plan.canInsert(created.stay);
                if (result.success()) {
                    statusLabel.setText(created.msg());
                    addButton.setDisable(false);
                } else {
                    statusLabel.setText(resultMsg(result));
                    addButton.setDisable(true);
                }
            } else {
                statusLabel.setText(created.msg());
            }
        }
        clearButton.setDisable(! complete);
        removeButton.setDisable(true);
    }

    record Created(Stay stay, String msg) {}

    private Created createStay() {
        try {
            var stay = new Stay(nameField.getText(),
                                startPicker.getValue(),
                                endPicker.getValue());
            return new Created(stay, "Ok");
        } catch (IllegalArgumentException iaE) {
            return new Created(null, resources.getString(iaE.getMessage()));
        }
    }

    private static boolean nullOrEmpty(String s) {
        return (null == s || s.length() == 0);
    }

    public void dispose() {
        storage.shutdown();
    }

    public void addStay() {
        var created = createStay();
        if (created.stay() != null) {
            var result = plan.insertStay(created.stay());
            if (result.success()) {
                clearInputs();
                // persist new list
                storage.store(plan.stayList());
                statusLabel.setText(resources.getString("added"));
                stayTable.getItems().add(created.stay());
            } else {
                statusLabel.setText(resultMsg(result));
            }
        } else {
            statusLabel.setText(created.msg());
        }
    }

    public void removeStay() {
        var sel = stayTable.getSelectionModel().getSelectedIndex();
        if (sel > -1) {
            plan.removeStay(sel);
            // persist new list
            storage.store(plan.stayList());
            statusLabel.setText(resources.getString("removed"));
            stayTable.getItems().remove(sel);
        } else {
            statusLabel.setText(resources.getString("noStay"));
        }
        removeButton.setDisable(true);
    }
    
    private void clearInputs() {
        nameField.clear();
        startPicker.setValue(null);
        endPicker.setValue(null);
        statusLabel.setText("");
        addButton.setDisable(true);
    }

    private String resultMsg(Result result) {
        var i18msg = resources.getString(result.msg());
        var args = result.pArgs();
        for (int i = 0; i < args.size(); i++) {
            i18msg = i18msg.replace("%" + Integer.toString(i), args.get(i));
        }
        return i18msg;
    }
}
