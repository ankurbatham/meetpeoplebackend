# Lombok Troubleshooting Guide

## Common Lombok Issues and Solutions

### 1. IDE Not Recognizing Lombok Annotations

**Problem**: IDE shows red errors for Lombok annotations like `@Data`, `@Getter`, `@Setter`

**Solutions**:
- Install Lombok plugin for your IDE:
  - **IntelliJ IDEA**: Install "Lombok" plugin from marketplace
  - **Eclipse**: Run `lombok.jar` as a Java application to install
  - **VS Code**: Install "Lombok Annotations Support" extension

- Enable annotation processing:
  - **IntelliJ IDEA**: Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing
  - **Eclipse**: Project Properties → Java Compiler → Annotation Processing → Enable annotation processing

### 2. Compilation Errors

**Problem**: Maven/IDE compilation fails with Lombok-related errors

**Solutions**:
- Clean and rebuild the project:
  ```bash
  mvn clean compile
  ```

- Check Lombok version compatibility:
  - Ensure Lombok version is compatible with your Java version
  - Current project uses Lombok 1.18.38 with Java 17

- Verify pom.xml configuration:
  ```xml
  <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
  </dependency>
  ```

### 3. Missing Generated Methods

**Problem**: Generated getters, setters, toString() methods are not available

**Solutions**:
- Ensure proper annotation usage:
  ```java
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public class MyClass {
      private String field;
  }
  ```

- Check for conflicting annotations or imports

### 4. JPA Entity Issues

**Problem**: JPA entities with Lombok annotations causing issues

**Solutions**:
- Use `@ToString(exclude = "field")` to exclude fields from toString()
- Use `@EqualsAndHashCode(exclude = "field")` to exclude fields from equals/hashCode
- For bidirectional relationships, use `@ToString.Exclude` and `@EqualsAndHashCode.Exclude`

### 5. Serialization Issues

**Problem**: JSON serialization/deserialization issues with Lombok

**Solutions**:
- Add `@JsonIgnoreProperties(ignoreUnknown = true)` for Jackson
- Use `@JsonProperty` for custom field names
- Ensure proper constructor annotations

### 6. Build Issues

**Problem**: Build fails in CI/CD or production

**Solutions**:
- Ensure Lombok is properly excluded from final JAR:
  ```xml
  <plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
      <configuration>
          <excludes>
              <exclude>
                  <groupId>org.projectlombok</groupId>
                  <artifactId>lombok</artifactId>
              </exclude>
          </excludes>
      </configuration>
  </plugin>
  ```

### 7. Alternative Solutions

If Lombok continues to cause issues, you can:

1. **Remove Lombok and use manual getters/setters**
2. **Use IDE code generation features**
3. **Use other annotation processors like MapStruct**

### 8. Verification Steps

To verify Lombok is working:

1. Run compilation:
   ```bash
   mvn clean compile
   ```

2. Check generated classes in target directory

3. Run tests:
   ```bash
   mvn test
   ```

4. Verify IDE shows no red errors for Lombok annotations

### 9. Current Project Status

The current project has been tested and compiles successfully with Lombok. All entities and DTOs use Lombok annotations correctly.

If you're experiencing specific issues, please provide:
- Error messages
- IDE being used
- Specific files causing problems
- Build output 