package com.example.mcptool.mcp;

import com.example.mcptool.data.MockDataStore;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ToolExecutionService {

    private final MockDataStore store;

    public ToolExecutionService(MockDataStore store) {
        this.store = store;
    }

    public Object execute(String toolName, Map<String, Object> args) {
        return switch (toolName) {
            case "listEmployees" -> store.listEmployees();
            case "getEmployeeById" -> {
                String employeeId = requireString(args, "employeeId");
                yield store.getEmployee(employeeId).orElse(null);
            }
            case "listTeams" -> store.listTeams();
            case "getTeamMembers" -> {
                String teamName = requireString(args, "teamName");
                yield store.getTeamMembers(teamName);
            }
            case "queryServiceNowTickets" -> {
                String status = optString(args, "status");
                String assignedGroup = optString(args, "assignedGroup");
                String projectId = optString(args, "projectId");
                String requestedByEmployeeId = optString(args, "requestedByEmployeeId");
                yield store.queryTickets(status, assignedGroup, projectId, requestedByEmployeeId);
            }
            case "compareEmployees" -> {
                String e1 = requireString(args, "employeeId1");
                String e2 = requireString(args, "employeeId2");
                yield store.compareEmployees(e1, e2);
            }
            case "getSoftwaresByProject" -> {
                String p1 = requireString(args, "projectIdOrName");
                yield store.getSoftwaresByProject(p1);
            }
            default -> throw new IllegalArgumentException("Unknown tool: " + toolName);
        };
    }

    private String requireString(Map<String, Object> args, String key) {
        Object v = args.get(key);
        if (v == null) throw new IllegalArgumentException("Missing required argument: " + key);
        String s = String.valueOf(v).trim();
        if (s.isEmpty()) throw new IllegalArgumentException("Blank argument: " + key);
        return s;
    }

    private String optString(Map<String, Object> args, String key) {
        Object v = args.get(key);
        if (v == null) return null;
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }
}
