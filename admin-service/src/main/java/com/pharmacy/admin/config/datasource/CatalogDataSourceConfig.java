package com.pharmacy.admin.config.datasource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.pharmacy.admin.repository.catalog",
    entityManagerFactoryRef = "catalogEntityManagerFactory",
    transactionManagerRef = "catalogTransactionManager"
)
public class CatalogDataSourceConfig {

    @Bean(name = "catalogDataSourceProperties")
    @ConfigurationProperties("catalog.datasource")
    public DataSourceProperties catalogDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "catalogDataSource")
    public DataSource catalogDataSource(
            @Qualifier("catalogDataSourceProperties") DataSourceProperties props) {
        Objects.requireNonNull(props, "DataSourceProperties must not be null");
        return props.initializeDataSourceBuilder().build();
    }

    @Bean(name = "catalogEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean catalogEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("catalogDataSource") DataSource dataSource) {
        Objects.requireNonNull(builder, "EntityManagerFactoryBuilder must not be null");
        Objects.requireNonNull(dataSource, "DataSource must not be null");
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        properties.put("hibernate.show_sql", "true");
        return builder
                .dataSource(dataSource)
                .packages("com.pharmacy.admin.entity.catalog")
                .persistenceUnit("catalog")
                .properties(properties)
                .build();
    }

    @Bean(name = "catalogTransactionManager")
    public PlatformTransactionManager catalogTransactionManager(
            @Qualifier("catalogEntityManagerFactory") LocalContainerEntityManagerFactoryBean factory) {
        Objects.requireNonNull(factory, "EntityManagerFactory must not be null");
        return new JpaTransactionManager(factory.getObject());
    }
}
