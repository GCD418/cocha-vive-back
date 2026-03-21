package cocha.vive.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCreateDTO {

    private String name;

    private String firstLastName;

    //private String secondLastName;

    private String email;

    private String googleProviderId;

    private String documentNumber; //TEMPORALLY: will hold photoUrl

    //private String documentExtension;

    private String role;
}
