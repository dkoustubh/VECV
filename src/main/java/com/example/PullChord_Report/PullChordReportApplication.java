package com.example.PullChord_Report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class PullChordReportApplication {

	public static void main(String[] args) {
		SpringApplication.run(PullChordReportApplication.class, args);
	}

	@org.springframework.context.annotation.Bean
	public org.springframework.boot.CommandLineRunner initSP(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				System.out.println("Updating stored procedure sp_CalcDowntime...");
				java.nio.file.Path path = java.nio.file.Paths.get("sql/sp_CalcDowntime.sql");
				if (java.nio.file.Files.exists(path)) {
					String sql = new String(java.nio.file.Files.readAllBytes(path));
					// Split by GO is often needed for MSSQL scripts executed via JDBC
					String[] batches = sql.split("(?i)\\nGO\\b");
					for (String batch : batches) {
						if (!batch.trim().isEmpty()) {
							jdbcTemplate.execute(batch);
						}
					}
					System.out.println("Stored procedure updated successfully.");
				} else {
					System.err.println("SQL file not found at " + path.toAbsolutePath());
				}
			} catch (Exception e) {
				System.err.println("Failed to update SP: " + e.getMessage());
				e.printStackTrace();
			}
		};
	}

}
