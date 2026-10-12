package com.zjgsu.wch.controller;
import com.zjgsu.wch.model.*;
import com.zjgsu.wch.service.LibraryService;
import com.zjgsu.wch.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(LibraryController.class)
class LibraryControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean LibraryService library;
    @Test void createsBook() throws Exception {
        given(library.createBook("Java", "Author", "api-1")).willReturn(new Book("Java", "Author", "api-1"));
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
            .content("""
{"title":"Java","author":"Author","isbn":"api-1"}
"""))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.title").value("Java"));
    }
    @Test void listsBooks() throws Exception {
        given(library.findBooks()).willReturn(List.of(new Book("Java", "Author", "api-2")));
        mvc.perform(get("/api/books")).andExpect(status().isOk()).andExpect(jsonPath("$[0].title").value("Java"));
    }
    @Test void rejectsInvalidInput() throws Exception {
        mvc.perform(post("/api/borrows").contentType(MediaType.APPLICATION_JSON)
            .content("""
{"copyId":-1,"readerName":""}
"""))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(library);
    }
    @Test void returnsConflict() throws Exception {
        given(library.borrow(1L, "Joe")).willThrow(new BusinessException(HttpStatus.CONFLICT, "该副本已借出，请先归还"));
        mvc.perform(post("/api/borrows").contentType(MediaType.APPLICATION_JSON)
            .content("""
{"copyId":1,"readerName":"Joe"}
"""))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.detail").value("该副本已借出，请先归还"));
    }
    @Test void returnsNotFound() throws Exception {
        given(library.returnBook(999L)).willThrow(new BusinessException(HttpStatus.NOT_FOUND, "借阅记录不存在"));
        mvc.perform(post("/api/borrows/999/return")).andExpect(status().isNotFound());
    }
}
