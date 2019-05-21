from py4j.java_gateway import *
import os
import json;


def main():
    gateway = JavaGateway()
    google_book = gateway.entry_point.searchGoogleBook("application", 10)
    google_book_obj = json.loads(google_book)
    print(google_book_obj)
    # gateway = JavaGateway.launch_gateway(die_on_exit=True)
    # gateway.start_callback_server()
    # gateway = JavaGateway(gateway_parameters=GatewayParameters())

    # Testing
    # random = gateway.jvm.java.util.Random()
    # number1 = random.nextInt(10)
    # number2 = random.nextInt(10)
    # print(number1, number2)
    # gateway.shutdown()


if __name__ == "__main__":
    main()
