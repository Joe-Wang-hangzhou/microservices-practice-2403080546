package com.zjgsu.wch.loan.service;
import com.zjgsu.wch.loan.model.Loan;
import com.zjgsu.wch.loan.repository.LoanRepository;
import com.zjgsu.wch.loan.exception.BusinessException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Transactional
public class LoanService {
    private final LoanRepository loans;
    private final LibraryClient client;
    public LoanService(LoanRepository loans,LibraryClient client) { this.loans=loans; this.client=client; }
    public Loan borrow(Long readerId,Long copyId) {
        client.validateReader(readerId);
        client.borrowCopy(copyId);
        return loans.save(new Loan(readerId,copyId));
    }
    public Loan returnBook(Long id) {
        Loan loan=find(id);
        if(loan.returnedAt!=null) throw new BusinessException(HttpStatus.CONFLICT,"借阅已归还");
        client.returnCopy(loan.copyId);
        loan.returnedAt=LocalDate.now(); return loans.save(loan);
    }
    @Transactional(readOnly=true)
    public Loan find(Long id) { return loans.findById(id).orElseThrow(() ->
        new BusinessException(HttpStatus.NOT_FOUND,"借阅记录不存在")); }
    @Transactional(readOnly=true)
    public List<Loan> all() { return loans.findAll(); }
}
