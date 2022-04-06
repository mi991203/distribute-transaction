> distribute-transaction模块是模拟基于rabbitmq消息流转实现外卖订单流程。其中common-module模块负责公共代码抽取以及封装一个分布式事务框架。

> 所需SQL在sql文件夹下，消息流转顺序是：order-service-->restaurant-->order-service-->deliveryman-service-->order-service-->settlement-service -->order-service-->reward-service

> 因为涉及到失败消息定时重发，所以涉及到分布式锁，避免服务多节点冲突问题，使用redisson框架。

> 所谓分布式框架，并不是所有流程流转前后所有细节需要保持一致，而是在某一个特定的状态需要保持一致，比如说订单状态.