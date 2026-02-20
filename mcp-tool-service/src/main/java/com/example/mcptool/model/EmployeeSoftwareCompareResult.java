package com.example.mcptool.model;

import java.util.List;

public record EmployeeSoftwareCompareResult(
        EmployeeRef employeeA,
        EmployeeRef employeeB,
        List<SoftwareRef> commonSoftwares,
        List<SoftwareRef> onlyInEmployeeA,
        List<SoftwareRef> onlyInEmployeeB
) {
    public record EmployeeRef(String employeeId, String employeeName) {}
    public record SoftwareRef(String softwareId, String softwareName) {}
}
