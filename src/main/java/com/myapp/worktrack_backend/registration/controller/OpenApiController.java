package com.myapp.worktrack_backend.registration.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Serves the comprehensive WorkTrack OpenAPI 3.0 specification and interactive Swagger UI.
 * Provides zero-dependency, bulletproof documentation rendering for frontends and developers.
 */
@RestController
public class OpenApiController {

    private String cachedOpenApiJson = null;

    @GetMapping(value = "/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getOpenApiSpecification() throws IOException {
        if (cachedOpenApiJson == null) {
            Resource resource = new ClassPathResource("static/openapi.json");
            if (resource.exists()) {
                cachedOpenApiJson = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            } else {
                return ResponseEntity.notFound().build();
            }
        }
        return ResponseEntity.ok(cachedOpenApiJson);
    }

    @GetMapping(value = {"/swagger-ui.html", "/swagger-ui"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getSwaggerUi() {
        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>WorkTrack REST API - Interactive Documentation</title>
                    <link rel="stylesheet" type="text/css" href="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui.css" />
                    <link rel="icon" type="image/png" href="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/favicon-32x32.png" sizes="32x32" />
                    <style>
                        html { box-sizing: border-box; overflow: -moz-scrollbars-vertical; overflow-y: scroll; }
                        *, *:before, *:after { box-sizing: inherit; }
                        body { margin: 0; background: #fafafa; font-family: sans-serif; }
                        .topbar { display: none; }
                    </style>
                </head>
                <body>
                    <div id="swagger-ui"></div>
                    <script src="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui-bundle.js" charset="UTF-8"></script>
                    <script src="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui-standalone-preset.js" charset="UTF-8"></script>
                    <script>
                        window.onload = function() {
                            const ui = SwaggerUIBundle({
                                url: "/api-docs",
                                dom_id: '#swagger-ui',
                                deepLinking: true,
                                presets: [
                                    SwaggerUIBundle.presets.apis,
                                    SwaggerUIStandalonePreset
                                ],
                                plugins: [
                                    SwaggerUIBundle.plugins.DownloadUrl
                                ],
                                layout: "BaseLayout"
                            });
                            window.ui = ui;
                        };
                    </script>
                </body>
                </html>
                """;
        return ResponseEntity.ok(html);
    }
}
