package com.example.mcptool.controller;

import com.example.mcptool.data.MockDataStore;
import com.example.mcptool.model.EmployeeSoftwareCompareResult;
import com.example.mcptool.model.InstalledSoftware;
import com.example.mcptool.model.ServiceNowTicket;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tools")
public class ToolsController {

    private final MockDataStore store;

    public ToolsController(MockDataStore store) {
        this.store = store;
    }

    @GetMapping("/employees")
    public Object listEmployees() {
        return store.listEmployees();
    }

    @GetMapping("/employees/{employeeId}")
    public Object getEmployee(@PathVariable("employeeId") String employeeId) {
        return store.getEmployee(employeeId).orElse(null);
    }

    @GetMapping("/teams")
    public List<String> listTeams() {
        return store.listTeams();
    }

    @GetMapping("/teams/{teamName}/members")
    public Object teamMembers(@PathVariable("teamName") String teamName) {
        return store.getTeamMembers(teamName);
    }

    @GetMapping("/servicenow/tickets")
    public List<ServiceNowTicket> queryTickets(
            @RequestParam(name = "status",required = false) String status,
            @RequestParam(name = "assignedGroup",required = false) String assignedGroup,
            @RequestParam(name = "projectId",required = false) String projectId,
            @RequestParam(name = "requestedByEmployeeId",required = false) String requestedByEmployeeId
    ) {
        return store.queryTickets(status, assignedGroup, projectId, requestedByEmployeeId);
    }

    @GetMapping("/project/softwares")
    public List<InstalledSoftware> getSoftwaresByProject(
            @RequestParam("projectIdOrName") String projectIdOrName
    ) {
        return store.getSoftwaresByProject(projectIdOrName);
    }

    @GetMapping("/compare/employees")
    public EmployeeSoftwareCompareResult compareEmployees(
            @RequestParam("employeeId1") String employeeId1,
            @RequestParam("employeeId2") String employeeId2
    ) {
        return store.compareEmployees(employeeId1, employeeId2);
    }

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("message", "pong");
    }
}
