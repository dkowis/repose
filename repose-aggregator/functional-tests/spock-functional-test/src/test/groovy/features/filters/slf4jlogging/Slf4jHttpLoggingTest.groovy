package features.filters.slf4jlogging

import framework.ReposeLogSearch
import framework.ReposeValveTest
import org.rackspace.deproxy.Deproxy
import org.rackspace.deproxy.MessageChain
import org.rackspace.deproxy.Response
import spock.lang.Unroll

/**
 * Created by jennyvo on 4/8/14.
 */
class Slf4jHttpLoggingTest extends ReposeValveTest{
    def setupSpec() {
        //remove old log
        def logSearch = new ReposeLogSearch(properties.logFile)
        logSearch.deleteLog()

        def params = properties.defaultTemplateParams
        repose.configurationProvider.applyConfigs("common", params)
        repose.configurationProvider.applyConfigs("features/filters/slf4jhttplogging", params)
        repose.start()
        deproxy = new Deproxy()
        deproxy.addEndpoint(properties.targetPort)

    }

    @Unroll("Test slf4jlog entry with #method")
    def "Test check slf4log for various methods" () {
        def logSearch = new ReposeLogSearch(properties.logFile)
        logSearch.cleanLog()

        when:
        deproxy.makeRequest(url: reposeEndpoint, method: method)

        then:
        logSearch.searchByString("my-test-log  - Remote IP=127.0.0.1 Local IP=127.0.0.1 Request Method=$method").size() == 1
        logSearch.searchByString("my-special-log  - Remote User=null\tURL Path Requested=http://localhost:${properties.targetPort}//\tRequest Protocol=HTTP/1.1").size() == 1

        where:
        method << [
                'GET',
                'POST',
                'PUT',
                'PATCH',
                'HEAD',
                'DELETE'
        ]

    }

    @Unroll("Test slf4jlog entry failed tests with #method and response code #responseCode")
    def "Test slf4j log entry for failed tests"(){
        given:
        def xmlResp = { request -> return new Response(responseCode) }
        def logSearch = new ReposeLogSearch(properties.logFile)
        logSearch.cleanLog()

        when:
        MessageChain mc = deproxy.makeRequest(url: reposeEndpoint, method: method, defaultHandler: xmlResp)

        then:
        logSearch.searchByString("my-test-log  - Remote IP=127.0.0.1 Local IP=127.0.0.1 Request Method=$method Response Code=$responseCode").size() == 1
        logSearch.searchByString("my-special-log  - Remote User=null\tURL Path Requested=http://localhost:${properties.targetPort}//\tRequest Protocol=HTTP/1.1").size() == 1

        where:
        method   | responseCode
        'GET'    | 404
        'POST'   | 404
        'PUT'    | 404
        'PATCH'  | 404
        'HEAD'   | 404
        'DELETE' | 404

    }

    def cleanupSpec() {
        repose.stop()
        deproxy.shutdown()
    }
}
