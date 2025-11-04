package com.apirip.trukeamonolito.auth.dto;

import jakarta.validation.constraints.*;

public record SignupForm(
        @NotBlank(message = "{signup.email.required}")
        @Email(message = "{signup.email.invalid}",
                regexp = "^[a-zA-Z]+(\\.[a-zA-Z]+[0-9]*)?@epn\\.edu\\.ec$")
        String email,

        @NotBlank(message = "{signup.fullname.required}")
        String fullName,

        @NotBlank(message = "{signup.password.required}")
        @Pattern(message = "{signup.password.pattern}",
                regexp="^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!_#%*?&])[A-Za-z\\d@$!_#%*?&]{8,}$")
        String password,

        @NotBlank(message = "{signup.uniquecode.required}")
        @Pattern(message = "{signup.uniquecode.pattern}", regexp="^\\d{9}$")
        String uniqueCode,

        @Pattern(message = "{signup.phone.pattern}", regexp="^\\d{10}$")
        String phone
) {}

