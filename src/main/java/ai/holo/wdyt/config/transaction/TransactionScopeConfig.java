package ai.holo.wdyt.config.transaction;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.SimpleTransactionScope;

@Configuration
public class TransactionScopeConfig implements BeanFactoryPostProcessor {

    public static final String TRANSACTION_SCOPE_NAME = "transaction";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.registerScope(TRANSACTION_SCOPE_NAME, new SimpleTransactionScope());
    }
}
