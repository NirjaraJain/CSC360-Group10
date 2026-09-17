package com.library.ui;

import com.library.model.Book;
import com.library.model.Member;
import com.library.service.LibraryService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

public class BookMasterDetailView extends MasterDetailView<Book, String> {

    private final LibraryService libraryService;

    // Form inputs
    private TextField txtId;
    private TextField txtTitle;
    private TextField txtIsbn;
    private TextField txtAuthor;
    private TextField txtCategory;
    private Spinner<Integer> spinYear;
    private Spinner<Integer> spinTotal;
    private Spinner<Integer> spinAvailable;
    private Spinner<Double> spinRating;
    private TextArea txtDescription;

    public BookMasterDetailView(LibraryService libraryService) {
        super(libraryService.getBookRepository());
        this.libraryService = libraryService;
    }

    @Override
    protected TableView<Book> buildMasterTable() {
        TableView<Book> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Book, String> colId = new TableColumn<>("Book ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(90);

        TableColumn<Book, String> colTitle = new TableColumn<>("Title");
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTitle.setPrefWidth(220);

        TableColumn<Book, String> colAuthor = new TableColumn<>("Author");
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colAuthor.setPrefWidth(160);

        TableColumn<Book, String> colCategory = new TableColumn<>("Category");
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCategory.setPrefWidth(140);

        TableColumn<Book, Integer> colYear = new TableColumn<>("Year");
        colYear.setCellValueFactory(new PropertyValueFactory<>("publicationYear"));
        colYear.setPrefWidth(70);

        TableColumn<Book, String> colStock = new TableColumn<>("Stock (Avail/Total)");
        colStock.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getAvailableCopies() + " / " + cell.getValue().getTotalCopies()
        ));
        colStock.setPrefWidth(120);

        TableColumn<Book, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().isAvailable() ? "AVAILABLE" : "OUT OF STOCK"
        ));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    if ("AVAILABLE".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-success");
                    } else {
                        badge.getStyleClass().add("badge-danger");
                    }
                    setGraphic(badge);
                }
            }
        });
        colStatus.setPrefWidth(120);

        table.getColumns().addAll(colId, colTitle, colAuthor, colCategory, colYear, colStock, colStatus);
        return table;
    }

    @Override
    protected void setupFilterOptions(ComboBox<String> combo) {
        combo.getItems().addAll(
                "Software Engineering",
                "Computer Science",
                "Software Architecture",
                "UI Engineering",
                "Available Only"
        );
    }

    @Override
    protected boolean matchesKeyword(Book book, String keyword) {
        return book.getTitle().toLowerCase().contains(keyword) ||
               book.getAuthor().toLowerCase().contains(keyword) ||
               book.getIsbn().toLowerCase().contains(keyword) ||
               book.getId().toLowerCase().contains(keyword) ||
               book.getCategory().toLowerCase().contains(keyword);
    }

    @Override
    protected boolean matchesCategory(Book book, String category) {
        if ("Available Only".equalsIgnoreCase(category)) {
            return book.isAvailable();
        }
        return book.getCategory().equalsIgnoreCase(category);
    }

    @Override
    protected String getDetailTitle(Book book) {
        return book == null ? "" : book.getTitle();
    }

    @Override
    protected Node buildDetailViewNode(Book book) {
        VBox box = new VBox(14);
        box.setPadding(new Insets(8, 0, 8, 0));

        // Status Badge Header
        Label badge = new Label(book.isAvailable() ? "AVAILABLE (" + book.getAvailableCopies() + " left)" : "OUT OF STOCK");
        badge.getStyleClass().addAll("badge", book.isAvailable() ? "badge-success" : "badge-danger");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        addDetailRow(grid, 0, "Book ID:", book.getId());
        addDetailRow(grid, 1, "ISBN:", book.getIsbn());
        addDetailRow(grid, 2, "Author:", book.getAuthor());
        addDetailRow(grid, 3, "Category:", book.getCategory());
        addDetailRow(grid, 4, "Published:", String.valueOf(book.getPublicationYear()));
        addDetailRow(grid, 5, "Total Copies:", String.valueOf(book.getTotalCopies()));
        addDetailRow(grid, 6, "Available:", String.valueOf(book.getAvailableCopies()));
        addDetailRow(grid, 7, "Rating:", "★ " + book.getRating() + " / 5.0");

        Label descTitle = new Label("Description:");
        descTitle.getStyleClass().add("detail-field-label");

        Label descText = new Label(book.getDescription() == null || book.getDescription().isBlank() ? "No description provided." : book.getDescription());
        descText.setWrapText(true);
        descText.getStyleClass().add("detail-field-value");

        VBox descBox = new VBox(4, descTitle, descText);

        box.getChildren().addAll(badge, grid, new Separator(), descBox);
        return box;
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("detail-field-label");
        Label val = new Label(value);
        val.getStyleClass().add("detail-field-value");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    @Override
    protected Node buildFormNode(Book book) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);

        boolean isNew = book == null || book.getId() == null;

        txtId = new TextField(isNew ? "BK-" + (100 + repository.count() + 1) : book.getId());
        txtId.setDisable(!isNew); // Primary key locked if editing

        txtTitle = new TextField(isNew ? "" : book.getTitle());
        txtIsbn = new TextField(isNew ? "" : book.getIsbn());
        txtAuthor = new TextField(isNew ? "" : book.getAuthor());
        txtCategory = new TextField(isNew ? "" : book.getCategory());

        spinYear = new Spinner<>(1900, 2030, isNew ? 2024 : book.getPublicationYear());
        spinYear.setEditable(true);

        spinTotal = new Spinner<>(1, 500, isNew ? 5 : book.getTotalCopies());
        spinTotal.setEditable(true);

        spinAvailable = new Spinner<>(0, 500, isNew ? 5 : book.getAvailableCopies());
        spinAvailable.setEditable(true);

        spinRating = new Spinner<>(1.0, 5.0, isNew ? 4.5 : book.getRating(), 0.1);
        spinRating.setEditable(true);

        txtDescription = new TextArea(isNew ? "" : book.getDescription());
        txtDescription.setPrefRowCount(3);
        txtDescription.setWrapText(true);

        grid.add(new Label("Book ID:"), 0, 0); grid.add(txtId, 1, 0);
        grid.add(new Label("Title:"), 0, 1); grid.add(txtTitle, 1, 1);
        grid.add(new Label("ISBN:"), 0, 2); grid.add(txtIsbn, 1, 2);
        grid.add(new Label("Author:"), 0, 3); grid.add(txtAuthor, 1, 3);
        grid.add(new Label("Category:"), 0, 4); grid.add(txtCategory, 1, 4);
        grid.add(new Label("Year:"), 0, 5); grid.add(spinYear, 1, 5);
        grid.add(new Label("Total Copies:"), 0, 6); grid.add(spinTotal, 1, 6);
        grid.add(new Label("Available:"), 0, 7); grid.add(spinAvailable, 1, 7);
        grid.add(new Label("Rating:"), 0, 8); grid.add(spinRating, 1, 8);
        grid.add(new Label("Description:"), 0, 9); grid.add(txtDescription, 1, 9);

        return grid;
    }

    @Override
    protected Book createNewInstance() {
        return new Book();
    }

    @Override
    protected Book readFormData(Book existing) throws Exception {
        String id = txtId.getText().trim();
        String title = txtTitle.getText().trim();
        String isbn = txtIsbn.getText().trim();
        String author = txtAuthor.getText().trim();
        String category = txtCategory.getText().trim();

        if (id.isEmpty() || title.isEmpty() || author.isEmpty()) {
            throw new IllegalArgumentException("Book ID, Title, and Author are required fields.");
        }

        int year = spinYear.getValue();
        int total = spinTotal.getValue();
        int avail = spinAvailable.getValue();
        double rating = spinRating.getValue();
        String desc = txtDescription.getText().trim();

        if (avail > total) {
            throw new IllegalArgumentException("Available copies cannot exceed total copies.");
        }

        return new Book(id, title, isbn, author, category, year, total, avail, rating, desc);
    }

    @Override
    protected Node buildCustomActionsNode(Book book) {
        if (book == null || !book.isAvailable()) return null;

        Button btnCheckout = new Button("📖 Borrow / Checkout Book");
        btnCheckout.getStyleClass().add("button-primary");
        btnCheckout.setMaxWidth(Double.MAX_VALUE);

        btnCheckout.setOnAction(e -> openCheckoutDialog(book));
        return btnCheckout;
    }

    private void openCheckoutDialog(Book book) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Checkout Book");
        dialog.setHeaderText("Checkout '" + book.getTitle() + "' to a Member");

        ButtonType checkoutButtonType = new ButtonType("Checkout", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(checkoutButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Member> memberCombo = new ComboBox<>();
        List<Member> activeMembers = libraryService.getMemberRepository().search(m -> "ACTIVE".equalsIgnoreCase(m.getStatus()));
        memberCombo.getItems().addAll(activeMembers);
        if (!activeMembers.isEmpty()) memberCombo.setValue(activeMembers.get(0));

        Spinner<Integer> daysSpin = new Spinner<>(1, 60, 14);

        grid.add(new Label("Select Member:"), 0, 0);
        grid.add(memberCombo, 1, 0);
        grid.add(new Label("Loan Period (Days):"), 0, 1);
        grid.add(daysSpin, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == checkoutButtonType) {
                Member selectedMember = memberCombo.getValue();
                if (selectedMember == null) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Please select a member.");
                    return false;
                }
                try {
                    libraryService.checkoutBook(book.getId(), selectedMember.getId(), daysSpin.getValue());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Book checked out successfully to " + selectedMember.getName());
                    refreshData();
                    return true;
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Checkout Error", ex.getMessage());
                    return false;
                }
            }
            return false;
        });

        dialog.showAndWait();
    }
}
