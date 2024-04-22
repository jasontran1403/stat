package com.stat.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDLbls;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumFmt;

import com.stat.user.Statistic;

public class ChartGenerator {
	public byte[] generatePieChart(List<Statistic> listData, double totalBalance) throws IOException {
		try (XSSFWorkbook wb = new XSSFWorkbook()) {
			XSSFSheet sheet = wb.createSheet("Báo cáo thu chi");

			double[] customValues;
			if (listData.size() > 0) {
				// Sum of values for type 0
				double totalFixed = listData.stream()
						.mapToDouble(Statistic::getFixedFee).sum();

				// Sum of values for type 1
				double totalIBShareAlex = listData.stream()
						.mapToDouble(Statistic::getShareAlex).sum();
				
				double totalIBShareHhl = listData.stream()
						.mapToDouble(Statistic::getShareHhl).sum();
				
				double totalIBShareQueen = listData.stream()
						.mapToDouble(Statistic::getShareQueen).sum();
				
				double totalIBSharePixiu = listData.stream()
						.mapToDouble(Statistic::getSharePixiu).sum();

				// Sum of values for type 1
				double totalOtherFee = listData.stream()
						.mapToDouble(Statistic::getOtherFee).sum();

				// Remaining value for customValues[2]
				double remainingValue = totalBalance - totalFixed - totalIBShareAlex - totalIBShareQueen - totalIBShareHhl - totalIBSharePixiu - totalOtherFee;

				// Set customValues
				double totalShareIB = totalIBShareAlex + totalIBShareQueen + totalIBShareHhl + totalIBSharePixiu;
				customValues = new double[] { totalFixed, totalShareIB, totalOtherFee, remainingValue };
			} else {
				customValues = new double[] { 0, 0, 0, 0 };
			}

			final int NUM_OF_ROWS = 4;
			final int NUM_OF_COLUMNS = 2;

			// Create a row and put some cells in it. Rows are 0 based.
			Row row;
			Cell cell;

			for (int rowIndex = 0; rowIndex < NUM_OF_ROWS; rowIndex++) {
				row = sheet.createRow((short) rowIndex);

				// Cột A: Tiêu đề
				cell = row.createCell(0);
				if (rowIndex == 0) {
					cell.setCellValue("Chi phí cố định");
				} else if (rowIndex == 1) {
					cell.setCellValue("Chia IB các nhánh");
				} else if (rowIndex == 2) {
					cell.setCellValue("Chi phí khác");
				} else if (rowIndex == 3) {
					cell.setCellValue("Số dư còn lại");
				}

				// Cột B: Giá trị của customValues
				cell = row.createCell(1);
				cell.setCellValue(customValues[rowIndex]);
			}

			// Set độ dài cho các cột
			sheet.setColumnWidth(0, 5000); // Cột A
			sheet.setColumnWidth(1, 5000); // Cột B
			sheet.setColumnWidth(2, 5000); // Cột C
			sheet.setColumnWidth(3, 5000); // Cột B
			sheet.setColumnWidth(4, 5000); // Cột B
			sheet.setColumnWidth(5, 5000); // Cột B
			sheet.setColumnWidth(6, 5000); // Cột B
			sheet.setColumnWidth(7, 5000); // Cột B

			// Set canh giữa cho tất cả các ô
			CellStyle style = wb.createCellStyle();
			style.setAlignment(HorizontalAlignment.CENTER);
			style.setVerticalAlignment(VerticalAlignment.CENTER);

			// Bước 1: Tạo dữ liệu của biểu đồ trước (từ dòng 18 đến 30)
			double totalAmount = 0;

			for (int r = 0; r < listData.size(); r++) {
				row = sheet.createRow(r + 18);

				// Bạn có thể sử dụng ngày tháng cụ thể thay vì "01/01/2024"
				cell = row.createCell(0);

				long timestampInSeconds = listData.get(r).getTime();
				Instant instant = Instant.ofEpochSecond(timestampInSeconds);
				LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
				String formattedDate = localDateTime.format(formatter);

				cell.setCellValue(formattedDate);
				cell.setCellStyle(style); // Áp dụetng canh giữa
				 // Áp dụng canh giữa
				cell = row.createCell(1);
				cell.setCellValue(listData.get(r).getTotalIb());
				cell.setCellStyle(style); // Áp dụetng canh giữa
				cell = row.createCell(2);
				cell.setCellValue(listData.get(r).getFixedFee());
				cell.setCellStyle(style);
				
				cell = row.createCell(3);
				cell.setCellValue(listData.get(r).getShareAlex());
				cell.setCellStyle(style);
				
				cell = row.createCell(4);
				cell.setCellValue(listData.get(r).getShareHhl());
				cell.setCellStyle(style);
				
				cell = row.createCell(5);
				cell.setCellValue(listData.get(r).getShareQueen());
				cell.setCellStyle(style);
				
				cell = row.createCell(6);
				cell.setCellValue(listData.get(r).getSharePixiu());
				cell.setCellStyle(style);
				
				cell = row.createCell(7);
				cell.setCellValue(listData.get(r).getOtherFee());
				cell.setCellStyle(style);

				// Sau đó, nếu bạn muốn định dạng hiển thị của cell, bạn có thể sử dụng
				// DataFormat
				CellStyle styleNumber = wb.createCellStyle();
				styleNumber.setDataFormat(wb.createDataFormat().getFormat("#.##0,00"));
				cell.setCellStyle(styleNumber);
				cell.setCellStyle(style); // Áp dụng canh giữa
			}

			// Bước 2: Tạo dữ liệu của bảng sau cùng (từ dòng 17 trở đi)
			row = sheet.createRow(17);
			Cell cell0 = row.createCell(0);
			Cell cell1 = row.createCell(1);
			Cell cell2 = row.createCell(2);
			Cell cell3 = row.createCell(3);
			Cell cell4 = row.createCell(4);
			Cell cell5 = row.createCell(5);
			Cell cell6 = row.createCell(6);
			Cell cell7 = row.createCell(7);

			// Set cell values
			cell0.setCellValue("Thời gian");
			cell1.setCellValue("Tổng IB");
			cell2.setCellValue("Chi phí cố định");
			cell3.setCellValue("Chia IB Alex");
			cell4.setCellValue("Chia IB HHL");
			cell5.setCellValue("Chia IB Queen");
			cell6.setCellValue("Chia IB PixiuGroup");
			cell7.setCellValue("Chi phí khác");
			
			sheet.setAutoFilter(CellRangeAddress.valueOf("A18:A" + (listData.size()+18)));

			Row totalRow = sheet.createRow(listData.size() + 18);
			Cell totalLabelCell = totalRow.createCell(0);
			Cell totalAmountCellIB = totalRow.createCell(1);
			Cell totalAmountCellFixedFee = totalRow.createCell(2);
			Cell totalAmountCellIBShareAlex = totalRow.createCell(3);
			Cell totalAmountCellIBShareHhl = totalRow.createCell(4);
			Cell totalAmountCellIBShareQueen = totalRow.createCell(5);
			Cell totalAmountCellIBSharePixiu = totalRow.createCell(6);
			Cell totalAmountCellOther = totalRow.createCell(7);

			totalLabelCell.setCellValue("Tổng cộng");

			String formulaTotalIB = "SUBTOTAL(109,B19:B" + (listData.size() + 18) + ")";
			totalAmountCellIB.setCellFormula(formulaTotalIB);
			
			String formulaFixedFee = "SUBTOTAL(109,C19:C" + (listData.size() + 18) + ")";
			totalAmountCellFixedFee.setCellFormula(formulaFixedFee);
			
			String formulaShareAlex = "SUBTOTAL(109,D19:D" + (listData.size() + 18) + ")";
			totalAmountCellIBShareAlex.setCellFormula(formulaShareAlex);
			
			String formulaShareHhl = "SUBTOTAL(109,E19:E" + (listData.size() + 18) + ")";
			totalAmountCellIBShareHhl.setCellFormula(formulaShareHhl);
			
			String formulaShareQueen = "SUBTOTAL(109,F19:F" + (listData.size() + 18) + ")";
			totalAmountCellIBShareQueen.setCellFormula(formulaShareQueen);
			
			String formulaSharePixiu = "SUBTOTAL(109,G19:G" + (listData.size() + 18) + ")";
			totalAmountCellIBSharePixiu.setCellFormula(formulaSharePixiu);
			
			String formulaOther = "SUBTOTAL(109,H19:H" + (listData.size() + 18) + ")";
			totalAmountCellOther.setCellFormula(formulaOther);

			CellStyle styleForSum = wb.createCellStyle();
			styleForSum.setAlignment(HorizontalAlignment.CENTER);
			styleForSum.setVerticalAlignment(VerticalAlignment.CENTER);
			
			Font fontForSum = wb.createFont();
			fontForSum.setBold(true);
			styleForSum.setFont(fontForSum);

			totalLabelCell.setCellStyle(styleForSum);
			totalAmountCellIB.setCellStyle(styleForSum);
			totalAmountCellFixedFee.setCellStyle(styleForSum);
			totalAmountCellIBShareAlex.setCellStyle(styleForSum);
			totalAmountCellIBShareHhl.setCellStyle(styleForSum);
			totalAmountCellIBShareQueen.setCellStyle(styleForSum);
			totalAmountCellIBSharePixiu.setCellStyle(styleForSum);
			totalAmountCellOther.setCellStyle(styleForSum);

			// Create a cell style for center alignment, yellow background, and bold font
			CellStyle styleForHeader = wb.createCellStyle();
			styleForHeader.setAlignment(HorizontalAlignment.CENTER);
			styleForHeader.setVerticalAlignment(VerticalAlignment.CENTER);
			styleForHeader.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
			styleForHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			Font font = wb.createFont();
			font.setBold(true);
			styleForHeader.setFont(font);

			// Apply the style to the cells
			cell0.setCellStyle(styleForHeader);
			cell1.setCellStyle(styleForHeader);
			cell2.setCellStyle(styleForHeader);
			cell3.setCellStyle(styleForHeader);
			cell4.setCellStyle(styleForHeader);
			cell5.setCellStyle(styleForHeader);
			cell6.setCellStyle(styleForHeader);
			cell7.setCellStyle(styleForHeader);

			XSSFDrawing drawing = sheet.createDrawingPatriarch();
			ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 0, 8, 16);

			XSSFChart chart = drawing.createChart(anchor);

			chart.setTitleText("Tổng IB đã nhận: " + totalBalance + "$\nSố dư hiện tại:" + customValues[3]);
			chart.setTitleOverlay(false);
			XDDFChartLegend legend = chart.getOrAddLegend();
			legend.setPosition(LegendPosition.BOTTOM);

			XDDFDataSource<String> cat = XDDFDataSourcesFactory.fromStringCellRange(sheet,
					new CellRangeAddress(0, NUM_OF_ROWS - 1, 0, 0));
			XDDFNumericalDataSource<Double> val = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
					new CellRangeAddress(0, NUM_OF_ROWS - 1, 1, 1));

			XDDFChartData data = chart.createData(ChartTypes.PIE, null, null);
			data.setVaryColors(true);
			data.addSeries(cat, val);
			chart.plot(data);

			// Format the labels
			if (!chart.getCTChart().getPlotArea().getPieChartArray(0).getSerArray(0).isSetDLbls()) {
				chart.getCTChart().getPlotArea().getPieChartArray(0).getSerArray(0).addNewDLbls();
			}
			CTDLbls dLbls = chart.getCTChart().getPlotArea().getPieChartArray(0).getSerArray(0).getDLbls();
			dLbls.addNewShowVal().setVal(true);
			dLbls.addNewShowSerName().setVal(false);
			dLbls.addNewShowCatName().setVal(false);
			dLbls.addNewShowPercent().setVal(false);
			dLbls.addNewShowLegendKey().setVal(false);
			CTNumFmt addNewNumFmt = dLbls.addNewNumFmt();
			addNewNumFmt.setFormatCode("0.00");
			// Set false to not follow source format
			addNewNumFmt.setSourceLinked(false);

			// Write the output to a ByteArrayOutputStream
			try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
				wb.write(byteArrayOutputStream);
				return byteArrayOutputStream.toByteArray();
			}
		}
	}

	private static String formatIfNumber(String input) {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setGroupingSeparator('.');
		symbols.setDecimalSeparator(',');

		// Tạo DecimalFormat với ký hiệu mới
		DecimalFormat decimalFormat = new DecimalFormat("#.##", symbols);
		decimalFormat.setGroupingUsed(true);
		decimalFormat.setGroupingSize(3);

		try {
			double number = Double.parseDouble(input);
			return decimalFormat.format(number);
		} catch (NumberFormatException e) {
			return input;
		}
	}

}
