package com.example.mcptool.data;

import com.example.mcptool.model.EmployeeProfile;
import com.example.mcptool.model.EmployeeSoftwareCompareResult;
import com.example.mcptool.model.InstalledSoftware;
import com.example.mcptool.model.ProjectAssignment;
import com.example.mcptool.model.ServiceNowTicket;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MockDataStore {

    private final Map<String, EmployeeProfile> employeesById = new LinkedHashMap<>();
    private final Set<String> teams = new LinkedHashSet<>();
    private final List<ServiceNowTicket> tickets = new ArrayList<>();

    public MockDataStore() {
        // 8 softwares
        var swJdk = software("SW001", "JDK 17", "INSTALLED");
        var swIntellij = software("SW002", "IntelliJ IDEA", "INSTALLED");
        var swDocker = software("SW003", "Docker Desktop", "INSTALLED");
        var swK8s = software("SW004", "kubectl", "APPROVED");
        var swPostman = software("SW005", "Postman", "INSTALLED");
        var swDBeaver = software("SW006", "DBeaver", "INSTALLED");
        var swKafkaTool = software("SW007", "Kafka Tool", "REQUESTED");
        var swVsCode = software("SW008", "VS Code", "INSTALLED");

        // 2 teams
        teams.add("Payments");
        teams.add("Risk");

        // Projects
        var prjPay = new ProjectAssignment("PRJ100", "Payment Modernization", "Senior Developer", "2025-09-01", "");
        var prjFraud = new ProjectAssignment("PRJ200", "Fraud Detection", "Tech Lead", "2025-10-15", "");
        var prjCore = new ProjectAssignment("PRJ300", "Core Platform", "Senior Engineer", "2024-05-01", "2025-08-31");

        // 5 employees
        addEmployee(new EmployeeProfile(
                "E001", "Ravi", "Senior Full Stack Developer", "Owns API + integrations", "Payments",
                List.of(prjPay, prjCore),
                List.of(swJdk, swIntellij, swDocker, swPostman, swDBeaver, swVsCode)
        ));

        addEmployee(new EmployeeProfile(
                "E002", "Anita", "Backend Engineer", "Works on microservices", "Payments",
                List.of(prjPay),
                List.of(swJdk, swIntellij, swDocker, swK8s, swPostman)
        ));

        addEmployee(new EmployeeProfile(
                "E003", "Suresh", "DevOps Engineer", "CI/CD and clusters", "Payments",
                List.of(prjPay),
                List.of(swDocker, swK8s, swVsCode, swDBeaver)
        ));

        addEmployee(new EmployeeProfile(
                "E004", "Meera", "QA Engineer", "Automation + test suites", "Risk",
                List.of(prjFraud),
                List.of(swPostman, swVsCode, swDBeaver)
        ));

        addEmployee(new EmployeeProfile(
                "E005", "John", "Tech Lead", "Architecture and review", "Risk",
                List.of(prjFraud, prjCore),
                List.of(swJdk, swIntellij, swDocker, swK8s, swKafkaTool, swVsCode)
        ));

        // Some ServiceNow tickets
        tickets.add(new ServiceNowTicket(
                "CHG001",
                "CHANGE_REQUEST",
                "Deploy Payment API v2",
                "Production deployment for Payment API v2",
                "OPEN",
                "Payments",
                "P2",
                "2026-02-01T10:00:00Z",
                "2026-02-07T09:10:00Z",
                "https://servicenow.example/chg/CHG001",
                new ServiceNowTicket.Approvals(true, List.of("approver.payments@example.com")),
                new ServiceNowTicket.References("PRJ100", "APP10", "E001")
        ));

        tickets.add(new ServiceNowTicket(
                "INC010",
                "INCIDENT",
                "Timeout in risk scoring",
                "High latency observed in risk scoring API",
                "IN_PROGRESS",
                "Risk",
                "P1",
                "2026-02-05T14:05:00Z",
                "2026-02-07T11:30:00Z",
                "https://servicenow.example/inc/INC010",
                new ServiceNowTicket.Approvals(false, List.of()),
                new ServiceNowTicket.References("PRJ200", "APP20", "E005")
        ));

        tickets.add(new ServiceNowTicket(
                "CHG002",
                "CHANGE_REQUEST",
                "Upgrade Postgres extension",
                "Upgrade pgvector extension on shared DB",
                "CLOSED",
                "Payments",
                "P3",
                "2026-01-15T09:00:00Z",
                "2026-01-20T18:00:00Z",
                "https://servicenow.example/chg/CHG002",
                new ServiceNowTicket.Approvals(false, List.of()),
                new ServiceNowTicket.References("PRJ100", "APP10", "E002")
        ));
    }

    private InstalledSoftware software(String id, String name, String status) {
        return new InstalledSoftware(
                id,
                name,
                "https://servicenow.example/software/" + id,
                "approver.it@example.com",
                status
        );
    }

    private void addEmployee(EmployeeProfile e) {
        employeesById.put(e.employeeId(), e);
    }

    // ---- Query helpers ----

    public List<String> listTeams() {
        return new ArrayList<>(teams);
    }

    public List<EmployeeProfile> listEmployees() {
        return new ArrayList<>(employeesById.values());
    }

    public Optional<EmployeeProfile> getEmployee(String employeeId) {
        return Optional.ofNullable(employeesById.get(employeeId));
    }

    public List<EmployeeProfile> getTeamMembers(String teamName) {
        return employeesById.values().stream()
                .filter(e -> e.teamName().equalsIgnoreCase(teamName))
                .toList();
    }

    public List<ServiceNowTicket> queryTickets(String status, String assignedGroup, String projectId, String requestedByEmployeeId) {
        return tickets.stream()
                .filter(t -> status == null || t.status().equalsIgnoreCase(status))
                .filter(t -> assignedGroup == null || t.assignedGroup().equalsIgnoreCase(assignedGroup))
                .filter(t -> projectId == null || (t.references() != null && projectId.equalsIgnoreCase(t.references().projectId())))
                .filter(t -> requestedByEmployeeId == null || (t.references() != null && requestedByEmployeeId.equalsIgnoreCase(t.references().requestedByEmployeeId())))
                .toList();
    }

    public List<InstalledSoftware> getSoftwaresByProject(String project) {
        return employeesById.values().stream().filter(emp -> {
                    for( ProjectAssignment projectAssignment: emp.projects()) {
                        if(projectAssignment.projectName().equalsIgnoreCase(project) ||
                                projectAssignment.projectId().equalsIgnoreCase(project)) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(EmployeeProfile::installedSoftwares).flatMap(List::stream).distinct().collect(Collectors.toList());
    }

    public EmployeeSoftwareCompareResult compareEmployees(String employeeId1, String employeeId2) {
        var e1 = getEmployee(employeeId1).orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId1));
        var e2 = getEmployee(employeeId2).orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId2));

        Map<String, String> sw1 = e1.installedSoftwares().stream()
                .collect(Collectors.toMap(InstalledSoftware::softwareId, InstalledSoftware::softwareName, (a,b)->a, LinkedHashMap::new));

        Map<String, String> sw2 = e2.installedSoftwares().stream()
                .collect(Collectors.toMap(InstalledSoftware::softwareId, InstalledSoftware::softwareName, (a,b)->a, LinkedHashMap::new));

        Set<String> commonIds = new LinkedHashSet<>(sw1.keySet());
        commonIds.retainAll(sw2.keySet());

        Set<String> only1 = new LinkedHashSet<>(sw1.keySet());
        only1.removeAll(sw2.keySet());

        Set<String> only2 = new LinkedHashSet<>(sw2.keySet());
        only2.removeAll(sw1.keySet());

        return new EmployeeSoftwareCompareResult(
                new EmployeeSoftwareCompareResult.EmployeeRef(e1.employeeId(), e1.employeeName()),
                new EmployeeSoftwareCompareResult.EmployeeRef(e2.employeeId(), e2.employeeName()),
                commonIds.stream().map(id -> new EmployeeSoftwareCompareResult.SoftwareRef(id, sw1.get(id))).toList(),
                only1.stream().map(id -> new EmployeeSoftwareCompareResult.SoftwareRef(id, sw1.get(id))).toList(),
                only2.stream().map(id -> new EmployeeSoftwareCompareResult.SoftwareRef(id, sw2.get(id))).toList()
        );
    }
}
