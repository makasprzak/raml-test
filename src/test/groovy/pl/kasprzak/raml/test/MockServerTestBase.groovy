package pl.kasprzak.raml.test

import org.mockserver.integration.ClientAndServer
import org.mockserver.socket.PortFactory

trait MockServerTestBase {
    def server
    def static randomPort = PortFactory.findFreePort()

    def setup() {
        server = ClientAndServer.startClientAndServer(randomPort)
    }

    def cleanup() {
        server.stop()
    }
}
