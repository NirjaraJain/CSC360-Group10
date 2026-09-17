package com.library.service;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.LoanStatus;
import com.library.model.Member;
import com.library.repository.GenericRepository;
import com.library.repository.InMemoryRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer coordinating Generic Repositories for Books, Members, and Loans.
 */
public class LibraryService {

    private final GenericRepository<Book, String> bookRepository;
    private final GenericRepository<Member, String> memberRepository;
    private final GenericRepository<Loan, String> loanRepository;

    public LibraryService() {
        this.bookRepository = new InMemoryRepository<>();
        this.memberRepository = new InMemoryRepository<>();
        this.loanRepository = new InMemoryRepository<>();
        seedInitialData();
    }

    public GenericRepository<Book, String> getBookRepository() {
        return bookRepository;
    }

    public GenericRepository<Member, String> getMemberRepository() {
        return memberRepository;
    }

    public GenericRepository<Loan, String> getLoanRepository() {
        return loanRepository;
    }

    // High-level circulation actions
    public Loan checkoutBook(String bookId, String memberId, int loanDays) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book with ID " + bookId + " not found."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member with ID " + memberId + " not found."));

        if (!book.isAvailable()) {
            throw new IllegalStateException("No available copies of '" + book.getTitle() + "' to borrow.");
        }

        if (!"ACTIVE".equalsIgnoreCase(member.getStatus())) {
            throw new IllegalStateException("Member account is suspended or inactive.");
        }

        // Deduct available copy
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // Update member active count
        member.setActiveLoansCount(member.getActiveLoansCount() + 1);
        memberRepository.save(member);

        // Create loan
        String loanId = "LN-" + (1000 + loanRepository.count() + 1);
        LocalDate now = LocalDate.now();
        LocalDate due = now.plusDays(loanDays <= 0 ? 14 : loanDays);

        Loan loan = new Loan(loanId, book.getId(), book.getTitle(), member.getId(), member.getName(),
                now, due, null, LoanStatus.ACTIVE, 0.0);

        return loanRepository.save(loan);
    }

    public Loan returnBook(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan with ID " + loanId + " not found."));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new IllegalStateException("This loan has already been returned.");
        }

        LocalDate now = LocalDate.now();
        loan.setReturnDate(now);
        loan.setStatus(LoanStatus.RETURNED);

        // Calculate fine if overdue
        if (now.isAfter(loan.getDueDate())) {
            long daysOverdue = ChronoUnit.DAYS.between(loan.getDueDate(), now);
            loan.setFineAmount(daysOverdue * 1.50); // $1.50 per day
        }

        // Return copy to book catalog
        bookRepository.findById(loan.getBookId()).ifPresent(b -> {
            b.setAvailableCopies(b.getAvailableCopies() + 1);
            bookRepository.save(b);
        });

        // Decrement member active loan count
        memberRepository.findById(loan.getMemberId()).ifPresent(m -> {
            m.setActiveLoansCount(Math.max(0, m.getActiveLoansCount() - 1));
            memberRepository.save(m);
        });

        return loanRepository.save(loan);
    }

    public void updateOverdueStatuses() {
        LocalDate now = LocalDate.now();
        List<Loan> activeLoans = loanRepository.search(l -> l.getStatus() == LoanStatus.ACTIVE);
        for (Loan loan : activeLoans) {
            if (now.isAfter(loan.getDueDate())) {
                loan.setStatus(LoanStatus.OVERDUE);
                long daysOverdue = ChronoUnit.DAYS.between(loan.getDueDate(), now);
                loan.setFineAmount(daysOverdue * 1.50);
                loanRepository.save(loan);
            }
        }
    }

    private void seedInitialData() {
        // Seed Books
        bookRepository.save(new Book("BK-101", "Clean Code: Refactoring & Patterns", "978-0132350884", "Robert C. Martin", "Software Engineering", 2008, 5, 3, 4.8, "A handbook of agile software craftsmanship outlining clean coding principles and techniques."));
        bookRepository.save(new Book("BK-102", "Effective Java (3rd Edition)", "978-0134685991", "Joshua Bloch", "Computer Science", 2017, 4, 2, 4.9, "Best practices guide for the Java programming language covering design patterns and lambdas."));
        bookRepository.save(new Book("BK-103", "Design Patterns: Reusable Object-Oriented Software", "978-0201633610", "Erich Gamma et al.", "Software Architecture", 1994, 3, 0, 4.7, "Classic Gang of Four reference catalog for object-oriented design patterns."));
        bookRepository.save(new Book("BK-104", "The Pragmatic Programmer", "978-0135957059", "Andrew Hunt, David Thomas", "Software Engineering", 2019, 6, 5, 4.9, "Timeless advice for modern programmers on career growth, debugging, and software craft."));
        bookRepository.save(new Book("BK-105", "Introduction to Algorithms (4th Edition)", "978-0262046305", "Thomas H. Cormen et al.", "Computer Science", 2022, 2, 1, 4.6, "Comprehensive textbook covering fundamental computer algorithms and data structures."));
        bookRepository.save(new Book("BK-106", "JavaFX in Action", "978-1617290886", "Carl Dea", "UI Engineering", 2020, 4, 4, 4.5, "Detailed guide to modern desktop client application development with JavaFX."));

        // Seed Members
        memberRepository.save(new Member("MB-1001", "Alice Johnson", "alice.j@university.edu", "+1 (555) 234-5678", "Student", LocalDate.of(2023, 9, 1), 2, "ACTIVE"));
        memberRepository.save(new Member("MB-1002", "Prof. Robert Langdon", "rlangdon@harvard.edu", "+1 (555) 987-6543", "Faculty", LocalDate.of(2021, 1, 15), 1, "ACTIVE"));
        memberRepository.save(new Member("MB-1003", "Clara Oswald", "clara.oswald@gmail.com", "+1 (555) 456-7890", "Regular", LocalDate.of(2024, 2, 10), 0, "ACTIVE"));
        memberRepository.save(new Member("MB-1004", "David Miller", "david.miller@techcorp.io", "+1 (555) 321-7654", "Student", LocalDate.of(2022, 11, 20), 1, "ACTIVE"));
        memberRepository.save(new Member("MB-1005", "Eleanor Vance", "evance@hillhouse.org", "+1 (555) 654-0987", "Regular", LocalDate.of(2023, 5, 12), 0, "SUSPENDED"));

        // Seed Loans
        LocalDate now = LocalDate.now();
        loanRepository.save(new Loan("LN-1001", "BK-101", "Clean Code: Refactoring & Patterns", "MB-1001", "Alice Johnson", now.minusDays(10), now.plusDays(4), null, LoanStatus.ACTIVE, 0.0));
        loanRepository.save(new Loan("LN-1002", "BK-102", "Effective Java (3rd Edition)", "MB-1001", "Alice Johnson", now.minusDays(5), now.plusDays(9), null, LoanStatus.ACTIVE, 0.0));
        loanRepository.save(new Loan("LN-1003", "BK-103", "Design Patterns: Reusable Object-Oriented Software", "MB-1002", "Prof. Robert Langdon", now.minusDays(20), now.minusDays(6), null, LoanStatus.OVERDUE, 9.00));
        loanRepository.save(new Loan("LN-1004", "BK-104", "The Pragmatic Programmer", "MB-1004", "David Miller", now.minusDays(15), now.minusDays(1), now.minusDays(1), LoanStatus.RETURNED, 0.0));
    }
}
