package fr.fabien.aspirateur.cotations.configuration.dummy

import org.springframework.beans.factory.BeanNameAware
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus

class PlatformTransactionManagerFactice : PlatformTransactionManager, BeanNameAware {
    override fun getTransaction(definition: TransactionDefinition?): TransactionStatus {
        TODO("Not yet implemented")
    }

    override fun commit(status: TransactionStatus) {
        TODO("Not yet implemented")
    }

    override fun rollback(status: TransactionStatus) {
        TODO("Not yet implemented")
    }

    override fun setBeanName(name: String) {
        TODO("Not yet implemented")
    }
}