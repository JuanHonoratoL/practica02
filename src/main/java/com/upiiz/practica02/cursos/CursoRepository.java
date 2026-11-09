package com.upiiz.practica02.cursos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.upiiz.practica02.cursos.entities.CursoEntity;

public interface CursoRepository extends JpaRepository<CursoEntity, Long> {
    Optional<CursoEntity> findByNombre(String nombre);

    List<CursoEntity> findByNombreContainingIgnoreCase(String nombre);
}
