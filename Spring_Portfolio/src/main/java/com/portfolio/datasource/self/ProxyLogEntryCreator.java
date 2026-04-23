package com.portfolio.datasource.self;


import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.StatementType;
import net.ttddyy.dsproxy.listener.logging.AbstractQueryLogEntryCreator;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;

/*
 * 繼承自 AbstractQueryLogEntryCreator，主要負責生成資料庫查詢的日誌條目。
 * 這個類別被用於記錄 SQL 查詢和其對應的參數，以便於後續的審計和除錯。
 * */
public class ProxyLogEntryCreator extends AbstractQueryLogEntryCreator {

    @Override
    public String getLogEntry(ExecutionInfo executionInfo, List<QueryInfo> list, boolean b, boolean b1, boolean b2) {
        return "";
    }

    public String getQueriesEntry(List<QueryInfo> queryInfoList) {
        StringBuilder stringBuilder = new StringBuilder();

        /*
         * 對於每個 QueryInfo，將其查詢語句附加到 StringBuilder。在每個查詢語句之間添加逗號。
         * */
        for (QueryInfo queryInfo : queryInfoList) {
            stringBuilder.append(queryInfo.getQuery());
            stringBuilder.append(",");
        }

        // chompIfEndWith 方法移除結果字串末尾的多餘逗號。
        this.chompIfEndWith(stringBuilder, ',');

        return stringBuilder.toString();
    }

    /*
     * 生成一個字串，包含所有查詢的參數資訊
     * */
    public String getParamEntry(ExecutionInfo executionInfo, List<QueryInfo> queryInfoList) {
        boolean isPrepared = executionInfo.getStatementType() == StatementType.PREPARED;
        StringBuilder stringBuilder = new StringBuilder();

        for (QueryInfo queryInfo : queryInfoList) {
            for (List<ParameterSetOperation> parameters : queryInfo.getParametersList()) {
                SortedMap<String, String> paramMap = this.getParametersToDisplay(parameters);
                if (isPrepared) {
                    this.writeParamsForSinglePreparedEntry(stringBuilder, paramMap);
                } else {
                    this.writeParamsForSingleCallableEntry(stringBuilder, paramMap);
                }
            }
        }

        chompIfEndWith(stringBuilder, ',');

        return stringBuilder.toString();
    }

    // 將單個預備語句的參數資訊附加到提供的 StringBuilder。
    private void writeParamsForSinglePreparedEntry(StringBuilder stringBuilder, SortedMap<String, String> paramMap) {
        for (Map.Entry<String, String> paramEntry : paramMap.entrySet()) {
            stringBuilder.append(paramEntry.getValue());
            stringBuilder.append(",");
        }

        chompIfEndWith(stringBuilder, ',');
    }

    // 將單個可呼叫語句的參數資訊附加到提供的 StringBuilder。
    private void writeParamsForSingleCallableEntry(StringBuilder stringBuilder, SortedMap<String, String> paramMap) {
        for (Map.Entry<String, String> paramEntry : paramMap.entrySet()) {
            stringBuilder.append(paramEntry.getKey());
            stringBuilder.append("=");
            stringBuilder.append(paramEntry.getValue());
            stringBuilder.append(",");
        }

        chompIfEndWith(stringBuilder, ',');
    }
}