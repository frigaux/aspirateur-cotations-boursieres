package fr.fabien.dummy

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
class ConfigurationDataSource

@Bean
fun dataSource(): DataSource {
    return DummyDataSource()
}

@Bean
fun transactionManager(dataSource: DataSource): PlatformTransactionManager {
    return DummyPlatformTransactionManager()
}