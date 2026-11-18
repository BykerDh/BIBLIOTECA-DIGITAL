package com.biblioteca.patterns.behavioral.visitor;

import com.biblioteca.domain.Models.Archivo;
import com.biblioteca.domain.Models.Libro;
import com.biblioteca.patterns.structural.composite.ComponenteCatalogo;
import com.biblioteca.patterns.structural.composite.ComponenteCatalogo.ColeccionNodo;
import com.biblioteca.patterns.structural.composite.ComponenteCatalogo.LibroHoja;
import java.util.*;

public final class InformeCatalogoVisitor implements VisitanteCatalogo {
    private final Map<Long,Libro> fichas;
    private final Map<Long,Set<String>> formatos = new HashMap<>();
    private final Deque<ComponenteCatalogo> pendientes = new ArrayDeque<>();
    private final Set<Long> colecciones = new HashSet<>();
    private final Set<Long> libros = new HashSet<>();
    private final Map<String,Integer> totales = new TreeMap<>(Map.of("PDF",0,"EPUB",0,"MOBI",0));
    private final List<LibroHoja> sinArchivo = new ArrayList<>();
    private long licencias;

    public record Resumen(int colecciones,int libros,long licencias,Map<String,Integer> formatos,
                          List<LibroHoja> sinArchivo,int sinColeccion) {}

    public InformeCatalogoVisitor(Map<Long,Libro> fichas,List<Archivo> archivosDisponibles) {
        this.fichas = Map.copyOf(fichas);
        for (Archivo archivo : archivosDisponibles)
            formatos.computeIfAbsent(archivo.libroId(),id -> new HashSet<>()).add(archivo.formato());
    }
    public Resumen analizar(List<? extends ComponenteCatalogo> raices) {
        pendientes.clear(); colecciones.clear(); libros.clear(); sinArchivo.clear(); licencias=0;
        totales.replaceAll((formato,total) -> 0);
        pendientes.addAll(raices);
        while (!pendientes.isEmpty()) pendientes.removeFirst().aceptar(this);
        int sinColeccion = 0;
        // Incluye libros que aun no pertenecen a una coleccion, sin inventar una coleccion persistida.
        for (Libro ficha : fichas.values()) if (!libros.contains(ficha.id())) {
            sinColeccion++;
            new LibroHoja(ficha.id(),ficha.titulo()).aceptar(this);
        }
        sinArchivo.sort(Comparator.comparing(LibroHoja::nombre).thenComparingLong(LibroHoja::id));
        return new Resumen(colecciones.size(),libros.size(),licencias,
                Collections.unmodifiableMap(new TreeMap<>(totales)),List.copyOf(sinArchivo),sinColeccion);
    }
    @Override public void visitar(ColeccionNodo coleccion) {
        if (colecciones.add(coleccion.id())) pendientes.addAll(coleccion.hijos());
    }
    @Override public void visitar(LibroHoja libro) {
        Libro ficha = fichas.get(libro.id());
        if (ficha==null || !libros.add(libro.id())) return;
        licencias += ficha.licencias();
        Set<String> disponibles = formatos.getOrDefault(libro.id(),Set.of());
        if (disponibles.isEmpty()) sinArchivo.add(libro);
        for (String formato : disponibles) totales.computeIfPresent(formato,(clave,total) -> total+1);
    }
}
