# AGENTS.md

This file provides essential information for agentic coding agents working in this Spring Boot backend codebase for the PIECE shop-in-shop matching platform.

## Project Overview

**Technology Stack:**
- **Language**: Java 17
- **Framework**: Spring Boot 3.5.9
- **Build Tool**: Gradle 8.5
- **Database**: MySQL 8.0 with JPA/Hibernate + Redis
- **Security**: Spring Security with OAuth2 (Kakao, Google, Naver) + JWT
- **Storage**: AWS S3 via Supabase
- **Query**: QueryDSL for complex queries
- **Real-time**: Server-Sent Events (SSE) for notifications
- **Documentation**: SpringDoc OpenAPI (Swagger)

**Package Structure**: Domain-driven design with clear separation:
```
kr.java.java/
├── global/          # Global configurations, exceptions, utilities
├── domain/          # Domain modules (auth, space, portfolio, matching, etc.)
```

## Build, Test, and Lint Commands

### Essential Commands
```bash
# Build without tests (fast)
./gradlew clean build -x test

# Full build with tests
./gradlew build

# Run application locally
./gradlew bootRun

# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests JavaApplicationTests

# Run tests with specific pattern
./gradlew test --tests "*ServiceTest"

# Clean build cache
./gradlew clean

# Docker commands
docker-compose up --build
docker build -t piece-backend .
```

### Testing Guidelines
- **Framework**: JUnit 5 with Spring Boot Test
- **Current Coverage**: Minimal (only basic context test)
- **Test Location**: `/src/test/java/kr/java/java/`
- **Pattern**: Use `@SpringBootTest` for integration tests, `@ExtendWith(MockitoExtension.class)` for unit tests

## Code Style Guidelines

### Naming Conventions
- **Classes**: PascalCase (`SpaceService`, `GlobalExceptionHandler`)
- **Methods**: camelCase (`createSpace`, `findByUuid`)
- **Variables**: camelCase
- **Constants**: UPPER_SNAKE_CASE (`REFRESH_TOKEN_MAX_AGE`)
- **Packages**: lowercase with dots (`kr.java.java.domain.space`)

### Import Organization
```java
// Standard Java imports first
import java.util.List;
import java.time.LocalDateTime;

// Third-party imports
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Project imports (static imports for QueryDSL Q-classes)
import kr.java.java.domain.space.entity.Space;
import static kr.java.java.domain.space.entity.QSpace.space;
```

### Annotations Usage
- **Lombok**: Use extensively (`@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`, `@RequiredArgsConstructor`)
- **Spring**: `@Service`, `@Transactional`, `@RestController`, `@RequestMapping`
- **Validation**: `@Valid`, JSR-303 annotations on DTOs
- **Swagger**: `@Tag`, `@Operation`, `@ApiResponses` for API documentation

### Entity Patterns
```java
@Entity
@Table(name = "spaces")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")  // Soft delete pattern
@SQLDelete(sql = "UPDATE spaces SET deleted_at = NOW() WHERE space_id = ?")
public class Space {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "space_id")
    private Long id;
    
    @Builder
    public Space(/* fields */) {
        // initialization logic
    }
    
    // Business logic methods
    public void update(SpaceUpdateRequest request) {
        if (request.title() != null) {
            this.title = request.title();
        }
    }
}
```

### DTO Patterns (Java Records)
```java
// Request DTO with validation and entity conversion
public record SpaceRequest(
    String title,
    String description,
    SpaceCategory category,
    String address,
    Double latitude,
    Double longitude,
    Integer pricePerMonth
) {
    public Space toEntity(User user) {
        return Space.builder()
            .title(this.title())
            .description(this.description())
            .category(this.category())
            .address(this.address())
            .latitude(BigDecimal.valueOf(this.latitude()))
            .user(user)
            .build();
    }
}

// Response DTO with static factory method
public record SpaceResponse(
    Long id,
    String title,
    String hostName,
    List<ImageResponse> images
) {
    public static SpaceResponse of(Space space, List<ImageResponse> images) {
        return new SpaceResponse(
            space.getId(),
            space.getTitle(),
            space.getUser().getNickname(),
            images
        );
    }
}
```

### Service Layer Patterns
```java
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)  // Default for read operations
public class SpaceService {
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    
    @Transactional  // Override for write operations
    public void createSpace(SpaceRequest request, UUID userId) {
        // Validation
        if (spaceRepository.existsByAddress(request.address())) {
            throw new DuplicateSpaceException("동일한 공간이 존재합니다.");
        }
        
        // Business logic
        User user = userRepository.findByUuid(userId)
            .orElseThrow(() -> new AuthException(AuthErrorCode.OAUTH2_USER_NOT_FOUND));
        
        Space space = request.toEntity(user);
        spaceRepository.save(space);
    }
}
```

## Error Handling

### Structured Exception Handling
```java
// 1. Define error codes as enum
@Getter
@RequiredArgsConstructor
public enum SpaceErrorCode {
    SPACE_NOT_FOUND(HttpStatus.NOT_FOUND, "공간을 찾을 수 없습니다."),
    DUPLICATE_SPACE(HttpStatus.CONFLICT, "이미 존재하는 공간입니다.");
    
    private final HttpStatus httpStatus;
    private final String message;
}

// 2. Create custom exceptions
public class SpaceException extends RuntimeException {
    private final SpaceErrorCode errorCode;
    
    public SpaceException(SpaceErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

// 3. Global exception handler
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(SpaceException.class)
    public ResponseEntity<Map<String, Object>> handleSpaceException(SpaceException e) {
        log.error("[SpaceException] {} : {}", e.getErrorCode().name(), e.getErrorCode().getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("code", e.getErrorCode().name());
        body.put("message", e.getErrorCode().getMessage());
        
        return ResponseEntity
            .status(e.getErrorCode().getHttpStatus())
            .body(body);
    }
}
```

### Error Response Format
```json
{
    "code": "SPACE_NOT_FOUND",
    "message": "공간을 찾을 수 없습니다."
}
```

## Performance Optimization Patterns

### N+1 Problem Prevention
```java
// Repository with fetch joins for complex queries
@Repository
public interface SpaceRepository extends JpaRepository<Space, Long>, SpaceRepositoryCustom {
    
    @Query("SELECT s FROM Space s LEFT JOIN FETCH s.user LEFT JOIN FETCH s.spaceImages")
    List<Space> findAllWithImagesAndUser();
}

// Entity with batch size for collections
@BatchSize(size = 100)
@OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
private List<SpaceImage> spaceImages = new ArrayList<>();
```

### Bulk Operations
```java
@Modifying(clearAutomatically = true)  // Clear persistence context after bulk operation
@Query("UPDATE Matching m SET m.status = 'CANCELLED' WHERE m.space.id = :spaceId AND m.status = 'WAITING'")
void bulkCancelBySpaceId(@Param("spaceId") Long spaceId);
```

## Security Patterns

### JWT Authentication
- **Pattern**: Access Token (short-lived) + Refresh Token (long-lived with rotation)
- **Storage**: Refresh tokens in Redis, blacklisted access tokens in Redis
- **Security**: RTR (Refresh Token Rotation) for enhanced security

### Method Security
```java
@GetMapping("/{id}")
public ResponseEntity<SpaceResponse> getSpace(
    @PathVariable Long id,
    @AuthenticationPrincipal CustomUserDetails userDetails  // Inject authenticated user
) {
    // Business logic with authorization checks
}
```

## API Design Conventions

### Controller Patterns
```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/piece/spaces")
@Tag(name = "Space", description = "공간 API")
public class SpaceController {
    
    @Operation(summary = "공간 등록", description = "새로운 공간을 등록합니다")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "공간 등록 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createSpace(
        @RequestPart("request") @Valid SpaceRequest request,
        @RequestPart(value = "images", required = false) List<MultipartFile> images,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        spaceService.createSpace(request, images, userDetails.getUuid());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
```

### Response Patterns
- **Success**: `200 OK`, `201 Created`, `204 No Content`
- **Error**: Structured error responses with codes and messages
- **File Upload**: `multipart/form-data` with `@RequestPart`

## Testing Guidelines

### Test Structure
```java
@SpringBootTest
@Transactional
class SpaceServiceTest {
    
    @Autowired
    private SpaceService spaceService;
    
    @MockBean
    private SpaceRepository spaceRepository;
    
    @Test
    @DisplayName("공간 생성 성공")
    void createSpace_Success() {
        // Given
        SpaceRequest request = new SpaceRequest(/* test data */);
        User user = User.builder().uuid(UUID.randomUUID()).build();
        
        // When
        Long spaceId = spaceService.createSpace(request, user.getUuid());
        
        // Then
        assertThat(spaceId).isNotNull();
    }
}
```

## Important Implementation Notes

### Database Considerations
- **Soft Deletes**: Use `@SQLDelete` and `@SQLRestriction` for soft deletes
- **Dual ID Strategy**: UUID for external exposure, Long PK for internal performance
- **Decimal Precision**: Use `BigDecimal` for monetary values and geographic coordinates

### Event-Driven Architecture
```java
// Domain event publishing
@EventListener
@Async
public void handleMatchingSuccessEvent(MatchingSuccessEvent event) {
    notificationService.sendNotification(event);
}
```

### File Handling
- **Storage**: AWS S3 via Supabase
- **Images**: Separate services for different entity types (SpaceImageService, PortfolioImageService)
- **Validation**: File size and type restrictions

### Configuration Management
- **Profiles**: Environment-specific configurations (dev, prod)
- **External Config**: Use `.env` file for sensitive data
- **Validation**: Comprehensive input validation with clear error messages

## Common Pitfalls to Avoid

1. **N+1 Queries**: Always use fetch joins or batch size for collections
2. **Transaction Boundaries**: Keep transactions focused, use events for cross-cutting concerns
3. **Exception Handling**: Never expose internal stack traces to clients
4. **Security**: Always validate authentication/authorization in service layer
5. **File Storage**: Never store files in the application directory

## Development Workflow

1. **Feature Development**: Create in feature branch, follow domain boundaries
2. **Code Review**: Ensure proper exception handling, validation, and documentation
3. **Testing**: Add comprehensive tests for new functionality
4. **Documentation**: Update Swagger annotations for API changes
5. **Performance**: Profile queries and optimize N+1 issues early

This codebase emphasizes clean architecture, comprehensive error handling, and performance optimization suitable for enterprise-level Java applications.