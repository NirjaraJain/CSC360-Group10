package com.library.util;

import com.library.model.Book;
import com.library.model.Member;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

/**
 * Robust Data Import Service for reading Books and Members from CSV and Excel (.xlsx, .xls) files.
 */
public class DataImportService {

    /**
     * Parses a CSV or Excel file to extract Book entities.
     */
    public static List<Book> importBooksFromFile(File file) throws Exception {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".csv")) {
            return parseBooksFromCsv(file);
        } else if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
            return parseBooksFromExcel(file);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + file.getName());
        }
    }

    /**
     * Parses a CSV or Excel file to extract Member entities.
     */
    public static List<Member> importMembersFromFile(File file) throws Exception {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".csv")) {
            return parseMembersFromCsv(file);
        } else if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
            return parseMembersFromExcel(file);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + file.getName());
        }
    }

    public static List<Book> parseBooksFromCsv(File file) throws Exception {
        List<Book> books = new ArrayList<>();
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setIgnoreHeaderCase(true).setTrim(true).build())) {

            List<CSVRecord> records = csvParser.getRecords();
            if (records.isEmpty()) return books;

            // Header mapping
            CSVRecord headerRecord = records.get(0);
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headerRecord.size(); i++) {
                headerMap.put(headerRecord.get(i).trim().toLowerCase(), i);
            }

            boolean hasHeaders = headerMap.containsKey("title") || headerMap.containsKey("authors") || headerMap.containsKey("author");
            int startIndex = hasHeaders ? 1 : 0;

            int idCounter = 101;
            for (int i = startIndex; i < records.size(); i++) {
                CSVRecord rec = records.get(i);
                if (rec.size() == 0) continue;

                String title = getVal(rec, headerMap, "title", 0);
                if (title == null || title.isBlank()) continue; // skip blank titles

                String author = getVal(rec, headerMap, "authors", 1);
                if (author == null || author.isBlank()) author = getVal(rec, headerMap, "author", 1);
                if (author == null || author.isBlank()) author = "Unknown Author";

                String description = getVal(rec, headerMap, "description", 2);
                String category = getVal(rec, headerMap, "category", 3);
                if (category == null || category.isBlank()) category = "General";

                String isbn = getVal(rec, headerMap, "isbn", -1);
                if (isbn == null || isbn.isBlank()) {
                    isbn = "978-" + String.format("%09d", Math.abs((title + author).hashCode() % 1000000000L));
                }

                String yearStr = getVal(rec, headerMap, "publish date (year)", 7);
                if (yearStr == null || yearStr.isBlank()) yearStr = getVal(rec, headerMap, "year", 7);
                int year = parseYear(yearStr, 2020);

                String ratingStr = getVal(rec, headerMap, "rating", -1);
                double rating = parseDouble(ratingStr, 4.2 + (Math.abs(title.hashCode() % 8) / 10.0));

                String id = "BK-" + idCounter++;
                int totalCopies = 3 + (Math.abs(title.hashCode()) % 5);
                int availCopies = Math.max(1, totalCopies - (Math.abs(author.hashCode()) % 3));

                books.add(new Book(id, title, isbn, author, category.trim(), year, totalCopies, availCopies, rating, description));
            }
        }
        return books;
    }

    public static List<Book> parseBooksFromExcel(File file) throws Exception {
        List<Book> books = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            int idCounter = 101;
            int rowIndex = 0;
            Map<String, Integer> headerMap = new HashMap<>();

            for (Row row : sheet) {
                if (rowIndex == 0) {
                    for (Cell cell : row) {
                        headerMap.put(formatter.formatCellValue(cell).trim().toLowerCase(), cell.getColumnIndex());
                    }
                    rowIndex++;
                    continue;
                }

                String title = getCellVal(row, headerMap, formatter, "title", 0);
                if (title == null || title.isBlank()) {
                    rowIndex++;
                    continue;
                }

                String author = getCellVal(row, headerMap, formatter, "author", 1);
                if (author == null || author.isBlank()) author = getCellVal(row, headerMap, formatter, "authors", 1);
                if (author == null || author.isBlank()) author = "Unknown Author";

                String description = getCellVal(row, headerMap, formatter, "description", 2);
                String category = getCellVal(row, headerMap, formatter, "category", 3);
                if (category == null || category.isBlank()) category = "General";

                String isbn = getCellVal(row, headerMap, formatter, "isbn", -1);
                if (isbn == null || isbn.isBlank()) {
                    isbn = "978-" + String.format("%09d", Math.abs((title + author).hashCode() % 1000000000L));
                }

                String yearStr = getCellVal(row, headerMap, formatter, "year", 4);
                int year = parseYear(yearStr, 2021);

                String id = "BK-" + idCounter++;
                int totalCopies = 4;
                int availCopies = 3;

                books.add(new Book(id, title, isbn, author, category, year, totalCopies, availCopies, 4.5, description));
                rowIndex++;
            }
        }
        return books;
    }

    public static List<Member> parseMembersFromCsv(File file) throws Exception {
        List<Member> members = new ArrayList<>();
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setIgnoreHeaderCase(true).setTrim(true).build())) {

            List<CSVRecord> records = csvParser.getRecords();
            if (records.isEmpty()) return members;

            Map<String, Integer> headerMap = new HashMap<>();
            CSVRecord header = records.get(0);
            for (int i = 0; i < header.size(); i++) {
                headerMap.put(header.get(i).trim().toLowerCase(), i);
            }

            int startIndex = headerMap.containsKey("name") || headerMap.containsKey("email") ? 1 : 0;
            for (int i = startIndex; i < records.size(); i++) {
                CSVRecord rec = records.get(i);
                String name = getVal(rec, headerMap, "name", 1);
                if (name == null || name.isBlank()) continue;

                String id = getVal(rec, headerMap, "enrolment number", 0);
                if (id == null || id.isBlank()) id = getVal(rec, headerMap, "id", 0);
                if (id == null || id.isBlank()) id = "MB-" + (1000 + i);

                String email = getVal(rec, headerMap, "email", 2);
                if (email == null || email.isBlank()) email = name.toLowerCase().replaceAll("[^a-z]", "") + "@university.edu";

                String phone = getVal(rec, headerMap, "phone", 3);
                if (phone == null || phone.isBlank()) phone = "+1 (555) " + (100 + i) + "-4321";

                String programme = getVal(rec, headerMap, "programme", 4);
                String type = parseMembershipType(programme);

                members.add(new Member(id, name, email, phone, type, LocalDate.now().minusMonths(i % 12), 0, "ACTIVE"));
            }
        }
        return members;
    }

    public static List<Member> parseMembersFromExcel(File file) throws Exception {
        List<Member> members = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            int idCol = -1, nameCol = -1, progCol = -1, emailCol = -1, phoneCol = -1;

            // Scan first 10 rows to locate header row
            int headerRowIndex = -1;
            for (int r = 0; r < Math.min(10, sheet.getLastRowNum() + 1); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                for (Cell cell : row) {
                    String val = formatter.formatCellValue(cell).trim().toLowerCase();
                    if (val.contains("enrolment") || val.contains("enrollment") || val.equals("id")) idCol = cell.getColumnIndex();
                    if (val.equals("name") || val.contains("full name")) nameCol = cell.getColumnIndex();
                    if (val.contains("programme") || val.contains("program") || val.contains("type")) progCol = cell.getColumnIndex();
                    if (val.contains("email")) emailCol = cell.getColumnIndex();
                    if (val.contains("phone") || val.contains("mobile") || val.contains("contact")) phoneCol = cell.getColumnIndex();
                }
                if (nameCol != -1) {
                    headerRowIndex = r;
                    break;
                }
            }

            // Fallbacks for positional columns if header missing
            if (idCol == -1) idCol = 1;
            if (nameCol == -1) nameCol = 2;
            if (progCol == -1) progCol = 3;
            if (emailCol == -1) emailCol = 4;
            if (phoneCol == -1) phoneCol = 5;

            int autoId = 1001;
            for (int r = (headerRowIndex >= 0 ? headerRowIndex + 1 : 0); r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String name = safeGetCell(row, nameCol, formatter);
                if (name == null || name.isBlank() || name.equalsIgnoreCase("Name") || name.equalsIgnoreCase("Course")) continue;

                String id = safeGetCell(row, idCol, formatter);
                if (id == null || id.isBlank() || id.equalsIgnoreCase("Enrolment Number")) id = "MB-" + autoId;
                autoId++;

                String programme = safeGetCell(row, progCol, formatter);
                String email = safeGetCell(row, emailCol, formatter);
                if (email == null || email.isBlank()) email = id.toLowerCase() + "@university.edu";

                String phone = safeGetCell(row, phoneCol, formatter);
                if (phone == null || phone.isBlank()) phone = "+1 (555) 01" + (r % 90 + 10);

                String type = parseMembershipType(programme);
                members.add(new Member(id, name, email, phone, type, LocalDate.now().minusDays(r * 3L % 300), 0, "ACTIVE"));
            }
        }
        return members;
    }

    private static String safeGetCell(Row row, int colIndex, DataFormatter formatter) {
        if (colIndex < 0 || row == null) return "";
        Cell cell = row.getCell(colIndex);
        if (cell == null) return "";
        return formatter.formatCellValue(cell).trim();
    }

    private static String getVal(CSVRecord rec, Map<String, Integer> map, String colName, int fallbackIndex) {
        if (map.containsKey(colName)) {
            int idx = map.get(colName);
            if (idx < rec.size()) return rec.get(idx).trim();
        }
        if (fallbackIndex >= 0 && fallbackIndex < rec.size()) {
            return rec.get(fallbackIndex).trim();
        }
        return "";
    }

    private static String getCellVal(Row row, Map<String, Integer> map, DataFormatter formatter, String colName, int fallbackIndex) {
        if (map.containsKey(colName)) {
            int idx = map.get(colName);
            Cell cell = row.getCell(idx);
            if (cell != null) return formatter.formatCellValue(cell).trim();
        }
        if (fallbackIndex >= 0) {
            Cell cell = row.getCell(fallbackIndex);
            if (cell != null) return formatter.formatCellValue(cell).trim();
        }
        return "";
    }

    private static int parseYear(String val, int defaultYear) {
        if (val == null || val.isBlank()) return defaultYear;
        try {
            return Integer.parseInt(val.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return defaultYear;
        }
    }

    private static double parseDouble(String val, double defaultVal) {
        if (val == null || val.isBlank()) return defaultVal;
        try {
            return Double.parseDouble(val.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private static String parseMembershipType(String programme) {
        if (programme == null) return "Regular";
        String lower = programme.toLowerCase();
        if (lower.contains("btech") || lower.contains("student") || lower.contains("bsc") || lower.contains("engineering")) {
            return "Student";
        } else if (lower.contains("prof") || lower.contains("faculty") || lower.contains("dr.")) {
            return "Faculty";
        }
        return "Regular";
    }
}
