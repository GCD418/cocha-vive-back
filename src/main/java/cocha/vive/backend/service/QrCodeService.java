package cocha.vive.backend.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
public class QrCodeService {

    public String generatePngBase64(String contents, int width, int height) {
        if (contents == null || contents.isBlank()) {
            throw new IllegalArgumentException("contents must not be null/blank");
        }

        try {
            Map<EncodeHintType, Object> hints = Map.of(
                EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name(),
                EncodeHintType.MARGIN, 1
            );

            BitMatrix bitMatrix = new QRCodeWriter()
                .encode(contents, BarcodeFormat.QR_CODE, width, height, hints);

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out);
                return Base64.getEncoder().encodeToString(out.toByteArray());
            }
        } catch (WriterException e) {
            log.error("Failed to generate QR code", e);
            throw new IllegalStateException("Failed to generate QR code", e);
        } catch (IOException e) {
            log.error("Failed to serialize QR code as PNG", e);
            throw new IllegalStateException("Failed to serialize QR code", e);
        }
    }
}
