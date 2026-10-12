package com.zjgsu.wch.service;
import com.zjgsu.wch.model.*;
import com.zjgsu.wch.repository.*;
import com.zjgsu.wch.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class LibraryServiceTest {
    @Autowired LibraryService library;
    @Autowired BookCopyRepository copies;
    @Autowired BorrowRecordRepository records;
    @Test void borrowReturnAndBorrowAgain() {
        Book book = library.createBook("Java", "Author", "service-1");
        BookCopy copy = copies.findAll().stream().filter(c -> c.getBook().getId().equals(book.getId())).findFirst().orElseThrow();
        BorrowRecord record = library.borrow(copy.getId(), "Joe");
        assertTrue(copy.isBorrowed());
        assertEquals(record.getBorrowedAt().plusDays(30), record.getDueAt());
        BusinessException ex = assertThrows(BusinessException.class, () -> library.borrow(copy.getId(), "Other"));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals(1, records.count());
        library.returnBook(record.getId());
        assertFalse(copy.isBorrowed());
        assertNotNull(record.getReturnedAt());
        assertThrows(BusinessException.class, () -> library.returnBook(record.getId()));
        assertNotNull(library.borrow(copy.getId(), "Other"));
    }
    @Test void missingCopyDoesNotCreateRecord() {
        BusinessException ex = assertThrows(BusinessException.class, () -> library.borrow(99999L, "Joe"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals(0, records.count());
    }
}
