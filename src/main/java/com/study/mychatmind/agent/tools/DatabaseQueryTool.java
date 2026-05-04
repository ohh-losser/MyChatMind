package com.study.mychatmind.agent.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库查询工具 - 可选工具（按 Agent 配置绑定）
 *
 * 让 AI 能够查询数据库，仅支持 SELECT 语句
 */
@Component
@Slf4j
public class DatabaseQueryTool implements Tool {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseQueryTool(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String getName() {
        return "databaseQuery";
    }

    @Override
    public String getDescription() {
        return "一个用于执行数据库查询操作的工具，主要用于从 PostgreSQL 中读取数据";
    }

    @Override
    public ToolCallback toToolCallback() {
        return MethodToolCallbackProvider.builder()
                .toolObjects(this)
                .build()
                .getToolCallbacks()[0];
    }

    /**
     * 执行 SQL 查询（仅支持 SELECT）
     */
    @org.springframework.ai.tool.annotation.Tool(
            name = "databaseQuery",
            description = "用于在 PostgreSQL 中执行只读查询（SELECT）。接收由模型生成的查询语句，并返回结构化数据结果。该工具仅用于检索数据，严禁任何写入或修改数据库的语句。"
    )
    public String query(String sql) {
        try {
            // 安全检查：只允许 SELECT 查询
            String trimmedSql = sql.trim().toUpperCase();
            if (!trimmedSql.startsWith("SELECT")) {
                log.warn("拒绝执行非 SELECT 查询: {}", sql);
                return "错误：仅支持 SELECT 查询语句";
            }

            // 执行查询并格式化结果
            List<String> rows = jdbcTemplate.query(sql, (ResultSet rs) -> {
                List<String> resultRows = new ArrayList<>();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                // 获取列名
                List<String> columnNames = new ArrayList<>();
                List<Integer> columnWidths = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    columnNames.add(columnName);
                    columnWidths.add(columnName.length());
                }

                // 收集数据行
                List<List<String>> dataRows = new ArrayList<>();
                while (rs.next()) {
                    List<String> rowData = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        Object value = rs.getObject(i);
                        String valueStr = value == null ? "NULL" : value.toString();
                        rowData.add(valueStr);
                        if (valueStr.length() > columnWidths.get(i - 1)) {
                            columnWidths.set(i - 1, valueStr.length());
                        }
                    }
                    dataRows.add(rowData);
                }

                // 格式化表头
                StringBuilder header = new StringBuilder("| ");
                for (int i = 0; i < columnCount; i++) {
                    header.append(String.format("%-" + columnWidths.get(i) + "s", columnNames.get(i))).append(" | ");
                }
                resultRows.add(header.toString());

                // 分隔线
                StringBuilder separator = new StringBuilder("|");
                for (int i = 0; i < columnCount; i++) {
                    separator.append("-".repeat(columnWidths.get(i) + 2)).append("|");
                }
                resultRows.add(separator.toString());

                // 数据行
                if (dataRows.isEmpty()) {
                    resultRows.add("| (无数据) |");
                } else {
                    for (List<String> rowData : dataRows) {
                        StringBuilder row = new StringBuilder("| ");
                        for (int i = 0; i < columnCount; i++) {
                            row.append(String.format("%-" + columnWidths.get(i) + "s", rowData.get(i))).append(" | ");
                        }
                        resultRows.add(row.toString());
                    }
                }

                return resultRows;
            });

            log.info("SQL 查询成功, 返回 {} 行", rows.size() - 2);
            return "查询结果:\n" + String.join("\n", rows);

        } catch (Exception e) {
            log.error("SQL 查询失败: {}", e.getMessage());
            return "错误：查询失败 - " + e.getMessage();
        }
    }

    @Override
    public ToolType getType() {
        return ToolType.OPTIONAL;
    }
}
