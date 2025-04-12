package org.food.sudaeda.configuration;

import com.zaxxer.hikari.HikariDataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
        basePackages = "org.food.sudaeda.analytics.repository",
        entityManagerFactoryRef = "analyticsEntityManagerFactory",
        transactionManagerRef = "analyticsTransactionManager"
)
public class AnalyticsDataSourceConfig {

    @Bean
    public EntityManagerFactoryBuilder analyticsEntityManagerFactoryBuilder() {
        return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), new HashMap<>(), null);
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.analytics-datasource")
    public DataSource analyticsDataSource() {
        return DataSourceBuilder
                .create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean analyticsEntityManagerFactory(
            @Qualifier("analyticsEntityManagerFactoryBuilder") EntityManagerFactoryBuilder builder,
            @Qualifier("analyticsDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("org.food.sudaeda.analytics.model")
                .persistenceUnit("analytics")
                .build();
    }

    @Bean
    public PlatformTransactionManager analyticsTransactionManager(
            @Qualifier("analyticsEntityManagerFactory") LocalContainerEntityManagerFactoryBean analyticsEntityManagerFactory) {
        return new JpaTransactionManager(analyticsEntityManagerFactory.getObject());
    }

    @Bean
    public SpringLiquibase analyticsLiquibase(
            @Qualifier("analyticsDataSource") DataSource dataSource,
            @Value("${liquibase.analytics.change-log}") String changeLog) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLog);
        liquibase.setLiquibaseSchema("public");
        liquibase.setShouldRun(true);
        return liquibase;
    }
}
