package com.zjgsu.wch.book.service;
import com.zjgsu.wch.book.model.*;
import com.zjgsu.wch.book.repository.*;
import com.zjgsu.wch.book.exception.BusinessException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Transactional
public class BookService {
    private final BookRepository books;
    private final BookCopyRepository copies;
    public BookService(BookRepository books, BookCopyRepository copies) { this.books=books; this.copies=copies; }
    public Book create(String title,String author,String isbn) {
        if(books.existsByIsbn(isbn)) throw new BusinessException(HttpStatus.CONFLICT,"ISBN 已存在");
        Book book=books.save(new Book(title,author,isbn)); copies.save(new BookCopy(book)); return book;
    }
    @Transactional(readOnly=true)
    public List<Book> books() { return books.findAll(); }
    @Transactional(readOnly=true)
    public BookCopy copy(Long id) { return copies.findById(id).orElseThrow(() ->
        new BusinessException(HttpStatus.NOT_FOUND,"馆藏副本不存在")); }
    @Transactional(readOnly=true)
    public List<BookCopy> copies() { return copies.findAll(); }
    public BookCopy borrow(Long id) {
        BookCopy copy=copy(id);
        if(copy.isBorrowed()) throw new BusinessException(HttpStatus.CONFLICT,"副本已借出");
        copy.setBorrowed(true); return copies.save(copy);
    }
    public BookCopy release(Long id) {
        BookCopy copy=copy(id);
        if(!copy.isBorrowed()) throw new BusinessException(HttpStatus.CONFLICT,"副本已归还");
        copy.setBorrowed(false); return copies.save(copy);
    }
}
