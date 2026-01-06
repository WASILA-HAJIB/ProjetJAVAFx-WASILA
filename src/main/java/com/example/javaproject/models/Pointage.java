package com.example.javaproject.models;
import java.time.LocalDate;
import java.time.LocalTime;

public class Pointage {
    private int id;
    private int employeId;
    private LocalDate datePointage;
    private LocalTime heurePointage;
    private String typePointage; // "ENTRÉE" ou "SORTIE"

    public Pointage(int id, int employeId, LocalDate datePointage, LocalTime heurePointage, String typePointage) {
        this.id = id;
        this.employeId = employeId;
        this.datePointage = datePointage;
        this.heurePointage = heurePointage;
        this.typePointage = typePointage;
    }

    // Getters
    public int getId() { return id; }
    public LocalDate getDatePointage() { return datePointage; }
    public LocalTime getHeurePointage() { return heurePointage; }
    public String getTypePointage() { return typePointage; }
}