package com.library.ui;

import com.library.model.Loan;
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

public class MemberMasterDetailView extends MasterDetailView<Member, String> {

    private final LibraryService libraryService;

    // Form inputs
    private TextField txtId;
    private TextField txtName;
    private TextField txtEmail;
    private TextField txtPhone;
    private ComboBox<String> comboType;
    private ComboBox<String> comboStatus;

    public MemberMasterDetailView(LibraryService libraryService) {
        super(libraryService.getMemberRepository());
        this.libraryService = libraryService;
    }

    @Override
    protected TableView<Member> buildMasterTable() {
        TableView<Member> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Member, String> colId = new TableColumn<>("Member ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(90);

        TableColumn<Member, String> colName = new TableColumn<>("Full Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setPrefWidth(180);

        TableColumn<Member, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(200);

        TableColumn<Member, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("membershipType"));
        colType.setPrefWidth(110);

        TableColumn<Member, Integer> colLoans = new TableColumn<>("Active Loans");
        colLoans.setCellValueFactory(new PropertyValueFactory<>("activeLoansCount"));
        colLoans.setPrefWidth(100);

        TableColumn<Member, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
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
                    if ("ACTIVE".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-success");
                    } else {
                        badge.getStyleClass().add("badge-danger");
                    }
                    setGraphic(badge);
                }
            }
        });
        colStatus.setPrefWidth(110);

        table.getColumns().addAll(colId, colName, colEmail, colType, colLoans, colStatus);
        return table;
    }

    @Override
    protected void setupFilterOptions(ComboBox<String> combo) {
        combo.getItems().addAll("Student", "Faculty", "Regular", "ACTIVE", "SUSPENDED");
    }

    @Override
    protected boolean matchesKeyword(Member member, String keyword) {
        return member.getName().toLowerCase().contains(keyword) ||
               member.getEmail().toLowerCase().contains(keyword) ||
               member.getId().toLowerCase().contains(keyword) ||
               member.getPhone().toLowerCase().contains(keyword);
    }

    @Override
    protected boolean matchesCategory(Member member, String category) {
        if ("ACTIVE".equalsIgnoreCase(category) || "SUSPENDED".equalsIgnoreCase(category)) {
            return member.getStatus().equalsIgnoreCase(category);
        }
        return member.getMembershipType().equalsIgnoreCase(category);
    }

    @Override
    protected String getDetailTitle(Member member) {
        return member == null ? "" : member.getName();
    }

    @Override
    protected Node buildDetailViewNode(Member member) {
        VBox box = new VBox(14);
        box.setPadding(new Insets(8, 0, 8, 0));

        Label badge = new Label(member.getStatus());
        badge.getStyleClass().addAll("badge", "ACTIVE".equalsIgnoreCase(member.getStatus()) ? "badge-success" : "badge-danger");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        addDetailRow(grid, 0, "Member ID:", member.getId());
        addDetailRow(grid, 1, "Full Name:", member.getName());
        addDetailRow(grid, 2, "Email:", member.getEmail());
        addDetailRow(grid, 3, "Phone:", member.getPhone());
        addDetailRow(grid, 4, "Type:", member.getMembershipType());
        addDetailRow(grid, 5, "Joined Date:", String.valueOf(member.getJoinDate()));
        addDetailRow(grid, 6, "Active Loans:", String.valueOf(member.getActiveLoansCount()));

        // Active Borrowed Books List
        VBox loansBox = new VBox(6);
        Label loansLabel = new Label("Current Borrowed Books:");
        loansLabel.getStyleClass().add("detail-field-label");
        loansBox.getChildren().add(loansLabel);

        List<Loan> memberLoans = libraryService.getLoanRepository().search(l -> l.getMemberId().equalsIgnoreCase(member.getId()));
        if (memberLoans.isEmpty()) {
            Label noLoans = new Label("No active or historical loans for this member.");
            noLoans.getStyleClass().add("detail-field-value");
            loansBox.getChildren().add(noLoans);
        } else {
            ListView<String> list = new ListView<>();
            list.setPrefHeight(120);
            for (Loan l : memberLoans) {
                list.getItems().add(l.getBookTitle() + " (" + l.getStatus() + ", Due: " + l.getDueDate() + ")");
            }
            loansBox.getChildren().add(list);
        }

        box.getChildren().addAll(badge, grid, new Separator(), loansBox);
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
    protected Node buildFormNode(Member member) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);

        boolean isNew = member == null || member.getId() == null;

        txtId = new TextField(isNew ? "MB-" + (1000 + repository.count() + 1) : member.getId());
        txtId.setDisable(!isNew);

        txtName = new TextField(isNew ? "" : member.getName());
        txtEmail = new TextField(isNew ? "" : member.getEmail());
        txtPhone = new TextField(isNew ? "" : member.getPhone());

        comboType = new ComboBox<>();
        comboType.getItems().addAll("Student", "Faculty", "Regular");
        comboType.setValue(isNew ? "Student" : member.getMembershipType());

        comboStatus = new ComboBox<>();
        comboStatus.getItems().addAll("ACTIVE", "SUSPENDED");
        comboStatus.setValue(isNew ? "ACTIVE" : member.getStatus());

        grid.add(new Label("Member ID:"), 0, 0); grid.add(txtId, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1); grid.add(txtName, 1, 1);
        grid.add(new Label("Email:"), 0, 2); grid.add(txtEmail, 1, 2);
        grid.add(new Label("Phone:"), 0, 3); grid.add(txtPhone, 1, 3);
        grid.add(new Label("Membership Type:"), 0, 4); grid.add(comboType, 1, 4);
        grid.add(new Label("Status:"), 0, 5); grid.add(comboStatus, 1, 5);

        return grid;
    }

    @Override
    protected Member createNewInstance() {
        return new Member();
    }

    @Override
    protected Member readFormData(Member existing) throws Exception {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String type = comboType.getValue();
        String status = comboStatus.getValue();

        if (id.isEmpty() || name.isEmpty() || email.isEmpty()) {
            throw new IllegalArgumentException("Member ID, Name, and Email are required fields.");
        }

        LocalDate joinDate = existing != null && existing.getJoinDate() != null ? existing.getJoinDate() : LocalDate.now();
        int activeLoans = existing != null ? existing.getActiveLoansCount() : 0;

        return new Member(id, name, email, phone, type, joinDate, activeLoans, status);
    }
}
