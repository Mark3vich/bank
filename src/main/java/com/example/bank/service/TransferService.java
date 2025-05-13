package com.example.bank.service;

import com.example.bank.dto.request.TransferRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface TransferService {
    void transferMoney(TransferRequest request, HttpServletRequest httpRequest);
}
