package com.zjgsu.wch.service;
import com.zjgsu.wch.model.*;
import com.zjgsu.wch.repository.*;
import com.zjgsu.wch.exception.BusinessException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional
public class LibraryService {
    private final BookRepository books;
    private final BookCopyRepository copies;
    private final BorrowRecordRepository records;
    public LibraryService(BookRepository books, BookCopyRepository copies, BorrowRecordRepository records) {
        this.books = books; this.copies = copies; this.records = records;
    }
    @Transactional(readOnly = true)
    public Book findBook(Long id) { return books.findById(id).orElseThrow(() ->
        new BusinessException(HttpStatus.NOT_FOUND, "书目不存在")); }
    @Transactional(readOnly = true)
    public BorrowRecord findRecord(Long id) { return records.findById(id).orElseThrow(() ->
        new BusinessException(HttpStatus.NOT_FOUND, "借阅记录不存在")); }
    public Book createBook(String title, String author, String isbn) {
        if (books.existsByIsbn(isbn)) throw new BusinessException(HttpStatus.CONFLICT, "ISBN 已存在");
        Book book = books.save(new Book(title, author, isbn));
        copies.save(new BookCopy(book));
        return book;
    }
    @Transactional(readOnly = true)
    public List<Book> findBooks() { return books.findAll(); }
    @Transactional(readOnly = true)
    public List<BookCopy> findCopies() { return copies.findAll(); }
    @Transactional(readOnly = true)
    public List<BorrowRecord> findRecords() { return records.findAll(); }
    public BorrowRecord borrow(Long copyId, String readerName) {
        BookCopy copy = copies.findById(copyId).orElseThrow(() ->
            new BusinessException(HttpStatus.NOT_FOUND, "馆藏副本不存在"));
        if (copy.isBorrowed()) throw new BusinessException(HttpStatus.CONFLICT, "该副本已借出，请先归还");
        copy.setBorrowed(true);
        copies.save(copy);
        return records.save(new BorrowRecord(copy, readerName));
    }
    public BorrowRecord returnBook(Long recordId) {
        BorrowRecord record = records.findById(recordId).orElseThrow(() ->
            new BusinessException(HttpStatus.NOT_FOUND, "借阅记录不存在"));
        if (record.getReturnedAt() != null)
            throw new BusinessException(HttpStatus.CONFLICT, "该借阅记录已归还");
        record.markReturned();
        record.getCopy().setBorrowed(false);
        copies.save(record.getCopy());
        return records.save(record);
    }
}
