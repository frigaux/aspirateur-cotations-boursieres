package fr.fabien.aspirateur.cotations.configuration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.batch.BatchDataSource
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import javax.sql.DataSource

// https://docs.spring.io/spring-boot/how-to/data-access.html#howto.data-access.configure-two-datasources
@Configuration(proxyBeanMethods = false)
@Profile("!test")
class ConfigurationDataSourceJobRepository {

    @Bean(defaultCandidate = false)
    @ConfigurationProperties("job-repository.datasource")
    fun dataSourcePropertiesJobRepository(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean(defaultCandidate = false)
    @ConfigurationProperties("job-repository.datasource.configuration")
    @BatchDataSource
    fun dataSourceJobRepository(@Qualifier("dataSourcePropertiesJobRepository") properties: DataSourceProperties): DataSource {
        return properties.initializeDataSourceBuilder().build()
    }
}
