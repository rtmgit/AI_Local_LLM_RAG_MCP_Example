package com.example.mcptool.mcp;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ToolRegistry {

    public List<ToolDefinition> tools() {
        return List.of(
                new ToolDefinition(
                        "listEmployees",
                        "List all employees with basic details",
                        schemaObject(Map.of())
                ),
                new ToolDefinition(
                        "getEmployeeById",
                        "Get employee profile by employeeId",
                        schemaObject(Map.of(
                                "employeeId", schemaString("Employee ID like E001")
                        ))
                ),
                new ToolDefinition(
                        "listTeams",
                        "List all teams",
                        schemaObject(Map.of())
                ),
                new ToolDefinition(
                        "getTeamMembers",
                        "Get team members by teamName",
                        schemaObject(Map.of(
                                "teamName", schemaString("Team name like Payments or Risk")
                        ))
                ),
                new ToolDefinition(
                        "queryServiceNowTickets",
                        "Query ServiceNow tickets by optional filters: status, assignedGroup, projectId, requestedByEmployeeId",
                        schemaObject(Map.of(
                                "status", schemaString("OPEN / IN_PROGRESS / CLOSED"),
                                "assignedGroup", schemaString("Payments / Risk"),
                                "projectId", schemaString("Project ID like PRJ100"),
                                "requestedByEmployeeId", schemaString("Employee ID like E001")
                        ))
                ),
                new ToolDefinition(
                        "compareEmployees",
                        "Compare installed software between two employees and return common/unique lists",
                        schemaObject(Map.of(
                                "employeeId1", schemaString("Employee ID like E001"),
                                "employeeId2", schemaString("Employee ID like E002")
                        ))
                ),
                new ToolDefinition(
                        "getSoftwaresByProject",
                        "Get all the installed softwares by project id or name for the given project by their team members ",
                        schemaObject(Map.of(
                                "projectIdOrName", schemaString("Project ID or Name like PRJ100 or Payment Modernization"),
                                "employeeId2", schemaString("Employee ID like E002")
                        ))
                )
        );
    }

    private Map<String, Object> schemaObject(Map<String, Object> properties) {
        return Map.of(
                "type", "object",
                "properties", properties
        );
    }

    private Map<String, Object> schemaString(String description) {
        return Map.of(
                "type", "string",
                "description", description
        );
    }
}
