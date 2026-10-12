package com.zjgsu.wch.repository;
import com.zjgsu.wch.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import jakarta.persistence.EntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
class LibraryRepositoryTest {
    @Autowired EntityManager entityManager;
    @Autowired BookRepository books;
    @Autowired BookCopyRepository copies;
    @Autowired BorrowRecordRepository records;
    @Test void savesAndReadsRelatedEntities() {
        Book book = books.saveAndFlush(new Book("Java", "Author", "repo-1"));
        BookCopy copy = copies.saveAndFlush(new BookCopy(book));
        BorrowRecord record = records.saveAndFlush(new BorrowRecord(copy, "Joe"));
        entityManager.clear(); // force database reads instead of returning cached entities
        assertEquals(book.getId(), copies.findById(copy.getId()).orElseThrow().getBook().getId());
        assertEquals("Joe", records.findById(record.getId()).orElseThrow().getReaderName());
    }
    @Test void rejectsDuplicateIsbn() {
        books.saveAndFlush(new Book("Java", "Author", "same-isbn"));
        assertThrows(DataIntegrityViolationException.class,
            () -> books.saveAndFlush(new Book("Other", "Author", "same-isbn")));
    }
}
