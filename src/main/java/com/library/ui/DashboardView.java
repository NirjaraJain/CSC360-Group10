package com.library.ui;

import com.library.model.Loan;
import com.library.model.LoanStatus;
import com.library.service.LibraryService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;

public class DashboardView extends ScrollPane {

    private final LibraryService libraryService;
    private final Runnable navToBooks;
    private final Runnable navToMembers;
    private final Runnable navToLoans;

    public DashboardView(LibraryService libraryService, Runnable navToBooks, Runnable navToMembers, Runnable navToLoans) {
        this.libraryService = libraryService;
        this.navToBooks = navToBooks;
        this.navToMembers = navToMembers;
        this.navToLoans = navToLoans;

        setFitToWidth(true);
        getStyleClass().add("dashboard-scroll");

        VBox content = new VBox(24);
        content.setPadding(new Insets(24));

        // Header Banner
        VBox header = new VBox(4);
        Label title = new Label("📊 Library Executive Overview & Metrics");
        title.getStyleClass().add("dashboard-header-title");
        Label subtitle = new Label("Real-time summary of catalog inventory, patron memberships, and circulation status.");
        subtitle.getStyleClass().add("dashboard-header-subtitle");
        header.getChildren().addAll(title, subtitle);

        // Metric Cards Grid
        GridPane metricGrid = buildMetricCards();

        // Recent Activity Table Box
        VBox recentBox = buildRecentActivityBox();

        content.getChildren().addAll(header, metricGrid, recentBox);
        setContent(content);
    }

    private GridPane buildMetricCards() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        long totalBooks = libraryService.getBookRepository().count();
        long totalMembers = libraryService.getMemberRepository().count();
        List<Loan> activeLoans = libraryService.getLoanRepository().search(l -> l.getStatus() == LoanStatus.ACTIVE);
        List<Loan> overdueLoans = libraryService.getLoanRepository().search(l -> l.getStatus() == LoanStatus.OVERDUE);
        double totalFines = libraryService.getLoanRepository().findAll().stream().mapToDouble(Loan::getFineAmount).sum();

        VBox card1 = createMetricCard("📚 Total Catalog Books", String.valueOf(totalBooks), "Items registered", "metric-blue", navToBooks);
        VBox card2 = createMetricCard("👥 Registered Patrons", String.valueOf(totalMembers), "Active members", "metric-green", navToMembers);
        VBox card3 = createMetricCard("📖 Active Borrowings", String.valueOf(activeLoans.size()), "Currently checked out", "metric-amber", navToLoans);
        VBox card4 = createMetricCard("⚠️ Overdue Items", String.valueOf(overdueLoans.size()), "Requires follow up", "metric-red", navToLoans);
        VBox card5 = createMetricCard("💰 Total Fines Accrued", String.format("$%.2f", totalFines), "Outstanding & settled", "metric-purple", navToLoans);

        grid.add(card1, 0, 0);
        grid.add(card2, 1, 0);
        grid.add(card3, 2, 0);
        grid.add(card4, 3, 0);
        grid.add(card5, 4, 0);

        // Equal width columns
        for (int i = 0; i < 5; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(20);
            grid.getColumnConstraints().add(cc);
        }

        return grid;
    }

    private VBox createMetricCard(String labelStr, String valueStr, String descStr, String colorClass, Runnable onClick) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("metric-card", colorClass);
        card.setPadding(new Insets(16));

        Label lbl = new Label(labelStr);
        lbl.getStyleClass().add("metric-title");

        Label val = new Label(valueStr);
        val.getStyleClass().add("metric-value");

        Label desc = new Label(descStr);
        desc.getStyleClass().add("metric-desc");

        card.getChildren().addAll(lbl, val, desc);
        card.setOnMouseClicked(e -> {
            if (onClick != null) onClick.run();
        });
        return card;
    }

    private VBox buildRecentActivityBox() {
        VBox box = new VBox(12);
        box.getStyleClass().add("dashboard-card");
        box.setPadding(new Insets(16));

        Label boxTitle = new Label("📋 Recent Circulation Log");
        boxTitle.getStyleClass().add("card-title");

        TableView<Loan> table = new TableView<>();
        table.setPrefHeight(220);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Loan, String> colId = new TableColumn<>("Loan ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Loan, String> colBook = new TableColumn<>("Book");
        colBook.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));

        TableColumn<Loan, String> colMember = new TableColumn<>("Member");
        colMember.setCellValueFactory(new PropertyValueFactory<>("memberName"));

        TableColumn<Loan, LocalDate> colIssue = new TableColumn<>("Issue Date");
        colIssue.setCellValueFactory(new PropertyValueFactory<>("issueDate"));

        TableColumn<Loan, LocalDate> colDue = new TableColumn<>("Due Date");
        colDue.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        TableColumn<Loan, LoanStatus> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(colId, colBook, colMember, colIssue, colDue, colStatus);
        table.getItems().setAll(libraryService.getLoanRepository().findAll());

        box.getChildren().addAll(boxTitle, table);
        return box;
    }
}
