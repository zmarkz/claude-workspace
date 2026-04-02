package com.portfolio.service;

import com.portfolio.entity.Portfolio;
import com.portfolio.entity.PortfolioHolding;
import com.portfolio.entity.Stock;
import com.portfolio.exception.BusinessException;
import com.portfolio.exception.ResourceNotFoundException;
import com.portfolio.repository.PortfolioHoldingRepository;
import com.portfolio.repository.PortfolioRepository;
import com.portfolio.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CsvUploadService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioHoldingRepository holdingRepository;
    private final StockRepository stockRepository;

    @CacheEvict(value = "portfolioAnalysis", key = "#portfolioId")
    public UploadResult uploadHoldings(Long portfolioId, MultipartFile file, boolean replaceExisting) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found"));

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        boolean isExcel = filename.endsWith(".xlsx") || filename.endsWith(".xls")
                || "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(file.getContentType())
                || "application/vnd.ms-excel".equals(file.getContentType());

        List<String[]> rows;
        if (isExcel) {
            rows = readExcel(file);
        } else {
            rows = readCsv(file);
        }

        if (rows.isEmpty()) {
            throw new BusinessException("File is empty or could not be parsed");
        }

        // Log first 5 rows for debugging
        for (int r = 0; r < Math.min(5, rows.size()); r++) {
            log.info("Upload row {}: {}", r, Arrays.toString(rows.get(r)));
        }

        // Find the actual header row (look for a row containing "instrument" or "symbol" or "qty")
        int headerIdx = 0;
        for (int r = 0; r < Math.min(10, rows.size()); r++) {
            String joined = String.join(" ", rows.get(r)).toLowerCase();
            if (joined.contains("instrument") || joined.contains("symbol") || joined.contains("stock")
                    || (joined.contains("qty") && (joined.contains("avg") || joined.contains("price")))) {
                headerIdx = r;
                break;
            }
        }

        String[] header = rows.get(headerIdx);
        ColumnMapping mapping = detectColumns(header);
        log.info("Upload: {} rows, headerIdx={}, header={}, mapping={}", rows.size(), headerIdx, Arrays.toString(header), mapping);

        if (replaceExisting) {
            holdingRepository.deleteByPortfolioId(portfolioId);
        }

        int synced = 0, created = 0, skipped = 0;
        List<String> errors = new ArrayList<>();

        for (int i = headerIdx + 1; i < rows.size(); i++) {
            String[] cols = rows.get(i);
            try {
                String symbol = getCol(cols, mapping.symbolIdx);
                if (symbol == null || symbol.isEmpty()) { skipped++; continue; }

                symbol = symbol.toUpperCase().replaceAll("[\"']", "").trim();
                symbol = symbol.replaceAll("\\.(NS|BSE|BO)$", "").replaceAll("-BE$", "");
                if (isHeaderWord(symbol)) { skipped++; continue; }

                BigDecimal quantity = parseNumber(getCol(cols, mapping.qtyIdx));
                BigDecimal avgCost = parseNumber(getCol(cols, mapping.avgCostIdx));

                if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) { skipped++; continue; }
                if (avgCost == null || avgCost.compareTo(BigDecimal.ZERO) <= 0) { skipped++; continue; }

                BigDecimal ltp = mapping.ltpIdx >= 0 ? parseNumber(getCol(cols, mapping.ltpIdx)) : null;

                final String sym = symbol;
                Stock stock = stockRepository.findBySymbol(sym)
                        .orElseGet(() -> stockRepository.save(Stock.builder()
                                .symbol(sym).companyName(sym).exchange("NSE")
                                .currentPrice(BigDecimal.ZERO).build()));

                if (ltp != null && ltp.compareTo(BigDecimal.ZERO) > 0) {
                    stock.setCurrentPrice(ltp);
                    stockRepository.save(stock);
                }

                var existing = holdingRepository.findByPortfolioIdAndStockId(portfolioId, stock.getId());
                if (existing.isPresent() && !replaceExisting) {
                    PortfolioHolding h = existing.get();
                    BigDecimal totalCost = h.getAverageBuyPrice().multiply(h.getQuantity())
                            .add(avgCost.multiply(quantity));
                    BigDecimal newQty = h.getQuantity().add(quantity);
                    h.setAverageBuyPrice(totalCost.divide(newQty, 2, RoundingMode.HALF_UP));
                    h.setQuantity(newQty);
                    holdingRepository.save(h);
                } else {
                    holdingRepository.save(PortfolioHolding.builder()
                            .portfolio(portfolio).stock(stock)
                            .quantity(quantity).averageBuyPrice(avgCost).build());
                    created++;
                }
                synced++;
            } catch (Exception e) {
                String desc = cols.length > 0 ? cols[0] : "row " + i;
                errors.add(desc + ": " + e.getMessage());
                log.warn("Row {} failed: {}", i, e.getMessage());
            }
        }

        log.info("Upload done: synced={}, created={}, skipped={}, errors={}", synced, created, skipped, errors.size());
        return new UploadResult(synced, created, rows.size() - 1, errors);
    }

    private List<String[]> readExcel(MultipartFile file) {
        List<String[]> rows = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (Row row : sheet) {
                List<String> cells = new ArrayList<>();
                for (int c = 0; c < row.getLastCellNum(); c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    cells.add(formatter.formatCellValue(cell).trim());
                }
                // Skip completely empty rows
                if (cells.stream().allMatch(String::isEmpty)) continue;
                rows.add(cells.toArray(new String[0]));
            }
        } catch (Exception e) {
            throw new BusinessException("Failed to read Excel file: " + e.getMessage());
        }
        return rows;
    }

    private List<String[]> readCsv(MultipartFile file) {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String firstLine = reader.readLine();
            if (firstLine == null) return rows;

            char delimiter = count(firstLine, '\t') > count(firstLine, ',') ? '\t' : ',';
            rows.add(splitLine(firstLine, delimiter));

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) rows.add(splitLine(line, delimiter));
            }
        } catch (Exception e) {
            throw new BusinessException("Failed to read CSV: " + e.getMessage());
        }
        return rows;
    }

    private int count(String s, char c) {
        int n = 0;
        for (char ch : s.toCharArray()) if (ch == c) n++;
        return n;
    }

    private String[] splitLine(String line, char delimiter) {
        List<String> cols = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '"') inQuotes = !inQuotes;
            else if (c == delimiter && !inQuotes) { cols.add(current.toString().trim()); current = new StringBuilder(); }
            else current.append(c);
        }
        cols.add(current.toString().trim());
        return cols.toArray(new String[0]);
    }

    private ColumnMapping detectColumns(String[] header) {
        int symbolIdx = 0, qtyIdx = -1, avgCostIdx = -1, ltpIdx = -1;

        // First pass: exact/preferred matches
        for (int i = 0; i < header.length; i++) {
            String raw = header[i].toLowerCase().trim();
            String h = raw.replaceAll("[^a-z0-9 ]", "");

            if (raw.contains("instrument") || raw.equals("symbol") || raw.contains("stock name") || raw.contains("scrip"))
                symbolIdx = i;
            // Zerodha: "Quantity Available" — prefer over "Quantity Pledged" etc.
            else if (raw.equals("qty.") || raw.equals("qty") || raw.equals("quantity") || raw.contains("quantity available") || raw.equals("shares"))
                qtyIdx = i;
            else if (raw.contains("avg") || raw.contains("average price") || raw.contains("buy avg") || raw.contains("cost price"))
                avgCostIdx = i;
            else if (raw.equals("ltp") || raw.contains("last traded") || raw.contains("previous closing") || raw.contains("closing price") || raw.contains("close price"))
                ltpIdx = i;
        }

        // Fallback pass: looser matching if we missed qty or avg
        if (qtyIdx < 0 || avgCostIdx < 0) {
            for (int i = 0; i < header.length; i++) {
                String h = header[i].toLowerCase().trim();
                if (qtyIdx < 0 && h.contains("quant") && !h.contains("pledg") && !h.contains("discrep") && !h.contains("long term"))
                    qtyIdx = i;
                if (avgCostIdx < 0 && (h.contains("avg") || h.contains("average") || h.contains("buy price")))
                    avgCostIdx = i;
            }
        }

        // Final fallback to positional
        if (qtyIdx < 0) qtyIdx = 1;
        if (avgCostIdx < 0) avgCostIdx = 2;

        return new ColumnMapping(symbolIdx, qtyIdx, avgCostIdx, ltpIdx);
    }

    private String getCol(String[] cols, int idx) {
        if (idx < 0 || idx >= cols.length) return null;
        return cols[idx].replaceAll("[\"']", "").trim();
    }

    private BigDecimal parseNumber(String val) {
        if (val == null) return null;
        val = val.trim().replaceAll("[₹$€,% ]", "").replaceAll("[^0-9.\\-]", "");
        if (val.isEmpty()) return null;
        try { return new BigDecimal(val); }
        catch (NumberFormatException e) { return null; }
    }

    private boolean isHeaderWord(String s) {
        String l = s.toLowerCase();
        return l.equals("instrument") || l.equals("symbol") || l.equals("stock") || l.equals("scrip");
    }

    private record ColumnMapping(int symbolIdx, int qtyIdx, int avgCostIdx, int ltpIdx) {}
    public record UploadResult(int synced, int created, int total, List<String> errors) {}
}
