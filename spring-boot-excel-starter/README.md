# excel-spring-boot-starter

A Spring Boot **auto-configuration starter** for Excel import/export, built on
[Spring Boot 3.2.5](https://spring.io/projects/spring-boot) and
[Alibaba EasyExcel](https://github.com/alibaba/easyexcel).

Add the dependency, inject `ExcelTemplate`, and read/write Excel with a few lines —
no manual EasyExcel bootstrapping required.

- Java 17+, Spring Boot 3.2.x (Jakarta EE 10 / `jakarta.*`)
- EasyExcel 3.3.4
- Zero-config: an `ExcelTemplate` bean is registered automatically

## Installation

Build and install into your local Maven repository:

```bash
mvn clean install
```

Then depend on it from another Spring Boot project:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>excel-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### 1. Define a model with EasyExcel annotations

```java
public class UserExcel {

    @ExcelProperty("Name")
    private String name;

    @ExcelProperty("Age")
    private Integer age;

    // getters / setters
}
```

### 2. Inject `ExcelTemplate`

```java
@Service
public class UserService {

    private final ExcelTemplate excelTemplate;

    public UserService(ExcelTemplate excelTemplate) {
        this.excelTemplate = excelTemplate;
    }
}
```

### 3. Export

Write to any `OutputStream`:

```java
excelTemplate.write(outputStream, users, UserExcel.class);
excelTemplate.write(outputStream, users, UserExcel.class, "Users");
```

Download directly from a web controller:

```java
@GetMapping("/users/export")
public void export(HttpServletResponse response) {
    List<UserExcel> users = loadUsers();
    excelTemplate.writeToResponse(response, "users", users, UserExcel.class);
    // -> Content-Disposition: attachment; filename*=utf-8''users.xlsx
}
```

### 4. Import

Read everything into a list:

```java
List<UserExcel> users = excelTemplate.read(inputStream, UserExcel.class);
List<UserExcel> sheet2 = excelTemplate.read(inputStream, UserExcel.class, 1); // sheet index
```

Stream large files in batches (bounded memory):

```java
excelTemplate.readInBatch(inputStream, UserExcel.class, batch -> {
    userRepository.saveAll(batch); // called once per `excel.batch-size` rows
});
```

## Configuration

All properties live under the `excel` prefix and are optional:

| Property                   | Default   | Description                                              |
|----------------------------|-----------|----------------------------------------------------------|
| `excel.enabled`            | `true`    | Enable/disable the auto-configuration.                   |
| `excel.default-sheet-name` | `Sheet1`  | Sheet name used when none is supplied on write.          |
| `excel.charset`            | `UTF-8`   | Charset for download headers / CSV.                      |
| `excel.batch-size`         | `1000`    | Rows per batch for `readInBatch`.                        |
| `excel.auto-close-stream`  | `true`    | Whether EasyExcel closes the underlying stream.          |
| `excel.head-row-number`    | `1`       | Number of header rows to skip when reading.              |
| `excel.excel-type`         | `XLSX`    | Default write format: `XLSX`, `XLS`, or `CSV`.           |

Example `application.yml`:

```yaml
excel:
  default-sheet-name: Data
  batch-size: 500
  excel-type: XLSX
```

## Customization

Provide your own `ExcelTemplate` bean to override the default — the starter backs
off automatically (`@ConditionalOnMissingBean`):

```java
@Bean
public ExcelTemplate excelTemplate(ExcelProperties properties) {
    return new MyExcelTemplate(properties);
}
```

## How it works

- `ExcelAutoConfiguration` is registered via
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
- It activates only when EasyExcel is on the classpath (`@ConditionalOnClass`) and
  `excel.enabled` is not `false`.
- `HttpServletResponse` support uses a `provided`/`optional` servlet dependency, so
  the starter also works in non-web applications.
