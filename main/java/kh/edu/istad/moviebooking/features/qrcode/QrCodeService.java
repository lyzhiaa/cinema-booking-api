package kh.edu.istad.moviebooking.features.qrcode;

public interface QrCodeService {
    byte[] generateQrCode(
            String content,
            int width,
            int height
    );
}
