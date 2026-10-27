package com.zjgsu.wch.loan;
import com.zjgsu.wch.loan.service.*;
import com.zjgsu.wch.loan.repository.LoanRepository;
import com.zjgsu.wch.loan.exception.BusinessException;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest
class LoanServiceTest {
    static HttpServer remote;
    static AtomicInteger readerStatus=new AtomicInteger(200),bookStatus=new AtomicInteger(200),bookCalls=new AtomicInteger();
    static {
        try {
            remote=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
            remote.createContext("/api/readers/",exchange -> {
                byte[] body="{\"id\":1,\"name\":\"reader\",\"active\":true}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type","application/json");
                exchange.sendResponseHeaders(readerStatus.get(),body.length); exchange.getResponseBody().write(body); exchange.close();
            });
            remote.createContext("/api/copies/",exchange -> {
                bookCalls.incrementAndGet();
                byte[] body="{\"id\":1,\"bookId\":1,\"borrowed\":true}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type","application/json");
                exchange.sendResponseHeaders(bookStatus.get(),body.length); exchange.getResponseBody().write(body); exchange.close();
            });
            remote.start();
        } catch(Exception ex) { throw new RuntimeException(ex); }
    }
    @DynamicPropertySource static void urls(DynamicPropertyRegistry registry) {
        registry.add("dependencies.reader-url",() -> "http://127.0.0.1:"+remote.getAddress().getPort());
        registry.add("dependencies.book-url",() -> "http://127.0.0.1:"+remote.getAddress().getPort());
    }
    @Autowired LoanService service;
    @Autowired LoanRepository repository;
    @BeforeEach void reset() { repository.deleteAll(); readerStatus.set(200);bookStatus.set(200);bookCalls.set(0); }
    @AfterAll static void stop() { remote.stop(0); }
    @Test void borrowsAndReturnsThroughRealHttp() {
        var loan=service.borrow(1L,1L);
        assertThat(repository.count()).isEqualTo(1);
        assertThat(service.returnBook(loan.id).returnedAt).isNotNull();
        assertThat(bookCalls.get()).isEqualTo(2);
    }
    @Test void missingReaderStopsBeforeBookMutation() {
        readerStatus.set(404);
        assertThatThrownBy(() -> service.borrow(999L,1L)).isInstanceOfSatisfying(BusinessException.class,
            ex -> assertThat(ex.getStatus().value()).isEqualTo(404));
        assertThat(repository.count()).isZero(); assertThat(bookCalls.get()).isZero();
    }
    @Test void unavailableBookDoesNotWriteLoan() {
        bookStatus.set(503);
        assertThatThrownBy(() -> service.borrow(1L,1L)).isInstanceOfSatisfying(BusinessException.class,
            ex -> assertThat(ex.getStatus().value()).isEqualTo(503));
        assertThat(repository.count()).isZero();
    }
    @Test void connectionRefusedReturns503() {
        var client=new LibraryClient("http://127.0.0.1:1","http://127.0.0.1:1",200,200);
        assertThatThrownBy(() -> client.validateReader(1L)).isInstanceOfSatisfying(BusinessException.class,
            ex -> assertThat(ex.getStatus().value()).isEqualTo(503));
    }
    @Test void missingCopyDoesNotWriteLoan() {
        bookStatus.set(404);
        assertThatThrownBy(() -> service.borrow(1L,999L)).isInstanceOf(BusinessException.class);
        assertThat(repository.count()).isZero();
    }
}
