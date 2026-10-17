package com.cafe;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Builds a UPI payment link and turns it into a QR code image using ZXing. */
public class QRCodeGenerator {

    // ---- CHANGE these two to the cafe's real UPI ID and name ----
    public static final String UPI_ID = "yourcafe@upi";
    public static final String PAYEE_NAME = "Simple Cafe";

    public static boolean isPlaceholderUpiId() {
        return UPI_ID.equals("yourcafe@upi");
    }

    /** Example: upi://pay?pa=yourcafe@upi&pn=Simple%20Cafe&am=130.00&cu=INR&tn=Bill%20CAFE-... */
    public static String buildUpiUri(double amount, String billNumber) {
        String amountText = String.format(Locale.US, "%.2f", amount);
        return "upi://pay?pa=" + UPI_ID
                + "&pn=" + encode(PAYEE_NAME)
                + "&am=" + amountText
                + "&cu=INR"
                + "&tn=" + encode("Bill " + billNumber);
    }

    public static BufferedImage generateQRImage(String text, int size) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);

        BitMatrix matrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size, hints);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }

    private static String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
