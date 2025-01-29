package fr.fabien.aspirateur.cotations.configuration

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
class ConfigurationDataSourceBusiness {

    @Bean
    @Primary
    @ConfigurationProperties("business.datasource")
    fun dataSourcePropertiesBusiness(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    @Primary
    @ConfigurationProperties("business.datasource.configuration")
    fun dataSourceBusiness(@Qualifier("dataSourcePropertiesBusiness") properties: DataSourceProperties): HikariDataSource {
        return properties.initializeDataSourceBuilder().type(HikariDataSource::class.java).build()
    }
}
