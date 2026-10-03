package com.universidad.tareas.controller;

import com.universidad.tareas.model.Prioridad;
import com.universidad.tareas.model.Tarea;
import com.universidad.tareas.service.TareaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tareas")
public class TareaApiController {

    private final TareaService tareaService;

    // Se inyecta por constructor el MISMO bean (singleton) de TareaService
    // que utiliza el TareaController, gracias a la inversión de control de Spring.
    // Esto asegura que ambas interfaces (vista HTML y API REST) compartan los mismos datos en memoria.
    public TareaApiController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    @GetMapping
    public ResponseEntity<List<Tarea>> obtenerTareas(
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) Boolean completada) {
        return ResponseEntity.ok(tareaService.filtrar(prioridad, completada));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarea> obtenerTareaPorId(@PathVariable Long id) {
        return tareaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Tarea> crearTarea(@Valid @RequestBody Tarea tarea) {
        Tarea nuevaTarea = tareaService.guardar(tarea);
        
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(nuevaTarea.getId())
                .toUri();
                
        return ResponseEntity.created(location).body(nuevaTarea);
    }

    // Decisión de diseño: Se utiliza PUT para reemplazo completo, 
    // donde el cliente envía todos los campos de la entidad.
    // Si la entidad no existe, se devuelve 404 (aunque PUT en algunos casos podría crear, 
    // en nuestra API hemos decidido que PUT solo actualiza si existe).
    @PutMapping("/{id}")
    public ResponseEntity<Tarea> actualizarTarea(@PathVariable Long id, @Valid @RequestBody Tarea tarea) {
        return tareaService.buscarPorId(id).map(t -> {
            tarea.setId(id);
            return ResponseEntity.ok(tareaService.guardar(tarea));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Decisión de diseño: Se utiliza PATCH para actualización parcial,
    // en este caso, una acción específica que modifica solo el estado "completada" 
    // de la tarea sin requerir enviar toda la representación de la entidad.
    @PatchMapping("/{id}/completar")
    public ResponseEntity<Tarea> completarTarea(@PathVariable Long id) {
        return tareaService.marcarCompletada(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTarea(@PathVariable Long id) {
        if (tareaService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
