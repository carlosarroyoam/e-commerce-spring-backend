package com.carlosarroyoam.ecommerce;

import com.carlosarroyoam.ecommerce.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test de humo de la infraestructura de integracion: verifica que el contenedor Testcontainers
 * arranca, que {@code schema.sql} se aplica sin error contra {@code ddl-auto=validate}, y que el
 * contexto completo de Spring carga.
 */
@SpringBootTest
class ECommerceApplicationTest extends AbstractIntegrationTest {
  @Test
  void contextLoads() {}
}
