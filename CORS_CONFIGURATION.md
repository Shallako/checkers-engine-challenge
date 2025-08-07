# CORS Configuration for Checkers Game API

## Current Configuration

The API currently uses a permissive CORS policy that allows requests from all origins and explicitly enables GET, POST, PUT, and DELETE methods:

```
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
```

This configuration is applied to the `GameController` class, which handles all game-related API endpoints.

## Development vs. Production

### Development Environment

For development purposes, the current permissive configuration (`origins = "*"`) is convenient as it allows:
- Testing with various frontend applications running on different ports
- Easy integration with development tools and environments
- Rapid prototyping without CORS-related blockers

### Production Environment Recommendations

For production environments, it's recommended to restrict CORS to only the specific origins that need access to the API:

```
@CrossOrigin(origins = {
    "https://your-production-domain.com", 
    "https://your-staging-domain.com"
})
```

Alternatively, for more complex CORS requirements, you can configure CORS globally using a `WebMvcConfigurer`:

```
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("https://your-production-domain.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

## Security Considerations

Allowing all origins (`origins = "*"`) in production:
- Increases the risk of cross-site request forgery (CSRF) attacks
- May expose your API to unauthorized access
- Does not comply with the principle of least privilege

Always restrict CORS to only the necessary origins in production environments.
