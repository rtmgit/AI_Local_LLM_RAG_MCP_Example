package com.example.mcptool.model;

import java.util.List;

public record EmployeeProfile(
        String employeeId,
        String employeeName,
        String employeeRole,
        String description,
        String teamName,
        List<ProjectAssignment> projects,
        List<InstalledSoftware> installedSoftwares
        ) {}
