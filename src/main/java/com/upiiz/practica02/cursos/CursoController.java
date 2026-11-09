package com.upiiz.practica02.cursos;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.upiiz.practica02.common.ApiRespuesta;
import com.upiiz.practica02.cursos.dto.CursoDto;
import com.upiiz.practica02.cursos.dto.NuevoCursoDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/cursos")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cursos", description = "Operaciones CRUD y búsqueda sobre los cursos con enlaces HATEOAS y seguridad JWT")
public class CursoController {
    private final CursoService cursoService;

    CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @Operation(
            summary = "Crear un curso (Requiere rol ADMIN)",
            description = "Registra un nuevo curso. El nombre no puede repetirse.",
            requestBody = @RequestBody(
                    description = "Datos del curso a crear",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = NuevoCursoDto.class))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Recurso creado correctamente", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos o incompletos", content = @Content),
                    @ApiResponse(responseCode = "401", description = "No autenticado (token ausente o inválido)", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Sin permisos (requiere rol ADMIN)", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflicto (duplicado)", content = @Content)
            })
    @PostMapping
    public ResponseEntity<ApiRespuesta<EntityModel<CursoDto>>> guardarCurso(
            @Valid @org.springframework.web.bind.annotation.RequestBody NuevoCursoDto nuevoCursoDto) {
        CursoDto guardado = cursoService.save(nuevoCursoDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiRespuesta.of(201, "Curso creado correctamente", agregarEnlaces(guardado)));
    }

    @Operation(
            summary = "Listar los cursos (Roles ADMIN o CONSULTA)",
            description = "Devuelve todos los cursos registrados, cada uno con sus enlaces HATEOAS.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de cursos", content = @Content),
                    @ApiResponse(responseCode = "401", description = "No autenticado (token ausente o inválido)", content = @Content)
            })
    @GetMapping
    public ResponseEntity<ApiRespuesta<CollectionModel<EntityModel<CursoDto>>>> listarTodos() {
        List<EntityModel<CursoDto>> cursos = cursoService.findAll().stream()
                .map(this::agregarEnlaces)
                .toList();
        CollectionModel<EntityModel<CursoDto>> coleccion = CollectionModel.of(cursos,
                linkTo(methodOn(CursoController.class).listarTodos()).withSelfRel(),
                linkTo(methodOn(CursoController.class).buscarPorNombre("")).withRel("buscar"));
        return ResponseEntity.ok(ApiRespuesta.of(200, "Cursos encontrados", coleccion));
    }

    @Operation(
            summary = "Buscar un curso por id (Roles ADMIN o CONSULTA)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recurso encontrado", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Parámetro inválido", content = @Content),
                    @ApiResponse(responseCode = "401", description = "No autenticado (token ausente o inválido)", content = @Content),
                    @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
            })
    @GetMapping("/{id}")
    public ResponseEntity<ApiRespuesta<EntityModel<CursoDto>>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiRespuesta.of(200, "Curso encontrado", agregarEnlaces(cursoService.findById(id))));
    }

    @Operation(
            summary = "Buscar cursos por nombre (Roles ADMIN o CONSULTA)",
            description = "Busca cursos cuyo nombre coincida parcial o totalmente con el criterio proporcionado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Búsqueda realizada correctamente", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Parámetro de búsqueda inválido", content = @Content),
                    @ApiResponse(responseCode = "401", description = "No autenticado (token ausente o inválido)", content = @Content)
            })
    @GetMapping("/buscar")
    public ResponseEntity<ApiRespuesta<CollectionModel<EntityModel<CursoDto>>>> buscarPorNombre(
            @RequestParam String nombre) {
        List<EntityModel<CursoDto>> cursos = cursoService.findByNombre(nombre).stream()
                .map(this::agregarEnlaces)
                .toList();
        CollectionModel<EntityModel<CursoDto>> coleccion = CollectionModel.of(cursos,
                linkTo(methodOn(CursoController.class).buscarPorNombre(nombre)).withSelfRel(),
                linkTo(methodOn(CursoController.class).listarTodos()).withRel("cursos"));
        return ResponseEntity.ok(ApiRespuesta.of(200, "Cursos encontrados en la búsqueda", coleccion));
    }

    @Operation(
            summary = "Actualizar un curso (Requiere rol ADMIN)",
            description = "Reemplaza los datos del curso indicado.",
            requestBody = @RequestBody(
                    description = "Nuevos datos del curso",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = NuevoCursoDto.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recurso actualizado", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos o incompletos", content = @Content),
                    @ApiResponse(responseCode = "401", description = "No autenticado (token ausente o inválido)", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Sin permisos (requiere rol ADMIN)", content = @Content),
                    @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflicto (duplicado)", content = @Content)
            })
    @PutMapping("/{id}")
    public ResponseEntity<ApiRespuesta<EntityModel<CursoDto>>> actualizarCurso(@PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody NuevoCursoDto nuevoCursoDto) {
        CursoDto actualizado = cursoService.update(id, nuevoCursoDto);
        return ResponseEntity.ok(ApiRespuesta.of(200, "Curso actualizado correctamente", agregarEnlaces(actualizado)));
    }

    @Operation(
            summary = "Eliminar un curso (Requiere rol ADMIN)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Eliminación exitosa", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Parámetro inválido", content = @Content),
                    @ApiResponse(responseCode = "401", description = "No autenticado (token ausente o inválido)", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Sin permisos (requiere rol ADMIN)", content = @Content),
                    @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiRespuesta<Void>> eliminar(@PathVariable Long id) {
        cursoService.delete(id);
        return ResponseEntity.ok(ApiRespuesta.of(200, "Curso eliminado correctamente"));
    }

    // HATEOAS: envuelve al curso junto con los enlaces a sus acciones (self, update, delete, cursos, buscar)
    private EntityModel<CursoDto> agregarEnlaces(CursoDto curso) {
        Long id = curso.id();
        return EntityModel.of(curso,
                linkTo(methodOn(CursoController.class).buscarPorId(id)).withSelfRel(),
                linkTo(methodOn(CursoController.class).actualizarCurso(id, null)).withRel("update"),
                linkTo(methodOn(CursoController.class).eliminar(id)).withRel("delete"),
                linkTo(methodOn(CursoController.class).listarTodos()).withRel("cursos"),
                linkTo(methodOn(CursoController.class).buscarPorNombre(curso.nombre())).withRel("buscar"));
    }
}
