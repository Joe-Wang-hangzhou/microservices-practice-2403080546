package com.zjgsu.wch.book;
import com.zjgsu.wch.book.service.BookService;
import com.zjgsu.wch.book.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest @Transactional
class BookServiceTest {
    @Autowired BookService service;
    @Test void createsCopyAndSupportsBorrowReturn() {
        var book=service.create("微服务","王超浩","test-isbn");
        var copy=service.copies().stream().filter(c -> c.getBook().getId().equals(book.getId())).findFirst().orElseThrow();
        assertThat(service.borrow(copy.getId()).isBorrowed()).isTrue();
        assertThatThrownBy(() -> service.borrow(copy.getId())).isInstanceOf(BusinessException.class);
        assertThat(service.release(copy.getId()).isBorrowed()).isFalse();
    }
    @Test void rejectsDuplicateIsbn() {
        service.create("图书","作者","unique-test");
        assertThatThrownBy(() -> service.create("图书","作者","unique-test")).isInstanceOf(BusinessException.class);
    }
}
