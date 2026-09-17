package kh.edu.istad.moviebooking.features.bakong;

import kh.edu.istad.moviebooking.features.bakong.dto.BakongKhqrResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface BakongKhqrService {
    BakongKhqrResult generateKhqr(BigDecimal amount, LocalDateTime expiresAt, String billNumber);
}
