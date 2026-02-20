package com.example.mcptool.model;

public record InstalledSoftware(
        String softwareId,
        String softwareName,
        String softwareSnowRequestUrl,
        String approverEmailId,
        String status // REQUESTED / APPROVED / INSTALLED
) {}
