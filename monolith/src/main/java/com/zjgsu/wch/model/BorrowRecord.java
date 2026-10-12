package com.zjgsu.wch.model;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity
@Table(name = "borrow_records")
public class BorrowRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "copy_id", nullable = false)
    private BookCopy copy;
    @Column(nullable = false, length = 50) private String readerName;
    @Column(nullable = false) private LocalDate borrowedAt;
    @Column(nullable = false) private LocalDate dueAt;
    private LocalDate returnedAt;
    protected BorrowRecord() {}
    public BorrowRecord(BookCopy copy, String readerName) {
        this.copy = copy; this.readerName = readerName;
        this.borrowedAt = LocalDate.now(); this.dueAt = borrowedAt.plusDays(30);
    }
    public Long getId() { return id; }
    public BookCopy getCopy() { return copy; }
    public String getReaderName() { return readerName; }
    public LocalDate getBorrowedAt() { return borrowedAt; }
    public LocalDate getDueAt() { return dueAt; }
    public LocalDate getReturnedAt() { return returnedAt; }
    public void markReturned() { returnedAt = LocalDate.now(); }
}
