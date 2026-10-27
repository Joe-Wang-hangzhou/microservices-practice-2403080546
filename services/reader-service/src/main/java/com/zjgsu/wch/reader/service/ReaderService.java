package com.zjgsu.wch.reader.service;
import com.zjgsu.wch.reader.model.Reader;
import com.zjgsu.wch.reader.repository.ReaderRepository;
import com.zjgsu.wch.reader.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Transactional
public class ReaderService {
    private final ReaderRepository readers;
    public ReaderService(ReaderRepository readers) { this.readers=readers; }
    public Reader create(String name) { return readers.save(new Reader(name)); }
    @Transactional(readOnly=true)
    public Reader find(Long id) { return readers.findById(id).orElseThrow(() ->
        new BusinessException(HttpStatus.NOT_FOUND,"读者不存在")); }
}
