package com.example.excel;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.excel.annotation.ExcelProperty;

import com.example.excel.autoconfigure.ExcelAutoConfiguration;
import com.example.excel.core.ExcelTemplate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration tests that write real {@code .xlsx} files to the build
 * output directory ({@code target/excel-samples/}) and read them back. The files
 * are left on disk after the run so they can be opened in Excel for manual
 * inspection of formatting and headers.
 */
class ExcelImportExportIntegrationTests {

    /** Directory under target/ where generated sample files are written. */
    private static final Path OUTPUT_DIR = Paths.get("target", "excel-samples");

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ExcelAutoConfiguration.class));

    @Test
    void exportWritesAnInspectableXlsxFile() {
        runner.run(context -> {
            ExcelTemplate template = context.getBean(ExcelTemplate.class);
            Files.createDirectories(OUTPUT_DIR);
            Path file = OUTPUT_DIR.resolve("export-users.xlsx");

            List<UserExcel> users = sampleUsers();
            try (OutputStream out = Files.newOutputStream(file)) {
                template.write(out, users, UserExcel.class, "Users");
            }

            assertThat(Files.exists(file)).isTrue();
            assertThat(Files.size(file)).isPositive();
            System.out.println("[export] wrote " + file.toAbsolutePath());
        });
    }

    @Test
    void exportThenImportRoundTripsFromDisk() {
        runner.run(context -> {
            ExcelTemplate template = context.getBean(ExcelTemplate.class);
            Files.createDirectories(OUTPUT_DIR);
            Path file = OUTPUT_DIR.resolve("roundtrip-users.xlsx");

            List<UserExcel> original = sampleUsers();

            try (OutputStream out = Files.newOutputStream(file)) {
                template.write(out, original, UserExcel.class);
            }

            List<UserExcel> loaded;
            try (InputStream in = Files.newInputStream(file)) {
                loaded = template.read(in, UserExcel.class);
            }

            assertThat(loaded).hasSize(original.size());
            assertThat(loaded).extracting(UserExcel::getName)
                    .containsExactly("Alice", "Bob", "Carol");
            assertThat(loaded).extracting(UserExcel::getEmail)
                    .containsExactly("alice@example.com", "bob@example.com", "carol@example.com");
            System.out.println("[roundtrip] read back " + loaded.size() + " rows from " + file.toAbsolutePath());
        });
    }

    @Test
    void batchImportInvokesConsumerPerBatch() {
        // batch-size defaults to 1000; force a smaller size so batching is observable.
        runner.withPropertyValues("excel.batch-size=2").run(context -> {
            ExcelTemplate template = context.getBean(ExcelTemplate.class);
            Files.createDirectories(OUTPUT_DIR);
            Path file = OUTPUT_DIR.resolve("batch-users.xlsx");

            List<UserExcel> original = manyUsers(5); // 5 rows, batch size 2 -> batches of 2,2,1
            try (OutputStream out = Files.newOutputStream(file)) {
                template.write(out, original, UserExcel.class);
            }

            List<Integer> batchSizes = new ArrayList<>();
            AtomicInteger totalRows = new AtomicInteger();
            try (InputStream in = Files.newInputStream(file)) {
                template.readInBatch(in, UserExcel.class, batch -> {
                    batchSizes.add(batch.size());
                    totalRows.addAndGet(batch.size());
                });
            }

            assertThat(totalRows.get()).isEqualTo(5);
            assertThat(batchSizes).containsExactly(2, 2, 1);
            System.out.println("[batch] consumed batches " + batchSizes + " from " + file.toAbsolutePath());
        });
    }

    private static List<UserExcel> sampleUsers() {
        return List.of(
                new UserExcel("Alice", 30, "alice@example.com"),
                new UserExcel("Bob", 25, "bob@example.com"),
                new UserExcel("Carol", 41, "carol@example.com"));
    }

    private static List<UserExcel> manyUsers(int count) {
        List<UserExcel> list = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            list.add(new UserExcel("User" + i, 20 + i, "user" + i + "@example.com"));
        }
        return list;
    }

    /** Sample import/export model demonstrating EasyExcel column annotations. */
    public static class UserExcel {

        @ExcelProperty("Name")
        private String name;

        @ExcelProperty("Age")
        private Integer age;

        @ExcelProperty("Email")
        private String email;

        public UserExcel() {
        }

        public UserExcel(String name, Integer age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
