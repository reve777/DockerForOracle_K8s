package com.portfolio.datasource.self;


import java.util.List;

import com.portfolio.uitls.LogUtils;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;

/*
 * QueryExecutionListener 用於攔截 JDBC 操作的工具庫。
 * */
public class ProxyDatasourceQueryExecutionListener implements QueryExecutionListener {
    private final ProxyLogEntryCreator logEntryCreator = new ProxyLogEntryCreator();

    public ProxyDatasourceQueryExecutionListener() {
    	LogUtils.system("Initializing ProxyDatasourceQueryExecutionListener");
    }

    @Override
    public void beforeQuery(ExecutionInfo executionInfo, List<QueryInfo> queryInfoList) {
    }

    /*
	     afterQuery：在查詢執行之後被呼叫，執行以下操作：
		 透過 logEntryCreator 生成查詢的 SQL 語句並記錄到日誌中。
		 同樣地，生成查詢的參數並記錄到日誌中。
		 記錄查詢是否成功執行。
		 將 SQL 語句和參數儲存到 AuditInfo 中，以供後續處理。
     * */
    @Override
    public void afterQuery(ExecutionInfo executionInfo, List<QueryInfo> queryInfoList) {

        String sql = logEntryCreator.getQueriesEntry(queryInfoList);
    	LogUtils.system("SQL : {}", sql);

        String params = logEntryCreator.getParamEntry(executionInfo, queryInfoList);
    	LogUtils.system("Params: : {}", params);
    	LogUtils.system("Success ? {}", executionInfo.isSuccess());

        store(sql, params);
    }

    private void store(String sql, String param) {
//        AuditInfo auditInfo = AuditInfoHolder.getAuditInfo();
//        if (auditInfo == null) {
//            return;
//        }
//        SqlLogEntry entry = new SqlLogEntry(sql, param);
//        auditInfo.addSqlLogEntry(entry);
    }


}