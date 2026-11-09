package com.upiiz.practica02.cursos;

import java.util.List;

import com.upiiz.practica02.cursos.dto.CursoDto;
import com.upiiz.practica02.cursos.dto.NuevoCursoDto;

// Capa de servicio
public interface CursoService {

    CursoDto save(NuevoCursoDto nuevoCursoDto);

    List<CursoDto> findAll();

    CursoDto findById(Long id);

    List<CursoDto> findByNombre(String nombre);

    CursoDto update(Long id, NuevoCursoDto updateCursoDto);

    void delete(Long id);
}
