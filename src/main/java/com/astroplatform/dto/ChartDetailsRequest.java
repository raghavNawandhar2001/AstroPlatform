package com.astroplatform.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartDetailsRequest {

    @Min(value = 1, message = "Year must be greater than 0")
    private int year;

    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private int month;

    @Min(value = 1, message = "Day must be between 1 and 31")
    @Max(value = 31, message = "Day must be between 1 and 31")
    private int day;

    @Min(0) @Max(23) private int hour;   // 24-hour format (e.g., 23 for 11 PM)
    @Min(0) @Max(59) private int minute;

    @NotBlank
    private String location; // e.g., "Jaipur, Rajasthan, India"

    @NotBlank
    private String name;

    @NotBlank
    private String gender;
}
