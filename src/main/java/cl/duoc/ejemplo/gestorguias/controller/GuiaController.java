package cl.duoc.ejemplo.gestorguias.controller;

import cl.duoc.ejemplo.gestorguias.entity.GuiaDespacho;
import cl.duoc.ejemplo.gestorguias.service.GuiaService;
import cl.duoc.ejemplo.gestorguias.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/guias")
public class GuiaController {

    @Autowired
    private GuiaService guiaService;

    @Autowired
    private S3Service s3Service;

    @PostMapping("/crear")
    public ResponseEntity<GuiaDespacho> crearGuia(@RequestBody GuiaDespacho guia) {
        return ResponseEntity.ok(guiaService.crearGuia(guia));
    }

    @PostMapping("/subir/{id}")
    public ResponseEntity<String> subirGuiaS3(@PathVariable Long id) {
        return ResponseEntity.ok(guiaService.subirAS3(id));
    }

    @DeleteMapping("/eliminar")
    public ResponseEntity<String> eliminarGuiaS3(@RequestParam String transportista,
                                                  @RequestParam String numeroGuia,
                                                  @RequestParam String fecha) {
        String carpeta = fecha + "/" + transportista;
        String nombreArchivo = numeroGuia + ".txt";
        
        if (s3Service.archivoExiste(carpeta, nombreArchivo)) {
            s3Service.eliminarArchivo(carpeta, nombreArchivo);
            return ResponseEntity.ok("Guía eliminada de S3");
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/consultar/transportista")
    public ResponseEntity<List<GuiaDespacho>> consultarPorTransportista(@RequestParam String transportista) {
        return ResponseEntity.ok(guiaService.consultarPorTransportista(transportista));
    }

    @GetMapping("/consultar")
    public ResponseEntity<List<GuiaDespacho>> consultarPorTransportistaYFecha(
            @RequestParam String transportista,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(guiaService.consultarPorTransportistaYFecha(transportista, inicio, fin));
    }

    @GetMapping("/todas")
    public ResponseEntity<List<GuiaDespacho>> obtenerTodas() {
        return ResponseEntity.ok(guiaService.obtenerTodas());
    }
}