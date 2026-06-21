package com.wishlist.nathan.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * Serve as fotos gravadas pelo {@code ArmazenamentoLocal} sob o prefixo público
 * configurado ({@code app.storage.public-base}, padrão {@code /media}). Cache de
 * 1 ano porque os nomes são únicos (UUID) — imagens nunca mudam de conteúdo.
 */
@Configuration
public class RecursosWebConfig implements WebMvcConfigurer {

    private final AppProperties props;

    public RecursosWebConfig(AppProperties props) {
        this.props = props;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String base = props.storage().publicBase().replaceAll("/+$", "");
        String diretorio = Path.of(props.storage().dir()).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler(base + "/**")
                .addResourceLocations(diretorio)
                .setCachePeriod(60 * 60 * 24 * 365);
    }
}
