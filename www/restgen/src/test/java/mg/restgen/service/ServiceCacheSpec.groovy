package mg.restgen.service;

import spock.lang.Specification

class ServiceCacheSpec extends Specification {

    def "test spec" {
        expect:
        name.size() == length

        where:
        name     | length
        "Spock"  | 5
        "Kirk"   | 4
        "Scotty" | 6
    }
}

