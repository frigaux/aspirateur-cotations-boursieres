package fr.fabien.aspirateur.cotations.configuration.dummy

import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

//@Configuration
class ConfigurationDataSource {

//    @Bean
    fun dataSource(): DataSource {
        return DataSourceFactice()
    }

//    @Bean
    fun transactionManager(dataSource: DataSource): PlatformTransactionManager {
        return PlatformTransactionManagerFactice()
    }
}