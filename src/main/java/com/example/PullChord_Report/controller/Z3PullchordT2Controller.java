package com.example.PullChord_Report.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.PullChord_Report.entity.Z3PullchordT2Entity;
import com.example.PullChord_Report.entity.Z5PullchordTEntity;
import com.example.PullChord_Report.entity.Z7PullchordTEntity;
import com.example.PullChord_Report.entity.Z9PullchordTEntity;
import com.example.PullChord_Report.repository.Z3PullchordT2Repository;
import com.example.PullChord_Report.repository.Z5PullchordTRepository;
import com.example.PullChord_Report.repository.Z7PullchordTRepository;
import com.example.PullChord_Report.repository.Z9PullchordTRepository;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class Z3PullchordT2Controller {

	@Autowired
	private Z3PullchordT2Repository z3PullchordT2RepositoryInstance;

	@Autowired
	private Z5PullchordTRepository z5PullchordTRepositoryInstance;

	@Autowired
	private Z7PullchordTRepository z7PullchordTRepositoryInstance;

	@Autowired
	private Z9PullchordTRepository z9PullchordTRepositoryInstance;

	// Note: /report endpoint removed - all data now shown on /dashboard
	// The dashboard controller handles all table viewing and filtering

	@GetMapping("/download")
	@ResponseBody
	public void downloadExcel(@RequestParam String selectedTable,
			@RequestParam(required = false) String fromDateTime,
			@RequestParam(required = false) String toDateTime,
			@RequestParam(required = false, name = "shift") String shiftName,
			@RequestParam(required = false) String station,
			@RequestParam(required = false) String objectName, HttpServletResponse response) throws IOException {

		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Sheet1");

		// Clean parameters
		if (station != null && station.trim().isEmpty())
			station = null;
		if (shiftName != null && shiftName.trim().isEmpty())
			shiftName = null;

		if (fromDateTime != null && !fromDateTime.trim().isEmpty()) {
			fromDateTime = fromDateTime.replace("T", " ");
			if (fromDateTime.length() == 16)
				fromDateTime += ":00";
		} else {
			fromDateTime = null;
		}

		if (toDateTime != null && !toDateTime.trim().isEmpty()) {
			toDateTime = toDateTime.replace("T", " ");
			if (toDateTime.length() == 16)
				toDateTime += ":59";
		} else {
			toDateTime = null;
		}

		// Use a large page size to fetch "all" matching records without crashing memory
		// 50k limit is reasonable for Excel; adjust if needed.
		Pageable filePageable = PageRequest.of(0, 50000);

		List<?> allRecords = new ArrayList<>();

		switch (selectedTable) {
			case "Z5 Pullchord T":
				allRecords = z5PullchordTRepositoryInstance
						.searchReports(station, shiftName, fromDateTime, toDateTime, filePageable).getContent();
				break;
			case "Z7 Pullchord T":
				allRecords = z7PullchordTRepositoryInstance
						.searchReports(station, shiftName, fromDateTime, toDateTime, filePageable).getContent();
				break;
			case "Z9 Pullchord T":
				allRecords = z9PullchordTRepositoryInstance
						.searchReports(station, shiftName, fromDateTime, toDateTime, filePageable).getContent();
				break;
			default: // Z3
				allRecords = z3PullchordT2RepositoryInstance
						.searchReports(station, shiftName, fromDateTime, toDateTime, filePageable).getContent();
				break;
		}

		if (!allRecords.isEmpty()) {
			// Load image (logo)
			try (InputStream inputStream = new FileInputStream(
					"src/main/resources/static/new_loho_VECV-removebg-preview.png")) {
				byte[] imageBytes = inputStream.readAllBytes();
				int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);

				Drawing<?> drawing = sheet.createDrawingPatriarch();
				CreationHelper helper = workbook.getCreationHelper();
				ClientAnchor anchor = helper.createClientAnchor();
				anchor.setCol1(0);
				anchor.setRow1(0);
				Picture picture = drawing.createPicture(anchor, pictureIdx);
				picture.resize(2);
			} catch (Exception e) {
				System.out.println("Logo not found or error loading: " + e.getMessage());
			}

			// Styles
			CellStyle titleStyle = workbook.createCellStyle();
			Font titleFont = workbook.createFont();
			titleFont.setBold(true);
			titleFont.setFontHeightInPoints((short) 20);
			titleFont.setColor(IndexedColors.BLACK.getIndex());
			titleStyle.setFont(titleFont);
			titleStyle.setAlignment(HorizontalAlignment.CENTER);
			titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			titleStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
			titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CellStyle headerStyle = workbook.createCellStyle();
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 12);
			headerStyle.setFont(headerFont);
			headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerStyle.setBorderBottom(BorderStyle.THIN);
			headerStyle.setBorderTop(BorderStyle.THIN);
			headerStyle.setBorderLeft(BorderStyle.THIN);
			headerStyle.setBorderRight(BorderStyle.THIN);

			CellStyle dataStyle = workbook.createCellStyle();
			dataStyle.setBorderBottom(BorderStyle.THIN);
			dataStyle.setBorderTop(BorderStyle.THIN);
			dataStyle.setBorderLeft(BorderStyle.THIN);
			dataStyle.setBorderRight(BorderStyle.THIN);

			CellStyle dateStyle = workbook.createCellStyle();
			dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
			dateStyle.setBorderBottom(BorderStyle.THIN);
			dateStyle.setBorderTop(BorderStyle.THIN);
			dateStyle.setBorderLeft(BorderStyle.THIN);
			dateStyle.setBorderRight(BorderStyle.THIN);

			Object first = allRecords.get(0);
			Field[] fields = first.getClass().getDeclaredFields();
			for (Field f : fields)
				f.setAccessible(true);

			// Title Row (Row 2)
			Row titleRow = sheet.createRow(2);
			Cell titleCell = titleRow.createCell(0);
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			titleCell.setCellValue("SCADA Report for " + selectedTable + " (Downloaded at: " + timestamp + ")");
			titleCell.setCellStyle(titleStyle);
			sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, fields.length - 1));

			// Header Row (Row 3)
			Row headerRow = sheet.createRow(3);
			for (int i = 0; i < fields.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(fields[i].getName());
				cell.setCellStyle(headerStyle);
			}

			// Data Rows (Row 4 onwards)
			for (int i = 0; i < allRecords.size(); i++) {
				Row dataRow = sheet.createRow(i + 4);
				Object record = allRecords.get(i);
				for (int j = 0; j < fields.length; j++) {
					Cell cell = dataRow.createCell(j);
					try {
						Object value = fields[j].get(record);
						if (value instanceof java.util.Date) {
							cell.setCellValue((java.util.Date) value);
							cell.setCellStyle(dateStyle);
						} else {
							if (value instanceof Number) {
								cell.setCellValue(((Number) value).doubleValue());
							} else {
								cell.setCellValue(value != null ? value.toString() : "");
							}
							cell.setCellStyle(dataStyle);
						}
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}

			// Auto-size columns
			for (int i = 0; i < fields.length; i++) {
				sheet.autoSizeColumn(i);
			}
		}

		// Set response headers
		response.setHeader("Content-Disposition",
				"attachment; filename=\"" + selectedTable.replace(" ", "_") + "_filtered.xlsx\"");
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

		workbook.write(response.getOutputStream());
		workbook.close();
	}
}
