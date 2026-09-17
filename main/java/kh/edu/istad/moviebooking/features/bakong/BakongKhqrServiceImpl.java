package kh.edu.istad.moviebooking.features.bakong;

import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.features.bakong.dto.BakongKhqrResult;
import kh.edu.istad.moviebooking.intergration.bakong.BakongProperties;
import kh.gov.nbc.bakong_khqr.BakongKHQR;
import kh.gov.nbc.bakong_khqr.model.IndividualInfo;
import kh.gov.nbc.bakong_khqr.model.KHQRCurrency;
import kh.gov.nbc.bakong_khqr.model.KHQRData;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class BakongKhqrServiceImpl implements BakongKhqrService {

    private final BakongProperties properties;

    @Override
    public BakongKhqrResult generateKhqr(BigDecimal amount, LocalDateTime expiresAt, String billNumber) {

        IndividualInfo info = new IndividualInfo();

        info.setBakongAccountId(properties.getAccountId());

        // SDK calls this MerchantName,
        // but for us it is the individual account holder name
        info.setMerchantName(properties.getAccountName());

        if ("USD".equalsIgnoreCase(properties.getCurrency())) {
            info.setCurrency(KHQRCurrency.USD);
        } else {
            info.setCurrency(KHQRCurrency.KHR);
        }

        info.setAmount(amount.doubleValue());

        info.setBillNumber(billNumber);

        info.setPurposeOfTransaction("Cinema booking");

        long expirationTimestamp = expiresAt.atZone(ZoneId.of("Asia/Phnom_Penh"))
                        .toInstant()
                        .toEpochMilli();

        info.setExpirationTimestamp(expirationTimestamp);

        KHQRResponse<KHQRData> response = BakongKHQR.generateIndividual(info);

        if (response.getKHQRStatus().getCode() != 0) {
            throw new BadRequestException("Failed to generate KHQR");
        }

        return new BakongKhqrResult(
                response.getData().getQr(),
                response.getData().getMd5()
        );
    }
}
