package com.universidad.tareas.service;

import com.universidad.tareas.model.Prioridad;
import com.universidad.tareas.model.Tarea;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class TareaService {

    // Se utiliza ConcurrentSkipListMap y AtomicLong para garantizar la seguridad con hilos (Thread-safe)
    // Esto es fundamental dado que este servicio es un Singleton compartido tanto por
    // el TareaController (vistas Thymeleaf) como por el TareaApiController (API REST).
    // Las peticiones HTTP simultáneas podrían modificar la colección al mismo tiempo.
    private final Map<Long, Tarea> tareas = new ConcurrentSkipListMap<>();
    private final AtomicLong contadorId = new AtomicLong(1);

    public TareaService() {
        // Inicializar con 3 tareas de ejemplo, usando fechas futuras dinámicas
        guardar(new Tarea(null, "Configurar entorno Spring Boot", "Configuración inicial del proyecto con dependencias", Prioridad.ALTA, LocalDate.now().plusDays(1), false));
        guardar(new Tarea(null, "Diseñar el modelo de dominio", "Crear clases Tarea y Prioridad", Prioridad.MEDIA, LocalDate.now().plusDays(3), false));
        guardar(new Tarea(null, "Escribir pruebas unitarias", "Asegurar la calidad del código", Prioridad.BAJA, LocalDate.now().plusDays(7), true));
    }

    public List<Tarea> obtenerTodas() {
        return new ArrayList<>(tareas.values());
    }

    public List<Tarea> filtrar(Prioridad prioridad, Boolean completada) {
        return tareas.values().stream()
                .filter(t -> prioridad == null || t.getPrioridad() == prioridad)
                .filter(t -> completada == null || t.isCompletada() == completada)
                .collect(Collectors.toList());
    }

    public Optional<Tarea> buscarPorId(Long id) {
        return Optional.ofNullable(tareas.get(id));
    }

    public Tarea guardar(Tarea tarea) {
        if (tarea.getId() == null) {
            tarea.setId(contadorId.getAndIncrement());
        }
        tareas.put(tarea.getId(), tarea);
        return tarea;
    }

    public Optional<Tarea> marcarCompletada(Long id) {
        return buscarPorId(id).map(tarea -> {
            tarea.setCompletada(true);
            tareas.put(id, tarea); // No estrictamente necesario si mutamos el objeto, pero buena práctica
            return tarea;
        });
    }

    public boolean eliminar(Long id) {
        return tareas.remove(id) != null;
    }
}
