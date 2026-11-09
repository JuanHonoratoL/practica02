package com.upiiz.practica02.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Cursos (HATEOAS + JWT)",
                description = "API REST nivel 3 de Richardson (HATEOAS) para administrar cursos, protegida mediante Spring Security y JSON Web Token (JWT) con roles ADMIN y CONSULTA.",
                termsOfService = "https://www.zacatecas.ipn.mx/",
                contact = @Contact(
                        name = "Juan Pablo Honorato López",
                        email = "jhonoratol2000@alumno.ipn.mx",
                        url = "https://www.zacatecas.ipn.mx/",
                        extensions = {
                                @Extension(properties = {
                                        @ExtensionProperty(name = "Escuela", value = "UPIIZ")
                                })
                        }
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                ),
                version = "1.0.0"
        ),
        servers = {
                @Server(description = "Servidor URL relativo", url = "/")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Ingrese el token JWT obtenido en /api/v1/auth/login o /api/v1/auth/registro",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {
}
