package com.zjgsu.wch.book.model;
import jakarta.persistence.*;
@Entity
@Table(name = "books")
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String title;
    @Column(nullable = false, length = 100) private String author;
    @Column(nullable = false, unique = true, length = 20) private String isbn;
    protected Book() {}
    public Book(String title, String author, String isbn) {
        this.title = title; this.author = author; this.isbn = isbn;
    }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
}
