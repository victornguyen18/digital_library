from py4j.java_gateway import *


def main():
    gateway = JavaGateway()
    stack = gateway.entry_point.getStack()
    stack.push("First %s" % 'item')
    stack.push("Second item")
    print(stack.pop())
    print(stack.pop())
    gateway.shutdown()


if __name__ == "__main__":
    main()
