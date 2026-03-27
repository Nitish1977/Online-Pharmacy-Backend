package com.pharmacy.admin.config;



import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
public class JpaConfig {

    /**
     * Manually registers the EntityManagerFactoryBuilder bean.
     *
     * This is required because AdminServiceApplication excludes
     * HibernateJpaAutoConfiguration (to prevent Spring Boot from
     * auto-configuring a single datasource), which also means the
     * EntityManagerFactoryBuilder bean is never registered automatically.
     * We register it here so both CatalogDataSourceConfig and
     * OrdersDataSourceConfig can inject it normally.
     */
    @Bean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);
        return new EntityManagerFactoryBuilder(vendorAdapter, new java.util.HashMap<>(), null);
    }
}