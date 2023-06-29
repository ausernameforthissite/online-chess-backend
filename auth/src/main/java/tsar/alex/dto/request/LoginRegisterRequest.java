package tsar.alex.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRegisterRequest {
    @NotNull(message = "Имя не должно быть пустым")
    @Size(min = 5, max = 30, message = "Имя должно быть длиной от 5 до 30 символов включительно")
    @Pattern(regexp = "^[A-Za-zА-ЯЁа-яё].+", message = "Имя должно начинаться с русской или латинской буквы")
    @Pattern(regexp = "^[-_ A-Za-zА-ЯЁа-яё0-9]+$", message = "Имя может содержать только русские и латинские буквы,"
                                                                + "цифры, пробелы и символы \"-\" и \"_\".")
    @Pattern(regexp = ".+[^ ]$", message = "Имя не может оканчиваться на пробел")
    @Pattern(regexp = "^(?!.*[- _]{2,}).+$", message = "Символы \"-\", \"_\" и пробел не могут идти подряд")
    private String username;

    @NotEmpty(message = "Пароль не должен быть пустым")
    private String password;

    @Override
    public String toString() {
        return "LoginRegisterRequest{" +
                "username='" + username + '\'' +
                ", password=" + (password != null && !password.isBlank() ? "hidden by security policy" : "")  + '\'' +
                '}';
    }
}