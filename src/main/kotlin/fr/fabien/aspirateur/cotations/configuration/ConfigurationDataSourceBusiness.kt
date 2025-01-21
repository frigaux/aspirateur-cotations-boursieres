package fr.fabien.aspirateur.cotations.configuration

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class ConfigurationDataSourceBusiness {

    @Bean
    @Primary
    @ConfigurationProperties("business.datasource")
    fun businessSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    @Primary
    @ConfigurationProperties("business.datasource.configuration")
    fun businessDataSource(businessSourceProperties: DataSourceProperties): HikariDataSource {
        return businessSourceProperties.initializeDataSourceBuilder().type(HikariDataSource::class.java).build()
    }
}
