package az.gov.adra.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Feedback {

    @NotNull
    private String name;
    @NotNull
    @Email
    private String email;
    @NotNull
    @Min(10)
    private String feedback;

}
