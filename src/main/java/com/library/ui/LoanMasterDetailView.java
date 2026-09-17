package com.library.ui;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.LoanStatus;
import com.library.model.Member;
import com.library.service.LibraryService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;

public class LoanMasterDetailView extends MasterDetailView<Loan, String> {

    private final LibraryService libraryService;

    // Form inputs
    private TextField txtId;
    private ComboBox<Book> comboBook;
    private ComboBox<Member> comboMember;
    private DatePicker dpIssue;
    private DatePicker dpDue;
    private ComboBox<LoanStatus> comboStatus;

    public LoanMasterDetailView(LibraryService libraryService) {
        super(libraryService.getLoanRepository());
        this.libraryService = libraryService;
        this.libraryService.updateOverdueStatuses();
    }

    @Override
    protected TableView<Loan> buildMasterTable() {
        TableView<Loan> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Loan, String> colId = new TableColumn<>("Loan ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(90);

        TableColumn<Loan, String> colBook = new TableColumn<>("Book Title");
        colBook.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colBook.setPrefWidth(210);

        TableColumn<Loan, String> colMember = new TableColumn<>("Member");
        colMember.setCellValueFactory(new PropertyValueFactory<>("memberName"));
        colMember.setPrefWidth(160);

        TableColumn<Loan, LocalDate> colIssue = new TableColumn<>("Issue Date");
        colIssue.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        colIssue.setPrefWidth(100);

        TableColumn<Loan, LocalDate> colDue = new TableColumn<>("Due Date");
        colDue.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colDue.setPrefWidth(100);

        TableColumn<Loan, String> colFine = new TableColumn<>("Fine");
        colFine.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getFineAmount() > 0 ? String.format("$%.2f", cell.getValue().getFineAmount()) : "$0.00"
        ));
        colFine.setPrefWidth(80);

        TableColumn<Loan, LoanStatus> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LoanStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item.getDisplayName());
                    badge.getStyleClass().add("badge");
                    switch (item) {
                        case ACTIVE -> badge.getStyleClass().add("badge-info");
                        case OVERDUE -> badge.getStyleClass().add("badge-danger");
                        case RETURNED -> badge.getStyleClass().add("badge-success");
                    }
                    setGraphic(badge);
                }
            }
        });
        colStatus.setPrefWidth(110);

        table.getColumns().addAll(colId, colBook, colMember, colIssue, colDue, colFine, colStatus);
        return table;
    }

    @Override
    protected void setupFilterOptions(ComboBox<String> combo) {
        combo.getItems().addAll("ACTIVE", "OVERDUE", "RETURNED");
    }

    @Override
    protected boolean matchesKeyword(Loan loan, String keyword) {
        return loan.getId().toLowerCase().contains(keyword) ||
               loan.getBookTitle().toLowerCase().contains(keyword) ||
               loan.getMemberName().toLowerCase().contains(keyword) ||
               loan.getBookId().toLowerCase().contains(keyword) ||
               loan.getMemberId().toLowerCase().contains(keyword);
    }

    @Override
    protected boolean matchesCategory(Loan loan, String category) {
        return loan.getStatus().name().equalsIgnoreCase(category);
    }

    @Override
    protected String getDetailTitle(Loan loan) {
        return loan == null ? "" : "Loan #" + loan.getId();
    }

    @Override
    protected Node buildDetailViewNode(Loan loan) {
        VBox box = new VBox(14);
        box.setPadding(new Insets(8, 0, 8, 0));

        Label badge = new Label(loan.getStatus().getDisplayName());
        badge.getStyleClass().add("badge");
        switch (loan.getStatus()) {
            case ACTIVE -> badge.getStyleClass().add("badge-info");
            case OVERDUE -> badge.getStyleClass().add("badge-danger");
            case RETURNED -> badge.getStyleClass().add("badge-success");
        }

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        addDetailRow(grid, 0, "Loan ID:", loan.getId());
        addDetailRow(grid, 1, "Book Title:", loan.getBookTitle() + " (" + loan.getBookId() + ")");
        addDetailRow(grid, 2, "Member:", loan.getMemberName() + " (" + loan.getMemberId() + ")");
        addDetailRow(grid, 3, "Issue Date:", String.valueOf(loan.getIssueDate()));
        addDetailRow(grid, 4, "Due Date:", String.valueOf(loan.getDueDate()));
        addDetailRow(grid, 5, "Return Date:", loan.getReturnDate() == null ? "Not returned yet" : String.valueOf(loan.getReturnDate()));
        addDetailRow(grid, 6, "Accrued Fine:", String.format("$%.2f", loan.getFineAmount()));

        box.getChildren().addAll(badge, grid);
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
    protected Node buildFormNode(Loan loan) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);

        boolean isNew = loan == null || loan.getId() == null;

        txtId = new TextField(isNew ? "LN-" + (1000 + repository.count() + 1) : loan.getId());
        txtId.setDisable(!isNew);

        comboBook = new ComboBox<>();
        List<Book> books = libraryService.getBookRepository().findAll();
        comboBook.getItems().addAll(books);
        if (!isNew) {
            books.stream().filter(b -> b.getId().equals(loan.getBookId())).findFirst().ifPresent(comboBook::setValue);
        } else if (!books.isEmpty()) {
            comboBook.setValue(books.get(0));
        }

        comboMember = new ComboBox<>();
        List<Member> members = libraryService.getMemberRepository().findAll();
        comboMember.getItems().addAll(members);
        if (!isNew) {
            members.stream().filter(m -> m.getId().equals(loan.getMemberId())).findFirst().ifPresent(comboMember::setValue);
        } else if (!members.isEmpty()) {
            comboMember.setValue(members.get(0));
        }

        dpIssue = new DatePicker(isNew ? LocalDate.now() : loan.getIssueDate());
        dpDue = new DatePicker(isNew ? LocalDate.now().plusDays(14) : loan.getDueDate());

        comboStatus = new ComboBox<>();
        comboStatus.getItems().addAll(LoanStatus.values());
        comboStatus.setValue(isNew ? LoanStatus.ACTIVE : loan.getStatus());

        grid.add(new Label("Loan ID:"), 0, 0); grid.add(txtId, 1, 0);
        grid.add(new Label("Book:"), 0, 1); grid.add(comboBook, 1, 1);
        grid.add(new Label("Member:"), 0, 2); grid.add(comboMember, 1, 2);
        grid.add(new Label("Issue Date:"), 0, 3); grid.add(dpIssue, 1, 3);
        grid.add(new Label("Due Date:"), 0, 4); grid.add(dpDue, 1, 4);
        grid.add(new Label("Status:"), 0, 5); grid.add(comboStatus, 1, 5);

        return grid;
    }

    @Override
    protected Loan createNewInstance() {
        return new Loan();
    }

    @Override
    protected Loan readFormData(Loan existing) throws Exception {
        String id = txtId.getText().trim();
        Book book = comboBook.getValue();
        Member member = comboMember.getValue();
        LocalDate issue = dpIssue.getValue();
        LocalDate due = dpDue.getValue();
        LoanStatus status = comboStatus.getValue();

        if (id.isEmpty() || book == null || member == null || issue == null || due == null) {
            throw new IllegalArgumentException("All loan fields must be completed.");
        }

        LocalDate returnDate = existing != null ? existing.getReturnDate() : null;
        double fine = existing != null ? existing.getFineAmount() : 0.0;

        return new Loan(id, book.getId(), book.getTitle(), member.getId(), member.getName(), issue, due, returnDate, status, fine);
    }

    @Override
    protected Node buildCustomActionsNode(Loan loan) {
        if (loan == null || loan.getStatus() == LoanStatus.RETURNED) return null;

        Button btnReturn = new Button("📥 Process Return & Calculate Fine");
        btnReturn.getStyleClass().add("button-success");
        btnReturn.setMaxWidth(Double.MAX_VALUE);

        btnReturn.setOnAction(e -> {
            try {
                Loan updated = libraryService.returnBook(loan.getId());
                refreshData();
                showAlert(Alert.AlertType.INFORMATION, "Book Returned",
                        "Book '" + updated.getBookTitle() + "' marked as RETURNED.\nAccrued fine: $" + String.format("%.2f", updated.getFineAmount()));
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Return Error", ex.getMessage());
            }
        });
        return btnReturn;
    }
}
