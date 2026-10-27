package com.zjgsu.wch.loan.controller;
import com.zjgsu.wch.loan.model.Loan;
import com.zjgsu.wch.loan.service.LoanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/loans")
public class LoanController {
    private final LoanService service;
    public LoanController(LoanService service) { this.service=service; }
    public record BorrowRequest(@NotNull @Positive Long readerId,@NotNull @Positive Long copyId) {}
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Loan borrow(@Valid @RequestBody BorrowRequest body) { return service.borrow(body.readerId(),body.copyId()); }
    @PostMapping("/{id}/return") public Loan release(@PathVariable Long id) { return service.returnBook(id); }
    @GetMapping public List<Loan> all() { return service.all(); }
    @GetMapping("/{id}") public Loan find(@PathVariable Long id) { return service.find(id); }
}
