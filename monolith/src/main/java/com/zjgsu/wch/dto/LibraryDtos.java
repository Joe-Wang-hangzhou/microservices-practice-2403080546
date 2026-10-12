package com.zjgsu.wch.dto;
import com.zjgsu.wch.model.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public final class LibraryDtos {
    private LibraryDtos() {}
    public record CreateBook(@NotBlank @Size(max=100) String title,
        @NotBlank @Size(max=100) String author, @NotBlank @Size(max=20) String isbn) {}
    public record BorrowRequest(@NotNull @Positive Long copyId,
        @NotBlank @Size(max=50) String readerName) {}
    public record BookResponse(Long id, String title, String author, String isbn) {
        public static BookResponse from(Book b) {
            return new BookResponse(b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn());
        }
    }
    public record CopyResponse(Long id, Long bookId, boolean borrowed) {
        public static CopyResponse from(BookCopy c) {
            return new CopyResponse(c.getId(), c.getBook().getId(), c.isBorrowed());
        }
    }
    public record BorrowResponse(Long id, Long copyId, String readerName,
        LocalDate borrowedAt, LocalDate dueAt, LocalDate returnedAt) {
        public static BorrowResponse from(BorrowRecord b) {
            return new BorrowResponse(b.getId(), b.getCopy().getId(), b.getReaderName(),
                b.getBorrowedAt(), b.getDueAt(), b.getReturnedAt());
        }
    }
}
