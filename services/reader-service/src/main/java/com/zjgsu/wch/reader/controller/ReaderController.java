package com.zjgsu.wch.reader.controller;
import com.zjgsu.wch.reader.model.Reader;
import com.zjgsu.wch.reader.service.ReaderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
@RestController @RequestMapping("/api/readers")
public class ReaderController {
    private final ReaderService service;
    public ReaderController(ReaderService service) { this.service=service; }
    public record CreateReader(@NotBlank @Size(max=50) String name) {}
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Reader create(@Valid @RequestBody CreateReader body) { return service.create(body.name()); }
    @GetMapping("/{id}") public Reader find(@PathVariable Long id) { return service.find(id); }
}
