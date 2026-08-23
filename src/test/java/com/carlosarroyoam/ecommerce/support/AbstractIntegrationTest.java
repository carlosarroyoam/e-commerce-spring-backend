package com.carlosarroyoam.ecommerce.support;

import com.carlosarroyoam.ecommerce.support.security.JwtTestTokenFactory;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Clase base para tests de integracion (L3/L4): levanta un contenedor MySQL real compartido
 * (patron singleton container) via {@link ServiceConnection}, activa el perfil {@code test} y
 * expone {@link JwtTestTokenFactory} para firmar JWT de prueba reales.
 *
 * <p>El nombre de base de datos coincide con el de {@code schema.sql} ({@code
 * spring-boot-e-commerce}), que el perfil {@code test} aplica automaticamente contra el
 * contenedor via {@code spring.sql.init.schema-locations}, ya que {@code ddl-auto=validate} nunca
 * crea tablas.
 */
@Testcontainers
@ActiveProfiles("test")
@Import(JwtTestTokenFactory.class)
public abstract class AbstractIntegrationTest {
  @Container
  @ServiceConnection
  static final MySQLContainer<?> MYSQL_CONTAINER =
      new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
          .withDatabaseName("spring-boot-e-commerce")
          .withReuse(true);
}
