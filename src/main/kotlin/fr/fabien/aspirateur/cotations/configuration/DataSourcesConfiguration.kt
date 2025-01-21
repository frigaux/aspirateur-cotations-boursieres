package fr.fabien.aspirateur.cotations.configuration

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.autoconfigure.batch.BatchDataSource
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import javax.sql.DataSource


@Configuration(proxyBeanMethods = false)
class DataSourcesConfiguration {

    @Bean
    @ConfigurationProperties("job-repository.datasource")
    fun jobRepositorySourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    @BatchDataSource
    fun jobRepositoryDataSource(jobRepositorySourceProperties: DataSourceProperties): DataSource {
        return jobRepositorySourceProperties.initializeDataSourceBuilder().build()
    }


    @Bean
    @ConfigurationProperties("business.datasource")
    fun businessSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    @Primary
    fun businessDataSource(businessSourceProperties: DataSourceProperties): HikariDataSource {
        return businessSourceProperties.initializeDataSourceBuilder().type(HikariDataSource::class.java).build()
    }
}
