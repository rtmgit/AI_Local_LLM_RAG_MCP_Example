package com.example.mcptool.model;

import java.util.List;

public record ServiceNowTicket(
        String ticketId,
        String type, // CHANGE_REQUEST / INCIDENT / SOFTWARE_REQUEST etc.
        String shortDescription,
        String description,
        String status, // OPEN / IN_PROGRESS / CLOSED
        String assignedGroup,
        String priority, // P1..P4
        String createdAt,
        String updatedAt,
        String url,
        Approvals approvals,
        References references
) {
    public record Approvals(boolean pending, List<String> pendingApprovers) {}
    public record References(String projectId, String appId, String requestedByEmployeeId) {}
}
