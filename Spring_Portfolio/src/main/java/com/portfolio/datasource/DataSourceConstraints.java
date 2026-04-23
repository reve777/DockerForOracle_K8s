package com.portfolio.datasource;

public interface DataSourceConstraints {
    String HIBERNATE_GENERATE_SQL = "hibernate.hbm2ddl.auto";
    String HIBERNATE_DIALECT = "hibernate.dialect";
    String HIBERNATE_SHOW_SQL = "hibernate.show_sql";
    String HIBERNATE_FORMAT_SQL = "hibernate.format_sql";

    String PROXY_DATA_SOURCE_NAME = "proxyDataSource";
    String SELF_ENTITIES_PACKAGE = "com.portfolio.entity";
}