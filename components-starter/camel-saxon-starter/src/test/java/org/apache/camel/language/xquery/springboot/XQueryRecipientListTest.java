/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.language.xquery.springboot;



import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;

import org.junit.jupiter.api.Test;


import org.apache.camel.test.spring.junit5.CamelSpringBootTest;


@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        XQueryRecipientListTest.class,
        XQueryRecipientListTest.TestConfiguration.class
    }
)
public class XQueryRecipientListTest {
    
    
    @Autowired
    ProducerTemplate template;
    
    @Autowired
    CamelContext context;

    @EndpointInject("mock:foo.London")
    protected MockEndpoint londonEndpoint;   
    
    @EndpointInject("mock:foo.Tampa")
    protected MockEndpoint tampaEndpoint;   
    
    @Test
    public void testSendLondonMessage() throws Exception {
        MockEndpoint.resetMocks(context);
        londonEndpoint.expectedMessageCount(1);
        tampaEndpoint.expectedMessageCount(0);

        template.sendBody("direct:start", "<person name='James' city='London'/>");
        MockEndpoint.assertIsSatisfied(context);
        
    }

    @Test
    public void testSendTampaMessage() throws Exception {
        MockEndpoint.resetMocks(context);
        londonEndpoint.expectedMessageCount(0);
        tampaEndpoint.expectedMessageCount(1);

        template.sendBody("direct:start", "<person name='Hiram' city='Tampa'/>");

        MockEndpoint.assertIsSatisfied(context);
    }
    
    // *************************************
    // Config
    // *************************************

    @Configuration
    public class TestConfiguration {

        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    // TODO is there a nicer way to do this with XQuery?

                    // START SNIPPET: example
                    from("direct:start").recipientList().xquery("concat('mock:foo.', /person/@city)", String.class);
                    // END SNIPPET: example
                }
            };
        }
    }
}
