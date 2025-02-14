package qrcodeapi.rest;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@RestController

public class QRCodeRestController {


    @GetMapping(path ="/api/health")
    public ResponseEntity<String> getHealth(){
        return ResponseEntity.ok("QR Code");
    }

    @GetMapping(path = "/api/qrcode")
    public ResponseEntity<?> getImage(String contents,@RequestParam(name="correction",defaultValue="L")String correction,@RequestParam(name="size",defaultValue="250")int size, @RequestParam (name="type", defaultValue="png")String type) throws WriterException {

        /// check content first
        if(contents.isEmpty() || contents.isBlank())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Contents cannot be null or blank"));

        if(size < 150 || size > 350){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Image size must be between 150 and 350 pixels"));
        }

        /// Valide error correction level
        ErrorCorrectionLevel correctionType;
        if(correction.equalsIgnoreCase("H")){
            correctionType = ErrorCorrectionLevel.H;
        }else if(correction.equalsIgnoreCase("M")){
            correctionType = ErrorCorrectionLevel.M;
        }else if(correction.equalsIgnoreCase("L")){
            correctionType = ErrorCorrectionLevel.L;
        }else if(correction.equalsIgnoreCase("Q")){
            correctionType = ErrorCorrectionLevel.Q;
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Permitted error correction levels are L, M, Q, H"));
        }
        /// validating  the size of the QR code

        String formatName;
        MediaType mediaType;


        /// checking if the format is png,jpeg or gif
        if (type.equalsIgnoreCase("png")) {
            formatName = "png";
            mediaType = MediaType.IMAGE_PNG;
        } else if (type.equalsIgnoreCase("jpg") || type.equalsIgnoreCase("jpeg")) {
            formatName = "jpg";
            mediaType = MediaType.IMAGE_JPEG;
        } else if (type.equalsIgnoreCase("gif")) {
            formatName = "gif";
            mediaType = MediaType.IMAGE_GIF;
        } else {
            // Should never reach here because you already checked for invalid types above,
            // but just in case:
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Only png, jpeg and gif image types are supported"));
        }

        /// Generating the image using the ZXling library

        //If all validation passed,generate the QR code
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        int width = size;
        int height = size;
        Map<EncodeHintType,?> hints = Map.of(EncodeHintType.ERROR_CORRECTION, correctionType);
        BitMatrix bitMatrix = qrCodeWriter.encode(contents, BarcodeFormat.QR_CODE, width,height, hints);

        /// Create a 250x250 image in memory
        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
       /// Draw a white background
//        Graphics2D  graphics = image.createGraphics();
//        graphics.setColor(Color.WHITE);
//        graphics.fillRect(0, 0, size, size);
//
//        graphics.dispose();

        /// 3. Convert BufferedImage to byte[]
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            ImageIO.write(image,formatName,baos);// png,jpeg,git is the format
            byte[] bytes = baos.toByteArray();
            return ResponseEntity
                    .ok()
                    .contentType(mediaType)
                    .body(bytes);

        }catch(IOException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }


    }

}
