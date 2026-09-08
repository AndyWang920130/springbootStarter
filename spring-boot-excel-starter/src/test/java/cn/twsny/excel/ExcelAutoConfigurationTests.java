package cn.twsny.excel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import com.alibaba.excel.annotation.ExcelProperty;

import cn.twsny.excel.autoconfigure.ExcelAutoConfiguration;
import cn.twsny.excel.core.ExcelTemplate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelAutoConfigurationTests {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ExcelAutoConfiguration.class));

    @Test
    void templateBeanIsCreatedByDefault() {
        runner.run(context -> assertThat(context).hasSingleBean(ExcelTemplate.class));
    }

    @Test
    void templateBeanIsDisabledWhenPropertyFalse() {
        runner.withPropertyValues("excel.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(ExcelTemplate.class));
    }

    @Test
    void writeThenReadRoundTrips() {
        runner.run(context -> {
            ExcelTemplate template = context.getBean(ExcelTemplate.class);

            List<Person> input = List.of(new Person("Alice", 30), new Person("Bob", 25));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            template.write(out, input, Person.class);

            List<Person> output = template.read(new ByteArrayInputStream(out.toByteArray()), Person.class);

            assertThat(output).hasSize(2);
            assertThat(output).extracting(Person::getName).containsExactly("Alice", "Bob");
            assertThat(output).extracting(Person::getAge).containsExactly(30, 25);
        });
    }

    public static class Person {

        @ExcelProperty("Name")
        private String name;

        @ExcelProperty("Age")
        private Integer age;

        public Person() {
        }

        public Person(String name, Integer age) {
            this.name = name;
            this.age = age;
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
    }
}
