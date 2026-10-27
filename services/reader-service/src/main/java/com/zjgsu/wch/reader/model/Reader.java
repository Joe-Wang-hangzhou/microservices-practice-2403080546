package com.zjgsu.wch.reader.model;
import jakarta.persistence.*;
@Entity @Table(name="readers")
public class Reader {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false, length=50) public String name;
    @Column(nullable=false) public boolean active = true;
    protected Reader() {}
    public Reader(String name) { this.name=name; }
}
