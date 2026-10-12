package com.zjgsu.wch.model;
import jakarta.persistence.*;
@Entity
@Table(name = "book_copies")
public class BookCopy {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    @Column(nullable = false) private boolean borrowed;
    protected BookCopy() {}
    public BookCopy(Book book) { this.book = book; }
    public Long getId() { return id; }
    public Book getBook() { return book; }
    public boolean isBorrowed() { return borrowed; }
    public void setBorrowed(boolean borrowed) { this.borrowed = borrowed; }
}
