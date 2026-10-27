package com.zjgsu.wch.reader;
import com.zjgsu.wch.reader.service.ReaderService;
import com.zjgsu.wch.reader.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest @Transactional
class ReaderServiceTest {
    @Autowired ReaderService service;
    @Test void persistsAndFindsReader() {
        var reader=service.create("王超浩");
        assertThat(service.find(reader.id).name).isEqualTo("王超浩");
        assertThat(reader.active).isTrue();
    }
    @Test void missingReaderFails() { assertThatThrownBy(() -> service.find(999999L)).isInstanceOf(BusinessException.class); }
}
