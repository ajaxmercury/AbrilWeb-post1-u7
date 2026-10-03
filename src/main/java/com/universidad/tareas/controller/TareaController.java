package com.universidad.tareas.controller;

import com.universidad.tareas.exception.TareaNoEncontradaException;
import com.universidad.tareas.model.Prioridad;
import com.universidad.tareas.model.Tarea;
import com.universidad.tareas.service.TareaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tareas")
public class TareaController {

    private final TareaService tareaService;

    // A partir de Spring 4.3, si una clase solo tiene un constructor,
    // Spring inyectará automáticamente las dependencias, por lo que la
    // anotación @Autowired se vuelve redundante y se puede omitir.
    public TareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    @GetMapping
    public String listarTareas(
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) Boolean completada,
            Model model) {
        model.addAttribute("tareas", tareaService.filtrar(prioridad, completada));
        model.addAttribute("prioridad", prioridad);
        model.addAttribute("completada", completada);
        return "tareas/lista";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNuevaTarea(Model model) {
        model.addAttribute("tarea", new Tarea());
        return "tareas/formulario";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditarTarea(@PathVariable Long id, Model model) {
        Tarea tarea = tareaService.buscarPorId(id)
                .orElseThrow(() -> new TareaNoEncontradaException("Tarea no encontrada con ID: " + id));
        model.addAttribute("tarea", tarea);
        return "tareas/formulario";
    }

    @PostMapping("/guardar")
    public String guardarTarea(@Valid @ModelAttribute("tarea") Tarea tarea, BindingResult result) {
        if (result.hasErrors()) {
            // Se devuelve la vista original para mostrar los mensajes de error
            return "tareas/formulario";
        }
        tareaService.guardar(tarea);
        return "redirect:/tareas"; // Patrón PRG (Post/Redirect/Get)
    }

    // Usamos POST para completar y eliminar en lugar de GET porque los métodos GET 
    // deben ser idempotentes y seguros (no deben modificar el estado del servidor). 
    // Los cambios de estado (actualización o borrado) siempre deben realizarse con POST (o PUT/DELETE/PATCH en REST).
    @PostMapping("/{id}/completar")
    public String completarTarea(@PathVariable Long id) {
        tareaService.marcarCompletada(id)
                .orElseThrow(() -> new TareaNoEncontradaException("Tarea no encontrada con ID: " + id));
        return "redirect:/tareas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarTarea(@PathVariable Long id) {
        if (!tareaService.eliminar(id)) {
            throw new TareaNoEncontradaException("Tarea no encontrada con ID: " + id);
        }
        return "redirect:/tareas";
    }
}
