/*
 * _=_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=
 * Repose
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Copyright (C) 2010 - 2015 Rackspace US, Inc.
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=_
 */

package features.filters.rackspaceauthuser.inputstream

import framework.ReposeValveTest
import org.rackspace.deproxy.Deproxy
import spock.lang.Unroll

import static features.filters.rackspaceauthuser.RackspaceAuthPayloads.*

class PrecededByTranslationFilterTest extends ReposeValveTest {

    def setupSpec() {
        deproxy = new Deproxy()
        deproxy.addEndpoint(properties.targetPort, 'origin service')

        Map params = properties.defaultTemplateParams + ["preceding-filter": "translation"]
        repose.configurationProvider.applyConfigs("common", params)
        repose.configurationProvider.applyConfigs("features/filters/rackspaceauthuser/inputstream", params)
        repose.start()
        repose.waitForNon500FromUrl(reposeEndpoint)
    }

    @Unroll
    def "username is parsed correctly when the translation filter precedes the Rackspace Auth User filter for test #payloadTest.testName"() {
        when:
        def mc = deproxy.makeRequest(
                url: reposeEndpoint,
                requestBody: payloadTest.requestBody,
                headers: payloadTest.contentType,
                method: "POST")

        then:
        mc.handlings[0].request.headers.findAll("x-pp-user").size() == 1
        mc.handlings[0].request.headers.getFirstValue("x-pp-user") == payloadTest.expectedUser + ";q=0.8"
        mc.handlings[0].request.headers.findAll("x-pp-groups").size() == 1
        mc.handlings[0].request.headers.getFirstValue("x-pp-groups") == "Some Group;q=0.8"

        where:
        payloadTest << payloadTests
    }
}
