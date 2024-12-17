package ai.holo.wdyt.config.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DataSourceConfig {
    private final Map<String, String> secretProperties;
    private final String dataSourceClass;
    private final String dataSourceUrl;

    public DataSourceConfig(Map<String, String> secretProperties,
                            @Value("${spring.datasource.class}") String dataSourceClass,
                            @Value("${spring.datasource.url}") String dataSourceUrl) {
        this.secretProperties = secretProperties;
        this.dataSourceClass = dataSourceClass;
        this.dataSourceUrl = dataSourceUrl;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(dataSourceClass);
        dataSource.setUrl(dataSourceUrl);
        dataSource.setUsername(secretProperties.get("mysqlDbUser"));
        dataSource.setPassword(secretProperties.get("mysqlDbPassword"));
        return dataSource;
    }
}
