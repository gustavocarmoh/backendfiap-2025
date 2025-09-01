package fiap.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Controller responsável por servir recursos estáticos como fotos padrão.
 * Fornece endpoints para acessar imagens e recursos do sistema.
 */
@RestController
@RequestMapping("/api/photos")
@Tag(name = "Photo Resources", description = "Recursos de imagens e fotos padrão")
public class PhotoResourceController {

    /**
     * Serve a foto padrão para usuários que não possuem foto personalizada.
     * Esta é uma imagem de avatar genérico que representa usuários sem foto.
     * 
     * @return foto padrão em formato de imagem
     */
    @GetMapping("/default")
    @Operation(summary = "Foto padrão de usuário", description = "Retorna uma imagem padrão para usuários sem foto personalizada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Foto padrão retornada com sucesso", content = @Content(mediaType = "image/png")),
            @ApiResponse(responseCode = "404", description = "Foto padrão não encontrada")
    })
    public ResponseEntity<byte[]> getDefaultPhoto() {
        try {
            // Tenta carregar uma imagem padrão do classpath
            Resource resource = new ClassPathResource("static/images/default-avatar.png");

            if (resource.exists()) {
                byte[] imageBytes = resource.getInputStream().readAllBytes();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setContentLength(imageBytes.length);
                headers.setCacheControl("public, max-age=86400"); // Cache por 24 horas

                return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            } else {
                // Se não existir a imagem, retorna uma imagem simples gerada
                return getGeneratedDefaultPhoto();
            }

        } catch (IOException e) {
            // Em caso de erro, retorna uma imagem simples gerada
            return getGeneratedDefaultPhoto();
        }
    }

    /**
     * Gera uma foto padrão simples quando não há imagem estática disponível.
     * 
     * @return imagem PNG de 1x1 pixel transparente
     */
    private ResponseEntity<byte[]> getGeneratedDefaultPhoto() {
        // Imagem PNG de 1x1 pixel transparente (mínima possível)
        byte[] defaultImageBytes = {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
                (byte) 0x89, 0x00, 0x00, 0x00, 0x0B, 0x49, 0x44, 0x41, 0x54, 0x78, (byte) 0xDA, 0x63, 0x60, 0x00, 0x02,
                0x00, 0x00, 0x05, 0x00, 0x01, (byte) 0xE2, 0x26, 0x05, (byte) 0x9B, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45,
                0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(defaultImageBytes.length);
        headers.setCacheControl("public, max-age=86400"); // Cache por 24 horas

        return new ResponseEntity<>(defaultImageBytes, headers, HttpStatus.OK);
    }
}
