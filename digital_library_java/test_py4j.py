from py4j.java_gateway import *


def main():
    gateway = JavaGateway()  # connect to the JVM
    math_lib = gateway.jvm.java.lang.Math  # create a java.lang.math instance
    x = math_lib.PI  # Call PI value in java
    y = math_lib.toDegrees(x / 2)
    print("PI number:", x)
    print("Degree of PI/2:", y)
    # gateway.shutdown()  # Close the JVM


if __name__ == "__main__":
    main()
