package me.fapcs.relay_agent

object RelayAgent {

    @JvmStatic
    fun main(args: Array<String>) {
        val javalinService = JavalinService()

        Runtime.getRuntime().addShutdownHook(Thread { javalinService.stop() })
        javalinService.start()
    }

}