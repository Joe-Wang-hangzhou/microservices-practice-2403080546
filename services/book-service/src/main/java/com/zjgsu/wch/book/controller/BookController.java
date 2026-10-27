package com.zjgsu.wch.book.controller;
import com.zjgsu.wch.book.model.*;
import com.zjgsu.wch.book.service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
@RestController @RequestMapping("/api")
public class BookController {
    private final BookService service;
    public BookController(BookService service) { this.service=service; }
    public record CreateBook(@NotBlank @Size(max=100) String title,
        @NotBlank @Size(max=100) String author,@NotBlank @Size(max=20) String isbn) {}
    public record CopyView(Long id,Long bookId,boolean borrowed) {
        static CopyView from(BookCopy c) { return new CopyView(c.getId(),c.getBook().getId(),c.isBorrowed()); }
    }
    @PostMapping("/books") @ResponseStatus(HttpStatus.CREATED)
    public Book create(@Valid @RequestBody CreateBook body) { return service.create(body.title(),body.author(),body.isbn()); }
    @GetMapping("/books") public List<Book> books() { return service.books(); }
    @GetMapping("/copies") public List<CopyView> copies() { return service.copies().stream().map(CopyView::from).toList(); }
    @GetMapping("/copies/{id}") public CopyView copy(@PathVariable Long id) { return CopyView.from(service.copy(id)); }
    @PostMapping("/copies/{id}/borrow") public CopyView borrow(@PathVariable Long id) { return CopyView.from(service.borrow(id)); }
    @PostMapping("/copies/{id}/return") public CopyView release(@PathVariable Long id) { return CopyView.from(service.release(id)); }
}
