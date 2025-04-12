package org.food.sudaeda.configuration;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.postgresql.xa.PGXADataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "org.food.sudaeda.analytics.repository",
        entityManagerFactoryRef = "analyticsEntityManagerFactory",
        transactionManagerRef = "transactionManager"
)
public class AnalyticsDataSourceConfig {

    @Value("${analytics-datasource.jdbc-url}")
    private String analyticsUrl;

    @Value("${analytics-datasource.username}")
    private String username;

    @Value("${analytics-datasource.password}")
    private String password;

    public Map<String, String> jpaProperties() {
        Map<String, String> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", "none");
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpaProperties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        jpaProperties.put("javax.persistence.transactionType", "JTA");
        return jpaProperties;
    }

    @Bean
    public EntityManagerFactoryBuilder analyticsEntityManagerFactoryBuilder() {
        return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), jpaProperties(), null);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean analyticsEntityManagerFactory(
            @Qualifier("analyticsEntityManagerFactoryBuilder") EntityManagerFactoryBuilder builder,
            @Qualifier("analyticsDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("org.food.sudaeda.analytics.model")
                .persistenceUnit("analytics")
                .properties(jpaProperties())
                .jta(true)
                .build();
    }

    @Bean("analyticsDataSourceProperties")
    public DataSourceProperties analyticsDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("analyticsDataSource")
    public DataSource analyticsDataSource(@Qualifier("analyticsDataSourceProperties") DataSourceProperties dataSourceProperties) {
        PGXADataSource pgxaDataSource = new PGXADataSource();
        pgxaDataSource.setUrl(analyticsUrl);
        pgxaDataSource.setUser(username);
        pgxaDataSource.setPassword(password);

        AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();
        xaDataSource.setXaDataSource(pgxaDataSource);
        xaDataSource.setUniqueResourceName("xa_analytics");
        xaDataSource.setMinPoolSize(5);
        xaDataSource.setMaxPoolSize(20);
        return xaDataSource;
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
