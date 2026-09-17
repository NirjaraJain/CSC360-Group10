package com.library;

import com.library.service.LibraryService;
import com.library.ui.BookMasterDetailView;
import com.library.ui.DashboardView;
import com.library.ui.LoanMasterDetailView;
import com.library.ui.MemberMasterDetailView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainApp extends Application {

    private LibraryService libraryService;
    private BorderPane mainLayout;

    // View Cache
    private DashboardView dashboardView;
    private BookMasterDetailView bookView;
    private MemberMasterDetailView memberView;
    private LoanMasterDetailView loanView;

    // Nav Buttons
    private Button btnDash;
    private Button btnBooks;
    private Button btnMembers;
    private Button btnLoans;

    @Override
    public void start(Stage primaryStage) {
        libraryService = new LibraryService();

        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("root-layout");

        // Top Header
        HBox header = buildHeader();
        mainLayout.setTop(header);

        // Sidebar Navigation
        VBox sidebar = buildSidebar();
        mainLayout.setLeft(sidebar);

        // Views Initialization
        dashboardView = new DashboardView(libraryService, this::showBooksView, this::showMembersView, this::showLoansView);
        bookView = new BookMasterDetailView(libraryService);
        memberView = new MemberMasterDetailView(libraryService);
        loanView = new LoanMasterDetailView(libraryService);

        // Default to Dashboard
        showDashboardView();

        Scene scene = new Scene(mainLayout, 1280, 800);
        // Load CSS stylesheet
        try {
            String cssPath = getClass().getResource("/styles/app.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Warning: CSS file /styles/app.css could not be loaded: " + e.getMessage());
        }

        primaryStage.setTitle("Generic Master-Detail Library Management System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(680);
        primaryStage.show();
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 20, 12, 20));
        header.getStyleClass().add("app-header");

        Label brandIcon = new Label("🏛️");
        brandIcon.setStyle("-fx-font-size: 24px;");

        VBox titleBox = new VBox(2);
        Label title = new Label("Library Master-Detail System");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Generic Repository & UI Framework");
        subtitle.getStyleClass().add("header-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label sysBadge = new Label("JavaFX 21 • JDK 21");
        sysBadge.getStyleClass().add("header-badge");

        header.getChildren().addAll(brandIcon, titleBox, spacer, sysBadge);
        return header;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(16, 12, 16, 12));
        sidebar.getStyleClass().add("app-sidebar");
        sidebar.setPrefWidth(220);

        Label navLabel = new Label("NAVIGATION");
        navLabel.getStyleClass().add("nav-section-label");

        btnDash = createNavButton("📊 Dashboard", this::showDashboardView);
        btnBooks = createNavButton("📚 Books Catalog", this::showBooksView);
        btnMembers = createNavButton("👥 Member Directory", this::showMembersView);
        btnLoans = createNavButton("📖 Circulation Loans", this::showLoansView);

        sidebar.getChildren().addAll(navLabel, btnDash, btnBooks, btnMembers, btnLoans);
        return sidebar;
    }

    private Button createNavButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private void setActiveNavButton(Button activeButton) {
        btnDash.getStyleClass().remove("nav-button-active");
        btnBooks.getStyleClass().remove("nav-button-active");
        btnMembers.getStyleClass().remove("nav-button-active");
        btnLoans.getStyleClass().remove("nav-button-active");

        if (activeButton != null && !activeButton.getStyleClass().contains("nav-button-active")) {
            activeButton.getStyleClass().add("nav-button-active");
        }
    }

    private void showDashboardView() {
        setActiveNavButton(btnDash);
        dashboardView = new DashboardView(libraryService, this::showBooksView, this::showMembersView, this::showLoansView);
        mainLayout.setCenter(dashboardView);
    }

    private void showBooksView() {
        setActiveNavButton(btnBooks);
        bookView.refreshData();
        mainLayout.setCenter(bookView);
    }

    private void showMembersView() {
        setActiveNavButton(btnMembers);
        memberView.refreshData();
        mainLayout.setCenter(memberView);
    }

    private void showLoansView() {
        setActiveNavButton(btnLoans);
        loanView.refreshData();
        mainLayout.setCenter(loanView);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
