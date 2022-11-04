package contracts.song

import org.springframework.cloud.contract.spec.Contract;

Contract.make {
    request {
        method GET()
        url "/songs/1"
    }
    response {
        status OK()
        body(
                name: "We are the champions",
                artist: "Queen",
                album: "News of the world",
                length: "2:59",
                resourceId: 1,
                year: 1977
        )
        headers {
            contentType('application/json')
        }
    }
}
