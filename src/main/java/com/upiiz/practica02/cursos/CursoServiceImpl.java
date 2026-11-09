package com.upiiz.practica02.cursos;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.upiiz.practica02.common.ConflictoException;
import com.upiiz.practica02.common.RecursoNoEncontradoException;
import com.upiiz.practica02.common.SolicitudInvalidaException;
import com.upiiz.practica02.cursos.dto.CursoDto;
import com.upiiz.practica02.cursos.dto.NuevoCursoDto;
import com.upiiz.practica02.cursos.entities.CursoEntity;

@Service
public class CursoServiceImpl implements CursoService {
    private final CursoRepository cursoRepository;

    // Constructor para la inyección de dependencias
    public CursoServiceImpl(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public CursoDto save(NuevoCursoDto nuevoCursoDto) {
        cursoRepository.findByNombre(nuevoCursoDto.nombre())
                .ifPresent(c -> {
                    throw new ConflictoException("Curso ya registrado");
                });
        return toDomain(cursoRepository.save(toEntity(nuevoCursoDto)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'CONSULTA')")
    public List<CursoDto> findAll() {
        return cursoRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'CONSULTA')")
    public CursoDto findById(Long id) {
        validarId(id);
        return toDomain(buscar(id));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'CONSULTA')")
    public List<CursoDto> findByNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new SolicitudInvalidaException("El parámetro nombre no puede estar vacío");
        }
        return cursoRepository.findByNombreContainingIgnoreCase(nombre.trim())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public CursoDto update(Long id, NuevoCursoDto updateCursoDto) {
        validarId(id);
        CursoEntity curso = buscar(id);

        cursoRepository.findByNombre(updateCursoDto.nombre())
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new ConflictoException("Ya existe otro curso con ese nombre");
                });

        curso.setNombre(updateCursoDto.nombre());
        curso.setDuracionHoras(updateCursoDto.duracionHoras());
        curso.setNivel(updateCursoDto.nivel());
        curso.setPrecio(updateCursoDto.precio());
        curso.setFechaInicio(updateCursoDto.fechaInicio());

        return toDomain(cursoRepository.save(curso));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        validarId(id);
        cursoRepository.delete(buscar(id));
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new SolicitudInvalidaException("El id debe ser un número positivo");
        }
    }

    private CursoEntity buscar(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado"));
    }

    private CursoEntity toEntity(NuevoCursoDto dto) {
        return new CursoEntity(dto.nombre(), dto.duracionHoras(), dto.nivel(), dto.precio(), dto.fechaInicio());
    }

    private CursoDto toDomain(CursoEntity entity) {
        return new CursoDto(entity.getId(), entity.getNombre(), entity.getDuracionHoras(), entity.getNivel(),
                entity.getPrecio(), entity.getFechaInicio());
    }
}
