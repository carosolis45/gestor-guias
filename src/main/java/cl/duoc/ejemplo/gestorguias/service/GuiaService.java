package cl.duoc.ejemplo.gestorguias.service;

import cl.duoc.ejemplo.gestorguias.entity.GuiaDespacho;
import cl.duoc.ejemplo.gestorguias.repository.GuiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class GuiaService {

    @Autowired
    private GuiaRepository guiaRepository;

    @Autowired
    private S3Service s3Service;

    @Value("${efs.mount.path}")
    private String efsPath;

    public GuiaDespacho crearGuia(GuiaDespacho guia) {
        guia.setNumeroGuia(generarNumeroGuia());
        guia.setFechaCreacion(LocalDateTime.now());
        guia.setEstado("PENDIENTE");
        
        GuiaDespacho saved = guiaRepository.save(guia);
        generarArchivoEFS(saved);
        return saved;
    }

    private String generarNumeroGuia() {
        return "GIA-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6);
    }

    private void generarArchivoEFS(GuiaDespacho guia) {
        String folderPath = efsPath + "/temporal/guias_pendientes/";
        String filePath = folderPath + guia.getNumeroGuia() + ".txt";
        
        try {
            Files.createDirectories(Paths.get(folderPath));
            String contenido = generarContenidoGuia(guia);
            Files.write(Paths.get(filePath), contenido.getBytes());
            guia.setRutaArchivo(filePath);
            guiaRepository.save(guia);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generarContenidoGuia(GuiaDespacho guia) {
        return "=== GUÍA DE DESPACHO ===\n" +
               "Número: " + guia.getNumeroGuia() + "\n" +
               "Transportista: " + guia.getTransportista() + "\n" +
               "Destinatario: " + guia.getDestinatario() + "\n" +
               "Origen: " + guia.getOrigen() + "\n" +
               "Destino: " + guia.getDestino() + "\n" +
               "Peso: " + guia.getPeso() + " kg\n" +
               "Valor Declarado: $" + guia.getValorDeclarado() + "\n" +
               "Fecha: " + guia.getFechaCreacion() + "\n" +
               "========================\n";
    }

    public String subirAS3(Long id) {
        GuiaDespacho guia = guiaRepository.findById(id).orElse(null);
        if (guia == null) return "Guía no encontrada";
        
        String fecha = guia.getFechaCreacion().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        
        try {
            byte[] contenido = Files.readAllBytes(Paths.get(guia.getRutaArchivo()));
            s3Service.subirArchivo(fecha + "/" + guia.getTransportista(), guia.getNumeroGuia() + ".txt", contenido);
            guia.setEstado("SUBIDA");
            guiaRepository.save(guia);
            return "Guía subida a S3: " + fecha + "/" + guia.getTransportista() + "/" + guia.getNumeroGuia() + ".txt";
        } catch (IOException e) {
            return "Error al subir: " + e.getMessage();
        }
    }

    public List<GuiaDespacho> consultarPorTransportista(String transportista) {
        return guiaRepository.findByTransportista(transportista);
    }

    public List<GuiaDespacho> consultarPorTransportistaYFecha(String transportista, LocalDateTime inicio, LocalDateTime fin) {
        return guiaRepository.findByTransportistaAndFechaCreacionBetween(transportista, inicio, fin);
    }

    public GuiaDespacho buscarPorId(Long id) {
        return guiaRepository.findById(id).orElse(null);
    }

    public List<GuiaDespacho> obtenerTodas() {
        return guiaRepository.findAll();
    }
}