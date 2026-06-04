package cl.duoc.ejemplo.gestorguias.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
public class S3Service {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public String subirArchivo(String carpeta, String nombreArchivo, byte[] contenido) {
        String key = carpeta + "/" + nombreArchivo;
        InputStream inputStream = new ByteArrayInputStream(contenido);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contenido.length);
        amazonS3.putObject(new PutObjectRequest(bucketName, key, inputStream, metadata));
        return key;
    }

    public byte[] descargarArchivo(String carpeta, String nombreArchivo) {
        String key = carpeta + "/" + nombreArchivo;
        S3Object s3Object = amazonS3.getObject(bucketName, key);
        try (InputStream inputStream = s3Object.getObjectContent()) {
            return inputStream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Error al descargar archivo", e);
        }
    }

    public void eliminarArchivo(String carpeta, String nombreArchivo) {
        String key = carpeta + "/" + nombreArchivo;
        amazonS3.deleteObject(bucketName, key);
    }

    public boolean archivoExiste(String carpeta, String nombreArchivo) {
        String key = carpeta + "/" + nombreArchivo;
        return amazonS3.doesObjectExist(bucketName, key);
    }
}