package com.masterminds.controller;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.masterminds.service.ReportService;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

	@Autowired
	private ReportService reportService;

    @GetMapping(value = "/export/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> downloadAccountReport(@PathVariable UUID userId) {
        try {
            String jsonReport = reportService.generateAccountReportJson(userId);
            byte[] reportBytes = jsonReport.getBytes(StandardCharsets.UTF_8);

            String filename = "Veranda_Report_" + userId.toString().substring(0, 8) + ".json";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .body(reportBytes);

        } catch (Exception e) {
        	e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
	
}
