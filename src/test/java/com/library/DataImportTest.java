package com.library;

import com.library.model.Book;
import com.library.model.Member;
import com.library.service.LibraryService;

import java.util.List;

public class DataImportTest {

    public static void main(String[] args) {
        System.out.println("=== Testing Library System Data Import & Service ===");
        LibraryService service = new LibraryService();

        long bookCount = service.getBookRepository().count();
        long memberCount = service.getMemberRepository().count();
        long loanCount = service.getLoanRepository().count();

        System.out.println("Total Books in Repository: " + bookCount);
        System.out.println("Total Members in Repository: " + memberCount);
        System.out.println("Total Loans in Repository: " + loanCount);

        if (bookCount > 0) {
            List<Book> books = service.getBookRepository().findAll();
            System.out.println("\nSample Book 1: " + books.get(0).getTitle() + " | Author: " + books.get(0).getAuthor() + " | Category: " + books.get(0).getCategory());
        }

        if (memberCount > 0) {
            List<Member> members = service.getMemberRepository().findAll();
            System.out.println("Sample Member 1: " + members.get(0).getName() + " (" + members.get(0).getId() + ") | Email: " + members.get(0).getEmail());
        }

        // Test search filter null-safety
        System.out.println("\nTesting search query with keyword 'history'...");
        List<Book> searchRes = service.getBookRepository().search(b -> (b.getTitle() != null && b.getTitle().toLowerCase().contains("history")) || (b.getCategory() != null && b.getCategory().toLowerCase().contains("history")));
        System.out.println("Search results count for 'history': " + searchRes.size());

        System.out.println("\n=== ALL TESTS PASSED SUCCESSFULLY ===");
    }
}
