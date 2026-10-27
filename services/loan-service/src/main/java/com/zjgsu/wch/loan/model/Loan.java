package com.zjgsu.wch.loan.model;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity @Table(name="loans")
public class Loan {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public Long readerId;
    @Column(nullable=false) public Long copyId;
    @Column(nullable=false) public LocalDate borrowedAt;
    @Column(nullable=false) public LocalDate dueAt;
    public LocalDate returnedAt;
    protected Loan() {}
    public Loan(Long readerId,Long copyId) {
        this.readerId=readerId; this.copyId=copyId;
        borrowedAt=LocalDate.now(); dueAt=borrowedAt.plusDays(30);
    }
}
