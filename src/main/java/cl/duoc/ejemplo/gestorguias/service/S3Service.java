package cl.duoc.ejemplo.gestorguias.service;

import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
public class S3Service {

    @Autowired
    private S3Template s3Template;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public void subirArchivo(String carpeta, String nombreArchivo, byte[] contenido) {
        String key = carpeta + "/" + nombreArchivo;
        InputStream inputStream = new ByteArrayInputStream(contenido);
        s3Template.upload(bucketName, key, inputStream);
    }

    public void eliminarArchivo(String carpeta, String nombreArchivo) {
        String key = carpeta + "/" + nombreArchivo;
        s3Template.deleteObject(bucketName, key);
    }

    public boolean archivoExiste(String carpeta, String nombreArchivo) {
        String key = carpeta + "/" + nombreArchivo;
        return s3Template.objectExists(bucketName, key);
    }
}