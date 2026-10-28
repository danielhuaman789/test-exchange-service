# API de conversión de monedas

Microservicio que consulta ExchangeRate-API mediante OpenFeign,
convierte montos entre monedas y almacena cada conversión en H2.

## Requisitos

- JDK 17 o superior.
- Acceso a Internet para descargar dependencias y consultar ExchangeRate-API.
- Una API Key de ExchangeRate-API.
- Maven Wrapper incluido; no es necesario instalar Maven.

## Tecnologías

- Java 17 como versión de compilación.
- Spring Boot 4.0.6.
- Spring Cloud OpenFeign 2025.1.1.
- Spring Data JPA.
- H2 en memoria.
- JUnit 5 y Mockito.

## Arquitectura

Se aplican principios de DDD con separación en tres capas:

- Domain: Currency como objeto de valor, Conversion como entidad
  identificada por UUID, excepciones y contratos de persistencia
  y consulta de tasas. No depende de Spring, JPA ni Feign.
- Application: controller REST, DTOs y servicio que coordina
  validación, consulta, cálculo y persistencia.
- Infrastructure: cliente OpenFeign, adaptador del proveedor,
  entidad JPA y adaptador de persistencia.

Las interfaces ConversionRepository y ExchangeRateProvider permiten
que la aplicación dependa de contratos del dominio y que las
implementaciones técnicas puedan sustituirse.

La entidad JPA está separada de la entidad de dominio.

## Ejecución en Windows PowerShell

Desde la carpeta del proyecto:

```powershell
$env:EXCHANGE_RATE_API_KEY="TU_API_KEY"
.\mvnw.cmd spring-boot:run
```

## Ejecución en Linux o macOS

```bash
export EXCHANGE_RATE_API_KEY="TU_API_KEY"
chmod +x mvnw
./mvnw spring-boot:run
```

La aplicación escucha en http://localhost:8080.

La clave se lee desde una variable de entorno y no se incluye
en el repositorio.

## Endpoint

POST /api/conversion

Content-Type: application/json

Request:

```json
{
  "monto": 100,
  "monedaOrigen": "USD",
  "monedaDestino": "PEN"
}
```

Ejemplo de respuesta HTTP 200:

```json
{
  "monto": 100,
  "montoConvertido": 344.86,
  "monedaOrigen": "USD",
  "monedaDestino": "PEN",
  "tipoCambio": 3.4486
}
```

La tasa y el resultado dependen de la respuesta del proveedor.

Ejemplo desde PowerShell:

```powershell
$body = @{
    monto = 100
    monedaOrigen = "USD"
    monedaDestino = "PEN"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/conversion" -Method Post -ContentType "application/json" -Body $body | ConvertTo-Json
```

## Validaciones y cálculo

- Monto obligatorio y mayor que cero.
- Hasta 20 dígitos enteros y 10 decimales en el monto.
- Códigos de moneda obligatorios de tres letras.
- Se normalizan espacios al inicio y al final, y minúsculas.
- Se validan las monedas usando el catálogo del JDK.
- La tasa debe ser positiva y existir en la respuesta del proveedor.
- Se utiliza BigDecimal para los cálculos.
- El resultado se redondea con HALF_UP según los decimales
  de la moneda destino.
- La conversión se guarda antes de devolver una respuesta exitosa.

## Manejo de errores

Las respuestas manejadas utilizan ProblemDetail.

| HTTP | Situación |
|---|---|
| 400 | Monto o moneda inválidos, moneda destino no soportada o JSON ilegible |
| 502 | Fallo al consultar el proveedor o respuesta externa inválida |
| 500 | Error al guardar la conversión |

Ejemplo de error por monto negativo:

```json
{
  "type": "about:blank",
  "title": "Solicitud inválida",
  "status": 400,
  "detail": "El monto debe ser mayor que cero",
  "instance": "/api/conversion"
}
```

## Persistencia y consola H2

Cada operación almacena:

- Identificador UUID.
- Monto original.
- Monedas de origen y destino.
- Tasa aplicada.
- Monto convertido.
- Fecha de operación como Instant.

Con la aplicación ejecutándose, abrir:

http://localhost:8080/h2-console

Datos de conexión:

- Driver: org.h2.Driver
- JDBC URL: jdbc:h2:mem:testdb
- Usuario: sa
- Contraseña: password

Consulta:

```sql
SELECT * FROM conversiones;
```

La base es temporal: los registros se pierden al detener la aplicación.

## Pruebas

Windows:

```powershell
.\mvnw.cmd test
```

Linux o macOS:

```bash
./mvnw test
```

Se incluyen siete pruebas unitarias del servicio y una prueba
de carga del contexto de Spring.

Las pruebas unitarias cubren:

- Conversión y envío de la operación al repositorio.
- Rechazo de monto negativo.
- Rechazo de monto nulo.
- Normalización de códigos de moneda.
- Rechazo de formato de moneda inválido.
- Ausencia de guardado cuando falla el proveedor.
- Redondeo según la moneda destino.

Las pruebas unitarias usan mocks y no necesitan una API Key
ni hacen llamadas reales al proveedor.

También se verificó manualmente una conversión USD a PEN
y su registro en H2.

## Limitaciones

- La disponibilidad de la conversión depende del proveedor externo.
- Los errores HTTP del proveedor se agrupan actualmente como 502.
- Las monedas disponibles dependen tanto del catálogo del JDK
  como del soporte del proveedor.
- No se incluye caché ni historial expuesto mediante API.
- Los límites de precisión de H2 pueden impedir almacenar
  resultados extremos.
  