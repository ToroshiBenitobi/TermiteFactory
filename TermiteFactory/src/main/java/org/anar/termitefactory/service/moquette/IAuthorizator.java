package org.anar.termitefactory.service.moquette;

import io.moquette.broker.security.IAuthorizatorPolicy;
import io.moquette.broker.subscriptions.Topic;
import org.springframework.stereotype.Component;

@Component
public class IAuthorizator implements IAuthorizatorPolicy {
    @Override
    public boolean canWrite(Topic topic, String s, String s1) {
        return true;
    }

    @Override
    public boolean canRead(Topic topic, String s, String s1) {
        return true;
    }
}
