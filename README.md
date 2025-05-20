# Spring boot 3 + ActiveMQ + XA-Transactions + Durable Subscription to JMS Topic

Currently, I can't get XA-Transactions AND Durable Subscription working at the same time.
I tried `atomikos` and `narayana` as XA-Transaction managers, but both failed to work with ActiveMQ when durable subscription is enabled.
Then the application tries to connect more than once to activemq with the same client id, and this restricts activemq.

* When only XA-Transactions are enabled, without durable subscription enabled, the application works fine (no client-id is set at the connection).
* When only Durable Subscription is enabled, without a JTA-Transaction Manager, the application works fine (on spring boot 3.3).
  * But on Spring Boot 3.4 or 3.5 even this fails with `Client: <my-unique-client-id> already connected from tcp://localhost:<some-port>`.
* When using Durable Subscription AND Atomikos Transaction Manager, the application "works" but complaining periodically every couple of seconds the following error:
```
2025-05-20T11:44:05.741+02:00 ERROR 2748380 --- [demo] [     Atomikos:3] c.a.icatch.imp.RecoveryDomainService     : Error in getting XA resource

com.atomikos.datasource.ResourceException: Error in getting XA resource
        at com.atomikos.datasource.xa.jms.JmsTransactionalResource.refreshXAConnection(JmsTransactionalResource.java:80) ~[transactions-jta-6.0.0-jakarta.jar:na]
        at com.atomikos.datasource.xa.XATransactionalResource.refreshXAResource(XATransactionalResource.java:389) ~[transactions-jta-6.0.0-jakarta.jar:na]
        at com.atomikos.datasource.xa.XATransactionalResource.getXAResource(XATransactionalResource.java:226) ~[transactions-jta-6.0.0-jakarta.jar:na]
        at com.atomikos.datasource.xa.XATransactionalResource.recover(XATransactionalResource.java:373) ~[transactions-jta-6.0.0-jakarta.jar:na]
        at com.atomikos.icatch.imp.RecoveryDomainService.performRecovery(RecoveryDomainService.java:82) ~[transactions-6.0.0.jar:na]
        at com.atomikos.icatch.imp.RecoveryDomainService$1.alarm(RecoveryDomainService.java:57) ~[transactions-6.0.0.jar:na]
        at com.atomikos.timing.PooledAlarmTimer.notifyListeners(PooledAlarmTimer.java:101) ~[atomikos-util-6.0.0.jar:na]
        at com.atomikos.timing.PooledAlarmTimer.run(PooledAlarmTimer.java:88) ~[atomikos-util-6.0.0.jar:na]
        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144) ~[na:na]
        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642) ~[na:na]
        at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]
Caused by: jakarta.jms.InvalidClientIDException: Broker: localhost - Client: my-unique-client-id already connected from tcp://192.168.48.1:45798
        at org.apache.activemq.broker.region.RegionBroker.addConnection(RegionBroker.java:265) ~[na:na]
        at org.apache.activemq.broker.jmx.ManagedRegionBroker.addConnection(ManagedRegionBroker.java:230) ~[na:na]
        at org.apache.activemq.broker.BrokerFilter.addConnection(BrokerFilter.java:99) ~[na:na]
        at org.apache.activemq.advisory.AdvisoryBroker.addConnection(AdvisoryBroker.java:119) ~[na:na]
        at org.apache.activemq.broker.BrokerFilter.addConnection(BrokerFilter.java:99) ~[na:na]
        at org.apache.activemq.broker.BrokerFilter.addConnection(BrokerFilter.java:99) ~[na:na]
        at org.apache.activemq.broker.BrokerFilter.addConnection(BrokerFilter.java:99) ~[na:na]
        at org.apache.activemq.broker.TransportConnection.processAddConnection(TransportConnection.java:854) ~[na:na]
        at org.apache.activemq.broker.jmx.ManagedTransportConnection.processAddConnection(ManagedTransportConnection.java:77) ~[na:na]
        at org.apache.activemq.command.ConnectionInfo.visit(ConnectionInfo.java:139) ~[activemq-client-6.1.6.jar:6.1.6]
        at org.apache.activemq.broker.TransportConnection.service(TransportConnection.java:337) ~[na:na]
        at org.apache.activemq.broker.TransportConnection$1.onCommand(TransportConnection.java:201) ~[na:na]
        at org.apache.activemq.transport.MutexTransport.onCommand(MutexTransport.java:50) ~[activemq-client-6.1.6.jar:6.1.6]
        at org.apache.activemq.transport.WireFormatNegotiator.onCommand(WireFormatNegotiator.java:125) ~[activemq-client-6.1.6.jar:6.1.6]
        at org.apache.activemq.transport.AbstractInactivityMonitor.onCommand(AbstractInactivityMonitor.java:302) ~[activemq-client-6.1.6.jar:6.1.6]
        at org.apache.activemq.transport.TransportSupport.doConsume(TransportSupport.java:83) ~[activemq-client-6.1.6.jar:6.1.6]
        at org.apache.activemq.transport.tcp.TcpTransport.doRun(TcpTransport.java:234) ~[activemq-client-6.1.6.jar:6.1.6]
        at org.apache.activemq.transport.tcp.TcpTransport.run(TcpTransport.java:216) ~[activemq-client-6.1.6.jar:6.1.6]
```
* When using Durable Subscription AND Narayana Transaction Manager, the application fails when sending a JMS message:
```
2025-05-20T11:54:51.865+02:00 ERROR 2767634 --- [demo] [nio-8080-exec-1] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: org.springframework.jms.InvalidClientIDException: Broker: localhost - Client: my-unique-client-id already connected from tcp://192.168.48.1:48538] with root cause

jakarta.jms.InvalidClientIDException: Broker: localhost - Client: my-unique-client-id already connected from tcp://192.168.48.1:48538
        at org.apache.activemq.broker.region.RegionBroker.addConnection(RegionBroker.java:265) ~[na:na]
        at org.apache.activemq.broker.jmx.ManagedRegionBroker.addConnection(ManagedRegionBroker.java:230) ~[na:na]
        at org.apache.activemq.broker.BrokerFilter.addConnection(BrokerFilter.java:99) ~[na:na]
        at org.apache.activemq.advisory.AdvisoryBroker.addConnection(AdvisoryBroker.java:119) ~[na:na]
        at org.apache.activemq.broker.BrokerFilter.addConnection(BrokerFilter.java:99) ~[na:na]
        at org.apache.activemq.broker.BrokerFilter.addConnection(BrokerFilter.java:99) ~[na:na]
        at org.apache.activemq.broker.BrokerFilter.addConnection(BrokerFilter.java:99) ~[na:na]
        at org.apache.activemq.broker.TransportConnection.processAddConnection(TransportConnection.java:854) ~[na:na]
        at org.apache.activemq.broker.jmx.ManagedTransportConnection.processAddConnection(ManagedTransportConnection.java:77) ~[na:na]
        at org.apache.activemq.command.ConnectionInfo.visit(ConnectionInfo.java:139) ~[activemq-client-6.1.6.jar:6.1.6]
        at org.apache.activemq.broker.TransportConnection.service(TransportConnection.java:337) ~[na:na]
        at org.apache.activemq.broker.TransportConnection$1.onCommand(TransportConnection.java:201) ~[na:na]
        at org.apache.activemq.transport.MutexTransport.onCommand(MutexTransport.java:50) ~[activemq-client-6.1.6.jar:6.1.6]
        at org.apache.activemq.transport.WireFormatNegotiator.onCommand(WireFormatNegotiator.java:125) ~[activemq-client-6.1.6.jar:6.1.6]
        at org.apache.activemq.transport.AbstractInactivityMonitor.onCommand(AbstractInactivityMonitor.java:302) ~[activemq-client-6.1.6.jar:6.1.6]
        at org.apache.activemq.transport.TransportSupport.doConsume(TransportSupport.java:83) ~[activemq-client-6.1.6.jar:6.1.6]
        at org.apache.activemq.transport.tcp.TcpTransport.doRun(TcpTransport.java:234) ~[activemq-client-6.1.6.jar:6.1.6]
        at org.apache.activemq.transport.tcp.TcpTransport.run(TcpTransport.java:216) ~[activemq-client-6.1.6.jar:6.1.6]
        at java.lang.Thread.run(Unknown Source) ~[na:na]

```
  * I also tried `org.messaginghub:pooled-jms` with Narayana Transaction Manager, then the message sending and receiving works, but somewhere after the commit the application fails with the following exception and der REST call returns the error page (HTTP 500):
```
2025-05-20T11:56:34.504+02:00 ERROR 2770489 --- [demo] [nio-8080-exec-1] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: org.springframework.jms.connection.SynchedLocalTransactionFailedException: Local JMS transaction failed to commit] with root cause

jakarta.jms.IllegalStateException: The session has already been closed
        at org.messaginghub.pooled.jms.JmsPoolSession.safeGetSessionHolder(JmsPoolSession.java:596) ~[pooled-jms-3.1.7.jar:na]
        at org.messaginghub.pooled.jms.JmsPoolSession.getInternalSession(JmsPoolSession.java:483) ~[pooled-jms-3.1.7.jar:na]
        at org.messaginghub.pooled.jms.JmsPoolSession.commit(JmsPoolSession.java:294) ~[pooled-jms-3.1.7.jar:na]
        at org.springframework.jms.connection.JmsResourceHolder.commitAll(JmsResourceHolder.java:241) ~[spring-jms-6.2.6.jar:6.2.6]
        at org.springframework.jms.connection.ConnectionFactoryUtils$JmsResourceSynchronization.processResourceAfterCommit(ConnectionFactoryUtils.java:439) ~[spring-jms-6.2.6.jar:6.2.6]
        at org.springframework.jms.connection.ConnectionFactoryUtils$JmsResourceSynchronization.processResourceAfterCommit(ConnectionFactoryUtils.java:419) ~[spring-jms-6.2.6.jar:6.2.6]
        at org.springframework.transaction.support.ResourceHolderSynchronization.afterCommit(ResourceHolderSynchronization.java:87) ~[spring-tx-6.2.6.jar:6.2.6]
        at org.springframework.transaction.support.TransactionSynchronizationUtils.invokeAfterCommit(TransactionSynchronizationUtils.java:165) ~[spring-tx-6.2.6.jar:6.2.6]
        at org.springframework.transaction.support.TransactionSynchronizationUtils.triggerAfterCommit(TransactionSynchronizationUtils.java:153) ~[spring-tx-6.2.6.jar:6.2.6]
        at org.springframework.transaction.support.AbstractPlatformTransactionManager.triggerAfterCommit(AbstractPlatformTransactionManager.java:1006) ~[spring-tx-6.2.6.jar:6.2.6]
        at org.springframework.transaction.support.AbstractPlatformTransactionManager.processCommit(AbstractPlatformTransactionManager.java:836) ~[spring-tx-6.2.6.jar:6.2.6]
        at org.springframework.transaction.support.AbstractPlatformTransactionManager.commit(AbstractPlatformTransactionManager.java:758) ~[spring-tx-6.2.6.jar:6.2.6]
        at org.springframework.transaction.interceptor.TransactionAspectSupport.commitTransactionAfterReturning(TransactionAspectSupport.java:698) ~[spring-tx-6.2.6.jar:6.2.6]
        at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:416) ~[spring-tx-6.2.6.jar:6.2.6]
        at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:119) ~[spring-tx-6.2.6.jar:6.2.6]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.2.6.jar:6.2.6]
        at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:728) ~[spring-aop-6.2.6.jar:6.2.6]
        at com.example.demo.UserController$$SpringCGLIB$$0.createUser(<generated>) ~[main/:na]
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
        at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
        at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:258) ~[spring-web-6.2.6.jar:6.2.6]
        at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:191) ~[spring-web-6.2.6.jar:6.2.6]
        at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:118) ~[spring-webmvc-6.2.6.jar:6.2.6]
        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:986) ~[spring-webmvc-6.2.6.jar:6.2.6]
        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:891) ~[spring-webmvc-6.2.6.jar:6.2.6]
        at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87) ~[spring-webmvc-6.2.6.jar:6.2.6]
        at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1089) ~[spring-webmvc-6.2.6.jar:6.2.6]
        at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:979) ~[spring-webmvc-6.2.6.jar:6.2.6]
        at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014) ~[spring-webmvc-6.2.6.jar:6.2.6]
        at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:903) ~[spring-webmvc-6.2.6.jar:6.2.6]
        at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:564) ~[tomcat-embed-core-10.1.40.jar:6.0]
        at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:885) ~[spring-webmvc-6.2.6.jar:6.2.6]
        at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658) ~[tomcat-embed-core-10.1.40.jar:6.0]
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:195) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:51) ~[tomcat-embed-websocket-10.1.40.jar:10.1.40]
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100) ~[spring-web-6.2.6.jar:6.2.6]
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.6.jar:6.2.6]
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93) ~[spring-web-6.2.6.jar:6.2.6]
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.6.jar:6.2.6]
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201) ~[spring-web-6.2.6.jar:6.2.6]
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.6.jar:6.2.6]
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:167) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:90) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:483) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:116) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:93) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:344) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:398) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:903) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1740) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1189) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:658) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:63) ~[tomcat-embed-core-10.1.40.jar:10.1.40]
        at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]
        
        
AND PERIODICALLY:        

2025-05-20T12:00:51.338+02:00  WARN 2770489 --- [demo] [riodic Recovery] com.arjuna.ats.jta                       : ARJUNA016116: Failed to create JMS connection

jakarta.jms.InvalidClientIDException: Broker: localhost - Client: my-unique-client-id already connected from tcp://192.168.48.1:50764
        at org.apache.activemq.broker.region.RegionBroker.addConnection(RegionBroker.java:265) ~[na:na]
        at org.apache.activemq.broker.jmx.ManagedRegionBroker.addConnection(ManagedRegionBroker.java:230) ~[na:na]
        at org.apache.activemq.broker.BrokerFilter.addConnection(BrokerFilter.java:99) ~[na:na]
        at org.apache.activemq.advisory.AdvisoryBroker.addConnection(AdvisoryBroker.java:119) ~[na:na]
        at org.apache.activemq.broker.BrokerFilter.addConnection(BrokerFilter.java:99) ~[na:na]
        at org.apache.activemq.broker.BrokerFilter.addConnection(BrokerFilter.java:99) ~[na:na]
        at org.apache.activemq.broker.BrokerFilter.addConnection(BrokerFilter.java:99) ~[na:na]
        at org.apache.activemq.broker.TransportConnection.processAddConnection(TransportConnection.java:854) ~[na:na]
        at org.apache.activemq.broker.jmx.ManagedTransportConnection.processAddConnection(ManagedTransportConnection.java:77) ~[na:na]
        at org.apache.activemq.command.ConnectionInfo.visit(ConnectionInfo.java:139) ~[activemq-client-6.1.6.jar:6.1.6]
        at org.apache.activemq.broker.TransportConnection.service(TransportConnection.java:337) ~[na:na]
        at org.apache.activemq.broker.TransportConnection$1.onCommand(TransportConnection.java:201) ~[na:na]
        at org.apache.activemq.transport.MutexTransport.onCommand(MutexTransport.java:50) ~[activemq-client-6.1.6.jar:6.1.6]
        at org.apache.activemq.transport.WireFormatNegotiator.onCommand(WireFormatNegotiator.java:125) ~[activemq-client-6.1.6.jar:6.1.6]
        at org.apache.activemq.transport.AbstractInactivityMonitor.onCommand(AbstractInactivityMonitor.java:302) ~[activemq-client-6.1.6.jar:6.1.6]
        at org.apache.activemq.transport.TransportSupport.doConsume(TransportSupport.java:83) ~[activemq-client-6.1.6.jar:6.1.6]
        at org.apache.activemq.transport.tcp.TcpTransport.doRun(TcpTransport.java:234) ~[activemq-client-6.1.6.jar:6.1.6]
        at org.apache.activemq.transport.tcp.TcpTransport.run(TcpTransport.java:216) ~[activemq-client-6.1.6.jar:6.1.6]
        at java.lang.Thread.run(Unknown Source) ~[na:na]

```

* I tried a lot of different configurations (see `application.properties`), also tried to set a dedicated `ConnectionFactory` for the JmsTemplate, which fixed the issue a little bit, but at least I get the periodic error in the logs complaining `InvalidClientIDException: Broker: localhost - Client: my-unique-client-id already connected from tcp://192.168.48.1:45798`.


## How to reproduce the issue

- start activemq via docker compose:
```bash
docker compose up
```
- then start the spring boot application:
```bash
./gradlew bootRun
```
- then trigger a Database insert and a JMS message send via the REST API:
```bash
curl -X GET http://localhost:8080/user/create
```
- in the application logs you can see the errors