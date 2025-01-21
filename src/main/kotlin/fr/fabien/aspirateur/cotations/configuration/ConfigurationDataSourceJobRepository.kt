package fr.fabien.aspirateur.cotations.configuration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.batch.BatchDataSource
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

// https://docs.spring.io/spring-boot/how-to/data-access.html#howto.data-access.configure-two-datasources
@Configuration(proxyBeanMethods = false)
class ConfigurationDataSourceJobRepository {

    @Qualifier("jobRepositorySourceProperties")
    @Bean(defaultCandidate = false)
    @ConfigurationProperties("job-repository.datasource")
    fun jobRepositorySourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean(defaultCandidate = false)
    @ConfigurationProperties("job-repository.datasource.configuration")
    @BatchDataSource
    fun jobRepositoryDataSource(@Qualifier("jobRepositorySourceProperties") jobRepositorySourceProperties: DataSourceProperties): DataSource {
        return jobRepositorySourceProperties.initializeDataSourceBuilder().build()
    }
}
