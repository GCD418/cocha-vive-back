package cocha.vive.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
            .info(new Info()
                .title("CochaVive Backend API")
                .version("1.0.0")
                .description("Events, Users and Categories management API")
                .contact(new Contact()
                    .name("FourElements")
                    .email("fourElements418@gmail.com")
                )
            );
    }
}
