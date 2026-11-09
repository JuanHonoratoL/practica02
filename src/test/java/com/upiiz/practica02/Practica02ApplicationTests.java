package com.upiiz.practica02;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.upiiz.practica02.auth.repository.Token;
import com.upiiz.practica02.auth.repository.TokenRepository;
import com.upiiz.practica02.auth.service.JwtService;
import com.upiiz.practica02.cursos.CursoRepository;
import com.upiiz.practica02.cursos.entities.CursoEntity;
import com.upiiz.practica02.usuario.Role;
import com.upiiz.practica02.usuario.User;
import com.upiiz.practica02.usuario.UserRepository;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration"
})
@AutoConfigureMockMvc
class Practica02ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TokenRepository tokenRepository;

    @MockitoBean
    private CursoRepository cursoRepository;

    private User adminUser;
    private User consultaUser;
    private String adminToken;
    private String consultaToken;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .name("Administrador IPN")
                .email("admin@ipn.mx")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build();

        consultaUser = User.builder()
                .id(2L)
                .name("Usuario Consulta")
                .email("consulta@ipn.mx")
                .password(passwordEncoder.encode("consulta123"))
                .role(Role.CONSULTA)
                .build();

        adminToken = jwtService.generateToken(adminUser);
        consultaToken = jwtService.generateToken(consultaUser);

        when(userRepository.findByEmail("admin@ipn.mx")).thenReturn(Optional.of(adminUser));
        when(userRepository.findByEmail("consulta@ipn.mx")).thenReturn(Optional.of(consultaUser));

        when(tokenRepository.findByToken(adminToken)).thenReturn(Optional.of(
                Token.builder().id(1L).token(adminToken).user(adminUser).expired(false).revoked(false).build()));
        when(tokenRepository.findByToken(consultaToken)).thenReturn(Optional.of(
                Token.builder().id(2L).token(consultaToken).user(consultaUser).expired(false).revoked(false).build()));
    }

    @Test
    @DisplayName("Prueba 1: Iniciar sesión con credenciales correctas devuelve JWT válido")
    void loginConCredencialesCorrectas() throws Exception {
        String body = """
                {
                  "email": "admin@ipn.mx",
                  "password": "admin123"
                }
                """;
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty());
    }

    @Test
    @DisplayName("Prueba 2: Iniciar sesión con contraseña incorrecta rechaza autenticación (401)")
    void loginConContrasenaIncorrecta() throws Exception {
        String body = """
                {
                  "email": "admin@ipn.mx",
                  "password": "wrongpassword"
                }
                """;
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.estado").value(401));
    }

    @Test
    @DisplayName("Prueba 3: Consultar un recurso sin token devuelve 401 Unauthorized")
    void consultarRecursoSinToken() throws Exception {
        mockMvc.perform(get("/api/v1/cursos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.estado").value(401));
    }

    @Test
    @DisplayName("Prueba 4 y 9: Consultar colección con token válido y verificar navegación HATEOAS")
    void consultarColeccionConTokenValido() throws Exception {
        CursoEntity curso = new CursoEntity("Spring Security y JWT", 40, "avanzado",
                new BigDecimal("1500.00"), LocalDate.of(2026, 11, 15));
        curso.setId(10L);
        when(cursoRepository.findAll()).thenReturn(List.of(curso));

        mockMvc.perform(get("/api/v1/cursos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + consultaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(200))
                .andExpect(jsonPath("$.datos.links[0].href").exists())
                .andExpect(jsonPath("$.datos.content[0].links[0].href").exists());
    }

    @Test
    @DisplayName("Prueba 5: Enviar un token manipulado o inválido devuelve 401 Unauthorized")
    void enviarTokenManipulado() throws Exception {
        mockMvc.perform(get("/api/v1/cursos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token.manipulado.invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.estado").value(401));
    }

    @Test
    @DisplayName("Prueba 6: Registrar un recurso con rol ADMIN devuelve 201 Created")
    void registrarRecursoConRolAdmin() throws Exception {
        CursoEntity guardado = new CursoEntity("Microservicios con Spring", 50, "avanzado",
                new BigDecimal("1999.90"), LocalDate.of(2026, 12, 1));
        guardado.setId(1L);

        when(cursoRepository.findByNombre("Microservicios con Spring")).thenReturn(Optional.empty());
        when(cursoRepository.save(any(CursoEntity.class))).thenReturn(guardado);

        String body = """
                {
                  "nombre": "Microservicios con Spring",
                  "duracionHoras": 50,
                  "nivel": "avanzado",
                  "precio": 1999.90,
                  "fechaInicio": "2026-12-01"
                }
                """;

        mockMvc.perform(post("/api/v1/cursos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value(201))
                .andExpect(jsonPath("$.datos.nombre").value("Microservicios con Spring"))
                .andExpect(jsonPath("$.datos.links[0].href").exists());
    }

    @Test
    @DisplayName("Prueba 7: Registrar un recurso con rol CONSULTA devuelve 403 Forbidden")
    void registrarRecursoConRolConsulta() throws Exception {
        String body = """
                {
                  "nombre": "Curso No Autorizado",
                  "duracionHoras": 30,
                  "nivel": "básico",
                  "precio": 999.00,
                  "fechaInicio": "2026-12-01"
                }
                """;

        mockMvc.perform(post("/api/v1/cursos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + consultaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.estado").value(403));
    }

    @Test
    @DisplayName("Prueba 8: Consultar un recurso individual incluye enlaces HATEOAS")
    void consultarRecursoIndividualConHateoas() throws Exception {
        CursoEntity curso = new CursoEntity("Java con Spring Boot", 40, "medio",
                new BigDecimal("1499.90"), LocalDate.of(2026, 11, 2));
        curso.setId(10L);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(curso));

        mockMvc.perform(get("/api/v1/cursos/10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + consultaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(200))
                .andExpect(jsonPath("$.datos.id").value(10))
                .andExpect(jsonPath("$.datos.links[?(@.rel=='self')].href").exists())
                .andExpect(jsonPath("$.datos.links[?(@.rel=='update')].href").exists())
                .andExpect(jsonPath("$.datos.links[?(@.rel=='delete')].href").exists())
                .andExpect(jsonPath("$.datos.links[?(@.rel=='cursos')].href").exists())
                .andExpect(jsonPath("$.datos.links[?(@.rel=='buscar')].href").exists());
    }

    @Test
    @DisplayName("Prueba 10: Actualizar y eliminar un recurso aplica permisos (ADMIN permitido, CONSULTA denegado)")
    void actualizarYEliminarConPermisos() throws Exception {
        CursoEntity curso = new CursoEntity("Java Básico", 20, "básico",
                new BigDecimal("800.00"), LocalDate.of(2026, 11, 2));
        curso.setId(5L);
        when(cursoRepository.findById(5L)).thenReturn(Optional.of(curso));
        when(cursoRepository.save(any(CursoEntity.class))).thenReturn(curso);

        String body = """
                {
                  "nombre": "Java Básico Actualizado",
                  "duracionHoras": 25,
                  "nivel": "básico",
                  "precio": 850.00,
                  "fechaInicio": "2026-11-02"
                }
                """;

        // Rol CONSULTA debe recibir 403 al intentar actualizar o eliminar
        mockMvc.perform(put("/api/v1/cursos/5")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + consultaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/cursos/5")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + consultaToken))
                .andExpect(status().isForbidden());

        // Rol ADMIN debe recibir 200 OK al actualizar y eliminar
        mockMvc.perform(put("/api/v1/cursos/5")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(200));

        mockMvc.perform(delete("/api/v1/cursos/5")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(200));
    }

    @Test
    @DisplayName("Prueba 11: Consultar un identificador inexistente devuelve 404 Not Found")
    void consultarIdentificadorInexistente() throws Exception {
        when(cursoRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/cursos/999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + consultaToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404));
    }

    @Test
    @DisplayName("Prueba 12: Enviar datos inválidos al registrar un recurso devuelve 400 Bad Request")
    void enviarDatosInvalidosAlRegistrarRecurso() throws Exception {
        String bodyInvalido = """
                {
                  "nombre": "",
                  "duracionHoras": -10,
                  "nivel": "",
                  "precio": -50
                }
                """;

        mockMvc.perform(post("/api/v1/cursos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400));
    }

    @Test
    @DisplayName("Registro de usuario asigna rol CONSULTA y búsqueda por nombre incluye HATEOAS")
    void registroYBusquedaPorNombre() throws Exception {
        User nuevoUsuario = User.builder()
                .id(3L)
                .name("Nuevo Estudiante")
                .email("nuevo@ipn.mx")
                .password(passwordEncoder.encode("secret123"))
                .role(Role.CONSULTA)
                .build();

        when(userRepository.findByEmail("nuevo@ipn.mx")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(nuevoUsuario);

        String registroBody = """
                {
                  "email": "nuevo@ipn.mx",
                  "password": "secret123",
                  "name": "Nuevo Estudiante"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.access_token").isNotEmpty());

        CursoEntity curso = new CursoEntity("Spring Boot Avanzado", 40, "avanzado",
                new BigDecimal("1499.90"), LocalDate.of(2026, 11, 2));
        curso.setId(7L);
        when(cursoRepository.findByNombreContainingIgnoreCase("Spring")).thenReturn(List.of(curso));

        mockMvc.perform(get("/api/v1/cursos/buscar")
                        .param("nombre", "Spring")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + consultaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(200))
                .andExpect(jsonPath("$.datos.content[0].nombre").value("Spring Boot Avanzado"))
                .andExpect(jsonPath("$.datos.links[?(@.rel=='self')].href").exists())
                .andExpect(jsonPath("$.datos.links[?(@.rel=='cursos')].href").exists());
    }

    public static void main(String[] args) throws Exception {
        org.springframework.test.context.TestContextManager tcm =
                new org.springframework.test.context.TestContextManager(Practica02ApplicationTests.class);
        Practica02ApplicationTests instance = new Practica02ApplicationTests();
        tcm.prepareTestInstance(instance);

        for (java.lang.reflect.Method m : Practica02ApplicationTests.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Test.class)) {
                instance.setUp();
                m.invoke(instance);
                System.out.println("[PASS] " + m.getName());
            }
        }
        System.out.println("ALL TESTS PASSED!");
    }
}
