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
package org.apache.camel.component.validator;

import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.support.processor.validation.SchemaReader;

import org.junit.jupiter.api.BeforeEach;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.UnknownHostException;

public abstract class ValidatorDtdAccessAbstractTest extends ContextTestSupport {

    protected MockEndpoint finallyEndpoint;
    protected MockEndpoint invalidEndpoint;
    protected MockEndpoint unknownHostExceptionEndpoint;
    protected MockEndpoint validEndpoint;

    protected String payloud = getPayloudPart("Hello world!");

    protected String ssrfPayloud = "<!DOCTYPE roottag PUBLIC \"-//VSR//PENTEST//EN\" \"http://notex.isting/test\">\n" + payloud;

    protected String xxePayloud
            = "<!DOCTYPE updateProfile [<!ENTITY file SYSTEM \"http://notex.istinghost/test\">]>\n" + getPayloudPart("&file;");

    private String getPayloudPart(String bodyValue) {
        return "<mail xmlns='http://foo.com/bar'><subject>Hey</subject><body>" + bodyValue + "</body></mail>";
    }

    protected abstract boolean accessExternalDTD();

    @Configuration
    class TestConfiguration {
        @Bean
        protected RouteBuilder createRouteBuilder() throws Exception {
            return new RouteBuilder() {

                @Override
                public void configure() throws Exception {
                    if(accessExternalDTD()) {
                        getContext().getGlobalOptions().put(SchemaReader.ACCESS_EXTERNAL_DTD, "true");
                    }
                    from("direct:start").doTry().to("validator:org/apache/camel/component/validator/schema.xsd").to("mock:valid")
                            .doCatch(ValidationException.class).to("mock:invalid")
                            .doCatch(UnknownHostException.class).to("mock:unknownHostException").doFinally().to("mock:finally")
                            .end();
                }
            };
        }
    }

    @BeforeEach
    public void before() {
        validEndpoint = context.getEndpoint("mock:valid", MockEndpoint.class);
        invalidEndpoint = context.getEndpoint("mock:invalid", MockEndpoint.class);
        finallyEndpoint = context.getEndpoint("mock:finally", MockEndpoint.class);
        unknownHostExceptionEndpoint = context.getEndpoint("mock:unknownHostException", MockEndpoint.class);

        validEndpoint.reset();
        invalidEndpoint.reset();
        finallyEndpoint.reset();
        unknownHostExceptionEndpoint.reset();
    }

}
