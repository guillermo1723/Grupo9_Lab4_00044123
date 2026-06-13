package com.server.app.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePasswordDto {

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String oldPassword;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&._-]).+$", message = "La contraseña debe incluir al menos una mayúscula, una minúscula, un número y un carácter especial")
    private String newPassword;

    @NotBlank(message = "Debes confirmar la nueva contraseña")
    private String confirmPassword;
}