package com.jendouba.agroconnect;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;
import io.dropwizard.db.DataSourceFactory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Configuration class for AgroConnect.
 * Holds database settings (JDBC URL, username, password, pool settings, etc.).
 */

public class AgroConnectConfig extends Configuration {
    @Valid
    @NotNull
    @JsonProperty("database")

    private DataSourceFactory database= new DataSourceFactory();
    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory( ){
        return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.database = dataSourceFactory;
    }

}
