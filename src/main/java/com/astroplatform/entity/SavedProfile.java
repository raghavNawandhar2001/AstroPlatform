package com.astroplatform.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "saved_profiles")
@Data
public class SavedProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String gender;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private String location;
}