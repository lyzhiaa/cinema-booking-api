package kh.edu.istad.moviebooking.features.qrcode;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class QrCodeServiceImpl implements QrCodeService {
    @Override
    public byte[] generateQrCode(
            String content,
            int width,
            int height
    ) {

        try {

            BitMatrix bitMatrix = new MultiFormatWriter()
                    .encode(content, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return outputStream.toByteArray();

        } catch (Exception exception) {

            throw new RuntimeException("Failed to generate QR code", exception);
        }
    }
}
