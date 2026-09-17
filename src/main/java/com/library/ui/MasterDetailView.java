package com.library.ui;

import com.library.model.BaseEntity;
import com.library.repository.GenericRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Reusable Generic Master-Detail UI Layout for JavaFX.
 *
 * @param <T>  Entity type extending BaseEntity<ID>
 * @param <ID> Key type identifier
 */
public abstract class MasterDetailView<T extends BaseEntity<ID>, ID> extends BorderPane {

    protected final GenericRepository<T, ID> repository;
    protected final ObservableList<T> masterData = FXCollections.observableArrayList();

    // UI Controls
    protected TableView<T> masterTable;
    protected TextField searchField;
    protected ComboBox<String> filterCombo;
    protected Label itemCountLabel;

    // Detail Panel Controls
    protected VBox detailDrawer;
    protected Label detailTitleLabel;
    protected VBox detailContentContainer;
    protected HBox detailActionBar;
    protected Button btnEdit;
    protected Button btnSave;
    protected Button btnCancel;
    protected Button btnDelete;

    protected T currentSelection;
    protected boolean isEditMode = false;

    public MasterDetailView(GenericRepository<T, ID> repository) {
        this.repository = repository;
        setPadding(new Insets(16));
        getStyleClass().add("master-detail-container");

        initUI();
        refreshData();
    }

    private void initUI() {
        // Build Top Toolbar (Search, Filter, Actions)
        HBox topToolbar = buildTopToolbar();
        setTop(topToolbar);

        // Build Master Table
        masterTable = buildMasterTable();
        masterTable.setItems(masterData);
        masterTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            currentSelection = newVal;
            isEditMode = false;
            updateDetailDrawer();
        });

        // Wrap Table in a VBox with Status Footer
        VBox masterContainer = new VBox(12);
        VBox.setVgrow(masterTable, Priority.ALWAYS);
        itemCountLabel = new Label("Total Items: 0");
        itemCountLabel.getStyleClass().add("status-footer-label");
        
        HBox footer = new HBox(itemCountLabel);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(4, 8, 4, 8));

        masterContainer.getChildren().addAll(masterTable, footer);

        // Build Detail Drawer Panel
        detailDrawer = buildDetailDrawer();

        // SplitPane or HBox layout
        SplitPane mainSplit = new SplitPane();
        mainSplit.getItems().addAll(masterContainer, detailDrawer);
        mainSplit.setDividerPositions(0.62);
        SplitPane.setResizableWithParent(detailDrawer, false);

        setCenter(mainSplit);
    }

    private HBox buildTopToolbar() {
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(0, 0, 16, 0));

        searchField = new TextField();
        searchField.setPromptText("🔍 Search by keyword...");
        searchField.setPrefWidth(260);
        searchField.getStyleClass().add("search-input");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        filterCombo = new ComboBox<>();
        filterCombo.getItems().add("All");
        setupFilterOptions(filterCombo);
        filterCombo.setValue("All");
        filterCombo.setOnAction(e -> applyFilter());

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.getStyleClass().add("button-secondary");
        btnRefresh.setOnAction(e -> refreshData());

        Button btnAdd = new Button("➕ Add New");
        btnAdd.getStyleClass().add("button-primary");
        btnAdd.setOnAction(e -> handleAddNew());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(searchField, filterCombo, btnRefresh, spacer, btnAdd);
        return toolbar;
    }

    private VBox buildDetailDrawer() {
        VBox drawer = new VBox(16);
        drawer.setPadding(new Insets(16));
        drawer.getStyleClass().add("detail-drawer");
        drawer.setMinWidth(380);
        drawer.setPrefWidth(420);

        // Drawer Header
        detailTitleLabel = new Label("Select an item");
        detailTitleLabel.getStyleClass().add("detail-header-title");

        HBox headerBox = new HBox(detailTitleLabel);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getStyleClass().add("detail-header");

        // Drawer Content Area
        detailContentContainer = new VBox(12);
        VBox.setVgrow(detailContentContainer, Priority.ALWAYS);

        // Action Bar
        detailActionBar = new HBox(10);
        detailActionBar.setAlignment(Pos.CENTER_RIGHT);

        btnEdit = new Button("✏️ Edit");
        btnEdit.getStyleClass().add("button-secondary");
        btnEdit.setOnAction(e -> {
            if (currentSelection != null) {
                isEditMode = true;
                updateDetailDrawer();
            }
        });

        btnSave = new Button("💾 Save");
        btnSave.getStyleClass().add("button-success");
        btnSave.setOnAction(e -> handleSave());

        btnCancel = new Button("❌ Cancel");
        btnCancel.getStyleClass().add("button-secondary");
        btnCancel.setOnAction(e -> {
            isEditMode = false;
            updateDetailDrawer();
        });

        btnDelete = new Button("🗑️ Delete");
        btnDelete.getStyleClass().add("button-danger");
        btnDelete.setOnAction(e -> handleDelete());

        detailActionBar.getChildren().addAll(btnDelete, btnEdit, btnCancel, btnSave);

        drawer.getChildren().addAll(headerBox, new Separator(), detailContentContainer, new Separator(), detailActionBar);
        return drawer;
    }

    public void refreshData() {
        masterData.setAll(repository.findAll());
        applyFilter();
        updateCountLabel();
    }

    protected void applyFilter() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedFilter = filterCombo.getValue();

        Predicate<T> predicate = entity -> {
            boolean matchesKeyword = keyword.isEmpty() || matchesKeyword(entity, keyword);
            boolean matchesCategory = selectedFilter == null || selectedFilter.equalsIgnoreCase("All") || matchesCategory(entity, selectedFilter);
            return matchesKeyword && matchesCategory;
        };

        List<T> filtered = repository.search(predicate);
        masterTable.setItems(FXCollections.observableArrayList(filtered));
        updateCountLabel();
    }

    private void updateCountLabel() {
        int count = masterTable.getItems().size();
        itemCountLabel.setText("Showing " + count + " of " + repository.count() + " record(s)");
    }

    private void updateDetailDrawer() {
        detailContentContainer.getChildren().clear();

        if (currentSelection == null && !isEditMode) {
            detailTitleLabel.setText("Select an item to view details");
            detailContentContainer.getChildren().add(new Label("No record selected from master list."));
            btnEdit.setVisible(false);
            btnSave.setVisible(false);
            btnCancel.setVisible(false);
            btnDelete.setVisible(false);
            return;
        }

        if (isEditMode) {
            detailTitleLabel.setText(currentSelection == null || currentSelection.getId() == null ? "New Entry" : "Edit Entry");
            Node formNode = buildFormNode(currentSelection);
            detailContentContainer.getChildren().add(formNode);

            btnEdit.setVisible(false);
            btnSave.setVisible(true);
            btnCancel.setVisible(true);
            btnDelete.setVisible(false);
        } else {
            detailTitleLabel.setText(getDetailTitle(currentSelection));
            Node viewNode = buildDetailViewNode(currentSelection);
            detailContentContainer.getChildren().add(viewNode);

            // Add custom domain actions if available
            Node customActions = buildCustomActionsNode(currentSelection);
            if (customActions != null) {
                detailContentContainer.getChildren().addAll(new Separator(), customActions);
            }

            btnEdit.setVisible(true);
            btnSave.setVisible(false);
            btnCancel.setVisible(false);
            btnDelete.setVisible(true);
        }
    }

    protected void handleAddNew() {
        currentSelection = createNewInstance();
        isEditMode = true;
        masterTable.getSelectionModel().clearSelection();
        updateDetailDrawer();
    }

    protected void handleSave() {
        try {
            T entityToSave = readFormData(currentSelection);
            if (entityToSave == null) return; // Validation error handled in subclass

            repository.save(entityToSave);
            isEditMode = false;
            refreshData();
            masterTable.getSelectionModel().select(entityToSave);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Record saved successfully!");
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", ex.getMessage());
        }
    }

    protected void handleDelete() {
        if (currentSelection == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this record?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Deletion");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            repository.deleteById(currentSelection.getId());
            currentSelection = null;
            isEditMode = false;
            refreshData();
            updateDetailDrawer();
        }
    }

    protected void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Abstract methods to be implemented by domain views
    protected abstract TableView<T> buildMasterTable();
    protected abstract void setupFilterOptions(ComboBox<String> combo);
    protected abstract boolean matchesKeyword(T entity, String keyword);
    protected abstract boolean matchesCategory(T entity, String category);
    protected abstract String getDetailTitle(T entity);
    protected abstract Node buildDetailViewNode(T entity);
    protected abstract Node buildFormNode(T entity);
    protected abstract T createNewInstance();
    protected abstract T readFormData(T existingEntity) throws Exception;

    protected Node buildCustomActionsNode(T entity) {
        return null; // Optional override for extra domain buttons
    }
}
