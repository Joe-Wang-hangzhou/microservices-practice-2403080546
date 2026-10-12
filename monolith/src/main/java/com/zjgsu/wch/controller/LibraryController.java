package com.zjgsu.wch.controller;
import com.zjgsu.wch.dto.LibraryDtos.*;
import com.zjgsu.wch.service.LibraryService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api")
public class LibraryController {
    private final LibraryService library;
    public LibraryController(LibraryService library) { this.library = library; }
    @PostMapping("/books")
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody CreateBook request) {
        BookResponse response = BookResponse.from(library.createBook(request.title(), request.author(), request.isbn()));
        return ResponseEntity.created(URI.create("/api/books/" + response.id())).body(response);
    }
    @GetMapping("/books/{id}")
    public BookResponse book(@PathVariable Long id) { return BookResponse.from(library.findBook(id)); }
    @GetMapping("/borrows/{id}")
    public BorrowResponse record(@PathVariable Long id) { return BorrowResponse.from(library.findRecord(id)); }
    @GetMapping("/books")
    public List<BookResponse> books() { return library.findBooks().stream().map(BookResponse::from).toList(); }
    @GetMapping("/copies")
    public List<CopyResponse> copies() { return library.findCopies().stream().map(CopyResponse::from).toList(); }
    @PostMapping("/borrows")
    public ResponseEntity<BorrowResponse> borrow(@Valid @RequestBody BorrowRequest request) {
        BorrowResponse response = BorrowResponse.from(library.borrow(request.copyId(), request.readerName()));
        return ResponseEntity.created(URI.create("/api/borrows/" + response.id())).body(response);
    }
    @GetMapping("/borrows")
    public List<BorrowResponse> borrows() { return library.findRecords().stream().map(BorrowResponse::from).toList(); }
    @PostMapping("/borrows/{id}/return")
    public BorrowResponse returnBook(@PathVariable Long id) { return BorrowResponse.from(library.returnBook(id)); }
}
