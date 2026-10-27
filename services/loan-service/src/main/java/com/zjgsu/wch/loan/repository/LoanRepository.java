package com.zjgsu.wch.loan.repository;
import com.zjgsu.wch.loan.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LoanRepository extends JpaRepository<Loan,Long> {}
